package org.simqle.generator;

import junit.framework.TestCase;
import org.simqle.model.*;
import org.simqle.parser.SimqleParser;
import org.simqle.parser.SyntaxTree;
import org.simqle.processor.*;
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
public class TestManualProduction extends TestCase {

    public void testDeclarationsInClassDefinition() throws Exception {
        Model model = new Model();
        SimqleParser parser = new SimqleParser(new FileReader("src/test-data/ManualProduction.sdl"));
        SyntaxTree node = new SyntaxTree(parser.SimqleUnit(), "ManualProduction.sdl");
        new InterfaceDeclarationsProcessor().process(node, model);
        new ClassDeclarationProcessor().process(node, model);
        new ProductionDeclarationProcessor().process(node, model);
        new MimicsProcessor().process(model);
        verifyBooleanExpressionClass(model);
        verifyProductionMethod(model);
    }

    public void testDeclarationsInAddendum() throws Exception {
        Model model = new Model();
        SimqleParser parser = new SimqleParser(new FileReader("src/test-data/ManualProductionWithAddendum.sdl"));
        SyntaxTree node = new SyntaxTree(parser.SimqleUnit(), "ManualProductionWithAddendum.sdl");
        new InterfaceDeclarationsProcessor().process(node, model);
        new ClassDeclarationProcessor().process(node, model);
        new ProductionDeclarationProcessor().process(node, model);
        new MimicsProcessor().process(model);
        verifyBooleanExpressionClass(model);
        verifyProductionMethod(model);
        final List<String> otherDeclarations = model.getClassPair("BooleanExpression").getBase().getBody().getOtherDeclarations();
        assertEquals(1, otherDeclarations.size());
        assertEquals("private static class Inner { }", TestUtils.normalizeFormatting(otherDeclarations.get(0)));
        assertEquals(0, model.getClassPair("BooleanExpression").getExtension().getBody().getOtherDeclarations().size());
    }

    public void testDuplicateMethod() throws Exception {
            Model model = new Model();
            SimqleParser parser = new SimqleParser(new FileReader("src/test-data/AddendumDuplicateMethod.sdl"));
            SyntaxTree node = new SyntaxTree(parser.SimqleUnit(), "AddendumDuplicateMethod.sdl");
        try {
            new InterfaceDeclarationsProcessor().process(node, model);
            new ClassDeclarationProcessor().process(node, model);
            new ProductionDeclarationProcessor().process(node, model);
            fail("GrammarException expected");
        } catch (GrammarException e) {
            assertTrue(e.getMessage().startsWith("Duplicate method: value"));
        }

    }

    public void testDuplicateField() throws Exception {
            Model model = new Model();
            SimqleParser parser = new SimqleParser(new FileReader("src/test-data/AddendumDuplicateField.sdl"));
            SyntaxTree node = new SyntaxTree(parser.SimqleUnit(), "AddendumDuplicateField.sdl");
        try {
            new InterfaceDeclarationsProcessor().process(node, model);
            new ClassDeclarationProcessor().process(node, model);
            new ProductionDeclarationProcessor().process(node, model);
            fail("GrammarException expected");
        } catch (GrammarException e) {
            assertTrue(e.getMessage(), e.getMessage().startsWith("Duplicate field: myBuilder"));
        }

    }

    private void verifyBooleanExpressionClass(final Model model) throws Exception {
        final ClassPair classPair = model.getClassPair("BooleanExpression");
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
            assertEquals("myContext", formalParameter.getName());
            final List<TypeNameWithTypeArguments> parameterNameChain = formalParameter.getType().getNameChain();
            assertEquals(1, parameterNameChain.size());
            final TypeNameWithTypeArguments parameterType = parameterNameChain.get(0);
            assertEquals("SqlContext", parameterType.getName());
            assertEquals(0, parameterType.getTypeArguments().size());
            assertEquals(Arrays.asList("final"), formalParameter.getModifiers());
            assertEquals("{ myBuilder.z$prepare$boolean_expression(myContext); }", TestUtils.normalizeFormatting(prepareMethod.getMethodBody()));
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
            assertEquals("myElement", formalParameter.getName());
            final List<TypeNameWithTypeArguments> parameterNameChain = formalParameter.getType().getNameChain();
            assertEquals(1, parameterNameChain.size());
            final TypeNameWithTypeArguments parameterType = parameterNameChain.get(0);
            assertEquals("Element", parameterType.getName());
            assertEquals(0, parameterType.getTypeArguments().size());
            assertEquals(Arrays.asList("final"), formalParameter.getModifiers());
            assertEquals("{ return myBuilder.value(myElement); }", TestUtils.normalizeFormatting(valueMethod.getMethodBody()));
            // make sure the body is compilable
            new SimqleParser(new StringReader(valueMethod.getMethodBody())).Block();
        }

        {
            assertEquals(1, base.getBody().getConstructors().size());
            final ConstructorDeclaration constructor = base.getBody().getConstructors().get(0);
            assertEquals("BooleanExpression", constructor.getName());
            assertEquals(1, constructor.getFormalParameters().size());
            assertEquals("final boolean_expression myBuilder", constructor.getFormalParameters().get(0).getImage());
            assertEquals("{ this.myBuilder = myBuilder; }", TestUtils.normalizeFormatting(constructor.getBody()));
        }
        {
            final ConstructorDeclaration constructor = base.getBody().getConstructors().get(0);
            assertEquals("BooleanExpression", constructor.getName());
            assertEquals(1, constructor.getFormalParameters().size());
            assertEquals("final boolean_expression myBuilder", constructor.getFormalParameters().get(0).getImage());
            assertEquals("{ this.myBuilder = myBuilder; }", TestUtils.normalizeFormatting(constructor.getBody()));
        }

        {
            final ConstructorDeclaration constructor = classPair.getExtension().getBody().getConstructors().get(0);
            assertEquals("BooleanExpression", constructor.getName());
            assertEquals(1, constructor.getFormalParameters().size());
            assertEquals("final boolean_expression myBuilder", constructor.getFormalParameters().get(0).getImage());
            assertEquals("{ super(myBuilder); }", TestUtils.normalizeFormatting(constructor.getBody()));
        }

    }

    private void verifyProductionMethod(final Model model) throws Exception {
        assertEquals(1, model.getAllFactoryMethods().size());
        final FactoryMethodModel factoryMethod = model.getAllFactoryMethods().get(0);
        final MethodDeclaration methodDeclaration = factoryMethod.getMethodDeclaration();
        assertEquals("boolean_expression_IS_expression_", methodDeclaration.getName());
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
        assertEquals("expression_<Boolean>", formalParameter.getType().getImage());
        String expectedBody = "{ return new boolean_expression() {\n" +
                "       @Override\n" +
                "       public Boolean value(final Element myElement) {\n" +
                "            return expr.value(myElement);\n" +
                "       }\n" +
                "        @Override\n" +
                "        public void z$prepare$boolean_expression(final SqlContext myContext) {\n" +
                "             expr.z$prepare$expression_(myContext);\n" +
                "        }\n" +
                "        @Override\n" +
                "        public Sql z$create$boolean_expression(final SqlContext myContext) {\n" +
                "            return new CompositeSql(expr.z$create$expression_(myContext));\n" +
                "        }\n" +
                "    };\n" +
                "}";
        assertEquals(expectedBody, methodDeclaration.getMethodBody().trim());
        // make sure the body is compilable
        new SimqleParser(new StringReader(methodDeclaration.getMethodBody())).Block();

    }


}
