/*
* Copyright Alexander Izyurov 2010
*/
package org.simqle.util;

import org.simqle.model.F;
import org.simqle.model.Function;
import org.simqle.parser.SimqleParser;
import org.simqle.parser.SyntaxTree;
import org.simqle.processor.GrammarException;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * <br/>13.11.2011
 *
 * @author Alexander Izyurov
 */
public class Utils {

    private Utils() {
    }

    // to make Cobertura happy
    static {
        new Utils();
    }

    public static final String LINE_BREAK = System.getProperty("line.separator", "\n");

    public static String indent(int indent, String... source) {
        StringBuilder builder = new StringBuilder();
        for (String s: source) {
            for (int i=0; i<indent && indent>=0; i++) {
                builder.append(" ");
            }
            builder.append(s).append(LINE_BREAK);
        }
        return builder.toString();
    }

    public static SimqleParser createParser(String source) {
        Reader reader = new StringReader(source);
        return new SimqleParser(reader);
    }

    public static <T> String format(final Collection<T> list, final String prefix,
                                 final String separator, final String suffix) {
        return format(list, prefix, separator, suffix, new F<T, String, RuntimeException>() {
            @Override
            public String apply(T t) {
                return t.toString();
            }
        });
    }

    public static <T, Ex extends Exception> String format(final Collection<T> list, final String prefix,
                                 final String separator, final String suffix, final F<T, String, Ex> f) throws Ex {
        if (list.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        builder.append(prefix);
        boolean theFirst = true;
        for (final T item: list) {
            if (!theFirst) {
                builder.append(separator);
            }
            theFirst = false;
            builder.append(f.apply(item));
        }
        builder.append(suffix);
        return builder.toString();
    }

    public static <T, R, Ex extends Exception> Collection<R>
    map(final Collection<T> list, final F<T, R, Ex> function) throws Ex {
        Collection<R> result = new LinkedList<R>();
        for (final T element: list) {
            result.add(function.apply(element));
        }
        return result;
    }

    public static <Arg> List<String> convertToStringList(List<Arg> list, Function<String, Arg> function) {
        final List<String> result = new ArrayList<String>(list.size());
        for (Arg arg: list) {
            result.add(function.apply(arg));            
        }
        return result;
    }

    public static List<String> values(List<SyntaxTree> nodes) {
        return convertToStringList(nodes, new Function<String, SyntaxTree>() {
            public String apply(SyntaxTree syntaxTree) {
                return syntaxTree.getValue();
            }
        });
    }

    public static List<String> bodies(List<SyntaxTree> nodes) {
        return convertToStringList(nodes, new Function<String, SyntaxTree>() {
            public String apply(SyntaxTree syntaxTree) {
                return syntaxTree.getBody();
            }
        });
    }

    public static String getAccessModifier(List<SyntaxTree> nodes) throws GrammarException {
        String accessModifier = "";
        for (SyntaxTree node: nodes) {
            final String candidate = node.getValue();
            if (ACCESS_MODIFIERS.contains(candidate)) {
                if (!accessModifier.equals("")) {
                    throw new GrammarException("Access modifiers conflict: "+accessModifier +", "+candidate, node);
                }
                accessModifier = candidate;
            }
        }
        return accessModifier;
    }

    public static Set<String> getNonAccessModifiers(List<SyntaxTree> nodes) {
        final Set<String> stringList = new HashSet<String>(values(nodes));
        stringList.removeAll(ACCESS_MODIFIERS);
        return stringList;
    }

    public static List<String> ACCESS_MODIFIERS = Arrays.asList("public", "protected", "private");



}
