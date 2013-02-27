/*
* Copyright Alexander Izyurov 2010
*/
package org.simqle.model;

import org.simqle.parser.SyntaxTree;
import org.simqle.processor.GrammarException;
import org.simqle.util.Assert;

/**
 * <br/>13.11.2011
 *
 * @author Alexander Izyurov
 */
public class Annotation {
    private String name;

    public Annotation(SyntaxTree node) throws GrammarException {
        Assert.assertOneOf(new GrammarException("Unexpected type: "+node.getType(), node), "Annotation", node.getType());
        name = node.find("Identifier").get(0).getValue();
    }

    public String getName() {
        return name;
    }
}
