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
