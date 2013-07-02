package org.symqle.model;

import junit.framework.TestCase;
import org.symqle.parser.ParseException;
import org.symqle.parser.SymqleParser;
import org.symqle.parser.SyntaxTree;
import org.symqle.processor.*;
import org.symqle.test.TestUtils;
import org.symqle.util.ModelUtils;

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
public class SymqleMethodProcessorTest extends TestCase {

    public void testList() throws Exception {
        final Model model = ModelUtils.prepareModel();
        String source = "src/test-data/model/SymqleMethod.sdl";
        final SyntaxTree syntaxTree = readSyntaxTree(source);
        new InterfaceDeclarationsProcessor().process(syntaxTree, model);
        new ClassDeclarationProcessor().process(syntaxTree, model);
        new ProductionDeclarationProcessor().process(syntaxTree, model);
        new SymqleMethodProcessor().process(syntaxTree, model);
        final ClassDefinition symqle = model.getClassDef("Symqle");

        final MethodDefinition list = symqle.getMethodBySignature("list(SelectStatement,Database)", model);
        assertEquals(TestUtils.pureCode("public List<T> list(final SelectStatement statement, final Database database) {\n" +
                "    final SqlContext context = new SqlContext();\n" +
                "    return database.list(statement.z$sqlOfSelectStatement(context));\n" +
                "}"), TestUtils.pureCode(list.toString()));

    }

    public void testImplicitConversion() throws Exception {
        final Model model = ModelUtils.prepareModel();
        SyntaxTree tree = readSyntaxTree("src/test-data/model/SmandaloneImplicit.sdl");
        new InterfaceDeclarationsProcessor().process(tree, model);
        new ClassDeclarationProcessor().process(tree, model);
        new ProductionDeclarationProcessor().process(tree, model);
        new ImplicitDeclarationProcessor().process(tree, model);
        System.out.println(model.getClassDef("Symqle"));
    }

    private SyntaxTree readSyntaxTree(String source) throws FileNotFoundException, ParseException {
        Reader reader = new InputStreamReader(new FileInputStream(source));
        SymqleParser parser = new SymqleParser(reader);
        final SyntaxTree syntaxTree = new SyntaxTree(parser.SymqleUnit(), source);
        return syntaxTree;
    }
}
