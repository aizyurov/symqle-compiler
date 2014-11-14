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

import junit.framework.TestCase;
import org.symqle.parser.SimpleNode;
import org.symqle.parser.SymqleParser;

import java.io.Reader;
import java.io.StringReader;

/**
 * <br/>13.11.2011
 *
 * @author Alexander Izyurov
 */
public class SyntaxTreeTest extends TestCase {

    public static SymqleParser createParser(String source) {
        Reader reader = new StringReader(source);
        return new SymqleParser(reader);
    }

    public static final String LINE_BREAK = System.getProperty("line.separator", "\n");

    public static String join(int indent, String... source) {
        StringBuilder builder = new StringBuilder();
        for (String s: source) {
            for (int i=0; i<indent && indent>=0; i++) {
                builder.append(" ");
            }
            builder.append(s).append(LINE_BREAK);
        }
        return builder.toString();
    }
    private final static String SCALAR_METHOD_COMMENT_FORMAT = join(8,
            "/**",
            "* Converts data from row element to Java object of type %s",
            "* @param element row element containing the data",
            "* @return object of type %s, may be null",
            "*/",
            "%s z$value%s(Element element);"
            );

    public void test() throws Exception {
        String typeParameter="T";
        String interfaceName="SelectSublist";
        final String methodSource = String.format(SCALAR_METHOD_COMMENT_FORMAT, typeParameter, typeParameter, typeParameter, interfaceName);
        final SimpleNode simpleNode = createParser(methodSource).AbstractMethodDeclaration();


        
    }


}
