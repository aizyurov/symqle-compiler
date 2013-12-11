package org.symqle.model;

import junit.framework.TestCase;
import org.symqle.parser.SymqleParser;
import org.symqle.parser.SyntaxTree;
import org.symqle.processor.ClassEnhancer;
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
        new ClassEnhancer().process(syntaxTrees, model);
        for (AbstractTypeDefinition classDef : model.getAllTypes()) {
            System.out.println(classDef);
        }

    }

}
