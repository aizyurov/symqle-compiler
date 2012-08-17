package org.simqle.generator;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.simqle.model.ClassDefinition;
import org.simqle.model.Model;
import org.simqle.parser.SimqleParser;
import org.simqle.parser.SyntaxTree;
import org.simqle.processor.ClassDeclarationProcessor;
import org.simqle.processor.InterfaceDeclarationsProcessor;
import org.simqle.processor.Processor;

import java.io.FileReader;

/**
 * Created by IntelliJ IDEA.
 * User: lvovich
 * Date: 26.06.12
 * Time: 12:24
 * To change this template use File | Settings | File Templates.
 */
public class TestTypeParameters extends TestCase {

    public void testParsing() throws Exception {
        Model model = new Model();
            SimqleParser parser = new SimqleParser(new FileReader("src/test-data/TypeParametersTest.sdl"));
            SyntaxTree node = new SyntaxTree(parser.SimqleUnit(), "TypeParametersTest.sdl");
        {
            Processor processor = new InterfaceDeclarationsProcessor();
            processor.process(node, model);
        }
        {
            Processor processor = new ClassDeclarationProcessor();
            processor.process(node, model);
        }
        Assert.assertEquals(2, model.getAllClasses().size());
        {
            final ClassDefinition classDef = model.getAllClasses().get(0).getBase();
            Assert.assertEquals(3, classDef.getBody().getMethods().size());
            Assert.assertEquals("public V value(final Element element)", classDef.getBody().getMethods().get(0).getSignature().trim());
            Assert.assertEquals("public Query<V> z$create$expression(final SqlContext context)", classDef.getBody().getMethods().get(1).getSignature().trim());
            Assert.assertEquals("public void z$prepare$expression(final SqlContext context)", classDef.getBody().getMethods().get(2).getSignature().trim());
            Assert.assertEquals(1, classDef.getBody().getFields().size());
            Assert.assertEquals("private final expression<V> sqlBuilder;", classDef.getBody().getFields().get(0).getImage().trim());
        }
        {
            final ClassDefinition classDef = model.getAllClasses().get(1).getBase();
            Assert.assertEquals(3, classDef.getBody().getMethods().size());
            Assert.assertEquals("public Boolean value(final Element element)", classDef.getBody().getMethods().get(0).getSignature().trim());
            Assert.assertEquals("public Query<Boolean> z$create$expression(final SqlContext context)", classDef.getBody().getMethods().get(1).getSignature().trim());
            Assert.assertEquals("public void z$prepare$expression(final SqlContext context)", classDef.getBody().getMethods().get(2).getSignature().trim());
            Assert.assertEquals(1, classDef.getBody().getFields().size());
            Assert.assertEquals("private final expression<Boolean> sqlBuilder;", classDef.getBody().getFields().get(0).getImage().trim());
        }

    }
}
