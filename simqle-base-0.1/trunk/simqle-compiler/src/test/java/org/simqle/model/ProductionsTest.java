package org.simqle.model;

import junit.framework.TestCase;
import org.simqle.parser.ParseException;
import org.simqle.parser.SimqleParser;
import org.simqle.parser.SyntaxTree;
import org.simqle.processor.ClassDeclarationProcessor;
import org.simqle.processor.GrammarException;
import org.simqle.processor.InterfaceDeclarationsProcessor;
import org.simqle.processor.ProductionDeclarationProcessor;
import org.simqle.test.TestUtils;
import org.simqle.util.Utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * @author lvovich
 */
public class ProductionsTest extends TestCase {

    public void testBasicProduction() throws Exception {
        String source = "src/test-data/model/BasicProduction.sdl";
        final SyntaxTree syntaxTree = readSyntaxTree(source);
        final Model model = new Model();
        new InterfaceDeclarationsProcessor().process(syntaxTree, model);
        new ClassDeclarationProcessor().process(syntaxTree, model);
        new ProductionDeclarationProcessor().process(syntaxTree, model);

        final ClassDefinition simqle = model.getClassDef("Simqle");
        System.out.println(simqle);
        System.out.println("==========");
        final ClassDefinition simqleGeneric = model.getClassDef("SimqleGeneric");
        System.out.println(simqleGeneric);

        // make sure that classes are compilable
        Utils.createParser(simqle.toString()).SimqleDeclarationBlock();;
        Utils.createParser(simqleGeneric.toString()).SimqleDeclarationBlock();

        //
        assertEquals(3, simqle.getDeclaredMethods().size());
        assertEquals(3, simqle.getDeclaredMethods().size());
        for (MethodDefinition method: simqle.getDeclaredMethods()) {
            assertTrue(method.getOtherModifiers().toString(), method.getOtherModifiers().contains("abstract"));
        }
        {
            final MethodDefinition method = simqle.getDeclaredMethodBySignature("z$zSelectStatement$from$zCursorSpecification(zCursorSpecification)");
            assertEquals("abstract <T> zSelectStatement<T> z$zSelectStatement$from$zCursorSpecification(zCursorSpecification<T> cspec)",
                    method.declaration());
        }
        {
            final MethodDefinition method = simqle.getDeclaredMethodBySignature("forReadOnly(zCursorSpecification)");
            assertEquals("public abstract <T> SelectStatement<T> forReadOnly(zCursorSpecification<T> cspec)",
                    method.declaration());
        }
    }

    public void testProductionWithOverride() throws Exception {
        String source = "src/test-data/model/ProductionWithOverride.sdl";
        final SyntaxTree syntaxTree = readSyntaxTree(source);
        final Model model = new Model();
        new InterfaceDeclarationsProcessor().process(syntaxTree, model);
        new ClassDeclarationProcessor().process(syntaxTree, model);
        new ProductionDeclarationProcessor().process(syntaxTree, model);

        final ClassDefinition simqleGeneric = model.getClassDef("SimqleGeneric");
        System.out.println(simqleGeneric);

        Utils.createParser(simqleGeneric.toString()).SimqleDeclarationBlock();

        //
        assertEquals(3, simqleGeneric.getDeclaredMethods().size());
        assertEquals(3, simqleGeneric.getAllMethods(model).size());
        for (MethodDefinition method: simqleGeneric.getDeclaredMethods()) {
            assertFalse(method.getOtherModifiers().toString(), method.getOtherModifiers().contains("abstract"));
        }
        {
            final MethodDefinition method = simqleGeneric.getDeclaredMethodBySignature("z$zSelectStatement$from$zCursorSpecification(zCursorSpecification)");
            assertEquals("<T> zSelectStatement<T> z$zSelectStatement$from$zCursorSpecification(final zCursorSpecification<T> cspec)",
                    method.declaration());
            assertTrue(method.toString(), method.toString().contains("throw new RuntimeException(\"Not implemented\");"));
        }
        {
            final MethodDefinition method = simqleGeneric.getDeclaredMethodBySignature("forReadOnly(zCursorSpecification)");
            assertEquals("public <T> SelectStatement<T> forReadOnly(final zCursorSpecification<T> cspec)",
                    method.declaration());
        }
    }

    public void testProductionWithScalar() throws Exception {
        String source = "src/test-data/model/ProductionWithScalar.sdl";
        final SyntaxTree syntaxTree = readSyntaxTree(source);
        final Model model = new Model();
        new InterfaceDeclarationsProcessor().process(syntaxTree, model);
        new ClassDeclarationProcessor().process(syntaxTree, model);
        new ProductionDeclarationProcessor().process(syntaxTree, model);

        final ClassDefinition simqle = model.getClassDef("Simqle");
        final ClassDefinition simqleGeneric = model.getClassDef("SimqleGeneric");

        Utils.createParser(simqle.toString()).SimqleDeclarationBlock();
        System.out.println(simqleGeneric.toString());
        Utils.createParser(simqleGeneric.toString()).SimqleDeclarationBlock();

        {
            final MethodDefinition method = simqleGeneric.getDeclaredMethodBySignature("z$zValueExpression$from$zValueExpressionPrimary(zValueExpressionPrimary)");
            assertEquals(TestUtils.pureCode(
                    "    <T> zValueExpression<T>" +
                    "    z$zValueExpression$from$zValueExpressionPrimary(final zValueExpressionPrimary<T> e) { \n" +
                    "        return new zValueExpression<T>() {\n" +
                            "    public T value(final Element element) throws SQLException {\n" +
                            "        return e.value(element);\n" +
                            "    }\n" +
                    "           public Sql z$create$zValueExpression(final SqlContext context) {\n" +
                    "               return e.z$create$zValueExpressionPrimary(context);\n" +
                    "           }\n" +
                    "       };\n" +
                    "   }"),
                    TestUtils.pureCode(method.toString()));
        }
    }

    private SyntaxTree readSyntaxTree(String source) throws FileNotFoundException, ParseException {
        Reader reader = new InputStreamReader(new FileInputStream(source));
        SimqleParser parser = new SimqleParser(reader);
        final SyntaxTree syntaxTree = new SyntaxTree(parser.SimqleUnit(), source);
        return syntaxTree;
    }

    public void testInterfaceMisspelling() throws Exception {
        SyntaxTree syntaxTree = readSyntaxTree("src/test-data/model/UnknownInterfaceInProduction.sdl");
        Model model = new Model();
        new InterfaceDeclarationsProcessor().process(syntaxTree, model);
        new ClassDeclarationProcessor().process(syntaxTree, model);
        try {
            new ProductionDeclarationProcessor().process(syntaxTree, model);
            fail("GrammarException expected");
        } catch (GrammarException e) {
            assertTrue(e.getMessage(), e.getMessage().startsWith("Type not found:"));
        }

    }

    public void testParentheses() throws Exception {
        SyntaxTree syntaxTree = readSyntaxTree("src/test-data/model/Parentheses.sdl");
        Model model = new Model();
        new InterfaceDeclarationsProcessor().process(syntaxTree, model);
        new ProductionDeclarationProcessor().process(syntaxTree, model);
        ClassDefinition simqleGeneric = model.getClassDef("SimqleGeneric");
        MethodDefinition method = simqleGeneric.getDeclaredMethodBySignature("z$Subquery$from$SelectList(SelectList)");
        assertEquals(TestUtils.pureCode(
                "<T> Subquery<T> z$Subquery$from$SelectList(final SelectList<T> sl) { \n" +
                        "        return new Subquery<T>() {\n" +
                        "            /**\n" +
                        "            * Creates a Query representing <code>this</code>\n" +
                        "            * @param context the Sql construction context\n" +
                        "            * @return query conforming to <code>this</code> syntax\n" +
                        "            */\n" +
                        "            public Query<T> z$create$Subquery(final SqlContext context) {\n" +
                        "                final Query<T> __query = sl.z$create$SelectList(context);\n" +
                        "                return new ComplexQuery<T>(__query, LEFT_PAREN, __query, RIGHT_PAREN);\n" +
                        "            }/*delegation*/\n" +
                        "\n" +
                        "        };/*anonymous*/\n" +
                        "    }/*rule method*/\n" +
                        ""
        ), TestUtils.pureCode(method.toString()));
    }

}
