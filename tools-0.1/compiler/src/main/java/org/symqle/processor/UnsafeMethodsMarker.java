package org.symqle.processor;

import org.symqle.model.ClassDefinition;
import org.symqle.model.F;
import org.symqle.model.FormalParameter;
import org.symqle.model.MethodDefinition;
import org.symqle.model.Model;
import org.symqle.model.ModelException;
import org.symqle.util.Utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author lvovich
 */
public class UnsafeMethodsMarker extends ModelProcessor {

    @Override
    void process(final Model model) throws ModelException {
        final ClassDefinition symqleTemplate = model.getSymqleTemplate();
        final Map<String, Set<MethodDefinition>>  methodsByReducedSignature = new HashMap<String, Set<MethodDefinition>>();
        for (MethodDefinition methodDef : symqleTemplate.getDeclaredMethods()) {
            final List<FormalParameter> formalParameters = methodDef.getFormalParameters();
            if (!formalParameters.isEmpty()) {
                final List<FormalParameter> reducedParams = formalParameters.subList(1, formalParameters.size());
                String reducedSignature = methodDef.getName() + "("  + Utils.format(reducedParams, "", ",", "", new F<FormalParameter, String, RuntimeException>() {
                    @Override
                    public String apply(final FormalParameter formalParameter) {
                        return formalParameter.getType().getSimpleName();
                    }
                }) + ")";
                Set<MethodDefinition> methodDefinitions = methodsByReducedSignature.get(reducedSignature);
                if (methodDefinitions == null) {
                    methodDefinitions = new HashSet<MethodDefinition>();
                    methodsByReducedSignature.put(reducedSignature, methodDefinitions);
                }
                methodDefinitions.add(methodDef);
            }
        }
        for (Map.Entry<String, Set<MethodDefinition>> entry: methodsByReducedSignature.entrySet()) {
            if (entry.getValue().size() > 1) {
                model.setAmbiguous(entry.getKey());
                System.err.println("Unsafe: " + entry.getKey());
            }
        }
    }
}
