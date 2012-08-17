/*
* Copyright Alexander Izyurov 2010
*/
package org.simqle.generator;

import junit.framework.TestCase;
import org.simqle.model.*;
import org.simqle.parser.SimqleParser;
import org.simqle.parser.SyntaxTree;
import org.simqle.processor.ClassDeclarationProcessor;
import org.simqle.processor.InterfaceDeclarationsProcessor;
import org.simqle.processor.Processor;
import org.simqle.processor.ProductionDeclarationProcessor;
import org.simqle.test.TestUtils;

import java.io.FileReader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;

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
        {
            final ClassDefinition def = model.getClassPair("CursorSpecification").getBase();

            final List<String> importLines = def.getImports();
            assertTrue(importLines.contains("import java.util.Map;"));
            assertTrue(importLines.contains("import java.util.HashMap;"));
            assertEquals(2, importLines.size());
            final Body body = def.getBody();
            // declared method an 2 interface methods
            assertEquals(3, body.getMethods().size());
            {
                final MethodDeclaration prepareMethod = body.getMethod("z$prepare$cursor_specification");
                assertNotNull(prepareMethod);
                assertEquals("public", prepareMethod.getAccessModifier());
                assertFalse(prepareMethod.isStatic());
                assertFalse(prepareMethod.isAbstract());
                assertEquals("", prepareMethod.getThrowsClause());
                assertEquals(0, prepareMethod.getTypeParameters().size());
                assertNull(prepareMethod.getResultType());
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
                assertEquals(" { sqlBuilder.z$prepare$cursor_specification(context); } ", TestUtils.normalizeFormatting(prepareMethod.getMethodBody()));
                // make sure the body is compilable
                new SimqleParser(new StringReader(prepareMethod.getMethodBody())).Block();
            }


            {
                final MethodDeclaration createMethod = body.getMethod("z$create$cursor_specification");
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
                assertEquals(" { return sqlBuilder.z$create$cursor_specification(context); } ", TestUtils.normalizeFormatting(createMethod.getMethodBody()));
                // make sure the body is compilable
                new SimqleParser(new StringReader(createMethod.getMethodBody())).Block();

            }

            {
                final MethodDeclaration toSelectStatement = body.getMethod("toSelectStatement");
                assertNotNull(toSelectStatement);
                assertEquals("protected", toSelectStatement.getAccessModifier());
                assertFalse(toSelectStatement.isStatic());
                assertFalse(toSelectStatement.isAbstract());
                assertEquals("", toSelectStatement.getThrowsClause());
                assertEquals(0, toSelectStatement.getTypeParameters().size());
                assertEquals("SelectStatement<T>", toSelectStatement.getResultType().getImage());
                final List<FormalParameter> formalParameters = toSelectStatement.getFormalParameters();
                assertEquals(0, formalParameters.size());
                assertEquals("{ return new SelectStatement<T>(SqlFactory.getInstance().select_statement_IS_cursor_specification(this)); }", TestUtils.normalizeFormatting(toSelectStatement.getMethodBody()));
                // make sure the body is compilable
                new SimqleParser(new StringReader(toSelectStatement.getMethodBody())).Block();
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

            assertEquals(0, def.getMimics().size());
            assertNull(def.getExtendedClass());
            assertEquals(1, body.getConstructors().size());
            final ConstructorDeclaration constructor = body.getConstructors().get(0);
            // actually the constructor name should be @SelectStatement$", the assigned value will be ignored at class generation
            assertEquals("CursorSpecification", constructor.getName());
            assertEquals(1, constructor.getFormalParameters().size());
            assertEquals("final cursor_specification<T> sqlBuilder", constructor.getFormalParameters().get(0).getImage());
        }
        {
            final ClassDefinition def = model.getClassPair("CursorSpecification").getExtension();

            assertEquals("CursorSpecification", def.getPairName());
            final List<String> importLines = def.getImports();
            // by convention, all imports go to base, extention does not have imports
            assertEquals(0, importLines.size());
            final Body body = def.getBody();
            // declared method and 2 interface methods are not overridden in extension class
            assertEquals(0, body.getMethods().size());
            assertEquals(0, body.getFields().size());
            assertEquals(1, def.getMimics().size());
            assertNotNull(def.getExtendedClass());
            assertEquals(1, body.getConstructors().size());
            // should have a constructor in extension class with the same parameter(s) as in the base class
            final ConstructorDeclaration constructor = body.getConstructors().get(0);
            assertEquals("CursorSpecification", constructor.getName());
            assertEquals(1, constructor.getFormalParameters().size());
            assertEquals("final cursor_specification<T> sqlBuilder", constructor.getFormalParameters().get(0).getImage());
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

}
