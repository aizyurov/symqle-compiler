/*
* Copyright Alexander Izyurov 2010
*/
package org.simqle.model;

import org.simqle.parser.SyntaxTree;

/**
 * <br/>13.11.2011
 *
 * @author Alexander Izyurov
 */
public class Annotation {
    private String name;

    public Annotation(SyntaxTree node) {
        if (!node.getType().equals("Annotation")) {
            throw new IllegalArgumentException("Illegal argument: "+node);
        }
        name = node.find("Identifier").get(0).getValue();
    }

    public String getName() {
        return name;
    }
}
