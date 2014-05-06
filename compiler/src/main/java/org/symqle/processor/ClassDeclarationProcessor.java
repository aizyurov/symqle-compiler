/*
* Copyright Alexander Izyurov 2010
*/
package org.symqle.processor;

import org.symqle.model.ClassDefinition;
import org.symqle.model.Model;
import org.symqle.model.ModelException;
import org.symqle.parser.SyntaxTree;

/**
 * <br/>19.11.2011
 *
 * @author Alexander Izyurov
 */
public class ClassDeclarationProcessor extends SyntaxTreeProcessor {

    @Override
    protected Processor predecessor() {
        return new InterfaceDeclarationsProcessor();
    }

    @Override
    protected void process(SyntaxTree tree, Model model) throws GrammarException {
        for (SyntaxTree classDeclarationNode: tree.find("SymqleDeclarationBlock.SymqleDeclaration.NormalClassDeclaration")) {
            ClassDefinition definition = new ClassDefinition(classDeclarationNode);
            try {
                model.addClass(definition);
            } catch (ModelException e) {
                throw new GrammarException(e, classDeclarationNode);
            }
        }
    }
}
