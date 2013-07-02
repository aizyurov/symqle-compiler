/*
* Copyright Alexander Izyurov 2010
*/
package org.symqle.generator;

import org.symqle.model.*;

/**
* <br/>17.11.2011
*
* @author Alexander Izyurov
*/
public class InterfaceModel {
    private final String packageName;
    private final InterfaceDefinition definition;

    public InterfaceModel(String packageName, InterfaceDefinition definition) {
        this.packageName = packageName;
        this.definition = definition;
    }

    public String getPackageName() {
        return packageName;
    }

    public InterfaceDefinition getDefinition() {
        return definition;
    }
}
