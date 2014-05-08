package org.symqle.model;

import org.symqle.parser.SyntaxTree;
import org.symqle.processor.GrammarException;

import java.util.Arrays;

/**
 * Used in constructors of model classes to verify argument.
 */
public final class AssertNodeType {

    private AssertNodeType() {
    }

    /**
     * Check that node type is one of expected.
     * @param node the node to inspect
     * @param expectedType possible types
     * @throws GrammarException type is not one of expected
     */
    public static void assertOneOf(final SyntaxTree node, final String... expectedType) throws GrammarException {
        final String actualType = node.getType();
        if (!Arrays.asList(expectedType).contains(actualType)) {
            throw new GrammarException("Unexpected type: " + actualType, node);
        }
    }

}
