/*
* Copyright Alexander Izyurov 2010
*/
package org.simqle.generator;

import org.simqle.model.Model;

import java.io.IOException;

/**
 * <br/>15.11.2011
 *
 * @author Alexander Izyurov
 */
public class GenericFactoryGenerator extends AbstractFreeMarkerGenerator {

    @Override
    protected String getTemplateName() {
        return "GenericSqlFactory.ftl";
    }

    @Override
    protected String getPackageName() {
        return "org.simqle.sql";
    }

    @Override
    protected void scanModel(Model model, GeneratorCallback callback)
            throws IOException {
           callback.generateFile("GenericSqlFactory.java",
                   new FactoryModel(model.getAllFactoryMethods(), getPackageName()));
    }

}
