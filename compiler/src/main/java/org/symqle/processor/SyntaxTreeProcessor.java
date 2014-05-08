package org.symqle.processor;

import org.symqle.model.Model;
import org.symqle.parser.SyntaxTree;
import org.symqle.util.Log;

import java.util.List;

/**
 * Processor, which reads all syntax trees and puts results to model.
 */
public abstract class SyntaxTreeProcessor extends ChainedProcessor {
    @Override
    public final void process(final List<SyntaxTree> trees, final Model model) throws GrammarException {
        predecessor().process(trees, model);
        Log.info("STARTING " + getClass().getSimpleName());
        for (SyntaxTree tree : trees) {
            process(tree, model);
        }
        Log.info("FINISHED " + getClass().getSimpleName());
    }

    /**
     * Read one syntax tree and do whatever needed.
     * @param tree source syntax tree
     * @param model where to put results
     * @throws GrammarException something is wrong
     */
    protected abstract void process(SyntaxTree tree, Model model) throws GrammarException;
}
