/*
* Copyright Alexander Izyurov 2010
*/
package org.simqle.processor;

import org.simqle.model.*;
import org.simqle.parser.SyntaxTree;
import org.simqle.util.Utils;

import java.util.Collection;
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
                "public abstract class Simqle {" + Utils.LINE_BREAK +
                "    public static Simqle get() { " + Utils.LINE_BREAK +
                "        return new SimqleGeneric(); " + Utils.LINE_BREAK +
                "    }" + Utils.LINE_BREAK +
                "}");
        final ClassDefinition simqleGeneric = ClassDefinition.parse("public class SimqleGeneric extends Simqle {}");

        try {
            model.addClass(simqle);
            model.addClass(simqleGeneric);
        } catch (ModelException e) {
            throw new RuntimeException("Internal error", e);
        }

        for (SyntaxTree production: tree.find("SimqleDeclarationBlock.SimqleDeclaration.ProductionDeclaration.ProductionChoice.ProductionRule")) {
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
                final Collection<MethodDefinition> anonymousClassAllMethods = anonymousClass.getAllMethods(model);
                for (MethodDefinition method: anonymousClassAllMethods) {
                    // implement non-implemented methods
                    final Set<String> modifiers = method.getOtherModifiers();
                    if (modifiers.contains("transient") && modifiers.contains("abstract")) {
                        // must implement
                        final String delegationCall;
                        if (Archetype.isArchetypeMethod(method)) {
                            delegationCall = delegateArchetypeMethod(model, productionRule, method);
                        } else {
                            delegationCall = delegateToLeftmostArg(model, productionRule, method);

                        }
                        StringBuilder bodyBuilder = new StringBuilder();
                        bodyBuilder.append(" {").append(Utils.LINE_BREAK);
                        bodyBuilder.append("                ");
                        if (!method.getResultType().equals(Type.VOID)) {
                            bodyBuilder.append("return ");
                        }
                        bodyBuilder.append(delegationCall).append(";").append(Utils.LINE_BREAK);
                        bodyBuilder.append("            }/*delegation*/");
                        method.implement("public", bodyBuilder.toString(), true);
                    }
                    // dow we can add the implementation of the method to SimqleGeneric
                    final MethodDefinition methodToImplement = simqleGeneric.getMethodBySignature(methodDefinition.signature(), model);
                    methodToImplement.implement("    public",
                            " { " +  Utils.LINE_BREAK +
                                    "        return new "+methodToImplement.getResultType()+"()" +
                            anonymousClass.instanceBodyAsString() + ";/*anonymous*/"+ Utils.LINE_BREAK +
                            "    }/*rule method*/"+Utils.LINE_BREAK,
                            true);

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

    private String delegateArchetypeMethod(final Model model, final ProductionRule productionRule, final MethodDefinition method) throws ModelException {
        StringBuilder builder = new StringBuilder();
        // either new CompositeSql or new CommpositeQuery<T>
        builder.append("new Composite").
            append(method.getResultType())
                .append("(")
                .append(Utils.format(productionRule.getElements(), "", ", ", "", new F<ProductionRule.RuleElement, String, ModelException>() {
                    @Override
                    public String apply(final ProductionRule.RuleElement ruleElement) throws ModelException {
                        return ruleElement.asMethodArgument(model);
                    }
                }))
                .append(")");
        return builder.toString();
    }

/**
 * new F<FormalParameter, String, ModelException>() {
                    @Override
                    public String apply(FormalParameter formalParameter) throws ModelException {
                       return  model.getInterface(formalParameter.getType()).getArchetypeMethod().invoke(formalParameter.getName());
                    }
                }))
  */
}
