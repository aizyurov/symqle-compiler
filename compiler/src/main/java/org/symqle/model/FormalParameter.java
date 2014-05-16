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
import org.symqle.util.AssertNodeType;
import org.symqle.util.Utils;

import java.util.*;


/**
 * Formal parameter of a method.
 *
 * @author Alexander Izyurov
 */
public class FormalParameter {

    private final Type rawType;

    private final String name;

    private final List<String> modifiers;

    private final boolean ellipsis;


    /**
     * Construct from AST.
     * @param node syntax tree
     * @throws GrammarException wrong tree
     */
    public FormalParameter(final SyntaxTree node) throws GrammarException {
        AssertNodeType.assertOneOf(node,
                "FormalParameter", "FormalParameterWithEllipsis");
        rawType = node.find("Type", Type.CONSTRUCT).get(0);
        if (node.getType().equals("FormalParameter")) {
            ellipsis = false;
        } else  {
            // must be FormalParameterWithEllipsis due to assertion above
            ellipsis = true;
        }
        name = node.find("VariableDeclaratorId.Identifier").get(0).getValue();
        modifiers = node.find("VariableModifiers.VariableModifier", SyntaxTree.BODY);
    }

    /**
     * Construct parameter of given type and name.
     * @param type parameter type
     * @param name parameter name
     */
    public FormalParameter(final Type type, final String name) {
        this(type, name, Collections.<String>emptyList(), false);
    }

    /**
     * Construct parameter.
     * @param type parameter type
     * @param name parameter name
     * @param modifiers parameter modifiers
     * @param ellipsis true for parameter with ellipsis {@code (Object... objects)}.
     * In this case {@link #getType()} returns Object[], but {@link #toString()} would return
     * "Object... objects".
     */
    public FormalParameter(final Type type, final String name, final List<String> modifiers, final boolean ellipsis) {
        this.rawType = type;
        this.name = name;
        this.modifiers = new ArrayList<String>(modifiers);
        this.ellipsis = ellipsis;
    }

    /**
     * Make parameter with the same type and name but force final or non-final modifier.
     * If current "final" modifier is already as expected, a copy of {@code this} is returned.
     * @param addFinal true to make final, false to remove final modifier.
     * @return new parameter with desired modifier.
     */
    public final FormalParameter makeFinal(final boolean addFinal) {
        final ArrayList<String> newModifiers = new ArrayList<String>(modifiers);
        if (addFinal) {
            if (!newModifiers.contains("final")) {
                newModifiers.add(0, "final");
            }
        } else {
            newModifiers.remove("final");
        }
        return new FormalParameter(rawType, name, newModifiers, ellipsis);
    }


    /**
     * Type of this parameter.
     * @return the type
     */
    public final Type getType() {
        return ellipsis ? rawType.arrayOf() : rawType;
    }


    /**
     * Name of this parameter.
     * @return the name
     */
    public final String getName() {
        return name;
    }

    @Override
    public final String toString() {
        return Utils.format(modifiers, "", " ", " ") + rawType.toString()
                + (ellipsis ? "..." : "") + " " + name;
    }

    /**
     * Parameter modifiers.
     * @return modifiers
     */
    public final List<String> getModifiers() {
        return modifiers;
    }


    /**
     * Erasure of this parameter type.
     * @param typeParameterNames type parameters in current context
     * @return erasure.
     */
    public final String erasure(final Set<String> typeParameterNames) {
        return getType().erasure(typeParameterNames);
    }

    /**
     * Function, which converts parameter to type erasure.
     * @param typeParameterNames type parameters in current context.
     * @return the function
     */
    public static F<FormalParameter, String, RuntimeException> f_erasure(final Set<String> typeParameterNames) {
        return new F<FormalParameter, String, RuntimeException>() {
            @Override
            public String apply(final FormalParameter formalParameter) {
                return formalParameter.erasure(typeParameterNames);
            }
        };
    }

    /**
     * Function, which converts SyntaxTree to FormalParameter.
     */
    public static final F<SyntaxTree, FormalParameter, GrammarException> CONSTRUCT =
            new F<SyntaxTree, FormalParameter, GrammarException>() {
                @Override
                public FormalParameter apply(final SyntaxTree syntaxTree) throws GrammarException {
                    return new FormalParameter(syntaxTree);
                }
            };

    /**
     * Function, which extracts name from FormalParameter.
     */
    public static final F<FormalParameter, String, RuntimeException> NAME =
            new F<FormalParameter, String, RuntimeException>() {
                @Override
                public String apply(final FormalParameter formalParameter) {
                    return formalParameter.getName();
                }
            };

    @Override
    public final boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FormalParameter that = (FormalParameter) o;

        if (ellipsis != that.ellipsis) {
            return false;
        }
//        if (!modifiers.equals(that.modifiers)) return false;
        if (!name.equals(that.name)) {
            return false;
        }
        if (!rawType.equals(that.rawType)) {
            return false;
        }

        return true;
    }

    @Override
    public final int hashCode() {
        int result = rawType.hashCode();
        result = 31 * result + name.hashCode();
//        result = 31 * result + modifiers.hashCode();
        result = 31 * result + (ellipsis ? 1 : 0);
        return result;
    }

    /**
     * Replace type parameters for type arguments.
     * @param mapping replacement map
     * @return new parameter with replaced type parameters.
     */
    public final FormalParameter replaceParams(final Map<String, TypeArgument> mapping) {
        return new FormalParameter(rawType.replaceParams(mapping), name, modifiers, ellipsis);
    }
}
