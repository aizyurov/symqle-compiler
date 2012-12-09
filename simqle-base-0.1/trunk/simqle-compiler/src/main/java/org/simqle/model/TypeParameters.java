package org.simqle.model;

import org.simqle.util.Utils;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: aizyurov
 * Date: 24.11.2012
 * Time: 21:19:34
 * To change this template use File | Settings | File Templates.
 */
public class TypeParameters {
    private final List<TypeParameter> typeParameters;

    public TypeParameters(final List<TypeParameter> typeParameters) {
        this.typeParameters = new ArrayList<TypeParameter>(typeParameters);
    }

    public String toString() {
        return typeParameters.isEmpty() ? "" : Utils.format(typeParameters, "<", ", ", ">");
    }

    public List<TypeParameter> list() {
        return typeParameters;
    }

    public Set<String> names() {
        return new HashSet<String>(Utils.map(typeParameters, new F<TypeParameter, String, RuntimeException>() {
            @Override
            public String apply(TypeParameter typeParameter) {
                return typeParameter.getName();
            }
        }));
    }

    public boolean isEmpty() {
        return typeParameters.isEmpty();
    }

    public int size() {
        return typeParameters.size();
    }

    public TypeArguments asTypeArguments() {
        List<TypeArgument> arguments = new ArrayList<TypeArgument>(typeParameters.size());
        for (TypeParameter typeParameter: typeParameters) {
            arguments.add(new TypeArgument(false, null, new Type(typeParameter.getName())));
        }
        return new TypeArguments(arguments);
    }

    /**
     * Creates a map of (type parameter name, type argument).
     * Infers the type arguments from formal parameter and actual argument type.
     * For Simqle, one formal parameter - argument pair suffices, although in general case
     * we could infer from multiple pairs.
     * @param formalType
     * @param actualArgType
     * @return
     * @throws ModelException
     */
    public Map<String, TypeArgument> inferTypeArguments(final Type formalType, final Type actualArgType) throws ModelException {
        final Map<String, TypeArgument> parameterMapping = new HashMap<String, TypeArgument>();
        for (String name: names()) {
            parameterMapping.put(name, null);
        }
        actualArgType.addInferredTypeArguments(formalType, parameterMapping);
        return  parameterMapping;
    }
}
