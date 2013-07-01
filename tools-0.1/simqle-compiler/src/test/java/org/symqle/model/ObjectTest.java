package org.symqle.model;

import junit.framework.TestCase;
import org.symqle.parser.SymqleParser;
import org.symqle.parser.SyntaxTree;
import org.symqle.processor.ClassDeclarationProcessor;

import java.io.FileReader;

/**
 * @author lvovich
 */
public class ObjectTest extends TestCase {
    public void testJustClass() throws Exception {
        Model model = new Model();
        SymqleParser parser = new SymqleParser(new FileReader("src/test-data/model/Object.sdl"));
        SyntaxTree node = new SyntaxTree(parser.SymqleUnit(), "Object.sdl");
        new ClassDeclarationProcessor().process(node, model);
        System.out.println(model.getClassDef("Object"));
    }

}
