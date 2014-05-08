/*
* Copyright Alexander Izyurov 2010
*/
package org.symqle.processor;

import org.symqle.parser.SyntaxTree;

/**
 * Indicates semantic error in SDL source, which can be associated with definite source location.
 *
 * @author Alexander Izyurov
 */
public class GrammarException extends Exception {

    /**
     * Indicates error, which was detected at certain node.
     * @param message informative message
     * @param node error location
     */
    public GrammarException(final String message, final SyntaxTree node) {
        super(appendPosition(message, node));
    }

    private static String appendPosition(final String message, final SyntaxTree node) {
        final StringBuilder builder = new StringBuilder();
        builder.append(message);
                builder.append(" [").append(node.getFileName()).append(":")
                        .append(node.getLine()).append(":")
                        .append(node.getColumn()).append("]");
        return builder.toString();
    }

    /**
     * Indicates error, which was detected at certain node.
     * @param cause the cause
     * @param node error location
     */
    public GrammarException(final Throwable cause, final SyntaxTree node) {
        super(appendPosition(cause.getMessage(), node), cause);
    }

}
