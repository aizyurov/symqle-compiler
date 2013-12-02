package org.symqle.processor;

import org.symqle.model.*;
import org.symqle.util.Utils;

import java.io.BufferedReader;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author lvovich
 */
public class ClassEnhancer extends ModelProcessor {

    @Override
    public void process(final Model model) throws ModelException {
        for (ClassDefinition classDef: model.getAllClasses()) {
            enhanceClass(classDef, model);
            classDef.makeAbstractIfNeeded(model);
        }
    }

    private void enhanceClass(final ClassDefinition classDefinition, final Model model) throws ModelException {
        for (MethodDefinition method: model.getExplicitSymqleMethods()) {
            String accessModifier = method.getAccessModifier();
            // package scope methods of Symqle get translated to public methods of classes and interfaces
            if (accessModifier.equals("private") || accessModifier.equals("protected")) {
                continue;
            }
            final List<FormalParameter> formalParameters = method.getFormalParameters();
            if (formalParameters.isEmpty()) {
                continue;
            }
            final Type firstArgType = formalParameters.get(0).getType();
            Type myType = classDefinition.getType();
            // names must match
            if (!myType.getSimpleName().equals(firstArgType.getSimpleName())) {
                continue;
            }
            final Map<String, TypeArgument> mapping;
            try {
                mapping = method.getTypeParameters().inferTypeArguments(firstArgType, myType);
            } catch (ModelException e) {
                // cannot infer type parameter values; skip this method
                continue;
            }
            if (firstArgType.replaceParams(mapping).equals(myType) ||
            (firstArgType.getTypeArguments().getArguments().size()==1
                    && firstArgType.getTypeArguments().getArguments().get(0).isWildCardArgument()
                    && myType.getTypeArguments().getArguments().size() == 1)) {
                // special case: both have a single parameter, which is wildcard in firstArg,
                // so types match
                final MethodDefinition newMethod = createMyMethod(classDefinition, method, myType, mapping);
                classDefinition.addMethod(newMethod);
            }
        }
        classDefinition.addImportLines(Arrays.asList("import org.symqle.common.*;"));
    }

    private MethodDefinition createMyMethod(final ClassDefinition classDef, MethodDefinition symqleMethod, Type myType, final Map<String, TypeArgument> mapping) {
        final List<TypeParameter> myTypeParameterList = new ArrayList<TypeParameter>();
        // skip parameters, which are in mapping: they are inferred
        for (TypeParameter typeParameter: symqleMethod.getTypeParameters().list()) {
            if (mapping.get(typeParameter.getName()) == null) {
                // TODO new type parameter may hide class type parameter; rename if necessary
                myTypeParameterList.add(typeParameter);
            }
        }
        final TypeParameters myTypeParameters = new TypeParameters(myTypeParameterList);
        List<FormalParameter> myFormalParameters = new ArrayList<FormalParameter>();
        final List<FormalParameter> symqleFormalParameters = symqleMethod.getFormalParameters();
        for (int i=1; i< symqleFormalParameters.size(); i++) {
            final FormalParameter symqleFormalParameter = symqleFormalParameters.get(i);
            myFormalParameters.add(symqleFormalParameter.replaceParams(mapping));
        }
        final StringBuilder builder = new StringBuilder();
        builder.append(symqleMethod.getComment());
        builder.append(myTypeParameters)
                .append(symqleMethod.getResultType().replaceParams(mapping))
                .append(" ")
                .append(symqleMethod.getName())
                .append("(")
                .append(Utils.format(myFormalParameters, "", ", ", ""))
                .append(")")
                .append(Utils.format(symqleMethod.getThrownExceptions(), " throws ", ", ", ""))
                .append(" {")
                .append(Utils.LINE_BREAK)
                .append("    return Symqle.")
                .append(symqleMethod.getName())
                .append("(")
                .append(Utils.format(myFormalParameters, "", ", ", "", new F<FormalParameter, String, RuntimeException>() {
                    @Override
                    public String apply(FormalParameter formalParameter) {
                        return formalParameter.getName();
                    }
                }))
                .append(");")
                .append("    }");

        final String body = builder.toString();
        final MethodDefinition method = MethodDefinition.parse(body, classDef);
        method.setSourceRef(symqleMethod.getSourceRef());
        return method;
    }


}
