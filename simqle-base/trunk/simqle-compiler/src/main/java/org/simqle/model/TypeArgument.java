/*
* Copyright Alexander Izyurov 2010
*/
package org.simqle.model;

import org.simqle.parser.SyntaxTree;

import java.util.Collections;
import java.util.List;

import static org.simqle.model.Utils.*;

/**
 * <br/>13.11.2011
 *
 * @author Alexander Izyurov
 */
public class TypeArgument {
    final boolean isWildCardArgument;
    // "extends" | "super" | null; in the last case reference should be null
    final String boundType;
    final Type reference;

    public TypeArgument(SyntaxTree node) {
        if (!node.getType().equals("TypeArgument")) {
            throw new IllegalArgumentException("Illegal argument: "+node);
        }
        final List<Type> references = convertChildren(node, "ReferenceType", Type.class);
        if (!references.isEmpty()) {
            isWildCardArgument = false;
            boundType = null;
            reference = references.get(0);
        } else {
            isWildCardArgument = true;
            final List<SyntaxTree> boundTypes = node.find("WildcardBounds.WildcardBoundType");
            boundType = boundTypes.isEmpty() ? null : boundTypes.get(0).getValue();
            List<Type> boundReferences = convertChildren(node, "WildcardBounds.ReferenceType", Type.class);
            reference = boundReferences.isEmpty() ? null : boundReferences.get(0);
        }
    }

    public TypeArgument(String simpleType) {
        isWildCardArgument = false;
        boundType = null;
        reference = new Type(Collections.singletonList(new TypeNameWithTypeArguments(simpleType)), 0);
    }

    public String getValue() {
        return boundType == null ? reference.getImage() : boundType + " " + reference.getImage();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final TypeArgument that = (TypeArgument) o;

        if (isWildCardArgument != that.isWildCardArgument) return false;
        if (boundType != null ? !boundType.equals(that.boundType) : that.boundType != null) return false;
        if (reference != null ? !reference.equals(that.reference) : that.reference != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (isWildCardArgument ? 1 : 0);
        result = 31 * result + (boundType != null ? boundType.hashCode() : 0);
        result = 31 * result + (reference != null ? reference.hashCode() : 0);
        return result;
    }
}
