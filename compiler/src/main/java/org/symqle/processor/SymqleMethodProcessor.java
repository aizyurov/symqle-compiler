package org.symqle.processor;

import org.symqle.model.ClassDefinition;
import org.symqle.model.MethodDefinition;
import org.symqle.model.Model;
import org.symqle.model.ModelException;
import org.symqle.parser.SyntaxTree;

import java.util.List;

/**
 * Read "standalone" (not production rule related) methods from syntax trees and put to model.
 * To change this template use File | Settings | File Templates.
 */
public class SymqleMethodProcessor extends SyntaxTreeProcessor {

    @Override
    protected final Processor predecessor() {
        return new ProductionProcessor();
    }

    @Override
    protected final void process(final SyntaxTree tree, final Model model) throws GrammarException {

        final ClassDefinition symqle;
        try {
            symqle = model.getClassDef("Symqle");
        } catch (ModelException e) {
            throw new IllegalStateException(e);
        }

        for (SyntaxTree methodNode: tree.find("SymqleDeclarationBlock.SymqleDeclaration.MethodDeclaration")) {
            MethodDefinition method = new MethodDefinition(methodNode, symqle);
            List<String> declarationImports = methodNode.find("^.^.ImportDeclaration", SyntaxTree.BODY);
            try {
                method.makeStatic();
                method.setSourceRef(methodNode);
                model.addExplicitMethod(method, null, declarationImports);
            } catch (ModelException e) {
                throw new GrammarException(e, methodNode);
            }
            symqle.addImportLines(declarationImports);
        }
    }
}
