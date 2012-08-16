/*
* Copyright Alexander Izyurov 2010
*/
package org.simqle.generator;

import junit.framework.TestCase;
import org.simqle.model.MethodDeclaration;
import org.simqle.model.Model;
import org.simqle.processor.InterfaceDeclarationsProcessor;

/**
 * <br/>13.11.2011
 *
 * @author Alexander Izyurov
 */
public class TestScalarMethod extends TestCase {
    public void test() throws Exception {
        Model model = new Model();
        final MethodDeclaration methodDeclaration = InterfaceDeclarationsProcessor.makeScalarMethod("T", "SelectSublist");
        System.out.println(methodDeclaration.getName());
        System.out.println(methodDeclaration.isInterfaceMethod());
        System.out.println(methodDeclaration.getSignature());
        System.out.println(methodDeclaration.getComment());
    }
}
