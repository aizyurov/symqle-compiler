/*
   Copyright 2011-2014 Alexander Izyurov

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.package org.symqle.common;
*/

package org.symqle.model;

import org.symqle.util.TSort;
import org.symqle.util.Utils;

import java.util.*;

/**
 * Collection of class and interface definitions.
 * @author Alexander Izyurov
 */
public final class Model {

    private final Map<String, AbstractTypeDefinition> classMap = new LinkedHashMap<>();
    private final Map<String, FactoryMethodModel> factoryMethods = new HashMap<>();

    private final Set<String> caseInsensitiveClassNames = new HashSet<>();

    private final List<ImplicitConversion> conversions = new ArrayList<>();
    private final Map<MethodDefinition, Set<String>> explicitSymqleMethods = new LinkedHashMap<>();
    private final Map<MethodDefinition, AnonymousClass> anonymousClassByMethod = new HashMap<>();
    private final Map<String, List<String>> rulesByTargetTypeName = new HashMap<>();

    // key is "reduced signature" -name and afgumetns but the first one
    private final Map<String, Boolean> symqleMethodUniqueness = new HashMap<>();

    private final List<InterfaceDefinition> testInterfaces = new ArrayList<>();

    /**
     * Add an implicit conversion to model..
     * @param conversion what to add
     */
    public void addConversion(final ImplicitConversion conversion) {
        conversions.add(conversion);
    }

    /**
     * Determine whether a Symqle method may be used as default implementation of an interface method
     * (in Java 8 sense).
     * The signature of would-be interface method should be unique within Symqle for a method to
     * be considered unambiguous.
     * @param method the method to investigate
     * @return true if unabimguous
     */
    public boolean isUnambiguous(final MethodDefinition method) {
        return symqleMethodUniqueness.get(reducedSignature(method));
    }

    /**
     * Determine whether a class/interface method may be implemented by delegation to a Symqle method.
     * @param method the method to investigate
     * @return true if Symqle has a method with appropriate signature
     */
    public boolean mayHaveSymqleImplementation(final MethodDefinition method) {
        return symqleMethodUniqueness.containsKey(method.signature());
    }

    /**
     * Add a prototype of new Symqle factory method for abstract class or interface construction.
     * The body of this method would be just "return new ${className}() {}".
     * @param method the method to add; typically abstract at this point. Body will be generated later.
     * @param anonymousClass returned anonymous class as described above
     * @param requiredImports import lines for return type and arguments if in other packages
     */
    public void addExplicitMethod(final MethodDefinition method,
                                  final AnonymousClass anonymousClass,
                                  final Collection<String> requiredImports) {
        explicitSymqleMethods.put(method, new HashSet<String>(requiredImports));
        final String key = reducedSignature(method);
        final Boolean isKnown = symqleMethodUniqueness.get(key);
        symqleMethodUniqueness.put(key, isKnown == null);
        anonymousClassByMethod.put(method, anonymousClass);
    }

    /**
     * Signature of a method but the first argument.
     * See {@link org.symqle.model.MethodDefinition#signature()} for signature format.
     * @param method the method to calculate reduced signature
     * @return reduced signature. For no-arg method returns method name.
     */
    public String reducedSignature(final MethodDefinition method) {
        final List<FormalParameter> formalParameters = method.getFormalParameters();
        if (formalParameters.size() == 0) {
            return method.getName();
        } else {
            HashSet<String> typeParameterNames = new HashSet<String>(method.getTypeParameters().names());
            return method.getName() + "("
                    + Utils.format(formalParameters.subList(1, formalParameters.size()),
                        "", ",", "", FormalParameter.f_erasure(typeParameterNames))
                    + ")";
        }
    }


    /**
     * Finds anonymous class to use in implementation of factory method by factory method.
     * @param method factory method to seek
     * @return corresponding anonymous class; null if not found
     */
    public AnonymousClass getAnonymousClassByMethod(final MethodDefinition method) {
        return anonymousClassByMethod.get(method);
    }

    /**
     * List all implicit conversions.
     * @return immutable list of all known implicit conversions
     */
    public List<ImplicitConversion> getConversions() {
        return Collections.unmodifiableList(conversions);
    }

    /**
     * List all Symqle methods, not associated with syntax rules.
     * @return all explicitly declared methods.
     */
    public List<MethodDefinition> getExplicitSymqleMethods() {
        return new ArrayList<MethodDefinition>(explicitSymqleMethods.keySet());
    }

    /**
     * Add an interface to model.
     * @param def interface definition
     * @throws ModelException duplicate class name
     */
    public void addInterface(final InterfaceDefinition def) throws ModelException {
        addClassOrInterface(def);
    }

    /**
     * Add class or interface to model.
     * @param def class or interface definition
     * @throws ModelException duplicate class name
     */
    private void addClassOrInterface(final AbstractTypeDefinition def) throws ModelException {
        final String name = def.getName();
        if (classMap.containsKey(name)) {
            throw new ModelException("Duplicate class name: " + name);
        } else if (caseInsensitiveClassNames.contains(name.toUpperCase())) {
            throw new ModelException("Name duplicate under Windows: " + name);
        }
        caseInsensitiveClassNames.add(name.toUpperCase());
        classMap.put(name, def);
    }

    /**
     * Find interface by name.
     * @param name interface simple name
     * @return interface definition; null if not found
     * @throws ModelException class with this name is not interface
     */
    public InterfaceDefinition getInterface(final String name) throws ModelException {
        try {
            return (InterfaceDefinition) getAbstractType(name);
        } catch (ClassCastException e) {
            throw new ModelException("Not interface: " + name);
        }
    }

    /**
     * Find class or interface by name.
     * @param name class/interface simple name
     * @return class or interface definition; null if not found
     */
    public AbstractTypeDefinition getAbstractType(final String name) {
        final AbstractTypeDefinition def = classMap.get(name);
        return def;
    }

    /**
     * List all interfaces.
     * @return list of interface definitions. The list is mutable, changes in returned list do not affect model.
     */
    public List<InterfaceDefinition> getAllInterfaces() {
        List<InterfaceDefinition> result = new LinkedList<InterfaceDefinition>();
        for (AbstractTypeDefinition candidate: classMap.values()) {
            if (candidate.getClass().equals(InterfaceDefinition.class)) {
                result.add((InterfaceDefinition) candidate);
            }
        }
        return result;
    }

    /**
     * All classes and interfaces.
     * @return immutable collection of class and interface definitions.
     */
    public Collection<AbstractTypeDefinition> getAllTypes() {
        return Collections.unmodifiableCollection(classMap.values());
    }

    /**
     * Add class definition to model.
     * @param def class definition
     * @throws ModelException duplicate class name
     */
    public void addClass(final ClassDefinition def) throws ModelException {
        addClassOrInterface(def);
    }

    /**
     * Find class definition by name.
     * @param name class simple name
     * @return class definition; null if not found
     * @throws ModelException the name belongs to an interface
     */
    public ClassDefinition getClassDef(final String name) throws ModelException {
        try {
            return (ClassDefinition) getAbstractType(name);
        } catch (ClassCastException e) {
            throw new ModelException("Not interface: " + name);
        }
    }

    /**
     * List all classes. Anonymous classes are not included.
     * @return list of interface definitions. The list is mutable, changes in returned list do not affect model.
     */
    public List<ClassDefinition> getAllClasses() {
        List<ClassDefinition> result = new LinkedList<ClassDefinition>();
        for (AbstractTypeDefinition candidate: classMap.values()) {
            if (candidate.getClass().equals(ClassDefinition.class)) {
                result.add((ClassDefinition) candidate);
            }
        }
        return result;
    }

    /**
     * Find interface by type.
     * @param t type
     * @return interface definition; null if not found
     * @throws ModelException type belongs to a class rather then interface
     */
    public InterfaceDefinition getInterface(final Type t) throws ModelException {
        return getInterface(t.getSimpleName());
    }

    /**
     * Find class by type.
     * @param t type
     * @return class definition; null if not found
     * @throws ModelException type belongs to an interface rather then class
     */
    public ClassDefinition getClassDef(final Type t) throws ModelException {
        return getClassDef(t.getSimpleName());
    }

    /**
     * Add syntax rule.
     * Example:
     * <pre>
     * <code>SelectStatement ::= QueryExpression
     *                       | QueryExpression FOR UPDATE
     * </code>
     * </pre>
     * Call this method twice to save these rules:
     * <pre>
     * <code>
     * add("SelectStatement", "QueryExpression");
     * add("SelectStatement", "QueryExpression FOR UPDATE");
     * </code>
     * </pre>
     * Rules are memorized in this format for later use mostly in javadoc. So, type parameters, variable names and
     * other java semantic information should be stripped, just plain BNF.
     * @param targetTypeName rule goal
     * @param rule rule body
     */
    public void addRule(final String targetTypeName, final String rule) {
        List<String> rules = rulesByTargetTypeName.get(targetTypeName);
        if (rules == null) {
            rules = new ArrayList<String>();
            rulesByTargetTypeName.put(targetTypeName, rules);
        }
        rules.add(rule);
    }

    /**
     * Get syntax rules for given target.
     * See @{link #addRule}.
     * @param targetTypeName rule target
     * @return all rules
     */
    public List<String> getRules(final String targetTypeName) {
        final List<String> rules = rulesByTargetTypeName.get(targetTypeName);
        return rules == null ? null : Collections.unmodifiableList(rules);
    }

    /**
     * List all model classes topologically sorted from ancestors to descendants.
     * @return list of class definitions. The list is mutable, changes do not affect model.
     */
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

    /**
     * Add test interface (generated code will go to tests directory).
     * The method does not check for duplicate interface names. If there are any,
     * generated test code would not compile.
     * @param classDef interface definition
     */
    public void addTestInterface(final InterfaceDefinition classDef) {
        testInterfaces.add(classDef);
    }

    /**
     * List test interfaces.
     * @return unmodifiable list of interface definitions.
     */
    public List<InterfaceDefinition> getTestInterfaces() {
        return Collections.unmodifiableList(testInterfaces);
    }
}


