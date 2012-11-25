/*
* Copyright Alexander Izyurov 2010
*/
package org.simqle.model;

import org.simqle.parser.SyntaxTree;
import org.simqle.processor.GrammarException;
import org.simqle.util.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.simqle.util.Utils.*;


/**
 * <br/>13.11.2011
 *
 * @author Alexander Izyurov
 */
public class TypeNameWithTypeArguments {
    private final String name;
    private final TypeArguments typeArguments;


    public TypeNameWithTypeArguments(SyntaxTree node)  throws GrammarException {
        Assert.assertOneOf(new GrammarException("Unexpected type: "+node.getType(), node), node.getType(), "IdentifierWithTypeArguments", "Identifier");
        typeArguments = node.find("TypeArguments", TypeArguments.CONSTRUCT).get(0);
        name = node.getType().equals("IDentifier") ?
                node.getValue() :
                node.find("Identifier").get(0).getValue();
    }

    public TypeNameWithTypeArguments(final String classOrVoidName, final TypeArguments typeArguments) {
        this.name = classOrVoidName;
        this.typeArguments = typeArguments;
        StringBuilder textBuilder = new StringBuilder();
        textBuilder.append(classOrVoidName);
    }

    public TypeNameWithTypeArguments(final String classOrVoidName) {
        this(classOrVoidName, TypeArguments.empty());
    }

    public String getName() {
        return name;
    }

    public TypeArguments getTypeArguments() {
        return typeArguments;
    }

    @Override
    public String toString() {
        return name+typeArguments;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final TypeNameWithTypeArguments that = (TypeNameWithTypeArguments) o;

        return name.equals(that.name)
                && typeArguments.equals(that.typeArguments);
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (typeArguments != null ? typeArguments.hashCode() : 0);
        return result;
    }

    public static final F<SyntaxTree, TypeNameWithTypeArguments, GrammarException> CONSTRUCT =
            new F<SyntaxTree, TypeNameWithTypeArguments, GrammarException>() {
                @Override
                public TypeNameWithTypeArguments apply(SyntaxTree syntaxTree) throws GrammarException {
                    return new TypeNameWithTypeArguments(syntaxTree);
                }
            };
}
