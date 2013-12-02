package org.symqle.processor;

import org.symqle.model.Model;
import org.symqle.model.ModelException;
import org.symqle.parser.SyntaxTree;

/**
 * Created by IntelliJ IDEA.
 * User: aizyurov
 * Date: 02.12.2012
 * Time: 19:52:00
 * To change this template use File | Settings | File Templates.
 */
public abstract class ModelProcessor implements Processor {

    @Override
    public boolean process(SyntaxTree tree, Model model) throws GrammarException {
        try {
            process(model);
            return false;
        } catch (ModelException e) {
            throw new GrammarException(e, tree);
        }
    }

    abstract void process(Model model) throws ModelException;
}
