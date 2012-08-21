/*
* Copyright Alexander Izyurov 2010
*/
package org.simqle.generator;

import junit.framework.TestCase;
import org.simqle.model.*;
import org.simqle.parser.SimqleParser;
import org.simqle.parser.SyntaxTree;
import org.simqle.processor.ClassDeclarationProcessor;
import org.simqle.processor.GrammarException;
import org.simqle.processor.InterfaceDeclarationsProcessor;
import org.simqle.processor.Processor;
import org.simqle.test.TestUtils;

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
        assertEquals(1, classDefinitionList.size());
        {
            final ClassDefinition def = classDefinitionList.get(0).getBase();

            assertEquals("SelectStatement", def.getPairName());
            assertEquals("public", def.getAccessModifier());
            assertEquals(0, def.getOtherModifiers().size());
            final List<Annotation> annotations = def.getAnnotations();
            assertEquals(1, annotations.size());
            assertEquals("ThreadSafe", annotations.get(0).getName());
            final List<Type> implementedInterfaces = def.getImplementedInterfaces();
            assertEquals(1, implementedInterfaces.size());
            assertEquals("select_statement", implementedInterfaces.get(0).getNameChain().get(0).getName());
            final List<String> importLines = classDefinitionList.get(0).getPublishedImports();
            assertTrue(importLines.contains("import java.util.List;"));
            assertEquals(3, importLines.size());

            final List<String> internalImportLines = classDefinitionList.get(0).getInternalImports();
            assertTrue(internalImportLines.contains("import java.util.LinkedList;"));
            assertEquals(1, internalImportLines.size());
            final Body body = def.getBody();
            // declared method an 2 interface methods
            assertEquals(3, body.getMethods().size());
            Set<String> expectedMethodNames = new HashSet<String>(Arrays.asList(""));
            assertEquals(1, body.getConstructors().size());
            final ConstructorDeclaration constructor = body.getConstructors().get(0);
            // actually the constructor name should be @SelectStatement$", the assigned value will be ignored at class generation
            assertEquals("SelectStatement", constructor.getName());
            assertEquals(1, constructor.getFormalParameters().size());
            assertEquals("final select_statement<T> sqlBuilder", constructor.getFormalParameters().get(0).getImage());
        }
        {
            final ClassDefinition def = classDefinitionList.get(0).getExtension();

            assertEquals("SelectStatement", def.getPairName());
            assertEquals("public", def.getAccessModifier());
            assertEquals(0, def.getOtherModifiers().size());
            final List<Annotation> annotations = def.getAnnotations();
            assertEquals(0, annotations.size());
            final List<Type> implementedInterfaces = def.getImplementedInterfaces();
            assertEquals(0, implementedInterfaces.size());
            final Body body = def.getBody();
            // declared method an 2 interface methods are not overridden in extension class
            assertEquals(0, body.getMethods().size());
            Set<String> expectedMethodNames = new HashSet<String>(Arrays.asList(""));
            // at this point there should be no extension class constructors;
            // they are added in ProductionProcessor
            assertEquals(0, body.getConstructors().size());
            // should have a constructor in extension class with the same parameter(s) as in the base class
//            final ConstructorDeclaration constructor = body.getConstructors().get(0);
//            assertEquals("SelectStatement", constructor.getName());
//            assertEquals(1, constructor.getFormalParameters().size());
//            assertEquals("final select_statement<T> sqlBuilder", constructor.getFormalParameters().get(0).getImage());
        }

    }

    public void testDuplicateClass() throws Exception {
        Model model = new Model();
            SimqleParser parser = new SimqleParser(new FileReader("src/test-data/DuplicateClass.sdl"));
            SyntaxTree node = new SyntaxTree(parser.SimqleUnit(), "DuplicateClass.sdl");
        {
            Processor processor = new InterfaceDeclarationsProcessor();
            processor.process(node, model);
        }
            Processor processor = new ClassDeclarationProcessor();
        try {
            processor.process(node, model);
            fail("GrammarException expected here");
        } catch (GrammarException e) {
            // expected
        }
    }

    public void testStandaloneClass() throws Exception {
        Model model = new Model();
            SimqleParser parser = new SimqleParser(new FileReader("src/test-data/StandaloneClass.sdl"));
            SyntaxTree node = new SyntaxTree(parser.SimqleUnit(), "StandaloneClass.sdl");
        {
            Processor processor = new InterfaceDeclarationsProcessor();
            processor.process(node, model);
        }
        {
            Processor processor = new ClassDeclarationProcessor();
            processor.process(node, model);
        }
        final ClassPair justForFun = model.getClassPair("JustForFun");
        assertNotNull(justForFun);
        assertEquals(1, justForFun.getBase().getBody().getMethods().size());
        assertEquals(0, justForFun.getExtension().getBody().getMethods().size());

    }

    public void testMultipleInterfaces() throws Exception {
        Model model = new Model();
            SimqleParser parser = new SimqleParser(new FileReader("src/test-data/MultipleInterfaces.sdl"));
            SyntaxTree node = new SyntaxTree(parser.SimqleUnit(), "MultipleInterfaces.sdl");
        {
            Processor processor = new InterfaceDeclarationsProcessor();
            processor.process(node, model);
        }
        {
            Processor processor = new ClassDeclarationProcessor();
            processor.process(node, model);
        }
        final ClassPair classPair = model.getClassPair("Column");
        final ClassDefinition base = classPair.getBase();
        assertEquals(2, base.getBody().getFields().size());
        for (FieldDeclaration field: base.getBody().getFields()) {
            if (field.getDeclarators().size()==1 && field.getDeclarators().get(0).getName().equals("nameBuilder")) {
                assertEquals("private final column_name nameBuilder;", TestUtils.normalizeFormatting(field.getImage()));
            } else if (field.getDeclarators().size()==1 && field.getDeclarators().get(0).getName().equals("tableColumnBuilder")) {
                assertEquals("private final table_column<T> tableColumnBuilder;", TestUtils.normalizeFormatting(field.getImage()));
            } else {
                fail("Unexpected field: "+field.getImage());
            }
        }

    }

    public void testUndefinedInterface() throws Exception {
        Model model = new Model();
            SimqleParser parser = new SimqleParser(new FileReader("src/test-data/UndefinedInterface.sdl"));
            SyntaxTree node = new SyntaxTree(parser.SimqleUnit(), "UndefinedInterface.sdl");
        {
            Processor processor = new InterfaceDeclarationsProcessor();
            processor.process(node, model);
        }
            Processor processor = new ClassDeclarationProcessor();
        try {
            processor.process(node, model);
            fail("GrammarException expected here");
        } catch (GrammarException e) {
            // expected
        }
    }

    public void testMultipleInterfacesConflict() throws Exception {
        Model model = new Model();
            SimqleParser parser = new SimqleParser(new FileReader("src/test-data/MultipleInterfacesConflict.sdl"));
            SyntaxTree node = new SyntaxTree(parser.SimqleUnit(), "MultipleInterfacesConflict.sdl");
        {
            Processor processor = new InterfaceDeclarationsProcessor();
            processor.process(node, model);
        }
            Processor processor = new ClassDeclarationProcessor();
        try {
            processor.process(node, model);
            fail("GrammarException expected here");
        } catch (GrammarException e) {
            // expected
            assertTrue(e.getMessage().startsWith("Duplicate variable sqlBuilder"));
        }
    }

    public void testComplexInterface() throws Exception {
        Model model = new Model();
        SimqleParser parser = new SimqleParser(new FileReader("src/test-data/ComplexInterface.sdl"));
        SyntaxTree node = new SyntaxTree(parser.SimqleUnit(), "ComplexInterface.sdl");
        new InterfaceDeclarationsProcessor().process(node, model);
        new ClassDeclarationProcessor().process(node, model);
        final ClassDefinition baseClass = model.getClassPair("TestClass").getBase();
        final MethodDeclaration dumpMethod = baseClass.getBody().getMethod("dump");
        assertNotNull(dumpMethod);
        assertEquals("public void dump(final OutputStream os, final boolean compress)", TestUtils.normalizeFormatting(dumpMethod.getSignature()));
        assertEquals("{ sqlBuilder.dump(os, compress); }", TestUtils.normalizeFormatting(dumpMethod.getMethodBody()));
    }

    public void testGeneratedMethodConflict() throws Exception {
        Model model = new Model();
            SimqleParser parser = new SimqleParser(new FileReader("src/test-data/GeneratedMethodConflict.sdl"));
            SyntaxTree node = new SyntaxTree(parser.SimqleUnit(), "GeneratedMethodConflict.sdl");
        {
            Processor processor = new InterfaceDeclarationsProcessor();
            processor.process(node, model);
        }
            Processor processor = new ClassDeclarationProcessor();
        try {
            processor.process(node, model);
            fail("GrammarException expected here");
        } catch (GrammarException e) {
            assertTrue(e.getMessage().startsWith("Method \"dump\" already defined but generation requested"));
        }
    }

    public void testDuplicateMethod() throws Exception {
        Model model = new Model();
            SimqleParser parser = new SimqleParser(new FileReader("src/test-data/DuplicateMethod.sdl"));
            SyntaxTree node = new SyntaxTree(parser.SimqleUnit(), "DuplicateMEthod.sdl");
            new InterfaceDeclarationsProcessor().process(node, model);
        try {
            new ClassDeclarationProcessor().process(node, model);
            fail("GrammarException expected here");
        } catch (GrammarException e) {
            assertTrue(e.getMessage().startsWith("Duplicate method: dump"));
        }
    }

    public void testDuplicateField() throws Exception {
        Model model = new Model();
            SimqleParser parser = new SimqleParser(new FileReader("src/test-data/DuplicateField.sdl"));
            SyntaxTree node = new SyntaxTree(parser.SimqleUnit(), "DuplicateField.sdl");
            new InterfaceDeclarationsProcessor().process(node, model);
        try {
            new ClassDeclarationProcessor().process(node, model);
            fail("GrammarException expected here");
        } catch (GrammarException e) {
            assertTrue(e.getMessage().startsWith("Duplicate field: variable"));
        }
    }

}
