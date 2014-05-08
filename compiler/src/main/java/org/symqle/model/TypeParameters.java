package org.symqle.model;

import org.symqle.util.Utils;

import java.util.*;

/**
 * Type parameters, JLS 5 8.1.2.
 */
public class TypeParameters {
    private final List<TypeParameter> typeParameters;

    /**
     * Construct from list of type parameter elements.
     * @param typeParameters list of type parameters
     */
    public TypeParameters(final List<TypeParameter> typeParameters) {
        this.typeParameters = new ArrayList<TypeParameter>(typeParameters);
    }

    @Override
    public final String toString() {
        return typeParameters.isEmpty() ? "" : Utils.format(typeParameters, "<", ", ", ">");
    }

    /**
     * As list of TypeParameter.
     * @return immutable list.
     */
    public final List<TypeParameter> list() {
        return Collections.unmodifiableList(typeParameters);
    }

    /**
     * Names of type parameters included to {@code this}.
     * @return set of names
     */
    public final Set<String> names() {
        return new HashSet<String>(Utils.map(typeParameters, new F<TypeParameter, String, RuntimeException>() {
            @Override
            public String apply(final TypeParameter typeParameter) {
                return typeParameter.getName();
            }
        }));
    }

    /**
     * True if there are no parameters.
     * @return true if there are no parameters
     */
    public final boolean isEmpty() {
        return typeParameters.isEmpty();
    }

    /**
     * Number of type parameters.
     * @return non-negative number.
     */
    public final int size() {
        return typeParameters.size();
    }

    /**
     * Convert to TypeArguments.
     * @return constructed TypeArguments instance
     */
    public final TypeArguments asTypeArguments() {
        List<TypeArgument> arguments = new ArrayList<TypeArgument>(typeParameters.size());
        for (TypeParameter typeParameter: typeParameters) {
            arguments.add(new TypeArgument(false, null, new Type(typeParameter.getName())));
        }
        return new TypeArguments(arguments);
    }

    /**
     * Creates a map of (type parameter name, type argument).
     * Infers the type arguments from formal parameter and actual argument type.
     * The keys of the returned map are type parameter names of {@code this}.
     * The values are inferred type arguments. Values may be null if corresponding parameter cannot be inferred.
     * @param formalType formal type
     * @param actualArgType actual type
     * @return mapping type parameter - type argument
     * @throws ModelException wrong model
     */
    public final Map<String, TypeArgument> inferTypeArguments(final Type formalType, final Type actualArgType)
                                                                                throws ModelException {
        final Map<String, TypeArgument> parameterMapping = new HashMap<String, TypeArgument>();
        for (String name: names()) {
            parameterMapping.put(name, null);
        }
        actualArgType.addInferredTypeArguments(formalType, parameterMapping);
        return  parameterMapping;
    }
}
