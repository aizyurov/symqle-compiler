/*
* Copyright Alexander Izyurov 2010
*/
package org.simqle.generator;

import org.simqle.model.*;

import java.io.IOException;

/**
 * <br/>15.11.2011
 *
 * @author Alexander Izyurov
 */
public class InterfaceGenerator extends AbstractFreeMarkerGenerator {

    @Override
    protected String getTemplateName() {
        return "Interface.ftl";
    }

    @Override
    protected String getPackageName() {
        return "org.simqle.sql";
    }

    @Override
    protected void scanModel(Model model, GeneratorCallback callback)
            throws IOException {
        for (InterfaceDefinition interfaceDefinition : model.getAllInterfaces()) {
           callback.generateFile(interfaceDefinition.getName()+".java",
                   new InterfaceModel(getPackageName(), interfaceDefinition));
        }
    }

}
