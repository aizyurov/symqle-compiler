package org.symqle.model;

import org.symqle.parser.SyntaxTree;
import org.symqle.processor.GrammarException;
import org.symqle.util.Assert;
import org.symqle.util.Utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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

    public abstract MethodDefinition createArchetypeMethod(InterfaceDefinition interfaceDefinition) throws ModelException;
    public abstract List<String> getRequiredImports();


    public static void verify(InterfaceDefinition interfaceDefinition) throws ModelException {
        for (MethodDefinition def: interfaceDefinition.getDeclaredMethods()) {
            if (def.getName().startsWith(ARCHETYPE_METHOD_PREFIX)) {
                throw new ModelException("Prefix \""+ARCHETYPE_METHOD_PREFIX+"\" is reserved for generated methods");
            }
        }
    }

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
            throw new GrammarException(e, node);
        }
    }

    public static boolean isArchetypeMethod(MethodDefinition method) {
        return method.getName().startsWith(ARCHETYPE_METHOD_PREFIX);
    }

    private static class SqlArchetype extends Archetype {

        private SqlArchetype(TypeParameters typeParameters) throws ModelException {
            super(typeParameters);
            if (!typeParameters.isEmpty()) {
                throw new ModelException("Sql archetype does not take type parameters, found: "+typeParameters.size());
            }
        }

        public MethodDefinition createArchetypeMethod(final InterfaceDefinition interfaceDefinition) throws ModelException {
            return MethodDefinition.parseAbstract(
                    String.format(SQL_METHOD_FORMAT, interfaceDefinition.getName(),
                            interfaceDefinition.getName(), ""), interfaceDefinition);
        }

        @Override
        public List<String> getRequiredImports() {
            return Arrays.asList("import org.symqle.common.SqlContext;", "import org.symqle.common.Sql;");
        }

    }

    private static class QueryArchetype extends Archetype {

        private QueryArchetype(TypeParameters typeParameters) throws ModelException {
            super(typeParameters);
            if (typeParameters.size()!=1) {
                throw new ModelException("Query archetype requires 1 type parameter, found: "+typeParameters.size());
            }

        }

        public MethodDefinition createArchetypeMethod(final InterfaceDefinition interfaceDefinition) throws ModelException {
            return MethodDefinition.parseAbstract(
                    String.format(QUERY_METHOD_FORMAT, interfaceDefinition.getName(),
                            getTypeParameters(),
                            interfaceDefinition.getName(), ""), interfaceDefinition);
        }

        @Override
        public List<String> getRequiredImports() {
            return Arrays.asList("import org.symqle.common.SqlContext;", "import org.symqle.common.Query;");
        }
    }

    private static final String ARCHETYPE_METHOD_PREFIX = "z$sqlOf";


    private final static String QUERY_METHOD_FORMAT = Utils.indent(12,
            "/**",
            "* Creates a Query representing %s.",
            "* @param context the Sql construction context",
            "* @return constructed Query",
            "*/",
            "Query%s " + ARCHETYPE_METHOD_PREFIX + "%s(%sSqlContext context);"
            );

    private final static String SQL_METHOD_FORMAT = Utils.indent(12,
            "/**",
            "* Creates an Sql representing %s.",
            "* @param context the Sql construction context",
            "* @return constructed Sql",
            "*/",
            "Sql " + ARCHETYPE_METHOD_PREFIX + "%s(%sSqlContext context);"
    );

    public static final Archetype NONE = new Archetype(new TypeParameters(Collections.<TypeParameter>emptyList())) {

        @Override
        public MethodDefinition createArchetypeMethod(InterfaceDefinition interfaceDefinition) throws ModelException {
            return null;
        }

        @Override
        public List<String> getRequiredImports() {
            return Collections.emptyList();
        }
    };

}
