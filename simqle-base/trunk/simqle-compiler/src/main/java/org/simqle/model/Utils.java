/*
* Copyright Alexander Izyurov 2010
*/
package org.simqle.model;

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

    public static Type substituteTypeArguments(final List<TypeArgument> typeArgumentsActual, final List<TypeParameter> typeParameters, final Type paramType) {
        return new Type(substituteTypeArguments(paramType.getNameChain(), typeParameters, typeArgumentsActual), paramType.getArrayDimensions());
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
            } catch (InstantiationException e) {
                throw new RuntimeException("Internal error", e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Internal error", e);
            } catch (InvocationTargetException e) {
                final Throwable cause = e.getCause();
                if (cause instanceof GrammarException) {
                    throw (GrammarException) cause;
                } else {
                    throw new RuntimeException("Internal error", e);
                }
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

    public static <T> String formatList(List<T> list, String prefix, 
                                 String separator, String suffix, Function<String, T> function) {
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

    public static List<String> getNonAccessModifiers(List<SyntaxTree> nodes) {
        final List<String> stringList = values(nodes);
        stringList.removeAll(ACCESS_MODIFIERS);
        return stringList;
    }

    public static List<TypeNameWithTypeArguments> substituteTypeArguments(List<TypeNameWithTypeArguments> source, List<TypeParameter> typeParameters, List<TypeArgument> typeArguments) {
        final Map<String, TypeArgument> substitutions = new HashMap<String, TypeArgument>(typeParameters.size());
        for (int i=0; i<typeParameters.size(); i++) {
            try {
                substitutions.put(typeParameters.get(i).getName(), typeArguments.get(i));
            } catch (Exception e) {
                throw new RuntimeException("Internal error", e);
            }
        }
        // type arguments are substituted with wildcards; type names itself are substituted with reference type only
        // it is not exactly correct for "super" bounds; we also do not check for correct bounds here - leaving it to Java compiler
        final List<TypeNameWithTypeArguments> result = new ArrayList<TypeNameWithTypeArguments>(source.size());
        for (TypeNameWithTypeArguments typeNameWithTypeArguments: source) {
            final String name = typeNameWithTypeArguments.getName();
            if (substitutions.containsKey(name)) {
//                result.addAll(substituteTypeArguments(substitutions.get(name).reference.getNameChain(), typeParameters, typeArguments));
                result.addAll(substitutions.get(name).reference.getNameChain());
            } else {
                final List<TypeArgument> typeArguments1 = typeNameWithTypeArguments.getTypeArguments();
                final List<String> newTypeArguments = new ArrayList<String>(typeArguments1.size());
                for (TypeArgument typeArgument: typeArguments1) {
                    final String value = typeArgument.getValue();
                    if (substitutions.containsKey(value)) {
                        newTypeArguments.add(substitutions.get(value).getValue());
                    } else {
                        newTypeArguments.add(value);
                    }
                }
                result.add(new TypeNameWithTypeArguments(name, newTypeArguments));
            }
        }
        return result;
    }

    private static List<String> ACCESS_MODIFIERS = Arrays.asList("public", "protected", "private");



}
