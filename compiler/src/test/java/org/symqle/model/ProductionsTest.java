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
import org.symqle.processor.GrammarException;
import org.symqle.processor.ProductionProcessor;
import org.symqle.test.TestUtils;
import org.symqle.util.ModelUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;

public class ProductionsTest extends TestCase {

    public void testBasicProduction() throws Exception {
        final Model model = ModelUtils.prepareModel();
        String source = "src/test-data/model/BasicProduction.sdl";
        final List<SyntaxTree> syntaxTrees = readSyntaxTree(source);
        new ProductionProcessor().process(syntaxTrees, model);
        final ClassDefinition symqle = model.getClassDef("Symqle");

        // make sure that classes are compilable
        SymqleParser.createParser(symqle.toString()).SymqleDeclarationBlock();

        //
        assertEquals(1, model.getConversions().size());
        assertEquals(2, model.getExplicitSymqleMethods().size());
        // TODO verify methods
    }

    public void testProductionWithOverride() throws Exception {
        final Model model = ModelUtils.prepareModel();
        String source = "src/test-data/model/ProductionWithOverride.sdl";
        final List<SyntaxTree> syntaxTrees = readSyntaxTree(source);
        new ProductionProcessor().process(syntaxTrees, model);

        final ClassDefinition symqle = model.getClassDef("Symqle");

        SymqleParser.createParser(symqle.toString()).SymqleDeclarationBlock();

        //
        assertEquals(1, model.getConversions().size());
        {
            final MethodDefinition method = model.getConversions().get(0).getConversionMethod();
            assertEquals("static <T> SelectStatementSqlBuilder<T> z$SelectStatement$from$CursorSpecification(final CursorSpecification<T> cspec)",
                    method.declaration());
            assertTrue(method.toString(), method.toString().contains("throw new RuntimeException(\"Not implemented\");"));
        }
        assertEquals(2, model.getExplicitSymqleMethods().size());
    }

    public void testProductionWithScalar() throws Exception {
        final Model model = ModelUtils.prepareModel();
        String source = "src/test-data/model/ProductionWithScalar.sdl";
        final List<SyntaxTree> syntaxTree = readSyntaxTree(source);
        new ProductionProcessor().process(syntaxTree, model);

        final List<ImplicitConversion> conversions = model.getConversions();
        assertEquals(1, conversions.size());
        {
            final MethodDefinition method = conversions.get(0).getConversionMethod();
            assertEquals(TestUtils.pureCode(
                    "    static <T> ValueExpressionSqlBuilder<T>" +
                    "    z$ValueExpression$from$ValueExpressionPrimary(final ValueExpressionPrimary<T> e) { \n" +
                    "        return new ValueExpressionSqlBuilder<T>() {\n" +
                    "           public SqlBuilder z$sqlOfValueExpression(final SqlContext context) {\n" +
                    "               return context.get(Dialect.class).ValueExpression_is_ValueExpressionPrimary(e.z$sqlOfValueExpressionPrimary(context));\n" +
                    "           }\n" +
                    "       };\n" +
                    "   }"),
                    TestUtils.pureCode(method.toString()));
        }
    }

    private List<SyntaxTree> readSyntaxTree(String source) throws FileNotFoundException, ParseException {
        Reader reader = new InputStreamReader(new FileInputStream(source));
        SymqleParser parser = new SymqleParser(reader);
        final SyntaxTree syntaxTree = new SyntaxTree(parser.SymqleUnit(), source);
        return Arrays.asList(syntaxTree);
    }

    public void testInterfaceMisspelling() throws Exception {
        final Model model = ModelUtils.prepareModel();
        List<SyntaxTree> syntaxTrees = readSyntaxTree("src/test-data/model/UnknownInterfaceInProduction.sdl");
        try {
            new ProductionProcessor().process(syntaxTrees, model);
            fail("GrammarException expected");
        } catch (GrammarException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("zCursorSpecification<T>"));
        }

    }

    public void testParentheses() throws Exception {
        final Model model = ModelUtils.prepareModel();
        List<SyntaxTree> syntaxTrees = readSyntaxTree("src/test-data/model/Parentheses.sdl");
        new ProductionProcessor().process(syntaxTrees, model);
        final List<ImplicitConversion> conversions = model.getConversions();
        assertEquals(1, conversions.size());
        MethodDefinition method = conversions.get(0).getConversionMethod();
        assertEquals(TestUtils.pureCode(
                "static <T> SubquerySqlBuilder<T> z$Subquery$from$SelectList(final SelectList<T> sl) { \n" +
                        "        return new SubquerySqlBuilder<T>() {\n" +
                        "            /**\n" +
                        "            * Creates a Query representing <code>this</code>\n" +
                        "            * @param context the Sql construction context\n" +
                        "            * @return query conforming to <code>this</code> syntax\n" +
                        "            */\n" +
                        "            public QueryBuilder<T> z$sqlOfSubquery(final SqlContext context) {\n" +
                        "                final QueryBuilder<T> rowMapper = sl.z$sqlOfSelectList(context); " +
                        "            return new ComplexQueryBuilder<T>(rowMapper, " +
                        "                context.get(Dialect.class).Subquery_is_LEFT_PAREN_SelectList_RIGHT_PAREN(rowMapper));" +
                        "            }/*delegation*/\n" +
                        "\n" +
                        "        };/*anonymous*/\n" +
                        "    }/*rule method*/\n" +
                        ""
        ), TestUtils.pureCode(method.toString()));
    }

    public void testProductionWithPropertyGetter() throws Exception {
        final Model model = ModelUtils.prepareModel();
        String source = "src/test-data/model/ProductionWithProperties.sdl";
        final List<SyntaxTree> syntaxTree = readSyntaxTree(source);
        new ProductionProcessor().process(syntaxTree, model);

        final List<ImplicitConversion> conversions = model.getConversions();
        assertEquals(1, conversions.size());

        final String expected = "static <T> ValueExpressionSqlBuilder<T> z$ValueExpression$from$ValueExpressionPrimary(final ValueExpressionPrimary<T> e)\n" +
                " { \n" +
                "        return new ValueExpressionSqlBuilder<T>() {\n" +
                "            public SqlBuilder z$sqlOfValueExpression(final SqlContext context) {\n" +
                "                return context.get(Dialect.class).ValueExpression_is_ValueExpressionPrimary(e.z$sqlOfValueExpressionPrimary(context));\n" +
                "            }\n" +
                "\n" +
                "        };\n" +
                "    }\n";
        assertEquals(TestUtils.pureCode(expected), TestUtils.pureCode(conversions.get(0).getConversionMethod().toString()));

    }

}
