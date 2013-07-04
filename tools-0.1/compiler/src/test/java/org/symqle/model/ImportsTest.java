package org.symqle.model;

import junit.framework.TestCase;
import org.symqle.parser.SymqleParser;
import org.symqle.parser.SyntaxTree;
import org.symqle.processor.ClassDeclarationProcessor;
import org.symqle.processor.ClassEnhancer;
import org.symqle.processor.InheritanceProcessor;
import org.symqle.processor.InterfaceDeclarationsProcessor;
import org.symqle.processor.InterfaceJavadocProcessor;
import org.symqle.processor.ProductionDeclarationProcessor;
import org.symqle.processor.SymqleMethodProcessor;
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
public class ImportsTest extends TestCase {

    public void testImports() throws Exception {
        final Model model = ModelUtils.prepareModel();
        String source = "src/test-data/model/Imports.sdl";
        Reader reader = new InputStreamReader(new FileInputStream(source));
        SymqleParser parser = new SymqleParser(reader);
        final SyntaxTree syntaxTree = new SyntaxTree(parser.SymqleUnit(), source);
        new InterfaceDeclarationsProcessor().process(syntaxTree, model);
        new ClassDeclarationProcessor().process(syntaxTree, model);
        new ProductionDeclarationProcessor().process(syntaxTree, model);
        new SymqleMethodProcessor().process(syntaxTree, model);
        new InheritanceProcessor().process(model);
        new ClassEnhancer().process(model);
        new InterfaceJavadocProcessor().process(model);
        final ClassDefinition cursorSpec = model.getClassDef("AbstractCursorSpecification");
        System.out.println("===");
        System.out.println(cursorSpec);
        System.out.println("===");
        final ClassDefinition selectStatement = model.getClassDef("AbstractSelectStatement");
        System.out.println(selectStatement);
        System.out.println("===");
        final ClassDefinition symqle = model.getClassDef("Symqle");
        System.out.println(symqle);
        System.out.println(model.getInterface("SelectStatement"));
//        MethodDefinition delegatedMethod = cursorSpec.getDeclaredMethodBySignature("z$sqlOfzSelectStatement(SqlContext)");
//        assertEquals(TestUtils.pureCode
//                ("public final Query<T> z$sqlOfzSelectStatement(final SqlContext context) {\n" +
//                "                return Symqle.get()\n" +
//                "                    .z$zSelectStatement$from$zCursorSpecification(this)\n" +
//                "                    .z$sqlOfzSelectStatement(context);\n" +
//                "            }"), TestUtils.pureCode(delegatedMethod.toString()));

    }

}