package org.simqle.model;

import org.simqle.parser.SyntaxTree;
import org.simqle.processor.GrammarException;

/**
 * Created by IntelliJ IDEA.
 * User: lvovich
 * Date: 27.06.12
 * Time: 15:59
 * To change this template use File | Settings | File Templates.
 */
public class BaseClassDefinition extends ClassDefinition {

    public BaseClassDefinition(final SyntaxTree node) throws GrammarException {
        super(node);
    }

    public String getBaseClassName() {
        return getName()+"$";
    }

}
