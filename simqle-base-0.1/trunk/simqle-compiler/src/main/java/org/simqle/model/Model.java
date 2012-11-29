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


    private final Map<String, ClassOrInterface> classMap = new LinkedHashMap<String, ClassOrInterface>();
    private final Map<String, FactoryMethodModel> factoryMethods = new HashMap<String, FactoryMethodModel>();

    private final Set<String> caseInsensitiveClassNames = new HashSet<String>();

    private final List<MethodDefinition> implicitSimqleMethods = new ArrayList<MethodDefinition>();
    private final List<MethodDefinition> explicitSimqleMethods = new ArrayList<MethodDefinition>();

    public void addImplicitMethod(MethodDefinition method) {
        implicitSimqleMethods.add(method);
    }

    public void addExplicitMethod(MethodDefinition method) {
        explicitSimqleMethods.add(method);
    }

    public void addInterface(InterfaceDefinition def) throws ModelException {
        addClassOrInterface(def.getName(), new ClassOrInterface(def));
    }

    private void addClassOrInterface(String name, ClassOrInterface classOrInterface) throws ModelException {
        if (classMap.containsKey(name)) {
            throw new ModelException("Duplicate interface: "+name);
        } else if (caseInsensitiveClassNames.contains(name.toUpperCase())) {
            throw new ModelException("Name duplicate under Windows: "+name);
        }
        caseInsensitiveClassNames.add(name.toUpperCase());
        classMap.put(name, classOrInterface);
    }

    public InterfaceDefinition getInterface(String name) throws ModelException {
        ClassOrInterface classOrInterface = getClassOrInterface(name);
        if (classOrInterface.isInterface) {
            return classOrInterface.interfaceDefinition;
        } else {
            throw new IllegalArgumentException(name + "is not interface");
        }
    }

    public AbstractTypeDefinition getAbstractType(String name) throws ModelException {
        return getClassOrInterface(name).getAbstract();
    }

    private ClassOrInterface getClassOrInterface(String name) throws ModelException {
        ClassOrInterface classOrInterface = classMap.get(name);
        if (classOrInterface == null) {
            throw new ModelException("Unknown class/interface: " + name);
        }
        return classOrInterface;
    }

    public List<InterfaceDefinition> getAllInterfaces() {
        List<InterfaceDefinition> result = new LinkedList<InterfaceDefinition>();
        for (ClassOrInterface candidate: classMap.values()) {
            if (candidate.isInterface) {
                result.add(candidate.interfaceDefinition);
            }
        }
        return result;
    }

    public void addClass(ClassDefinition def) throws ModelException {
        addClassOrInterface(def.getName(), new ClassOrInterface(def));
    }

    public ClassDefinition getClassDef(String name) throws ModelException {
        ClassOrInterface classOrInterface = getClassOrInterface(name);
        if (!classOrInterface.isInterface) {
            return classOrInterface.classDefinition;
        } else {
            throw new IllegalArgumentException(name + "is not class");
        }
    }

    public List<ClassDefinition> getAllClasses() {
        List<ClassDefinition> result = new LinkedList<ClassDefinition>();
        for (ClassOrInterface candidate: classMap.values()) {
            if (!candidate.isInterface) {
                result.add(candidate.classDefinition);
            }
        }
        return result;
    }

    public List<FactoryMethodModel> getAllFactoryMethods() {
        return new ArrayList<FactoryMethodModel>(factoryMethods.values());
    }

    private static class ClassOrInterface {
        private final ClassDefinition classDefinition;
        private final InterfaceDefinition interfaceDefinition;
        private boolean isInterface;

        private ClassOrInterface(InterfaceDefinition interfaceDefinition) {
            this.interfaceDefinition = interfaceDefinition;
            this.classDefinition = null;
            this.isInterface = true;
        }

        private ClassOrInterface(ClassDefinition classDefinition) {
            this.classDefinition = classDefinition;
            this.interfaceDefinition = null;
            this.isInterface = false;
        }

        private AbstractTypeDefinition getAbstract() {
            return isInterface ? interfaceDefinition : classDefinition;
        }
    }

    public InterfaceDefinition getInterface(Type t) throws ModelException {
        return getInterface(resolveName(t));
    }

    public ClassDefinition getClassDef(Type t) throws ModelException {
        return getClassDef(resolveName(t));
    }

    private String resolveName(final Type t) {
        List<TypeNameWithTypeArguments> nameChain = t.getNameChain();
        return nameChain.get(nameChain.size()-1).getName();
    }

}


