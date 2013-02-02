package org.simqle.model;

import junit.framework.TestCase;
import org.simqle.parser.ParseException;
import org.simqle.parser.SimqleParser;
import org.simqle.parser.SyntaxTree;
import org.simqle.processor.*;
import org.simqle.test.TestUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Created by IntelliJ IDEA.
 * User: aizyurov
 * Date: 02.12.2012
 * Time: 18:35:36
 * To change this template use File | Settings | File Templates.
 */
public class SimqleMethodProcessorTest extends TestCase {

    public void testList() throws Exception {
        String source = "src/test-data/model/SimqleMethod.sdl";
        final SyntaxTree syntaxTree = readSyntaxTree(source);
        final Model model = new Model();
        new InterfaceDeclarationsProcessor().process(syntaxTree, model);
        new ClassDeclarationProcessor().process(syntaxTree, model);
        new ProductionDeclarationProcessor().process(syntaxTree, model);
        new SimqleMethodProcessor().process(syntaxTree, model);
        final ClassDefinition simqle = model.getClassDef("Simqle");

        final MethodDefinition list = simqle.getMethodBySignature("list(SelectStatement,Database)", model);
        assertEquals(TestUtils.pureCode("public List<T> list(final SelectStatement statement, final Database database) {\n" +
                "    final SqlContext context = new SqlContext();\n" +
                "    return database.list(statement.z$create$SelectStatement(context));\n" +
                "}"), TestUtils.pureCode(list.toString()));

    }

    public void testImplicitConversion() throws Exception {
        Model model = new Model();
        SyntaxTree tree = readSyntaxTree("src/test-data/model/SmandaloneImplicit.sdl");
        new InterfaceDeclarationsProcessor().process(tree, model);
        new ClassDeclarationProcessor().process(tree, model);
        new ProductionDeclarationProcessor().process(tree, model);
        new ImplicitDeclarationProcessor().process(tree, model);
        System.out.println(model.getClassDef("Simqle"));
    }

    private SyntaxTree readSyntaxTree(String source) throws FileNotFoundException, ParseException {
        Reader reader = new InputStreamReader(new FileInputStream(source));
        SimqleParser parser = new SimqleParser(reader);
        final SyntaxTree syntaxTree = new SyntaxTree(parser.SimqleUnit(), source);
        return syntaxTree;
    }
}
