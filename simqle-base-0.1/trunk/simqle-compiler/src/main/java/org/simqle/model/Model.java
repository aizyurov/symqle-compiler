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

    private final Set<String> caseInsensitiveClassNames = new HashSet<String>();

    public void addInterface(InterfaceDefinition def) throws ModelException {
        final String name = def.getName();
        if (interfaces.containsKey(name)) {
            throw new ModelException("Duplicate interface: "+name);
        } else if (caseInsensitiveClassNames.contains(name.toUpperCase())) {
            throw new ModelException("Name duplicate under Windows: "+name);
        }
        interfaces.put(name, def);
        caseInsensitiveClassNames.add(name.toUpperCase());
    }

    public InterfaceDefinition getInterface(String name) {
        return interfaces.get(name);
    }

    public List<InterfaceDefinition> getAllInterfaces() {
        return new ArrayList<InterfaceDefinition>(interfaces.values());
    }

    public void addClass(ClassPair classPair) throws ModelException {
        String name = classPair.getExtension().getClassName();
        if (null!=classes.put(name, classPair)) {
            throw new ModelException("Duplicate class "+classPair.getExtension().getClassName());
        } else if (caseInsensitiveClassNames.contains(name.toUpperCase())) {
            throw new ModelException("Name duplicate under Windows: "+name);
        }
        caseInsensitiveClassNames.add(name.toUpperCase());
    }

    public ClassPair getClassPair(String name) {
        return classes.get(name);
    }

    public ClassPair findClassPair(Type type) throws ModelException {
        if (type.getNameChain().size()!=1) {
            throw new ModelException("Class name should be a simple name, actually "+type.getImage());
        }
        return getClassPair(type.getNameChain().get(0).getName());
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


