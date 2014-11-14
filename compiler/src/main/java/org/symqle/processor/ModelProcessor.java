/*
   Copyright 2011-2014 Alexander Izyurov

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.package org.symqle.common;
*/

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
