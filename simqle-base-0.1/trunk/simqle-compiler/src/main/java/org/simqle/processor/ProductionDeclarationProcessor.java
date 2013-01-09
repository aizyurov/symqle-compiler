/*
* Copyright Alexander Izyurov 2010
*/
package org.simqle.processor;

import org.simqle.model.*;
import org.simqle.parser.SyntaxTree;
import org.simqle.util.Utils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <br/>20.11.2011
 *
 * @author Alexander Izyurov
 */
public class ProductionDeclarationProcessor implements Processor {


    @Override
    public void process(final SyntaxTree tree, final Model model) throws GrammarException {

        final ClassDefinition simqle;
        final ClassDefinition simqleGeneric;
        try {
            simqle = model.getClassDef("Simqle");
            simqleGeneric = model.getClassDef("SimqleGeneric");
        } catch (ModelException e) {
            throw new IllegalStateException("Internal error", e);
        }

        for (SyntaxTree production: tree.find("SimqleDeclarationBlock.SimqleDeclaration.ProductionDeclaration.ProductionChoice.ProductionRule")) {
                // create the ProductionRule
            ProductionRule productionRule = new ProductionRule(production);
            for (FormalParameter formalParameter: productionRule.getFormalParameters()) {
                try {
                    model.getInterface(formalParameter.getType());
                } catch (ModelException e) {
                    throw new GrammarException(e, production);
                }
            }
                // add imports
            final List<String> declarationImports = production.find("^.^.^.^.ImportDeclaration", SyntaxTree.BODY);
                // these go to both
            simqle.addImportLines(declarationImports);
            simqleGeneric.addImportLines(declarationImports);
            final List<String> implementationImports = production.find("^.ProductionImplementation.ImportDeclaration", SyntaxTree.BODY);
                // implementation only
            simqleGeneric.addImportLines(implementationImports);
                // create abstract method for Simqle class. Register method as explicit or implicit.
            String abstractMethodDeclaration =
                    productionRule.generatedComment() +
                    productionRule.asAbstractMethodDeclaration()+";";
            final MethodDefinition methodDefinition;
            try {
                methodDefinition = MethodDefinition.parse(abstractMethodDeclaration, simqle);
            } catch (RuntimeException e) {
                System.out.println(abstractMethodDeclaration);
                throw e;
            }
            try {
                if (!methodDefinition.getAccessModifier().equals("private")) {
                    simqle.addMethod(methodDefinition);
                    if (productionRule.isImplicit()) {
                        model.addImplicitMethod(methodDefinition);
                    } else {
                        model.addExplicitMethod(methodDefinition, declarationImports);
                    }
                }
            } catch (ModelException e) {
                throw new GrammarException(e, production);
            }
            // create implementing method in SimqleGeneric via anonymous class
            try {
                final AnonymousClass anonymousClass = new AnonymousClass(production);
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
                        } else if (method.getName().startsWith("get")
                                && method.getName().length() > 3
                                && method.getFormalParameters().size()==0) {
                            // "property getter"
                            final String methodName = method.getName();
                            String propertyName = methodName.substring(3,4).toLowerCase() +
                                    methodName.substring(4, methodName.length());
                            String fieldDeclarationSource = "private final " +
                                    method.getResultType() + " " + propertyName + " = " +
                                    callLeftmostArg(model, productionRule, method);
                            delegationCall = propertyGetter(propertyName);
                            anonymousClass.addFieldDeclaration(FieldDeclaration.parse(fieldDeclarationSource));
                        } else {
                            delegationCall = delegateToLeftmostArg(model, productionRule, method);
                        }
                        method.implement("public", delegationCall, true);
                    }
                }
                // dow we can add the implementation of the method to SimqleGeneric
                MethodDefinition methodToImplement = simqleGeneric.getMethodBySignature(methodDefinition.signature(), model);
                if (methodToImplement == null) {
                    methodToImplement = methodDefinition.override(simqleGeneric, model);
                }
                methodToImplement.implement(methodDefinition.getAccessModifier(),
                        " { " +  Utils.LINE_BREAK +
                                "        return new "+methodToImplement.getResultType()+"()" +
                        anonymousClass.instanceBodyAsString() + ";/*anonymous*/"+ Utils.LINE_BREAK +
                        "    }/*rule method*/"+Utils.LINE_BREAK,
                        true);
            } catch (ModelException e) {
                throw new GrammarException(e, production);
            }
        }

    }

    private String propertyGetter(String propertyName) {
        final StringBuilder bodyBuilder = new StringBuilder();
        bodyBuilder.append(" {").append(Utils.LINE_BREAK);
        bodyBuilder.append("                ");
        bodyBuilder.append("return ").append(propertyName).append(";");
        bodyBuilder.append("            }");
        return bodyBuilder.toString();
    }

    private String delegateToLeftmostArg(final Model model, final ProductionRule productionRule, final MethodDefinition method) throws ModelException {
        final StringBuilder bodyBuilder = new StringBuilder();
        bodyBuilder.append(" {").append(Utils.LINE_BREAK);
        bodyBuilder.append("                ");
        if (!method.getResultType().equals(Type.VOID)) {
            bodyBuilder.append("return ");
        }
        bodyBuilder.append(callLeftmostArg(model, productionRule, method));
        bodyBuilder.append(Utils.LINE_BREAK);
        bodyBuilder.append("            }");
        return bodyBuilder.toString();
    }

    private String callLeftmostArg(final Model model, final ProductionRule productionRule, final MethodDefinition method) throws ModelException {

        // delegate to leftmost argument
        final List<FormalParameter> formalParameters = productionRule.getFormalParameters();
        if (!formalParameters.isEmpty()) {
            final FormalParameter formalParameter = formalParameters.get(0);
            final InterfaceDefinition anInterface = model.getInterface(formalParameter.getType());
            // amInterface may have type parameters; actual type is formalParameter.getType().
            final Map<String,TypeArgument> mapping = anInterface.getTypeParameters().inferTypeArguments(anInterface.getType(), formalParameter.getType());
            final MethodDefinition candidate = anInterface.getMethodBySignature(method.signature(), model);
            if (candidate == null) {
                throw new ModelException("Cannot implement by delegation "+method.declaration());
            }
            final MethodDefinition delegate = candidate.replaceParams(method.getOwner(), mapping);
            if (!delegate.matches(method)) {
                throw new ModelException("Cannot implement by delegation "+method.declaration());
            }
            ;
            final StringBuilder bodyBuilder = new StringBuilder();
            bodyBuilder.append(delegate.delegationInvocation(formalParameter.getName())).append(";").append(Utils.LINE_BREAK);
            return bodyBuilder.toString();
        } else {
            throw new ModelException("Cannot implement " + method.getName());
        }
    }

    private String delegateArchetypeMethod(final Model model, final ProductionRule productionRule, final MethodDefinition method) throws ModelException {
        StringBuilder builder = new StringBuilder();
        // find leftmost element, which is FormalParameter
        if (!productionRule.getElements().get(0).isConstant() || method.getResultType().getSimpleName().equals("Sql")) {
            if (productionRule.getElements().size() == 1) {
                // optimization: do not create extra objects
                builder.append(" {").append(Utils.LINE_BREAK);
                builder.append("                ");
                builder.append("return ");
                builder.append(productionRule.getElements().get(0).asMethodArgument(model))
                .append(";").append(Utils.LINE_BREAK);
                builder.append("            }/*delegation*/");
            } else {
                // either new CompositeSql or new CompositeQuery<T>
                builder.append(" {").append(Utils.LINE_BREAK);
                builder.append("                ");
                builder.append("return ");
                builder.append("new Composite").
                    append(method.getResultType())
                        .append("(")
                        .append(Utils.format(productionRule.getElements(), "", ", ", "", new F<ProductionRule.RuleElement, String, ModelException>() {
                            @Override
                            public String apply(final ProductionRule.RuleElement ruleElement) throws ModelException {
                                return ruleElement.asMethodArgument(model);
                            }
                        }))
                        .append(")").append(";").append(Utils.LINE_BREAK);
                builder.append("            }/*delegation*/");
            }
        } else {
            // scan elements until is not constant
            final ProductionRule.RuleElement queryDelegate = findVariable(productionRule);
            builder.append(" {").append(Utils.LINE_BREAK);
            builder.append("                ")
                    .append("final ")
                    .append(method.getResultType())
                    .append(" __query = ")
                    .append(queryDelegate.asMethodArgument(model))
                    .append(";").append(Utils.LINE_BREAK);
            builder.append("                ");
            builder.append("return ");
            builder.append("new Complex").
                append(method.getResultType())
                    .append("(")
                    .append("__query, ")
                    .append(Utils.format(productionRule.getElements(), "", ", ", "", new F<ProductionRule.RuleElement, String, ModelException>() {
                        @Override
                        public String apply(final ProductionRule.RuleElement ruleElement) throws ModelException {
                            return (ruleElement == queryDelegate) ? "__query" : ruleElement.asMethodArgument(model);
                        }
                    }))
                    .append(")").append(";").append(Utils.LINE_BREAK);
            builder.append("            }/*delegation*/");
        }
        return builder.toString();
    }

    private static ProductionRule.RuleElement findVariable(final ProductionRule production) throws ModelException {
        for (ProductionRule.RuleElement element:production.getElements()) {
            if (!element.isConstant()) {
                return element;
            }
        }
        throw new ModelException("Cannot generate method");
    }
}
