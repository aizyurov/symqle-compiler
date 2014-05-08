/*
* Copyright Alexander Izyurov 2010
*/
package org.symqle.util;

import org.symqle.model.F;
import org.symqle.parser.SyntaxTree;
import org.symqle.processor.GrammarException;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * A collection of commonly used static methods, mostly string construction.
 *
 * @author Alexander Izyurov
 */
public final class Utils {

    private Utils() {
    }

    // to make Cobertura happy
    static {
        new Utils();
    }

    /**
     * OS-specific line break character sequence: "\r", "\n" or "\r\n".
     */
    public static final String LINE_BREAK = System.getProperty("line.separator", "\n");

    /**
     * Indent each line in the list and concatenate the resulting lines using line break as separator.
     * @param indent number of spaces to prepend
     * @param source lines
     * @return resulting string
     */
    public static String indent(final int indent, final String... source) {
        StringBuilder builder = new StringBuilder();
        for (String s: source) {
            for (int i = 0; i < indent && indent >= 0; i++) {
                builder.append(" ");
            }
            builder.append(s).append(LINE_BREAK);
        }
        return builder.toString();
    }


    /**
     * Create a string, which contains string representation of list.
     * A given prefix is prepended, separator inserted between items and suffix appended.
     * If the list is empty, empty string is returned (prefix and suffix not used).
     * @param list items. toString() is applied to each.
     * @param prefix string to prepend
     * @param separator string to insert between items
     * @param suffix string to append
     * @param <T> item type
     * @return string formatted as described above
     */
    public static <T> String format(final Collection<T> list, final String prefix,
                                 final String separator, final String suffix) {
        return format(list, prefix, separator, suffix, new F<T, String, RuntimeException>() {
            @Override
            public String apply(final T t) {
                return t.toString();
            }
        });
    }

    /**
     * Create a string, which contains string representation of list.
     * Function, which converts item to String, is applied to each item.
     * A given prefix is prepended, separator inserted between items and suffix appended.
     * If the list is empty, empty string is returned (prefix and suffix not used).
     * @param list items. toString() is applied to each.
     * @param prefix string to prepend
     * @param separator string to insert between items
     * @param suffix string to append
     * @param f function to convert items to String
     * @param <T> item type
     * @param <Ex> exception thrown by conversion function
     * @return string formatted as described above
     * @throws Ex if invocation of {@code f} throws Ex
     */
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

    /**
     * Convert a collection of type {@code T} to a collection of {@code R}.
     * @param list original collection
     * @param function conversion function for items
     * @param <T> item type
     * @param <R> result type
     * @param <Ex> type of exception thrown by conversion function
     * @return collection of converted items
     * @throws Ex if invocation of {@code function} throws Ex
     */
    public static <T, R, Ex extends Exception> Collection<R>
    map(final Collection<T> list, final F<T, R, Ex> function) throws Ex {
        Collection<R> result = new LinkedList<R>();
        for (final T element: list) {
            result.add(function.apply(element));
        }
        return result;
    }

    /**
     * Find access modifiers among class, method or variable modifiers.
     * @param nodes syntax trees of proper type
     * @return access modifier. Empty string if package scope.
     * @throws GrammarException conflicting access modifiers in the list
     */
    public static String getAccessModifier(final List<SyntaxTree> nodes) throws GrammarException {
        String accessModifier = "";
        for (SyntaxTree node: nodes) {
            final String candidate = node.getValue();
            if (ACCESS_MODIFIERS.contains(candidate)) {
                if (!accessModifier.equals("")) {
                    throw new GrammarException("Access modifiers conflict: " + accessModifier + ", " + candidate, node);
                }
                accessModifier = candidate;
            }
        }
        return accessModifier;
    }

    /**
     * Find modifiers other than access modifier among class, method or variable modifiers.
     * @param nodes syntax trees of proper type
     * @return set of string form of modifiers
     */
    public static Set<String> getNonAccessModifiers(final List<SyntaxTree> nodes) {
        final Set<String> stringList = new HashSet<String>(map(nodes, SyntaxTree.VALUE));
        stringList.removeAll(ACCESS_MODIFIERS);
        return stringList;
    }

    /**
     * Access modifiers according to JLS.
     */
    public static final List<String> ACCESS_MODIFIERS = Arrays.asList("public", "protected", "private");



}
