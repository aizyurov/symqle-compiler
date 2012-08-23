/*
* Copyright Alexander Izyurov 2010
*/
package org.simqle.processor;

import org.simqle.model.*;
import org.simqle.parser.SyntaxTree;
import org.simqle.util.Utils;

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
                    final MethodDeclaration valueMethod = makeScalarMethod(definition.getScalarTypeArgument().getImage(), interfaceName);
                        try {
                            body.addMethod(valueMethod);
                        } catch (ModelException e) {
                            throw new GrammarException("Method \"value\" cannot be defined explicitly", interfaceDeclaration);
                        }
                }

                final MethodDeclaration prepareMethod = makePrepareMethod(interfaceName);
                try {
                    body.addMethod(prepareMethod);
                } catch (ModelException e) {
                    throw new GrammarException("Method \"z$prepare$"+interfaceName+"\" cannot be defined explicitly", interfaceDeclaration);
                }

                final MethodDeclaration methodToAdd = definition.isQuery() ?
                        makeQueryMethod(definition.getQueryTypeParameter().getImage(), interfaceName) :
                        makeSqlMethod(interfaceName);
                try {
                    body.addMethod(methodToAdd);
                } catch (ModelException e) {
                    throw new GrammarException("Method \"z$create$"+interfaceName+"\" cannot be defined explicitly", interfaceDeclaration);
                }
                try {
                    model.addInterface(definition);
                } catch (ModelException e) {
                    throw new GrammarException(e.getMessage(), interfaceDeclaration);
                }
            }
        }
    }

    private final static String SCALAR_METHOD_FORMAT = Utils.join(8,
            "/**",
            "* Converts data from row element to Java object of type %s",
            "* @param element row element containing the data",
            "* @return object of type %s, may be null",
            "*/",
            "%s value(final Element element);"
            );

    public static MethodDeclaration makeScalarMethod(String typeParameter, String interfaceName) throws GrammarException {
        final String methodSource = String.format(SCALAR_METHOD_FORMAT, typeParameter, typeParameter, typeParameter, interfaceName);
        return MethodDeclaration.parseAbstractMethod(methodSource);
    }

    private final static String PREPARE_METHOD_FORMAT = Utils.join(8,
            "/**",
            "* Prepares SQL context for construction of %s Sql clause",
            "* @param context the Sql construction context",
            "*/",
            "void z$prepare$%s(final SqlContext context);"
            );

    private MethodDeclaration makePrepareMethod(String interfaceName) throws GrammarException {
        final String methodSource = String.format(PREPARE_METHOD_FORMAT, interfaceName, interfaceName);
        return MethodDeclaration.parseAbstractMethod(methodSource);
    }

    private final static String QUERY_METHOD_FORMAT = Utils.join(8,
            "/**",
            "* Creates a Query",
            "* @param context the Sql construction context",
            "* @return query conforming to <code>this</code> syntax",
            "*/",
            "Query<%s> z$create$%s(final SqlContext context);"
            );

    private MethodDeclaration makeQueryMethod(String typeParameter, String interfaceName) throws GrammarException {
        final String methodSource = String.format(QUERY_METHOD_FORMAT, typeParameter, interfaceName);
        return MethodDeclaration.parseAbstractMethod(methodSource);
    }

    private final static String SQL_METHOD_FORMAT = Utils.join(8,
            "/**",
            "* Creates an Sql representing <code>this</code>",
            "* @param context the Sql construction context",
            "* @return sql conforming to <code>this</code> syntax",
            "*/",
            "Sql z$create$%s(final SqlContext context);"
    );


    private MethodDeclaration makeSqlMethod(String interfaceName) throws GrammarException {
        final String methodSource = String.format(SQL_METHOD_FORMAT, interfaceName);
        return MethodDeclaration.parseAbstractMethod(methodSource);
    }
}