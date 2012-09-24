package org.simqle.test;

import java.io.*;

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
        return source.replaceAll("\\s+", " ").trim();
    }

    public static String readTextFile(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        final CharArrayWriter charArrayWriter = new CharArrayWriter();
        PrintWriter writer = new PrintWriter(charArrayWriter);
        for (String line = reader.readLine(); line!=null; line=reader.readLine()) {
            writer.println(line);
        }
        reader.close();
        writer.close();
        return charArrayWriter.toString();
    }
}
