/*
* Copyright Alexander Izyurov 2010
*/
package org.symqle.processor;

import org.symqle.model.Model;
import org.symqle.parser.SyntaxTree;

/**
 * <br/>14.11.2011
 *
 * @author Alexander Izyurov
 */
public interface Processor {
    boolean process(SyntaxTree tree, Model model) throws GrammarException;
}
