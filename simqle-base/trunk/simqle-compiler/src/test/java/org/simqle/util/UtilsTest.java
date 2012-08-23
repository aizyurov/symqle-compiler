package org.simqle.util;

import junit.framework.TestCase;
import org.simqle.model.ClassDefinition;
import org.simqle.parser.SimqleParser;
import org.simqle.parser.SyntaxTree;
import org.simqle.processor.GrammarException;

import java.io.FileReader;

/**
 * Created by IntelliJ IDEA.
 * User: lvovich
 * Date: 23.08.12
 * Time: 16:39
 * To change this template use File | Settings | File Templates.
 */
public class UtilsTest extends TestCase {

    public void testNoInstances() {
        try {
            new Utils();
        } catch (RuntimeException e) {
            assertEquals("No instances: utility class", e.getMessage());
        }
    }

    public void testAbstractClass() throws Exception {
        SimqleParser parser = new SimqleParser(new FileReader("src/test-data/InterfaceTest.sdl"));
        SyntaxTree node = new SyntaxTree(parser.SimqleUnit(), "InterfaceTest.sdl");
        assertFalse(node.find("SimqleDeclarationBlock").isEmpty());
        try {
            Utils.convertChildren(node, "SimqleDeclarationBlock", AbstractSimqleDeclaration.class);
            fail("RuntimeException expected");
        } catch (RuntimeException e) {
            assertEquals("Internal error", e.getMessage());
        }
    }

    public void testErroneousClass() throws Exception {
        SimqleParser parser = new SimqleParser(new FileReader("src/test-data/InterfaceTest.sdl"));
        SyntaxTree node = new SyntaxTree(parser.SimqleUnit(), "InterfaceTest.sdl");
        assertFalse(node.find("SimqleDeclarationBlock").isEmpty());
        try {
            Utils.convertChildren(node, "SimqleDeclarationBlock", BadClass.class);
            fail("RuntimeException expected");
        } catch (RuntimeException e) {
            assertEquals("Internal error", e.getMessage());
        }
    }

    public void testPrivateConstructor() throws Exception {
        SimqleParser parser = new SimqleParser(new FileReader("src/test-data/InterfaceTest.sdl"));
        SyntaxTree node = new SyntaxTree(parser.SimqleUnit(), "InterfaceTest.sdl");
        assertFalse(node.find("SimqleDeclarationBlock").isEmpty());
        try {
            Utils.convertChildren(node, "SimqleDeclarationBlock", NoPublicConstructorSimqleDeclaration.class);
            fail("RuntimeException expected");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage(), e.getMessage().startsWith("Internal error"));
        }
    }

    public void testWrongConverterClass() throws Exception {
        SimqleParser parser = new SimqleParser(new FileReader("src/test-data/InterfaceTest.sdl"));
        SyntaxTree node = new SyntaxTree(parser.SimqleUnit(), "InterfaceTest.sdl");
        final String path = "SimqleDeclarationBlock.SimqleDeclaration.SimqleInterfaceDeclaration";
        assertFalse(node.find(path).isEmpty());
        try {
            Utils.convertChildren(node, path, ClassDefinition.class);
            fail("GrammarException expected");
        } catch (GrammarException e) {
            assertTrue(e.getMessage(), e.getMessage().startsWith("Unexpected type: SimqleInterfaceDeclaration"));
        }
    }

    private static abstract class AbstractSimqleDeclaration {
        public AbstractSimqleDeclaration(final SyntaxTree node) {
        }
    }


    private static abstract class BadClass {
        public BadClass(final SyntaxTree node) {
            throw new NullPointerException();
        }
    }


}
