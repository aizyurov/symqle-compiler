/*
* Copyright Alexander Izyurov 2010
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
