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

/**
 * Annotation. Only simple annotations (without parameters) are supported.
 * @author Alexander Izyurov
 */
public class Annotation {
    private String name;

    /**
     * Constructs from AST.
     * @param node the syntax tree
     * @throws GrammarException wrong tree
     */
    public Annotation(final SyntaxTree node) throws GrammarException {
        AssertNodeType.assertOneOf(node, "Annotation");
        name = node.find("Identifier").get(0).getValue();
    }

    /**
     * Name of this annotation.
     * @return the name
     */
    public final String getName() {
        return name;
    }
}
