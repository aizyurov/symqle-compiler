/*
   Copyright 2011-2014 Alexander Izyurov

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.package org.symqle.common;
*/

package org.symqle.test;

import java.io.*;
import java.util.regex.Pattern;

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
    private final static Pattern singleLineComment = Pattern.compile("//.*");
    private final static Pattern whiteSpace = Pattern.compile("\\s+");
}
