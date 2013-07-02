package org.symqle.processor;

import org.symqle.model.ClassDefinition;
import org.symqle.model.MethodDefinition;
import org.symqle.model.Model;
import org.symqle.model.ModelException;
import org.symqle.parser.SyntaxTree;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: aizyurov
 * Date: 02.12.2012
 * Time: 10:59:32
 * To change this template use File | Settings | File Templates.
 */
public class SymqleMethodProcessor implements Processor {

    @Override
    public void process(SyntaxTree tree, Model model) throws GrammarException {
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
                symqle.addMethod(method);
                // private and protected methods are not ExplicitMethods
                // protected make no sense because Symqle is final
                if (!method.getAccessModifier().equals("private") && !method.getAccessModifier().equals("protected"))
                {
                    model.addExplicitMethod(symqle.getDeclaredMethodBySignature(method.signature()), declarationImports);
                }
            } catch (ModelException e) {
                throw new GrammarException(e, methodNode);
            }
            symqle.addImportLines(declarationImports);
        }
    }
}
