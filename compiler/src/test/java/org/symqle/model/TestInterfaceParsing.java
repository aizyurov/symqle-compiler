package org.symqle.model;

import junit.framework.TestCase;
import org.symqle.parser.ParseException;
import org.symqle.parser.SymqleParser;
import org.symqle.parser.SyntaxTree;
import org.symqle.processor.GrammarException;
import org.symqle.processor.InterfaceDeclarationsProcessor;
import org.symqle.util.ModelUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: lvovich
 * Date: 26.11.12
 * Time: 19:58
 * To change this template use File | Settings | File Templates.
 */
public class TestInterfaceParsing extends TestCase {

    private SyntaxTree readSyntaxTree() throws FileNotFoundException, ParseException {
        String source = "src/test-data/model/Interfaces.sdl";
        Reader reader = new InputStreamReader(new FileInputStream(source));
        SymqleParser parser = new SymqleParser(reader);
        return new SyntaxTree(parser.SymqleUnit(), source);
    }

    public void testScalar() throws Exception {
        final SyntaxTree syntaxTree = readSyntaxTree();
        Model model = new Model();
        new InterfaceDeclarationsProcessor().process(Arrays.asList(syntaxTree), model);
        final InterfaceDefinition scalar = model.getInterface("Scalar");
        assertEquals("Scalar", scalar.getName());
        assertEquals(2, scalar.getDeclaredMethods().size());
        assertEquals(2, scalar.getAllMethods(model).size());
        {
            final MethodDefinition valueMethod;
            valueMethod = scalar.getDeclaredMethodBySignature("value()");
            assertEquals("", valueMethod.getAccessModifier());
            assertEquals(0, valueMethod.getOtherModifiers().size());
            assertEquals("T value() throws SQLException", valueMethod.declaration());
            assertEquals("T value() throws SQLException;", valueMethod.toString().trim());
            assertTrue(valueMethod.isPublic());
            assertTrue(valueMethod.isAbstract());
        }
        {
            final MethodDefinition paramMethod = scalar.getDeclaredMethodBySignature("param(Object)");
            assertEquals("", paramMethod.getAccessModifier());
            assertEquals(0, paramMethod.getOtherModifiers().size());
            assertEquals("ValueExpression<T> param(T value)", paramMethod.declaration());
            assertEquals("ValueExpression<T> param(T value);", paramMethod.toString().trim());
            assertTrue(paramMethod.isPublic());
            assertTrue(paramMethod.isAbstract());
        }
    }


    public void testChild1() throws Exception {
        final SyntaxTree syntaxTree = readSyntaxTree();
        final Model model = ModelUtils.prepareModel();
        new InterfaceDeclarationsProcessor().process(Arrays.asList(syntaxTree), model);
        final InterfaceDefinition child = model.getInterface("Child1");
        assertEquals("Child1", child.getName());
        assertEquals(1, child.getDeclaredMethods().size());
        assertEquals(3, child.getAllMethods(model).size());
        {
            final MethodDefinition myMethod;
            myMethod = child.getDeclaredMethodBySignature("myMethod(int)");
            assertEquals("", myMethod.getAccessModifier());
            assertEquals(0, myMethod.getOtherModifiers().size());
            assertEquals("Collection<T> myMethod(int i) throws ArrayIndexOutOfBoundsException, NoSuchElementException", myMethod.declaration());
            assertEquals("Collection<T> myMethod(int i) throws ArrayIndexOutOfBoundsException, NoSuchElementException;", myMethod.toString().trim());
            assertTrue(myMethod.isPublic());
            assertTrue(myMethod.isAbstract());
        }
        {
            final MethodDefinition valueMethod;
            valueMethod = child.getMethodBySignature("value()", model);
            assertEquals("", valueMethod.getAccessModifier());
            assertEquals(1, valueMethod.getOtherModifiers().size());
            assertEquals("volatile T value() throws SQLException", valueMethod.declaration());
            assertEquals("volatile T value() throws SQLException;", valueMethod.toString().trim());
            assertTrue(valueMethod.isPublic());
            assertTrue(valueMethod.isAbstract());
        }
        {
            final MethodDefinition paramMethod = child.getMethodBySignature("param(Object)", model);
            assertEquals("", paramMethod.getAccessModifier());
            assertEquals(1, paramMethod.getOtherModifiers().size());
            assertEquals("volatile ValueExpression<T> param(T value)", paramMethod.declaration());
            assertEquals("volatile ValueExpression<T> param(T value);", paramMethod.toString().trim());
            assertTrue(paramMethod.isPublic());
            assertTrue(paramMethod.isAbstract());
        }
    }

    public void testChild2() throws Exception {
        final SyntaxTree syntaxTree = readSyntaxTree();
        final Model model = ModelUtils.prepareModel();
        new InterfaceDeclarationsProcessor().process(Arrays.asList(syntaxTree), model);
        final InterfaceDefinition child = model.getInterface("Child2");
        assertEquals("Child2", child.getName());
        assertEquals(0, child.getDeclaredMethods().size());
        assertEquals(2, child.getAllMethods(model).size());
        {
            final MethodDefinition valueMethod;
            valueMethod = child.getMethodBySignature("value()", model);
            assertEquals("", valueMethod.getAccessModifier());
            assertEquals(1, valueMethod.getOtherModifiers().size());
            assertEquals("volatile Boolean value() throws SQLException", valueMethod.declaration());
            assertEquals("volatile Boolean value() throws SQLException;", valueMethod.toString().trim());
            assertTrue(valueMethod.isPublic());
            assertTrue(valueMethod.isAbstract());
        }
        {
            final MethodDefinition paramMethod = child.getMethodBySignature("param(Boolean)", model);
            assertEquals("", paramMethod.getAccessModifier());
            assertEquals(1, paramMethod.getOtherModifiers().size());
            assertEquals("volatile ValueExpression<Boolean> param(Boolean value)", paramMethod.declaration());
            assertEquals("volatile ValueExpression<Boolean> param(Boolean value);", paramMethod.toString().trim());
            assertTrue(paramMethod.isPublic());
            assertTrue(paramMethod.isAbstract());
        }
    }

    public void testChild3() throws Exception {
        final SyntaxTree syntaxTree = readSyntaxTree();
        final Model model = ModelUtils.prepareModel();
        new InterfaceDeclarationsProcessor().process(Arrays.asList(syntaxTree), model);
        final InterfaceDefinition child = model.getInterface("Child3");
        assertEquals("Child3", child.getName());
        assertEquals(0, child.getDeclaredMethods().size());
        assertEquals(2, child.getAllMethods(model).size());
        {
            final MethodDefinition valueMethod;
            valueMethod = child.getMethodBySignature("value()", model);
            assertEquals("", valueMethod.getAccessModifier());
            assertEquals(1, valueMethod.getOtherModifiers().size());
            assertEquals("volatile List<T> value() throws SQLException", valueMethod.declaration());
            assertEquals("volatile List<T> value() throws SQLException;", valueMethod.toString().trim());
            assertTrue(valueMethod.isPublic());
            assertTrue(valueMethod.isAbstract());
        }
        {
            final MethodDefinition paramMethod = child.getMethodBySignature("param(List)", model);
            assertEquals("", paramMethod.getAccessModifier());
            assertEquals(1, paramMethod.getOtherModifiers().size());
            assertEquals("volatile ValueExpression<List<T>> param(List<T> value)", paramMethod.declaration());
            assertEquals("volatile ValueExpression<List<T>> param(List<T> value);", paramMethod.toString().trim());
            assertTrue(paramMethod.isPublic());
            assertTrue(paramMethod.isAbstract());
        }
    }

    public void testChild4() throws Exception {
        final SyntaxTree syntaxTree = readSyntaxTree();
        final Model model = ModelUtils.prepareModel();
        new InterfaceDeclarationsProcessor().process(Arrays.asList(syntaxTree), model);
        final InterfaceDefinition child = model.getInterface("Child4");
        assertEquals("Child4", child.getName());
        assertEquals(0, child.getDeclaredMethods().size());
        assertEquals(2, child.getAllMethods(model).size());
        {
            final MethodDefinition valueMethod;
            valueMethod = child.getMethodBySignature("value()", model);
            assertEquals("", valueMethod.getAccessModifier());
            assertEquals(1, valueMethod.getOtherModifiers().size());
            assertEquals("volatile List<? extends T> value() throws SQLException", valueMethod.declaration());
            assertEquals("volatile List<? extends T> value() throws SQLException;", valueMethod.toString().trim());
            assertTrue(valueMethod.isPublic());
            assertTrue(valueMethod.isAbstract());
        }
        {
            final MethodDefinition paramMethod = child.getMethodBySignature("param(List)", model);
            assertEquals("", paramMethod.getAccessModifier());
            assertEquals(1, paramMethod.getOtherModifiers().size());
            assertEquals("volatile ValueExpression<List<? extends T>> param(List<? extends T> value)", paramMethod.declaration());
            assertEquals("volatile ValueExpression<List<? extends T>> param(List<? extends T> value);", paramMethod.toString().trim());
            assertTrue(paramMethod.isPublic());
            assertTrue(paramMethod.isAbstract());
        }
    }

    public void testChild5() throws Exception {
        final SyntaxTree syntaxTree = readSyntaxTree();
        final Model model = ModelUtils.prepareModel();
        new InterfaceDeclarationsProcessor().process(Arrays.asList(syntaxTree), model);
        final InterfaceDefinition child = model.getInterface("Child5");
        assertEquals("Child5", child.getName());
        assertEquals(0, child.getDeclaredMethods().size());
        assertEquals(2, child.getAllMethods(model).size());
        {
            final MethodDefinition valueMethod;
            valueMethod = child.getMethodBySignature("value()", model);
            assertEquals("", valueMethod.getAccessModifier());
            assertEquals(1, valueMethod.getOtherModifiers().size());
            assertEquals("volatile List<? super T> value() throws SQLException", valueMethod.declaration());
            assertEquals("volatile List<? super T> value() throws SQLException;", valueMethod.toString().trim());
            assertTrue(valueMethod.isPublic());
            assertTrue(valueMethod.isAbstract());
        }
        {
            final MethodDefinition paramMethod = child.getMethodBySignature("param(List)", model);
            assertEquals("", paramMethod.getAccessModifier());
            assertEquals(1, paramMethod.getOtherModifiers().size());
            assertEquals("volatile ValueExpression<List<? super T>> param(List<? super T> value)", paramMethod.declaration());
            assertEquals("volatile ValueExpression<List<? super T>> param(List<? super T> value);", paramMethod.toString().trim());
            assertTrue(paramMethod.isPublic());
            assertTrue(paramMethod.isAbstract());
        }
    }

    public void testExplicitValueMethod() throws Exception {
        SymqleParser parser = new SymqleParser(new FileReader("src/test-data/ExplicitValueMethodDeclaration.sdl"));
        SyntaxTree node = new SyntaxTree(parser.SymqleUnit(), "ExplicitValueMethodDeclaration.sdl");
        final Model model = ModelUtils.prepareModel();
        try {
            new InterfaceDeclarationsProcessor().process(Arrays.asList(node), model);
            model.getInterface("expression").getAllMethods(model);
            fail("ModelException expected");
        } catch (ModelException e) {
            // expected
            assertTrue(e.getMessage(), e.getMessage().startsWith("Name clash"));
        }
    }

    public void testExplicitCreateMethod() throws Exception {
        SymqleParser parser = new SymqleParser(new FileReader("src/test-data/ExplicitCreateMethodDeclaration.sdl"));
        SyntaxTree node = new SyntaxTree(parser.SymqleUnit(), "ExplicitCreateMethodDeclaration.sdl");
        final Model model = ModelUtils.prepareModel();
        try {
            new InterfaceDeclarationsProcessor().process(Arrays.asList(node), model);
            model.getInterface("expression").getAllMethods(model);
            fail("ModelException expected");
        } catch (GrammarException e) {
            // expected
            assertTrue(e.getMessage(), e.getMessage().startsWith("Prefix \"z$sqlOf\" is reserved for generated methods"));
        }
    }

    public void testDuplicateInterface() throws Exception {
        SymqleParser parser = new SymqleParser(new FileReader("src/test-data/DuplicateInterface.sdl"));
        SyntaxTree node = new SyntaxTree(parser.SymqleUnit(), "DuplicateInterface.sdl");
        final Model model = ModelUtils.prepareModel();
        try {
            new InterfaceDeclarationsProcessor().process(Arrays.asList(node), model);
            fail("GrammarException expected");
        } catch (GrammarException e) {
            assertTrue(e.getMessage(), e.getMessage().startsWith("Duplicate class name: test_interface"));
        }
    }

    public void testDuplicateMethodInInterface() throws Exception {
        SymqleParser parser = new SymqleParser(new FileReader("src/test-data/InterfaceMethodOverloading.sdl"));
        SyntaxTree node = new SyntaxTree(parser.SymqleUnit(), "InterfaceMethodOverloading.sdl");
        final Model model = ModelUtils.prepareModel();
//        try {
//            new InterfaceDeclarationsProcessor().process(node, model);
//            new InterfaceValidator().process(node, model);
//            fail("ModelException expected");
//        } catch (GrammarException e) {
//            assertTrue(e.getMessage(), e.getMessage().startsWith("Name clash"));
//        }
    }

}
