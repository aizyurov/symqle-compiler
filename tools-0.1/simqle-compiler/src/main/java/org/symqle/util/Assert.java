package org.symqle.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * We need Assert, but we do not want to make production dependent of JUnit
 * So custom class with one method
 * Time: 15:27
 * To change this template use File | Settings | File Templates.
 */
public class Assert{

    Assert() {
        throw new IllegalStateException("No instances - this is a utility class");
    }

    public static <T, E extends Exception> void assertOneOf(E throwIfFalse, T actual, T... expected) throws E {
        Set<T> expectedSet = new HashSet<T>(Arrays.asList(expected));
        if (!expectedSet.contains(actual)) {
            throw throwIfFalse;
        }
    }

}
