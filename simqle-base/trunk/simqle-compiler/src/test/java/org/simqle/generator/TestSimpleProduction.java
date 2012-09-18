/*
* Copyright Alexander Izyurov 2010
*/
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <br/>15.11.2011
 *
 * @author Alexander Izyurov
 */
public class TestSimpleProduction extends TestCase {

    public void testParsing() throws Exception {
        Model model = new Model();
        {
            SimqleParser parser = new SimqleParser(new FileReader("src/test-data/InterfaceTest.sdl"));
            SyntaxTree node = new SyntaxTree(parser.SimqleUnit(), "InterfaceTest.sdl");
            Processor processor = new InterfaceDeclarationsProcessor();
            processor.process(node, model);
        }
        {
            SimqleParser parser = new SimqleParser(new FileReader("src/test-data/SimpleProductionTest.sdl"));
            SyntaxTree node = new SyntaxTree(parser.SimqleUnit(), "SimpleProductionTest.sdl");
            Processor interfaceProcessor = new InterfaceDeclarationsProcessor();
            interfaceProcessor.process(node, model);
            Processor processor = new ClassDeclarationProcessor();
            processor.process(node, model);
            Processor productionProcessor = new ProductionDeclarationProcessor();
            productionProcessor.process(node, model);
        }
        final List<ClassPair> classDefinitionList = model.getAllClasses();
        assertEquals(2, classDefinitionList.size());
        final ClassPair classPair = model.getClassPair("CursorSpecification");
        assertEquals(1, classPair.getMimics().size());
        final List<String> importLines = classPair.getPublishedImports();
        assertTrue(importLines.contains("import java.util.Map;"));
        assertEquals(1, importLines.size());
        final List<String> internalImportLines = classPair.getInternalImports();
        assertTrue(internalImportLines.contains("import java.util.HashMap;"));
        assertEquals(1, internalImportLines.size());
        {
            final ClassDefinition def = classPair.getBase();

            final Body body = def.getBody();
            // declared method an 2 interface methods
            assertEquals(2, body.getMethods().size());
            {
                final MethodDeclaration prepareMethod = body.getMethod("z$prepare$cursor_specification(SqlContext)");
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
                assertEquals("{ sqlBuilder.z$prepare$cursor_specification(context); }", TestUtils.normalizeFormatting(prepareMethod.getMethodBody()));
                // make sure the body is compilable
                new SimqleParser(new StringReader(prepareMethod.getMethodBody())).Block();
            }


            {
                final MethodDeclaration createMethod = body.getMethod("z$create$cursor_specification(SqlContext)");
                assertNotNull(createMethod);
                assertEquals("public", createMethod.getAccessModifier());
                assertFalse(createMethod.isStatic());
                assertFalse(createMethod.isAbstract());
                assertEquals("", createMethod.getThrowsClause());
                assertEquals(0, createMethod.getTypeParameters().size());
                assertEquals("Query<T>", createMethod.getResultType().getImage());
                final List<FormalParameter> formalParameters = createMethod.getFormalParameters();
                assertEquals(1, formalParameters.size());
                final FormalParameter formalParameter = formalParameters.get(0);
                assertEquals("context", formalParameter.getName());
                final List<TypeNameWithTypeArguments> parameterNameChain = formalParameter.getType().getNameChain();
                assertEquals(1, parameterNameChain.size());
                final TypeNameWithTypeArguments parameterType = parameterNameChain.get(0);
                assertEquals("SqlContext", parameterType.getName());
                assertEquals(0, parameterType.getTypeArguments().size());
                assertEquals(Arrays.asList("final"), formalParameter.getModifiers());
                assertEquals("{ return sqlBuilder.z$create$cursor_specification(context); }", TestUtils.normalizeFormatting(createMethod.getMethodBody()));
                // make sure the body is compilable
                new SimqleParser(new StringReader(createMethod.getMethodBody())).Block();

            }

            assertEquals(1, body.getFields().size());
            {
                final FieldDeclaration fieldDeclaration = body.getFields().get(0);
                assertEquals("private", fieldDeclaration.getAccessModifier());
                assertEquals(Arrays.asList("final"), fieldDeclaration.getOtherModifiers());
                assertEquals(1, fieldDeclaration.getDeclarators().size());
                assertEquals("sqlBuilder", fieldDeclaration.getDeclarators().get(0).getName());
                assertEquals("", fieldDeclaration.getDeclarators().get(0).getInitializer());
            }

            assertNull(def.getExtendedClass());
            assertEquals(1, body.getConstructors().size());
            final ConstructorDeclaration constructor = body.getConstructors().get(0);
            // actually the constructor name should be @SelectStatement$", the assigned value will be ignored at class generation
            assertEquals("CursorSpecification", constructor.getName());
            assertEquals(1, constructor.getFormalParameters().size());
            assertEquals("final cursor_specification<T> sqlBuilder", constructor.getFormalParameters().get(0).getImage());
            assertEquals("{ this.sqlBuilder = sqlBuilder; }", TestUtils.normalizeFormatting(constructor.getBody()));
        }
        {
            final ClassDefinition def = classPair.getExtension();

            assertEquals("CursorSpecification", def.getPairName());
            final Body body = def.getBody();
            // declared method and 2 interface methods are not overridden in extension class
            assertEquals(0, body.getMethods().size());
            assertEquals(0, body.getFields().size());
            assertNotNull(def.getExtendedClass());
            assertEquals(1, body.getConstructors().size());
            // should have a constructor in extension class with the same parameter(s) as in the base class
            final ConstructorDeclaration constructor = body.getConstructors().get(0);
            assertEquals("CursorSpecification", constructor.getName());
            assertEquals(1, constructor.getFormalParameters().size());
            assertEquals("final cursor_specification<T> sqlBuilder", constructor.getFormalParameters().get(0).getImage());
            assertEquals("{ super(sqlBuilder); }", TestUtils.normalizeFormatting(constructor.getBody()));
        }

        {
            assertEquals(1, model.getAllFactoryMethods().size());
            final FactoryMethodModel factoryMethod = model.getAllFactoryMethods().get(0);
            final MethodDeclaration methodDeclaration = factoryMethod.getMethodDeclaration();
            assertEquals("select_statement_IS_cursor_specification", methodDeclaration.getName());
            assertEquals("select_statement<T>", methodDeclaration.getResultType().getImage());
            assertFalse(methodDeclaration.isStatic());
            assertFalse(methodDeclaration.isAbstract());
            assertEquals("public", methodDeclaration.getAccessModifier());
            assertEquals("", methodDeclaration.getThrowsClause());
            assertEquals(1, methodDeclaration.getTypeParameters().size());
            assertEquals("T", methodDeclaration.getTypeParameters().get(0).getImage());
            assertEquals(1, methodDeclaration.getFormalParameters().size());
            final FormalParameter formalParameter = methodDeclaration.getFormalParameters().get(0);
            assertEquals(Arrays.asList("final"), formalParameter.getModifiers());
            assertEquals("cursorSpec", formalParameter.getName());
            assertEquals("cursor_specification<T>", formalParameter.getType().getImage());
            String expectedBody = "{ return new select_statement<T>() {\n" +
                    "    @Override\n" +
                    "    public void z$prepare$select_statement(final SqlContext context) {\n" +
                    "         cursorSpec.z$prepare$cursor_specification(context);\n" +
                    "    }\n" +
                    "    @Override\n" +
                    "    public Query<T> z$create$select_statement(final SqlContext context) {\n" +
                    "        return new CompositeQuery<T>(cursorSpec.z$create$cursor_specification(context));\n" +
                    "    }\n" +
                    "    };\n" +
                    "}";
            assertEquals(expectedBody, methodDeclaration.getMethodBody());
            // make sure the body is compilable
            new SimqleParser(new StringReader(methodDeclaration.getMethodBody())).Block();
        }


    }

    public void testWrongTypeInMimics() throws Exception {
        Model model = new Model();
        {
            SimqleParser parser = new SimqleParser(new FileReader("src/test-data/InterfaceTest.sdl"));
            SyntaxTree node = new SyntaxTree(parser.SimqleUnit(), "InterfaceTest.sdl");
            Processor processor = new InterfaceDeclarationsProcessor();
            processor.process(node, model);
        }
        SimqleParser parser = new SimqleParser(new FileReader("src/test-data/WrongTypeInMimics.sdl"));
        SyntaxTree node = new SyntaxTree(parser.SimqleUnit(), "WrongTypeInMimics.sdl");
        Processor interfaceProcessor = new InterfaceDeclarationsProcessor();
        interfaceProcessor.process(node, model);
        Processor processor = new ClassDeclarationProcessor();
        processor.process(node, model);
        Processor productionProcessor = new ProductionDeclarationProcessor();
        try {
            productionProcessor.process(node, model);
            fail("GrammarException expected");
        } catch (GrammarException e) {
            e.printStackTrace();
            assertTrue(e.getMessage().startsWith("TypeParameters do not match"));
        }

    }

    public void testDoubleMimics() throws Exception {
        Model model = new Model();
        SimqleParser parser = new SimqleParser(new FileReader("src/test-data/DoubleMimics.sdl"));
        SyntaxTree node = new SyntaxTree(parser.SimqleUnit(), "DoubleMimics.sdl");
        Processor interfaceProcessor = new InterfaceDeclarationsProcessor();
        interfaceProcessor.process(node, model);
        Processor processor = new ClassDeclarationProcessor();
        processor.process(node, model);
        Processor productionProcessor = new ProductionDeclarationProcessor();
        productionProcessor.process(node, model);
        final ClassPair classPair = model.getClassPair("BooleanExpression");
        final ClassDefinition base = classPair.getBase();
        assertEquals(1, classPair.getMimics().size());
        assertEquals("Expression", classPair.getMimics().iterator().next().getNameChain().get(0).getName());

    }

    public void testWrongDoubleMimics() throws Exception {
        Model model = new Model();
        SimqleParser parser = new SimqleParser(new FileReader("src/test-data/WrongDoubleMimics.sdl"));
        SyntaxTree node = new SyntaxTree(parser.SimqleUnit(), "WrongDoubleMimics.sdl");
        Processor interfaceProcessor = new InterfaceDeclarationsProcessor();
        interfaceProcessor.process(node, model);
        Processor processor = new ClassDeclarationProcessor();
        processor.process(node, model);
        Processor productionProcessor = new ProductionDeclarationProcessor();
        try {
            productionProcessor.process(node, model);
            fail("GrammarException expected");
        } catch (GrammarException e) {
            assertTrue(e.getMessage().startsWith("Cannot mimic one class with different type parameters"));
        }
    }

    public void testConstantsInProduction() throws Exception {
        Model model = new Model();
        SimqleParser parser = new SimqleParser(new FileReader("src/test-data/ProductionWithConstants.sdl"));
        SyntaxTree node = new SyntaxTree(parser.SimqleUnit(), "ProductionWithConstants.sdl");
        new InterfaceDeclarationsProcessor().process(node, model);
        new ClassDeclarationProcessor().process(node, model);
        new ProductionDeclarationProcessor().process(node, model);
        final List<FactoryMethodModel> allFactoryMethods = model.getAllFactoryMethods();
        Map<String, FactoryMethodModel> methodsByName = new HashMap<String, FactoryMethodModel>();
        for (FactoryMethodModel method: allFactoryMethods) {
            methodsByName.put(method.getName(), method);
        }
        final FactoryMethodModel method1 = methodsByName.get("primary_IS_LEFT_PAREN_expression_RIGHT_PAREN");
        assertNotNull(method1);
        final Type resultType = method1.getMethodDeclaration().getResultType();
        assertEquals("primary<T>", resultType.getImage());
        assertEquals("public", method1.getMethodDeclaration().getAccessModifier());
        assertEquals(1, method1.getMethodDeclaration().getFormalParameters().size());
        assertEquals("final expression<T> expr", method1.getMethodDeclaration().getFormalParameters().get(0).getImage());
        final String body = method1.getMethodDeclaration().getMethodBody();
        assertEquals(TestUtils.normalizeFormatting("{ return new primary<T>() {\n" +
                "       @Override\n" +
                "       public T value(final Element element) {\n" +
                "            return expr.value(element); \n" +
                "       }\n" +
                "    @Override\n" +
                "    public void z$prepare$primary(final SqlContext context) {\n" +
                "         expr.z$prepare$expression(context);\n" +
                "    }\n" +
                "    @Override\n" +
                "    public Sql z$create$primary(final SqlContext context) {\n" +
                "        return new CompositeSql(LEFT_PAREN, expr.z$create$expression(context), RIGHT_PAREN);    }\n" +
                "    };\n" +
                "}"), TestUtils.normalizeFormatting(body));
    }

    public void testMistypeInProductionResult() throws Exception {
        Model model = new Model();
        SimqleParser parser = new SimqleParser(new FileReader("src/test-data/MistypeInProductionResult.sdl"));
        SyntaxTree node = new SyntaxTree(parser.SimqleUnit(), "MistypeInProductionResult.sdl");
        new InterfaceDeclarationsProcessor().process(node, model);
        new ClassDeclarationProcessor().process(node, model);
        try {
            new ProductionDeclarationProcessor().process(node, model);
            fail("GrammarException expected");
        } catch (GrammarException e) {
            assertTrue(e.getMessage(), e.getMessage().startsWith("Unknown interface: prmary"));
        }
    }

    public void testMistypeInProductionElementType() throws Exception {
        Model model = new Model();
        SimqleParser parser = new SimqleParser(new FileReader("src/test-data/MistypeInProductionElementType.sdl"));
        SyntaxTree node = new SyntaxTree(parser.SimqleUnit(), "MistypeInProductionElementType.sdl");
        new InterfaceDeclarationsProcessor().process(node, model);
        new ClassDeclarationProcessor().process(node, model);
        try {
            new ProductionDeclarationProcessor().process(node, model);
            fail("GrammarException expected");
        } catch (GrammarException e) {
            assertTrue(e.getMessage(), e.getMessage().startsWith("Unknown interface: epression"));
        }
    }

    public void testWrongTypeParametersOfProductionResult() throws Exception {
        Model model = new Model();
        SimqleParser parser = new SimqleParser(new FileReader("src/test-data/WrongTypeParametersOfProductionResult.sdl"));
        SyntaxTree node = new SyntaxTree(parser.SimqleUnit(), "WrongTypeParametersOfProductionResult.sdl");
        new InterfaceDeclarationsProcessor().process(node, model);
        new ClassDeclarationProcessor().process(node, model);
        try {
            new ProductionDeclarationProcessor().process(node, model);
            fail("GrammarException expected");
        } catch (GrammarException e) {
            assertTrue(e.getMessage(), e.getMessage().startsWith("Return type expression requires 1 type parameter, found: 0"));
        }
    }

    public void testWrongTypeParametersOfRuleElement() throws Exception {
        Model model = new Model();
        SimqleParser parser = new SimqleParser(new FileReader("src/test-data/WrongTypeParametersOfRuleElement.sdl"));
        SyntaxTree node = new SyntaxTree(parser.SimqleUnit(), "WrongTypeParametersOfRuleElement.sdl");
        new InterfaceDeclarationsProcessor().process(node, model);
        new ClassDeclarationProcessor().process(node, model);
        try {
            new ProductionDeclarationProcessor().process(node, model);
            fail("GrammarException expected");
        } catch (GrammarException e) {
            assertTrue(e.getMessage(), e.getMessage().startsWith("Rule element prim:primary requires 1 type parameter, found: 0"));
        }
    }

    public void testWrongConstantRuleElement() throws Exception {
        Model model = new Model();
        SimqleParser parser = new SimqleParser(new FileReader("src/test-data/WrongConstantRuleElement.sdl"));
        SyntaxTree node = new SyntaxTree(parser.SimqleUnit(), "WrongConstantRuleElement.sdl");
        new InterfaceDeclarationsProcessor().process(node, model);
        new ClassDeclarationProcessor().process(node, model);
        try {
            new ProductionDeclarationProcessor().process(node, model);
            fail("GrammarException expected");
        } catch (GrammarException e) {
            assertTrue(e.getMessage(), e.getMessage().startsWith("LEFTPAREN is not a constant non-terminal"));
        }
    }

    public void testDuplicateProduction() throws Exception {
        Model model = new Model();
        SimqleParser parser = new SimqleParser(new FileReader("src/test-data/DuplicateProduction.sdl"));
        SyntaxTree node = new SyntaxTree(parser.SimqleUnit(), "DuplicateProduction.sdl");
        new InterfaceDeclarationsProcessor().process(node, model);
        new ClassDeclarationProcessor().process(node, model);
        try {
            new ProductionDeclarationProcessor().process(node, model);
            fail("GrammarException expected");
        } catch (GrammarException e) {
            assertTrue(e.getMessage(), e.getMessage().startsWith("Duplicate rule primary_IS_LEFT_PAREN_expression_RIGHT_PAREN"));
        }
    }

    public void testClassNameMistypeInAddendum() throws Exception {
        Model model = new Model();
        SimqleParser parser = new SimqleParser(new FileReader("src/test-data/ClassNameMistypeInAddendum.sdl"));
        SyntaxTree node = new SyntaxTree(parser.SimqleUnit(), "ClassNameMistypeInAddendum");
        new InterfaceDeclarationsProcessor().process(node, model);
        new ClassDeclarationProcessor().process(node, model);
        try {
            new ProductionDeclarationProcessor().process(node, model);
            fail("GrammarException expected");
        } catch (GrammarException e) {
            assertTrue(e.getMessage(), e.getMessage().startsWith("Class not found: BoleanExpression"));
        }
    }

    public void testNoGuessForScalar() throws Exception {
        Model model = new Model();
        SimqleParser parser = new SimqleParser(new FileReader("src/test-data/NoGuessForScalar.sdl"));
        SyntaxTree node = new SyntaxTree(parser.SimqleUnit(), "NoGuessForScalar");
        new InterfaceDeclarationsProcessor().process(node, model);
        new ClassDeclarationProcessor().process(node, model);
        try {
            new ProductionDeclarationProcessor().process(node, model);
            fail("GrammarException expected");
        } catch (GrammarException e) {
            assertTrue(e.getMessage(), e.getMessage().startsWith("Method Boolean value(Element element) must be implemented; cannot guess implementation"));
        }
    }

    public void testNoGuessForScalar2() throws Exception {
        Model model = new Model();
        SimqleParser parser = new SimqleParser(new FileReader("src/test-data/NoGuessForScalar2.sdl"));
        SyntaxTree node = new SyntaxTree(parser.SimqleUnit(), "NoGuessForScalar2");
        new InterfaceDeclarationsProcessor().process(node, model);
        new ClassDeclarationProcessor().process(node, model);
        try {
            new ProductionDeclarationProcessor().process(node, model);
            fail("GrammarException expected");
        } catch (GrammarException e) {
            assertTrue(e.getMessage(), e.getMessage().startsWith("Method Boolean value(Element element) must be implemented; cannot guess implementation"));
        }
    }

    public void testNoGuessForQuery() throws Exception {
        Model model = new Model();
        SimqleParser parser = new SimqleParser(new FileReader("src/test-data/NoGuessForQuery.sdl"));
        SyntaxTree node = new SyntaxTree(parser.SimqleUnit(), "NoGuessForQuery");
        new InterfaceDeclarationsProcessor().process(node, model);
        new ClassDeclarationProcessor().process(node, model);
        try {
            new ProductionDeclarationProcessor().process(node, model);
            fail("GrammarException expected");
        } catch (GrammarException e) {
            assertTrue(e.getMessage(), e.getMessage().startsWith("Method Query<T> z$create$select_sublist(SqlContext context) must be implemented; cannot guess implementation"));
        }
    }

    public void testQueryAutogeneration() throws Exception {
        Model model = new Model();
        SimqleParser parser = new SimqleParser(new FileReader("src/test-data/QueryTest.sdl"));
        SyntaxTree node = new SyntaxTree(parser.SimqleUnit(), "QueryTest");
        new InterfaceDeclarationsProcessor().process(node, model);
        new ClassDeclarationProcessor().process(node, model);
        new ProductionDeclarationProcessor().process(node, model);
        final List<FactoryMethodModel> allFactoryMethods = model.getAllFactoryMethods();
        assertEquals(1, allFactoryMethods.size());
        final FactoryMethodModel factoryMethodModel = allFactoryMethods.get(0);
        assertEquals("query_base_IS_SELECT_select_list", factoryMethodModel.getName());
        assertEquals("query_base<T>", factoryMethodModel.getMethodDeclaration().getResultType().getImage());
        assertEquals(1, factoryMethodModel.getMethodDeclaration().getFormalParameters().size());
        assertEquals("final select_list<T> list", factoryMethodModel.getMethodDeclaration().getFormalParameters().get(0).getImage());
        final String body = factoryMethodModel.getMethodDeclaration().getMethodBody();
        assertEquals(TestUtils.normalizeFormatting("{ return new query_base<T>() {\n" +
                "    @Override\n" +
                "    public void z$prepare$query_base(final SqlContext context) {\n" +
                "         list.z$prepare$select_list(context);\n" +
                "    }\n" +
                "    @Override\n" +
                "    public Query<T> z$create$query_base(final SqlContext context) {\n" +
                "        final DataExtractor<T> list_query = list.z$create$select_list(context); \n" +
                "        return new CompoundQuery<T>(list_query, new CompositeSql(SELECT, list_query)\n" +
                "            );\n" +
                "        }\n" +
                "    };\n" +
                "}"), TestUtils.normalizeFormatting(body));
    }

    public void testWrongTypeParametersInProduction() throws Exception {
        Model model = new Model();
        SimqleParser parser = new SimqleParser(new FileReader("src/test-data/WrongTypeParametersInProduction.sdl"));
        SyntaxTree node = new SyntaxTree(parser.SimqleUnit(), "WrongTypeParametersInProduction.sdl");
        new InterfaceDeclarationsProcessor().process(node, model);
        new ClassDeclarationProcessor().process(node, model);
        try {
            new ProductionDeclarationProcessor().process(node, model);
            fail("GrammarException expected");
        } catch (GrammarException e) {
            assertTrue(e.getMessage(), e.getMessage().startsWith("list:select_list requires 1 type parameters, found: 0"));
        }
    }

    public void testMultiParameterProduction() throws Exception {
        Model model = new Model();
        SimqleParser parser = new SimqleParser(new FileReader("src/test-data/MultiParameterProduction.sdl"));
        SyntaxTree node = new SyntaxTree(parser.SimqleUnit(), "MultiParameterProduction.sdl");
        new InterfaceDeclarationsProcessor().process(node, model);
        new ClassDeclarationProcessor().process(node, model);
        new ProductionDeclarationProcessor().process(node, model);
        final List<FactoryMethodModel> allFactoryMethods = model.getAllFactoryMethods();
        assertEquals(1, allFactoryMethods.size());
        final FactoryMethodModel factoryMethodModel = allFactoryMethods.get(0);
        assertEquals("select_list_IS_select_list_COMMA_select_list", factoryMethodModel.getName());
        assertEquals("select_list<Pair<T,U>>", factoryMethodModel.getMethodDeclaration().getResultType().getImage());
        assertEquals(2, factoryMethodModel.getMethodDeclaration().getFormalParameters().size());
        assertEquals("final select_list<T> first", factoryMethodModel.getMethodDeclaration().getFormalParameters().get(0).getImage());
        assertEquals("final select_list<U> second", TestUtils.normalizeFormatting(factoryMethodModel.getMethodDeclaration().getFormalParameters().get(1).getImage()));
        final String body = factoryMethodModel.getMethodDeclaration().getMethodBody();
        assertEquals(TestUtils.normalizeFormatting(" {\n" +
                "    return new select_list<Pair<T,U>>() {\n" +
                "        public Query<Pair<T,U>> z$create$select_list(final SqlContext context) {\n" +
                "            final Query<T> sql0 = arg0.z$create$select_list(context);\n" +
                "            final Query<U> sql1 = arg1.z$create$select_list(context);\n" +
                "            DataExtractor<Pair<T,U>> extractor = new DataExtractor<Pair<T, U>>() {\n" +
                "                public Pair<T, U> extract(final Row row) throws SQLException {\n" +
                "                    final T first = sql0.extract(row);\n" +
                "                    final U second = sql1.extract(row);\n" +
                "                    return Pair.of(first, second);\n" +
                "                }\n" +
                "            };\n" +
                "            return new CompoundQuery<Pair<T,U>>(extractor, new CompositeSql(sql0, SqlTerminal.COMMA, sql1));\n" +
                "        }\n" +
                "    };\n" +
                "}"), TestUtils.normalizeFormatting(body));
    }

    public void testWrongReturnType() throws Exception {
        Model model = new Model();
        SimqleParser parser = new SimqleParser(new FileReader("src/test-data/WrongReturnTypeInProduction.sdl"));
        SyntaxTree node = new SyntaxTree(parser.SimqleUnit(), "WrongReturnTypeInProduction.sdl");
        new InterfaceDeclarationsProcessor().process(node, model);
        new ClassDeclarationProcessor().process(node, model);
        try {
            new ProductionDeclarationProcessor().process(node, model);
            fail("GrammarException expected");
        } catch (GrammarException e) {
            assertTrue(e.getMessage().startsWith("Does not have Query nor Sql archetype: Scalar["));
        }

    }


}
