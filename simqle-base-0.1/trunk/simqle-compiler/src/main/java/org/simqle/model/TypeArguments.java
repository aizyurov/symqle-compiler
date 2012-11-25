package org.simqle.model;

import org.simqle.parser.SyntaxTree;
import org.simqle.processor.GrammarException;
import org.simqle.util.Assert;
import org.simqle.util.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: aizyurov
 * Date: 24.11.2012
 * Time: 21:30:13
 * To change this template use File | Settings | File Templates.
 */
public class TypeArguments {
    private final List<TypeArgument> arguments;

    public TypeArguments(final SyntaxTree node) throws GrammarException {
        Assert.assertOneOf(new GrammarException("Unexpected type: "+node.getType(), node), node.getType(), "TypeArguments");
        this.arguments = node.find("TypeArgument", TypeArgument.CONSTRUCT);
    }

    public TypeArguments(List<TypeArgument> arguments) {
        this.arguments = new ArrayList<TypeArgument>(arguments);
    }

    private TypeArguments() {
        this(Collections.<TypeArgument>emptyList());
    }

    public static final F<SyntaxTree, TypeArguments, GrammarException> CONSTRUCT =
            new F<SyntaxTree, TypeArguments, GrammarException>() {
        @Override
        public TypeArguments apply(SyntaxTree syntaxTree) throws GrammarException {
            return new TypeArguments(syntaxTree);
        }
    };

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

    public TypeArguments substituteParameters(TypeParameters typeParameters, TypeArguments typeArguments) throws ModelException {
        final List<TypeArgument> result = new ArrayList<TypeArgument>(arguments.size());
        for (TypeArgument arg: arguments) {
            result.add(arg.substituteParameters(typeParameters, typeArguments));
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
}
