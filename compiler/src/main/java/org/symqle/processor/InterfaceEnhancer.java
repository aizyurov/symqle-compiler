package org.symqle.processor;

import org.symqle.model.*;
import org.symqle.util.Log;
import org.symqle.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Adds to interface methods, which can be delegated to Symqle static methods.
 * In Java 8, the methods would have default implementations.
 */
public class InterfaceEnhancer extends ModelProcessor {

    @Override
    protected final Processor predecessor() {
        return new InheritanceProcessor();
    }

    @Override
    protected final void process(final Model model) throws ModelException {
        Log.debug("All interfaces: "
                + Utils.map(model.getAllInterfaces(), new F<InterfaceDefinition, String, RuntimeException>() {
            @Override
            public String apply(final InterfaceDefinition o) {
                return o.getName();
            }
        }));
        for (InterfaceDefinition interfaceDefinition: model.getAllInterfaces()) {
            enhanceInterface(interfaceDefinition, model);
        }
    }

    private void enhanceInterface(final InterfaceDefinition interfaceDefinition,
                                  final Model model) throws ModelException {
        for (MethodDefinition method: model.getExplicitSymqleMethods()) {
            if (!model.isUnambiguous(method)) {
                continue;
            }
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
            Type myType = interfaceDefinition.getType();
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
            if (firstArgType.replaceParams(mapping).equals(myType)
                    || firstArgType.getTypeArguments().getArguments().size() == 1
                    && firstArgType.getTypeArguments().getArguments().get(0).isWildCardArgument()
                    && myType.getTypeArguments().getArguments().size() == 1) {
                // special case: both have a single parameter, which is wildcard in firstArg,
                // so types match
                final MethodDefinition newMethod = createMyMethod(interfaceDefinition, method, mapping);
                // remove comment from Symqle: moved to interface.
                method.replaceComment("    /**" + Utils.LINE_BREAK
                        + "     *  see {@link " + interfaceDefinition.getName() + "#" + newMethod.signature() + "}"
                        + Utils.LINE_BREAK + "     */" + Utils.LINE_BREAK);
                try {
                    interfaceDefinition.addDelegateMethod(newMethod);
                } catch (ModelException e) {
                    Log.debug("Explicit methods are " + model.getExplicitSymqleMethods());
                    throw new RuntimeException("Internal error", e);
                }
            }
        }
    }

    private MethodDefinition createMyMethod(final InterfaceDefinition interfaceDefinition,
                                            final MethodDefinition symqleMethod,
                                            final Map<String, TypeArgument> mapping) {
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
        for (int i = 1; i < symqleFormalParameters.size(); i++) {
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
                .append(";");
        final String body = builder.toString();
        final MethodDefinition method = MethodDefinition.parse(body, interfaceDefinition);
        method.setSourceRef(symqleMethod.getSourceRef());
        return method;
    }


}
