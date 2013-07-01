package org.symqle.model;

import junit.framework.TestCase;
import org.symqle.parser.ParseException;
import org.symqle.parser.SimqleParser;
import org.symqle.parser.SyntaxTree;
import org.symqle.processor.InterfaceDeclarationsProcessor;
import org.symqle.util.ModelUtils;

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
        final Model model = ModelUtils.prepareModel();
        new InterfaceDeclarationsProcessor().process(syntaxTree, model);
        final InterfaceDefinition child = model.getInterface("Child1");
        assertEquals("Child1", child.getName());
        assertEquals(2, child.getDeclaredMethods().size());
        assertEquals(4, child.getAllMethods(model).size());
        {
            final MethodDefinition myMethod = child.getDeclaredMethodBySignature("myMethod(int)");
            assertEquals("", myMethod.getAccessModifier());
            assertEquals(0, myMethod.getOtherModifiers().size());
            assertEquals("Collection<T> myMethod(int i) throws ArrayIndexOutOfBoundsException, NoSuchElementException", myMethod.declaration());
            assertEquals("Collection<T> myMethod(int i) throws ArrayIndexOutOfBoundsException, NoSuchElementException;", myMethod.toString().trim());
            assertTrue(myMethod.isPublic());
            assertTrue(myMethod.isAbstract());
        }
        {
            final MethodDefinition myMethod = child.getDeclaredMethodBySignature("z$sqlOfChild1(SqlContext)");
            assertEquals("", myMethod.getAccessModifier());
            assertEquals(0, myMethod.getOtherModifiers().size());
            assertEquals("Sql z$sqlOfChild1(SqlContext context)", myMethod.declaration());
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
        final Model model = ModelUtils.prepareModel();
        new InterfaceDeclarationsProcessor().process(syntaxTree, model);
        final InterfaceDefinition child = model.getInterface("Child2");
        assertEquals("Child2", child.getName());
        assertEquals(1, child.getDeclaredMethods().size());
        assertEquals(1, child.getAllMethods(model).size());
        {
            final MethodDefinition myMethod = child.getDeclaredMethodBySignature("z$sqlOfChild2(SqlContext)");
            assertEquals("", myMethod.getAccessModifier());
            assertEquals(0, myMethod.getOtherModifiers().size());
            assertEquals("Query<T> z$sqlOfChild2(SqlContext context)", myMethod.declaration());
            assertTrue(myMethod.isPublic());
            assertTrue(myMethod.isAbstract());
        }
    }

    public void testChild3() throws Exception {
        final SyntaxTree syntaxTree = readSyntaxTree();
        final Model model = ModelUtils.prepareModel();
        new InterfaceDeclarationsProcessor().process(syntaxTree, model);
        final InterfaceDefinition child = model.getInterface("Child3");
        assertEquals("Child3", child.getName());
        assertEquals(1, child.getDeclaredMethods().size());
        assertEquals(1, child.getAllMethods(model).size());
        {
            final MethodDefinition myMethod = child.getDeclaredMethodBySignature("z$sqlOfChild3(SqlContext)");
            assertEquals("", myMethod.getAccessModifier());
            assertEquals(0, myMethod.getOtherModifiers().size());
            assertEquals("Query<Boolean> z$sqlOfChild3(SqlContext context)", myMethod.declaration());
            assertTrue(myMethod.isPublic());
            assertTrue(myMethod.isAbstract());
        }
    }

    public void testChild4() throws Exception {
        final SyntaxTree syntaxTree = readSyntaxTree();
        final Model model = ModelUtils.prepareModel();
        new InterfaceDeclarationsProcessor().process(syntaxTree, model);
        final InterfaceDefinition child = model.getInterface("Child4");
        System.out.println(child);
        for (MethodDefinition method: child.getAllMethods(model)) {
            System.out.println("------");
            System.out.println(method);
        }
        assertEquals("Child4", child.getName());
        assertEquals(1, child.getDeclaredMethods().size());
        assertEquals(3, child.getAllMethods(model).size());
        {
            final MethodDefinition myMethod = child.getDeclaredMethodBySignature("z$sqlOfChild4(SqlContext)");
            assertEquals("", myMethod.getAccessModifier());
            assertEquals(0, myMethod.getOtherModifiers().size());
            assertEquals("Query<Boolean> z$sqlOfChild4(SqlContext context)", myMethod.declaration());
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
