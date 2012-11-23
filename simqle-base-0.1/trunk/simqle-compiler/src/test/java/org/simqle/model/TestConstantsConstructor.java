package org.simqle.model;

import junit.framework.TestCase;

/**
 * Created by IntelliJ IDEA.
 * User: lvovich
 * Date: 23.08.12
 * Time: 18:43
 * To change this template use File | Settings | File Templates.
 */
public class TestConstantsConstructor extends TestCase {

    public void testConstructor() {
        try {
            new Constants();
            fail("RuntimeException expected");
        } catch (RuntimeException e) {
            // expected
        }
    }
}
