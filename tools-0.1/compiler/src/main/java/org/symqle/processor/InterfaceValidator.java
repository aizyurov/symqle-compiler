package org.symqle.processor;

import org.symqle.model.InterfaceDefinition;
import org.symqle.model.Model;
import org.symqle.model.ModelException;
import org.symqle.parser.SyntaxTree;

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
