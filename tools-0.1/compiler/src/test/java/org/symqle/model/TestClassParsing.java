/*
* Copyright Alexander Izyurov 2010
*/
package org.symqle.model;

import junit.framework.TestCase;
import org.symqle.parser.SymqleParser;
import org.symqle.parser.SyntaxTree;
import org.symqle.processor.ClassCompletionProcessor;
import org.symqle.processor.ClassDeclarationProcessor;
import org.symqle.processor.GrammarException;
import org.symqle.processor.InterfaceDeclarationsProcessor;
import org.symqle.processor.Processor;
import org.symqle.util.ModelUtils;

import java.io.FileReader;
import java.util.Arrays;
import java.util.List;

/**
 * <br/>15.11.2011
 *
 * @author Alexander Izyurov
 */
public class TestClassParsing extends TestCase {

    public void testRegularClass() throws Exception {
        Model model = new Model();
        SymqleParser parser = new SymqleParser(new FileReader("src/test-data/model/SimpleClass.sdl"));
        final List<SyntaxTree> nodes = Arrays.asList(new SyntaxTree(parser.SymqleUnit(), "SimpleClass.sdl"));
        new ClassCompletionProcessor().process(nodes, model);
        final ClassDefinition selectStatement = model.getClassDef("AbstractSelectStatement");
        assertEquals(2, selectStatement.getAllMethods(model).size());
        assertEquals(2, selectStatement.getDeclaredMethods().size());
        final MethodDefinition generated = selectStatement.getDeclaredMethodBySignature("z$sqlOfSelectStatement(SqlContext)");
        assertNotNull(generated);
        assertEquals("public abstract Query<T> z$sqlOfSelectStatement(SqlContext context)", generated.declaration());
    // getDeclaredMethod and getMethod should return the same
        final MethodDefinition generated1 = selectStatement.getMethodBySignature("z$sqlOfSelectStatement(SqlContext)", model);
        assertNotNull(generated1);
        assertEquals("public abstract Query<T> z$sqlOfSelectStatement(SqlContext context)", generated1.declaration());
        assertTrue(generated.matches(generated1));
        assertTrue(generated1.matches(generated));
    }

    public void testDuplicateClass() throws Exception {
        final Model model = ModelUtils.prepareModel();
            SymqleParser parser = new SymqleParser(new FileReader("src/test-data/DuplicateClass.sdl"));
            SyntaxTree node = new SyntaxTree(parser.SymqleUnit(), "DuplicateClass.sdl");
        {
            Processor processor = new InterfaceDeclarationsProcessor();
            processor.process(Arrays.asList(node), model);
        }
            Processor processor = new ClassDeclarationProcessor();
        try {
            processor.process(Arrays.asList(node), model);
            fail("GrammarException expected here");
        } catch (GrammarException e) {
            // expected
        }
    }

    public void testStandaloneClass() throws Exception {
//        final Model model = ModelUtils.prepareModel();
//            SymqleParser parser = new SymqleParser(new FileReader("src/test-data/StandaloneClass.sdl"));
//            SyntaxTree node = new SyntaxTree(parser.SymqleUnit(), "StandaloneClass.sdl");
//        {
//            Processor processor = new InterfaceDeclarationsProcessor();
//            processor.process(node, model);
//        }
//        {
//            Processor processor = new ClassDeclarationProcessor();
//            processor.process(node, model);
//        }
//        final ClassDefinition justForFun = model.getClassDef("JustForFun");
//        assertNotNull(justForFun);
//        assertEquals(1, justForFun.getDeclaredMethods().size());
//
    }


    public void testUndefinedInterface() throws Exception {
//        final Model model = ModelUtils.prepareModel();
//            SymqleParser parser = new SymqleParser(new FileReader("src/test-data/UndefinedInterface.sdl"));
//            SyntaxTree node = new SyntaxTree(parser.SymqleUnit(), "UndefinedInterface.sdl");
//        {
//            Processor processor = new InterfaceDeclarationsProcessor();
//            processor.process(node, model);
//        }
//            Processor processor = new ClassDeclarationProcessor();
//        try {
//            processor.process(node, model);
//            new AbstractMethodsProcessor().process(model);
//            fail("ModelException expected here");
//        } catch (ModelException e) {
//            // expected
//        }
    }

    public void testGeneratedMethodConflict() throws Exception {
//        final Model model = ModelUtils.prepareModel();
//            SymqleParser parser = new SymqleParser(new FileReader("src/test-data/GeneratedMethodConflict.sdl"));
//            SyntaxTree node = new SyntaxTree(parser.SymqleUnit(), "GeneratedMethodConflict.sdl");
//        {
//            Processor processor = new InterfaceDeclarationsProcessor();
//            processor.process(node, model);
//        }
//            Processor processor = new ClassDeclarationProcessor();
//        try {
//            processor.process(node, model);
//            new AbstractMethodsProcessor().process(model);
//            fail("ModelException expected here");
//        } catch (ModelException e) {
//            assertTrue(e.getMessage(), e.getMessage().startsWith("Name clash in TestClass"));
//        }
    }

    public void testDuplicateMethod() throws Exception {
        final Model model = ModelUtils.prepareModel();
            SymqleParser parser = new SymqleParser(new FileReader("src/test-data/DuplicateMethod.sdl"));
            SyntaxTree node = new SyntaxTree(parser.SymqleUnit(), "DuplicateMEthod.sdl");
        try {
            new ClassDeclarationProcessor().process(Arrays.asList(node), model);
            fail("GrammarException expected here");
        } catch (GrammarException e) {
            assertTrue(e.getMessage().startsWith("Duplicate method: dump"));
        }
    }

}
