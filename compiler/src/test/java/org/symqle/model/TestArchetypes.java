/*
   Copyright 2011-2014 Alexander Izyurov

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.package org.symqle.common;
*/

package org.symqle.model;

import junit.framework.TestCase;
import org.symqle.parser.ParseException;
import org.symqle.parser.SymqleParser;
import org.symqle.parser.SyntaxTree;
import org.symqle.processor.InterfaceDeclarationsProcessor;
import org.symqle.util.ModelUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;

public class TestArchetypes extends TestCase {

    private SyntaxTree readSyntaxTree() throws FileNotFoundException, ParseException {
        String source = "src/test-data/model/Archetypes.sdl";
        Reader reader = new InputStreamReader(new FileInputStream(source));
        SymqleParser parser = new SymqleParser(reader);
        return new SyntaxTree(parser.SymqleUnit(), source);
    }

    public void testChild1() throws Exception {
        final SyntaxTree syntaxTree = readSyntaxTree();
        final Model model = ModelUtils.prepareModel();
        new InterfaceDeclarationsProcessor().process(Arrays.asList(syntaxTree), model);
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
            assertEquals("SqlBuilder z$sqlOfChild1(SqlContext context)", myMethod.declaration());
            assertTrue(myMethod.isPublic());
            assertTrue(myMethod.isAbstract());
        }
        {
            final MethodDefinition valueMethod = child.getMethodBySignature("value()", model);
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
        assertEquals(1, child.getDeclaredMethods().size());
        assertEquals(1, child.getAllMethods(model).size());
        {
            final MethodDefinition myMethod = child.getDeclaredMethodBySignature("z$sqlOfChild2(SqlContext)");
            assertEquals("", myMethod.getAccessModifier());
            assertEquals(0, myMethod.getOtherModifiers().size());
            assertEquals("QueryBuilder<T> z$sqlOfChild2(SqlContext context)", myMethod.declaration());
            assertTrue(myMethod.isPublic());
            assertTrue(myMethod.isAbstract());
        }
    }

    public void testChild3() throws Exception {
        final SyntaxTree syntaxTree = readSyntaxTree();
        final Model model = ModelUtils.prepareModel();
        new InterfaceDeclarationsProcessor().process(Arrays.asList(syntaxTree), model);
        final InterfaceDefinition child = model.getInterface("Child3");
        assertEquals("Child3", child.getName());
        assertEquals(1, child.getDeclaredMethods().size());
        assertEquals(1, child.getAllMethods(model).size());
        {
            final MethodDefinition myMethod = child.getDeclaredMethodBySignature("z$sqlOfChild3(SqlContext)");
            assertEquals("", myMethod.getAccessModifier());
            assertEquals(0, myMethod.getOtherModifiers().size());
            assertEquals("QueryBuilder<Boolean> z$sqlOfChild3(SqlContext context)", myMethod.declaration());
            assertTrue(myMethod.isPublic());
            assertTrue(myMethod.isAbstract());
        }
    }

    public void testChild4() throws Exception {
        final SyntaxTree syntaxTree = readSyntaxTree();
        final Model model = ModelUtils.prepareModel();
        new InterfaceDeclarationsProcessor().process(Arrays.asList(syntaxTree), model);
        final InterfaceDefinition child = model.getInterface("Child4");
        assertEquals("Child4", child.getName());
        assertEquals(1, child.getDeclaredMethods().size());
        assertEquals(3, child.getAllMethods(model).size());
        {
            final MethodDefinition myMethod = child.getDeclaredMethodBySignature("z$sqlOfChild4(SqlContext)");
            assertEquals("", myMethod.getAccessModifier());
            assertEquals(0, myMethod.getOtherModifiers().size());
            assertEquals("QueryBuilder<Boolean> z$sqlOfChild4(SqlContext context)", myMethod.declaration());
            assertTrue(myMethod.isPublic());
            assertTrue(myMethod.isAbstract());
        }
        {
            final MethodDefinition valueMethod = child.getMethodBySignature("value()", model);
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

}
