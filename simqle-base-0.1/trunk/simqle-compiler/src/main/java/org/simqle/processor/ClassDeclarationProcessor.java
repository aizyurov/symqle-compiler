/*
* Copyright Alexander Izyurov 2010
*/
package org.simqle.processor;

import org.simqle.model.*;
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
                throw new GrammarException(e, classDeclarationNode);
            }
        }

    }

}
