/*
* Copyright Alexander Izyurov 2010
*/
package org.simqle.model;

import org.simqle.parser.SyntaxTree;
import org.simqle.util.Assert;

/**
 * <br/>13.11.2011
 *
 * @author Alexander Izyurov
 */
public class Annotation {
    private String name;

    public Annotation(SyntaxTree node) {
        Assert.assertOneOf("Annotation", node.getType());
        if (!node.getType().equals("Annotation")) {
            throw new IllegalArgumentException("Illegal argument: "+node);
        }
        name = node.find("Identifier").get(0).getValue();
    }

    public String getName() {
        return name;
    }
}
