package org.symqle.model;

import junit.framework.TestCase;
import org.symqle.parser.SymqleParser;
import org.symqle.parser.SyntaxTree;
import org.symqle.processor.InterfaceJavadocProcessor;
import org.symqle.util.ModelUtils;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: aizyurov
 * Date: 02.12.2012
 * Time: 18:35:36
 * To change this template use File | Settings | File Templates.
 */
public class ImportsTest extends TestCase {

    public void testImports() throws Exception {
        final Model model = ModelUtils.prepareModel();
        String source = "src/test-data/model/Imports.sdl";
        Reader reader = new InputStreamReader(new FileInputStream(source));
        SymqleParser parser = new SymqleParser(reader);
        final List<SyntaxTree> syntaxTrees = Arrays.asList(new SyntaxTree(parser.SymqleUnit(), source));
        new InterfaceJavadocProcessor().process(syntaxTrees, model);
        final ClassDefinition cursorSpec = model.getClassDef("AbstractCursorSpecification");
        final String cursorSpecString = cursorSpec.toString();
        assertTrue(cursorSpecString, cursorSpecString.startsWith("import org.symqle.common.QueryBuilder;"));
        assertTrue(cursorSpecString, cursorSpecString.contains("import org.symqle.common.SqlContext;"));
        final ClassDefinition symqle = model.getClassDef("Symqle");
        final String symqleString = symqle.toString();
        assertTrue(symqleString, symqleString.startsWith("import java.util.ArrayList;"));
        assertTrue(symqleString, symqleString.contains("import java.util.List;"));
    }

}