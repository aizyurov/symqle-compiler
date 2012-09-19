package org.simqle.generator;

import junit.framework.TestCase;
import org.simqle.model.*;
import org.simqle.parser.SimqleParser;
import org.simqle.parser.SyntaxTree;
import org.simqle.processor.ClassDeclarationProcessor;
import org.simqle.processor.InterfaceDeclarationsProcessor;
import org.simqle.processor.MimicsProcessor;
import org.simqle.processor.ProductionDeclarationProcessor;

import java.io.FileReader;

/**
 * Created by IntelliJ IDEA.
 * User: lvovich
 * Date: 19.09.12
 * Time: 12:30
 * To change this template use File | Settings | File Templates.
 */
public class TestMimics extends TestCase {

    public void testSimple() throws Exception {
        Model model = new Model();
        SimqleParser parser = new SimqleParser(new FileReader("src/test-data/SimpleMimics.sdl"));
        SyntaxTree node = new SyntaxTree(parser.SimqleUnit(), "SimpleMimics.sdl");
        new InterfaceDeclarationsProcessor().process(node, model);
        new ClassDeclarationProcessor().process(node, model);
        new ProductionDeclarationProcessor().process(node, model);
        new MimicsProcessor().process(model);
        final ClassPair booleanExpression = model.getClassPair("BooleanExpression");
        final ClassDefinition extension = booleanExpression.getExtension();
        final ClassDefinition base = booleanExpression.getBase();
        final MethodDeclaration toExpression = base.getBody().getMethod("toExpression()");
        assertNotNull(toExpression);
        assertEquals("protected", toExpression.getAccessModifier());
        assertEquals("", toExpression.getThrowsClause());
        assertEquals("Expression<Boolean>", toExpression.getResultType().getImage());
        final MethodDeclaration createExpressionMethod = extension.getBody().getMethod("z$create$expression(SqlContext)");
        assertNotNull(createExpressionMethod);
        assertEquals("public", createExpressionMethod.getAccessModifier());
        assertEquals("", createExpressionMethod.getThrowsClause());
        assertEquals("{ return toExpression().z$create$expression(context); }", createExpressionMethod.getMethodBody());
        final Type resultType = createExpressionMethod.getResultType();
        assertEquals("Sql", resultType.getImage());

        final MethodDeclaration overriddenValueMethod = extension.getBody().getMethod("value(Element)");
        assertNull(overriddenValueMethod);

        final MethodDeclaration valueMethod = base.getBody().getMethod("value(Element)");
        assertNotNull(valueMethod);

        final MethodDeclaration toStringMethod = extension.getBody().getMethod("toString()");
        assertNotNull(toStringMethod);

        final MethodDeclaration staticMethod = extension.getBody().getMethod("createEmptyString()");
        assertNull(staticMethod);

        final MethodDeclaration privateMethod = extension.getBody().getMethod("hash(Integer)");
        assertNull(privateMethod);
    }

}
