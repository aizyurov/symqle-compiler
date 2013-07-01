package org.symqle.model;

import junit.framework.TestCase;
import org.symqle.parser.SymqleParser;
import org.symqle.parser.SyntaxTree;
import org.symqle.processor.ClassDeclarationProcessor;
import org.symqle.processor.InheritanceProcessor;
import org.symqle.processor.InterfaceDeclarationsProcessor;
import org.symqle.processor.ProductionDeclarationProcessor;
import org.symqle.processor.SymqleMethodProcessor;
import org.symqle.test.TestUtils;
import org.symqle.util.ModelUtils;

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
        final Model model = ModelUtils.prepareModel();
        String source = "src/test-data/model/SimpleInheritance.sdl";
        Reader reader = new InputStreamReader(new FileInputStream(source));
        SymqleParser parser = new SymqleParser(reader);
        final SyntaxTree syntaxTree = new SyntaxTree(parser.SymqleUnit(), source);
        new InterfaceDeclarationsProcessor().process(syntaxTree, model);
        new ClassDeclarationProcessor().process(syntaxTree, model);
        new ProductionDeclarationProcessor().process(syntaxTree, model);
        new SymqleMethodProcessor().process(syntaxTree, model);
        new InheritanceProcessor().process(model);
        final ClassDefinition cursorSpec = model.getClassDef("CursorSpecification");
//        System.out.println(cursorSpec);
        MethodDefinition delegatedMethod = cursorSpec.getDeclaredMethodBySignature("z$sqlOfzSelectStatement(SqlContext)");
        assertEquals(TestUtils.pureCode
                ("public final Query<T> z$sqlOfzSelectStatement(final SqlContext context) {\n" +
                "                return Symqle.get()\n" +
                "                    .z$zSelectStatement$from$zCursorSpecification(this)\n" +
                "                    .z$sqlOfzSelectStatement(context);\n" +
                "            }"), TestUtils.pureCode(delegatedMethod.toString()));

    }

    public void testChain() throws Exception {
        final Model model = ModelUtils.prepareModel();
        String source = "src/test-data/model/ChainInheritance.sdl";
        Reader reader = new InputStreamReader(new FileInputStream(source));
        SymqleParser parser = new SymqleParser(reader);
        final SyntaxTree syntaxTree = new SyntaxTree(parser.SymqleUnit(), source);
        new InterfaceDeclarationsProcessor().process(syntaxTree, model);
        new ClassDeclarationProcessor().process(syntaxTree, model);
        new ProductionDeclarationProcessor().process(syntaxTree, model);
        new SymqleMethodProcessor().process(syntaxTree, model);
        new InheritanceProcessor().process(model);
        ClassDefinition queryExpr = model.getClassDef("QueryExpression");
        System.out.println(queryExpr);
        final MethodDefinition asCursorSpec = queryExpr.getDeclaredMethodBySignature("z$sqlOfzCursorSpecification(SqlContext)");
        assertEquals(TestUtils.pureCode(
                "public final Query<T> z$sqlOfzCursorSpecification(final SqlContext context) {\n" +
                        "                return Symqle.get()\n" +
                        "                    .z$zCursorSpecification$from$zQueryExpression(this)\n" +
                        "                    .z$sqlOfzCursorSpecification(context);\n" +
                        "            }"
        ), TestUtils.pureCode(asCursorSpec.toString()));
        final MethodDefinition asSelectStatement = queryExpr.getDeclaredMethodBySignature("z$sqlOfzSelectStatement(SqlContext)");
        assertEquals(TestUtils.pureCode(
                "public final Query<T> z$sqlOfzSelectStatement(final SqlContext context) {\n" +
                        "                return Symqle.get()\n" +
                        "                    .z$zSelectStatement$from$zCursorSpecification(this)\n" +
                        "                    .z$sqlOfzSelectStatement(context);\n" +
                        "            }"),
                TestUtils.pureCode(asSelectStatement.toString()));
    }

    public void testCyclic() throws Exception {
        final Model model = ModelUtils.prepareModel();
        String source = "src/test-data/model/CyclicInheritance.sdl";
        Reader reader = new InputStreamReader(new FileInputStream(source));
        SymqleParser parser = new SymqleParser(reader);
        final SyntaxTree syntaxTree = new SyntaxTree(parser.SymqleUnit(), source);
        new InterfaceDeclarationsProcessor().process(syntaxTree, model);
        new ClassDeclarationProcessor().process(syntaxTree, model);
        new ProductionDeclarationProcessor().process(syntaxTree, model);
        new SymqleMethodProcessor().process(syntaxTree, model);
        new InheritanceProcessor().process(model);
        for (ClassDefinition classDef: model.getAllClasses()) {
            System.out.println(classDef);
            System.out.println("==============");
        }

    }

    public void testGenerics() throws Exception {
        final Model model = ModelUtils.prepareModel();
        String source = "src/test-data/model/GenericsInImplicits.sdl";
        Reader reader = new InputStreamReader(new FileInputStream(source));
        SymqleParser parser = new SymqleParser(reader);
        final SyntaxTree syntaxTree = new SyntaxTree(parser.SymqleUnit(), source);
        new InterfaceDeclarationsProcessor().process(syntaxTree, model);
        new ClassDeclarationProcessor().process(syntaxTree, model);
        new ProductionDeclarationProcessor().process(syntaxTree, model);
        new SymqleMethodProcessor().process(syntaxTree, model);
        new InheritanceProcessor().process(model);
        for (ClassDefinition classDef: model.getAllClasses()) {
            System.out.println(classDef);
            System.out.println("==============");
        }

    }

    public void testDependentInterface() throws Exception {
        final Model model = ModelUtils.prepareModel();
        String source = "src/test-data/model/DependsPropagation.sdl";
        Reader reader = new InputStreamReader(new FileInputStream(source));
        SymqleParser parser = new SymqleParser(reader);
        final SyntaxTree syntaxTree = new SyntaxTree(parser.SymqleUnit(), source);
        new InterfaceDeclarationsProcessor().process(syntaxTree, model);
        new ClassDeclarationProcessor().process(syntaxTree, model);
        for (ClassDefinition classDef: model.getAllClasses()) {
            System.out.println(classDef);
            System.out.println("==============");
        }

    }
}