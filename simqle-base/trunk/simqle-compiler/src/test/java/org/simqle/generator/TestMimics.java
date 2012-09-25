package org.simqle.generator;

import junit.framework.TestCase;
import org.simqle.model.*;
import org.simqle.parser.SimqleParser;
import org.simqle.parser.SyntaxTree;
import org.simqle.processor.ClassDeclarationProcessor;
import org.simqle.processor.InterfaceDeclarationsProcessor;
import org.simqle.processor.MimicsProcessor;
import org.simqle.processor.ProductionDeclarationProcessor;
import org.simqle.test.TestUtils;

import java.io.FileReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

        final List<Type> implementedInterfaces = extension.getImplementedInterfaces();
        assertEquals(1, implementedInterfaces.size());
        final Type interfaceType = implementedInterfaces.get(0);
        assertEquals("expression<Boolean>", interfaceType.getImage());
    }

    public void testCyclic() throws Exception {
        Model model = new Model();
        SimqleParser parser = new SimqleParser(new FileReader("src/test-data/CyclicMimics.sdl"));
        SyntaxTree node = new SyntaxTree(parser.SimqleUnit(), "CyclicMimics.sdl");
        new InterfaceDeclarationsProcessor().process(node, model);
        new ClassDeclarationProcessor().process(node, model);
        new ProductionDeclarationProcessor().process(node, model);
        new MimicsProcessor().process(model);
        assertNotNull(model);
        final ClassPair expression = model.getClassPair("Expression");
        final ClassDefinition expressionBase = expression.getBase();
        final ClassDefinition expressionExtension = expression.getExtension();
        // add should be implemented directly in base
        assertNotNull(expressionBase.getBody().getMethod("add(term)"));
        final MethodDeclaration addMethod = expressionBase.getBody().getMethod("add(term)");
        assertEquals("{ return new Expression<T>(SqlFactory.getInstance().expression_IS_expression_PLUS_term(this, t)); }", TestUtils.normalizeFormatting(addMethod.getMethodBody()));
        final List<Type> expressionInterfaces = expressionExtension.getImplementedInterfaces();
        assertEquals(2, expressionInterfaces.size());
        Set<String> typeImages = new HashSet<String>();
        for (Type t: expressionInterfaces) {
            typeImages.add(t.getImage());
        }
        assertEquals(2, typeImages.size());
        assertTrue(typeImages.contains("term<T>"));
        assertTrue(typeImages.contains("primary<T>"));

        assertNull(expressionExtension.getBody().getMethod("add(term)"));
        // mult should be implemented by delegation
        assertNull(expressionBase.getBody().getMethod("mult(primary)"));
        assertNotNull(expressionExtension.getBody().getMethod("mult(primary)"));
        final MethodDeclaration delegatedMultMethod = expressionExtension.getBody().getMethod("mult(primary)");
        assertEquals("{ return toPrimary().mult(p); }", TestUtils.normalizeFormatting(delegatedMultMethod.getMethodBody()));

        // primary must in turn delegate to Term
        final MethodDeclaration delegatedMult2 = model.getClassPair("Primary").getExtension().getBody().getMethod("mult(primary)");
        assertNotNull(delegatedMult2);
        assertEquals("{ return toTerm().mult(p); }", TestUtils.normalizeFormatting(delegatedMult2.getMethodBody()));

    }

    public void testTypedMimicsUntyped() throws Exception {
        Model model = new Model();
        SimqleParser parser = new SimqleParser(new FileReader("src/test-data/TypedMimicsUntyped.sdl"));
        SyntaxTree node = new SyntaxTree(parser.SimqleUnit(), "TypedMimicsUntyped.sdl");
        new InterfaceDeclarationsProcessor().process(node, model);
        new ClassDeclarationProcessor().process(node, model);
        new ProductionDeclarationProcessor().process(node, model);
        new MimicsProcessor().process(model);
        final ClassDefinition valueClass = model.getClassPair("Value").getExtension();
        final MethodDeclaration createMethod = valueClass.getBody().getMethod("z$create$sort_key(SqlContext)");
        assertNotNull(createMethod);
        assertEquals("Sql", createMethod.getResultType().getImage());
    }

    public void testNoArgMethodInMimics() throws Exception {
        Model model = new Model();
        SimqleParser parser = new SimqleParser(new FileReader("src/test-data/TypedMimicsUntyped.sdl"));
        SyntaxTree node = new SyntaxTree(parser.SimqleUnit(), "TypedMimicsUntyped.sdl");
        new InterfaceDeclarationsProcessor().process(node, model);
        new ClassDeclarationProcessor().process(node, model);
        new ProductionDeclarationProcessor().process(node, model);
        new MimicsProcessor().process(model);
        final ClassDefinition valueClass = model.getClassPair("Value").getExtension();
        final MethodDeclaration noArgMethod = valueClass.getBody().getMethod("noArgMethod()");
        assertNotNull(noArgMethod);
        assertEquals("void", noArgMethod.getResultType().getImage());
        assertEquals("{ toSortKey().noArgMethod(); }", TestUtils.normalizeFormatting(noArgMethod.getMethodBody()));

    }

}
