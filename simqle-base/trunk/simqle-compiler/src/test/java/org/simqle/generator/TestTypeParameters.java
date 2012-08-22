package org.simqle.generator;

import junit.framework.TestCase;
import org.simqle.model.ClassDefinition;
import org.simqle.model.MethodDeclaration;
import org.simqle.model.Model;
import org.simqle.parser.SimqleParser;
import org.simqle.parser.SyntaxTree;
import org.simqle.processor.ClassDeclarationProcessor;
import org.simqle.processor.InterfaceDeclarationsProcessor;
import org.simqle.processor.Processor;

import java.io.FileReader;

/**
 * Created by IntelliJ IDEA.
 * User: lvovich
 * Date: 26.06.12
 * Time: 12:24
 * To change this template use File | Settings | File Templates.
 */
public class TestTypeParameters extends TestCase {

    public void testParsing() throws Exception {
        Model model = new Model();
            SimqleParser parser = new SimqleParser(new FileReader("src/test-data/TypeParametersTest.sdl"));
            SyntaxTree node = new SyntaxTree(parser.SimqleUnit(), "TypeParametersTest.sdl");
        {
            Processor processor = new InterfaceDeclarationsProcessor();
            processor.process(node, model);
        }
        {
            Processor processor = new ClassDeclarationProcessor();
            processor.process(node, model);
        }
        assertEquals(2, model.getAllClasses().size());
        {
            final ClassDefinition classDef = model.getClassPair("GenericExpression").getBase();
            assertEquals(3, classDef.getBody().getMethods().size());

            {
                final MethodDeclaration valueMethod = classDef.getBody().getMethod("value");
                assertNotNull(valueMethod);
                assertEquals("V", valueMethod.getResultType().getImage());
                assertEquals(1, valueMethod.getFormalParameters().size());
                assertEquals("final Element element", valueMethod.getFormalParameters().get(0).getImage());
            }

            {
                final MethodDeclaration createMethod = classDef.getBody().getMethod("z$create$expression");
                assertNotNull(createMethod);
                assertEquals("Query<V>", createMethod.getResultType().getImage());
                assertEquals(1, createMethod.getFormalParameters().size());
                assertEquals("final SqlContext context", createMethod.getFormalParameters().get(0).getImage());
            }

            {
                final MethodDeclaration prepareMethod = classDef.getBody().getMethod("z$prepare$expression");
                assertNotNull(prepareMethod);
                assertNull("void", prepareMethod.getResultType());
                assertEquals(1, prepareMethod.getFormalParameters().size());
                assertEquals("final SqlContext context", prepareMethod.getFormalParameters().get(0).getImage());
            }

            assertEquals(1, classDef.getBody().getFields().size());
            assertEquals("private final expression<V> sqlBuilder;", classDef.getBody().getFields().get(0).getImage().trim());
        }
        {
            final ClassDefinition classDef = model.getClassPair("BooleanExpression").getBase();
            assertEquals(3, classDef.getBody().getMethods().size());
            {
                final MethodDeclaration valueMethod = classDef.getBody().getMethod("value");
                assertNotNull(valueMethod);
                assertEquals("Boolean", valueMethod.getResultType().getImage());
                assertEquals(1, valueMethod.getFormalParameters().size());
                assertEquals("final Element element", valueMethod.getFormalParameters().get(0).getImage());
            }

            {
                final MethodDeclaration createMethod = classDef.getBody().getMethod("z$create$expression");
                assertNotNull(createMethod);
                assertEquals("Query<Boolean>", createMethod.getResultType().getImage());
                assertEquals(1, createMethod.getFormalParameters().size());
                assertEquals("final SqlContext context", createMethod.getFormalParameters().get(0).getImage());
            }

            {
                final MethodDeclaration prepareMethod = classDef.getBody().getMethod("z$prepare$expression");
                assertNotNull(prepareMethod);
                assertNull(prepareMethod.getResultType());
                assertEquals(1, prepareMethod.getFormalParameters().size());
                assertEquals("final SqlContext context", prepareMethod.getFormalParameters().get(0).getImage());
            }
            assertEquals(1, classDef.getBody().getFields().size());
            assertEquals("private final expression<Boolean> sqlBuilder;", classDef.getBody().getFields().get(0).getImage().trim());
        }

    }
}
