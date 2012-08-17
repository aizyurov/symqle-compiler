/*
* Copyright Alexander Izyurov 2010
*/
package org.simqle.processor;

import org.simqle.parser.ParseException;
import org.simqle.parser.SyntaxTree;

/**
 * <br/>13.11.2011
 *
 * @author Alexander Izyurov
 */
public class GrammarException extends Exception {

    public GrammarException(final String message, final SyntaxTree node) {
        super(appendPosition(message, node));
    }

    public GrammarException(final String message, final Throwable cause, final SyntaxTree node) {
        super(appendPosition(message, node), cause);
    }

    public GrammarException(final ParseException parseException) {
        super(parseException.getMessage(), parseException);
    }

    private static String appendPosition(String message, SyntaxTree node) {
        final StringBuilder builder = new StringBuilder();
        builder.append(message);
        if (node!=null) {
                builder.append("[").append(node.getFileName()).append(":")
                        .append(node.getLine()).append(":")
                        .append(node.getColumn()).append("]");
        }
        return builder.toString();
    }
    
}
