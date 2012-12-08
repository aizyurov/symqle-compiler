/*
* Copyright Alexander Izyurov 2010
*/
package org.simqle.model;

import org.simqle.parser.SyntaxTree;
import org.simqle.processor.GrammarException;
import org.simqle.util.Assert;
import org.simqle.util.Utils;

import java.util.List;

/**
 * <br/>20.11.2011
 *
 * @author Alexander Izyurov
 */
public class VariableDeclarator {
    private final String name;
    private final String initializer;

    public VariableDeclarator(SyntaxTree node) throws GrammarException {
        Assert.assertOneOf(new GrammarException("Unexpected type: "+node.getType(), node), node.getType(), "VariableDeclarator");
        name = node.find("VariableDeclaratorId").get(0).getValue();
        final List<String> initializers = node.find("VariableInitializer", SyntaxTree.BODY);
        this.initializer = initializers.isEmpty() ? "" : " = " +initializers.get(0);
    }

    public String getName() {
        return name;
    }

    public String getInitializer() {
        return initializer;
    }
}
