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
import org.simqle.processor.ProductionDeclarationProcessor;

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
public class TestClassAndAddendumParsing extends TestCase {

    public void testCorrectSdl() throws Exception {
        Model model = new Model();
        {
            SimqleParser parser = new SimqleParser(new FileReader("src/test-data/InterfaceTest.sdl"));
            SyntaxTree node = new SyntaxTree(parser.SimqleUnit(), "InterfaceTest.sdl");
            Processor processor = new InterfaceDeclarationsProcessor();
            processor.process(node, model);
        }
        {
            SimqleParser parser = new SimqleParser(new FileReader("src/test-data/ClassAndAddendumTest.sdl"));
            SyntaxTree node = new SyntaxTree(parser.SimqleUnit(), "ClassAndAddendumTest.sdl");
            Processor interfaceProcessor = new InterfaceDeclarationsProcessor();
            interfaceProcessor.process(node, model);
            Processor processor = new ClassDeclarationProcessor();
            processor.process(node, model);
            Processor productionProcessor = new ProductionDeclarationProcessor();
            productionProcessor.process(node, model);
        }
        final List<ClassPair> classDefinitionList = model.getAllClasses();
        Assert.assertEquals(2, classDefinitionList.size());
        {
            final ClassDefinition def = model.getClassPair("CursorSpecification").getBase();

            final List<String> importLines = def.getImports();
            Assert.assertTrue(importLines.contains("import java.util.Map;"));
            Assert.assertTrue(importLines.contains("import java.util.HashMap;"));
            Assert.assertEquals(2, importLines.size());
            final Body body = def.getBody();
            // declared method an 2 interface methods
            Assert.assertEquals(2, body.getMethods().size());
            Set<String> expectedMethodNames = new HashSet<String>(Arrays.asList(""));
            Assert.assertEquals(1, body.getConstructors().size());
            final ConstructorDeclaration constructor = body.getConstructors().get(0);
            // actually the constructor name should be @SelectStatement$", the assigned value will be ignored at class generation
            Assert.assertEquals("CursorSpecification", constructor.getName());
            Assert.assertEquals(1, constructor.getFormalParameters().size());
            Assert.assertEquals("final cursor_specification<T> sqlBuilder", constructor.getFormalParameters().get(0).getImage());
        }
        {
            final ClassDefinition def = model.getClassPair("CursorSpecification").getExtension();

            Assert.assertEquals("CursorSpecification", def.getPairName());
            final List<String> importLines = def.getImports();
            // by convention, all imports go to base, extention does not have imports
            Assert.assertEquals(0, importLines.size());
            final Body body = def.getBody();
            // declared method and 2 interface methods are not overridden in extension class
            Assert.assertEquals(0, body.getMethods().size());
            Set<String> expectedMethodNames = new HashSet<String>(Arrays.asList(""));
            Assert.assertEquals(1, body.getConstructors().size());
            // should have a constructor in extension class with the same parameter(s) as in the base class
            final ConstructorDeclaration constructor = body.getConstructors().get(0);
            Assert.assertEquals("CursorSpecification", constructor.getName());
            Assert.assertEquals(1, constructor.getFormalParameters().size());
            Assert.assertEquals("final cursor_specification<T> sqlBuilder", constructor.getFormalParameters().get(0).getImage());
        }

    }

}
