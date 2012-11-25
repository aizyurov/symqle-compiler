package org.simqle.model;

import org.simqle.parser.ParseException;
import org.simqle.parser.SimpleNode;
import org.simqle.parser.SyntaxTree;
import org.simqle.processor.GrammarException;
import org.simqle.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: aizyurov
 * Date: 25.11.2012
 * Time: 8:32:28
 * To change this template use File | Settings | File Templates.
 */
public class MethodDefinition {
    private final String comment;
    private final String accessModifier;
    private final Set<String> otherModifiers;
    private final MethodDeclaration declaration;
    private final String body;

    public static MethodDefinition parseAbstractMethod(String source) {
        try {
            final SimpleNode simpleNode = Utils.createParser(source).AbstractMethodDeclaration();
            return new MethodDefinition(new SyntaxTree(simpleNode, source));
        } catch (ParseException e) {
            throw new RuntimeException("Internal error", e);
        } catch (GrammarException e) {
            throw new RuntimeException("Internal error", e);
        }
    }



    public MethodDefinition(String comment, String accessModifier, Set<String> otherModifiers, MethodDeclaration declaration, String body) {
        this.comment = comment;
        this.accessModifier = accessModifier;
        this.otherModifiers = otherModifiers;
        this.declaration = declaration;
        this.body = body;
    }

    public MethodDefinition(SyntaxTree node) throws GrammarException {
        this(node.getComments(),
                Utils.getAccessModifier(findModifierNodes(node)),
                Utils.getNonAccessModifiers(findModifierNodes(node)),
                new MethodDeclaration(node),
                node.find("MethodBody").get(0).getImage()
            );
    }

    public String getName() {
        return declaration.getName();
    }

    public String getSignature() {
        return declaration.signature();
    }

    private static List<SyntaxTree> findModifierNodes(SyntaxTree node) {
        List<SyntaxTree> modifierNodes = node.find("MethodModifiers.MethodModifier");
        modifierNodes.addAll(node.find("AbstractMethodModifiers.AbstractMethodModifier"));
        return modifierNodes;
    }

    public MethodDeclaration getDeclaration() {
        return declaration;
    }

    public String getComment() {
        return comment;
    }

    public String getAccessModifier() {
        return accessModifier;
    }

    public Set<String> getOtherModifiers() {
        return otherModifiers;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(comment);
        builder.append(accessModifier);
        builder.append(Utils.format(new ArrayList(otherModifiers), " ", " ", ""));
        builder.append(declaration);
        builder.append(body);
        return builder.toString();
    }

}
