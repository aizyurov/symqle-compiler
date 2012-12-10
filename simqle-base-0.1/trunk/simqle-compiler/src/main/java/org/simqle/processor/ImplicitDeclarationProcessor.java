package org.simqle.processor;

import org.simqle.model.*;
import org.simqle.parser.SyntaxTree;

/**
 * Created by IntelliJ IDEA.
 * User: aizyurov
 * Date: 09.12.2012
 * Time: 20:03:35
 * To change this template use File | Settings | File Templates.
 */
public class ImplicitDeclarationProcessor implements Processor {

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

        for (SyntaxTree methodNode: tree.find("SimqleDeclarationBlock.SimqleDeclaration.ImplicitConversionDeclaration")) {
            final Type targetType = methodNode.find("ClassOrInterfaceType", Type.CONSTRUCT).get(0);
            final TypeParameters typeParameters =
                    new TypeParameters(methodNode.find("TypeParameters.TypeParameter", TypeParameter.CONSTRUCT));
            final FormalParameter argument = methodNode.find("FormalParameter", FormalParameter.CONSTRUCT).get(0);
            final String name = "z$" + targetType.getSimpleName() +"$from$" +argument.getType().getSimpleName();
            final String methodBody = methodNode.find("MethodBody").get(0).getImage();

            StringBuilder builder = new StringBuilder();
            builder.append(methodNode.getComments())
                    .append(typeParameters)
                    .append(" ")
                    .append(targetType)
                    .append(" ")
                    .append(name)
                    .append("(")
                    .append(argument)
                    .append(")")
                    .append(methodBody);
            MethodDefinition method = MethodDefinition.parse(builder.toString(), simqleGeneric);
            try {
                simqleGeneric.addMethod(method);
                method.pullUpAbstractMethod(simqle);
                model.addImplicitMethod(method);
            } catch (ModelException e) {
                throw new GrammarException(e, methodNode);
            }
        }
    }
}