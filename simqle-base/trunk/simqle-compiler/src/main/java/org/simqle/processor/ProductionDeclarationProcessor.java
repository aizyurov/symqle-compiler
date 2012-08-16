/*
* Copyright Alexander Izyurov 2010
*/
package org.simqle.processor;

import org.simqle.model.*;
import org.simqle.parser.SyntaxTree;

import java.util.*;

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
                    }
                    final String methodBodySource;
                    List<FormalParameter> formalParameters = createFormalParameters(productionRule);
                    if (methodBodies.isEmpty()) {
                        // generate the body
                    // TODO add auto-generates z$create$, z$prepare$ and value methods to the body
                        try {
                            methodBodySource = generateFactoryMethodBody(productionRule, model, returnType);
                        } catch (ModelException e) {
                            throw new GrammarException(e.getMessage(), productionChoice);
                        }
                        final String valueMethodName = "value";
//                    final MethodDeclaration valueMethod = returnedInterface.getBody().getMethod(valueMethodName);
//                    if (body.getMethod(valueMethodName)==null) {
//                        // create one
//                        createValueMethodImplementation(valueMethod, productionRule);
//                    }
//
//                    }
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
                    MethodDeclaration method = null;
                    try {
                        method = new MethodDeclaration(false, "public", false, false, typeParameters, returnType, returnType.getNameChain().get(0).getName()+"_IS"+productionRule.getName(), formalParameters, "", "", methodBodySource);
                    } catch (ModelException e) {
                        throw new RuntimeException("Internal error", e);
                    }
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
                        Body classBody = Utils.convertChildren(addendum, "ClassBody", Body.class).get(0);
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
                                classPair.getBase().addMimics(virtualAncestor);
                                classPair.getExtension().addMimics(virtualAncestor);
                            } catch (ModelException e) {
                                throw new GrammarException(e.getMessage(), addendum);
                            }
                            // create toXxx method if possible and not exists
                            final String virtualAncestorName = virtualAncestor.getNameChain().get(0).getName();
                            final List<TypeArgument> virtualAncestorTypeArguments = virtualAncestor.getNameChain().get(0).getTypeArguments();
                            final String methodName = "to"+virtualAncestorName;
                            if (null==model.getClassPair(virtualAncestorName).getBase().getBody().getMethod(methodName)) {
                                // we expect that there is a trivial implementation;
                                // it may not compile e.g. because class does not implement proper interface,
                                // wrong number of parameters, target class not having proper constructor etc.
                                StringBuilder methodBodyBuilder = new StringBuilder();
                                methodBodyBuilder.append("{ return new ").append(virtualAncestorName);
                                String typeArgumentsString = virtualAncestorTypeArguments.isEmpty() ? "" :
                                        Utils.formatList(virtualAncestorTypeArguments, "<", ",", ">", new Function<String, TypeArgument>() {
                                            @Override
                                            public String apply(final TypeArgument typeArgument) {
                                                return typeArgument.getValue();
                                            }
                                        });
                                methodBodyBuilder.append("(SqlFactory.getInstance().")
                                .append(productionRule.getName()).append("(this)); }");
                                try {
                                    // TODO make final -requires changes in MethodDeclaration
                                    MethodDeclaration declaration = new MethodDeclaration(false, "protected", false, false,
                                            Collections.<TypeParameter>emptyList(), virtualAncestor, methodName,
                                            Collections.<FormalParameter>emptyList(), "", "", methodBodyBuilder.toString());
                                } catch (ModelException e) {
                                    // not expected here
                                    throw new RuntimeException("Internal error", e);
                                }
                            }
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
        ProductionRule.RuleElement delegate = null;
        for (ProductionRule.RuleElement element: rule.getElements()) {
            if (element.getType()!=null) {
                final InterfaceDefinition elementInterface = model.getInterface(element.getType().getNameChain().get(0).getName());
                final MethodDeclaration elementValueMethod = elementInterface.getBody().getMethod("value");
                if (elementValueMethod==null) {
                    continue;
                }
                // compare return types of Value methods using parameter substitutions
                final List<TypeParameter> typeParameters = elementInterface.getTypeParameters();
                final List<TypeArgument> typeArguments = element.getType().getNameChain().get(0).getTypeArguments();
                if (typeArguments.size()!=typeParameters.size()) {
                    throw new ModelException(element.getName()+":"+element.getType().getNameChain().get(0).getName()+" requires "+typeParameters.size()+", found: "+typeArguments.size());
                }
                if (Utils.substituteTypeArguments(typeArguments, typeParameters, elementValueMethod.getResultType()).equals(requiredReturnType)) {
                    delegate = element;
                    break;
                }
            }
        }
        if (delegate==null) {
            throw new ModelException("Method "+requiredReturnType.getImage()+" value(Element element) must be implemented; cannot guess implementation");
        } else {
            return "        @override\n" +
                    "       public "+requiredReturnType+" value(final Element element) {\n" +
                    "            return "+delegate.getName() +".value(element); \n" +
                    "       }\n";
        }
    }

    private String generateCreateQueryMethodSource(final String methodName, final Type requiredQueryType,
                                                   ProductionRule rule, final Model model) throws ModelException {
        // find suitable candidate
        int delegateIndex = -1;
        final List<ProductionRule.RuleElement> elements = rule.getElements();
        for (int i=0; i<elements.size(); i++) {
            final ProductionRule.RuleElement element = elements.get(i);
            if (element.getType()!=null) {
                final String elementInterfaceName = element.getType().getNameChain().get(0).getName();
                final InterfaceDefinition elementInterface = model.getInterface(elementInterfaceName);
                final String elementCreateMethodName = "z$create$"+elementInterfaceName;
                final MethodDeclaration elementValueMethod = elementInterface.getBody().getMethod(elementCreateMethodName);
                if (elementValueMethod==null) {
                    continue;
                }
                // compare return types of Value methods using parameter substitutions
                final List<TypeParameter> typeParameters = elementInterface.getTypeParameters();
                final List<TypeArgument> typeArguments = element.getType().getNameChain().get(0).getTypeArguments();
                if (typeArguments.size()!=typeParameters.size()) {
                    throw new ModelException(element.getName()+":"+ elementInterfaceName +" requires "+typeParameters.size()+", found: "+typeArguments.size());
                }
                if (Utils.substituteTypeArguments(typeArguments, typeParameters, elementValueMethod.getResultType()).equals(requiredQueryType)) {
                    delegateIndex = i;
                    break;
                }
            }
        }
        if (delegateIndex<0) {
            throw new ModelException("Method "+requiredQueryType.getImage()+methodName+"(Element element) must be implemented; cannot guess implementation");
        } else {
            final StringBuilder builder = new StringBuilder();
            builder.append("    public "+requiredQueryType.getImage()+" "+methodName+"(final SqlContext context) {\n");
            final String typeArgument = requiredQueryType.getNameChain().get(0).getTypeArguments().get(0).getValue();
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
                builder.append("             "+queryVariableName+", new CompositeSql(");
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
                builder.append("         "+element.getName()+"z$prepare$"+element.getType().getNameChain().get(0).getText()+"(context);\n");
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
                builder.append(element.getName()).append(".z$create$").append(element.getType().getNameChain().get(0).getText()).append("(context)");
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
        if (!typeArguments.isEmpty()) {
            builder.append("<");
            boolean isFirst = true;
            for (TypeArgument typeArgument: typeArguments) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    builder.append(",");
                }
                builder.append(typeArgument.getValue());
            }
            builder.append(">");
        }
        builder.append("() {\n");
        final List<TypeParameter> typeParameters = returnedInterface.getTypeParameters();
        if (typeArguments.size()!=typeParameters.size()) {
            throw new ModelException("Returned value "+returnType+" requires "+typeParameters.size()+", found: "+typeArguments.size());
        }
        {
            final MethodDeclaration valueMethod = returnedInterface.getBody().getMethod("value");
            if (valueMethod!=null) {
                Type requiredReturnType = Utils.substituteTypeArguments(typeArguments, typeParameters, valueMethod.getResultType());
                builder.append(generateValueMethodSource(requiredReturnType, productionRule, model));
            }
        }
        {
        final String prepareMethodName = "z$prepare$" + returnedInterfaceName;
            final MethodDeclaration prepareMethod = returnedInterface.getBody().getMethod(prepareMethodName);
            if (prepareMethod!=null) {
                // prepare method is void, so no type parameters substitution here
                builder.append(generatePrepareMethodSource(prepareMethodName, productionRule));
            }
        }
        {
            final String createMethodName = "z$create$" + returnedInterfaceName;
            final MethodDeclaration createMethod = returnedInterface.getBody().getMethod(createMethodName);
            if (createMethod!=null) {
                Type requiredReturnType = Utils.substituteTypeArguments(typeArguments, typeParameters, createMethod.getResultType());
                if (returnedInterface.isQuery()) {
                    builder.append(generateCreateQueryMethodSource(createMethodName, requiredReturnType, productionRule, model));
                } else {
                    builder.append(generateCreateSqlMethodSource(createMethodName, productionRule));
                }
            }
        }
        builder.append("    }\n}");
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