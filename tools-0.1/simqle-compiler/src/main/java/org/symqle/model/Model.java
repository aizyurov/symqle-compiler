/*
* Copyright Alexander Izyurov 2010
*/
package org.symqle.model;

import java.util.*;

/**
 * <br/>13.11.2011
 *
 * @author Alexander Izyurov
 */
public class Model {

    private final Map<String, AbstractTypeDefinition> classMap = new LinkedHashMap<String, AbstractTypeDefinition>();
    private final Map<String, FactoryMethodModel> factoryMethods = new HashMap<String, FactoryMethodModel>();

    private final Set<String> caseInsensitiveClassNames = new HashSet<String>();

    private final List<MethodDefinition> implicitSimqleMethods = new ArrayList<MethodDefinition>();
    private final Map<MethodDefinition, Set<String>> explicitSimqleMethods = new LinkedHashMap<MethodDefinition, Set<String>>();

    public void addImplicitMethod(MethodDefinition method) {
        implicitSimqleMethods.add(method);
    }

    public void addExplicitMethod(MethodDefinition method, Collection<String> requiredImports) {
        explicitSimqleMethods.put(method, new HashSet<String>(requiredImports));
    }

    public List<MethodDefinition> getImplicitSimqleMethods() {
        return implicitSimqleMethods;
    }

    public List<MethodDefinition> getExplicitSimqleMethods() {
        return new ArrayList<MethodDefinition>(explicitSimqleMethods.keySet());
    }

    public Set<String> getImportsForExplicitMethod(MethodDefinition def) {
        return Collections.unmodifiableSet(explicitSimqleMethods.get(def));
    }

    public void addInterface(InterfaceDefinition def) throws ModelException {
        addClassOrInterface(def);
    }

    private void addClassOrInterface(AbstractTypeDefinition def) throws ModelException {
        final String name = def.getName();
        if (classMap.containsKey(name)) {
            throw new ModelException("Duplicate class name: "+name);
        } else if (caseInsensitiveClassNames.contains(name.toUpperCase())) {
            throw new ModelException("Name duplicate under Windows: "+name);
        }
        caseInsensitiveClassNames.add(name.toUpperCase());
        classMap.put(name, def);
    }

    public InterfaceDefinition getInterface(String name) throws ModelException {
        try {
            return (InterfaceDefinition) getAbstractType(name);
        } catch (ClassCastException e) {
            throw new ModelException("Not interface: " + name);
        }
    }

    public AbstractTypeDefinition getAbstractType(String name) throws ModelException {
        final AbstractTypeDefinition def = classMap.get(name);
        if (def == null) {
            throw new ModelException("Type not found: "+name);
        }
        return def;
    }

    public List<InterfaceDefinition> getAllInterfaces() {
        List<InterfaceDefinition> result = new LinkedList<InterfaceDefinition>();
        for (AbstractTypeDefinition candidate: classMap.values()) {
            if (candidate instanceof InterfaceDefinition) {
                result.add((InterfaceDefinition) candidate);
            }
        }
        return result;
    }

    public Collection<AbstractTypeDefinition> getAllTypes() {
        return classMap.values();
    }

    public void addClass(ClassDefinition def) throws ModelException {
        addClassOrInterface(def);
    }

    public ClassDefinition getClassDef(String name) throws ModelException {
        try {
            return (ClassDefinition) getAbstractType(name);
        } catch (ClassCastException e) {
            throw new ModelException("Not interface: " + name);
        }
    }

    public List<ClassDefinition> getAllClasses() {
        List<ClassDefinition> result = new LinkedList<ClassDefinition>();
        for (AbstractTypeDefinition candidate: classMap.values()) {
            if (candidate instanceof ClassDefinition) {
                result.add((ClassDefinition) candidate);
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

    public boolean hasType(Type t) {
        return classMap.containsKey(t.getSimpleName());        
    }

    public InterfaceDefinition getInterface(Type t) throws ModelException {
        return getInterface(t.getSimpleName());
    }

    public ClassDefinition getClassDef(Type t) throws ModelException {
        return getClassDef(t.getSimpleName());
    }

}


