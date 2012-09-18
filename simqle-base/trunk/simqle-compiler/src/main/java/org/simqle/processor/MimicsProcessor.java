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
        createConverterMethods(model);
        final Map<String, Set<String>> primaryImplementors = collectPrimaryImplementors(model);
        for (String signature: primaryImplementors.keySet()) {
            createDelegatingMethods(signature, model, primaryImplementors.get(signature));
        }

    }

    private void createDelegatingMethods(final String signature, final Model model, final Set<String> primaryImplementors) throws ModelException {
        Set<String> currentActiveSet = null;
        Set<String> nextActiveSet = new HashSet<String>(primaryImplementors);
        Set<String> unknown = new HashSet<String>();
        // init unknown
        for (ClassPair pair: model.getAllClasses()) {
            final String className = pair.getExtension().getClassName();
            if (!currentActiveSet.contains(className)) {
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
                final Type myResultType = Utils.substituteTypeArguments(chosenAncestorType.getNameChain().get(0).getTypeArguments(), candidate.getExtension().getTypeParameters(), delegateResultType);

                final List<FormalParameter> delegateFormalParameters = methodToDelegate.getFormalParameters();
                final List<FormalParameter> myFormalParameters = new ArrayList<FormalParameter>(delegateFormalParameters.size());
                for (FormalParameter parameter: delegateFormalParameters) {
                    myFormalParameters.add(new FormalParameter(
                            Utils.substituteTypeArguments(chosenAncestorType.getNameChain().get(0).getTypeArguments(), candidate.getExtension().getTypeParameters(), parameter.getType()),
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
                                +"to"+chosenAncestor.getExtension().getClassName()+"()."+methodToDelegate.getName()+
                                Utils.formatList(myFormalParameters, "(", ", ", ")", new Function<String, FormalParameter>() {
                                    @Override
                                    public String apply(final FormalParameter formalParameter) {
                                        return formalParameter.getName();
                                    }
                                })+"; }"
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
                final String className = classPair.getBase().getClassName();
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
}
