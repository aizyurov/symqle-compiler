package org.symqle.model;

import org.symqle.parser.ParseException;
import org.symqle.parser.SimpleNode;
import org.symqle.parser.SyntaxTree;
import org.symqle.processor.GrammarException;
import org.symqle.util.AssertNodeType;
import org.symqle.util.Utils;

import java.util.List;
import java.util.Set;

/**
 * Field declaration.
 */
public class FieldDeclaration {
    private final String accessModifier;
    private final Set<String> otherModifiers;
    private final Type type;
    private final List<VariableDeclarator> variables;
    private final String comment;

    /**
     * Construct from String.
     * The string should contain valid field declaration.
     * @param source the string
     * @return new instance
     */
    public static FieldDeclaration parse(final String source) {
        try {
            final SimpleNode simpleNode = Utils.createParser(source).FieldDeclaration();
            SyntaxTree syntaxTree = new SyntaxTree(simpleNode, source);
            return new FieldDeclaration(syntaxTree);
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        } catch (GrammarException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Construct from AST.
     * @param node the syntax tree
     * @throws GrammarException wrong tree
     */
    public FieldDeclaration(final SyntaxTree node) throws GrammarException {
        AssertNodeType.assertOneOf(node,
                "FieldDeclaration");
        List<SyntaxTree> modifierNodes = node.find("FieldModifiers");
        this.accessModifier = Utils.getAccessModifier(modifierNodes);
        this.otherModifiers = Utils.getNonAccessModifiers(modifierNodes);
        // mandatory and only one
        this.type = node.find("Type", Type.CONSTRUCT).get(0);
        this.variables = node.find("VariableDeclarator", VariableDeclarator.CONSTRUCT);
        this.comment = node.getComments();
    }

    @Override
    public final String toString() {
        return comment + accessModifier + " " + Utils.format(otherModifiers, "", " ", " ")
                + type + " " + Utils.format(variables, "", ", ", ";");
    }

}
