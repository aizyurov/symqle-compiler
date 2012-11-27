/*
* Copyright Alexander Izyurov 2010
*/
package org.simqle.model;

import org.simqle.parser.SyntaxTree;
import org.simqle.processor.GrammarException;
import org.simqle.util.Assert;
import org.simqle.util.Utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <br/>19.11.2011
 *
 * @author Alexander Izyurov
 */
public class ClassDefinition extends AbstractTypeDefinition {

    // null if does not extend nothing but Object
    private final Type extendedClass;

    private final List<Type> implementedInterfaces;


    public ClassDefinition(SyntaxTree node) throws GrammarException {
        super(node);
        Assert.assertOneOf(new GrammarException("Unexpected type: "+node.getType(), node), node.getType(), "NormalClassDeclaration");
        final List<SyntaxTree> extendedTypes = node.find("Super.ClassOrInterfaceType");
        if (extendedTypes.isEmpty()) {
            this.extendedClass = null;
        } else {
            this.extendedClass = new Type(extendedTypes.get(0));
        }
        this.implementedInterfaces = node.find("Interfaces.ClassOrInterfaceType", Type.CONSTRUCT);
    }

    public void addImplementedInterface(final Type interfaceType) {
        // TODO check for duplicates
        implementedInterfaces.add(interfaceType);
    }

    @Override
    protected String getExtendsImplements() {
        return (extendedClass == null ? "" :
                "extends " + extendedClass.toString()) + Utils.format(implementedInterfaces, "implements ", ", ", "");
    }

    @Override
    public Map<String, MethodDefinition> getAllMethodsMap(Model model) throws ModelException {
        final Map<String, MethodDefinition> methodMap = new HashMap<String, MethodDefinition>();
        for (MethodDefinition method: getDeclaredMethods()) {
            methodMap.put(method.signature(), method);
        }
        if (extendedClass!=null) {
            ClassDefinition parent = model.getClassDef(extendedClass);
            for (MethodDefinition method: parent.getAllMethods(model)) {
                if (!"private".equals(method.getAccessModifier())) {
                    String signature = method.signature();
                    MethodDefinition myMethod = methodMap.get(signature);
                    final MethodDefinition candidate = method.override(this, model);
                    if (myMethod == null) {
                        // add fake method if possible: we do not care about body
                        methodMap.put(signature, candidate);
                    } else {
                        // make sure it is Ok to override
                        if (!myMethod.matches(candidate)) {
                            throw new ModelException("Name clash in " + getName() + "#"+myMethod.declaration() + " and " + candidate.declaration());
                        } else {
                            // do not add: it isoverridden.
                            // leaving decrease of access check to Java compiler
                        }
                    }
                }
            }
        }
        return methodMap;
    }

    @Override
    protected Type getAncestorTypeByName(final String name) {
        if (name.equals(extendedClass.getSimpleName())) {
            return extendedClass;
        }
        for (Type t: implementedInterfaces) {
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
}
