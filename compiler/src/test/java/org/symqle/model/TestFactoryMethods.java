package org.symqle.model;

import junit.framework.TestCase;
import org.symqle.parser.SymqleParser;
import org.symqle.parser.SyntaxTree;
import org.symqle.processor.FinalizationProcessor;
import org.symqle.test.TestUtils;
import org.symqle.util.ModelUtils;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;

/**
 * @author lvovich
 */
public class TestFactoryMethods extends TestCase {

    public void testSimple() throws Exception {
        final Model model = ModelUtils.prepareModel();
        String source = "src/test-data/model/SimpleFactoryMethod.sdl";
        Reader reader = new InputStreamReader(new FileInputStream(source));
        SymqleParser parser = new SymqleParser(reader);
        final List<SyntaxTree> syntaxTrees = Arrays.asList(new SyntaxTree(parser.SymqleUnit(), source));
        new FinalizationProcessor().process(syntaxTrees, model);
        final ClassDefinition symqle = model.getClassDef("Symqle");
        final MethodDefinition forUpdate = symqle.getDeclaredMethodBySignature("forUpdate(CursorSpecification)");
        assertNotNull(forUpdate);
        assertEquals("AbstractSelectStatement", forUpdate.getResultType().getSimpleName());

        final ClassDefinition abstractSelectStatement = model.getClassDef("AbstractSelectStatement");
        final MethodDefinition show = abstractSelectStatement.getDeclaredMethodBySignature("show()");
        assertEquals(TestUtils.pureCode("public final String show() { return Symqle.show(this); }"),
                TestUtils.pureCode(show.toString()));

    }

}
