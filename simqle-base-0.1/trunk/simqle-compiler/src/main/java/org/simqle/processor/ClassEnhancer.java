package org.simqle.processor;

import org.simqle.model.*;
import org.simqle.util.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author lvovich
 */
public class ClassEnhancer implements ModelProcessor {

    @Override
    public void process(final Model model) throws ModelException {
        for (ClassDefinition classDef: model.getAllClasses()) {
            enhanceClass(classDef, model);
        }
    }

    private void enhanceClass(final ClassDefinition classDef, final Model model) throws ModelException {
        Map<String, MethodTemplate> generatedMethods = new HashMap<String, MethodTemplate>();
        Set<String> ambiguousMethods = new HashSet<String>();
        for (MethodDefinition method: model.getExplicitSimqleMethods()) {
            final MethodTemplate methodTemplate = tryAddMethod(classDef, method, model);
            if (methodTemplate != null) {
                final String signature = methodTemplate.myAbstractMethod.signature();
                if (generatedMethods.containsKey(signature)) {
                    // TODO method conflict
                    // for now keep the first one
                    System.err.println("WARN: conflicting methods; keep: "+generatedMethods.get(signature).myAbstractMethod.declaration()+
                    " throw away: "+methodTemplate.myAbstractMethod.declaration());
                    ambiguousMethods.add(signature);
                } else {
                    generatedMethods.put(signature, methodTemplate);
                }
            }
            classDef.addImportLines(model.getImportsForExplicitMethod(method));
            classDef.ensureRequiredImports(model);
        }
        // generate real methods
        for (Map.Entry<String, MethodTemplate> entry: generatedMethods.entrySet()) {
                final MethodDefinition myAbstractMethod = entry.getValue().myAbstractMethod;
                List<String> parameters = new ArrayList<String>(Utils.map(myAbstractMethod.getFormalParameters(), FormalParameter.NAME));
                if (ambiguousMethods.contains(entry.getKey())) {
                    // must explicitly cast to myType
                    parameters.add(0, "("+entry.getValue().myType+") this");
                } else {
                    parameters.add(0, "this");
                }

                ((MethodDefinition) myAbstractMethod).implement(myAbstractMethod.getAccessModifier(), " {" + Utils.LINE_BREAK +
                        "        " +
                        (myAbstractMethod.getResultType().equals(Type.VOID) ? "" : "return ")+
                        "Simqle.get()." +
                        myAbstractMethod.getName() +
                        "(" +
                        Utils.format(parameters, "", ", ", "") +
                        ");" + Utils.LINE_BREAK +
                        "    }"
                );
        }
    }

    private MethodTemplate tryAddMethod(final ClassDefinition classDef, final MethodDefinition method, final Model model) {
        String accessModifier = method.getAccessModifier();
        if (accessModifier.equals("private") || accessModifier.equals("protected")) {
            return null;
        }
        final List<FormalParameter> formalParameters = method.getFormalParameters();
        if (formalParameters.isEmpty()) {
            return null;
        }
        final Type firstArgType = formalParameters.get(0).getType();

        for (Type myType: classDef.getImplementedInterfaces()) {
            // names must match
            if (!myType.getSimpleName().equals(firstArgType.getSimpleName())) {
                continue;
            }
            final Map<String, TypeArgument> mapping;
            try {
                mapping = method.getTypeParameters().inferTypeArguments(firstArgType, myType);
            } catch (ModelException e) {
                // cannot infer type parameter values; skip this method
                return null;
            }
            if (firstArgType.replaceParams(mapping).equals(myType) ||
            (firstArgType.getTypeArguments().getArguments().size()==1
                    && firstArgType.getTypeArguments().getArguments().get(0).isWildCardArgument()
                    && myType.getTypeArguments().getArguments().size() == 1)) {
                // special case: both have a single parameter, which is wildcard in firstArg,
                // so types match
                return new MethodTemplate(createMyMethod(classDef, method, myType, mapping), myType);

            } else {
                return null;
            }
        }
        // no type matches
        return null;
    }

    private static class MethodTemplate {
        private final MethodDefinition myAbstractMethod;
        private final Type myType;

        private MethodTemplate(final MethodDefinition myAbstractMethod, final Type myType) {
            this.myAbstractMethod = myAbstractMethod;
            this.myType = myType;
        }
    }


    private MethodDefinition createMyMethod(final ClassDefinition classDef, MethodDefinition simqleMethod, Type myType, final Map<String, TypeArgument> mapping) {
        final List<TypeParameter> myTypeParameterList = new ArrayList<TypeParameter>();
        // skip parameters, which are in mapping: they are inferred
        for (TypeParameter typeParameter: simqleMethod.getTypeParameters().list()) {
            if (!mapping.containsKey(typeParameter.getName())) {
                // TODO new type parameter may hide class type parameter; rename if necessary
                myTypeParameterList.add(typeParameter);
            }
        }
        final TypeParameters myTypeParameters = new TypeParameters(myTypeParameterList);
        List<FormalParameter> myFormalParameters = new ArrayList<FormalParameter>();
        final List<FormalParameter> simqleFormalParameters = simqleMethod.getFormalParameters();
        for (int i=1; i< simqleFormalParameters.size(); i++) {
            final FormalParameter simqleFormalParameter = simqleFormalParameters.get(i);
            myFormalParameters.add(simqleFormalParameter.replaceParams(mapping));
        }
        Set<String> myModifiers = new HashSet<String>(simqleMethod.getOtherModifiers());
        myModifiers.add("abstract");
        final StringBuilder builder = new StringBuilder();
        builder.append(Utils.LINE_BREAK).append("    ");
        builder.append(simqleMethod.getAccessModifier())
                .append(" ")
                .append(Utils.format(simqleMethod.getOtherModifiers(), "", " ", " "))
                .append(myTypeParameters)
                .append(simqleMethod.getResultType().replaceParams(mapping))
                .append(" ")
                .append(simqleMethod.getName())
                .append("(")
                .append(Utils.format(myFormalParameters, "", ", ", ""))
                .append(")")
                .append(Utils.format(simqleMethod.getThrownExceptions(), " throws ", ", ", ""))
                .append(";");
        final String body = builder.toString();
        return MethodDefinition.parse(body, classDef);
    }


}
