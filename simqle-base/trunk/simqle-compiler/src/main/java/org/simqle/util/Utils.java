/*
* Copyright Alexander Izyurov 2010
*/
package org.simqle.util;

import org.simqle.model.*;
import org.simqle.parser.SimqleParser;
import org.simqle.parser.SyntaxTree;
import org.simqle.processor.GrammarException;

import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * <br/>13.11.2011
 *
 * @author Alexander Izyurov
 */
public class Utils {

    Utils() {
        throw new RuntimeException("No instances: utility class");
    }

    public static Type substituteTypeArguments(final List<TypeArgument> typeArgumentsActual, final List<TypeParameter> typeParameters, final Type paramType) throws ModelException {
        return paramType == null ? null :
                new Type(substituteTypeArguments(paramType.getNameChain(), typeParameters, typeArgumentsActual), paramType.getArrayDimensions());
    }

    public static interface Factory<T> {
        T create(SyntaxTree node)  throws GrammarException;
    }

    private static class ReflectionFactory<T>  implements Factory<T> {
        private final Constructor<T> constructor;

        private ReflectionFactory(Class<T> clazz) {
            try {
                constructor = clazz.getConstructor(SyntaxTree.class);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("Internal error: no appropriate constructor in "+clazz.getName(), e);
            }
        }

        @Override
        public T create(final SyntaxTree node) throws GrammarException {
            try {
                return constructor.newInstance(node);
            } catch (InvocationTargetException e) {
                final Throwable cause = e.getCause();
                if (cause instanceof GrammarException) {
                    throw (GrammarException) cause;
                } else {
                    throw new RuntimeException("Internal error", e);
                }
            } catch (Exception e) {
                throw new RuntimeException("Internal error", e);
            }
        }
    }

    public static <T> List<T> convertChildren(SyntaxTree parent, String path, Factory<T> factory)  throws GrammarException {
        List<T> children = new ArrayList<T>();
        for (SyntaxTree child: parent.find(path)) {
            children.add(factory.create(child));
        }
        return children;
    }

    public static <T> List<T> convertChildren(SyntaxTree parent, String path, Class<T> clazz) throws GrammarException {
        return convertChildren(parent, path, new ReflectionFactory<T>(clazz));
    }

    public static String getChildrenImage(SyntaxTree parent, String path) {
        StringBuilder builder = new StringBuilder();
        for (SyntaxTree child: parent.find(path)) {
            builder.append(child.getImage());
        }
        return builder.toString();
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

    public static String concat(List<String> sources, String separator) {
        StringBuilder builder = new StringBuilder();
        for (String source: sources) {
            if (builder.length()>0) {
                builder.append(separator);
            }
            builder.append(source);
        }
        return builder.toString();
    }

    public static String concat(String separator, String... sources) {
        return concat(Arrays.asList(sources), separator);
    }

    public static SimqleParser createParser(String source) {
        Reader reader = new StringReader(source);
        return new SimqleParser(reader);
    }

    /**
     * Formats the list as follows:
     * prefix, separator-separated items converted with function, suffix.
     * If the list is empty, returns empty stirng (no prefix and suffix)!
     * @param list
     * @param prefix
     * @param separator
     * @param suffix
     * @param function
     * @param <T>
     * @return
     */
    public static <T> String formatList(List<T> list, String prefix, 
                                 String separator, String suffix, Function<String, T> function) {
        if (list.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        builder.append(prefix);
        for (int i=0; i<list.size(); i++) {
            T item = list.get(i);
            if (i>0) {
                builder.append(separator);
            }
            builder.append(function.apply(item));
        }
        builder.append(suffix);
        return builder.toString();
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

    private static List<TypeNameWithTypeArguments> substituteTypeArguments(List<TypeNameWithTypeArguments> source, List<TypeParameter> typeParameters, List<TypeArgument> typeArguments) throws ModelException {
        final Map<String, TypeArgument> substitutions = new HashMap<String, TypeArgument>(typeParameters.size());
        if (typeParameters.size()!=typeArguments.size()) {
            throw new ModelException("Required "+typeParameters.size()+" parameters but found "+typeArguments.size());
        }
        for (int i=0; i<typeParameters.size(); i++) {
            substitutions.put(typeParameters.get(i).getName(), typeArguments.get(i));
        }
        // type arguments are substituted with wildcards; type names itself are substituted with reference type only
        // it is not exactly correct for "super" bounds; we also do not check for correct bounds here - leaving it to Java compiler
        final String name = source.get(0).getName();
        // if the source is just a type perameter name, substitute it for its value (mutatis mutandis)
        if (source.size()==1 && substitutions.containsKey(name)) {
                // if bound is "super", substitute for "Object" else for reference (valid for not-bound and "extends")
                List<TypeNameWithTypeArguments> chain =
                        "super".equals(substitutions.get(name).getBoundType()) || substitutions.get(name).getReference()==null ?
                        Collections.singletonList(new TypeNameWithTypeArguments("Object")) :
                        substitutions.get(name).getReference().getNameChain() ;
                return chain;
        } else {
            final List<TypeNameWithTypeArguments> result = new ArrayList<TypeNameWithTypeArguments>(source.size());
            //  the source is a class name, may be with type arguments. Replase type parameter in type arguments only;
            // if the type parameter appears as a class name, it is an error
            for (TypeNameWithTypeArguments typeNameWithTypeArguments: source) {
                final String typeName = typeNameWithTypeArguments.getName();
                if (substitutions.containsKey(typeName)) {
                    throw new IllegalArgumentException(typeName+" is a parameter and cannot appear in this context: "+
                            substitutions.get(name).getReference().getImage());
                } else {
                    final List<TypeArgument> currentTypeArguments = typeNameWithTypeArguments.getTypeArguments();
                    final List<TypeArgument> newTypeArguments = new ArrayList<TypeArgument>(currentTypeArguments.size());
                    for (TypeArgument typeArgument: currentTypeArguments) {
                        final Type reference = typeArgument.getReference();
                        // make substitutions in this Type
                        final Type newType = reference == null ? null : substituteTypeArguments(typeArguments, typeParameters, reference);
                        TypeArgument newTypeArgument = new TypeArgument(typeArgument.isWildCardArgument(), typeArgument.getBoundType(), newType);
                        newTypeArguments.add(newTypeArgument);
                    }
                    result.add(new TypeNameWithTypeArguments(name, newTypeArguments));
                }
            }
            return result;
        }
    }

    private static List<String> ACCESS_MODIFIERS = Arrays.asList("public", "protected", "private");



}
