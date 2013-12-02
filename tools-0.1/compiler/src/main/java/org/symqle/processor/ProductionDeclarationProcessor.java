/*
* Copyright Alexander Izyurov 2010
*/
package org.symqle.processor;

import org.symqle.model.*;
import org.symqle.parser.SyntaxTree;

import java.util.List;

/**
 * <br/>20.11.2011
 *
 * @author Alexander Izyurov
 */
public class ProductionDeclarationProcessor implements Processor {

    @Override
    public boolean process(final SyntaxTree tree, final Model model) throws GrammarException {

        final ClassDefinition symqleTemplate = model.getSymqleTemplate();
        for (SyntaxTree productionImplNode: tree.find("SymqleDeclarationBlock.SymqleDeclaration.ProductionDeclaration.ProductionChoice.ProductionImplementation")) {
            final List<String> declarationImports = productionImplNode.find("^.^.^.^.ImportDeclaration", SyntaxTree.BODY);
            symqleTemplate.addImportLines(declarationImports);
            final List<String> implementationImports = productionImplNode.find("ImportDeclaration", SyntaxTree.BODY);
                // implementation only
            symqleTemplate.addImportLines(implementationImports);
            final ProductionImplementation productionImpl = new ProductionImplementation(productionImplNode);
            for (FormalParameter formalParameter: productionImpl.getFormalParameters()) {
                try {
                    model.getInterface(formalParameter.getType());
                } catch (ModelException e) {
                    throw new GrammarException(e, productionImplNode);
                }
            }
            String staticMethodDeclaration =
                    productionImpl.getComment() +
                    productionImpl.asStaticMethodDeclaration();

            final AnonymousClass anonymousClass = new AnonymousClass(productionImplNode, productionImpl.getImplementationType());

            // create abstract method for symqleTemplate
            MethodDefinition methodDefinition = MethodDefinition.parse("abstract "+staticMethodDeclaration +";", symqleTemplate);
            try {
                symqleTemplate.addMethod(methodDefinition);
                if (productionImpl.isImplicit()) {
                    model.addImplicitMethod(methodDefinition, anonymousClass);
                } else {
                    model.addExplicitMethod(methodDefinition, anonymousClass, implementationImports);
                }
                    // associate with Dialect method
                    final SyntaxTree productionRuleNode = productionImplNode.find("^.ProductionRule").get(0);
                    final ProductionRule productionRule = new ProductionRule(productionRuleNode);
                    final String dialectName = productionRule.getName();
                    model.associateDialectName(dialectName, methodDefinition);
            } catch (ModelException e) {
                throw new GrammarException(e, productionImplNode);
            }
        } // for ProductionImplNode
        return true;
    }


}
