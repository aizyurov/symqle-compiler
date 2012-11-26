/*
* Copyright Alexander Izyurov 2010
*/
package org.simqle.generator;

import junit.framework.TestCase;
import org.simqle.model.ClassDefinition;
import org.simqle.model.Model;
import org.simqle.parser.SimqleParser;
import org.simqle.parser.SyntaxTree;
import org.simqle.processor.ClassDeclarationProcessor;
import org.simqle.processor.GrammarException;
import org.simqle.processor.InterfaceDeclarationsProcessor;
import org.simqle.processor.Processor;

import java.io.FileReader;

/**
 * <br/>15.11.2011
 *
 * @author Alexander Izyurov
 */
public class TestClassParsing extends TestCase {

    public void testJustClass() throws Exception {
        Model model = new Model();
        SimqleParser parser = new SimqleParser(new FileReader("src/test-data/model/SimpleClass.sdl"));
        SyntaxTree node = new SyntaxTree(parser.SimqleUnit(), "ClassTest.sdl");
        Processor processor = new ClassDeclarationProcessor();
        processor.process(node, model);
    }

    public void testCorrectSdl() throws Exception {
//        Model model = new Model();
//        {
//            SimqleParser parser = new SimqleParser(new FileReader("src/test-data/InterfaceTest.sdl"));
//            SyntaxTree node = new SyntaxTree(parser.SimqleUnit(), "InterfaceTest.sdl");
//            Processor processor = new InterfaceDeclarationsProcessor();
//            processor.process(node, model);
//        }
//        {
//            SimqleParser parser = new SimqleParser(new FileReader("src/test-data/ClassTest.sdl"));
//            SyntaxTree node = new SyntaxTree(parser.SimqleUnit(), "ClassTest.sdl");
//            Processor processor = new ClassDeclarationProcessor();
//            processor.process(node, model);
//        }
//        final List<ClassDefinition> classDefinitionList = model.getAllClasses();
//        assertEquals(1, classDefinitionList.size());
//        {
//            final ClassDefinition def = classDefinitionList.get(0);
//
//            assertEquals("SelectStatement", def.getName());
//            assertEquals("public", def.getAccessModifier());
//            assertEquals(1, def.getOtherModifiers().size());
//            assertEquals("abstract", def.getOtherModifiers().get(0));
//            final List<Annotation> annotations = def.getAnnotations();
//            assertEquals(1, annotations.size());
//            assertEquals("ThreadSafe", annotations.get(0).getName());
//            final List<Type> implementedInterfaces = def.getImplementedInterfaces();
//            assertEquals(1, implementedInterfaces.size());
//            assertEquals("select_statement", implementedInterfaces.get(0).getNameChain().get(0).getName());
//            final Set<String> importLines = def.getImports();
//            assertTrue(importLines.contains("import java.util.List;"));
//            assertEquals(3, importLines.size());
//
//            final Body body = def.getBody();
//            // declared method an 2 interface methods
//            assertEquals(3, body.getMethods().size());
//            assertEquals(1, body.getConstructors().size());
//            final ConstructorDeclaration constructor = body.getConstructors().get(0);
//            // actually the constructor name should be @SelectStatement$", the assigned value will be ignored at class generation
//            assertEquals("SelectStatement", constructor.getName());
//            assertEquals(1, constructor.getFormalParameters().size());
//            assertEquals("final select_statement<T> sqlBuilder", constructor.getFormalParameters().get(0).getImage());
//        }
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
        final ClassDefinition justForFun = model.getClassDef("JustForFun");
        assertNotNull(justForFun);
        assertEquals(1, justForFun.getDeclaredMethods().size());

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
//        Model model = new Model();
//        SimqleParser parser = new SimqleParser(new FileReader("src/test-data/ComplexInterface.sdl"));
//        SyntaxTree node = new SyntaxTree(parser.SimqleUnit(), "ComplexInterface.sdl");
//        new InterfaceDeclarationsProcessor().process(node, model);
//        new ClassDeclarationProcessor().process(node, model);
//        final ClassDefinition baseClass = model.getClassDef("TestClass");
//        final MethodDeclaration dumpMethod = baseClass.getBody().getMethod("dump(OutputStream,boolean)");
//        assertNotNull(dumpMethod);
//        assertEquals(2, dumpMethod.getFormalParameters().size());
//        assertEquals("final OutputStream os", dumpMethod.getFormalParameters().get(0).getImage());
//        assertEquals("final boolean compress", dumpMethod.getFormalParameters().get(1).getImage());
//        assertEquals("{ sqlBuilder.dump(os, compress); }", TestUtils.normalizeFormatting(dumpMethod.getMethodBody()));
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
