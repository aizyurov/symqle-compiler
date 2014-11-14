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

import org.symqle.model.ClassDefinition;
import org.symqle.model.Model;
import org.symqle.model.ModelException;
import org.symqle.parser.SyntaxTree;

import java.util.List;

/**
 * Reads class declarations from source and puts into the model.
 * Requires: all interfaces in the model.
 * @author Alexander Izyurov
 */
public class ClassDeclarationProcessor extends SyntaxTreeProcessor {

    @Override
    protected final Processor predecessor() {
        return new InterfaceDeclarationsProcessor();
    }

    @Override
    protected final void process(final SyntaxTree tree, final Model model) throws GrammarException {
        final List<SyntaxTree> syntaxTrees =
                tree.find("SymqleDeclarationBlock.SymqleDeclaration.NormalClassDeclaration");
        for (SyntaxTree classDeclarationNode: syntaxTrees) {
            ClassDefinition definition = new ClassDefinition(classDeclarationNode);
            try {
                model.addClass(definition);
            } catch (ModelException e) {
                throw new GrammarException(e, classDeclarationNode);
            }
        }
    }
}
