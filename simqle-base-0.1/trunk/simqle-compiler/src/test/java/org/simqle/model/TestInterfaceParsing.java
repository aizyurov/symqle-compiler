package org.simqle.model;

import junit.framework.TestCase;
import org.simqle.parser.ParseException;
import org.simqle.parser.SimqleParser;
import org.simqle.parser.SyntaxTree;
import org.simqle.processor.InterfaceDeclarationsProcessor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;

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
        SimqleParser parser = new SimqleParser(reader);
        return new SyntaxTree(parser.SimqleUnit(), source);
    }

    public void testScalar() throws Exception {
        final SyntaxTree syntaxTree = readSyntaxTree();
        Model model = new Model();
        new InterfaceDeclarationsProcessor().process(syntaxTree, model);
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
        Model model = new Model();
        new InterfaceDeclarationsProcessor().process(syntaxTree, model);
        final InterfaceDefinition child = model.getInterface("Child1");
        assertEquals("Child1", child.getName());
        assertEquals(1, child.getDeclaredMethods().size());
        assertEquals(3, child.getAllMethods(model).size());
        {
            final MethodDefinition myMethod;
            myMethod = child.getDeclaredMethodBySignature("myMethod(int)");
            assertEquals("", myMethod.getAccessModifier());
            assertEquals(0, myMethod.getOtherModifiers().size());
            assertEquals("Collection<T> myMethod(int i) throws NoSuchElementException, ArrayIndexOutOfBoundsException", myMethod.declaration());
            assertEquals("Collection<T> myMethod(int i) throws NoSuchElementException, ArrayIndexOutOfBoundsException;", myMethod.toString().trim());
            assertTrue(myMethod.isPublic());
            assertTrue(myMethod.isAbstract());
        }
        {
            final MethodDefinition valueMethod;
            valueMethod = child.getMethodBySignature("value()", model);
            assertEquals("", valueMethod.getAccessModifier());
            assertEquals(1, valueMethod.getOtherModifiers().size());
            assertEquals("transient T value() throws SQLException", valueMethod.declaration());
            assertEquals("transient T value() throws SQLException;", valueMethod.toString().trim());
            assertTrue(valueMethod.isPublic());
            assertTrue(valueMethod.isAbstract());
        }
        {
            final MethodDefinition paramMethod = child.getMethodBySignature("param(Object)", model);
            assertEquals("", paramMethod.getAccessModifier());
            assertEquals(1, paramMethod.getOtherModifiers().size());
            assertEquals("transient ValueExpression<T> param(T value)", paramMethod.declaration());
            assertEquals("transient ValueExpression<T> param(T value);", paramMethod.toString().trim());
            assertTrue(paramMethod.isPublic());
            assertTrue(paramMethod.isAbstract());
        }
    }

    public void testChild2() throws Exception {
        final SyntaxTree syntaxTree = readSyntaxTree();
        Model model = new Model();
        new InterfaceDeclarationsProcessor().process(syntaxTree, model);
        final InterfaceDefinition child = model.getInterface("Child2");
        assertEquals("Child2", child.getName());
        assertEquals(0, child.getDeclaredMethods().size());
        assertEquals(2, child.getAllMethods(model).size());
        {
            final MethodDefinition valueMethod;
            valueMethod = child.getMethodBySignature("value()", model);
            assertEquals("", valueMethod.getAccessModifier());
            assertEquals(1, valueMethod.getOtherModifiers().size());
            assertEquals("transient Boolean value() throws SQLException", valueMethod.declaration());
            assertEquals("transient Boolean value() throws SQLException;", valueMethod.toString().trim());
            assertTrue(valueMethod.isPublic());
            assertTrue(valueMethod.isAbstract());
        }
        {
            final MethodDefinition paramMethod = child.getMethodBySignature("param(Boolean)", model);
            assertEquals("", paramMethod.getAccessModifier());
            assertEquals(1, paramMethod.getOtherModifiers().size());
            assertEquals("transient ValueExpression<Boolean> param(Boolean value)", paramMethod.declaration());
            assertEquals("transient ValueExpression<Boolean> param(Boolean value);", paramMethod.toString().trim());
            assertTrue(paramMethod.isPublic());
            assertTrue(paramMethod.isAbstract());
        }
    }

    public void testChild3() throws Exception {
        final SyntaxTree syntaxTree = readSyntaxTree();
        Model model = new Model();
        new InterfaceDeclarationsProcessor().process(syntaxTree, model);
        final InterfaceDefinition child = model.getInterface("Child3");
        assertEquals("Child3", child.getName());
        assertEquals(0, child.getDeclaredMethods().size());
        assertEquals(2, child.getAllMethods(model).size());
        {
            final MethodDefinition valueMethod;
            valueMethod = child.getMethodBySignature("value()", model);
            assertEquals("", valueMethod.getAccessModifier());
            assertEquals(1, valueMethod.getOtherModifiers().size());
            assertEquals("transient List<T> value() throws SQLException", valueMethod.declaration());
            assertEquals("transient List<T> value() throws SQLException;", valueMethod.toString().trim());
            assertTrue(valueMethod.isPublic());
            assertTrue(valueMethod.isAbstract());
        }
        {
            final MethodDefinition paramMethod = child.getMethodBySignature("param(List)", model);
            assertEquals("", paramMethod.getAccessModifier());
            assertEquals(1, paramMethod.getOtherModifiers().size());
            assertEquals("transient ValueExpression<List<T>> param(List<T> value)", paramMethod.declaration());
            assertEquals("transient ValueExpression<List<T>> param(List<T> value);", paramMethod.toString().trim());
            assertTrue(paramMethod.isPublic());
            assertTrue(paramMethod.isAbstract());
        }
    }

    public void testChild4() throws Exception {
        final SyntaxTree syntaxTree = readSyntaxTree();
        Model model = new Model();
        new InterfaceDeclarationsProcessor().process(syntaxTree, model);
        final InterfaceDefinition child = model.getInterface("Child4");
        assertEquals("Child4", child.getName());
        assertEquals(0, child.getDeclaredMethods().size());
        assertEquals(2, child.getAllMethods(model).size());
        {
            final MethodDefinition valueMethod;
            valueMethod = child.getMethodBySignature("value()", model);
            assertEquals("", valueMethod.getAccessModifier());
            assertEquals(1, valueMethod.getOtherModifiers().size());
            assertEquals("transient List<? extends T> value() throws SQLException", valueMethod.declaration());
            assertEquals("transient List<? extends T> value() throws SQLException;", valueMethod.toString().trim());
            assertTrue(valueMethod.isPublic());
            assertTrue(valueMethod.isAbstract());
        }
        {
            final MethodDefinition paramMethod = child.getMethodBySignature("param(List)", model);
            assertEquals("", paramMethod.getAccessModifier());
            assertEquals(1, paramMethod.getOtherModifiers().size());
            assertEquals("transient ValueExpression<List<? extends T>> param(List<? extends T> value)", paramMethod.declaration());
            assertEquals("transient ValueExpression<List<? extends T>> param(List<? extends T> value);", paramMethod.toString().trim());
            assertTrue(paramMethod.isPublic());
            assertTrue(paramMethod.isAbstract());
        }
    }

    public void testChild5() throws Exception {
        final SyntaxTree syntaxTree = readSyntaxTree();
        Model model = new Model();
        new InterfaceDeclarationsProcessor().process(syntaxTree, model);
        final InterfaceDefinition child = model.getInterface("Child5");
        assertEquals("Child5", child.getName());
        assertEquals(0, child.getDeclaredMethods().size());
        assertEquals(2, child.getAllMethods(model).size());
        {
            final MethodDefinition valueMethod;
            valueMethod = child.getMethodBySignature("value()", model);
            assertEquals("", valueMethod.getAccessModifier());
            assertEquals(1, valueMethod.getOtherModifiers().size());
            assertEquals("transient List<? super T> value() throws SQLException", valueMethod.declaration());
            assertEquals("transient List<? super T> value() throws SQLException;", valueMethod.toString().trim());
            assertTrue(valueMethod.isPublic());
            assertTrue(valueMethod.isAbstract());
        }
        {
            final MethodDefinition paramMethod = child.getMethodBySignature("param(List)", model);
            assertEquals("", paramMethod.getAccessModifier());
            assertEquals(1, paramMethod.getOtherModifiers().size());
            assertEquals("transient ValueExpression<List<? super T>> param(List<? super T> value)", paramMethod.declaration());
            assertEquals("transient ValueExpression<List<? super T>> param(List<? super T> value);", paramMethod.toString().trim());
            assertTrue(paramMethod.isPublic());
            assertTrue(paramMethod.isAbstract());
        }
    }

}
