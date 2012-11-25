/*
* Copyright Alexander Izyurov 2010
*/
package org.simqle.generator;

import org.simqle.model.ClassDefinition;
import org.simqle.model.Model;

import java.io.IOException;

/**
 * <br/>15.11.2011
 *
 * @author Alexander Izyurov
 */
public class ClassGenerator extends AbstractFreeMarkerGenerator {

    @Override
    protected String getTemplateName() {
        return "Class.ftl";
    }

    @Override
    protected String getPackageName() {
        return "org.simqle.sql";
    }

    @Override
    protected void scanModel(Model model, GeneratorCallback callback)
            throws IOException {
        for (ClassDefinition classPair : model.getAllClasses()) {
           callback.generateFile(classPair.getName()+".java",
                   new ClassModel(classPair, getPackageName(), model));
        }
    }

}
