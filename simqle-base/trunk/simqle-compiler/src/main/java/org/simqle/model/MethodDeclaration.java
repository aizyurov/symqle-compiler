/*
* Copyright Alexander Izyurov 2010
*/
package org.simqle.model;

import org.simqle.parser.ParseException;
import org.simqle.parser.SimpleNode;
import org.simqle.parser.SyntaxTree;
import org.simqle.processor.GrammarException;

import java.util.List;

import static org.simqle.model.Utils.convertChildren;

/**
 * <br/>13.11.2011
 *
 * @author Alexander Izyurov
 */
public class MethodDeclaration {
    private final boolean isInterfaceMethod;
    private final String accessModifier;
    private final boolean isStatic;
    private final boolean isAbstract;
    private final List<TypeParameter> typeParameters;
    // null for void methods
    private final Type resultType;

    private final String name;
    private final List<FormalParameter> formalParameters;


    private final String comment;
    private final String signature;
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

    public MethodDeclaration(SyntaxTree node) throws GrammarException {
        if (node.getType().equals("AbstractMethodDeclaration")) {
            isInterfaceMethod = true;
        } else if (node.getType().equals("MethodDeclaration")) {
            isInterfaceMethod = false;
        } else {
            throw new IllegalArgumentException("IllegalArgument: "+node);
        }
        String accessModifier="";
        boolean isAbstract = false;
        boolean isStatic = false;
        // actually only one find returns non-empty list
        final List<SyntaxTree> modifiers = node.find("AbstractMethodModifiers.AbstractMethodModifier");
        modifiers.addAll(node.find("MethodModifiers.MethodModifier"));
        for (SyntaxTree modifier : modifiers) {
            final String value = modifier.getValue();
            if (value.equals("public") || value.equals("protected") || value.equals("private")) {
                if (!accessModifier.equals("")) {
                    throw new GrammarException("incompatible modifiers: "+accessModifier+", "+value, modifier);
                }
                accessModifier = value;
            } else if (value.equals("abstract")) {
                isAbstract = true;
            } else if (value.equals("static")) {
                isStatic = true;
            }
        }
        this.accessModifier = accessModifier;
        this.isAbstract = isInterfaceMethod || isAbstract;
        this.isStatic = isStatic;

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
        StringBuilder signatureBuilder = new StringBuilder();
        for (int i=0; i<modifiers.size(); i++) {
            signatureBuilder.append(modifiers.get(i).getBody());
            signatureBuilder.append(" ");
        }
        for (SyntaxTree typeParams: node.find("TypeParameters")) {
            signatureBuilder.append(typeParams.getBody());
            signatureBuilder.append(" ");
        }
        for (SyntaxTree resultType: node.find("ResultType")) {
            signatureBuilder.append(resultType.getBody());
            signatureBuilder.append(" ");
        }
        for (SyntaxTree declarator: node.find("MethodDeclarator")) {
            signatureBuilder.append(declarator.getBody());
        }
        String throwsString = "";
        for (SyntaxTree throwsClause: node.find("Throws")) {
            signatureBuilder.append(" ");
            signatureBuilder.append(throwsClause.getBody());
            throwsString = throwsClause.getBody();
        }
        this.throwsClause = throwsString;
        signature = signatureBuilder.toString();
        StringBuilder bodyBuilder = new StringBuilder();
        for (SyntaxTree bodyBlock: node.find("MethodBody.Block")) {
            bodyBuilder.append(bodyBlock.getImage());
        }
        methodBody = bodyBuilder.length()==0 ? null : bodyBuilder.toString();
    }

    public MethodDeclaration(boolean interfaceMethod, String accessModifier, boolean aStatic, boolean anAbstract, List<TypeParameter> typeParameters, Type resultType, String name, List<FormalParameter> formalParameters, String throwsClause, String comment, String methodBody) throws ModelException {
        isInterfaceMethod = interfaceMethod;
        this.accessModifier = accessModifier;
        isStatic = aStatic;
        isAbstract = anAbstract;
        this.typeParameters = typeParameters;
        this.resultType = resultType;
        this.name = name;
        this.formalParameters = formalParameters;
        this.comment = comment;
        StringBuilder signatureBuilder = new StringBuilder();
        signatureBuilder.append(accessModifier);
        if (isAbstract && !isInterfaceMethod) {
            signatureBuilder.append(" abstract");
        }
        if (isStatic) {
            signatureBuilder.append(" static");
        }
        if (typeParameters.size()>0) {
            signatureBuilder.append(" <");
            for (int i=0; i<typeParameters.size(); i++) {
                TypeParameter param = typeParameters.get(i);
                if (i>0) {
                    signatureBuilder.append(param);
                }
                signatureBuilder.append(param.getImage());
            }
            signatureBuilder.append(">");
        }
        signatureBuilder.append(" ")
                .append(resultType == null ? "void" : resultType.getImage())
                .append(" ")
                .append(name)
                .append("(");
        for (int i=0; i<formalParameters.size(); i++) {
            FormalParameter formalParam = formalParameters.get(i);
            if (i>0) {
                signatureBuilder.append(", ");
            }
            signatureBuilder.append(formalParam.getImage());
        }
        signatureBuilder.append(") ");
        signatureBuilder.append(throwsClause);
        this.throwsClause = throwsClause;
        signature = signatureBuilder.toString();

        this.methodBody = methodBody;
        // TODO validate body
    }

    public String getName() {
        return name;
    }

    public boolean isInterfaceMethod() {
        return isInterfaceMethod;
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

    public String getSignature() {
        return signature;
    }

    public String getMethodBody() {
        return methodBody;
    }

    public String getThrowsClause() {
        return throwsClause;
    }
}
