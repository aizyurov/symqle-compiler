/*
* Copyright Alexander Izyurov 2010
*/
package org.simqle.model;

import java.util.*;

/**
 * <br/>13.11.2011
 *
 * @author Alexander Izyurov
 */
public class Model {

    private final Map<String, InterfaceDefinition> interfaces = new LinkedHashMap<String, InterfaceDefinition>();
    private final Map<String, ClassPair> classes = new LinkedHashMap<String, ClassPair>();
    private final Map<String, FactoryMethodModel> factoryMethods = new HashMap<String, FactoryMethodModel>();

    public void addInterface(InterfaceDefinition def) throws ModelException {
        final String name = def.getName();
        if (interfaces.containsKey(name)) {
            throw new ModelException("Diplicate interface: "+name);
        }
        interfaces.put(name, def);
    }

    public InterfaceDefinition getInterface(String name) {
        return interfaces.get(name);
    }

    public List<InterfaceDefinition> getAllInterfaces() {
        return new ArrayList<InterfaceDefinition>(interfaces.values());
    }

    public void addClass(ClassPair classPair) throws ModelException {
        if (null!=classes.put(classPair.getExtension().getPairName(), classPair)) {
            throw new ModelException("Duplicate class "+classPair.getExtension().getPairName());
        }
    }

    public ClassPair getClassPair(String name) {
        return classes.get(name);
    }

    public List<ClassPair> getAllClasses() {
        return new ArrayList<ClassPair>(classes.values());
    }

    public void addFactoryMethod(FactoryMethodModel factoryMethod) throws ModelException {
        if (null!=factoryMethods.put(factoryMethod.getName(), factoryMethod)) {
            throw new ModelException("Duplicate rule "+factoryMethod.getName());
        }
    }

    public List<FactoryMethodModel> getAllFactoryMethods() {
        return new ArrayList<FactoryMethodModel>(factoryMethods.values());
    }

}


