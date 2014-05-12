package org.symqle.util;

import org.symqle.parser.SyntaxTree;
import org.symqle.processor.GrammarException;

import java.util.Arrays;

/**
 * We need AssertNodeType, but we do not want to make production dependent of JUnit.
 * So custom class with one method.
 */
public class AssertNodeType {

    private AssertNodeType() {
    }

    public static void assertOneOf(SyntaxTree node, String... expectedType) throws GrammarException {
        final String actualType = node.getType();
        if (!Arrays.asList(expectedType).contains(actualType)) {
            throw new GrammarException("Unexpected type: " + actualType, node);
        }
    }

}
