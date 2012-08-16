/*
* Copyright Alexander Izyurov 2010
*/
package org.simqle.generator;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.simqle.model.*;
import org.simqle.parser.SimqleParser;
import org.simqle.parser.SyntaxTree;
import org.simqle.processor.ClassDeclarationProcessor;
import org.simqle.processor.InterfaceDeclarationsProcessor;
import org.simqle.processor.Processor;

import java.io.FileReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <br/>15.11.2011
 *
 * @author Alexander Izyurov
 */
public class TestClassParsing extends TestCase {

    public void testCorrectSdl() throws Exception {
        Model model = new Model();
        {
            SimqleParser parser = new SimqleParser(new FileReader("src/test-data/InterfaceTest.sdl"));
            SyntaxTree node = new SyntaxTree(parser.SimqleUnit(), "InterfaceTest.sdl");
            Processor processor = new InterfaceDeclarationsProcessor();
            processor.process(node, model);
        }
        {
            SimqleParser parser = new SimqleParser(new FileReader("src/test-data/ClassTest.sdl"));
            SyntaxTree node = new SyntaxTree(parser.SimqleUnit(), "ClassTest.sdl");
            Processor processor = new ClassDeclarationProcessor();
            processor.process(node, model);
        }
        final List<ClassPair> classDefinitionList = model.getAllClasses();
        Assert.assertEquals(1, classDefinitionList.size());
        {
            final ClassDefinition def = classDefinitionList.get(0).getBase();

            Assert.assertEquals("SelectStatement", def.getPairName());
            final List<String> importLines = def.getImports();
            Assert.assertTrue(importLines.contains("import java.util.List;"));
            Assert.assertEquals(4, importLines.size());
            Assert.assertTrue(importLines.contains("import java.util.LinkedList;"));
            final Body body = def.getBody();
            // declared method an 2 interface methods
            Assert.assertEquals(3, body.getMethods().size());
            Set<String> expectedMethodNames = new HashSet<String>(Arrays.asList(""));
            Assert.assertEquals(1, body.getConstructors().size());
            final ConstructorDeclaration constructor = body.getConstructors().get(0);
            // actually the constructor name should be @SelectStatement$", the assigned value will be ignored at class generation
            Assert.assertEquals("SelectStatement", constructor.getName());
            Assert.assertEquals(1, constructor.getFormalParameters().size());
            Assert.assertEquals("final select_statement<T> sqlBuilder", constructor.getFormalParameters().get(0).getImage());
        }
        {
            final ClassDefinition def = classDefinitionList.get(0).getExtension();

            Assert.assertEquals("SelectStatement", def.getPairName());
            final List<String> importLines = def.getImports();
            Assert.assertEquals(0, importLines.size());
            final Body body = def.getBody();
            // declared method an 2 interface methods are not overridden in extension class
            Assert.assertEquals(0, body.getMethods().size());
            Set<String> expectedMethodNames = new HashSet<String>(Arrays.asList(""));
            Assert.assertEquals(1, body.getConstructors().size());
            // should have a constructor in extension class with the same parameter(s) as in the base class
            final ConstructorDeclaration constructor = body.getConstructors().get(0);
            Assert.assertEquals("SelectStatement", constructor.getName());
            Assert.assertEquals(1, constructor.getFormalParameters().size());
            Assert.assertEquals("final select_statement<T> sqlBuilder", constructor.getFormalParameters().get(0).getImage());
        }

    }

}