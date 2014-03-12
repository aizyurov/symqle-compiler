package org.symqle.model;

import junit.framework.TestCase;
import org.symqle.parser.SymqleParser;
import org.symqle.parser.SyntaxTree;
import org.symqle.processor.ClassDeclarationProcessor;
import org.symqle.processor.InheritanceProcessor;
import org.symqle.test.TestUtils;
import org.symqle.util.ModelUtils;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;

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
        final List<SyntaxTree> syntaxTrees = Arrays.asList(new SyntaxTree(parser.SymqleUnit(), source));
        new InheritanceProcessor().process(syntaxTrees, model);
        final ClassDefinition cursorSpec = model.getClassDef("AbstractCursorSpecification");
//        System.out.println(cursorSpec);
        MethodDefinition delegatedMethod = cursorSpec.getDeclaredMethodBySignature("z$sqlOfSelectStatement(SqlContext)");
        assertEquals(TestUtils.pureCode
                ("public final Query<T> z$sqlOfSelectStatement(final SqlContext context) {\n" +
                "                return Symqle.z$SelectStatement$from$CursorSpecification(this)\n" +
                        ".z$sqlOfSelectStatement(context);\n" +
                "            }"), TestUtils.pureCode(delegatedMethod.toString()));

    }

    public void testChain() throws Exception {
        final Model model = ModelUtils.prepareModel();
        String source = "src/test-data/model/ChainInheritance.sdl";
        Reader reader = new InputStreamReader(new FileInputStream(source));
        SymqleParser parser = new SymqleParser(reader);
        final List<SyntaxTree> syntaxTrees = Arrays.asList(new SyntaxTree(parser.SymqleUnit(), source));
        new InheritanceProcessor().process(syntaxTrees, model);
        ClassDefinition queryExpr = model.getClassDef("AbstractQueryExpression");
        System.out.println(queryExpr);
        final MethodDefinition asCursorSpec = queryExpr.getDeclaredMethodBySignature("z$sqlOfCursorSpecification(SqlContext)");
        assertEquals(TestUtils.pureCode(
                "public final Query<T> z$sqlOfCursorSpecification(final SqlContext context) {\n" +
                        "                return Symqle.z$CursorSpecification$from$QueryExpression(this)\n" +
                        ".z$sqlOfCursorSpecification(context);\n" +
                        "            }"
        ), TestUtils.pureCode(asCursorSpec.toString()));
        final MethodDefinition asSelectStatement = queryExpr.getDeclaredMethodBySignature("z$sqlOfSelectStatement(SqlContext)");
        assertEquals(TestUtils.pureCode(
                "public final Query<T> z$sqlOfSelectStatement(final SqlContext context) {\n" +
                        "        return Symqle.z$SelectStatement$from$CursorSpecification(this)\n" +
                        "            .z$sqlOfSelectStatement(context);\n" +
                        "    }"),
                TestUtils.pureCode(asSelectStatement.toString()));
    }

    public void testCyclic() throws Exception {
        final Model model = ModelUtils.prepareModel();
        String source = "src/test-data/model/CyclicInheritance.sdl";
        Reader reader = new InputStreamReader(new FileInputStream(source));
        SymqleParser parser = new SymqleParser(reader);
        final List<SyntaxTree> syntaxTrees = Arrays.asList(new SyntaxTree(parser.SymqleUnit(), source));
        new InheritanceProcessor().process(syntaxTrees, model);
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
        final List<SyntaxTree> syntaxTrees = Arrays.asList(new SyntaxTree(parser.SymqleUnit(), source));
        new InheritanceProcessor().process(syntaxTrees, model);
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
        final List<SyntaxTree> syntaxTrees = Arrays.asList(new SyntaxTree(parser.SymqleUnit(), source));
        new ClassDeclarationProcessor().process(syntaxTrees, model);
        for (ClassDefinition classDef: model.getAllClasses()) {
            System.out.println(classDef);
            System.out.println("==============");
        }

    }

    public void testAllAncestorsForClass() throws Exception {
        final Model model = ModelUtils.prepareModel();
        String source = "src/test-data/model/ClassHierarchy.sdl";
        Reader reader = new InputStreamReader(new FileInputStream(source));
        SymqleParser parser = new SymqleParser(reader);
        final List<SyntaxTree> syntaxTrees = Arrays.asList(new SyntaxTree(parser.SymqleUnit(), source));
        new ClassDeclarationProcessor().process(syntaxTrees, model);
        for (AbstractTypeDefinition classDef : model.getAllTypes()) {
            System.out.println(classDef.getName() + classDef.getTypeParameters() + " <- " + classDef.getAllAncestors(model));
        }

    }
}