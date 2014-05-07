package org.symqle.model;

import org.symqle.parser.SyntaxTree;
import org.symqle.processor.GrammarException;
import org.symqle.util.Utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Anonymous class is created for each ProductionImplementation and each
 * standalone MethodDeclaration
 * the methods (which belong to Symqle class) are always standard:
 * return new ReturnType() {...} - this is the anonymous class.
 * The anonymous class always extends its ReturnType (class or interface).
 * Name is composed from methodName; actually it does not matter because
 * anonymous classes are never registered with the Model
 * @author lvovich
 */
public class AnonymousClass extends AbstractTypeDefinition {
    // class or interface - does not matter
    private Type extended;

    /**
     * Constructs from AST. Extended type is supplied separately
     * (cannot be determined from ProductionImplementation tree)
     * @param node should be ProductionImplementation or MethodDeclaration
     * @param extendedType the parent type
     * @throws GrammarException wrong tree
     */
    public AnonymousClass(final SyntaxTree node, final Type extendedType) throws GrammarException {
        super(node);

        extended = extendedType;
    }



    @Override
    protected final String getTypeKeyword() {
        return "class";
    }

    @Override
    public final Set<Type> getAllAncestors(final Model model) throws ModelException {
        final Set<Type> ancestors = new HashSet<Type>();
        final Type parentType = extended;
        ancestors.add(parentType);
        final Set<Type> myAncestors = getInheritedAncestors(parentType, model);
        ancestors.addAll(myAncestors);
        return ancestors;
    }

    @Override
    public final String implicitMethodAccessModifier(final MethodDefinition methodDefinition) {
        return "public";
    }

    @Override
    public final Set<String> implicitMethodModifiers(final MethodDefinition methodDefinition) {
        // add abstract if absent. Anonymous class can temporarily have "abstract volatile" methods.
        // at the end of construction all methods whould be implemented.
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
        return "public".equals(explicitAccessModifier);
    }

    /**
     * Class body, including enclosing braces.
     * @return the body.
     */
    public final String instanceBodyAsString() {
        return " {" + Utils.LINE_BREAK
                + bodyStringWithoutBraces()
                + Utils.LINE_BREAK + "        }";
    }

    @Override
    protected final Map<String, MethodDefinition> getAllMethodsMap(final Model model) throws ModelException {
        final Map<String, MethodDefinition> methodMap = new HashMap<String, MethodDefinition>();
        for (MethodDefinition method: getDeclaredMethods()) {
            methodMap.put(method.signature(), method);
        }
        // extended is never null for AnonymousClass
        addInheritedMethodsToMap(model, methodMap, extended);
        return methodMap;
    }

    @Override
    public final String getExtendsImplements() {
        throw new IllegalStateException("Method not applicable");
    }

    /**
     * Parent type.
     * @return parent
     */
    public final Type getParent() {
        return extended;
    }

    @Override
    protected final Type getAncestorTypeByName(final String ancestorName) {
        if (ancestorName.equals(extended.getSimpleName())) {
            return extended;
        }
        throw new IllegalArgumentException(getName() + " does not implement " + ancestorName);
    }
}
