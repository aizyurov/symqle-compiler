/*
* Copyright Alexander Izyurov 2010
*/
package org.symqle.model;

import org.symqle.util.TSort;
import org.symqle.util.Utils;

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

    private final List<ImplicitConversion> conversions = new ArrayList<ImplicitConversion>();
    private final Map<MethodDefinition, Set<String>> explicitSymqleMethods = new LinkedHashMap<MethodDefinition, Set<String>>();
    private final Map<MethodDefinition, AnonymousClass> anonymousClassByMethod = new HashMap<MethodDefinition, AnonymousClass>();
    private final Map<String, List<String>> rulesByTargetTypeName = new HashMap<String, List<String>>();

    private final Map<String, String> dialectNameBySymqleSignature = new HashMap<String, String>();

    // key is "reduced signature" -name and afgumetns but the first one
    private final Map<String, Boolean> symqleMethodUniqueness = new HashMap<String, Boolean>();

    /**
     *
     */
    public void addConversion(ImplicitConversion conversion) {
        conversions.add(conversion);
    }

    public boolean isUnambiguous(MethodDefinition method) {
        return symqleMethodUniqueness.get(reducedSignature(method));
    }

    /**
     *
     * @param method
     */
    public void addExplicitMethod(MethodDefinition method, AnonymousClass anonymousClass, Collection<String> requiredImports) {
        explicitSymqleMethods.put(method, new HashSet<String>(requiredImports));
        final String key = reducedSignature(method);
        final Boolean isKnown = symqleMethodUniqueness.get(key);
        symqleMethodUniqueness.put(key, isKnown == null);
        anonymousClassByMethod.put(method, anonymousClass);
    }

    public String reducedSignature(MethodDefinition method) {
        final List<FormalParameter> formalParameters = method.getFormalParameters();
        if (formalParameters.size() == 0) {
            return method.getName();
        } else {
            return method.getName() + "(" + Utils.format(formalParameters.subList(1, formalParameters.size()), "", ",", "", new F<FormalParameter, String, RuntimeException>() {
                @Override
                public String apply(final FormalParameter formalParameter) {
                    return formalParameter.getType().getSimpleName();
                }
            }) +")";
        }
    }

    public AnonymousClass getAnonymousClassByMethod(MethodDefinition method) {
        return anonymousClassByMethod.get(method);
    }

    public List<ImplicitConversion> getConversions() {
        return Collections.unmodifiableList(conversions);
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

    public AbstractTypeDefinition getAbstractType(String name) {
        final AbstractTypeDefinition def = classMap.get(name);
        return def;
    }

    public List<InterfaceDefinition> getAllInterfaces() {
        List<InterfaceDefinition> result = new LinkedList<InterfaceDefinition>();
        for (AbstractTypeDefinition candidate: classMap.values()) {
            if (candidate.getClass().equals(InterfaceDefinition.class)) {
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
            if (candidate.getClass().equals(ClassDefinition.class)) {
                result.add((ClassDefinition) candidate);
            }
        }
        return result;
    }

    public List<FactoryMethodModel> getAllFactoryMethods() {
        return new ArrayList<FactoryMethodModel>(factoryMethods.values());
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

    public List<ClassDefinition> getSortedClasses() {
        TSort<ClassDefinition> tSort = new TSort<ClassDefinition>();
        for (ClassDefinition classDef: getAllClasses()) {
            final Type extendedClass = classDef.getExtendedClass();
            if (extendedClass == null) {
                tSort.add(classDef);
            } else {
                final AbstractTypeDefinition parent = classMap.get(extendedClass.getSimpleName());
                if (parent == null || !parent.getClass().equals(ClassDefinition.class)) {
                    tSort.add(classDef);
                } else {
                    tSort.add(classDef, (ClassDefinition) parent);
                }
            }
        }
        return tSort.sort();
    }

}


