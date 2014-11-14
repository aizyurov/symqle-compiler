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

import org.symqle.parser.SyntaxTree;
import org.symqle.processor.GrammarException;
import org.symqle.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Type parameter, JLS 5 4.4.
 * @author Alexander Izyurov
 */
public class TypeParameter {
    private final String name;
    private final List<Type> typeBound;

    /**
     * Construct from AST.
     * @param node syntax tree
     * @throws GrammarException wrong tree
     */
    public TypeParameter(final SyntaxTree node) throws GrammarException {
        AssertNodeType.assertOneOf(node, "TypeParameter");
        name = node.find("Identifier").get(0).getValue();
        typeBound = node.find("TypeBound.ClassOrInterfaceType", Type.CONSTRUCT);
    }

    private TypeParameter(final String name, final List<Type> typeBound) {
        this.name = name;
        this.typeBound = typeBound;
    }

    /**
     * Create a copy with different name.
     * @param newName new name
     * @return new type parameter with same type bounds
     */
    public final TypeParameter rename(final String newName) {
        return new TypeParameter(newName, new ArrayList<Type>(typeBound));
    }

    /**
     * Type parameter name.
     * @return name
     */
    public final String getName() {
        return name;
    }

    @Override
    public final boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final TypeParameter that = (TypeParameter) o;

        if (!name.equals(that.name)) {
            return false;
        }
        if (!typeBound.equals(that.typeBound)) {
            return false;
        }

        return true;
    }

    @Override
    public final int hashCode() {
        int result = name.hashCode();
        result = 31 * result + typeBound.hashCode();
        return result;
    }

    @Override
    public final String toString() {
        return name + Utils.format(typeBound, " extends ", "& ", "");
    }

    /**
     * Converts SyntaxTree to TypeParameter.
     */
    public static final F<SyntaxTree, TypeParameter, GrammarException> CONSTRUCT =
            new F<SyntaxTree, TypeParameter, GrammarException>() {
                @Override
                public TypeParameter apply(final SyntaxTree syntaxTree) throws GrammarException {
                    return new TypeParameter(syntaxTree);
                }
            };
}


