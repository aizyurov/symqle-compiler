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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A Symqle notion of interface archetype. Archetyped interfaces have similar methods
 * with different names. The methods have the same parameter {@code }SqlContext} and different return values.
 * Currently there are 2 archetypes: arhetype methods of SqlArtetype return {@code SqlBuilder}.
 * Archetype methods of QueryArchetype return {@code QueryBuilder<T>}, T is type parameter.
 * SDL uses special syntax:
 * <pre>
 * {@code public interface SelectStatement<T> : QueryBuilder<T> {}}
 * </pre>
 * is expanded to
 * <pre>
  *{@code public interface SelectStatement<T> : QueryBuilder<T> {
 *     QueryBuilder<T> z$sqlOfSelectStatement(SqlContext context);
 * }
 * }
  * </pre>
 */
public abstract class Archetype {
    private final TypeParameters typeParameters;

    /**
     * Constructs with given type parameters.
     * @param typeParameters type parameters
     */
    protected Archetype(final TypeParameters typeParameters) {
        this.typeParameters = typeParameters;
    }

    /**
     * Type parameters of {@code this}.
     * @return type parameters
     */
    protected final TypeParameters getTypeParameters() {
        return typeParameters;
    }

    /**
     * Build archetype method for this archetype and given interface.
     * @param interfaceDefinition would-be owner of the method
     * @return created method.
     */
    public abstract MethodDefinition createArchetypeMethod(InterfaceDefinition interfaceDefinition);

    /**
     * Import lines required for archetyped interface.
     * Include impores for SqlContext, SqlBuilder or QueryBuilder etc.
     * @return required import lines.
     */
    public abstract List<String> getRequiredImports();


    /**
     * Verifies that interface definition can be archetyped.
     * @param interfaceDefinition interface
     * @throws ModelException not compatible to Archetype
     */
    public static void verify(final InterfaceDefinition interfaceDefinition) throws ModelException {
        for (MethodDefinition def: interfaceDefinition.getDeclaredMethods()) {
            if (def.getName().startsWith(ARCHETYPE_METHOD_PREFIX)) {
                throw new ModelException(
                        "Prefix \"" + ARCHETYPE_METHOD_PREFIX + "\" is reserved for generated methods");
            }
        }
    }

    /**
     * Creates an Archetype from AST.
     * @param node syntax tree
     * @return constructed archetype
     * @throws GrammarException wrong tree
     */
    public static Archetype create(final SyntaxTree node) throws GrammarException {
        AssertNodeType.assertOneOf(node, "Archetype");
        TypeParameters parameters = new TypeParameters(
                node.find("TypeParameters.TypeParameter", TypeParameter.CONSTRUCT));
        String name = node.find("Identifier", SyntaxTree.VALUE).get(0);
        try {
            if ("SqlBuilder".equals(name)) {
                return new SqlArchetype(parameters);
            } else if ("QueryBuilder".equals(name)) {
                return new QueryArchetype(parameters);
            } else {
                throw new GrammarException("Unknown archetype: " + name, node);
            }
        } catch (ModelException e) {
            throw new GrammarException(e, node);
        }
    }

    /**
     * Determines whether a method is archetyped one.
     * @param method the method to check
     * @return true if archetyped
     */
    public static boolean isArchetypeMethod(final MethodDefinition method) {
        return method.getName().startsWith(ARCHETYPE_METHOD_PREFIX);
    }

    private static final class SqlArchetype extends Archetype {

        private SqlArchetype(final TypeParameters typeParameters) throws ModelException {
            super(typeParameters);
            if (!typeParameters.isEmpty()) {
                throw new ModelException("SqlBuilder archetype does not take type parameters, found: "
                        + typeParameters.size());
            }
        }

        @Override
        public MethodDefinition createArchetypeMethod(final InterfaceDefinition interfaceDefinition) {
            return MethodDefinition.parseAbstract(
                    String.format(SQL_METHOD_FORMAT, interfaceDefinition.getName(),
                            interfaceDefinition.getName(), ""), interfaceDefinition);
        }

        @Override
        public List<String> getRequiredImports() {
            return Arrays.asList("import org.symqle.common.SqlContext;", "import org.symqle.common.SqlBuilder;");
        }

    }

    private static final class QueryArchetype extends Archetype {

        private QueryArchetype(final TypeParameters typeParameters) throws ModelException {
            super(typeParameters);
            if (typeParameters.size() != 1) {
                throw new ModelException("Query archetype requires 1 type parameter, found: " + typeParameters.size());
            }

        }

        @Override
        public MethodDefinition createArchetypeMethod(final InterfaceDefinition interfaceDefinition) {
            return MethodDefinition.parseAbstract(
                    String.format(QUERY_METHOD_FORMAT, interfaceDefinition.getName(),
                            getTypeParameters(),
                            interfaceDefinition.getName(), ""), interfaceDefinition);
        }

        @Override
        public List<String> getRequiredImports() {
            return Arrays.asList("import org.symqle.common.SqlContext;", "import org.symqle.common.QueryBuilder;");
        }
    }

    private static final String ARCHETYPE_METHOD_PREFIX = "z$sqlOf";


    private static final String QUERY_METHOD_FORMAT = Utils.indent(4,
            "/**",
            "* Creates a QueryBuilder, which constructs SQL conforming to {@link %s}.",
            "* @param context the Sql construction context",
            "* @return constructed QueryBuilder",
            "*/",
            "QueryBuilder%s " + ARCHETYPE_METHOD_PREFIX + "%s(%sSqlContext context);"
            );

    private static final String SQL_METHOD_FORMAT = Utils.indent(4,
            "/**",
            "* Creates an SqlBuilder, which constructs SQL conforming to {@link %s}.",
            "* @param context the Sql construction context",
            "* @return constructed SqlBuilder",
            "*/",
            "SqlBuilder " + ARCHETYPE_METHOD_PREFIX + "%s(%sSqlContext context);"
    );

    /**
     * No archetype.
     */
    public static final Archetype NONE = new Archetype(new TypeParameters(Collections.<TypeParameter>emptyList())) {

        @Override
        public MethodDefinition createArchetypeMethod(final InterfaceDefinition interfaceDefinition) {
            return null;
        }

        @Override
        public List<String> getRequiredImports() {
            return Collections.emptyList();
        }
    };

}
