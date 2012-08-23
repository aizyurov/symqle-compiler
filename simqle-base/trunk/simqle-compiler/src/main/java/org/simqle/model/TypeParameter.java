/*
* Copyright Alexander Izyurov 2010
*/
package org.simqle.model;

import org.simqle.parser.SyntaxTree;
import org.simqle.processor.GrammarException;
import org.simqle.util.Assert;

import java.util.List;
import static org.simqle.util.Utils.*;

/**
 * <br/>13.11.2011
 *
 * @author Alexander Izyurov
 */
public class TypeParameter {
    private final String name;
    private final List<Type> typeBound;
    private final String image;

    public TypeParameter(SyntaxTree node) throws GrammarException{
        Assert.assertOneOf(new GrammarException("Unexpected type: "+node.getType(), node), node.getType(), "TypeParameter");
        name = node.find("Identifier").get(0).getValue();
        typeBound = convertChildren(node, "TypeBound.ClassOrInterfaceType", Type.class);
        image = node.getImage();

    }

    public String getImage() {
        return image;
    }

    public String getName() {
        return name;
    }

    public List<Type> getTypeBound() {
        return typeBound;
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
        return image;
    }
}


