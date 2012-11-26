/*
* Copyright Alexander Izyurov 2010
*/
package org.simqle.processor;

import org.simqle.model.ClassDefinition;
import org.simqle.model.MethodDefinition;
import org.simqle.model.Model;
import org.simqle.model.ModelException;
import org.simqle.parser.SyntaxTree;

import java.util.HashMap;
import java.util.Map;

/**
 * <br/>19.11.2011
 *
 * @author Alexander Izyurov
 */
public class ClassDeclarationProcessor implements Processor {

    public void process(SyntaxTree tree, Model model) throws GrammarException {
        final Map<String, SyntaxTree> nodeByName = new HashMap<String, SyntaxTree>();
        for (SyntaxTree classDeclarationNode: tree.find("SimqleDeclarationBlock.SimqleDeclaration.NormalClassDeclaration")) {
            ClassDefinition definition = new ClassDefinition(classDeclarationNode);
            try {
                model.addClass(definition);
                nodeByName.put(definition.getName(), classDeclarationNode);
            } catch (ModelException e) {
                throw new GrammarException(e.getMessage(), classDeclarationNode);
            }
        }

        for (ClassDefinition classDefinition: model.getAllClasses()) {
            try {
                addAbstractMethods(classDefinition, model);
            } catch (ModelException e) {
                throw new GrammarException(e.getMessage(), nodeByName.get(classDefinition.getName()));
            }
        }
    }

    private void addAbstractMethods(final ClassDefinition definition, final Model model) throws ModelException {

        for (final MethodDefinition method: definition.getAllMethods(model)) {
            if (method.getOtherModifiers().contains("transient") && method.getOtherModifiers().contains("abstract")) {
                method.declareAbstract("public");
            }
        }
    }


}
