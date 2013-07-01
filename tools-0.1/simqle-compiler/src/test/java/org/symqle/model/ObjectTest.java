package org.symqle.model;

import junit.framework.TestCase;
import org.symqle.parser.SimqleParser;
import org.symqle.parser.SyntaxTree;
import org.symqle.processor.ClassDeclarationProcessor;

import java.io.FileReader;

/**
 * @author lvovich
 */
public class ObjectTest extends TestCase {
    public void testJustClass() throws Exception {
        Model model = new Model();
        SimqleParser parser = new SimqleParser(new FileReader("src/test-data/model/Object.sdl"));
        SyntaxTree node = new SyntaxTree(parser.SimqleUnit(), "OBject.sdl");
        new ClassDeclarationProcessor().process(node, model);
        System.out.println(model.getClassDef("Object"));
    }

}
