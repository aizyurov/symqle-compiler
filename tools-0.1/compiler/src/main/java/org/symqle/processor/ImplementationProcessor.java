package org.symqle.processor;

import org.symqle.model.*;
import org.symqle.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: aizyurov
 * Date: 02.12.2013
 * Time: 6:24:14
 * To change this template use File | Settings | File Templates.
 */
public class ImplementationProcessor extends ModelProcessor {

    @Override
    protected Processor predecessor() {
        return new InterfaceEnhancer();
    }

    @Override
    protected void process(Model model) throws ModelException {

        final ClassDefinition symqle;
        try {
            symqle = model.getClassDef("Symqle");
        } catch (ModelException e) {
            throw new IllegalStateException(e);
        }

        for (ClassDefinition classDef : model.getSortedClasses()) {
            implement(classDef, model);
        }

        for (MethodDefinition method: model.getExplicitSymqleMethods()) {
            final AnonymousClass classDef = model.getAnonymousClassByMethod(method);
            if (classDef != null) {
                implement(classDef, model);
                symqle.addMethod(reimplementMethod(method, classDef));
            } else {
                symqle.addMethod(method);
            }
        }
    }

    private MethodDefinition reimplementMethod(MethodDefinition method, AnonymousClass classDef) {
        StringBuilder builder = new StringBuilder();
        builder.append(method.getAccessModifier());
        builder.append(Utils.format(method.getOtherModifiers(), " ", " ", " "));
        builder.append(method.getTypeParameters()).append(" ");
        builder.append(method.getResultType());
        builder.append(" ").append(method.getName());
        builder.append("(").append(Utils.format(method.getFormalParameters(), "", ", ", "")).append(")");
        builder.append(" { ").append(Utils.LINE_BREAK)
                .append("        return new ").append(method.getResultType()).append("()")
        .append(classDef.instanceBodyAsString()).append(";").append(Utils.LINE_BREAK)
        .append("    }").append(Utils.LINE_BREAK);
        return MethodDefinition.parse(builder.toString(), method.getOwner());
    }

    private void implement(AbstractTypeDefinition classDef, Model model) throws ModelException {
        System.err.print("Implementing " + classDef.getName());
        if (classDef.getClass().equals(AnonymousClass.class)) {
            System.err.println(" extends " + ((AnonymousClass)classDef).getExtendsImplements());
        }
        for (MethodDefinition myMethod: classDef.getAllMethods(model)) {
            final Set<String> modifiers = myMethod.getOtherModifiers();
            if (modifiers.contains("abstract") && modifiers.contains("volatile")) {
                System.err.println(classDef.getName() + "implementing " + myMethod.getOtherModifiers() + " " + myMethod.getName());
                List<String> parameters = new ArrayList<String>();
                parameters.add("this");
                parameters.addAll(Utils.map(myMethod.getFormalParameters(), FormalParameter.NAME));
                StringBuilder builder = new StringBuilder();
                builder.append("{ ");
                if (!myMethod.getResultType().equals(Type.VOID)) {
                    builder.append("return ");
                }
                builder.append("Symqle.").append(myMethod.getName()).append("(")
                        .append(Utils.format(parameters, "", ", ", ""))
                        .append("); }");
                myMethod.implement("public", builder.toString(), true, true);
            } else {
                System.err.println(classDef.getName() + "skipping " + myMethod.getOtherModifiers() + " " + myMethod.getName());
            }
        }
    }
}
