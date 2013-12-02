/*
* Copyright Alexander Izyurov 2010
*/
package org.symqle.processor;

import org.symqle.model.*;
import org.symqle.parser.ParseException;
import org.symqle.parser.SyntaxTree;
import org.symqle.util.Utils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <br/>20.11.2011
 *
 * @author Alexander Izyurov
 */
public class DialectCompilationProcessor implements Processor {


    @Override
    public void process(final SyntaxTree tree, final Model model) throws GrammarException {

        final InterfaceDefinition dialect;
        final ClassDefinition genericDialect;
        try {
            dialect = model.getInterface("Dialect");
            genericDialect = model.getClassDef("GenericDialect");
        } catch (ModelException e) {
            throw new IllegalStateException("Internal error", e);
        }

        for (SyntaxTree productionChoice: tree.find("SymqleDeclarationBlock.SymqleDeclaration.ProductionDeclaration.ProductionChoice")) {
            // must be exactrly one ProductionRule
            final SyntaxTree productionRuleNode = productionChoice.find("ProductionRule").get(0);
            ProductionRule productionRule = new ProductionRule(productionRuleNode);
            MethodDefinition dialectMethod = createDialectMethod(productionRule, dialect);
            try {
                dialect.addMethod(dialectMethod);
                final MethodDefinition genericDialectMethod = dialectMethod.override(genericDialect, model);
                genericDialectMethod.implement("public", " {" + Utils.LINE_BREAK +
                        "        return concat(" + productionRule.asMethodArguments() + ");" + Utils.LINE_BREAK +
                        "    }", true, false
                );
            } catch (ModelException e) {
                throw new GrammarException(e, productionRuleNode);
            }
            model.addRule(productionRule.getTargetTypeName(), productionRule.getShortRule());
        }
    }

    private MethodDefinition createDialectMethod(ProductionRule rule, InterfaceDefinition dialect) {
        return MethodDefinition.parseAbstract(rule.asAbstractMethodDeclaration() + ";", dialect);
    }
}