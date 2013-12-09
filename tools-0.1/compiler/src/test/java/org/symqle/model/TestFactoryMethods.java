package org.symqle.model;

import junit.framework.TestCase;
import org.symqle.parser.SymqleParser;
import org.symqle.parser.SyntaxTree;
import org.symqle.processor.*;
import org.symqle.processor.ProductionProcessor;
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
        SymqleParser parser = new SymqleParser(reader);
        final SyntaxTree syntaxTree = new SyntaxTree(parser.SymqleUnit(), source);
        new InterfaceDeclarationsProcessor().process(syntaxTree, model);
        new ClassDeclarationProcessor().process(syntaxTree, model);
        new ProductionProcessor().process(syntaxTree, model);
        new SymqleMethodProcessor().process(syntaxTree, model);
        new InheritanceProcessor().process(model);
        new ClassEnhancer().process(model);
        for (ClassDefinition classDef : model.getAllClasses()) {
            System.out.println(classDef);
        }

    }

}
