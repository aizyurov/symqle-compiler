/*
* Copyright Alexander Izyurov 2010
*/
package org.simqle.processor;

import org.simqle.model.*;
import org.simqle.parser.SyntaxTree;
import org.simqle.util.Utils;

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
                throw new GrammarException(e.getMessage(), node);
            }
        }

        // validate for no name clashes: getAllInterfaces will throw ModelException if any
        for (InterfaceDefinition def: model.getAllInterfaces()) {
            try {
                def.getAllMethods(model);
            } catch (ModelException e) {
                throw new GrammarException(e.getMessage(), nodeByName.get(def.getName()));
            }
        }
    }

}