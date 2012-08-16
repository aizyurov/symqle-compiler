/*
* Copyright Alexander Izyurov 2010
*/
package org.simqle.processor;

import org.simqle.model.*;
import org.simqle.parser.ParseException;
import org.simqle.parser.SimpleNode;
import org.simqle.parser.SyntaxTree;

import java.util.ArrayList;
import java.util.List;

/**
 * <br/>13.11.2011
 *
 * @author Alexander Izyurov
 */
public class InterfaceDeclarationsProcessor implements Processor {

    public void process(SyntaxTree tree, Model model) throws GrammarException {
        for (SyntaxTree block: tree.find("SimqleDeclarationBlock")) {
            final List<SyntaxTree> simqleInterfaceDeclarations =
                    block.find("SimqleDeclaration.SimqleInterfaceDeclaration");
            if (!simqleInterfaceDeclarations.isEmpty()) {
                List<String> importLines = new ArrayList<String>();
                for (SyntaxTree importDeclaration : block.find("ImportDeclaration")) {
                    importLines.add(importDeclaration.getBody());
                }
                final SyntaxTree interfaceDeclaration =
                        simqleInterfaceDeclarations.get(0);
                final InterfaceDefinition definition = new InterfaceDefinition(interfaceDeclaration, importLines);
                final Body body = definition.getBody();
                final String interfaceName = definition.getName();
                if (definition.isScalar()) {
                    final MethodDeclaration valueMethod = makeScalarMethod(definition.getScalarTypeArgument().getValue(), interfaceName);
                        try {
                            body.addMethod(valueMethod);
                        } catch (ModelException e) {
                            throw new GrammarException(e.getMessage(), interfaceDeclaration);
                        }
                        definition.addImportLine(SCALAR_REQUIRED_IMPORT);
                }

                final MethodDeclaration prepareMethod = makePrepareMethod(interfaceName);
                try {
                    body.addMethod(prepareMethod);
                } catch (ModelException e) {
                    throw new GrammarException(e.getMessage(), interfaceDeclaration);
                }

                definition.addImportLine(PREPARE_REQUIRED_IMPORT);

                if (definition.isQuery()) {
                    try {
                        body.addMethod(makeQueryMethod(definition.getQueryTypeParameter().getImage(), interfaceName));
                    } catch (ModelException e) {
                        throw new GrammarException(e.getMessage(), interfaceDeclaration);
                    }
                    definition.addImportLine(QUERY_REQUIRED_IMPORT);
                } else  {
                    try {
                        body.addMethod(makeSqlMethod(interfaceName));
                    } catch (ModelException e) {
                        throw new GrammarException(e.getMessage(), interfaceDeclaration);
                    }
                    definition.addImportLine(SQL_REQUIRED_IMPORT);
                }
                try {
                    model.addInterface(definition);
                } catch (ModelException e) {
                    throw new GrammarException(e.getMessage(), interfaceDeclaration);
                }
            }
        }
    }

    private final static String SCALAR_REQUIRED_IMPORT = "import org.simqle.Element;";
    private final static String SCALAR_METHOD_COMMENT_FORMAT = Utils.join(8,
            "/**",
            "* Converts data from row element to Java object of type %s",
            "* @param element row element containing the data",
            "* @return object of type %s, may be null",
            "*/",
            "%s value(Element element);"
            );

    public static MethodDeclaration makeScalarMethod(String typeParameter, String interfaceName) {
        final String methodSource = String.format(SCALAR_METHOD_COMMENT_FORMAT, typeParameter, typeParameter, typeParameter, interfaceName);
        try {
            final SimpleNode node = Utils.createParser(methodSource).AbstractMethodDeclaration();
            return new MethodDeclaration(new SyntaxTree(node, "SCALAR_METHOD_COMMENT_FORMAT"));
        } catch (ParseException e) {
            throw new RuntimeException("Internal Error", e);
        } catch (GrammarException e) {
            throw new RuntimeException("Internal Error", e);
        }
    }

    private final static String PREPARE_REQUIRED_IMPORT = "import org.simqle.SqlContext;";
    private final static String PREPARE_METHOD_COMMENT_FORMAT = Utils.join(8,
            "/**",
            "* Prepares SQL context for construction of %s Sql clause",
            "* @param context the Sql construction context",
            "*/",
            "void z$prepare$%s(SqlContext context);"
            );

    private MethodDeclaration makePrepareMethod(String interfaceName) {
        final String methodSource = String.format(PREPARE_METHOD_COMMENT_FORMAT, interfaceName, interfaceName);
        try {
            final SimpleNode node = Utils.createParser(methodSource).AbstractMethodDeclaration();
            return new MethodDeclaration(new SyntaxTree(node, "PREPARE_METHOD_COMMENT_FORMAT"));
        } catch (ParseException e) {
            throw new RuntimeException("Internal Error", e);
        } catch (GrammarException e) {
            throw new RuntimeException("Internal Error", e);
        }
    }

    private final static String QUERY_REQUIRED_IMPORT = "import org.simqle.Query;";
    private final static String QUERY_METHOD_COMMENT_FORMAT = Utils.join(8,
            "/**",
            "* Creates a Query",
            "* @param context the Sql construction context",
            "* @return query conforming to <code>this</code> syntax",
            "*/",
            "Query<%s> z$create$%s(SqlContext context);"
            );

    private MethodDeclaration makeQueryMethod(String typeParameter, String interfaceName) {
        final String methodSource = String.format(QUERY_METHOD_COMMENT_FORMAT, typeParameter, interfaceName);
        try {
            final SimpleNode node = Utils.createParser(methodSource).AbstractMethodDeclaration();
            return new MethodDeclaration(new SyntaxTree(node, "QUERY_METHOD_COMMENT_FORMAT"));
        } catch (ParseException e) {
            throw new RuntimeException("Internal Error", e);
        } catch (GrammarException e) {
            throw new RuntimeException("Internal Error", e);
        }
    }

    private final static String SQL_REQUIRED_IMPORT = "import org.simqle.Sql;";
    private final static String SQL_METHOD_COMMENT_FORMAT = Utils.join(8,
            "/**",
            "* Creates an Sql representing <code>this</code>",
            "* @param context the Sql construction context",
            "* @return sql conforming to <code>this</code> syntax",
            "*/",
            "Sql z$create$%s(SqlContext context);"
            );


    private MethodDeclaration makeSqlMethod(String interfaceName) {
        final String methodSource = String.format(SQL_METHOD_COMMENT_FORMAT, interfaceName);
        try {
            final SimpleNode node = Utils.createParser(methodSource).AbstractMethodDeclaration();
            return new MethodDeclaration(new SyntaxTree(node,"SQL_METHOD_COMMENT_FORMAT"));
        } catch (ParseException e) {
            throw new RuntimeException("Internal Error", e);
        } catch (GrammarException e) {
            throw new RuntimeException("Internal Error", e);
        }

    }
}