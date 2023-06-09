/*
   Copyright 2011-2014 Alexander Izyurov

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.package org.symqle.common;
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
 * Process all syntax productions in the tree. Create ProductionRules, ProductionImplementations and put to the model.
 * @author Alexander Izyurov
 */
public class ProductionProcessor extends SyntaxTreeProcessor {


    @Override
    protected final Processor predecessor() {
        return new ClassCompletionProcessor();
    }

    @Override
    protected final void process(final SyntaxTree tree, final Model model) throws GrammarException {

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

        final List<SyntaxTree> productionChoices =
                tree.find("SymqleDeclarationBlock.SymqleDeclaration.ProductionDeclaration.ProductionChoice");

        for (SyntaxTree productionChoice: productionChoices) {
            // must be exactly one ProductionRule
            final SyntaxTree productionRuleNode = productionChoice.find("ProductionRule").get(0);
            ProductionRule productionRule = new ProductionRule(productionRuleNode);
            MethodDefinition dialectMethod = createDialectMethod(productionRule, dialect);
            try {
                dialect.addMethod(dialectMethod);
                final MethodDefinition genericDialectMethod = dialectMethod.override(genericDialect, model);
                genericDialectMethod.implement("public", " {" + Utils.LINE_BREAK
                        + "        return concat(" + productionRule.asMethodArguments() + ");" + Utils.LINE_BREAK
                        + "    }", true, false
                );
            } catch (ModelException e) {
                throw new GrammarException(e, productionRuleNode);
            }
            model.addRule(productionRule.getTarget(), productionRule.getShortRule());

            for (SyntaxTree productionImplNode: productionChoice.find("ProductionImplementation")) {
                final List<String> declarationImports =
                        productionImplNode.find("^.^.^.^.ImportDeclaration", SyntaxTree.BODY);
                symqle.addImportLines(declarationImports);
                final List<String> implementationImports =
                        productionImplNode.find("ImportDeclaration", SyntaxTree.BODY);
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
                // create implementing method in Symqle via anonymous class
                try {

                    final Type implementationType = productionImpl.isImplicit()
                            ? ImplicitConversion.getImplementationType(productionImpl.getReturnType(), model)
                            : productionImpl.getImplementationType();

                    final Type formalReturnType = productionImpl.isImplicit()
                            ? implementationType
                            : productionImpl.getReturnType();
                    final String staticMethodDeclaration =
                            productionImpl.getComment()
                                    + productionImpl.getAccessModifier()
                                    + " " + "static "
                                    + productionImpl.getTypeParameters()
                                    + " " + formalReturnType + " "
                                    + productionImpl.getName()
                                    + "(" + Utils.format(productionImpl.getFormalParameters(), "", ", ", "",
                                        new F<FormalParameter, String, RuntimeException>() {
                                            @Override
                                            public String apply(final FormalParameter formalParameter) {
                                                return "final " + formalParameter.toString();
                                            }
                                        })
                                    + ")";

                    final AnonymousClass anonymousClass = new AnonymousClass(productionImplNode, implementationType);
                        // create implementing method for SymqleGeneric class (uses the anonymous class and the rule)
                        // first implement all non-implemented methods in the class
                        final Collection<MethodDefinition> anonymousClassAllMethods =
                                anonymousClass.getAllMethods(model);
                        for (MethodDefinition method: anonymousClassAllMethods) {
                            // implement non-implemented methods
                            final Set<String> modifiers = method.getOtherModifiers();
                            if (modifiers.contains("volatile") && modifiers.contains("abstract")) {
                                // must implement
                                final String delegationCall;
                                if (Archetype.isArchetypeMethod(method)) {
                                    delegationCall =
                                            delegateArchetypeMethod(model, productionImpl, method, productionRule);
                                } else {
                                    final String getterPrefix = "get";
                                    final int getterPrefixLength = getterPrefix.length();
                                    if (method.getName().startsWith(getterPrefix)
                                            && method.getName().length() > getterPrefixLength
                                            && method.getFormalParameters().size() == 0) {
                                        // "property getter"
                                        final String methodName = method.getName();
                                        String propertyName =
                                                methodName.substring(getterPrefixLength, getterPrefixLength + 1)
                                                        .toLowerCase()
                                                + methodName.substring(getterPrefixLength + 1, methodName.length());
                                        String fieldDeclarationSource = "        private final "
                                                + method.getResultType() + " " + propertyName + " = "
                                                + callLeftmostArg(model, productionImpl, method);
                                        delegationCall = propertyGetter(propertyName);
                                        anonymousClass.addFieldDeclaration(
                                                FieldDeclaration.parse(fieldDeclarationSource));
                                    } else {
                                        delegationCall = delegateToLeftmostArg(model, productionImpl, method);
                                    }
                                }
                                method.implement("public", delegationCall, true, false);
                            }
                        }
                        final MethodDefinition methodToImplement = MethodDefinition.parse(staticMethodDeclaration
                                + " {" +  Utils.LINE_BREAK
                                + "        return new " + implementationType + "()"
                                + anonymousClass.instanceBodyAsString() + ";" + Utils.LINE_BREAK
                                + "    }" + Utils.LINE_BREAK,
                                symqle);
                        methodToImplement.setSourceRef(productionImpl.getSourceRef());
                        if (productionImpl.isImplicit()) {
                            model.addConversion(new ImplicitConversion(productionImpl.getTypeParameters(),
                                    productionImpl.getFormalParameters().get(0).getType(),
                                    productionImpl.getReturnType(),
                                    methodToImplement));
                            symqle.addMethod(methodToImplement);
                        } else {
                            model.addExplicitMethod(methodToImplement, anonymousClass, declarationImports);
                        }
                /*    } */
                } catch (ModelException e) {
                    throw new GrammarException(e, productionImplNode);
                }
            }
        }
    }

    private String propertyGetter(final String propertyName) {
        final StringBuilder bodyBuilder = new StringBuilder();
        bodyBuilder.append(" {").append(Utils.LINE_BREAK);
        bodyBuilder.append("                ");
        bodyBuilder.append("return ").append(propertyName).append(";");
        bodyBuilder.append("            }");
        return bodyBuilder.toString();
    }

    private String delegateToLeftmostArg(final Model model,
                                         final ProductionImplementation productionImpl,
                                         final MethodDefinition method) throws ModelException {
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

    private String callLeftmostArg(final Model model,
                                   final ProductionImplementation productionImpl,
                                   final MethodDefinition method) throws ModelException {

        // delegate to leftmost argument
        final List<FormalParameter> formalParameters = productionImpl.getFormalParameters();
        if (!formalParameters.isEmpty()) {
            final FormalParameter formalParameter = formalParameters.get(0);
            final InterfaceDefinition anInterface = model.getInterface(formalParameter.getType());
            // amInterface may have type parameters; actual type is formalParameter.getType().
            final Map<String, TypeArgument> mapping =
                    anInterface.getTypeParameters()
                            .inferTypeArguments(anInterface.getType(), formalParameter.getType());
            final MethodDefinition candidate = anInterface.getMethodBySignature(method.signature(), model);
            if (candidate == null) {
                throw new ModelException("Cannot implement by delegation " + method.declaration());
            }
            final MethodDefinition delegate = candidate.replaceParams(method.getOwner(), mapping);
            if (!delegate.matches(method)) {
                throw new ModelException("Cannot implement by delegation " + method.declaration());
            }
            final StringBuilder bodyBuilder = new StringBuilder();
            bodyBuilder.append(delegate.delegationInvocation(formalParameter.getName()))
                    .append(";").append(Utils.LINE_BREAK);
            return bodyBuilder.toString();
        } else {
            throw new ModelException("Cannot implement " + method.getName());
        }
    }

    private String delegateArchetypeMethod(final Model model,
                                           final ProductionImplementation productionImpl,
                                           final MethodDefinition method,
                                           final ProductionRule rule) throws ModelException {
        StringBuilder builder = new StringBuilder();
        // find leftmost element, which is FormalParameter
        if (method.getResultType().getSimpleName().equals("SqlBuilder")) {
            // just what is returned by Symqle
            builder.append(" {").append(Utils.LINE_BREAK);
            builder.append("                ");
            builder.append("return ");
            builder.append("context.get(Dialect.class).").
                append(rule.getName())
                    .append("(")
                    .append(
                            Utils.format(productionImpl.getVariableElements(), "", ", ", "",
                                    new F<ProductionImplementation.RuleElement, String, ModelException>() {
                        @Override
                        public String apply(final ProductionImplementation.RuleElement ruleElement)
                                throws ModelException {
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
                    .append(" rowMapper = ")
                    .append(queryDelegate.asMethodArgument(model))
                    .append(";").append(Utils.LINE_BREAK);
            builder.append("                ");
            builder.append("return ");
            builder.append("new Complex").
                append(method.getResultType())
                    .append("(")
                    .append("rowMapper, ")
                    .append("context.get(Dialect.class).")
                    .append(rule.getName())
                    .append("(")
                    .append(
                            Utils.format(productionImpl.getVariableElements(), "", ", ", "",
                                    new F<ProductionImplementation.RuleElement, String, ModelException>() {
                        @Override
                        public String apply(final ProductionImplementation.RuleElement ruleElement)
                                throws ModelException {
                            return (ruleElement == queryDelegate) ? "rowMapper" : ruleElement.asMethodArgument(model);
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

    private static ProductionImplementation.RuleElement findVariable(final ProductionImplementation production)
            throws ModelException {
        for (ProductionImplementation.RuleElement element:production.getElements()) {
            if (!element.isConstant()) {
                return element;
            }
        }
        throw new ModelException("Cannot generate method");
    }

    private MethodDefinition createDialectMethod(final ProductionRule rule,
                                                 final InterfaceDefinition dialect) {
        return MethodDefinition.parseAbstract(rule.asAbstractMethodDeclaration() + ";", dialect);
    }
}
