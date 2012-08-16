/*
* Copyright Alexander Izyurov 2010
*/
package org.simqle.generator;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.simqle.model.InterfaceDefinition;
import org.simqle.model.MethodDeclaration;
import org.simqle.model.Model;
import org.simqle.model.Type;
import org.simqle.parser.SimqleParser;
import org.simqle.parser.SyntaxTree;
import org.simqle.processor.InterfaceDeclarationsProcessor;
import org.simqle.processor.Processor;

import java.io.File;
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
        Assert.assertEquals(4, interfaceDefinitionList.size());
        final InterfaceDefinition def1 = interfaceDefinitionList.get(0);

        final String comment = def1.getComment();
        Assert.assertNotNull(comment);
        Assert.assertTrue(comment.contains("interface1 select_statement"));
        Assert.assertFalse(comment.contains("#"));

        Assert.assertEquals("select_statement", def1.getName());
        Assert.assertTrue(def1.getBody().hasMethod("z$prepare$select_statement"));
        final MethodDeclaration method1 = def1.getBody().getMethod("z$prepare$select_statement");
        Assert.assertNull(method1.getResultType());
        Assert.assertEquals("void z$prepare$select_statement(SqlContext context)", method1.getSignature());
        Assert.assertEquals(1, method1.getFormalParameters().size());
        final Type arg0type = method1.getFormalParameters().get(0).getType();
        Assert.assertEquals("SqlContext", arg0type.getNameChain().get(0).getName());

        final List<String> importLines = def1.getImportLines();
        Assert.assertTrue(importLines.contains("import org.simqle.SqlContext;"));
        Assert.assertTrue(importLines.contains("import org.simqle.Query;"));
        Assert.assertEquals(2, importLines.size());
    }

    public void testGenerate() throws Exception {
        SimqleParser parser = new SimqleParser(new FileReader("src/test-data/InterfaceTest.sdl"));
        SyntaxTree node = new SyntaxTree(parser.SimqleUnit(), "InterfaceTest.sdl");
        Model model = new Model();
        Processor processor = new InterfaceDeclarationsProcessor();
        processor.process(node, model);
        InterfaceGenerator generator = new InterfaceGenerator();
        generator.generate(model, new File("src/test-data/generated-sorces-1"));
    }
}
