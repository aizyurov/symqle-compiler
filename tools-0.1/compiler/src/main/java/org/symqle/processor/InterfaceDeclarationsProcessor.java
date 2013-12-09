/*
* Copyright Alexander Izyurov 2010
*/
package org.symqle.processor;

import org.symqle.model.InterfaceDefinition;
import org.symqle.model.Model;
import org.symqle.model.ModelException;
import org.symqle.parser.SyntaxTree;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <br/>13.11.2011
 *
 * @author Alexander Izyurov
 */
public class InterfaceDeclarationsProcessor extends SyntaxTreeProcessor {

    @Override
    protected Processor predecessor() {
        // nothing required
        return new Processor() {
            @Override
            public void process(List<SyntaxTree> trees, Model model) throws GrammarException {
                // do nothing
            }
        };
    }

    public void process(SyntaxTree tree, Model model) throws GrammarException {

        final Map<String, SyntaxTree> nodeByInterfaceName = new HashMap<String, SyntaxTree>();
        for (SyntaxTree node : tree.find(
                        "SymqleDeclarationBlock.SymqleDeclaration.SymqleInterfaceDeclaration")) {
            try {
                InterfaceDefinition definition = new InterfaceDefinition(node);
                model.addInterface(definition);
                nodeByInterfaceName.put(definition.getName(), node);
            } catch (ModelException e) {
                throw new GrammarException(e, node);
            }
        }
    }

}