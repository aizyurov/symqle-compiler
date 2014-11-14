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
import org.symqle.model.MethodDefinition;
import org.symqle.model.Model;
import org.symqle.model.ModelException;
import org.symqle.parser.SyntaxTree;

import java.util.List;

/**
 * Read "standalone" (not production rule related) methods from syntax trees and put to model.
 * To change this template use File | Settings | File Templates.
 */
public class SymqleMethodProcessor extends SyntaxTreeProcessor {

    @Override
    protected final Processor predecessor() {
        return new ProductionProcessor();
    }

    @Override
    protected final void process(final SyntaxTree tree, final Model model) throws GrammarException {

        final ClassDefinition symqle;
        try {
            symqle = model.getClassDef("Symqle");
        } catch (ModelException e) {
            throw new IllegalStateException(e);
        }

        for (SyntaxTree methodNode: tree.find("SymqleDeclarationBlock.SymqleDeclaration.MethodDeclaration")) {
            MethodDefinition method = new MethodDefinition(methodNode, symqle);
            List<String> declarationImports = methodNode.find("^.^.ImportDeclaration", SyntaxTree.BODY);
            try {
                method.makeStatic();
                method.setSourceRef(methodNode);
                model.addExplicitMethod(method, null, declarationImports);
            } catch (ModelException e) {
                throw new GrammarException(e, methodNode);
            }
            symqle.addImportLines(declarationImports);
        }
    }
}
