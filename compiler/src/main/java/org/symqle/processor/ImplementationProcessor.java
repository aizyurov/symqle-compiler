package org.symqle.processor;

import org.symqle.model.*;
import org.symqle.util.Log;
import org.symqle.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * For each class in the model implements all methods, which are not implemented yet and not explicitly abstract.
 * By the time of call all "virtual interfaces" should be added. The methods of these interfaces
 * can be implemented by delegation to proper Symqle methods (poor man's JAVA 8).
 * Then the same is applied to all anonymous classes returned by Symqle factory methods; the methods body are
 * corrected and the methods put to Symqle class (they are detached before).
 */
public class ImplementationProcessor extends ModelProcessor {

    @Override
    protected final Processor predecessor() {
        return new ClassEnhancer();
    }

    @Override
    protected final void process(final Model model) throws ModelException {

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

    private MethodDefinition reimplementMethod(final MethodDefinition method, final AnonymousClass classDef) {
        StringBuilder builder = new StringBuilder();
        builder.append(method.getAccessModifier());
        builder.append(Utils.format(method.getOtherModifiers(), " ", " ", " "));
        builder.append(method.getTypeParameters()).append(" ");
        builder.append(method.getResultType());
        builder.append(" ").append(method.getName());
        builder.append("(").append(Utils.format(method.getFormalParameters(), "", ", ", "")).append(")");
        builder.append(" {").append(Utils.LINE_BREAK)
                .append("        return new ").append(method.getResultType()).append("()")
        .append(classDef.instanceBodyAsString()).append(";").append(Utils.LINE_BREAK)
        .append("    }").append(Utils.LINE_BREAK);
        final MethodDefinition reimplemented = MethodDefinition.parse(builder.toString(), method.getOwner());
        reimplemented.setSourceRef(method.getSourceRef());
        reimplemented.replaceComment(method.getComment());
        return reimplemented;
    }

    private void implement(final AbstractTypeDefinition classDef, final Model model) throws ModelException {
        Log.info("Implementing " + classDef.getName());
        final boolean isAnonymous = classDef.getClass().equals(AnonymousClass.class);
        if (isAnonymous) {
            Log.debug(": " + ((AnonymousClass) classDef).getParent());
        }
        for (MethodDefinition myMethod: classDef.getAllMethods(model)) {
            final Set<String> modifiers = myMethod.getOtherModifiers();
            if (modifiers.contains("abstract") && modifiers.contains("volatile")) {
                Log.debug(classDef.getName() + " implementing " + myMethod.getOtherModifiers()
                        + " " + myMethod.getName());
                List<String> parameters = new ArrayList<String>();
                parameters.add("this");
                parameters.addAll(Utils.map(myMethod.getFormalParameters(), FormalParameter.NAME));
                StringBuilder builder = new StringBuilder();
                builder.append(" {").append(Utils.LINE_BREAK).append("        ");
                if (!myMethod.getResultType().equals(Type.VOID)) {
                    builder.append("return ");
                }
                builder.append("Symqle.").append(myMethod.getName()).append("(")
                        .append(Utils.format(parameters, "", ", ", ""))
                        .append(");").append(Utils.LINE_BREAK).append("    }");
                // anonymous class methods are final because they cannot have descendants
                myMethod.implement("public", builder.toString(), true, !isAnonymous);
            } else {
                Log.debug(classDef.getName() + " skipping " + myMethod.getOtherModifiers() + " " + myMethod.getName());
            }
        }
    }
}
