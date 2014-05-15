package org.symqle.model;

import junit.framework.TestCase;
import org.symqle.parser.SymqleParser;
import org.symqle.parser.SyntaxTree;
import org.symqle.processor.ClassDeclarationProcessor;

import java.io.FileReader;
import java.util.Arrays;

/**
 * @author lvovich
 */
public class ObjectTest extends TestCase {
    public void testObjectClass() throws Exception {
        Model model = new Model();
        SymqleParser parser = new SymqleParser(new FileReader("src/test-data/model/Object.sdl"));
        SyntaxTree node = new SyntaxTree(parser.SymqleUnit(), "Object.sdl");
        new ClassDeclarationProcessor().process(Arrays.asList(node), model);

        final ClassDefinition object = model.getClassDef("Object");
        assertNotNull(object);
        assertNotNull(object.getMethodBySignature("equals(Object)", model));
        assertNotNull(object.getDeclaredMethodBySignature("equals(Object)"));
    }

}
