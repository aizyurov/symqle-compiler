/*
   Copyright 2011-2014 Alexander Izyurov

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.package org.symqle.common;
*/

package org.symqle.model;

import org.symqle.parser.SyntaxTree;
import org.symqle.processor.GrammarException;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Java type. Object or primitive type.
 * @author Alexander Izyurov
 */
public class Type {

    /**
     * Used as return type for void methods.
     */
    public static final Type VOID = new Type("void");

    private final String name;
    private final TypeArguments typeArguments;

    private final int arrayDimensions;

    /**
     * Construct from AST.
     * @param node syntax tree
     * @throws GrammarException wrong tree
     */
    public Type(final SyntaxTree node) throws GrammarException {
        final SyntaxTree start = node.getType().equals("Type") ? node.getChildren().get(0) : node;

        AssertNodeType.assertOneOf(start,
                "ClassOrInterfaceType", "ReferenceType", "PrimitiveType", "ExceptionType");

        if (start.getType().equals("ClassOrInterfaceType")) {
            final List<SyntaxTree> chain = start.find("IdentifierWithTypeArguments");
            if (chain.size() > 1) {
                throw new GrammarException("Symqle supports only simple type names", start);
            }
            final SyntaxTree identifierWithTypeArgumentsNode = chain.get(0);
            name = identifierWithTypeArgumentsNode.find("Identifier", SyntaxTree.VALUE).get(0);
            typeArguments = new TypeArguments(identifierWithTypeArgumentsNode.find("TypeArguments.TypeArgument",
                    TypeArgument.CONSTRUCT));
            arrayDimensions = 0;
        } else if (start.getType().equals("ReferenceType")) {
            final SyntaxTree firstChild = start.getChildren().get(0);
            if (firstChild.getType().equals("PrimitiveType")) {
                name = firstChild.getValue();
                typeArguments = new TypeArguments();
            } else /* ClassOrInterfaceType*/ {
                final List<SyntaxTree> chain = start.find("ClassOrInterfaceType.IdentifierWithTypeArguments");
                if (chain.size() > 1) {
                    throw new GrammarException("Symqle supports only simple type names", start);
                }
                final SyntaxTree identifierWithTypeArgumentsNode = chain.get(0);
                name = identifierWithTypeArgumentsNode.find("Identifier", SyntaxTree.VALUE).get(0);
                typeArguments = new TypeArguments(identifierWithTypeArgumentsNode.find("TypeArguments.TypeArgument",
                        TypeArgument.CONSTRUCT));
            }
            arrayDimensions = start.find("ArrayOf").size();
        } else if (start.getType().equals("ExceptionType")) {
            final List<SyntaxTree> chain = start.find("Name.Identifier");
            if (chain.size() > 1) {
                throw new GrammarException("Symqle supports only simple type names", start);
            }
            name = chain.get(0).getValue();
            typeArguments = new TypeArguments();
            arrayDimensions = 0;
        } else { /* PrimitiveType */
            name = start.getValue();
            typeArguments = new TypeArguments();
            arrayDimensions = 0;
        }
    }

    /**
     * Construct from name. No type arguments, no arrays.
     * @param name plain class name or primitive type name
     */
    public Type(final String name) {
        this(name, new TypeArguments(), 0);
    }

    /**
     * Generic constructor.
     * @param name type name
     * @param typeArguments type arguments
     * @param arrayDimensions number of following []'s
     */
    public Type(final String name, final TypeArguments typeArguments, final int arrayDimensions) {
        this.name = name;
        this.typeArguments = typeArguments;
        this.arrayDimensions = arrayDimensions;
    }

    /**
     * Create a type, which is array of {@code this}.
     * @return new type
     */
    public final Type arrayOf() {
        return new Type(name, typeArguments, arrayDimensions + 1);
    }

    /**
     * Type arguments.
     * @return type arguments
     */
    public final TypeArguments getTypeArguments() {
        return typeArguments;
    }

    /**
     * Array dimension.
     * 1 for String[], 2 for String[][], 0 for String.
     * @return dimension
     */
    public final int getArrayDimensions() {
        return arrayDimensions;
    }

    /**
     * Type erasure. Removes type arguments; if {@code this} is type parameter, returns Object.
     * @param typeParameterNames type parameters in current context
     * @return erasure
     */
    public final String erasure(final Set<String> typeParameterNames) {
        final String effectiveName = typeParameterNames.contains(name) ? "Object" : name;
        StringBuilder builder = new StringBuilder();
        builder.append(effectiveName);
        for (int i = 0; i < arrayDimensions; i++) {
            builder.append("[]");
        }
        return builder.toString();
    }

    @Override
    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(name).append(typeArguments);
        for (int i = 0; i < arrayDimensions; i++) {
            builder.append("[]");
        }
        return builder.toString();
    }

    @Override
    public final boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Type type = (Type) o;

        return arrayDimensions == type.arrayDimensions
                && name.equals(type.name)
                && typeArguments.equals(type.typeArguments);
    }

    @Override
    public final int hashCode() {
        int result = name.hashCode();
        result = 31 * result + arrayDimensions;
        result = 31 * result + typeArguments.hashCode();
        return result;
    }

    /**
     * Replace type parameters for type arguments.
     * @param mapping replacement map
     * @return new type
     */
    public final Type replaceParams(final Map<String, TypeArgument> mapping) {
        final TypeArgument typeArgument = mapping.get(getSimpleName());
        if (typeArgument != null) {
            return typeArgument.asType();
        } else if (mapping.containsKey(getSimpleName())) {
            // my name is type parameter, but is is unresolved
            return this;
        } else {
            // no mapping, real type name - proceed with arguments
            // replace my type arguments
            return new Type(this.getSimpleName(), this.getTypeArguments().replaceParams(mapping), this.arrayDimensions);
        }
    }

    /**
     * Simple class name.
     * @return name
     */
    public final String getSimpleName() {
        return name;
    }

    /**
     * Infer type arguments from {@code this} where it is used as actual type for formal type with parameters.
     * {@code parameterMapping} initally contains pairs (typeParameterName, null) for all context type parameters
     * (class or method scope). The method updates values for keys, which it is able to resolve by matching
     * {@code formalType} to {@code this}.
     * <p/>
     * Note: Current implementation can correctly process only exact match of formal type to actual type.
     * For example, if a formal type is {@code Collection<T>} and actual type
     * is {@code MyListOfLong implements Collection<Long>)}, Symqle compiler will give up.
     * <p/>
     * If formal type is {@code Collection<T>} and actual type
     * is {@code Collection<Long>)}, and T is in parameterMapping, (T, Long) will be put to mapping.
     * <p/>
     * Symqle compiler does not care about "unknown" type names: if a name is not mapping key, it is assumed it is a
     * valid class name available in current context.
     * @param formalType formal type to match itself
     * @param parameterMapping initial mapping
     * @throws ModelException wrong model
     */
    public final void addInferredTypeArguments(final Type formalType,
                                               final Map<String, TypeArgument> parameterMapping)
                                                throws ModelException {
        String formalTypeName = formalType.getSimpleName();
        if (parameterMapping.containsKey(formalTypeName)) {
            final TypeArgument newTypeArgument = new TypeArgument(false, null, this);
            TypeArgument oldMapping = parameterMapping.put(formalTypeName, newTypeArgument);
            if (oldMapping != null && !oldMapping.equals(newTypeArgument)) {
                throw new ModelException("Cannot infer" + formalTypeName);
            }
        } else {
            // we do not process inheritance: actual type should exactly match to formal type for
            // Symqle to be able to infer type parameters
            // if a formal type is Collection<T> and actual type is MyListOfLong implements Collection<Long>,
            // Symqle will give up.
            if (!this.getSimpleName().equals(formalTypeName)) {
                throw new ModelException(
                        "Cannot infer type arguments from: " + this + " <- " + formalType);
            }
            final List<TypeArgument> actualArguments = this.getTypeArguments().getArguments();
            final List<TypeArgument> formalArguments = formalType.getTypeArguments().getArguments();
            if (actualArguments.size() != formalArguments.size()) {
                throw new ModelException("formal parameter type arguments differ from actual parameter type arguments: " +formalArguments + " and " + actualArguments + " for type " + formalType);
            }
            for (int i = 0; i < actualArguments.size(); i++) {
                actualArguments.get(i).addInferredTypeArguments(formalArguments.get(i), parameterMapping);
            }
        }
    }


    /**
     * Function, which converts SyntaxTree to Type.
     */
    public static final F<SyntaxTree, Type, GrammarException> CONSTRUCT =
            new F<SyntaxTree, Type, GrammarException>() {
                @Override
                public Type apply(final SyntaxTree syntaxTree) throws GrammarException {
                    return new Type(syntaxTree);
                }
            };
}
