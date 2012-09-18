package org.simqle.generator;

import junit.framework.TestCase;
import org.simqle.model.*;
import org.simqle.parser.SimqleParser;
import org.simqle.parser.SyntaxTree;
import org.simqle.processor.ClassDeclarationProcessor;
import org.simqle.processor.InterfaceDeclarationsProcessor;
import org.simqle.processor.ProductionDeclarationProcessor;
import org.simqle.test.TestUtils;

import java.io.FileReader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: lvovich
 * Date: 17.08.12
 * Time: 16:24
 * To change this template use File | Settings | File Templates.
 */
public class TestScalarProduction extends TestCase {

    public void testParsing() throws Exception {
        Model model = new Model();
        SimqleParser parser = new SimqleParser(new FileReader("src/test-data/ScalarProduction.sdl"));
        SyntaxTree node = new SyntaxTree(parser.SimqleUnit(), "ScalarProduction.sdl");
        new InterfaceDeclarationsProcessor().process(node, model);
        new ClassDeclarationProcessor().process(node, model);
        new ProductionDeclarationProcessor().process(node, model);
        verifyBooleanExpressionClass(model);
        verifyProductionMethod(model);
    }

    private void verifyBooleanExpressionClass(final Model model) throws Exception {
        final ClassPair classPair = model.getClassPair("BooleanExpression");
        {
            final ClassDefinition base = classPair.getBase();

            {
                final MethodDeclaration prepareMethod = base.getBody().getMethod("z$prepare$boolean_expression(SqlContext)");
                assertNotNull(prepareMethod);
                assertEquals("public", prepareMethod.getAccessModifier());
                assertFalse(prepareMethod.isStatic());
                assertFalse(prepareMethod.isAbstract());
                assertEquals("", prepareMethod.getThrowsClause());
                assertEquals(0, prepareMethod.getTypeParameters().size());
                assertEquals(Type.VOID, prepareMethod.getResultType());
                final List<FormalParameter> formalParameters = prepareMethod.getFormalParameters();
                assertEquals(1, formalParameters.size());
                final FormalParameter formalParameter = formalParameters.get(0);
                assertEquals("context", formalParameter.getName());
                final List<TypeNameWithTypeArguments> parameterNameChain = formalParameter.getType().getNameChain();
                assertEquals(1, parameterNameChain.size());
                final TypeNameWithTypeArguments parameterType = parameterNameChain.get(0);
                assertEquals("SqlContext", parameterType.getName());
                assertEquals(0, parameterType.getTypeArguments().size());
                assertEquals(Arrays.asList("final"), formalParameter.getModifiers());
                assertEquals("{ sqlBuilder.z$prepare$boolean_expression(context); }", TestUtils.normalizeFormatting(prepareMethod.getMethodBody()));
                // make sure the body is compilable
                new SimqleParser(new StringReader(prepareMethod.getMethodBody())).Block();
            }

            {
                final MethodDeclaration valueMethod = base.getBody().getMethod("value(Element)");
                assertNotNull(valueMethod);
                assertEquals("public", valueMethod.getAccessModifier());
                assertFalse(valueMethod.isStatic());
                assertFalse(valueMethod.isAbstract());
                assertEquals("", valueMethod.getThrowsClause());
                assertEquals(0, valueMethod.getTypeParameters().size());
                assertEquals("Boolean", valueMethod.getResultType().getImage());
                final List<FormalParameter> formalParameters = valueMethod.getFormalParameters();
                assertEquals(1, formalParameters.size());
                final FormalParameter formalParameter = formalParameters.get(0);
                assertEquals("element", formalParameter.getName());
                final List<TypeNameWithTypeArguments> parameterNameChain = formalParameter.getType().getNameChain();
                assertEquals(1, parameterNameChain.size());
                final TypeNameWithTypeArguments parameterType = parameterNameChain.get(0);
                assertEquals("Element", parameterType.getName());
                assertEquals(0, parameterType.getTypeArguments().size());
                assertEquals(Arrays.asList("final"), formalParameter.getModifiers());
                assertEquals("{ return sqlBuilder.value(element); }", TestUtils.normalizeFormatting(valueMethod.getMethodBody()));
                // make sure the body is compilable
                new SimqleParser(new StringReader(valueMethod.getMethodBody())).Block();
            }

        }
    }

    private void verifyProductionMethod(final Model model) throws Exception {
        assertEquals(1, model.getAllFactoryMethods().size());
        final FactoryMethodModel factoryMethod = model.getAllFactoryMethods().get(0);
        final MethodDeclaration methodDeclaration = factoryMethod.getMethodDeclaration();
        assertEquals("boolean_expression_IS_expression", methodDeclaration.getName());
        assertEquals("boolean_expression", methodDeclaration.getResultType().getImage());
        assertFalse(methodDeclaration.isStatic());
        assertFalse(methodDeclaration.isAbstract());
        assertEquals("public", methodDeclaration.getAccessModifier());
        assertEquals("", methodDeclaration.getThrowsClause());
        assertEquals(0, methodDeclaration.getTypeParameters().size());
        assertEquals(1, methodDeclaration.getFormalParameters().size());
        final FormalParameter formalParameter = methodDeclaration.getFormalParameters().get(0);
        assertEquals(Arrays.asList("final"), formalParameter.getModifiers());
        assertEquals("expr", formalParameter.getName());
        assertEquals("expression<Boolean>", formalParameter.getType().getImage());
        String expectedBody = "{ return new boolean_expression() {\n" +
                "       @Override\n" +
                "       public Boolean value(final Element element) {\n" +
                "            return expr.value(element); \n" +
                "       }\n" +
                "    @Override\n" +
                "    public void z$prepare$boolean_expression(final SqlContext context) {\n" +
                "         expr.z$prepare$expression(context);\n" +
                "    }\n" +
                "    @Override\n" +
                "    public Sql z$create$boolean_expression(final SqlContext context) {\n" +
                "        return new CompositeSql(expr.z$create$expression(context));    }\n" +
                "    };\n" +
                "}";
        assertEquals(expectedBody, methodDeclaration.getMethodBody());
        // make sure the body is compilable
        new SimqleParser(new StringReader(methodDeclaration.getMethodBody())).Block();

    }
}
