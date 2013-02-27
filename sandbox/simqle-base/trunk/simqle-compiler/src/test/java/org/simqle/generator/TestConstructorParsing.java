package org.simqle.generator;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.simqle.model.ConstructorDeclaration;
import org.simqle.parser.SimqleParser;
import org.simqle.parser.SyntaxTree;

import java.io.FileReader;

/**
 * Created by IntelliJ IDEA.
 * User: lvovich
 * Date: 26.06.12
 * Time: 14:12
 * To change this template use File | Settings | File Templates.
 */
public class TestConstructorParsing extends TestCase {

    public void testParsing() throws Exception {
        SimqleParser parser = new SimqleParser(new FileReader("src/test-data/ConstructorCode.sdl"));
        SyntaxTree node = new SyntaxTree(parser.ConstructorDeclaration(), "ConstructorCode.sdl");
        ConstructorDeclaration constructorDeclaration = new ConstructorDeclaration(node);
        Assert.assertEquals("Expression", constructorDeclaration.getName());
    }
}
