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
import org.symqle.parser.SymqleParser;
import org.symqle.parser.SyntaxTree;
import org.symqle.processor.FinalizationProcessor;
import org.symqle.processor.InheritanceProcessor;
import org.symqle.test.TestUtils;
import org.symqle.util.ModelUtils;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class InheritanceTest extends TestCase {

    public void testSimple() throws Exception {
        final Model model = ModelUtils.prepareModel();
        String source = "src/test-data/model/SimpleInheritance.sdl";
        Reader reader = new InputStreamReader(new FileInputStream(source));
        SymqleParser parser = new SymqleParser(reader);
        final List<SyntaxTree> syntaxTrees = Arrays.asList(new SyntaxTree(parser.SymqleUnit(), source));
        new InheritanceProcessor().process(syntaxTrees, model);
        final ClassDefinition cursorSpec = model.getClassDef("AbstractCursorSpecification");
//        System.out.println(cursorSpec);
        MethodDefinition delegatedMethod = cursorSpec.getDeclaredMethodBySignature("z$sqlOfSelectStatement(SqlContext)");
        assertEquals(TestUtils.pureCode
                ("public final QueryBuilder<T> z$sqlOfSelectStatement(final SqlContext context) {\n" +
                "                return Symqle.z$SelectStatement$from$CursorSpecification(this)\n" +
                        ".z$sqlOfSelectStatement(context);\n" +
                "            }"), TestUtils.pureCode(delegatedMethod.toString()));

    }

    public void testChain() throws Exception {
        final Model model = ModelUtils.prepareModel();
        String source = "src/test-data/model/ChainInheritance.sdl";
        Reader reader = new InputStreamReader(new FileInputStream(source));
        SymqleParser parser = new SymqleParser(reader);
        final List<SyntaxTree> syntaxTrees = Arrays.asList(new SyntaxTree(parser.SymqleUnit(), source));
        new InheritanceProcessor().process(syntaxTrees, model);
        ClassDefinition queryExpr = model.getClassDef("AbstractQueryExpression");
        final MethodDefinition asCursorSpec = queryExpr.getDeclaredMethodBySignature("z$sqlOfCursorSpecification(SqlContext)");
        assertEquals(TestUtils.pureCode(
                "public final QueryBuilder<T> z$sqlOfCursorSpecification(final SqlContext context) {\n" +
                        "                return Symqle.z$CursorSpecification$from$QueryExpression(this)\n" +
                        ".z$sqlOfCursorSpecification(context);\n" +
                        "            }"
        ), TestUtils.pureCode(asCursorSpec.toString()));
        final MethodDefinition asSelectStatement = queryExpr.getDeclaredMethodBySignature("z$sqlOfSelectStatement(SqlContext)");
        assertEquals(TestUtils.pureCode(
                "public final QueryBuilder<T> z$sqlOfSelectStatement(final SqlContext context) {\n" +
                        "        return Symqle.z$SelectStatement$from$CursorSpecification(this)\n" +
                        "            .z$sqlOfSelectStatement(context);\n" +
                        "    }"),
                TestUtils.pureCode(asSelectStatement.toString()));
    }

    public void testCyclic() throws Exception {
        final Model model = ModelUtils.prepareModel();
        String source = "src/test-data/model/CyclicInheritance.sdl";
        Reader reader = new InputStreamReader(new FileInputStream(source));
        SymqleParser parser = new SymqleParser(reader);
        final List<SyntaxTree> syntaxTrees = Arrays.asList(new SyntaxTree(parser.SymqleUnit(), source));
        new InheritanceProcessor().process(syntaxTrees, model);
        {
            final ClassDefinition abstractClass = model.getClassDef("Value");
            final Set<Type> valueClassAncestors = abstractClass.getAllAncestors(model);
            final TypeArgument typeArgument = new TypeArgument(false, null, new Type("T"));
            final Type valueExpressionPrimary = new Type("ValueExpressionPrimary", new TypeArguments(Collections.singletonList(typeArgument)), 0);
            final Type valueExpression = new Type("ValueExpression", new TypeArguments(Collections.singletonList(typeArgument)), 0);
            assertTrue(abstractClass.toString(), valueClassAncestors.contains(valueExpressionPrimary));
            assertTrue(abstractClass.toString(), valueClassAncestors.contains(valueExpression));
        }

        {
            final ClassDefinition abstractClass = model.getClassDef("AbstractValueExpressionPrimary");
            final Set<Type> valueClassAncestors = abstractClass.getAllAncestors(model);
            final TypeArgument typeArgument = new TypeArgument(false, null, new Type("T"));
            final Type valueExpressionPrimary = new Type("ValueExpressionPrimary", new TypeArguments(Collections.singletonList(typeArgument)), 0);
            final Type valueExpression = new Type("ValueExpression", new TypeArguments(Collections.singletonList(typeArgument)), 0);
            assertTrue(abstractClass.toString(), valueClassAncestors.contains(valueExpressionPrimary));
            assertTrue(abstractClass.toString(), valueClassAncestors.contains(valueExpression));
        }
    }

    public void testGenerics() throws Exception {
        final Model model = ModelUtils.prepareModel();
        String source = "src/test-data/model/GenericsInImplicits.sdl";
        Reader reader = new InputStreamReader(new FileInputStream(source));
        SymqleParser parser = new SymqleParser(reader);
        final List<SyntaxTree> syntaxTrees = Arrays.asList(new SyntaxTree(parser.SymqleUnit(), source));
        new FinalizationProcessor().process(syntaxTrees, model);
        final ClassDefinition abstractValueExpressionPrimary = model.getClassDef("AbstractValueExpressionPrimary");
        final MethodDefinition plus = abstractValueExpressionPrimary.getMethodBySignature("plus(ValueExpressionPrimary)", model);
        assertNotNull(plus);
        assertEquals("public final Value<Number> plus(final ValueExpressionPrimary<V> r)", plus.declaration());
    }

    public void testAllAncestorsForClass() throws Exception {
        final Model model = ModelUtils.prepareModel();
        String source = "src/test-data/model/ClassHierarchy.sdl";
        Reader reader = new InputStreamReader(new FileInputStream(source));
        SymqleParser parser = new SymqleParser(reader);
        final List<SyntaxTree> syntaxTrees = Arrays.asList(new SyntaxTree(parser.SymqleUnit(), source));
        new FinalizationProcessor().process(syntaxTrees, model);
        final ClassDefinition hashMap = model.getClassDef("HashMap");
        final TypeArgument k = new TypeArgument(false, null, new Type("K"));
        final TypeArgument v = new TypeArgument(false, null, new Type("V"));
        final Set<Type> allAncestors = hashMap.getAllAncestors(model);
        System.out.println(hashMap.getName() + hashMap.getTypeParameters() + " <- " + hashMap.getAllAncestors(model));
        assertTrue(hashMap.toString(), allAncestors.contains(new Type("Map", new TypeArguments(Arrays.asList(k,v)), 0)));
        assertTrue(hashMap.toString(), allAncestors.contains(new Type("AbstractMap", new TypeArguments(Arrays.asList(k,v)), 0)));

    }
}