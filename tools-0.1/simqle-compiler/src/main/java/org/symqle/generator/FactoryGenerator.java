/*
* Copyright Alexander Izyurov 2010
*/
package org.symqle.generator;

import org.symqle.model.Model;

import java.io.IOException;

/**
 * <br/>15.11.2011
 *
 * @author Alexander Izyurov
 */
public class FactoryGenerator extends AbstractFreeMarkerGenerator {

    @Override
    protected String getTemplateName() {
        return "SqlFactory.ftl";
    }

    @Override
    protected String getPackageName() {
        return "org.symqle.sql";
    }

    @Override
    protected void scanModel(Model model, GeneratorCallback callback)
            throws IOException {
           callback.generateFile("SqlFactory.java",
                   new FactoryModel(model.getAllFactoryMethods(), getPackageName()));
    }

}
