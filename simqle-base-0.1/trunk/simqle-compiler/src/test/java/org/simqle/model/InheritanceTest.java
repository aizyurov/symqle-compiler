package org.simqle.model;

import junit.framework.TestCase;
import org.simqle.parser.SimqleParser;
import org.simqle.parser.SyntaxTree;
import org.simqle.processor.*;
import org.simqle.test.TestUtils;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Created by IntelliJ IDEA.
 * User: aizyurov
 * Date: 02.12.2012
 * Time: 18:35:36
 * To change this template use File | Settings | File Templates.
 */
public class InheritanceTest extends TestCase {

    public void testSimple() throws Exception {
        String source = "src/test-data/model/SimpleInheritance.sdl";
        Reader reader = new InputStreamReader(new FileInputStream(source));
        SimqleParser parser = new SimqleParser(reader);
        final SyntaxTree syntaxTree = new SyntaxTree(parser.SimqleUnit(), source);
        final Model model = new Model();
        new InterfaceDeclarationsProcessor().process(syntaxTree, model);
        new ClassDeclarationProcessor().process(syntaxTree, model);
        new ProductionDeclarationProcessor().process(syntaxTree, model);
        new SimqleMethodProcessor().process(syntaxTree, model);
        new InheritanceProcessor().process(model);
        final ClassDefinition cursorSpec = model.getClassDef("CursorSpecification");
//        System.out.println(cursorSpec);
        MethodDefinition delegatedMethod = cursorSpec.getDeclaredMethodBySignature("z$create$zSelectStatement(SqlContext)");
        assertEquals(TestUtils.pureCode
                ("public Query<T> z$create$zSelectStatement(final SqlContext context) {\n" +
                "                return Simqle.get()\n" +
                "                    .z$zSelectStatement$from$zCursorSpecification(this)\n" +
                "                    .z$create$zSelectStatement(context);\n" +
                "            }"), TestUtils.pureCode(delegatedMethod.toString()));

    }

    public void testChain() throws Exception {
        String source = "src/test-data/model/ChainInheritance.sdl";
        Reader reader = new InputStreamReader(new FileInputStream(source));
        SimqleParser parser = new SimqleParser(reader);
        final SyntaxTree syntaxTree = new SyntaxTree(parser.SimqleUnit(), source);
        final Model model = new Model();
        new InterfaceDeclarationsProcessor().process(syntaxTree, model);
        new ClassDeclarationProcessor().process(syntaxTree, model);
        new ProductionDeclarationProcessor().process(syntaxTree, model);
        new SimqleMethodProcessor().process(syntaxTree, model);
        new InheritanceProcessor().process(model);
        ClassDefinition queryExpr = model.getClassDef("QueryExpression");
        System.out.println(queryExpr);
    }
}