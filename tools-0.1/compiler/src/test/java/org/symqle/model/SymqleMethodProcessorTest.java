package org.symqle.model;

import junit.framework.TestCase;
import org.symqle.parser.ParseException;
import org.symqle.parser.SymqleParser;
import org.symqle.parser.SyntaxTree;
import org.symqle.processor.SymqleMethodProcessor;
import org.symqle.test.TestUtils;
import org.symqle.util.ModelUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
public class SymqleMethodProcessorTest extends TestCase {

    public void testList() throws Exception {
        final Model model = ModelUtils.prepareModel();
        String source = "src/test-data/model/SymqleMethod.sdl";
        final List<SyntaxTree> syntaxTree = Arrays.asList(readSyntaxTree(source));
        new SymqleMethodProcessor().process(syntaxTree, model);
        final ClassDefinition symqle = model.getClassDef("Symqle");
        assertEquals(0, symqle.getAllMethods(model).size());

        final List<MethodDefinition> methods = model.getExplicitSymqleMethods();
        assertEquals(1, methods.size());

        assertEquals(TestUtils.pureCode("static List<T> list(final SelectStatement statement, final Database database) {\n" +
                "    final SqlContext context = new SqlContext();\n" +
                "    return database.list(statement.z$sqlOfSelectStatement(context));\n" +
                "}"), TestUtils.pureCode(methods.get(0).toString()));

    }

    private SyntaxTree readSyntaxTree(String source) throws FileNotFoundException, ParseException {
        Reader reader = new InputStreamReader(new FileInputStream(source));
        SymqleParser parser = new SymqleParser(reader);
        final SyntaxTree syntaxTree = new SyntaxTree(parser.SymqleUnit(), source);
        return syntaxTree;
    }
}
