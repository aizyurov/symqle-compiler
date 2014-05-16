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

import org.symqle.model.InterfaceDefinition;
import org.symqle.model.Model;
import org.symqle.model.ModelException;
import org.symqle.parser.SyntaxTree;

import java.util.List;

/**
 * Load interface declarations to the model.
 * @author Alexander Izyurov
 */
public class InterfaceDeclarationsProcessor extends SyntaxTreeProcessor {

    @Override
    protected final Processor predecessor() {
        // nothing required
        return new Processor() {
            @Override
            public void process(final List<SyntaxTree> trees, final Model model) throws GrammarException {
                // do nothing
            }
        };
    }

    @Override
    protected final void process(final SyntaxTree tree, final Model model) throws GrammarException {

        for (SyntaxTree node : tree.find(
                        "SymqleDeclarationBlock.SymqleDeclaration.SymqleInterfaceDeclaration")) {
            try {
                InterfaceDefinition definition = new InterfaceDefinition(node);
                model.addInterface(definition);
            } catch (ModelException e) {
                throw new GrammarException(e, node);
            }
        }
    }

}
