/*
* Copyright Alexander Izyurov 2010
*/
package org.simqle.generator;

import junit.framework.TestCase;
import org.simqle.model.InterfaceDefinition;
import org.simqle.model.MethodDeclaration;
import org.simqle.model.Model;
import org.simqle.parser.SimqleParser;
import org.simqle.parser.SyntaxTree;
import org.simqle.processor.GrammarException;
import org.simqle.processor.InterfaceDeclarationsProcessor;
import org.simqle.processor.Processor;
import org.simqle.test.TestUtils;

import java.io.FileReader;
import java.util.List;

/**
 * <br/>15.11.2011
 *
 * @author Alexander Izyurov
 */
public class TestInterfaceParsing extends TestCase {

    public void testCorrectSdl() throws Exception {
        SimqleParser parser = new SimqleParser(new FileReader("src/test-data/InterfaceTest.sdl"));
        SyntaxTree node = new SyntaxTree(parser.SimqleUnit(), "InterfaceTest.sdl");
        Model model = new Model();
        Processor processor = new InterfaceDeclarationsProcessor();
        processor.process(node, model);
        final List<InterfaceDefinition> interfaceDefinitionList = model.getAllInterfaces();
        assertEquals(4, interfaceDefinitionList.size());
        final InterfaceDefinition def1 = interfaceDefinitionList.get(0);

        final String comment = def1.getComment();
        assertNotNull(comment);
        assertTrue(comment.contains("interface1 select_statement"));
        assertFalse(comment.contains("#"));

        assertEquals("select_statement", def1.getName());
        assertTrue(def1.getBody().hasMethod("z$prepare$select_statement"));
        final MethodDeclaration method1 = def1.getBody().getMethod("z$prepare$select_statement");
        assertNull(method1.getResultType());
        assertEquals(1, method1.getFormalParameters().size());
        assertEquals("final SqlContext context", method1.getFormalParameters().get(0).getImage());
        assertNull(method1.getResultType());

        final List<String> importLines = def1.getImportLines();
        assertTrue(importLines.contains("import org.simqle.SqlContext;"));
        assertTrue(importLines.contains("import org.simqle.Query;"));
        assertTrue(importLines.contains("import java.util.List;"));
        assertEquals(3, importLines.size());

        assertEquals(1, def1.getBody().getOtherDeclarations().size());
        assertEquals("boolean YES = true;", TestUtils.normalizeFormatting(def1.getBody().getOtherDeclarations().get(0)));
    }

    public void testExplicitValueMethod() throws Exception {
        SimqleParser parser = new SimqleParser(new FileReader("src/test-data/ExplicitValueMethodDeclaration.sdl"));
        SyntaxTree node = new SyntaxTree(parser.SimqleUnit(), "ExplicitValueMethodDeclaration.sdl");
        final Model model = new Model();
        try {
            new InterfaceDeclarationsProcessor().process(node, model);
            fail("GrammarException expected");
        } catch (GrammarException e) {
            // expected
            assertTrue(e.getMessage().startsWith("Method \"value\" cannot be defined explicitly"));
        }
    }

    public void testExplicitPrepareMethod() throws Exception {
        SimqleParser parser = new SimqleParser(new FileReader("src/test-data/ExplicitPrepareMethodDeclaration.sdl"));
        SyntaxTree node = new SyntaxTree(parser.SimqleUnit(), "ExplicitPrepareMethodDeclaration.sdl");
        final Model model = new Model();
        try {
            new InterfaceDeclarationsProcessor().process(node, model);
            fail("GrammarException expected");
        } catch (GrammarException e) {
            // expected
            assertTrue(e.getMessage().startsWith("Method \"z$prepare$expression\" cannot be defined explicitly"));
        }
    }

    public void testExplicitCreateMethod() throws Exception {
        SimqleParser parser = new SimqleParser(new FileReader("src/test-data/ExplicitCreateMethodDeclaration.sdl"));
        SyntaxTree node = new SyntaxTree(parser.SimqleUnit(), "ExplicitCreateMethodDeclaration.sdl");
        final Model model = new Model();
        try {
            new InterfaceDeclarationsProcessor().process(node, model);
            fail("GrammarException expected");
        } catch (GrammarException e) {
            // expected
            assertTrue(e.getMessage().startsWith("Method \"z$create$expression\" cannot be defined explicitly"));
        }
    }

    public void testDuplicateInterface() throws Exception {
        SimqleParser parser = new SimqleParser(new FileReader("src/test-data/DuplicateInterface.sdl"));
        SyntaxTree node = new SyntaxTree(parser.SimqleUnit(), "DuplicateInterface.sdl");
        final Model model = new Model();
        try {
            new InterfaceDeclarationsProcessor().process(node, model);
            fail("GrammarException expected");
        } catch (GrammarException e) {
            assertTrue(e.getMessage().startsWith("Duplicate interface: test_interface"));
        }
    }

    public void testDuplicateMethodInInterface() throws Exception {
        SimqleParser parser = new SimqleParser(new FileReader("src/test-data/InterfaceMethodOverloading.sdl"));
        SyntaxTree node = new SyntaxTree(parser.SimqleUnit(), "InterfaceMethodOverloading.sdl");
        final Model model = new Model();
        try {
            new InterfaceDeclarationsProcessor().process(node, model);
            fail("GrammarException expected");
        } catch (GrammarException e) {
            assertTrue(e.getMessage().startsWith("Method overloading is not allowed in Simqle: getValue"));
        }
    }
}
