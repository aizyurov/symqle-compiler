/*
* Copyright Alexander Izyurov 2010
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
