/*
* Copyright Alexander Izyurov 2010
*/
package org.symqle.processor;

import org.symqle.model.ClassDefinition;
import org.symqle.model.InterfaceDefinition;
import org.symqle.model.MethodDefinition;
import org.symqle.model.Model;
import org.symqle.model.ModelException;
import org.symqle.model.ProductionRule;
import org.symqle.parser.SyntaxTree;
import org.symqle.util.Utils;

/**
 * <br/>20.11.2011
 *
 * @author Alexander Izyurov
 */
public class DialectCompilationProcessor implements Processor {


    @Override
    public boolean process(final SyntaxTree tree, final Model model) throws GrammarException {

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
        return true;
    }

    private MethodDefinition createDialectMethod(ProductionRule rule, InterfaceDefinition dialect) {
        return MethodDefinition.parseAbstract(rule.asAbstractMethodDeclaration() + ";", dialect);
    }
}