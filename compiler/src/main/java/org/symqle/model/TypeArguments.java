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

package org.symqle.model;

import org.symqle.util.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Type arguments, JLS 5 4.5.1.
 */
public class TypeArguments {

    private final List<TypeArgument> arguments;

    /**
     * Construct form arument list.
      * @param arguments list of arguments
     */
    public TypeArguments(final List<TypeArgument> arguments) {
        this.arguments = new ArrayList<TypeArgument>(arguments);
    }


    /**
     * Empty type arguments.
     */
    public TypeArguments() {
        this(Collections.<TypeArgument>emptyList());
    }

    @Override
    public final String toString() {
        return arguments.isEmpty()
                ? ""
                : Utils.format(arguments, "<", ", ", ">");
    }

    /**
     * As list of type argument elements.
     * @return immutable list of TypeArgument
     */
    public final List<TypeArgument> getArguments() {
        return Collections.unmodifiableList(arguments);
    }


    /**
     * Create new TypeArguments by replacing type parameters in {@code this}.
     * @param mapping type parameters to arguments mapping
     * @return new type arguments.
     */
    public final TypeArguments replaceParams(final Map<String, TypeArgument> mapping) {
        final List<TypeArgument> result = new ArrayList<TypeArgument>(arguments.size());
        for (TypeArgument arg: arguments) {
            result.add(arg.replaceParams(mapping));
        }
        return new TypeArguments(result);

    }

    @Override
    public final boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TypeArguments that = (TypeArguments) o;

        return arguments.equals(that.arguments);
    }

    @Override
    public final int hashCode() {
        return arguments.hashCode();
    }

}
