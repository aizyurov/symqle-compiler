/*
* Copyright Alexander Izyurov 2010
*/
package org.simqle.model;

import org.simqle.parser.SyntaxTree;
import org.simqle.processor.GrammarException;
import org.simqle.util.Assert;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <br/>13.11.2011
 *
 * @author Alexander Izyurov
 */
public class Type {

    public final static Type VOID = new Type(new TypeNameWithTypeArguments("void"),0);

    private final TypeNameWithTypeArguments nameChain;
    private final int arrayDimensions;

    public Type(SyntaxTree node) throws GrammarException {
        final SyntaxTree start = node.getType().equals("Type") ? node.getChildren().get(0) : node;

        Assert.assertOneOf(new GrammarException("Unexpected type: "+node.getType(), node), start.getType(), "ClassOrInterfaceType", "ReferenceType", "PrimitiveType", "ExceptionType");
        
        if (start.getType().equals("ClassOrInterfaceType")) {
            final List<TypeNameWithTypeArguments> chain = start.find("IdentifierWithTypeArguments", TypeNameWithTypeArguments.CONSTRUCT);
            if (chain.size()>1) {
                throw new GrammarException("Simqle supports only simple type names", start);
            }
            nameChain = chain.get(0);
        arrayDimensions = 0;
        } else if (start.getType().equals("ReferenceType")) {
            final SyntaxTree firstChild = start.getChildren().get(0);
            if (firstChild.getType().equals("PrimitiveType")) {
                nameChain = new TypeNameWithTypeArguments(firstChild.getValue());
            } else /* ClassOrInterfaceType*/{
                final List<TypeNameWithTypeArguments> chain = start.find("ClassOrInterfaceType.IdentifierWithTypeArguments", TypeNameWithTypeArguments.CONSTRUCT);
                if (chain.size()>1) {
                    throw new GrammarException("Simqle supports only simple type names", start);
                }
                nameChain = chain.get(0);
            }
            arrayDimensions = start.find("ArrayOf").size();
        } else if (start.getType().equals("ExceptionType")) {
            final List<TypeNameWithTypeArguments> chain = start.find("Name.Identifier", TypeNameWithTypeArguments.CONSTRUCT);
            if (chain.size()>1) {
                throw new GrammarException("Simqle supports only simple type names", start);
            }
            nameChain = chain.get(0);
            arrayDimensions = 0;
        } else { /* PrimitiveType */
            nameChain = new TypeNameWithTypeArguments(start.getValue());
            arrayDimensions = 0;
        }
    }

    public Type(String name) {
        this(new TypeNameWithTypeArguments(name), 0);
    }

    public Type(TypeNameWithTypeArguments nameChain, int arrayDimensions) {
        this.nameChain = nameChain;
        this.arrayDimensions = arrayDimensions;
    }

    public Type arrayOf() {
        return new Type(nameChain, arrayDimensions+1);
    }

    public TypeNameWithTypeArguments getNameChain() {
        return nameChain;
    }

    public TypeArguments getTypeArguments() {
        final List<TypeArgument> typeArguments = new LinkedList<TypeArgument>();
        typeArguments.addAll(nameChain.getTypeArguments().getArguments());
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
        builder.append(typeFormatter.apply(nameChain));
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

    public Type replaceParams(final Map<String, TypeArgument> mapping) {
        final TypeArgument typeArgument = mapping.get(getSimpleName());
        if (typeArgument != null) {
            return typeArgument.asType();
        } else {
            // replace my type arguments
            return new Type(new TypeNameWithTypeArguments(this.getSimpleName(), this.getTypeArguments().replaceParams(mapping)), this.arrayDimensions);
        }
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
        String myName = nameChain.getName();
        if (substitutions.containsKey(myName)) {
            // I am a parameter; replace me
            return substitutions.get(myName).asType();
        } else {
            // name cannot be a parameter (except the if branch above)
                // so it is just copied
                // arguments may contain parameters, so replace recursively
                String name = nameChain.getName();
                TypeArguments newTypeArguments = nameChain.getTypeArguments().substituteParameters(typeParameters, typeArguments);
            final TypeNameWithTypeArguments newNameChain = new TypeNameWithTypeArguments(name, newTypeArguments);
            return new Type(newNameChain, arrayDimensions);
        }
    }

    public String getSimpleName() {
        return nameChain.getName();
    }

    public void addInferredTypeArguments(final Type formalType, final Map<String, TypeArgument> parameterMapping) throws ModelException {
        String name = formalType.getSimpleName();
        if (parameterMapping.containsKey(name)) {
            TypeArgument oldMapping = parameterMapping.put(name, new TypeArgument(false, null, this));
            if (oldMapping!=null && !oldMapping.equals(formalType)) {
                throw new ModelException("Cannot infer"+name);
            }
        } else {
            // we do not process inheritance: actual type should exactly match to formal type for
            // Simqle to be able to infer type parameters
            // if a formal type is Collection<T> and actual type is MyListOfLong implements Collection<Long>,
            // Simqle will give up.
            if (!this.getSimpleName().equals(formalType.getSimpleName())) {
                throw new ModelException(
                        "Cannot infer type arguments from: "+this+" <- " +formalType);
            }
            final List<TypeArgument> actualArguments = this.getTypeArguments().getArguments();
            final List<TypeArgument> formalArguments = formalType.getTypeArguments().getArguments();
            if (actualArguments.size() != formalArguments.size()) {
                throw new ModelException("formal parameter type arguments differ from actual parameter type arguments");
            }
            for (int i=0; i<actualArguments.size(); i++) {
                actualArguments.get(i).addInferredTypeArguments(formalArguments.get(i), parameterMapping);
            }
        }
    }



    public static final F<SyntaxTree, Type, GrammarException> CONSTRUCT =
            new F<SyntaxTree, Type, GrammarException>() {
                @Override
                public Type apply(SyntaxTree syntaxTree) throws GrammarException {
                    return new Type(syntaxTree);
                }
            };
}
