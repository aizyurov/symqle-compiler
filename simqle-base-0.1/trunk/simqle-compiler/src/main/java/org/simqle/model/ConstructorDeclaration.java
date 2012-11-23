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

import java.util.Arrays;
import java.util.List;

/**
 * <br/>20.11.2011
 *
 * @author Alexander Izyurov
 */
public class ConstructorDeclaration {
    private final String accessModifier;
    private final String comment;
    private final String name;
    private final String body;
    private final List<TypeParameter> typeParameters;
    private final List<FormalParameter> formalParameters;

    public static ConstructorDeclaration parse(String source) {
        try {
            final SimpleNode constructorNode = Utils.createParser(source).ConstructorDeclaration();
            return new ConstructorDeclaration(new SyntaxTree(constructorNode, source));
        } catch (ParseException e) {
            throw new RuntimeException("Internal error", e);
        } catch (GrammarException e) {
            throw new RuntimeException("Internal error", e);
        }
    }

    public ConstructorDeclaration(SyntaxTree node) throws GrammarException {
        Assert.assertOneOf(new GrammarException("Unexpected type: "+node.getType(), node), node.getType(), "ConstructorDeclaration");
        accessModifier = Utils.getAccessModifier(node.find("ConstructorModifiers.ConstructorModifier"));
        name=node.find("Identifier").get(0).getValue();
        comment = node.getComments();
        typeParameters = Utils.convertChildren(node, "TypeParameters.TypeParameter", TypeParameter.class);
        formalParameters = Utils.convertChildren(node, "FormalParameterList.FormalParameter", FormalParameter.class);
        body = node.find("ConstructorBody").get(0).getImage();
    }

    public String getAccessModifier() {
        return accessModifier;
    }

    public String getComment() {
        return comment;
    }

    public List<String> getCommentLines() {
        return Arrays.asList(comment.split("\\r\\n|\\n"));
    }

    public String getName() {
        return name;
    }

    public String getBody() {
        return body;
    }

    public List<TypeParameter> getTypeParameters() {
        return typeParameters;
    }

    public List<FormalParameter> getFormalParameters() {
        return formalParameters;
    }

    public List<String> getBodyLines() {
        return Arrays.asList(body.split("\\r\\n|\\n"));
    }

    public String getSignature() {
        StringBuilder builder = new StringBuilder();
        builder.append(accessModifier);
        if (!typeParameters.isEmpty()) {
            builder.append(" <");
            for (TypeParameter param: typeParameters) {
                builder.append(param.getImage());
            }
            builder.append(">");
        }
        builder.append(" ");
        builder.append(name);
        builder.append("(");
        for (FormalParameter formalParameter: formalParameters) {
            builder.append(formalParameter.getImage());
        }
        builder.append(")");
        return builder.toString();
    }

    public String getImage() {
        StringBuilder builder = new StringBuilder();
        builder.append(accessModifier).append(" ");
        builder.append(Utils.formatList(typeParameters, "<", ", ", "> ", new Function<String, TypeParameter>() {
            @Override
            public String apply(final TypeParameter typeParameter) {
                return typeParameter.getImage();
            }
        }));
        builder.append(name);
        builder.append("(");
        builder.append(Utils.formatList(formalParameters, "", ", ", "", new Function<String, FormalParameter>() {
            @Override
            public String apply(final FormalParameter formalParameter) {
                return formalParameter.getImage();
            }
        }));
        builder.append(") ");
        builder.append(body);
        return builder.toString();
    }
}
