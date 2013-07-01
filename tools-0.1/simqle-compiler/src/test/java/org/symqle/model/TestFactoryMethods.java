package org.symqle.model;

import junit.framework.TestCase;
import org.symqle.parser.SymqleParser;
import org.symqle.parser.SyntaxTree;
import org.symqle.processor.ClassDeclarationProcessor;
import org.symqle.processor.ClassEnhancer;
import org.symqle.processor.InheritanceProcessor;
import org.symqle.processor.InterfaceDeclarationsProcessor;
import org.symqle.processor.ProductionDeclarationProcessor;
import org.symqle.processor.SimqleMethodProcessor;
import org.symqle.util.ModelUtils;

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
