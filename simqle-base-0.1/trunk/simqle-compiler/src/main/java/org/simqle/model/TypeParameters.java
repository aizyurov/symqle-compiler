package org.simqle.model;

import org.simqle.util.Utils;

import java.util.ArrayList;
import java.util.List;

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
}
