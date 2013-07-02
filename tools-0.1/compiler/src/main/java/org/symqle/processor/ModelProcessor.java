package org.symqle.processor;

import org.symqle.model.Model;
import org.symqle.model.ModelException;

/**
 * Created by IntelliJ IDEA.
 * User: aizyurov
 * Date: 02.12.2012
 * Time: 19:52:00
 * To change this template use File | Settings | File Templates.
 */
public interface ModelProcessor {

    void process(Model model) throws ModelException;
}
