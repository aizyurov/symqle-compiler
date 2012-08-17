/*
* Copyright Alexander Izyurov 2010
*/
package org.simqle.generator;

import junit.framework.TestCase;
import org.simqle.model.MethodDeclaration;
import org.simqle.processor.InterfaceDeclarationsProcessor;

/**
 * <br/>13.11.2011
 *
 * @author Alexander Izyurov
 */
public class TestScalarMethod extends TestCase {
    public void test() throws Exception {
        final MethodDeclaration methodDeclaration = InterfaceDeclarationsProcessor.makeScalarMethod("T", "SelectSublist");
        assertEquals("value", methodDeclaration.getName());
        assertTrue(methodDeclaration.isInterfaceMethod());
        assertEquals("T value(Element element)", methodDeclaration.getSignature());
        final String expectedComment="        /**\n" +
                "        * Converts data from row element to Java object of type T\n" +
                "        * @param element row element containing the data\n" +
                "        * @return object of type T, may be null\n" +
                "        */";
        assertEquals(expectedComment.trim(), methodDeclaration.getComment().trim());
    }
}
