package org.simqle.model;

import junit.framework.TestCase;
import org.simqle.parser.SimqleParser;
import org.simqle.parser.SyntaxTree;
import org.simqle.processor.ClassDeclarationProcessor;
import org.simqle.processor.InterfaceDeclarationsProcessor;
import org.simqle.processor.ProductionDeclarationProcessor;
import org.simqle.processor.SimqleMethodProcessor;
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
public class SimqleMethodProcessorTest extends TestCase {

    public void testList() throws Exception {
        String source = "src/test-data/model/SimqleMethod.sdl";
        Reader reader = new InputStreamReader(new FileInputStream(source));
        SimqleParser parser = new SimqleParser(reader);
        final SyntaxTree syntaxTree = new SyntaxTree(parser.SimqleUnit(), source);
        final Model model = new Model();
        new InterfaceDeclarationsProcessor().process(syntaxTree, model);
        new ClassDeclarationProcessor().process(syntaxTree, model);
        new ProductionDeclarationProcessor().process(syntaxTree, model);
        new SimqleMethodProcessor().process(syntaxTree, model);
        final ClassDefinition simqle = model.getClassDef("Simqle");
        final ClassDefinition simqleGeneric = model.getClassDef("SimqleGeneric");

        final MethodDefinition listDecl = simqle.getMethodBySignature("list(SelectStatement,Database)", model);
        assertEquals("public abstract List<T> list(final SelectStatement statement, final Database database)", listDecl.declaration());
        assertEquals("public abstract List<T> list(final SelectStatement statement, final Database database);", TestUtils.pureCode(listDecl.toString()));

        final MethodDefinition list = simqleGeneric.getMethodBySignature("list(SelectStatement,Database)", model);
        assertEquals("public List<T> list(final SelectStatement statement, final Database database)", list.declaration());
        assertEquals(TestUtils.pureCode("public List<T> list(final SelectStatement statement, final Database database) {\n" +
                "    final SqlContext context = new SqlContext();\n" +
                "    return database.list(statement.z$create$SelectStatement(context));\n" +
                "}"), TestUtils.pureCode(list.toString()));
    }
}
