/*
* Copyright Alexander Izyurov 2010
*/
package org.simqle.model;

import org.simqle.parser.ParseException;
import org.simqle.parser.SimpleNode;
import org.simqle.parser.SyntaxTree;
import org.simqle.processor.GrammarException;
import org.simqle.util.Assert;
import org.simqle.util.Utils;

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

    private final Map<String, ImplementationInfo> implementedInterfaces;


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
    protected Set<AbstractTypeDefinition> getAllAncestors(Model model) throws ModelException {
        final Set<AbstractTypeDefinition> ancestors = new HashSet<AbstractTypeDefinition>();
        if (extendedClass != null) {
            final AbstractTypeDefinition ancestor = model.getAbstractType(extendedClass.getSimpleName());
            ancestors.add(ancestor);
            ancestors.addAll(ancestor.getAllAncestors(model));
        }
        for (Type type: getImplementedInterfaces()) {
            final AbstractTypeDefinition ancestor = model.getAbstractType(type.getSimpleName());
            ancestors.add(ancestor);
            ancestors.addAll(ancestor.getAllAncestors(model));
        }
        return ancestors;
    }

    private final static F<ImplementationInfo, Type, RuntimeException> TYPE = new F<ImplementationInfo, Type, RuntimeException>() {
        @Override
        public Type apply(final ImplementationInfo implementationInfo) {
            return implementationInfo.type;
        }
    };


    public List<Type> getImplementedInterfaces() {
        return new ArrayList<Type>(Utils.map(implementedInterfaces.values(), TYPE));
    }

    public int getPriority(Type type) {
        final ImplementationInfo implementationInfo = implementedInterfaces.get(type.getSimpleName());
        if (implementationInfo == null) {
            throw new IllegalArgumentException("Interface not found: " + type.getSimpleName());
        } else {
            return implementationInfo.priority;
        }
    }


    public void makeAbstractIfNeeded(Model model) throws ModelException {
        for (MethodDefinition method: getAllMethods(model)) {
            if (method.getOtherModifiers().contains("abstract") ||
                    method.getOtherModifiers().contains("transient")) {
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
        this.implementedInterfaces = new HashMap<String, ImplementationInfo>();
        for (Type type: node.find("Interfaces.ClassOrInterfaceType", Type.CONSTRUCT)) {
            implementedInterfaces.put(type.getSimpleName(), new ImplementationInfo(type, 0));
        }
    }

    public void addImplementedInterface(final Type interfaceType, int priority) throws ModelException {
        // TODO check for duplicates
        if (null != implementedInterfaces.put(interfaceType.getSimpleName(), new ImplementationInfo(interfaceType, priority))) {
            throw new ModelException("Duplicate implemented interface: " +  interfaceType.getSimpleName());
        }
    }

    @Override
    protected String getExtendsImplements() {
        return (extendedClass == null ? "" :
                "extends " + extendedClass.toString() + " ") + Utils.format(getImplementedInterfaces(), "implements ", ", ", "");
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
        for (AbstractTypeDefinition ancestor: getAllAncestors(model)) {
            addImportLines(ancestor.getImportLines());
        }
    }

    private static class ImplementationInfo {
        final Type type;
        // the less, the more priority
        final int priority;

        private ImplementationInfo(final Type type, final int priority) {
            this.type = type;
            this.priority = priority;
        }
    }

}
