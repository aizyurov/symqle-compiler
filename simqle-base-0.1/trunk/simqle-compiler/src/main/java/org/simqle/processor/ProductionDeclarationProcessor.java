/*
* Copyright Alexander Izyurov 2010
*/
package org.simqle.processor;

import org.simqle.model.*;
import org.simqle.parser.SyntaxTree;
import org.simqle.util.Utils;

import java.util.List;
import java.util.Set;

/**
 * <br/>20.11.2011
 *
 * @author Alexander Izyurov
 */
public class ProductionDeclarationProcessor implements Processor {
    @Override
    public void process(final SyntaxTree tree, final Model model) throws GrammarException {
        final ClassDefinition simqle = ClassDefinition.parse(
                "public abstract class Simqle {" +
                "    public static get() { " +
                "        return new SimqleGeneric(); " +
                "    }" +
                "}");
        final ClassDefinition simqleGeneric = ClassDefinition.parse("public abstract class SimqleGeneric extends Simqle {}");

        for (SyntaxTree production: tree.find("ProductionDeclaration.ProductionChoice")) {
                // create the ProductionRule
            ProductionRule productionRule = new ProductionRule(production);
                // create an anonymous class
            AnonymousClass anonymousClass = new AnonymousClass(production);
                // create abstract method for Simqle class. Register method as explicit or implicit.
            String abstractMethodDeclaration = "public "+productionRule.asAbstractMethodDeclaration()+";";
            final MethodDefinition methodDefinition = MethodDefinition.parseAbstract(abstractMethodDeclaration, simqle);
            try {
                simqle.addMethod(methodDefinition);
                if (productionRule.isImplicit()) {
                    model.addImplicitMethod(methodDefinition);
                } else {
                    model.addExplicitMethod(methodDefinition);
                }
            } catch (ModelException e) {
                throw new GrammarException(e, production);
            }
            try {
// create implementing method for SimqleGeneric class (uses the anonymous class and the rule)
                // first implement all non-implemented methods in the class
                for (MethodDefinition method: anonymousClass.getAllMethods(model)) {
                    // implement non-implemented methods
                    final Set<String> modifiers = method.getOtherModifiers();
                    if (modifiers.contains("transient") && modifiers.contains("abstract")) {
                        // must implement
                        final String delegationCall;
                        if (Archetype.isArchetypeMethod(method)) {
                            delegationCall = delegateArchetypeMethod(model, method, productionRule.getFormalParameters());
                        } else {
                            delegationCall = delegateToLeftmostArg(model, productionRule, method);

                        }
                        StringBuilder bodyBuilder = new StringBuilder();
                        bodyBuilder.append(" {");
                        bodyBuilder.append("            ");
                        if (!method.getResultType().equals(Type.VOID)) {
                            bodyBuilder.append("return ");
                        }
                        bodyBuilder.append(delegationCall).append(";");
                        bodyBuilder.append("        {");

                    }
                }
            } catch (ModelException e) {
                throw new GrammarException(e, production);
            }
        }

    }

    private String delegateToLeftmostArg(final Model model, final ProductionRule productionRule, final MethodDefinition method) throws ModelException {
        // delegate to leftmost argument
        final List<FormalParameter> formalParameters = productionRule.getFormalParameters();
        if (!formalParameters.isEmpty()) {
            final FormalParameter formalParameter = formalParameters.get(0);
            final InterfaceDefinition anInterface = model.getInterface(formalParameter.getType());
            final MethodDefinition delegate = anInterface.getDeclaredMethodBySignature(method.signature());
            return delegate.invoke(formalParameter.getName());
        } else {
            throw new ModelException("Cannot implement " + method.getName());
        }
    }

    private String delegateArchetypeMethod(final Model model, final MethodDefinition method, final List<FormalParameter> formalParameters) throws ModelException {
        StringBuilder builder = new StringBuilder();
        builder.append(" {"+ Utils.LINE_BREAK);
        // either new CompositeSql or new CommpositeQuery<T>
        builder.append("            return new Composite").
            append(method.getResultType())
                .append("(")
                .append(Utils.format(formalParameters, "", ", ", "", new F<FormalParameter, String, ModelException>() {
                    @Override
                    public String apply(FormalParameter formalParameter) throws ModelException {
                       return  model.getInterface(formalParameter.getType()).getArchetypeMethod().invoke(formalParameter.getName());
                    }
                }))
                .append(");");
                builder.append(Utils.LINE_BREAK).append("        }").append(Utils.LINE_BREAK);
        return builder.toString();
    }
}
