package org.simqle.test;

/**
 * Created by IntelliJ IDEA.
 * User: lvovich
 * Date: 17.08.12
 * Time: 12:27
 * To change this template use File | Settings | File Templates.
 */
public class TestUtils {

    private TestUtils() {
    }

    public static String normalizeFormatting(String source) {
        return source.replaceAll("\\s+", " ");

    }
}
