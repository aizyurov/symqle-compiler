/*
* Copyright Alexander Izyurov 2010
*/
package org.simqle.generator;

import org.simqle.model.*;

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
        return parameters.isEmpty() ? "" :
                Utils.formatList(parameters, "<", ", ", ">", new Function<String, TypeParameter>() {
                    public String apply(TypeParameter typeParameter) {
                        return typeParameter.getImage();
                    }
                });
    }

    public List<String> getImports() {
        return definition.getImportLines();
    }

    public String getFirstTypeParameter() {
        final List<TypeParameter> parameters = definition.getTypeParameters();
        if (parameters.isEmpty()) {
            throw new IllegalStateException("This interface has no parameters");
        }
        return parameters.get(0).getName();
    }

    public List<String> getModifiers() {
        List<String> modifiers = new ArrayList<String>();
        modifiers.add(definition.getAccessModifier());
        modifiers.addAll(definition.getOtherModifiers());
        return modifiers;
    }

    public String getExtendsList() {
        final List<Type> extended = definition.getExtended();
        return  extended.isEmpty() ? "" :
                Utils.formatList(extended, "extends ", ", ", "", new Function<String, Type>() {
            public String apply(Type type) {
                return type.getImage();
            }
        });
    }

    public List<MethodDeclaration> getMethods() {
        return definition.getBody().getMethods();
    }

    public List<String> getOtherDeclarations() {
        return definition.getBody().getOtherDeclarations();
    }

    public String getComment() {
        return definition.getComment();
    }

}
