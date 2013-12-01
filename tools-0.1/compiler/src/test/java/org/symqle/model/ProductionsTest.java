package org.symqle.model;

import junit.framework.TestCase;
import org.symqle.parser.ParseException;
import org.symqle.parser.SymqleParser;
import org.symqle.parser.SyntaxTree;
import org.symqle.processor.ClassDeclarationProcessor;
import org.symqle.processor.GrammarException;
import org.symqle.processor.InterfaceDeclarationsProcessor;
import org.symqle.processor.ProductionDeclarationProcessor;
import org.symqle.test.TestUtils;
import org.symqle.util.ModelUtils;
import org.symqle.util.Utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * @author lvovich
 */
public class ProductionsTest extends TestCase {

    public void testBasicProduction() throws Exception {
        final Model model = ModelUtils.prepareModel();
        String source = "src/test-data/model/BasicProduction.sdl";
        final SyntaxTree syntaxTree = readSyntaxTree(source);
        new InterfaceDeclarationsProcessor().process(syntaxTree, model);
        new ClassDeclarationProcessor().process(syntaxTree, model);
        new ProductionDeclarationProcessor().process(syntaxTree, model);

        final ClassDefinition symqle = model.getClassDef("Symqle");
        System.out.println(symqle);
        System.out.println("==========");

        // make sure that classes are compilable
        Utils.createParser(symqle.toString()).SymqleDeclarationBlock();;

        //
        assertEquals(3, symqle.getStaticMethods().size());
        for (MethodDefinition method: symqle.getStaticMethods()) {
            assertFalse(method.getOtherModifiers().toString(), method.getOtherModifiers().contains("abstract"));
        }
        {
            final MethodDefinition method = symqle.getDeclaredMethodBySignature("z$zSelectStatement$from$zCursorSpecification(zCursorSpecification)");
            assertEquals("static <T> zSelectStatement<T> z$zSelectStatement$from$zCursorSpecification(final zCursorSpecification<T> cspec)",
                    method.declaration());
        }
        {
            final MethodDefinition method = symqle.getDeclaredMethodBySignature("forReadOnly(zCursorSpecification)");
            assertEquals("public static <T> SelectStatement<T> forReadOnly(final zCursorSpecification<T> cspec)",
                    method.declaration());
        }
    }

    public void testProductionWithOverride() throws Exception {
        final Model model = ModelUtils.prepareModel();
        String source = "src/test-data/model/ProductionWithOverride.sdl";
        final SyntaxTree syntaxTree = readSyntaxTree(source);
        new InterfaceDeclarationsProcessor().process(syntaxTree, model);
        new ClassDeclarationProcessor().process(syntaxTree, model);
        new ProductionDeclarationProcessor().process(syntaxTree, model);

        final ClassDefinition symqle = model.getClassDef("Symqle");
        System.out.println(symqle);

        Utils.createParser(symqle.toString()).SymqleDeclarationBlock();

        //
        assertEquals(3, symqle.getStaticMethods().size());
        for (MethodDefinition method: symqle.getStaticMethods()) {
            assertFalse(method.getOtherModifiers().toString(), method.getOtherModifiers().contains("abstract"));
        }
        {
            final MethodDefinition method = symqle.getDeclaredMethodBySignature("z$zSelectStatement$from$zCursorSpecification(zCursorSpecification)");
            assertEquals("static <T> zSelectStatement<T> z$zSelectStatement$from$zCursorSpecification(final zCursorSpecification<T> cspec)",
                    method.declaration());
            assertTrue(method.toString(), method.toString().contains("throw new RuntimeException(\"Not implemented\");"));
        }
        {
            final MethodDefinition method = symqle.getDeclaredMethodBySignature("forReadOnly(zCursorSpecification)");
            assertEquals("public static <T> SelectStatement<T> forReadOnly(final zCursorSpecification<T> cspec)",
                    method.declaration());
        }
    }

    public void testProductionWithScalar() throws Exception {
        final Model model = ModelUtils.prepareModel();
        String source = "src/test-data/model/ProductionWithScalar.sdl";
        final SyntaxTree syntaxTree = readSyntaxTree(source);
        new InterfaceDeclarationsProcessor().process(syntaxTree, model);
        new ClassDeclarationProcessor().process(syntaxTree, model);
        new ProductionDeclarationProcessor().process(syntaxTree, model);

        final ClassDefinition symqle = model.getClassDef("Symqle");

        System.out.println(symqle.toString());
        Utils.createParser(symqle.toString()).SymqleDeclarationBlock();

        {
            final MethodDefinition method = symqle.getDeclaredMethodBySignature("z$zValueExpression$from$zValueExpressionPrimary(zValueExpressionPrimary)");
            System.out.println(method);
            assertEquals(TestUtils.pureCode(
                    "    static <T> zValueExpression<T>" +
                    "    z$zValueExpression$from$zValueExpressionPrimary(final zValueExpressionPrimary<T> e) { \n" +
                    "        return new Value<T>() {\n" +
                            "    public T value(final Element element) throws SQLException {\n" +
                            "        return e.value(element);\n" +
                            "    }\n" +
                    "           public Sql z$sqlOfzValueExpression(final SqlContext context) {\n" +
                    "               return context.get(Dialect.class).zValueExpression_is_zValueExpressionPrimary(e.z$sqlOfzValueExpressionPrimary(context));\n" +
                    "           }\n" +
                    "       };\n" +
                    "   }"),
                    TestUtils.pureCode(method.toString()));
        }
    }

    private SyntaxTree readSyntaxTree(String source) throws FileNotFoundException, ParseException {
        Reader reader = new InputStreamReader(new FileInputStream(source));
        SymqleParser parser = new SymqleParser(reader);
        final SyntaxTree syntaxTree = new SyntaxTree(parser.SymqleUnit(), source);
        return syntaxTree;
    }

    public void testInterfaceMisspelling() throws Exception {
        final Model model = ModelUtils.prepareModel();
        SyntaxTree syntaxTree = readSyntaxTree("src/test-data/model/UnknownInterfaceInProduction.sdl");
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
        final Model model = ModelUtils.prepareModel();
        SyntaxTree syntaxTree = readSyntaxTree("src/test-data/model/Parentheses.sdl");
        new InterfaceDeclarationsProcessor().process(syntaxTree, model);
        new ClassDeclarationProcessor().process(syntaxTree, model);
        new ProductionDeclarationProcessor().process(syntaxTree, model);
        ClassDefinition symqle = model.getClassDef("Symqle");
        MethodDefinition method = symqle.getDeclaredMethodBySignature("z$Subquery$from$SelectList(SelectList)");
        assertEquals(TestUtils.pureCode(
                "static <T> Subquery<T> z$Subquery$from$SelectList(final SelectList<T> sl) { \n" +
                        "        return new AbstractSubquery<T>() {\n" +
                        "            /**\n" +
                        "            * Creates a Query representing <code>this</code>\n" +
                        "            * @param context the Sql construction context\n" +
                        "            * @return query conforming to <code>this</code> syntax\n" +
                        "            */\n" +
                        "            public Query<T> z$sqlOfSubquery(final SqlContext context) {\n" +
                        "                final Query<T> __rowMapper = sl.z$sqlOfSelectList(context); " +
                        "            return new ComplexQuery<T>(__rowMapper, " +
                        "                context.get(Dialect.class).Subquery_is_LEFT_PAREN_SelectList_RIGHT_PAREN(__rowMapper));" +
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
        final SyntaxTree syntaxTree = readSyntaxTree(source);
        new InterfaceDeclarationsProcessor().process(syntaxTree, model);
        new ClassDeclarationProcessor().process(syntaxTree, model);
        new ProductionDeclarationProcessor().process(syntaxTree, model);

        final ClassDefinition symqle = model.getClassDef("Symqle");

        Utils.createParser(symqle.toString()).SymqleDeclarationBlock();

    }

}
