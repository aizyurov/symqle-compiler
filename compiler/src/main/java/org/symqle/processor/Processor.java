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

import java.util.List;

/**
 * Common interface for all processors.
 *
 * @author Alexander Izyurov
 */
public interface Processor {
    /**
     * Do whatever needed with source syntax trees and model.
     * @param trees source syntax trees
     * @param model collection of known classes and interfaces
     * @throws GrammarException something goes wrong
     */
    void process(List<SyntaxTree> trees, Model model) throws GrammarException;
}
