package org.simqle.processor;

import org.simqle.model.ClassDefinition;
import org.simqle.model.MethodDefinition;
import org.simqle.model.Model;
import org.simqle.model.ModelException;

/**
 * @author lvovich
 */
public class AbstractMethodsProcessor implements ModelProcessor {

    @Override
    public void process(final Model model) throws ModelException {
        for (ClassDefinition classDefinition: model.getAllClasses()) {

            for (final MethodDefinition method: classDefinition.getAllMethods(model)) {
                if (method.getOtherModifiers().contains("transient") && method.getOtherModifiers().contains("abstract")) {
                    method.declareAbstract("public");
                }
            }
        }
    }


}
