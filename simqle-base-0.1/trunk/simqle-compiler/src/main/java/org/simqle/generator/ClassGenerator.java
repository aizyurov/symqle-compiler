/*
* Copyright Alexander Izyurov 2010
*/
package org.simqle.generator;

import org.simqle.model.ClassPair;
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
        for (ClassPair classPair : model.getAllClasses()) {
           callback.generateFile(classPair.getExtension().getClassName()+".java",
                   new ClassModel(classPair, getPackageName(), model));
        }
    }

}
