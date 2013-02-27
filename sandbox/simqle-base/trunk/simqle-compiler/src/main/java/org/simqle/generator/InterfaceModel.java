/*
* Copyright Alexander Izyurov 2010
*/
package org.simqle.generator;

import org.simqle.model.*;
import org.simqle.util.Utils;

import java.util.ArrayList;
import java.util.List;

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

    public String getName() {
        return definition.getName();
    }

    public String getTypeParameters() {
        final List<TypeParameter> parameters = definition.getTypeParameters();
        return Utils.formatList(parameters, "<", ", ", ">", new Function<String, TypeParameter>() {
                    public String apply(TypeParameter typeParameter) {
                        return typeParameter.getImage();
                    }
                });
    }

    public List<String> getModifiers() {
        List<String> modifiers = new ArrayList<String>();
        modifiers.add(definition.getAccessModifier());
        modifiers.addAll(definition.getOtherModifiers());
        return modifiers;
    }

    public String getExtendsList() {
        final List<Type> extended = definition.getExtended();
        return  Utils.formatList(extended, "extends ", ", ", "", new Function<String, Type>() {
                    public String apply(Type type) {
                        return type.getImage();
                    }
                });
    }

}