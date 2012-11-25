/*
* Copyright Alexander Izyurov 2010
*/
package org.simqle.model;

import org.simqle.parser.ParseException;
import org.simqle.parser.SimpleNode;
import org.simqle.parser.SyntaxTree;
import org.simqle.processor.GrammarException;
import org.simqle.util.Assert;
import org.simqle.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.simqle.util.Utils.convertChildren;

/**
 * <br/>13.11.2011
 *
 * @author Alexander Izyurov
 */
public class MethodDeclaration {
    private final TypeParameters typeParameters;
    // null for void methods
    private final Type resultType;

    private final String name;
    private final List<FormalParameter> formalParameters;

    private final Set<Type> thrownExceptions;


    public MethodDeclaration(TypeParameters typeParameters, Type resultType, String name, List<FormalParameter> formalParameters, Set<Type> thrownExceptions) {
        this.typeParameters = typeParameters;
        this.resultType = resultType;
        this.name = name;
        this.formalParameters = formalParameters;
        this.thrownExceptions = new TreeSet<Type>(thrownExceptions);
    }

    public static MethodDeclaration parse(String source) {
        try {
            final SimpleNode simpleNode = Utils.createParser(source).MethodDeclaration();
            return new MethodDeclaration(new SyntaxTree(simpleNode, source));
        } catch (ParseException e) {
            throw new RuntimeException("Internal error", e);
        } catch (GrammarException e) {
            throw new RuntimeException("Internal error", e);
        }
    }

    public MethodDeclaration(SyntaxTree node) throws GrammarException {
        Assert.assertOneOf(new GrammarException("Unexpected type: "+node.getType(), node), node.getType(), "AbstractMethodDeclaration", "MethodDeclaration");
        typeParameters = node.find("TypeParameters", TypeParameters.CONSTRUCT).get(0);
        final List<SyntaxTree> resultTypes = node.find("ResultType.Type");
        if (resultTypes.isEmpty()) {
            // void
            resultType = Type.VOID;
        } else {
            resultType = new Type(resultTypes.get(0));
        }

        final List<SyntaxTree> nodes = node.find("MethodDeclarator.Identifier");
        name = nodes.get(0).getValue();
        formalParameters = node.find("MethodDeclarator.FormalParameterList.FormalParameter", FormalParameter.CONSTRUCT);
        formalParameters.addAll
                (node.find("MethodDeclarator.FormalParameterList.FormalParameterWithEllipsis", FormalParameter.CONSTRUCT));
        
        final List<String> throwClauses = node.find("Throws", SyntaxTree.BODY);
        // may be at most one by grammar
        this.thrownExceptions = new TreeSet<Type>(node.find("Throws.ExceptionType", Type.CONSTRUCT));
    }

    public String getName() {
        return name;
    }

    public TypeParameters getTypeParameters() {
        return typeParameters;
    }

    public Type getResultType() {
        return resultType;
    }

    public List<FormalParameter> getFormalParameters() {
        return formalParameters;
    }

    /**
     * Not the same signature as javac generates!
     * @return
     */
    public String signature() {
        final StringBuilder builder = new StringBuilder();
        builder.append(name)
                .append("(")
                .append(
                    Utils.formatList(formalParameters, "", ",", "", new Function<String, FormalParameter>() {
                        @Override
                        public String apply(final FormalParameter formalParameter) {
                            return formalParameter.erasure();
                        }
                    }))
                .append(")");
        return builder.toString();
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append(typeParameters);
        if (typeParameters.isEmpty()) {
            builder.append(" ");
        }
        builder.append(resultType).append(" ");
        builder.append(name)
                .append("(")
                .append(Utils.format(formalParameters, "", ", ", ""))
                .append(")")
                .append(Utils.format(thrownExceptions, "throws ", ", ", ""));
        return builder.toString();
    }

    public MethodDeclaration override(TypeParameters typeParameters, TypeArguments typeArguments) throws ModelException {
        final TypeParameters newTypeParameters = typeParameters;
        final List<FormalParameter> newFormalParameters = new ArrayList<FormalParameter>(formalParameters.size());
        for (FormalParameter formalParameter: formalParameters) {
            newFormalParameters.add(formalParameter.substituteParameters(typeParameters, typeArguments));
        }
        final String newName = name;
        final Type newResultType = resultType.substituteParameters(typeParameters, typeArguments);
        final Set<Type> newThrownExceptions = new TreeSet<Type>();
        for (Type exceptionType: thrownExceptions) {
            newThrownExceptions.add(exceptionType.substituteParameters(typeParameters, typeArguments));
        }
        return new MethodDeclaration(newTypeParameters,
                newResultType,
                newName,
                newFormalParameters,
                newThrownExceptions);
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!( obj  instanceof MethodDeclaration)) {
            return false;
        }
        MethodDeclaration that = (MethodDeclaration) obj;
        try {
            MethodDeclaration adjusted = that.override(that.typeParameters, this.typeParameters.asTypeArguments());
            return this.name.equals(adjusted.name)
                    && this.formalParameters.equals(adjusted.formalParameters)
                    && this.resultType.equals(adjusted.resultType)
                    && this.thrownExceptions.equals(adjusted.thrownExceptions);
        } catch (ModelException e) {
            // parameters number mismatch somewhere or like
            return false;
        }
    }

}
