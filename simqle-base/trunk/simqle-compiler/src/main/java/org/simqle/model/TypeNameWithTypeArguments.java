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
public class TypeNameWithTypeArguments {
    private final String name;
    private final List<TypeArgument> typeArguments;
    private final String text;


    public TypeNameWithTypeArguments(SyntaxTree node) {
        if (!node.getType().equals("IdentifierWithTypeArguments")) {
            throw new IllegalArgumentException("Illegal argument: "+node);
        }
        typeArguments = convertChildren(node, "TypeArguments.TypeArgument", TypeArgument.class);
        name = node.find("Identifier").get(0).getValue();
        text = node.getImage();
    }

    public TypeNameWithTypeArguments(final String classOrVoidName, final List<String> typeArguments) {
        this.name = classOrVoidName;
        this.typeArguments = new ArrayList<TypeArgument>(typeArguments.size());
        for (String arg: typeArguments) {
            this.typeArguments.add(new TypeArgument(arg));
        }
        StringBuilder textBuilder = new StringBuilder();
        textBuilder.append(classOrVoidName);
        if (!typeArguments.isEmpty()) {
            textBuilder.append("<");
            for (int i=0; i<typeArguments.size(); i++) {
                if (i>0) {
                    textBuilder.append(",");
                }
                textBuilder.append(typeArguments.get(i));
            }
            textBuilder.append(">");
        }
        text = textBuilder.toString();
    }
    public TypeNameWithTypeArguments(final String classOrVoidName) {
        this(classOrVoidName, Collections.<String>emptyList());
    }

    public String getName() {
        return name;
    }

    public List<TypeArgument> getTypeArguments() {
        return typeArguments;
    }

    public String getText() {
        return text;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final TypeNameWithTypeArguments that = (TypeNameWithTypeArguments) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (typeArguments != null ? !typeArguments.equals(that.typeArguments) : that.typeArguments != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (typeArguments != null ? typeArguments.hashCode() : 0);
        return result;
    }
}
