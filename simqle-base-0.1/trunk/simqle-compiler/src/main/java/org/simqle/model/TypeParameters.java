package org.simqle.model;

import org.simqle.parser.SyntaxTree;
import org.simqle.processor.GrammarException;
import org.simqle.util.Assert;
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

    public TypeParameters(SyntaxTree node) throws GrammarException {
        Assert.assertOneOf(new GrammarException("Unexpected type: "+node.getType(), node), node.getType(), "TypeParameter");
        this.typeParameters = node.find("TypeParameter", TypeParameter.CONSTRUCT);
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

    public static final F<SyntaxTree, TypeParameters, GrammarException> CONSTRUCT =
            new F<SyntaxTree, TypeParameters, GrammarException>() {
                @Override
                public TypeParameters apply(SyntaxTree syntaxTree) throws GrammarException {
                    return new TypeParameters(syntaxTree);
                }
            };

    public TypeArguments asTypeArguments() {
        List<TypeArgument> arguments = new ArrayList<TypeArgument>(typeParameters.size());
        for (TypeParameter typeParameter: typeParameters) {
            arguments.add(new TypeArgument(false, null, new Type(typeParameter.getName())));
        }
        return new TypeArguments(arguments);
    }
}
