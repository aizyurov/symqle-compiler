/*
* Copyright Alexander Izyurov 2010
*/
package org.simqle.generator;

import junit.framework.TestCase;
import org.simqle.model.MethodDeclaration;
import org.simqle.processor.InterfaceDeclarationsProcessor;
import org.simqle.test.TestUtils;

/**
 * <br/>13.11.2011                                                         
 *
 * @author Alexander Izyurov
 */
public class TestScalarMethod extends TestCase {
    public void test() throws Exception {
        final MethodDeclaration methodDeclaration = InterfaceDeclarationsProcessor.makeScalarMethod("T", "SelectSublist");
        assertEquals("value", methodDeclaration.getName());
        assertEquals("T", methodDeclaration.getResultType().getNameChain().get(0).getName());
        assertEquals(1, methodDeclaration.getFormalParameters().size());
        assertEquals("final Element element", methodDeclaration.getFormalParameters().get(0).getImage());
        final String expectedComment="        /**\n" +
                "        * Converts data from row element to Java object of type T\n" +
                "        * @param element row element containing the data\n" +
                "        * @return object of type T, may be null\n" +
                "        */";
        assertEquals(TestUtils.normalizeFormatting(expectedComment), TestUtils.normalizeFormatting(methodDeclaration.getComment()));
    }
}
