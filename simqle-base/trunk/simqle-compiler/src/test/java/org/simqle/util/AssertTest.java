package org.simqle.util;

import junit.framework.TestCase;

/**
 * Created by IntelliJ IDEA.
 * User: lvovich
 * Date: 22.08.12
 * Time: 15:32
 * To change this template use File | Settings | File Templates.
 */
public class AssertTest extends TestCase {
    public void testEquals() {
        // constructing equal but not same strings for test
        String expected = "expected";
        StringBuilder actualBuilder = new StringBuilder();
        actualBuilder.append(expected);
        final String actual = actualBuilder.toString();
        assertFalse(expected == actual);
        assertEquals(expected, actual);
        Assert.assertOneOf(new RuntimeException("test failed"), actual, expected);
    }

    public void testNullsAreEqual() {
        Assert.assertOneOf(new RuntimeException("test failed"), null, (String) null);
    }

    public void testNotEqual() {
        try {
            Assert.assertOneOf(new IllegalArgumentException("actual not in [expected]"), "actual", "expected");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            assertEquals("actual not in [expected]", e.getMessage());
        }
    }

    public void testNullIsNotEqual() {
        try {
            Assert.assertOneOf(new IllegalArgumentException("null not in [expected]"), null, "expected");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            assertEquals("null not in [expected]", e.getMessage());
        }

        try {
            Assert.assertOneOf(new IllegalArgumentException("actual not in [null]"), "actual", (String) null);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            assertEquals("actual not in [null]", e.getMessage());
        }
    }

    public void testMultipleChoices() {
        String expected = "expected";
        StringBuilder actualBuilder = new StringBuilder();
        actualBuilder.append(expected);
        final String actual = actualBuilder.toString();
        assertFalse(expected == actual);
        assertEquals(expected, actual);
        final String expected2 = "expected2";
        Assert.assertOneOf(new RuntimeException("test failed"), actual, expected2, expected);
        Assert.assertOneOf(new RuntimeException("test failed"), actual, expected, expected2);

    }

    public void testNoMatchMultiple() {
        try {
            Assert.assertOneOf(new IllegalArgumentException("actual not in [expected1, expected2]"), "actual", "expected1", "expected2");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            assertTrue("actual not in [expected1, expected2]".equals(e.getMessage()));
        }
    }

    public void testNoInstances() {
        try {
            new Assert();
            fail("IllegalStateException expected");
        } catch (IllegalStateException e) {
            // expected
        }

    }

}
