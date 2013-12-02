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
    public boolean process(SyntaxTree tree, Model model) throws GrammarException {

        final ClassDefinition symqleTemplate = model.getSymqleTemplate();
        for (SyntaxTree methodNode: tree.find("SymqleDeclarationBlock.SymqleDeclaration.MethodDeclaration")) {
            MethodDefinition method = new MethodDefinition(methodNode, symqleTemplate);
            List<String> declarationImports = methodNode.find("^.^.ImportDeclaration", SyntaxTree.BODY);
            try {
                method.makeStatic();
                symqleTemplate.addMethod(method);
                method.setSourceRef(methodNode);
                model.addExplicitMethod(symqleTemplate.getDeclaredMethodBySignature(method.signature()), null, declarationImports);
            } catch (ModelException e) {
                throw new GrammarException(e, methodNode);
            }
            symqleTemplate.addImportLines(declarationImports);
        }
        return true;
    }
}
