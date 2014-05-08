/*
* Copyright Alexander Izyurov 2010
*/
package org.symqle.processor;

import org.symqle.model.ClassDefinition;
import org.symqle.model.Model;
import org.symqle.model.ModelException;
import org.symqle.parser.SyntaxTree;

import java.util.List;

/**
 * Reads class declarations from source and puts into the model.
 * Requires: all interfaces in the model.
 * @author Alexander Izyurov
 */
public class ClassDeclarationProcessor extends SyntaxTreeProcessor {

    @Override
    protected final Processor predecessor() {
        return new InterfaceDeclarationsProcessor();
    }

    @Override
    protected final void process(final SyntaxTree tree, final Model model) throws GrammarException {
        final List<SyntaxTree> syntaxTrees =
                tree.find("SymqleDeclarationBlock.SymqleDeclaration.NormalClassDeclaration");
        for (SyntaxTree classDeclarationNode: syntaxTrees) {
            ClassDefinition definition = new ClassDefinition(classDeclarationNode);
            try {
                model.addClass(definition);
            } catch (ModelException e) {
                throw new GrammarException(e, classDeclarationNode);
            }
        }
    }
}
