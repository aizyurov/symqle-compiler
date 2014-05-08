package org.symqle.processor;

import org.symqle.model.Model;
import org.symqle.model.ModelException;
import org.symqle.parser.SyntaxTree;
import org.symqle.util.Log;

import java.util.List;

/**
 * Processor, which scans and updates model.
 * It ignores syntax trees provided to its {@link #process(java.util.List, org.symqle.model.Model)}
 */
public abstract class ModelProcessor extends ChainedProcessor {

    @Override
    public final void process(final List<SyntaxTree> trees, final Model model) throws GrammarException {
        try {
            predecessor().process(trees, model);
            Log.info("STARTING " + getClass().getSimpleName());
            process(model);
            Log.info("FINISHED " + getClass().getSimpleName());
        } catch (ModelException e) {
            throw new GrammarException(e, trees.get(0));
        }
    }

    /**
     * Scan and update model.
     * @param model the model
     * @throws ModelException if something goes wrong
     */
    protected abstract void process(Model model) throws ModelException;
}
