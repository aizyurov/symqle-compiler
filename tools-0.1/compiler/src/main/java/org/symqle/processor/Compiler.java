package org.symqle.processor;

import org.symqle.model.Model;
import org.symqle.model.ModelException;

/**
 * Created by IntelliJ IDEA.
 * User: aizyurov
 * Date: 07.12.2013
 * Time: 16:37:03
 * To change this template use File | Settings | File Templates.
 */
public class Compiler extends ModelProcessor {

    @Override
    protected void process(Model model) throws ModelException {
        // does nothing; just entry point
    }

    @Override
    protected Processor predecessor() {
        return new InterfaceJavadocProcessor();
    }
}
