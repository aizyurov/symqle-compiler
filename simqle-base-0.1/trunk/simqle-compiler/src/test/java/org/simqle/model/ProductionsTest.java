package org.simqle.model;

import junit.framework.TestCase;
import org.simqle.parser.SimqleParser;
import org.simqle.parser.SyntaxTree;
import org.simqle.processor.ClassDeclarationProcessor;
import org.simqle.processor.InterfaceDeclarationsProcessor;
import org.simqle.processor.ProductionDeclarationProcessor;
import org.simqle.util.Utils;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * @author lvovich
 */
public class ProductionsTest extends TestCase {

    public void testBasicProduction() throws Exception {
        String source = "src/test-data/model/Productions.sdl";
        Reader reader = new InputStreamReader(new FileInputStream(source));
        SimqleParser parser = new SimqleParser(reader);
        final SyntaxTree syntaxTree = new SyntaxTree(parser.SimqleUnit(), source);
        final Model model = new Model();
        new InterfaceDeclarationsProcessor().process(syntaxTree, model);
        new ClassDeclarationProcessor().process(syntaxTree, model);
        new ProductionDeclarationProcessor().process(syntaxTree, model);

        final ClassDefinition simqle = model.getClassDef("Simqle");
        System.out.println(simqle);
        System.out.println("==========");
        final ClassDefinition simqleGeneric = model.getClassDef("SimqleGeneric");
        System.out.println(simqleGeneric);

        // make sure that classes are compilable
        Utils.createParser(simqle.toString()).NormalClassDeclaration();
        Utils.createParser(simqleGeneric.toString()).NormalClassDeclaration();

        //
        assertEquals(3, simqle.getDeclaredMethods().size());
        assertEquals(3, simqle.getDeclaredMethods().size());
        for (MethodDefinition method: simqle.getDeclaredMethods()) {
            assertTrue(method.getOtherModifiers().toString(), method.getOtherModifiers().contains("abstract"));
            assertEquals("public", method.getAccessModifier());
        }
        {
            final MethodDefinition method = simqle.getDeclaredMethodBySignature("asSelectStatement(zCursorSpecification)");
            assertEquals("public abstract <T> zSelectStatement<T> asSelectStatement(zCursorSpecification<T> cspec);",
                    method.toString());
        }
        {
            final MethodDefinition method = simqle.getDeclaredMethodBySignature("forReadOnly(zCursorSpecification)");
            assertEquals("public abstract <T> SelectStatement<T> forReadOnly(zCursorSpecification<T> cspec);",
                    method.toString());
        }
    }
}
