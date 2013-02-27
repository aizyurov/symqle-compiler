/*
* Copyright Alexander Izyurov 2010
*/
package org.simqle.processor;

import org.simqle.model.Model;
import org.simqle.parser.SyntaxTree;

/**
 * <br/>14.11.2011
 *
 * @author Alexander Izyurov
 */
public interface Processor {
    void process(SyntaxTree tree, Model model) throws GrammarException;
}
