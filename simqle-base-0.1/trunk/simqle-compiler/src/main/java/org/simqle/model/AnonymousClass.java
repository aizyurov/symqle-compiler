package org.simqle.model;

import org.simqle.parser.SyntaxTree;
import org.simqle.processor.GrammarException;
import org.simqle.util.Utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Anonymous class is created for each ProductionImplementation and each
 * standalone MethodDeclaration
 * the methods (which belong to Simqle class) are always standard:
 * return new ReturnType() {...} - this is the anonymous class.
 * The anonymous class always extends its ReturnType (class or interface).
 * Name is composed from methodName; actually it does not matter because
 * anonymous classes are never registered with the Model
 * @author lvovich
 */
public class AnonymousClass extends AbstractTypeDefinition {
    // class or interface - does not matter
    private Type extended;

    public AnonymousClass(final SyntaxTree node) throws GrammarException {
        super(node);

        final Type defaultExtended = node.find("^.^.ClassOrInterfaceType", Type.CONSTRUCT).get(0);
        final List<Type> overridingExtended = node.find("^.ProductionImplementation.ClassOrInterfaceType", Type.CONSTRUCT);

        extended = overridingExtended.isEmpty() ? defaultExtended : overridingExtended.get(0);
    }



    @Override
    protected String getTypeKeyword() {
        return "class";
    }

    @Override
    protected Set<AbstractTypeDefinition> getAllAncestors(Model model) throws ModelException {
        final Set<AbstractTypeDefinition> ancestors = new HashSet<AbstractTypeDefinition>();
        final AbstractTypeDefinition ancestor = model.getAbstractType(extended.getSimpleName());
        ancestors.add(ancestor);
        ancestors.addAll(ancestor.getAllAncestors(model));
        return ancestors;
    }

    @Override
    public String implicitMethodAccessModifier(final MethodDefinition methodDefinition) {
        return "public";
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
        return "public".equals(explicitAccessModifier);
    }

    public String instanceBodyAsString() {
        return (" {") + Utils.LINE_BREAK +
                bodyStringWithoutBraces() +
                Utils.LINE_BREAK +"        }";
    }

    @Override
    protected Map<String, MethodDefinition> getAllMethodsMap(final Model model) throws ModelException {
        final Map<String, MethodDefinition> methodMap = new HashMap<String, MethodDefinition>();
        for (MethodDefinition method: getDeclaredMethods()) {
            methodMap.put(method.signature(), method);
        }
        // extended is never null for AnonymousClass
        addInheritedMethodsToMap(model, methodMap, extended);
        return methodMap;
    }

    @Override
    protected String getExtendsImplements() {
        // will produce non-compilable code if extended is interface
        return "extends " + extended;
    }

    @Override
    protected Type getAncestorTypeByName(final String name) {
        if (name.equals(extended.getSimpleName())) {
            return extended;
        }
        throw new IllegalArgumentException(getName() + " does not implement " + name);
    }
}
