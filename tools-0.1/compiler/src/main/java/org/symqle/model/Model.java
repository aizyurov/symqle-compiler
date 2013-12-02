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

    private final List<MethodDefinition> implicitSymqleMethods = new ArrayList<MethodDefinition>();
    private final Map<MethodDefinition, Set<String>> explicitSymqleMethods = new LinkedHashMap<MethodDefinition, Set<String>>();
    private final Map<String, List<String>> rulesByTargetTypeName = new HashMap<String, List<String>>();

    private final Map<String, AnonymousClass> anonymousClassByMethodSignature = new HashMap<String, AnonymousClass>();

    private final ClassDefinition symqleTemplate = ClassDefinition.parse("class symqleTemplate {}");

    private final Map<String, String> dialectNameBySymqleSignature = new HashMap<String, String>();

    private final Set<String> ambiguousMethodSignature = new HashSet<String>();

    /**
     *
     * @param method
     * @param classDefinition not null for abstract methods
     */
    public void addImplicitMethod(MethodDefinition method, AnonymousClass classDefinition) {
        implicitSymqleMethods.add(method);
        anonymousClassByMethodSignature.put(method.signature(), classDefinition);
    }

    public void associateDialectName(String dialectName, MethodDefinition methodDef) {
        dialectNameBySymqleSignature.put(methodDef.signature(), dialectName);
    }

    public String getAssociatedDialectName(MethodDefinition methodDef) {
        return dialectNameBySymqleSignature.get(methodDef.signature());
    }

    public void setAmbiguous(String signature) {
        ambiguousMethodSignature.add(signature);
    }

    public boolean isAmbiguous(String signature) {
        return ambiguousMethodSignature.contains(signature);
    }

    /**
     *
     * @param method
     * @param classDefinition not null for abstract methods
     */
    public void addExplicitMethod(MethodDefinition method, AnonymousClass classDefinition, Collection<String> requiredImports) {
        explicitSymqleMethods.put(method, new HashSet<String>(requiredImports));
        anonymousClassByMethodSignature.put(method.signature(), classDefinition);
    }

    public AnonymousClass getAnonymousClassByMethodSignature(String signature) {
        return anonymousClassByMethodSignature.get(signature);
    }

    public List<MethodDefinition> getImplicitSymqleMethods() {
        return implicitSymqleMethods;
    }

    public List<MethodDefinition> getExplicitSymqleMethods() {
        return new ArrayList<MethodDefinition>(explicitSymqleMethods.keySet());
    }

    public Set<String> getImportsForExplicitMethod(MethodDefinition def) {
        return Collections.unmodifiableSet(explicitSymqleMethods.get(def));
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

    public void addRule(String targetTypeName, String rule) {
        List<String> rules = rulesByTargetTypeName.get(targetTypeName);
        if (rules == null) {
            rules = new ArrayList<String>();
            rulesByTargetTypeName.put(targetTypeName, rules);
        }
        rules.add(rule);
    }

    public List<String> getRules(String targetTypeName) {
        final List<String> rules = rulesByTargetTypeName.get(targetTypeName);
        return rules == null ? null : Collections.unmodifiableList(rules);
    }

    public ClassDefinition getSymqleTemplate() {
        return symqleTemplate;
    }
}


