package org.simqle.model;

import junit.framework.TestCase;
import org.simqle.parser.SimqleParser;
import org.simqle.parser.SyntaxTree;
import org.simqle.processor.ClassDeclarationProcessor;
import org.simqle.processor.InterfaceDeclarationsProcessor;
import org.simqle.processor.ProductionDeclarationProcessor;
import org.simqle.test.TestUtils;
import org.simqle.util.Utils;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * @author lvovich
 */
public class ProductionsTest extends TestCase {

    public void testBasicProduction() throws Exception {
        String source = "src/test-data/model/BasicProduction.sdl";
        Reader reader = new InputStreamReader(new FileInputStream(source));
        SimqleParser parser = new SimqleParser(reader);
        final SyntaxTree syntaxTree = new SyntaxTree(parser.SimqleUnit(), source);
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
            assertEquals("public", method.getAccessModifier());
        }
        {
            final MethodDefinition method = simqle.getDeclaredMethodBySignature("asSelectStatement(zCursorSpecification)");
            assertEquals("public abstract <T> zSelectStatement<T> asSelectStatement(zCursorSpecification<T> cspec)",
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
        Reader reader = new InputStreamReader(new FileInputStream(source));
        SimqleParser parser = new SimqleParser(reader);
        final SyntaxTree syntaxTree = new SyntaxTree(parser.SimqleUnit(), source);
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
            assertEquals("public", method.getAccessModifier());
        }
        {
            final MethodDefinition method = simqleGeneric.getDeclaredMethodBySignature("asSelectStatement(zCursorSpecification)");
            assertEquals("public <T> zSelectStatement<T> asSelectStatement(final zCursorSpecification<T> cspec)",
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
        Reader reader = new InputStreamReader(new FileInputStream(source));
        SimqleParser parser = new SimqleParser(reader);
        final SyntaxTree syntaxTree = new SyntaxTree(parser.SimqleUnit(), source);
        final Model model = new Model();
        new InterfaceDeclarationsProcessor().process(syntaxTree, model);
        new ClassDeclarationProcessor().process(syntaxTree, model);
        new ProductionDeclarationProcessor().process(syntaxTree, model);

        final ClassDefinition simqle = model.getClassDef("Simqle");
        final ClassDefinition simqleGeneric = model.getClassDef("SimqleGeneric");

        Utils.createParser(simqle.toString()).SimqleDeclarationBlock();
        Utils.createParser(simqleGeneric.toString()).SimqleDeclarationBlock();

        {
            final MethodDefinition method = simqleGeneric.getDeclaredMethodBySignature("asValue(zValueExpressionPrimary)");
            assertEquals(TestUtils.pureCode(
                    "    public <T> zValueExpression<T> asValue(final zValueExpressionPrimary<T> e) { \n" +
                    "        return new zValueExpression<T>() {\n" +
                            "    public T value(final Element element) throws SQLException {\n" +
                            "        return e.value(element);\n" +
                            "    }\n" +
                    "           public Sql z$create$zValueExpression(final SqlContext context) {\n" +
                    "               return new CompositeSql(e.z$create$zValueExpressionPrimary(context));\n" +
                    "           }\n" +
                    "       };\n" +
                    "   }"),
                    TestUtils.pureCode(method.toString()));
        }
    }

}
