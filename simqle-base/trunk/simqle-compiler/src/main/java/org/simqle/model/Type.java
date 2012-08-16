/*
* Copyright Alexander Izyurov 2010
*/
package org.simqle.model;

import org.simqle.parser.SyntaxTree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.simqle.model.Utils.*;

/**
 * <br/>13.11.2011
 *
 * @author Alexander Izyurov
 */
public class Type {

    private final List<TypeNameWithTypeArguments> nameChain;
    private final int arrayDimensions;
    private final String image;

    public Type(SyntaxTree node) {
        final SyntaxTree start = node.getType().equals("Type") ? node.getChildren().get(0) : node;
        
        if (start.getType().equals("ClassOrInterfaceType")) {
            nameChain = convertChildren(start, "IdentifierWithTypeArguments", TypeNameWithTypeArguments.class);
        arrayDimensions = 0;
        } else if (start.getType().equals("ReferenceType")) {
            final SyntaxTree firstChild = start.getChildren().get(0);
            if (firstChild.getType().equals("PrimitiveType")) {
                nameChain = Collections.singletonList(new TypeNameWithTypeArguments(firstChild.getValue()));
            } else /* ClassOrInterfaceType*/{
                nameChain = convertChildren(start, "ClassOrInterfaceType.IdentifierWithTypeArguments", TypeNameWithTypeArguments.class);
            }
            arrayDimensions = start.find("ArrayOf").size();
        } else if (start.getType().equals("PrimitiveType")) {
            nameChain = Collections.singletonList(new TypeNameWithTypeArguments(start.getValue()));
            arrayDimensions = 0;
        } else {
            throw new IllegalArgumentException("Illegal argument: "+start);
        }
        this.image = node.getImage().trim();
    }

    public Type(List<TypeNameWithTypeArguments> nameChain, int arrayDimensions) {
        this.nameChain = new ArrayList<TypeNameWithTypeArguments>(nameChain);
        this.arrayDimensions = arrayDimensions;
        StringBuilder builder = new StringBuilder();
        for (TypeNameWithTypeArguments typeName: nameChain) {
            if (builder.length()>0) {
                builder.append(".");
            }
            builder.append(typeName.getText());
        }
        for (int i=0; i<arrayDimensions; i++) {
            builder.append("[]");
        }
        image = builder.toString();
    }

    public Type arrayOf() {
        return new Type(nameChain, arrayDimensions+1);
    }

    public List<TypeNameWithTypeArguments> getNameChain() {
        return nameChain;
    }

    public int getArrayDimensions() {
        return arrayDimensions;
    }

    public String getImage() {
        return image;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Type type = (Type) o;

        if (arrayDimensions != type.arrayDimensions) return false;
        if (nameChain != null ? !nameChain.equals(type.nameChain) : type.nameChain != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = nameChain != null ? nameChain.hashCode() : 0;
        result = 31 * result + arrayDimensions;
        return result;
    }
}
