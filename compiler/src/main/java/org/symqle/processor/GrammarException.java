/*
   Copyright 2011-2014 Alexander Izyurov

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.package org.symqle.common;
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
