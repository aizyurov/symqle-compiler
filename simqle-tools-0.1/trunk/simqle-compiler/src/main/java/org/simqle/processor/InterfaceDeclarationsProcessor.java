/*
* Copyright Alexander Izyurov 2010
*/
package org.simqle.processor;

import org.simqle.model.InterfaceDefinition;
import org.simqle.model.Model;
import org.simqle.model.ModelException;
import org.simqle.parser.SyntaxTree;

import java.util.HashMap;
import java.util.Map;

/**
 * <br/>13.11.2011
 *
 * @author Alexander Izyurov
 */
public class InterfaceDeclarationsProcessor implements Processor {

    public void process(SyntaxTree tree, Model model) throws GrammarException {

        Map<String, SyntaxTree> nodeByName = new HashMap<String, SyntaxTree>();
        for (SyntaxTree node : tree.find(
                        "SimqleDeclarationBlock.SimqleDeclaration.SimqleInterfaceDeclaration")) {
            try {
                InterfaceDefinition definition = new InterfaceDefinition(node);
                model.addInterface(definition);
                nodeByName.put(definition.getName(), node);
            } catch (ModelException e) {
                throw new GrammarException(e, node);
            }
        }

    }

}