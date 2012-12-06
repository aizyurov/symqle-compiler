/*
* Copyright Alexander Izyurov 2010
*/
package org.simqle.model;

import org.simqle.parser.SyntaxTree;
import org.simqle.processor.GrammarException;
import org.simqle.util.Assert;

import java.util.List;
import java.util.Map;

/**
 * <br/>13.11.2011
 *
 * @author Alexander Izyurov
 */
public class TypeArgument {
    private final boolean isWildCardArgument;
    // "extends" | "super" | null
    private final String boundType;
    private final Type reference;

    public TypeArgument(SyntaxTree node)  throws GrammarException {
        Assert.assertOneOf(new GrammarException("Unexpected type: "+node.getType(), node), node.getType(), "TypeArgument");
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

    public TypeArgument(final boolean wildCardArgument, final String boundType, final Type reference) {
        isWildCardArgument = wildCardArgument;
        this.boundType = boundType;
        this.reference = reference;
    }

    public Type asType() {
        return "super".equals(boundType) ? new Type("Object") : reference;
    }

    public Type getReference() {
        return reference;
    }

    public String getBoundType() {
        return boundType;
    }

    public boolean isWildCardArgument() {
        return isWildCardArgument;
    }

    public TypeArgument(String simpleType) {
        isWildCardArgument = false;
        boundType = null;
        reference = new Type(new TypeNameWithTypeArguments(simpleType), 0);
    }

    public String getImage() {
        return boundType == null ? reference.toString() : "? "+boundType + " " + reference.toString();
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

    public final static F<SyntaxTree, TypeArgument, GrammarException> CONSTRUCT =
            new F<SyntaxTree, TypeArgument, GrammarException>() {
                @Override
                public TypeArgument apply(SyntaxTree syntaxTree) throws GrammarException {
                    return new TypeArgument(syntaxTree);
                }
            };

    public TypeArgument replaceParams(final Map<String, TypeArgument> mapping) {
        return new TypeArgument(isWildCardArgument, boundType,
                reference == null ? null : reference.replaceParams(mapping));
    }

    public TypeArgument substituteParameters(TypeParameters typeParameters, TypeArguments typeArguments) throws ModelException {
        return new TypeArgument(isWildCardArgument, boundType,
                reference == null ? null : reference.substituteParameters(typeParameters, typeArguments));
    }

    public void addInferredTypeArguments(final TypeArgument formalTypeArgument, final Map<String, TypeArgument> parameterMapping) throws ModelException {
        // ignore wildcard arguments: they are OK, but do not help in inferring
        if (!formalTypeArgument.isWildCardArgument()) {
            final String name = formalTypeArgument.getReference().getSimpleName();
            if (parameterMapping.containsKey(name)) {
                final TypeArgument oldTypeArgument = parameterMapping.get(name);
                if (oldTypeArgument != null && !oldTypeArgument.equals(this)) {
                    throw new ModelException("Cannot infer type parameters");
                }
                parameterMapping.put(name, this);
            } else {
                // assume it is a real class
                if (!this.isWildCardArgument() && this.getReference().getSimpleName().equals(name)) {
                    final List<TypeArgument> formalArguments = formalTypeArgument.getReference().getTypeArguments().getArguments();
                    final List<TypeArgument> actualArguments = this.getReference().getTypeArguments().getArguments();
                    if (actualArguments.size() != formalArguments.size()) {
                        throw new ModelException("formal parameter type arguments differ from actual parameter type arguments");
                    }
                    for (int i=0; i<actualArguments.size(); i++) {
                        formalArguments.get(i).addInferredTypeArguments(actualArguments.get(i), parameterMapping);
                    }
                }
            }
        }
    }

    @Override
    public String toString() {
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
