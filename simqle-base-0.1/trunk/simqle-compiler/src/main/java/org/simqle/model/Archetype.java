package org.simqle.model;

import org.simqle.parser.SyntaxTree;
import org.simqle.processor.GrammarException;
import org.simqle.util.Assert;
import org.simqle.util.Utils;

/**
 * Created by IntelliJ IDEA.
 * User: aizyurov
 * Date: 25.11.2012
 * Time: 18:50:55
 * To change this template use File | Settings | File Templates.
 */
public abstract class Archetype {
    private final TypeParameters typeParameters;

    protected Archetype(TypeParameters typeParameters) {
        this.typeParameters = typeParameters;
    }

    protected final TypeParameters getTypeParameters() {
        return typeParameters;
    }

    public abstract void apply(InterfaceDefinition interfaceDefinition) throws ModelException;

    public static Archetype create(final SyntaxTree node) throws GrammarException {
        Assert.assertOneOf(new GrammarException("Unexpected type: "+node.getType(), node), node.getType(), "Archetype");
        TypeParameters parameters = new TypeParameters(node.find("TypeParameters.TypeParameter", TypeParameter.CONSTRUCT));
        String name = node.find("Identifier", SyntaxTree.VALUE).get(0);
        try {
            if ("Sql".equals(name)) {
                return new SqlArchetype(parameters);
            } else if ("Query".equals(name)) {
                return new QueryArchetype(parameters);
            } else {
                throw new GrammarException("Unknown archetype: " + name, node);
            }
        } catch (ModelException e) {
            throw new GrammarException(e.getMessage(), node);
        }
    }

    private static class SqlArchetype extends Archetype {

        private SqlArchetype(TypeParameters typeParameters) throws ModelException {
            super(typeParameters);
            if (!typeParameters.isEmpty()) {
                throw new ModelException("Sql archetype does not take type parameters, found: "+typeParameters);
            }
        }

        @Override
        public void apply(InterfaceDefinition interfaceDefinition) throws ModelException {
            MethodDefinition methodDefinition = MethodDefinition.parseAbstract(
                    String.format(SQL_METHOD_FORMAT,
                            interfaceDefinition.getName()), interfaceDefinition);
            interfaceDefinition.addMethod(methodDefinition);
        }
    }

    private static class QueryArchetype extends Archetype {

        private QueryArchetype(TypeParameters typeParameters) throws ModelException {
            super(typeParameters);
            if (typeParameters.size()!=1) {
                throw new ModelException("Query archetype requires 1 type parameter, found: "+typeParameters);
            }

        }

        @Override
        public void apply(InterfaceDefinition interfaceDefinition) throws ModelException {
            MethodDefinition methodDefinition = MethodDefinition.parseAbstract(
                    String.format(QUERY_METHOD_FORMAT,
                            getTypeParameters().list().get(0),
                            interfaceDefinition.getName()), interfaceDefinition);
            interfaceDefinition.addMethod(methodDefinition);
        }
    }

    private final static String QUERY_METHOD_FORMAT = Utils.indent(4,
            "/**",
            "* Creates a Query",
            "* @param context the Sql construction context",
            "* @return query conforming to <code>this</code> syntax",
            "*/",
            "Query<%s> z$create$%s(final SqlContext context);"
            );

    private final static String SQL_METHOD_FORMAT = Utils.indent(8,
            "/**",
            "* Creates an Sql representing <code>this</code>",
            "* @param context the Sql construction context",
            "* @return sql conforming to <code>this</code> syntax",
            "*/",
            "Sql z$create$%s(final SqlContext context);"
    );

}
