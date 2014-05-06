package org.symqle.model;

import junit.framework.TestCase;
import org.symqle.parser.SymqleParser;
import org.symqle.parser.SyntaxTree;
import org.symqle.processor.GrammarException;
import org.symqle.processor.InterfaceDeclarationsProcessor;
import org.symqle.util.ModelUtils;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;

/**
 * @author lvovich
 */
public class TestArchetypeTypeParam extends TestCase {

    public void testNoQueryParam() throws Exception {
        final Model model = ModelUtils.prepareModel();
        String source = "src/test-data/model/ArchetypeNoQueryParam.sdl";
        Reader reader = new InputStreamReader(new FileInputStream(source));
        SymqleParser parser = new SymqleParser(reader);
        final SyntaxTree syntaxTree = new SyntaxTree(parser.SymqleUnit(), source);
        try {
            new InterfaceDeclarationsProcessor().process(Arrays.asList(syntaxTree), model);
            fail("GrammarException expected");
        } catch (GrammarException e) {
            assertTrue(e.getMessage().startsWith("Query archetype requires 1 type parameter, found: 0"));
        }
    }

    public void testSqlParam() throws Exception {
        final Model model = ModelUtils.prepareModel();
        String source = "src/test-data/model/ArchetypeSqlParam.sdl";
        Reader reader = new InputStreamReader(new FileInputStream(source));
        SymqleParser parser = new SymqleParser(reader);
        final SyntaxTree syntaxTree = new SyntaxTree(parser.SymqleUnit(), source);
        try {
            new InterfaceDeclarationsProcessor().process(Arrays.asList(syntaxTree), model);
            fail("GrammarException expected");
        } catch (GrammarException e) {
            assertTrue(e.getMessage().startsWith("SqlBuilder archetype does not take type parameters, found: 1"));
        }
    }

    public void testIllegalMethodName() throws Exception {
        final Model model = ModelUtils.prepareModel();
        String source = "src/test-data/model/ArchetypeIllegalMethodName.sdl";
        Reader reader = new InputStreamReader(new FileInputStream(source));
        SymqleParser parser = new SymqleParser(reader);
        final SyntaxTree syntaxTree = new SyntaxTree(parser.SymqleUnit(), source);
        try {
            new InterfaceDeclarationsProcessor().process(Arrays.asList(syntaxTree), model);
            fail("GrammarException expected");
        } catch (GrammarException e) {
            assertTrue(e.getMessage(), e.getMessage().startsWith("Prefix \"z$sqlOf\" is reserved for generated methods"));
        }
    }

}
