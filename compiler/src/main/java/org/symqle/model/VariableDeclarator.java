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
