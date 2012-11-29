/*
* Copyright Alexander Izyurov 2010
*/
package org.simqle.model;

import org.simqle.parser.SyntaxTree;
import org.simqle.processor.GrammarException;
import org.simqle.util.Assert;
import org.simqle.util.Utils;

import java.util.*;

/**
 * <br/>13.11.2011
 *
 * @author Alexander Izyurov
 */
public class Type {

    public final static Type VOID = new Type(Collections.singletonList(new TypeNameWithTypeArguments("void")),0);

    private final List<TypeNameWithTypeArguments> nameChain;
    private final int arrayDimensions;

    public Type(SyntaxTree node) throws GrammarException {
        final SyntaxTree start = node.getType().equals("Type") ? node.getChildren().get(0) : node;

        Assert.assertOneOf(new GrammarException("Unexpected type: "+node.getType(), node), start.getType(), "ClassOrInterfaceType", "ReferenceType", "PrimitiveType", "ExceptionType");
        
        if (start.getType().equals("ClassOrInterfaceType")) {
            nameChain = start.find("IdentifierWithTypeArguments", TypeNameWithTypeArguments.CONSTRUCT);
        arrayDimensions = 0;
        } else if (start.getType().equals("ReferenceType")) {
            final SyntaxTree firstChild = start.getChildren().get(0);
            if (firstChild.getType().equals("PrimitiveType")) {
                nameChain = Collections.singletonList(new TypeNameWithTypeArguments(firstChild.getValue()));
            } else /* ClassOrInterfaceType*/{
                nameChain = start.find("ClassOrInterfaceType.IdentifierWithTypeArguments", TypeNameWithTypeArguments.CONSTRUCT);
            }
            arrayDimensions = start.find("ArrayOf").size();
        } else if (start.getType().equals("ExceptionType")) {
            nameChain = start.find("Name.Identifier", TypeNameWithTypeArguments.CONSTRUCT);
            arrayDimensions = 0;
        } else { /* PrimitiveType */
            nameChain = Collections.singletonList(new TypeNameWithTypeArguments(start.getValue()));
            arrayDimensions = 0;
        }
    }

    public Type(String name) {
        this(Collections.singletonList(new TypeNameWithTypeArguments(name)), 0);
    }

    public Type(List<TypeNameWithTypeArguments> nameChain, int arrayDimensions) {
        this.nameChain = new ArrayList<TypeNameWithTypeArguments>(nameChain);
        this.arrayDimensions = arrayDimensions;
    }

    public Type arrayOf() {
        return new Type(nameChain, arrayDimensions+1);
    }

    public List<TypeNameWithTypeArguments> getNameChain() {
        return nameChain;
    }

    public TypeArguments getTypeArguments() {
        final List<TypeArgument> typeArguments = new LinkedList<TypeArgument>();
        TypeNameWithTypeArguments element = nameChain.get(nameChain.size()-1);
            typeArguments.addAll(element.getTypeArguments().getArguments());
        return new TypeArguments(typeArguments);
    }

    public int getArrayDimensions() {
        return arrayDimensions;
    }

    public String erasure(final Set<String> typeParameterNames) {
        return format(new F<TypeNameWithTypeArguments, String, RuntimeException>() {
            @Override
            public String apply(TypeNameWithTypeArguments typeNameWithTypeArguments) {
                String name = typeNameWithTypeArguments.getName();
                return typeParameterNames.contains(name) ? "Object" : name;
            }
        });

    }

    private String format(F<TypeNameWithTypeArguments, String, RuntimeException> typeFormatter) {
        StringBuilder builder = new StringBuilder();
        builder.append(Utils.format(nameChain, "", ".", "", typeFormatter));
        for (int i=0; i<arrayDimensions; i++) {
            builder.append("[]");
        }
        return builder.toString();
    }

    public String toString() {
        return format(new F<TypeNameWithTypeArguments, String, RuntimeException>() {
            @Override
            public String apply(TypeNameWithTypeArguments typeNameWithTypeArguments) {
                return typeNameWithTypeArguments.toString();
            }
        });
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Type type = (Type) o;

        return arrayDimensions==type.arrayDimensions
                && nameChain.equals(type.nameChain);
    }

    @Override
    public int hashCode() {
        int result = nameChain != null ? nameChain.hashCode() : 0;
        result = 31 * result + arrayDimensions;
        return result;
    }

    public Type substituteParameters(TypeParameters typeParameters, TypeArguments typeArguments) throws ModelException {
        final List<TypeParameter> parameters = typeParameters.list();
        List<TypeArgument> arguments = typeArguments.getArguments();
        if (arguments.size()!=parameters.size()) {
            throw new ModelException("Sizes do not match: parameters: "+parameters.size()+", arguments: "+ arguments.size());
        }
        final Map<String, TypeArgument> substitutions = new HashMap<String, TypeArgument>(parameters.size());
        for (int i=0; i<parameters.size(); i++) {
            TypeArgument argument = arguments.get(i);
            substitutions.put(parameters.get(i).getName(), argument);
        }
        String myName = nameChain.get(0).getName();
        if (nameChain.size()==1 && substitutions.containsKey(myName)) {
            // I am a parameter; replace me
            return substitutions.get(myName).asType();
        } else {
            List<TypeNameWithTypeArguments> newNameChain = new ArrayList<TypeNameWithTypeArguments>(nameChain.size());
            for (TypeNameWithTypeArguments typeWithArgs : nameChain) {
                // name cannot be a parameter (except the if branch above)
                // so it is just copied
                // arguments may contain parameters, so replace recursively
                String name = typeWithArgs.getName();
                TypeArguments newTypeArguments = typeWithArgs.getTypeArguments().substituteParameters(typeParameters, typeArguments);
                newNameChain.add(new TypeNameWithTypeArguments(name, newTypeArguments));
            }
            return new Type(newNameChain, arrayDimensions);
        }
    }

    public String getSimpleName() {
        return nameChain.get(nameChain.size()-1).getName();
    }



    public static final F<SyntaxTree, Type, GrammarException> CONSTRUCT =
            new F<SyntaxTree, Type, GrammarException>() {
                @Override
                public Type apply(SyntaxTree syntaxTree) throws GrammarException {
                    return new Type(syntaxTree);
                }
            };
}
