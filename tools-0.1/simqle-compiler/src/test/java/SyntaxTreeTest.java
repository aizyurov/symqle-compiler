/*
* Copyright Alexander Izyurov 2010
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
