package org.simqle.processor;

import org.simqle.model.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
