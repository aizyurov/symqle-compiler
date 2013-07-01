package org.symqle.model;

import org.symqle.util.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: aizyurov
 * Date: 24.11.2012
 * Time: 21:30:13
 * To change this template use File | Settings | File Templates.
 */
public class TypeArguments {
    private final List<TypeArgument> arguments;

    public TypeArguments(List<TypeArgument> arguments) {
        this.arguments = new ArrayList<TypeArgument>(arguments);
    }

    public TypeArguments() {
        this(Collections.<TypeArgument>emptyList());
    }

    @Override
    public String toString() {
        return arguments.isEmpty() ? "" :
                Utils.format(arguments, "<", ", ", ">");
    }

    public static TypeArguments empty() {
        return new TypeArguments();
    }

    public List<TypeArgument> getArguments() {
        return arguments;
    }

    public TypeArguments replaceParams(final Map<String, TypeArgument> mapping) {
        final List<TypeArgument> result = new ArrayList<TypeArgument>(arguments.size());
        for (TypeArgument arg: arguments) {
            result.add(arg.replaceParams(mapping));
        }
        return new TypeArguments(result);

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TypeArguments that = (TypeArguments) o;

        return arguments.equals(that.arguments);
    }

    @Override
    public int hashCode() {
        return arguments.hashCode();
    }

//    /**
//     * creates a new TypeArguments from {@code this}.
//     * The result has the same size as @{code params}.
//     * If a parameter is used in {@code this}, its counterpart in
//     * result is matching value in actualArguments.
//     * Unused parameters match to themselves.
//     * @param params type parameters
//     * @param actualArguments must be the sames size sa {@code this}
//     * @return
//     */
//    public TypeArguments derive(final TypeParameters params, final TypeArguments actualArguments) throws ModelException {
//        final Map<String, TypeArgument> paramMapping = new HashMap<String, TypeArgument>();
//        final Set<String> paramNames = params.names();
//        // TODO recurse into generic types; for now handling one level only
//        for (int i=0; i<arguments.size(); i++) {
//            final String name = arguments.get(i).getReference().getSimpleName();
//            if (paramNames.contains(name)) {
//                final TypeArgument replacement = actualArguments.arguments.get(i);
//                final TypeArgument oldReplacement = paramMapping.put(name, replacement);
//                if (oldReplacement!=null && !oldReplacement.equals(replacement)) {
//                    throw new ModelException(name + " matches to different types: "+replacement + " and "+oldReplacement);
//                }
//            }
//        }
//        final List<TypeArgument> newArguments = new LinkedList<TypeArgument>();
//        for (TypeParameter param: params.list()) {
//            final String name = param.getName();
//            if (paramMapping.containsKey(name)) {
//                newArguments.add(paramMapping.get(name));
//            } else {
//                newArguments.add(new TypeArgument(name));
//            }
//        }
//        return new TypeArguments(newArguments);
//    }

}
