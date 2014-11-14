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
