package org.simqle.model;

import junit.framework.TestCase;
import org.simqle.parser.ParseException;
import org.simqle.processor.GrammarException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: lvovich
 * Date: 23.08.12
 * Time: 19:53
 * To change this template use File | Settings | File Templates.
 */
public class TestClassParsing extends TestCase {

    public void testAccessModifiersConflict() throws Exception {

        final String source = readFileToString("src/test-data/model/PublicPrivateClass.sample");
        try {
            ClassDefinition.parse(source);
        } catch (RuntimeException e) {
            assertEquals("Internal error", e.getMessage());
            assertTrue(e.getCause() instanceof GrammarException);
            assertTrue(e.getCause().getMessage(), e.getCause().getMessage().startsWith("Access modifiers conflict: public, private"));
        }
    }

    public void testParseError() throws Exception {

        final String source = readFileToString("src/test-data/model/PublicPrivateMethod.sample");
        try {
            ClassDefinition.parse(source);
        } catch (RuntimeException e) {
            assertEquals("Internal error", e.getMessage());
            assertTrue(e.getCause() instanceof ParseException);
            assertTrue(e.getCause().getMessage(), e.getCause().getMessage().startsWith("Parse error"));
        }
    }

    private String readFileToString(final String fileName) throws IOException {
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        for (String line = reader.readLine(); line!=null; line = reader.readLine()) {
            builder.append(line).append("\n");
        }
        return builder.toString();
    }

}
