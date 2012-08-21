package org.simqle.model;

import org.simqle.parser.SyntaxTree;
import org.simqle.processor.GrammarException;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: lvovich
 * Date: 27.06.12
 * Time: 15:59
 * To change this template use File | Settings | File Templates.
 */
public class BaseClassDefinition extends ClassDefinition {

    public BaseClassDefinition(final SyntaxTree node, final List<SyntaxTree> importDeclarations) throws GrammarException {
        super(node, importDeclarations);
    }

    @Override
    public String getClassName() {
        return getPairName()+"$";
    }


}
