package org.simqle.model;

import junit.framework.TestCase;
import org.simqle.parser.SimqleParser;
import org.simqle.parser.SyntaxTree;
import org.simqle.processor.InterfaceDeclarationsProcessor;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Created by IntelliJ IDEA.
 * User: lvovich
 * Date: 26.11.12
 * Time: 19:58
 * To change this template use File | Settings | File Templates.
 */
public class TestInterfaceParsing extends TestCase {

    public void testScalar() throws Exception {
        String source = "src/test-data/model/Scalar.sdl";
        Reader reader = new InputStreamReader(new FileInputStream(source));
        SimqleParser parser = new SimqleParser(reader);
        final SyntaxTree syntaxTree = new SyntaxTree(parser.SimqleUnit(), source);
        Model model = new Model();
        new InterfaceDeclarationsProcessor().process(syntaxTree, model);
        for (InterfaceDefinition def: model.getAllInterfaces()) {
            System.out.println("================");
            System.out.println(def);
            System.out.println("================");
        }
    }
}
