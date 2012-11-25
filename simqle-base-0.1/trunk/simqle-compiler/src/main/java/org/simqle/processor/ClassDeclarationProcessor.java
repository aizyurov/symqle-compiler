/*
* Copyright Alexander Izyurov 2010
*/
package org.simqle.processor;

import org.simqle.model.*;
import org.simqle.parser.SyntaxTree;
import org.simqle.util.Utils;

import java.util.*;

/**
 * <br/>19.11.2011
 *
 * @author Alexander Izyurov
 */
public class ClassDeclarationProcessor implements Processor {

    public void process(SyntaxTree tree, Model model) throws GrammarException {
        for (SyntaxTree classDeclarationNode: tree.find("SimqleDeclarationBlock.SimqleDeclaration.NormalClassDeclaration")) {
            ClassDefinition definition = new BaseClassDefinition(classDeclarationNode);
            try {
                model.addClass(definition);
                addAbstractMethods(definition, model);
            } catch (ModelException e) {
                throw new GrammarException(e.getMessage(), classDeclarationNode);
            }
        }

        for (ClassDefinition classDefinition: model.getAllClasses()) {
            addAbstractMethods(classDefinition, model);
        }
    }

    private void addAbstractMethods(final ClassDefinition definition, final Model model) {

        for (Type parentType: definition.getImplementedInterfaces()) {

        }
        definition.getImplementedInterfaces();
    }


}
