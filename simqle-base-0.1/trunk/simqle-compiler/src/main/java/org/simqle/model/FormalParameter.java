/*
* Copyright Alexander Izyurov 2010
*/
package org.simqle.model;

import org.simqle.parser.SyntaxTree;
import org.simqle.processor.GrammarException;
import org.simqle.util.Assert;
import org.simqle.util.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;


/**
 * <br/>13.11.2011
 *
 * @author Alexander Izyurov
 */
public class FormalParameter {

    private final Type rawType;

    private final String name;

    private final List<String> modifiers;

    private final boolean ellipsis;
    

    public FormalParameter(SyntaxTree node) throws GrammarException {
        Assert.assertOneOf(new GrammarException("Unexpected type: "+node.getType(), node), node.getType(), "FormalParameter", "FormalParameterWithEllipsis");
        rawType = node.find("Type", Type.CONSTRUCT).get(0);
        if (node.getType().equals("FormalParameter")) {
            ellipsis = false;
        } else  {
            // must be FormalParameterWithEllipsis due to asserttion above
            ellipsis = true;
        }
        name = node.find("VariableDeclaratorId.Identifier").get(0).getValue();
        modifiers = Utils.bodies(node.find("VariableModifiers.VariableModifier"));
    }

    public FormalParameter(Type type, String name) {
        this(type, name, Collections.<String>emptyList(), false);
    }

    public FormalParameter(final Type type, final String name, final List<String> modifiers, boolean ellipsis) {
        this.rawType = type;
        this.name = name;
        this.modifiers = new ArrayList<String>(modifiers);
        this.ellipsis = ellipsis;
    }

    public FormalParameter makeFinal(boolean addFinal) {
        final ArrayList<String> newModifiers = new ArrayList<String>(modifiers);
        if (addFinal) {
            if (! newModifiers.contains("final")) {
                newModifiers.add(0, "final");
            }
        } else {
            newModifiers.remove("final");
        }
        return new FormalParameter(rawType, name, newModifiers, ellipsis);
    }


    public Type getType() {
        return ellipsis ? rawType.arrayOf() : rawType;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return Utils.format(modifiers, "", " ", " ") + rawType.toString() +
                ( ellipsis ? "..." : "" ) +
                " " + name;
    }

    public List<String> getModifiers() {
        return modifiers;
    }

    public boolean isEllipsis() {
        return ellipsis;
    }

    public FormalParameter substituteParameters(TypeParameters typeParameters, TypeArguments typeArguments) throws ModelException {
        return new FormalParameter(rawType.substituteParameters(typeParameters, typeArguments),
                name,
                modifiers,
                ellipsis
                );
    }

    public String erasure(final Set<String> typeParameterNames) {
        return getType().erasure(typeParameterNames); 
    }

    public static F<FormalParameter, String, RuntimeException> f_erasure(final Set<String> typeParameterNames) {
        return new F<FormalParameter, String, RuntimeException>() {
            @Override
            public String apply(FormalParameter formalParameter) throws RuntimeException {
                return formalParameter.erasure(typeParameterNames);
            }
        };
    }

    public static F<SyntaxTree, FormalParameter, GrammarException> CONSTRUCT =
            new F<SyntaxTree, FormalParameter, GrammarException>() {
                @Override
                public FormalParameter apply(SyntaxTree syntaxTree) throws GrammarException {
                    return new FormalParameter(syntaxTree);
                }
            };

    public static F<FormalParameter, String, RuntimeException> NAME =
            new F<FormalParameter, String, RuntimeException>() {
                @Override
                public String apply(final FormalParameter formalParameter) {
                    return formalParameter.getName();
                }
            };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FormalParameter that = (FormalParameter) o;

        if (ellipsis != that.ellipsis) return false;
//        if (!modifiers.equals(that.modifiers)) return false;
        if (!name.equals(that.name)) return false;
        if (!rawType.equals(that.rawType)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = rawType.hashCode();
        result = 31 * result + name.hashCode();
//        result = 31 * result + modifiers.hashCode();
        result = 31 * result + (ellipsis ? 1 : 0);
        return result;
    }
}
