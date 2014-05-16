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

import org.symqle.parser.ParseException;
import org.symqle.parser.SimpleNode;
import org.symqle.parser.SymqleParser;
import org.symqle.parser.SyntaxTree;
import org.symqle.processor.GrammarException;
import org.symqle.util.AssertNodeType;
import org.symqle.util.Log;
import org.symqle.util.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Java class definition.
 * @author Alexander Izyurov
 */
public class ClassDefinition extends AbstractTypeDefinition {

    // null if does not extend nothing but Object
    private final Type extendedClass;

//    private final List<Type> implementedInterfaces;

    private  Set<Type> implementedInterfaces;

    // Implemented interface: key, inplemented via: value
    // directly implemented interfaces are not included
    private Map<Type, Type> pathInfo = new HashMap<Type, Type>();


    /**
     * Parses source. The source should be class declaration as per JLS, without package and import statements.
     * @param source the source
     * @return constructed class definition
     */
    public static ClassDefinition parse(final String source) {
        try {
            final SimpleNode simpleNode = SymqleParser.createParser(source).NormalClassDeclaration();
            SyntaxTree syntaxTree = new SyntaxTree(simpleNode, source);
            return new ClassDefinition(syntaxTree);
        } catch (ParseException e) {
            throw new RuntimeException("Internal error", e);
        } catch (GrammarException e) {
            throw new RuntimeException("Internal error", e);
        }
    }

    @Override
    protected final String getTypeKeyword() {
        return "class";
    }

    @Override
    public final Set<Type> getAllAncestors(final Model model) throws ModelException {
        final Set<Type> ancestors = new HashSet<Type>();
        if (extendedClass != null) {
            ancestors.add(extendedClass);
            ancestors.addAll(getInheritedAncestors(extendedClass, model));
        }
        for (Type type: getImplementedInterfaces()) {
            ancestors.add(type);
            ancestors.addAll(getInheritedAncestors(type, model));
        }
        return ancestors;
    }

    /**
     * Direct superinterfaces.
     * @return declared interfaces.
     */
    public final List<Type> getImplementedInterfaces() {
        return new ArrayList<Type>(implementedInterfaces);
    }

    /**
     * If the class has at least one abstract or not implemented method, make the class abstract.
     * @param model model to analyze inherited methods.
     * @throws ModelException wrong model, e.g. method name clash.
     */
    public final void makeAbstractIfNeeded(final Model model) throws ModelException {
        for (MethodDefinition method: getAllMethods(model)) {
            if (method.getOtherModifiers().contains("abstract")
                    || method.getOtherModifiers().contains("volatile") && method.isAbstract()) {
                Log.debug(getName() + " made abstract due to " + method);
                makeAbstract();

                return;
            }
        }
    }

    /**
     * Constructs from AST. NormalClassDeclaration expected.
     * @param node the syntax tree
     * @throws GrammarException wrong tree
     */
    public ClassDefinition(final SyntaxTree node) throws GrammarException {
        super(node);
        AssertNodeType.assertOneOf(node, "NormalClassDeclaration");
        final List<SyntaxTree> extendedTypes = node.find("Super.ClassOrInterfaceType");
        if (extendedTypes.isEmpty()) {
            this.extendedClass = null;
        } else {
            this.extendedClass = new Type(extendedTypes.get(0));
        }
        this.implementedInterfaces = new HashSet<Type>();
        for (Type type: node.find("Interfaces.ClassOrInterfaceType", Type.CONSTRUCT)) {
            implementedInterfaces.add(type);
        }
    }

    /**
     * Adds implemented interface.
     * Duplicates make no harm.
     * @param interfaceType interface
     * @throws ModelException
     */
    public final void addImplementedInterface(final Type interfaceType) {
        implementedInterfaces.add(interfaceType);
    }

    @Override
    protected final String getExtendsImplements() {
        return (extendedClass == null
                ? ""
                : "extends " + extendedClass.toString() + " ") + Utils.format(getImplementedInterfaces(),
                        "implements ",
                        ",\n                                                            ",
                        "");
    }

    @Override
    public final Map<String, MethodDefinition> getAllMethodsMap(final Model model) throws ModelException {
        final Map<String, MethodDefinition> methodMap = new HashMap<String, MethodDefinition>();
        for (MethodDefinition method: getDeclaredMethods()) {
            methodMap.put(method.signature(), method);
        }
        if (extendedClass != null) {
            addInheritedMethodsToMap(model, methodMap, extendedClass);
        }
        for (Type parentType: getImplementedInterfaces()) {
            addInheritedMethodsToMap(model, methodMap, parentType);
        }
        return methodMap;
    }

    @Override
    protected final Type getAncestorTypeByName(final String ancestorName) {
        if (extendedClass != null && ancestorName.equals(extendedClass.getSimpleName())) {
            return extendedClass;
        }
        for (Type t : getImplementedInterfaces()) {
            if (ancestorName.equals(t.getSimpleName())) {
                return t;
            }
        }
        throw new IllegalArgumentException(getName() + " does not implement " + ancestorName);
    }

    @Override
    public final String implicitMethodAccessModifier(final MethodDefinition methodDefinition) {
        // add public if absent
        return methodDefinition.isPublic() ? "public" : methodDefinition.getAccessModifier();
    }

    @Override
    public final Set<String> implicitMethodModifiers(final MethodDefinition methodDefinition) {
        // add abstract if absent
        final HashSet<String> newModifiers = new HashSet<String>(methodDefinition.getOtherModifiers());
        if (methodDefinition.isAbstract()) {
            newModifiers.add("abstract");
        }
        return newModifiers;
    }

    @Override
    public final boolean methodIsAbstract(final Set<String> modifiers) {
        return modifiers.contains("abstract");
    }

    @Override
    public final boolean methodIsPublic(final String explicitAccessModifier) {
        return explicitAccessModifier.equals("public");
    }

    /**
     * Add imports for classes, which are used in signatures of implemented interface methods.
     * Import line are copied from all implemented interfaces.
     * @param model contains all interfaces
     * @throws ModelException wrong model
     */
    public final void ensureRequiredImports(final Model model) throws ModelException {
        for (Type ancestor: getAllAncestors(model)) {
            // copy imports from implemented interfaces: they are needed for method declarations
            // if something is needed from extended c lasses, it should be copied manually
            if (model.getAbstractType(ancestor.getSimpleName()).getClass().equals(InterfaceDefinition.class)) {
                addImportLines(model.getAbstractType(ancestor.getSimpleName()).getImportLines());
            }
        }
    }

    /**
     * Adds implicit conversion chain. {@code this} can implement {@code to}
     * because it implements {@code from} and {@code from} is convertible to {@code to}.
     * @param to target interface type
     * @param from mediator interface type
     */
    public final void addPath(final Type to, final Type from) {
        pathInfo.put(to, from);
    }

    /**
     * The number of implicit conversion steps to get {@code type} from {@code this}.
     * @param type target interface type
     * @param model contains all known interfaces and classes
     * @return the number. 0 for directly implemented interfaces.
     * @throws ModelException wrong model
     * @throws IllegalArgumentException {@code type} is not implemented by {@code this}
     */
    public final int distance(final Type type, final Model model) throws ModelException {
        if (!getAllAncestors(model).contains(type)) {
            throw new IllegalArgumentException(type + " is not ancestor of " + this.getType());
        }
        final Type previous = pathInfo.get(type);
        if (previous == null) {
            return 0;
        } else {
            return 1 + distance(previous, model);
        }
    }

    /**
     * Superclass.
     * @return superclass, null if superclass is not declared explicitly (and thus is Object).
     */
    public final Type getExtendedClass() {
        return extendedClass;
    }

    /**
     * Remove redundant declared interfaces. Declared superinterface is redundant if it
     * is also inherited transifively via another interface or superclass.
     * For example, {@code extends List<String>, Collection<String>, Serializable} would shrink to
     * {@code implements List<String>}.
     * @param model the model containing all classes and interfaces
     * @throws ModelException wrong model (e.g. same interface inherited twice with different type parameters)
     */
    public final void removeRedundantInterfaces(final Model model) throws ModelException {
        final Set<Type> indirectInterfaces = new HashSet<Type>();
        for (Type t : implementedInterfaces) {
            indirectInterfaces.addAll(getInheritedAncestors(t, model));
        }
        implementedInterfaces.removeAll(indirectInterfaces);
    }
}
