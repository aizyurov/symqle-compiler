/*
* Copyright Alexander Izyurov 2010
*/
package org.symqle.model;

import org.symqle.parser.SyntaxTree;
import org.symqle.processor.GrammarException;
import org.symqle.util.Assert;
import org.symqle.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * <br/>13.11.2011
 *
 * @author Alexander Izyurov
 */
public class TypeParameter {
    private final String name;
    private final List<Type> typeBound;

    public TypeParameter(SyntaxTree node) throws GrammarException{
        Assert.assertOneOf(new GrammarException("Unexpected type: "+node.getType(), node), node.getType(), "TypeParameter");
        name = node.find("Identifier").get(0).getValue();
        typeBound = node.find("TypeBound.ClassOrInterfaceType", Type.CONSTRUCT);
    }

    private TypeParameter(final String name, final List<Type> typeBound) {
        this.name = name;
        this.typeBound = typeBound;
    }

    public TypeParameter rename(String newName) {
        return new TypeParameter(newName, new ArrayList<Type>(typeBound));
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final TypeParameter that = (TypeParameter) o;

        if (!name.equals(that.name)) return false;
        if (!typeBound.equals(that.typeBound)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + typeBound.hashCode();
        return result;
    }

    public String toString() {
        return name + Utils.format(typeBound, " extends ", "& ", "");
    }

    public static final F<SyntaxTree, TypeParameter, GrammarException> CONSTRUCT =
            new F<SyntaxTree, TypeParameter, GrammarException>() {
                @Override
                public TypeParameter apply(SyntaxTree syntaxTree) throws GrammarException {
                    return new TypeParameter(syntaxTree);
                }
            };
}


