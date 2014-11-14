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

/**
 * Java type argument.
 * @author Alexander Izyurov
 */
public class TypeArgument {
    private final boolean isWildCardArgument;
    // "extends" | "super" | null
    private final String boundType;
    private final Type reference;

    /**
     * Construct from AST.
     * @param node syntax tree
     * @throws GrammarException wrong tree
     */
    public TypeArgument(final SyntaxTree node)  throws GrammarException {
        AssertNodeType.assertOneOf(node, "TypeArgument");
        final List<Type> references = node.find("ReferenceType", Type.CONSTRUCT);
        if (!references.isEmpty()) {
            isWildCardArgument = false;
            boundType = null;
            reference = references.get(0);
        } else {
            isWildCardArgument = true;
            final List<SyntaxTree> boundTypes = node.find("WildcardBounds.WildcardBoundType");
            boundType = boundTypes.isEmpty() ? null : boundTypes.get(0).getValue();
            List<Type> boundReferences = node.find("WildcardBounds.ReferenceType", Type.CONSTRUCT);
            reference = boundReferences.isEmpty() ? null : boundReferences.get(0);
        }
    }

    /**
     * Construct with give properties.
     * @param wildCardArgument true if wildcard
     * @param boundType "extends" | "super" | null
     * @param reference referenced type
     */
    public TypeArgument(final boolean wildCardArgument, final String boundType, final Type reference) {
        isWildCardArgument = wildCardArgument;
        this.boundType = boundType;
        this.reference = reference;
    }

    /**
     * Converts {@code this} to Type.
     * <p/>
     * Non-wildcard argument is converted to itself.
    * <p/>
     * {@code ?} and {@code ? super X} are converted to Object.
     * <p/>
     * {@code ? extends X} is converted to X.
     * <p/>
     * @return appropriate type
     */
    public final Type asType() {
        return "super".equals(boundType)
                || reference == null // "?"
                ? new Type("Object")
                : reference;
    }

    /**
     * True if this is wildcard argument.
     * @return true if this is wildcard argument.
     */
    public final boolean isWildCardArgument() {
        return isWildCardArgument;
    }

    /**
     * Create type argument, which is just type.
     * @param simpleType source type name
     */
    public TypeArgument(final String simpleType) {
        isWildCardArgument = false;
        boundType = null;
        reference = new Type(simpleType);
    }

    @Override
    public final boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final TypeArgument that = (TypeArgument) o;

        if (isWildCardArgument != that.isWildCardArgument) {
            return false;
        }
        if (boundType != null ? !boundType.equals(that.boundType) : that.boundType != null) {
            return false;
        }
        if (reference != null ? !reference.equals(that.reference) : that.reference != null) {
            return false;
        }

        return true;
    }

    @Override
    public final int hashCode() {
        int result = (isWildCardArgument ? 1 : 0);
        result = 31 * result + (boundType != null ? boundType.hashCode() : 0);
        result = 31 * result + (reference != null ? reference.hashCode() : 0);
        return result;
    }

    /**
     * Converts SyntaxTree fo TypeArgument.
     */
    public static final F<SyntaxTree, TypeArgument, GrammarException> CONSTRUCT =
            new F<SyntaxTree, TypeArgument, GrammarException>() {
                @Override
                public TypeArgument apply(final SyntaxTree syntaxTree) throws GrammarException {
                    return new TypeArgument(syntaxTree);
                }
            };

    /**
     * Create new TypeArgument by replacing type parameters in {@code this}.
     * @param mapping type parameters to arguments mapping
     * @return new type argument
     */
    public final TypeArgument replaceParams(final Map<String, TypeArgument> mapping) {
        if (isWildCardArgument()) {
            final String name = "#" + System.identityHashCode(this);
            if (mapping.containsKey(name)) {
                return mapping.get(name);
            } else {
                return this;
            }
        } else {
            return new TypeArgument(isWildCardArgument, boundType,
                reference == null ? null : reference.replaceParams(mapping));
        }
    }

    /**
     * Infer type arguments from {@code this} where it is used as actual type for formal type argument with parameters.
     * {@code parameterMapping} initally contains pairs (typeParameterName, actualArgument)
     * for all context type parameters
     * (class or method scope). Some actualArgument values may be null (not inferred yet) in the provided mapping.
     * The method updates values for keys, which it is able to resolve by matching. It also may add new (generated)
     * unique key to the map if formalTypeArgument is a wildcard argument (analog of "Capture #xxx of ...").
     * {@code formalTypeArgument} to {@code this}.
     * <p/>
     * @param formalTypeArgument formal type to match itself
     * @param parameterMapping initial mapping
     * @throws ModelException wrong model
     */
    public final void addInferredTypeArguments(final TypeArgument formalTypeArgument,
                                               final Map<String, TypeArgument> parameterMapping)
                                                throws ModelException {
        if (!formalTypeArgument.isWildCardArgument()) {
            final String name = formalTypeArgument.reference.getSimpleName();
            if (parameterMapping.containsKey(name)) {
                final TypeArgument oldTypeArgument = parameterMapping.get(name);
                if (oldTypeArgument != null && !oldTypeArgument.equals(this)) {
                    throw new ModelException("Cannot infer type parameters");
                }
                parameterMapping.put(name, this);
            } else {
                // assume it is a real class
                if (!this.isWildCardArgument() && reference.getSimpleName().equals(name)) {
                    final List<TypeArgument> formalArguments =
                            formalTypeArgument.reference.getTypeArguments().getArguments();
                    final List<TypeArgument> actualArguments = reference.getTypeArguments().getArguments();
                    if (actualArguments.size() != formalArguments.size()) {
                        throw new ModelException(
                                "formal parameter type arguments differ from actual parameter type arguments");
                    }
                    for (int i = 0; i < actualArguments.size(); i++) {
                        formalArguments.get(i).addInferredTypeArguments(actualArguments.get(i), parameterMapping);
                    }
                }
            }
        } else {
            // each wildcard argument is distinct anonymous type; create a name and put to map
            final String name = "#" + System.identityHashCode(formalTypeArgument);
            parameterMapping.put(name, this);
        }
    }

    @Override
    public final String toString() {
        StringBuilder builder = new StringBuilder();
        if (isWildCardArgument) {
            if (boundType == null) {
                return "?";
            } else {
                builder.append("? ").append(boundType).append(" ");
            }
        }
        builder.append(reference);
        return builder.toString();
    }
}
