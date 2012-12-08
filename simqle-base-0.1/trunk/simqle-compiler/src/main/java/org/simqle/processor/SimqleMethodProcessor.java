package org.simqle.processor;

import org.simqle.model.ClassDefinition;
import org.simqle.model.MethodDefinition;
import org.simqle.model.Model;
import org.simqle.model.ModelException;
import org.simqle.parser.SyntaxTree;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: aizyurov
 * Date: 02.12.2012
 * Time: 10:59:32
 * To change this template use File | Settings | File Templates.
 */
public class SimqleMethodProcessor implements Processor {

    @Override
    public void process(SyntaxTree tree, Model model) throws GrammarException {
        final ClassDefinition simqle;
        final ClassDefinition simqleGeneric;
        try {
            simqle = model.getClassDef("Simqle");
            simqleGeneric = model.getClassDef("SimqleGeneric");
        } catch (ModelException e) {
            throw new IllegalStateException(e);
        }

        for (SyntaxTree methodNode: tree.find("SimqleDeclarationBlock.SimqleDeclaration.MethodDeclaration")) {
            MethodDefinition method = new MethodDefinition(methodNode, simqleGeneric);
            List<String> declarationImports = methodNode.find("^.^.ImportDeclaration", SyntaxTree.BODY);
            try {
                simqleGeneric.addMethod(method);
                // private methods are for SimqleGeneric only
                // protected can be overridden in dialects
                // only public and package scope go to Simqle
                if (!method.getAccessModifier().equals("private") && !method.getAccessModifier().equals("protected"))
                {
                    method.pullUpAbstractMethod(simqle);
                    model.addExplicitMethod(simqle.getDeclaredMethodBySignature(method.signature()), declarationImports);
                }
            } catch (ModelException e) {
                throw new GrammarException(e, methodNode);
            }
            simqle.addImportLines(declarationImports);
        }
    }
}
