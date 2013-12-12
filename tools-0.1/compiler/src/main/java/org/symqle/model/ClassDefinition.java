/*
* Copyright Alexander Izyurov 2010
*/
package org.symqle.model;

import org.symqle.parser.ParseException;
import org.symqle.parser.SimpleNode;
import org.symqle.parser.SyntaxTree;
import org.symqle.processor.GrammarException;
import org.symqle.util.Assert;
import org.symqle.util.Utils;

import java.util.*;

/**
 * <br/>19.11.2011
 *
 * @author Alexander Izyurov
 */
public class ClassDefinition extends AbstractTypeDefinition {

    // null if does not extend nothing but Object
    private final Type extendedClass;

//    private final List<Type> implementedInterfaces;

    private  Set<Type> implementedInterfaces;

    // Implemented interface: kew, inplemented via: value
    // directly implemented interfaces are not included
    private Map<Type, Type> pathInfo = new HashMap<Type, Type>();


    public static ClassDefinition parse(final String source) {
        try {
            final SimpleNode simpleNode = Utils.createParser(source).NormalClassDeclaration();
            SyntaxTree syntaxTree = new SyntaxTree(simpleNode, source);
            return new ClassDefinition(syntaxTree);
        } catch (ParseException e) {
            throw new RuntimeException("Internal error", e);
        } catch (GrammarException e) {
            throw new RuntimeException("Internal error", e);
        }
    }

    @Override
    protected String getTypeKeyword() {
        return "class";
    }

    @Override
    public Set<Type> getAllAncestors(Model model) throws ModelException {
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

    public List<Type> getImplementedInterfaces() {
        return new ArrayList<Type>(implementedInterfaces);
    }

    public void makeAbstractIfNeeded(Model model) throws ModelException {
        for (MethodDefinition method: getAllMethods(model)) {
            if (method.getOtherModifiers().contains("abstract") ||
                    ( method.getOtherModifiers().contains("volatile") && method.isAbstract())) {
//                System.out.println(getName()+ " made abstract due to "+method);
                makeAbstract();

                return;
            }
        }
    }

    public ClassDefinition(SyntaxTree node) throws GrammarException {
        super(node);
        Assert.assertOneOf(new GrammarException("Unexpected type: "+node.getType(), node), node.getType(), "NormalClassDeclaration");
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

    public void addImplementedInterface(final Type interfaceType) throws ModelException {
        implementedInterfaces.add(interfaceType);
    }

    @Override
    protected String getExtendsImplements() {
        return (extendedClass == null ? "" :
                "extends " + extendedClass.toString() + " ") +
                Utils.format(getImplementedInterfaces(),
                        "implements ",
                        ",\n                                                            ",
                        "");
    }

    @Override
    public Map<String, MethodDefinition> getAllMethodsMap(Model model) throws ModelException {
        final Map<String, MethodDefinition> methodMap = new HashMap<String, MethodDefinition>();
        for (MethodDefinition method: getDeclaredMethods()) {
            methodMap.put(method.signature(), method);
        }
        if (extendedClass !=null) {
            addInheritedMethodsToMap(model, methodMap, extendedClass);
        }
        for (Type parentType: getImplementedInterfaces()) {
            addInheritedMethodsToMap(model, methodMap, parentType);
        }
        return methodMap;
    }

    @Override
    protected Type getAncestorTypeByName(final String name) {
        if (extendedClass!=null && name.equals(extendedClass.getSimpleName())) {
            return extendedClass;
        }
        for (Type t: getImplementedInterfaces()) {
            if (name.equals(t.getSimpleName())) {
                return t;
            }
        }
        throw new IllegalArgumentException(getName() + " does not implement " + name);
    }

    @Override
    public String implicitMethodAccessModifier(final MethodDefinition methodDefinition) {
        // add public if absent
        return methodDefinition.isPublic() ? "public" : methodDefinition.getAccessModifier();
    }

    @Override
    public Set<String> addImplicitMethodModifiers(final MethodDefinition methodDefinition) {
        // add abstract if absent
        final HashSet<String> newModifiers = new HashSet<String>(methodDefinition.getOtherModifiers());
        if (methodDefinition.isAbstract()) {
            newModifiers.add("abstract");
        }
        return newModifiers;
    }

    @Override
    public boolean methodIsAbstract(final Set<String> modifiers) {
        return modifiers.contains("abstract");
    }

    @Override
    public boolean methodIsPublic(final String explicitAccessModifier) {
        return explicitAccessModifier.equals("public");
    }

    public void ensureRequiredImports(final Model model) throws ModelException {
        for (Type ancestor: getAllAncestors(model)) {
            addImportLines(model.getAbstractType(ancestor.getSimpleName()).getImportLines());
        }
    }

    public void addPath(Type to, Type from) {
        pathInfo.put(to, from);
    }

    public int distance(Type type, Model model) throws ModelException {
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

    public Type getExtendedClass() {
        return extendedClass;
    }
}
