package org.symqle.processor;

import org.symqle.model.ClassDefinition;
import org.symqle.model.MethodDefinition;
import org.symqle.model.Model;
import org.symqle.model.ModelException;

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
