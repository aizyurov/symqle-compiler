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
 * @author lvovich
 */
public class TestArchetypes extends TestCase {

    private SyntaxTree readSyntaxTree() throws FileNotFoundException, ParseException {
        String source = "src/test-data/model/Archetypes.sdl";
        Reader reader = new InputStreamReader(new FileInputStream(source));
        SimqleParser parser = new SimqleParser(reader);
        return new SyntaxTree(parser.SimqleUnit(), source);
    }

    public void testChild1() throws Exception {
        final SyntaxTree syntaxTree = readSyntaxTree();
        Model model = new Model();
        new InterfaceDeclarationsProcessor().process(syntaxTree, model);
        final InterfaceDefinition child = model.getInterface("Child1");
        assertEquals("Child1", child.getName());
        assertEquals(2, child.getDeclaredMethods().size());
        assertEquals(4, child.getAllMethods(model).size());
        {
            final MethodDefinition myMethod = child.getDeclaredMethodBySignature("myMethod(int)");
            assertEquals("", myMethod.getAccessModifier());
            assertEquals(0, myMethod.getOtherModifiers().size());
            assertEquals("Collection<T> myMethod(int i) throws NoSuchElementException, ArrayIndexOutOfBoundsException", myMethod.declaration());
            assertEquals("Collection<T> myMethod(int i) throws NoSuchElementException, ArrayIndexOutOfBoundsException;", myMethod.toString().trim());
            assertTrue(myMethod.isPublic());
            assertTrue(myMethod.isAbstract());
        }
        {
            final MethodDefinition myMethod = child.getDeclaredMethodBySignature("z$create$Child1(SqlContext)");
            assertEquals("", myMethod.getAccessModifier());
            assertEquals(0, myMethod.getOtherModifiers().size());
            assertEquals("Sql z$create$Child1(SqlContext context)", myMethod.declaration());
            assertTrue(myMethod.isPublic());
            assertTrue(myMethod.isAbstract());
        }
        {
            final MethodDefinition valueMethod = child.getMethodBySignature("value()", model);
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
        assertEquals(1, child.getDeclaredMethods().size());
        assertEquals(1, child.getAllMethods(model).size());
        {
            final MethodDefinition myMethod = child.getDeclaredMethodBySignature("z$create$Child2(SqlContext)");
            assertEquals("", myMethod.getAccessModifier());
            assertEquals(0, myMethod.getOtherModifiers().size());
            assertEquals("Query<T> z$create$Child2(SqlContext context)", myMethod.declaration());
            assertTrue(myMethod.isPublic());
            assertTrue(myMethod.isAbstract());
        }
    }

    public void testChild3() throws Exception {
        final SyntaxTree syntaxTree = readSyntaxTree();
        Model model = new Model();
        new InterfaceDeclarationsProcessor().process(syntaxTree, model);
        final InterfaceDefinition child = model.getInterface("Child3");
        assertEquals("Child3", child.getName());
        assertEquals(1, child.getDeclaredMethods().size());
        assertEquals(1, child.getAllMethods(model).size());
        {
            final MethodDefinition myMethod = child.getDeclaredMethodBySignature("z$create$Child3(SqlContext)");
            assertEquals("", myMethod.getAccessModifier());
            assertEquals(0, myMethod.getOtherModifiers().size());
            assertEquals("Query<Boolean> z$create$Child3(SqlContext context)", myMethod.declaration());
            assertTrue(myMethod.isPublic());
            assertTrue(myMethod.isAbstract());
        }
    }

    public void testChild4() throws Exception {
        final SyntaxTree syntaxTree = readSyntaxTree();
        Model model = new Model();
        new InterfaceDeclarationsProcessor().process(syntaxTree, model);
        final InterfaceDefinition child = model.getInterface("Child4");
        assertEquals("Child4", child.getName());
        assertEquals(1, child.getDeclaredMethods().size());
        assertEquals(3, child.getAllMethods(model).size());
        {
            final MethodDefinition myMethod = child.getDeclaredMethodBySignature("z$create$Child4(SqlContext)");
            assertEquals("", myMethod.getAccessModifier());
            assertEquals(0, myMethod.getOtherModifiers().size());
            assertEquals("Query<Boolean> z$create$Child4(SqlContext context)", myMethod.declaration());
            assertTrue(myMethod.isPublic());
            assertTrue(myMethod.isAbstract());
        }
        {
            final MethodDefinition valueMethod = child.getMethodBySignature("value()", model);
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

}
