/*
* Copyright Alexander Izyurov 2010
*/
package org.simqle.processor;

import org.simqle.model.*;
import org.simqle.parser.SyntaxTree;
import org.simqle.util.Callback;
import org.simqle.util.CallbackIterator;
import org.simqle.util.StopException;
import org.simqle.util.Utils;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * <br/>20.11.2011
 *
 * @author Alexander Izyurov
 */
public class ProductionDeclarationProcessor implements Processor {
    public void process(SyntaxTree tree, Model model) throws GrammarException {
        for (SyntaxTree block: tree.find("SimqleDeclarationBlock")) {
            final List<SyntaxTree> simqleProductionDeclarations =
                    block.find("SimqleDeclaration.ProductionDeclaration");
            if (!simqleProductionDeclarations.isEmpty()) {
                final SyntaxTree productionDeclaration = simqleProductionDeclarations.get(0);
                final List<String> imports = Utils.bodies(block.find("ImportDeclaration"));
                List<TypeParameter> typeParameters = Utils.convertChildren(productionDeclaration, "TypeParameters.TypeParameter", TypeParameter.class);
                Type returnType = Utils.convertChildren(productionDeclaration, "ClassOrInterfaceType", Type.class).get(0);
                for (SyntaxTree productionChoice: productionDeclaration.find("ProductionChoice")) {
                    // guaranteed exactly one
                    final ProductionRule productionRule = Utils.convertChildren(productionChoice, "ProductionRule", ProductionRule.class).get(0);
                    final List<String> implImports = Utils.bodies(productionChoice.find("ProductionImplementation.ImportDeclaration"));
                    // at most one
                    final List<SyntaxTree> methodBodies = productionChoice.find("ProductionImplementation.Block");
                    final String interfaceName = returnType.getNameChain().get(0).getName();
                    final InterfaceDefinition returnedInterface = model.getInterface(interfaceName);
                    if (returnedInterface==null) {
                        throw new GrammarException("Unknown interface: "+interfaceName, productionChoice);
                    } else if (!returnedInterface.isQuery() && !returnedInterface.isSql()) {
                        throw new GrammarException("Does not have Query nor Sql archetype: "+interfaceName, productionChoice);
                    }
                    final String methodBodySource;
                    List<FormalParameter> formalParameters = createFormalParameters(productionRule);
                    if (methodBodies.isEmpty()) {
                        // generate the body
                        try {
                            methodBodySource = generateFactoryMethodBody(productionRule, model, returnType);
                        } catch (ModelException e) {
                            throw new GrammarException(e.getMessage(), productionChoice);
                        }
                    } else {
                        final String rawImage = methodBodies.get(0).getImage();
                    // mactosubstitutions in body:
                    // _THIS_PRODUCTION_ for production method name,
                    // _PREPARE_ for z$prepare${returnType.name}
                    // _CREATE_ for z$create${returnType.name}
                        final Map<String, String> substitutions = new HashMap<String, String>();
                        substitutions.put("_THIS_PRODUCTION_", productionRule.getName());
                        final String prepareMethodName = "z$prepare$" + interfaceName;
                        substitutions.put("_PREPARE_", prepareMethodName);
                        final String createMethodName = "z$create$" + interfaceName;
                        substitutions.put("_CREATE_", createMethodName);
                        methodBodySource = makeSubstitutions(rawImage, substitutions);
                    }
                    StringBuilder builder = new StringBuilder();
                    builder.append("public ");
                    builder.append(Utils.formatList(typeParameters, "<", ", ", "> ", new Function<String, TypeParameter>() {
                        @Override
                        public String apply(final TypeParameter typeParameter) {
                            return typeParameter.getImage();
                        }
                    }));
                    builder.append(returnType.getImage())
                            .append(" ")
                            .append(productionRule.getName())
                            .append("(");
                    builder.append(Utils.formatList(formalParameters, "", ", ", "", new Function<String, FormalParameter>() {
                        @Override
                        public String apply(final FormalParameter formalParameter) {
                            return formalParameter.getImage();
                        }
                    }));
                    builder.append(")");
                    builder.append(methodBodySource);
                    MethodDeclaration method = MethodDeclaration.parse(builder.toString());
                    final FactoryMethodModel factoryMethodModel = new FactoryMethodModel(imports,  implImports, productionRule,
                            method);
                    // add it to the model
                    try {
                        model.addFactoryMethod(factoryMethodModel);
                    } catch (ModelException e) {
                        throw new GrammarException(e.getMessage(), productionChoice);
                    }

                    // now process class addendum
                    final List<SyntaxTree> classAddenda = productionChoice.find("ClassAddendum");
                    for (SyntaxTree addendum: classAddenda) {
                        final String className = addendum.find("Identifier").get(0).getValue();
                        final ClassPair classPair = model.getClassPair(className);
                        if (classPair==null) {
                            throw new GrammarException("Class not found: "+className, addendum);
                        }
                        List<TypeParameter> classTypeParameters = Utils.convertChildren(addendum, "TypeParameters.TypeParameter", TypeParameter.class);
                        final List<TypeParameter> knownTypeParameters = classPair.getBase().getTypeParameters();
                        if (!knownTypeParameters.equals(classTypeParameters)) {
                            throw new GrammarException("TypeParameters do not match to that in class definition: "+classTypeParameters, addendum);
                        }
                        // exactly one by syntax
                        final String rawBody = addendum.find("ClassBody").get(0).getImage();
                        final String replacement = "SqlFactory.getInstance()." + productionRule.getName();
                        final String bodySource = rawBody.replaceAll("_APPLY_RULE_", replacement);
                        Body classBody = Body.parseClassBody(bodySource);
                        // copy everything to base
                        Body targetBody = classPair.getBase().getBody();
                        try {
                            targetBody.merge(classBody);
                        } catch (ModelException e) {
                            throw new GrammarException(e.getMessage(), addendum);
                        }
                        // now process MIMICS
                        final List<Type> types = Utils.convertChildren(addendum, "Mimics.ClassOrInterfaceType", Type.class);
                        for (Type virtualAncestor: types) {
                            try {
                                classPair.addMimics(virtualAncestor, productionRule.getName());
                            } catch (ModelException e) {
                                throw new GrammarException(e.getMessage(), addendum);
                            }
//                            // create toXxx method if possible and not exists
                            // TODO move to MimicsProcessor
//                            final String virtualAncestorName = virtualAncestor.getNameChain().get(0).getName();
//                            final List<TypeArgument> virtualAncestorTypeArguments = virtualAncestor.getNameChain().get(0).getTypeArguments();
//                            final String methodName = "to"+virtualAncestorName;
//                            if (null==targetBody.getMethod(methodName)) {
//                                // we expect that there is a trivial implementation;
//                                // it may not compile e.g. because class does not implement proper interface,
//                                // wrong number of parameters, target class not having proper constructor etc.
//                                StringBuilder methodBodyBuilder = new StringBuilder();
//                                methodBodyBuilder.append("{ return new ").append(virtualAncestorName);
//                                String typeArgumentsString = virtualAncestorTypeArguments.isEmpty() ? "" :
//                                        Utils.formatList(virtualAncestorTypeArguments, "<", ",", ">", new Function<String, TypeArgument>() {
//                                            @Override
//                                            public String apply(final TypeArgument typeArgument) {
//                                                return typeArgument.getValue();
//                                            }
//                                        });
//                                methodBodyBuilder.append(typeArgumentsString);
//                                methodBodyBuilder.append("(SqlFactory.getInstance().")
//                                .append(productionRule.getName()).append("(this)); }");
//                                try {
//                                    // TODO make final -requires changes in MethodDeclaration
//                                    MethodDeclaration declaration = new MethodDeclaration(false, "protected", false, false,
//                                            Collections.<TypeParameter>emptyList(), virtualAncestor, methodName,
//                                            Collections.<FormalParameter>emptyList(), "", "", methodBodyBuilder.toString());
//                                    targetBody.addMethod(declaration);
//                                } catch (ModelException e) {
//                                    // not expected here: absense of method checked in enclosing if block
//                                    throw new RuntimeException("Internal error", e);
//                                }
//                            }
                        }

                    }

                }
            }
        }
    }


    private String makeSubstitutions(String source, Map<String, String> substitutions) {
        String result = source;
        for (final Map.Entry<String, String> substitution : substitutions.entrySet()) {
            result = result.replaceAll(substitution.getKey(), substitution.getValue());
        }
        return result;
    }

    private String generateValueMethodSource(final Type requiredReturnType,
                                                   ProductionRule rule, final Model model) throws ModelException {
        // find suitable candidate
        final AtomicReference<ProductionRule.RuleElement> delegateRef = new AtomicReference<ProductionRule.RuleElement>();
        new CallbackIterator<ProductionRule.RuleElement, ModelException>(rule.getElements()).iterate(new Callback<ProductionRule.RuleElement, ModelException>() {
            @Override
            public void call(final ProductionRule.RuleElement element) throws StopException, ModelException {
                if (element.getType()!=null) {
                    final String name = element.getType().getNameChain().get(0).getName();
                    final InterfaceDefinition elementInterface = model.getInterface(name);
                    if (elementInterface == null) {
                        throw new ModelException("Unknown interface: "+name);
                    }
                    final MethodDeclaration elementValueMethod = elementInterface.getMethodBySignature("value(Element)", model);
                    if (elementValueMethod==null) {
                        // give up
                        throw new StopException();
                    }
                    // compare return types of Value methods using parameter substitutions
                    final List<TypeParameter> typeParameters = elementInterface.getTypeParameters();
                    final List<TypeArgument> typeArguments = element.getType().getNameChain().get(0).getTypeArguments();
                    if (typeArguments.size()!=typeParameters.size()) {
                        throw new ModelException("Rule element "+element.getName()+":"+ name +" requires "+typeParameters.size()+" type parameter, found: "+typeArguments.size());
                    }
                    if (Utils.substituteTypeArguments(typeArguments, typeParameters, elementValueMethod.getResultType()).equals(requiredReturnType)) {
                        delegateRef.set(element);
                    }
                    throw new StopException();
                }
            }
        });
        final ProductionRule.RuleElement delegate = delegateRef.get();
        if (delegate==null) {
            throw new ModelException("Method "+requiredReturnType.getImage()+" value(Element element) must be implemented; cannot guess implementation");
        } else {
            return  "       @Override\n" +
                    "       public "+requiredReturnType.getImage()+" value(final Element element) {\n" +
                    "            return "+delegate.getName() +".value(element); \n" +
                    "       }\n";
        }
    }

    private String generateCreateQueryMethodSource(final String methodName, final Type requiredQueryType,
                                                   ProductionRule rule, final Model model) throws ModelException {
        // find suitable candidate
        final AtomicInteger delegateIndexHolder = new AtomicInteger(-1);
        final List<ProductionRule.RuleElement> elements = rule.getElements();
        CallbackIterator<ProductionRule.RuleElement, ModelException> callbackIterator =
                new CallbackIterator<ProductionRule.RuleElement, ModelException>(elements);
        callbackIterator.iterate(new Callback<ProductionRule.RuleElement, ModelException>() {
            private int position = 0;
            @Override
            public void call(final ProductionRule.RuleElement element) throws StopException, ModelException {
                if (element.getType()!=null) {
                    final String elementInterfaceName = element.getType().getNameChain().get(0).getName();
                    final InterfaceDefinition elementInterface = model.getInterface(elementInterfaceName);
                    final String elementCreateMethodName = "z$create$"+elementInterfaceName;
                    final String elementCreateMethodSignature = elementCreateMethodName + "(SqlContext)";
                    final MethodDeclaration elementCreateMethod = elementInterface.getBody().getMethod(elementCreateMethodSignature);
                    // compare return types of Value methods using parameter substitutions
                    final List<TypeParameter> typeParameters = elementInterface.getTypeParameters();
                    final List<TypeArgument> typeArguments = element.getType().getNameChain().get(0).getTypeArguments();
                    if (typeArguments.size()!=typeParameters.size()) {
                        throw new ModelException(element.getName()+":"+ elementInterfaceName +" requires "+typeParameters.size()+" type parameters, found: "+typeArguments.size());
                    }
                    if (Utils.substituteTypeArguments(typeArguments, typeParameters, elementCreateMethod.getResultType()).equals(requiredQueryType)) {
                        delegateIndexHolder.set(position);
                    }
                    throw new StopException();
                }
                position += 1;
            }
        });
        final int delegateIndex = delegateIndexHolder.get();
        if (delegateIndex<0) {
            throw new ModelException("Method "+requiredQueryType.getImage()+" "+methodName+"(SqlContext context) must be implemented; cannot guess implementation");
        } else {
            final StringBuilder builder = new StringBuilder();
            builder.append("    @Override\n");
            builder.append("    public "+requiredQueryType.getImage()+" "+methodName+"(final SqlContext context) {\n");
            final String typeArgument = requiredQueryType.getNameChain().get(0).getTypeArguments().get(0).getImage();
            if (delegateIndex==0) {
                // short form
                builder.append("        return new CompositeQuery<"+typeArgument+">(");
                appendCreateMethodArguments(rule, builder);
                builder.append(");\n");
                builder.append("    }\n");
            } else {
                // long form -- need additional local variable
                final ProductionRule.RuleElement delegate = elements.get(delegateIndex);
                final String delegateTypeName = delegate.getType().getNameChain().get(0).getName();
                final String queryVariableName = delegate.getName()+"_query";
                builder.append("        final DataExtractor<"+typeArgument+"> "+queryVariableName+" = "+delegate.getName()+".z$create$"+delegateTypeName+"(context); \n");
                builder.append("        return new CompoundQuery<"+typeArgument+">(");
                builder.append(queryVariableName+", new CompositeSql(");
                for (int i=0; i<elements.size(); i++) {
                    if (i>0) {
                        builder.append(", ");
                    }
                    if (i==delegateIndex) {
                        builder.append(queryVariableName);
                    } else {
                        builder.append(elements.get(i).getName());
                    }
                }
                builder.append(")\n");
                builder.append("            );\n");
                builder.append("        }\n");
            }
            return builder.toString();
        }
    }

    private String generatePrepareMethodSource(String methodName, ProductionRule rule) {
        StringBuilder builder = new StringBuilder();
        builder.append("    @Override\n")
                .append("    public void "+methodName+"(final SqlContext context) {\n");
        for (ProductionRule.RuleElement element: rule.getElements()) {
            if (element.getType()!=null) {
                // all interfaces have z$prepare$ method, so no check here
                builder.append("         ").append(element.getName()).append(".z$prepare$").append(element.getType().getNameChain().get(0).getName()).append("(context);\n");
            }
        }
        builder.append("    }\n");
        return builder.toString();
    }

    private String generateCreateSqlMethodSource(String methodName, ProductionRule rule) {
        StringBuilder builder = new StringBuilder();
        builder.append("    @Override\n")
                .append("    public Sql ").append(methodName).append("(final SqlContext context) {\n")
                .append("        return new CompositeSql(");
        appendCreateMethodArguments(rule, builder);
        builder.append(");");
        builder.append("    }\n");
        return builder.toString();
    }

    private void appendCreateMethodArguments(final ProductionRule rule, final StringBuilder builder) {
        boolean firstArgument = true;
        for (ProductionRule.RuleElement element: rule.getElements()) {
            if (!firstArgument) {
                builder.append(", ");
            } else {
                firstArgument = false;
            }
            if (element.getType()!=null) {
                // all interfaces have z$prepare$ method, so no check here
                builder.append(element.getName()).append(".z$create$").append(element.getType().getNameChain().get(0).getName()).append("(context)");
            } else {
                builder.append(element.getName());
            }
        }
    }

    private String generateFactoryMethodBody(final ProductionRule productionRule, final Model model, final Type returnType) throws ModelException {
        final String returnedInterfaceName = returnType.getNameChain().get(0).getName();
        final InterfaceDefinition returnedInterface = model.getInterface(returnedInterfaceName);
        StringBuilder builder = new StringBuilder();
        builder.append("{ return new ").append(returnType.getNameChain().get(0).getName());
        final List<TypeArgument> typeArguments = returnType.getNameChain().get(0).getTypeArguments();
        builder.append(Utils.formatList(typeArguments, "<", ",", ">", new Function<String, TypeArgument>() {
            @Override
            public String apply(final TypeArgument typeArgument) {
                return typeArgument.getImage();
            }
        }));
        builder.append("() {\n");
        final List<TypeParameter> typeParameters = returnedInterface.getTypeParameters();
        if (typeArguments.size()!=typeParameters.size()) {
            throw new ModelException("Return type "+returnType.getImage()+" requires "+typeParameters.size()+" type parameter, found: "+typeArguments.size());
        }
        {
            final MethodDeclaration valueMethod = returnedInterface.getMethodBySignature("value(Element)", model);
            if (valueMethod!=null) {
                Type requiredReturnType = Utils.substituteTypeArguments(typeArguments, typeParameters, valueMethod.getResultType());
                builder.append(generateValueMethodSource(requiredReturnType, productionRule, model));
            }
        }
        {
            final String prepareMethodName = "z$prepare$" + returnedInterfaceName;
            // prepare method is void, so no type parameters substitution here
            builder.append(generatePrepareMethodSource(prepareMethodName, productionRule));
        }
        {
            final String createMethodName = "z$create$" + returnedInterfaceName;
            final String createMethodSignature = createMethodName + "(SqlContext)";
            final MethodDeclaration createMethod = returnedInterface.getBody().getMethod(createMethodSignature);
            Type requiredReturnType = Utils.substituteTypeArguments(typeArguments, typeParameters, createMethod.getResultType());
            if (returnedInterface.isQuery()) {
                builder.append(generateCreateQueryMethodSource(createMethodName, requiredReturnType, productionRule, model));
            } else  {
                builder.append(generateCreateSqlMethodSource(createMethodName, productionRule));
            }
        }
        builder.append("    };\n");
        builder.append("}");
        return builder.toString();
    }

    private List<FormalParameter> createFormalParameters(final ProductionRule productionRule) {
        List<FormalParameter> formalParameters = new ArrayList<FormalParameter>();
        for (ProductionRule.RuleElement element: productionRule.getElements()) {
            if (!element.isConst()) {
                // make all parameters of SqlFactory methods final
                formalParameters.add(new FormalParameter(element.getType(), element.getName(), Collections.singletonList("final")));
            }
        }
        return formalParameters;
    }
}
