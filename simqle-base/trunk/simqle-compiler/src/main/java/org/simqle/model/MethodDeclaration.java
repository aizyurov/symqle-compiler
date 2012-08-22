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

import java.util.List;
import java.util.Set;

import static org.simqle.util.Utils.convertChildren;

/**
 * <br/>13.11.2011
 *
 * @author Alexander Izyurov
 */
public class MethodDeclaration {
    private final String accessModifier;
    private final boolean isStatic;
    private final boolean isAbstract;
    private final List<TypeParameter> typeParameters;
    // null for void methods
    private final Type resultType;

    private final String name;
    private final List<FormalParameter> formalParameters;


    private final String comment;
    // null for abstract methods
    private final String methodBody;

    private final String throwsClause;


    public static MethodDeclaration parseAbstractMethod(String source) {
        try {
            final SimpleNode simpleNode = Utils.createParser(source).AbstractMethodDeclaration();
            return new MethodDeclaration(new SyntaxTree(simpleNode, source));
        } catch (ParseException e) {
            throw new RuntimeException("Internal error", e);
        } catch (GrammarException e) {
            throw new RuntimeException("Internal error", e);
        }
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
        Assert.assertOneOf(node.getType(), "AbstractMethodDeclaration", "MethodDeclaration");
        final boolean isInterfaceMethod = node.getType().equals("AbstractMethodDeclaration");
        String accessModifier="";
        // actually only one find returns non-empty list
        final List<SyntaxTree> modifiers = node.find("AbstractMethodModifiers.AbstractMethodModifier");
        modifiers.addAll(node.find("MethodModifiers.MethodModifier"));
        accessModifier = Utils.getAccessModifier(modifiers);
        final Set<String> otherModifiers = Utils.getNonAccessModifiers(modifiers);
        final boolean isAbstract = otherModifiers.contains("abstract");
        this.accessModifier = accessModifier;
        this.isAbstract = isInterfaceMethod || isAbstract;
        this.isStatic = otherModifiers.contains("static");

        typeParameters = convertChildren(node, "TypeParameters.TypeParameter", TypeParameter.class);
        final List<SyntaxTree> resultTypes = node.find("ResultType.Type");
        if (resultTypes.isEmpty()) {
            // void
            resultType = null;
        } else {
            resultType = new Type(resultTypes.get(0));
        }

        final List<SyntaxTree> nodes = node.find("MethodDeclarator.Identifier");
        name = nodes.get(0).getValue();
        formalParameters = convertChildren(node, "MethodDeclarator.FormalParameterList.FormalParameter", FormalParameter.class);
        formalParameters.addAll(convertChildren(node, "MethodDeclarator.FormalParameterList.FormalParameterWithEllipsis", FormalParameter.class));
        
        comment = node.getComments();
        final List<String> throwClauses = Utils.bodies(node.find("Throws"));
        // may be at most one by grammar
        this.throwsClause = Utils.concat(throwClauses, " ");
        StringBuilder bodyBuilder = new StringBuilder();
        for (SyntaxTree bodyBlock: node.find("MethodBody.Block")) {
            bodyBuilder.append(bodyBlock.getImage());
        }
        methodBody = bodyBuilder.length()==0 ? null : bodyBuilder.toString();
    }

    @Deprecated
    public MethodDeclaration(boolean interfaceMethod, String accessModifier, boolean aStatic, boolean anAbstract, List<TypeParameter> typeParameters, Type resultType, String name, List<FormalParameter> formalParameters, String throwsClause, String comment, String methodBody) throws ModelException {
        this.accessModifier = accessModifier;
        isStatic = aStatic;
        isAbstract = anAbstract;
        this.typeParameters = typeParameters;
        this.resultType = resultType;
        this.name = name;
        this.formalParameters = formalParameters;
        this.comment = comment;
        this.throwsClause = throwsClause;
        this.methodBody = methodBody;
        // TODO validate body
    }

    public String getName() {
        return name;
    }

    public String getAccessModifier() {
        return accessModifier;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public boolean isAbstract() {
        return isAbstract;
    }

    public List<TypeParameter> getTypeParameters() {
        return typeParameters;
    }

    public Type getResultType() {
        return resultType;
    }

    public List<FormalParameter> getFormalParameters() {
        return formalParameters;
    }

    public String getComment() {
        return comment;
    }

    public String getMethodBody() {
        return methodBody;
    }

    public String getThrowsClause() {
        return throwsClause;
    }
}
