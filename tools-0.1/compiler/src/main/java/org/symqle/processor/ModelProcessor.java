package org.symqle.processor;

import org.symqle.model.Model;
import org.symqle.model.ModelException;
import org.symqle.parser.SyntaxTree;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: aizyurov
 * Date: 02.12.2012
 * Time: 19:52:00
 * To change this template use File | Settings | File Templates.
 */
public abstract class ModelProcessor extends ChainedProcessor {

    @Override
    public void process(List<SyntaxTree> trees, Model model) throws GrammarException {
        try {
            predecessor().process(trees, model);
            process(model);
        } catch (ModelException e) {
            throw new GrammarException(e, trees.get(0));
        }
    }

    abstract void process(Model model) throws ModelException;
}
