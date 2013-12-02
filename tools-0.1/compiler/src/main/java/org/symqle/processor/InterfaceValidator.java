package org.symqle.processor;

import org.symqle.model.InterfaceDefinition;
import org.symqle.model.Model;
import org.symqle.model.ModelException;

/**
 * Created by IntelliJ IDEA.
 * User: aizyurov
 * Date: 08.12.2012
 * Time: 21:54:44
 * To change this template use File | Settings | File Templates.
 */
public class InterfaceValidator extends ModelProcessor {

    @Override
    public void process(Model model) throws ModelException {
        // validate for no name clashes: getAllInterfaces will throw ModelException if any
        for (InterfaceDefinition def: model.getAllInterfaces()) {
            def.getAllMethods(model);
        }
    }
}
