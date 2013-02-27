package org.simqle.model;

import junit.framework.TestCase;
import org.simqle.parser.SimqleParser;
import org.simqle.parser.SyntaxTree;
import org.simqle.processor.ClassDeclarationProcessor;
import org.simqle.processor.ClassEnhancer;
import org.simqle.processor.InheritanceProcessor;
import org.simqle.processor.InterfaceDeclarationsProcessor;
import org.simqle.processor.ProductionDeclarationProcessor;
import org.simqle.processor.SimqleMethodProcessor;
import org.simqle.util.ModelUtils;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * @author lvovich
 */
public class TestFactoryMethods extends TestCase {

    public void testSimple() throws Exception {
        final Model model = ModelUtils.prepareModel();
        String source = "src/test-data/model/SimpleFactoryMethod.sdl";
        Reader reader = new InputStreamReader(new FileInputStream(source));
        SimqleParser parser = new SimqleParser(reader);
        final SyntaxTree syntaxTree = new SyntaxTree(parser.SimqleUnit(), source);
        new InterfaceDeclarationsProcessor().process(syntaxTree, model);
        new ClassDeclarationProcessor().process(syntaxTree, model);
        new ProductionDeclarationProcessor().process(syntaxTree, model);
        new SimqleMethodProcessor().process(syntaxTree, model);
        new InheritanceProcessor().process(model);
        new ClassEnhancer().process(model);
        for (ClassDefinition classDef : model.getAllClasses()) {
            System.out.println(classDef);
        }

    }

}
