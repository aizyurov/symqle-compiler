/*
* Copyright Alexander Izyurov 2010
*/
package org.symqle.model;

import org.symqle.parser.SyntaxTree;
import org.symqle.processor.GrammarException;
import org.symqle.util.AssertNodeType;

import java.util.List;

/**
 * Variable declarator, JLS 5 8.3.
 *
 * @author Alexander Izyurov
 */
public class VariableDeclarator {
    private final String name;
    private final String initializer;

    /**
     * Construct from AST.
     * @param node syntax tree
     * @throws GrammarException wrong tree
     */
    public VariableDeclarator(final SyntaxTree node) throws GrammarException {
        AssertNodeType.assertOneOf(node, "VariableDeclarator");
        name = node.find("VariableDeclaratorId").get(0).getValue();
        final List<String> initializers = node.find("VariableInitializer", SyntaxTree.BODY);
        this.initializer = initializers.isEmpty() ? "" : " = " + initializers.get(0);
    }

    /**
     * Variable name.
     * @return name
     */
    public final String getName() {
        return name;
    }

    @Override
    public final String toString() {
        return name + initializer;
    }

    /**
     * Funstion, which converts SyntaxTree to VariableDeclarator.
     */
    public static final F<SyntaxTree, VariableDeclarator, GrammarException> CONSTRUCT =
            new F<SyntaxTree, VariableDeclarator, GrammarException>() {
        @Override
        public VariableDeclarator apply(final SyntaxTree syntaxTree) throws GrammarException {
            return new VariableDeclarator(syntaxTree);
        }
    };
}
