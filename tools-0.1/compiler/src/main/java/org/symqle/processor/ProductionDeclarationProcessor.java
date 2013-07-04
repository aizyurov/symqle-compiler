/*
* Copyright Alexander Izyurov 2010
*/
package org.symqle.processor;

import org.symqle.model.*;
import org.symqle.parser.SyntaxTree;
import org.symqle.util.Utils;

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

        final ClassDefinition symqle;
        final InterfaceDefinition dialect;
        final ClassDefinition genericDialect;
        try {
            symqle = model.getClassDef("Symqle");
            dialect = model.getInterface("Dialect");
            genericDialect = model.getClassDef("GenericDialect");
        } catch (ModelException e) {
            throw new IllegalStateException("Internal error", e);
        }

        for (SyntaxTree productionChoice: tree.find("SymqleDeclarationBlock.SymqleDeclaration.ProductionDeclaration.ProductionChoice")) {
            // must be exactrly one ProductionRule
            final SyntaxTree productionRuleNode = productionChoice.find("ProductionRule").get(0);
            ProductionRule productionRule = new ProductionRule(productionRuleNode);
            MethodDefinition dialectMethod = createDialectMethod(productionRule, dialect);
            try {
                dialect.addMethod(dialectMethod);
                final MethodDefinition genericDialectMethod = dialectMethod.override(genericDialect, model);
                genericDialectMethod.implement("public", " {" + Utils.LINE_BREAK +
                        "        return concat(" + productionRule.asMethodArguments() + ");" + Utils.LINE_BREAK +
                        "    }", true, false
                );
            } catch (ModelException e) {
                throw new GrammarException(e, productionRuleNode);
            }

            for (SyntaxTree productionImplNode: productionChoice.find("ProductionImplementation")) {
                final List<String> declarationImports = productionImplNode.find("^.^.^.^.ImportDeclaration", SyntaxTree.BODY);
                symqle.addImportLines(declarationImports);
                final List<String> implementationImports = productionImplNode.find("ImportDeclaration", SyntaxTree.BODY);
                    // implementation only
                symqle.addImportLines(implementationImports);
                final ProductionImplementation productionImpl = new ProductionImplementation(productionImplNode);
                for (FormalParameter formalParameter: productionImpl.getFormalParameters()) {
                    try {
                        model.getInterface(formalParameter.getType());
                    } catch (ModelException e) {
                        throw new GrammarException(e, productionRuleNode);
                    }
                }
                String abstractMethodDeclaration =
                        productionImpl.getComment() +
                        productionImpl.asAbstractMethodDeclaration()+";";
                final MethodDefinition methodToImplement = MethodDefinition.parse(abstractMethodDeclaration, symqle);
                methodToImplement.setSourceRef(productionImpl.getSourceRef());
                if (productionImpl.isImplicit()) {
                    model.addImplicitMethod(methodToImplement);
                } else {
                    if (!methodToImplement.getAccessModifier().equals("private")
                            && !methodToImplement.getAccessModifier().equals("protected"))
                    model.addExplicitMethod(methodToImplement, declarationImports);
                }
                // create implementing method in Symqle via anonymous class
                try {
                    final AnonymousClass anonymousClass = new AnonymousClass(productionImplNode);
                    // create implementing method for SymqleGeneric class (uses the anonymous class and the rule)
                    // first implement all non-implemented methods in the class
                    final Collection<MethodDefinition> anonymousClassAllMethods = anonymousClass.getAllMethods(model);
                    for (MethodDefinition method: anonymousClassAllMethods) {
                        // implement non-implemented methods
                        final Set<String> modifiers = method.getOtherModifiers();
                        if (modifiers.contains("transient") && modifiers.contains("abstract")) {
                            // must implement
                            final String delegationCall;
                            if (Archetype.isArchetypeMethod(method)) {
                                delegationCall = delegateArchetypeMethod(model, productionImpl, method, productionRule);
                            } else if (method.getName().startsWith("get")
                                    && method.getName().length() > 3
                                    && method.getFormalParameters().size()==0) {
                                // "property getter"
                                final String methodName = method.getName();
                                String propertyName = methodName.substring(3,4).toLowerCase() +
                                        methodName.substring(4, methodName.length());
                                String fieldDeclarationSource = "        private final " +
                                        method.getResultType() + " " + propertyName + " = " +
                                        callLeftmostArg(model, productionImpl, method);
                                delegationCall = propertyGetter(propertyName);
                                anonymousClass.addFieldDeclaration(FieldDeclaration.parse(fieldDeclarationSource));
                            } else {
                                delegationCall = delegateToLeftmostArg(model, productionImpl, method);
                            }
                            method.implement("public", delegationCall, true, false);
                        }
                    }
                    methodToImplement.implement(methodToImplement.getAccessModifier(),
                            " { " +  Utils.LINE_BREAK +
                                    "        return new "+methodToImplement.getResultType()+"()" +
                            anonymousClass.instanceBodyAsString() + ";"+ Utils.LINE_BREAK +
                            "    }"+Utils.LINE_BREAK,
                            true, false);
                } catch (ModelException e) {
                    throw new GrammarException(e, productionImplNode);
                }
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

    private String delegateToLeftmostArg(final Model model, final ProductionImplementation productionImpl, final MethodDefinition method) throws ModelException {
        final StringBuilder bodyBuilder = new StringBuilder();
        bodyBuilder.append(" {").append(Utils.LINE_BREAK);
        bodyBuilder.append("                ");
        if (!method.getResultType().equals(Type.VOID)) {
            bodyBuilder.append("return ");
        }
        bodyBuilder.append(callLeftmostArg(model, productionImpl, method));
        bodyBuilder.append(Utils.LINE_BREAK);
        bodyBuilder.append("            }");
        return bodyBuilder.toString();
    }

    private String callLeftmostArg(final Model model, final ProductionImplementation productionImpl, final MethodDefinition method) throws ModelException {

        // delegate to leftmost argument
        final List<FormalParameter> formalParameters = productionImpl.getFormalParameters();
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

    private String delegateArchetypeMethod(final Model model, final ProductionImplementation productionImpl, final MethodDefinition method, final ProductionRule rule) throws ModelException {
        StringBuilder builder = new StringBuilder();
        // find leftmost element, which is FormalParameter
        if (method.getResultType().getSimpleName().equals("Sql")) {
            // just what is returned by Symqle
            builder.append(" {").append(Utils.LINE_BREAK);
            builder.append("                ");
            builder.append("return ");
            builder.append("context.get(Dialect.class).").
                append(rule.getName())
                    .append("(")
                    .append(Utils.format(productionImpl.getVariableElements(), "", ", ", "", new F<ProductionImplementation.RuleElement, String, ModelException>() {
                        @Override
                        public String apply(final ProductionImplementation.RuleElement ruleElement) throws ModelException {
                            return ruleElement.asMethodArgument(model);
                        }
                    }))
                    .append(")").append(";").append(Utils.LINE_BREAK);
            builder.append("            }");
        } else {
            // scan elements until is not constant
            final ProductionImplementation.RuleElement queryDelegate = findVariable(productionImpl);
            builder.append(" {").append(Utils.LINE_BREAK);
            builder.append("                ")
                    .append("final ")
                    .append(method.getResultType())
                    .append(" __rowMapper = ")
                    .append(queryDelegate.asMethodArgument(model))
                    .append(";").append(Utils.LINE_BREAK);
            builder.append("                ");
            builder.append("return ");
            builder.append("new Complex").
                append(method.getResultType())
                    .append("(")
                    .append("__rowMapper, ")
                    .append("context.get(Dialect.class).")
                    .append(rule.getName())
                    .append("(")
                    .append(Utils.format(productionImpl.getVariableElements(), "", ", ", "", new F<ProductionImplementation.RuleElement, String, ModelException>() {
                        @Override
                        public String apply(final ProductionImplementation.RuleElement ruleElement) throws ModelException {
                            return (ruleElement == queryDelegate) ? "__rowMapper" : ruleElement.asMethodArgument(model);
                        }
                    }))
                    .append(")")
                    .append(")")
                    .append(";")
                    .append(Utils.LINE_BREAK);
            builder.append("            }");
        }
        return builder.toString();
    }

    private static ProductionImplementation.RuleElement findVariable(final ProductionImplementation production) throws ModelException {
        for (ProductionImplementation.RuleElement element:production.getElements()) {
            if (!element.isConstant()) {
                return element;
            }
        }
        throw new ModelException("Cannot generate method");
    }

    private MethodDefinition createDialectMethod(ProductionRule rule, InterfaceDefinition dialect) {
        return MethodDefinition.parseAbstract(rule.asAbstractMethodDeclaration() + ";", dialect);
    }
}
