package org.simqle.model;

import junit.framework.TestCase;
import org.simqle.parser.SimqleParser;
import org.simqle.parser.SyntaxTree;
import org.simqle.processor.GrammarException;
import org.simqle.processor.InterfaceDeclarationsProcessor;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * @author lvovich
 */
public class TestArchetypeTypeParam extends TestCase {

    public void testNoQueryParam() throws Exception {
        String source = "src/test-data/model/ArchetypeNoQueryParam.sdl";
        Reader reader = new InputStreamReader(new FileInputStream(source));
        SimqleParser parser = new SimqleParser(reader);
        final SyntaxTree syntaxTree = new SyntaxTree(parser.SimqleUnit(), source);
        Model model = new Model();
        try {
            new InterfaceDeclarationsProcessor().process(syntaxTree, model);
            fail("GrammarException expected");
        } catch (GrammarException e) {
            assertTrue(e.getMessage().startsWith("Query archetype requires 1 type parameter, found: 0"));
        }
    }

    public void testSqlParam() throws Exception {
        String source = "src/test-data/model/ArchetypeSqlParam.sdl";
        Reader reader = new InputStreamReader(new FileInputStream(source));
        SimqleParser parser = new SimqleParser(reader);
        final SyntaxTree syntaxTree = new SyntaxTree(parser.SimqleUnit(), source);
        Model model = new Model();
        try {
            new InterfaceDeclarationsProcessor().process(syntaxTree, model);
            fail("GrammarException expected");
        } catch (GrammarException e) {
            assertTrue(e.getMessage().startsWith("Sql archetype does not take type parameters, found: 1"));
        }
    }

    public void testIllegalMethodName() throws Exception {
        String source = "src/test-data/model/ArchetypeIllegalMethodName.sdl";
        Reader reader = new InputStreamReader(new FileInputStream(source));
        SimqleParser parser = new SimqleParser(reader);
        final SyntaxTree syntaxTree = new SyntaxTree(parser.SimqleUnit(), source);
        Model model = new Model();
        try {
            new InterfaceDeclarationsProcessor().process(syntaxTree, model);
            fail("GrammarException expected");
        } catch (GrammarException e) {
            assertTrue(e.getMessage(), e.getMessage().startsWith("Prefix \"z$create$\" is reserved for generated methods"));
        }
    }

}
