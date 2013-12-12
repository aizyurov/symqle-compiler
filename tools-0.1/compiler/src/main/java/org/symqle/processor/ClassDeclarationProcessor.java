/*
* Copyright Alexander Izyurov 2010
*/
package org.symqle.processor;

import org.symqle.model.ClassDefinition;
import org.symqle.model.F;
import org.symqle.model.FormalParameter;
import org.symqle.model.MethodDefinition;
import org.symqle.model.Model;
import org.symqle.model.ModelException;
import org.symqle.model.Type;
import org.symqle.parser.SyntaxTree;
import org.symqle.util.Utils;

import static org.symqle.util.Utils.LINE_BREAK;

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

    private void finalizeClass(Model model, ClassDefinition classDefinition) throws ModelException {
        StringBuilder javadocBuilder = new StringBuilder();
        javadocBuilder.append("/**").append(LINE_BREAK);
        javadocBuilder.append(" * Basic implementation of interface methods." ).append(LINE_BREAK);
        javadocBuilder.append(" * Subclasses must implement:").append(LINE_BREAK);
        javadocBuilder.append(" *<ul>").append(LINE_BREAK);
        boolean hasAbstractMethods = false;
        for (final MethodDefinition method: classDefinition.getAllMethods(model)) {
            if (method.getOtherModifiers().contains("volatile") && method.getOtherModifiers().contains("abstract")) {
                method.declareAbstract("public");
                javadocBuilder.append(" * <li>{@link #").append(method.getName());
                javadocBuilder.append(Utils.format(method.getFormalParameters(), "(", ", ", ")", new F<FormalParameter, String, RuntimeException>() {
                    @Override
                    public String apply(final FormalParameter formalParameter) {
                        return formalParameter.getType().getSimpleName();
                    }
                }));
                javadocBuilder.append("}</li>").append(LINE_BREAK);
                hasAbstractMethods = true;
            }
        }
        ;
        javadocBuilder.append(" *</ul>").append(LINE_BREAK);
        javadocBuilder.append(" */").append(LINE_BREAK);
        if (hasAbstractMethods) {
            classDefinition.replaceComment(javadocBuilder.toString());
        }
        for (Type type : classDefinition.getImplementedInterfaces()) {
            classDefinition.addImportLines(model.getInterface(type).getImportLines());
        }
    }

}
