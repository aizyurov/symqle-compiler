package org.symqle.processor;

import org.symqle.model.Model;
import org.symqle.parser.SyntaxTree;
import org.symqle.util.Log;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: aizyurov
 * Date: 07.12.2013
 * Time: 13:09:08
 * To change this template use File | Settings | File Templates.
 */
public abstract class SyntaxTreeProcessor extends ChainedProcessor {
    @Override
    public final void process(List<SyntaxTree> trees, Model model) throws GrammarException {
        predecessor().process(trees, model);
        Log.info("STARTING " + getClass().getSimpleName());
        for (SyntaxTree tree : trees) {
            process(tree, model);
        }
        Log.info("FINISHED " + getClass().getSimpleName());
    }

    protected abstract void process(SyntaxTree tree, Model model) throws GrammarException;
}
