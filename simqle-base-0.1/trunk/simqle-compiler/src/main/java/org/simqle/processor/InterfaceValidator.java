package org.simqle.processor;

import org.simqle.model.InterfaceDefinition;
import org.simqle.model.Model;
import org.simqle.model.ModelException;
import org.simqle.parser.SyntaxTree;

/**
 * Created by IntelliJ IDEA.
 * User: aizyurov
 * Date: 08.12.2012
 * Time: 21:54:44
 * To change this template use File | Settings | File Templates.
 */
public class InterfaceValidator implements Processor {

    @Override
    public void process(SyntaxTree tree, Model model) throws GrammarException {
        // validate for no name clashes: getAllInterfaces will throw ModelException if any
        for (InterfaceDefinition def: model.getAllInterfaces()) {
            try {
                def.getAllMethods(model);
            } catch (ModelException e) {
                throw new GrammarException(e, tree);
            }
        }
    }
}
