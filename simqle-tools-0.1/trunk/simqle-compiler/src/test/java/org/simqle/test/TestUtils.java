package org.simqle.test;

import java.io.*;
import java.util.regex.Pattern;

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

    public static String pureCode(String source) {
        final String s1 = multilineComment.matcher(source).replaceAll("");
        final String s2 = singleLineComment.matcher(s1).replaceAll("");
        return whiteSpace.matcher(s2).replaceAll(" ").trim();

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

    private final static Pattern multilineComment = Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL | Pattern.MULTILINE);
    private final static Pattern singleLineComment = Pattern.compile("//.*?");
    private final static Pattern whiteSpace = Pattern.compile("\\s+");
}
