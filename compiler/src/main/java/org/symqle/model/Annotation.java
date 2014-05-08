/*
* Copyright Alexander Izyurov 2010
*/
package org.symqle.model;

import org.symqle.parser.SyntaxTree;
import org.symqle.processor.GrammarException;

/**
 * Annotation. Only simple annotations (without parameters) are supported.
 * @author Alexander Izyurov
 */
public class Annotation {
    private String name;

    /**
     * Constructs from AST.
     * @param node the syntax tree
     * @throws GrammarException wrong tree
     */
    public Annotation(final SyntaxTree node) throws GrammarException {
        AssertNodeType.assertOneOf(node, "Annotation");
        name = node.find("Identifier").get(0).getValue();
    }

    /**
     * Name of this annotation.
     * @return the name
     */
    public final String getName() {
        return name;
    }
}
