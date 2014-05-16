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
