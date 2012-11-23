package org.simqle.processor;

import org.simqle.model.*;
import org.simqle.util.Utils;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: lvovich
 * Date: 18.09.12
 * Time: 15:47
 * To change this template use File | Settings | File Templates.
 */
public class MimicsProcessor {

    public void process(final Model model) throws ModelException {
        addConstructorsForDerivedClasses(model);
        createConverterMethods(model);
        final Map<String, Set<String>> primaryImplementors = collectPrimaryImplementors(model);
        for (String signature: primaryImplementors.keySet()) {
            createDelegatingMethods(signature, model, primaryImplementors.get(signature));
        }
        final List<InterfaceDefinition> allInterfaces = model.getAllInterfaces();
        final Map<String, Set<String>> interfacePrimaryImplementors = collectInterfaces(model);
        for (String name: interfacePrimaryImplementors.keySet()) {
            createImplementedInterfaces(name, interfacePrimaryImplementors.get(name), model);
        }

    }

    private void addConstructorsForDerivedClasses(final Model model) {
        for (ClassPair classPair : model.getAllClasses()) {
            final ClassDefinition baseClassDefinition = classPair.getBase();
            final ClassDefinition extensionClassDefinition = classPair.getExtension();
            // transfer all constructors keeping signature; implement as call to super() with same arguments
            for (final ConstructorDeclaration constructor : baseClassDefinition.getBody().getConstructors()) {
                final List<FormalParameter> formalParameters = constructor.getFormalParameters();
                StringBuilder argumentsBuilder = new StringBuilder();
                for (FormalParameter formalParameter: formalParameters) {
                    if (argumentsBuilder.length()>0) {
                        argumentsBuilder.append(", ");
                    }
                    argumentsBuilder.append(formalParameter.getName());
                }

                StringBuilder parametersBuilder = new StringBuilder();
                for (FormalParameter formalParameter: formalParameters) {
                    if (parametersBuilder.length()>0) {
                        parametersBuilder.append(", ");
                    }
                    parametersBuilder.append(formalParameter.getImage());
                }
                final String constructorSource = String.format(EXTENSION_CONSTRUCTOR_FORMAT, baseClassDefinition.getPairName(),parametersBuilder.toString(),
                        argumentsBuilder.toString());
                final ConstructorDeclaration constructorDeclaration = ConstructorDeclaration.parse(constructorSource);
                extensionClassDefinition.getBody().unsafeAddConstructorDeclaration((constructorDeclaration));
            }
        }
    }


    private void createImplementedInterfaces(final String interfaceName, final Set<String> primaryImplementors, final Model model) throws ModelException {
        Set<String> currentActiveSet = new HashSet<String>();
        Set<String> nextActiveSet = new HashSet<String>(primaryImplementors);
        Set<String> unknown = new HashSet<String>();

        // init unknown
        for (ClassPair pair: model.getAllClasses()) {
            final String className = pair.getExtension().getClassName();
            if (!nextActiveSet.contains(className)) {
                unknown.add(className);
            }
        }

        while (!nextActiveSet.isEmpty()) {
            currentActiveSet.clear();
            currentActiveSet.addAll(nextActiveSet);
            nextActiveSet.clear();
            for (Iterator<String> iterator = unknown.iterator(); iterator.hasNext(); ) {
                final String candidateName = iterator.next();
                final ClassPair candidate = model.getClassPair(candidateName);
                Set<Type> ancestorCandidates = new HashSet<Type>();
                for (Type virtualAncestor: candidate.getMimics()) {
                    final ClassPair classPair = model.findClassPair(virtualAncestor);
                    if (currentActiveSet.contains(classPair.getExtension().getClassName())) {
                        ancestorCandidates.add(virtualAncestor);
                    }
                }
                if (ancestorCandidates.isEmpty()) {
                    continue;
                }
                final Type chosenAncestorType = ancestorCandidates.iterator().next();
                if (ancestorCandidates.size()>1) {
                    System.err.println("Multiple candidates for delegation of "+interfaceName+" in "+candidateName+"; chosing "+chosenAncestorType.getImage());
                }
                final ClassPair chosenAncestor = model.findClassPair(chosenAncestorType);
                List<Type> interfaces = new ArrayList<Type>(chosenAncestor.getExtension().getImplementedInterfaces());
                interfaces.addAll(chosenAncestor.getBase().getImplementedInterfaces());
                Type interfaceToImplement = null;
                for (Type interfaceToTest: interfaces) {
                    if (interfaceToTest.getNameChain().get(interfaceToTest.getNameChain().size()-1).getName().equals(interfaceName)) {
                        interfaceToImplement = interfaceToTest;
                        break;
                    }
                }
                if (interfaceToImplement==null) {
                    throw new IllegalStateException("Interface "+interfaceName+" expected but not found in class "+chosenAncestor.getExtension().getClassName());
                }
                // parameters substitution
                Type implementedInterface = Utils.substituteTypeArguments(chosenAncestorType.getNameChain().get(0).getTypeArguments(), chosenAncestor.getExtension().getTypeParameters(), interfaceToImplement);
                candidate.getExtension().addImplementedInterface(implementedInterface);
                nextActiveSet.add(candidate.getExtension().getClassName());
                iterator.remove();
            }
        }
    }

    private Map<String, Set<String>> collectInterfaces(Model model) {
        Map<String, Set<String>> interfaceImplementationMap = new HashMap<String, Set<String>>();
        for (ClassPair pair: model.getAllClasses()) {

            final List<Type> interfaces = pair.getExtension().getImplementedInterfaces();
            interfaces.addAll(pair.getBase().getImplementedInterfaces());
            for (Type type: interfaces) {
                final String interfaceName = type.getNameChain().get(type.getNameChain().size() - 1).getName();
                Set<String> implementors = interfaceImplementationMap.get(interfaceName);
                if (implementors==null) {
                    implementors = new HashSet<String>();
                    interfaceImplementationMap.put(interfaceName, implementors);
                }
                implementors.add(pair.getExtension().getClassName());
            }
        }
        return interfaceImplementationMap;
    }


    private void createDelegatingMethods(final String signature, final Model model, final Set<String> primaryImplementors) throws ModelException {
        Set<String> currentActiveSet = null;
        Set<String> nextActiveSet = new HashSet<String>(primaryImplementors);
        Set<String> unknown = new HashSet<String>();
        // init unknown
        for (ClassPair pair: model.getAllClasses()) {
            final String className = pair.getExtension().getClassName();
            if (!nextActiveSet.contains(className)) {
                unknown.add(className);
            }
        }
        while (!nextActiveSet.isEmpty()) {
            currentActiveSet = nextActiveSet;
            nextActiveSet = new HashSet<String>();
            for (Iterator<String> iterator = unknown.iterator(); iterator.hasNext(); ) {
                final String candidateName = iterator.next();
                final ClassPair candidate = model.getClassPair(candidateName);
                Set<Type> ancestorCandidates = new HashSet<Type>();
                for (Type virtualAncestor: candidate.getMimics()) {
                    final ClassPair classPair = model.findClassPair(virtualAncestor);
                    if (currentActiveSet.contains(classPair.getExtension().getClassName())) {
                        ancestorCandidates.add(virtualAncestor);
                    }
                }
                if (ancestorCandidates.isEmpty()) {
                    continue;
                }
                final Type chosenAncestorType = ancestorCandidates.iterator().next();
                if (ancestorCandidates.size()>1) {
                    System.err.println("Multiple candidates for delegation of "+signature+" in "+candidateName+"; chosing "+chosenAncestorType.getImage());
                }
                final ClassPair chosenAncestor = model.findClassPair(chosenAncestorType);
                final MethodDeclaration baseMethod = chosenAncestor.getBase().getBody().getMethod(signature);
                final MethodDeclaration methodToDelegate = baseMethod !=null ? baseMethod : chosenAncestor.getExtension().getBody().getMethod(signature);
                // should never be null
                // now construct the delegating method
                // substitute type parameters
                final Type delegateResultType = methodToDelegate.getResultType();
                final Type myResultType = Utils.substituteTypeArguments(chosenAncestorType.getNameChain().get(0).getTypeArguments(), chosenAncestor.getExtension().getTypeParameters(), delegateResultType);

                final List<FormalParameter> delegateFormalParameters = methodToDelegate.getFormalParameters();
                final List<FormalParameter> myFormalParameters = new ArrayList<FormalParameter>(delegateFormalParameters.size());
                for (FormalParameter parameter: delegateFormalParameters) {
                    myFormalParameters.add(new FormalParameter(
                            Utils.substituteTypeArguments(chosenAncestorType.getNameChain().get(0).getTypeArguments(), chosenAncestor.getBase().getTypeParameters(), parameter.getType()),
                            parameter.getName(),
                            parameter.getModifiers()
                    ));
                }
                candidate.getExtension().getBody().addMethod(new MethodDeclaration(false, "public", false, false,
                        methodToDelegate.getTypeParameters(),
                        myResultType,
                        methodToDelegate.getName(),
                        myFormalParameters,
                        methodToDelegate.getThrowsClause(),
                        "", // TODO add comment
                        "{ "+
                                (myResultType.equals(Type.VOID) ? "" : "return ")
                                +"to"+chosenAncestor.getExtension().getClassName()+"()."+methodToDelegate.getName()+"("+
                                Utils.formatList(myFormalParameters, "", ", ", "", new Function<String, FormalParameter>() {
                                    @Override
                                    public String apply(final FormalParameter formalParameter) {
                                        return formalParameter.getName();
                                    }
                                })+"); }"
                        ));
                nextActiveSet.add(candidate.getExtension().getClassName());
                iterator.remove();
            }
        }

    }

    private Map<String, Set<String>> collectPrimaryImplementors(Model model) {
        Map<String, Set<String>> primaryImplementorsBySignature = new HashMap<String, Set<String>>();
        for (ClassPair pair: model.getAllClasses()) {
            for (MethodDeclaration method: pair.getBase().getBody().getMethods()) {
                if (method.getAccessModifier().equals("public") && !method.isStatic()) {
                    final String signature = method.getSignature();
                    Set<String> classPairs = primaryImplementorsBySignature.get(signature);
                    if (classPairs==null) {
                        classPairs = new HashSet<String>();
                        primaryImplementorsBySignature.put(signature, classPairs);
                    }
                    classPairs.add(pair.getExtension().getClassName());
                }
            }
        }
        return primaryImplementorsBySignature;
    }

    private void createConverterMethods(final Model model) throws ModelException {
        for (ClassPair pair: model.getAllClasses()) {
            final Set<Type> mimics = pair.getMimics();
            for (Type type: mimics) {
                final ClassPair classPair = model.findClassPair(type);
                if (classPair==null) {
                    throw new ModelException("Class not found: "+type.getImage());
                }
                final String className = classPair.getExtension().getClassName();
                final String signature = "to" + className+"()";
                final MethodDeclaration converterMethod = pair.getBase().getBody().getMethod(signature);
                if (converterMethod==null || converterMethod.getFormalParameters().size()>0) {
                    // this is a different method, implement
                    final String ruleName = pair.getRuleNameForMimics(type);
                    if (ruleName==null) {
                        throw new ModelException("No idea how to implement "+type.getImage()+" "+signature+";");
                    }
                    pair.getBase().getBody().addMethod(generateConverterImplementation(signature, type, ruleName));
                } else if (converterMethod!=null && !converterMethod.getResultType().equals(type)) {
                    throw new ModelException("Wrong conversion method "+signature+" required return type "+type+", actual "+converterMethod.getResultType());
                }
            }
        }
    }

    private MethodDeclaration generateConverterImplementation(final String signature, final Type type, String ruleName) {
        StringBuilder builder = new StringBuilder();
        builder.append("protected final ")
                .append(type.getImage())
                .append(" ")
                .append(signature)
                .append(" {")
                .append("    return new ").append(type.getImage()).append("(SqlFactory.getInstance().")
                .append(ruleName).append("(this));")
                .append("    }");
        return MethodDeclaration.parse(builder.toString());
    }

    private final static String EXTENSION_CONSTRUCTOR_FORMAT = "public %s(%s) { super(%s); }";


}
