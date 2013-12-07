/*
* Copyright Alexander Izyurov 2010
*/
package org.symqle.processor;

import org.symqle.model.Model;
import org.symqle.parser.SyntaxTree;

import java.util.List;

/**
 * <br/>14.11.2011
 *
 * @author Alexander Izyurov
 */
public interface Processor {
    void process(List<SyntaxTree> trees, Model model) throws GrammarException;
}
