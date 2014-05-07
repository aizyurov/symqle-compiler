/*
* Copyright Alexander Izyurov 2010
*/
package org.symqle.model;

import org.symqle.parser.SyntaxTree;
import org.symqle.processor.GrammarException;
import org.symqle.util.Assert;
import org.symqle.util.Utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Java interface definition.
 *
 * @author Alexander Izyurov
 */
public class InterfaceDefinition extends AbstractTypeDefinition {
    private final List<Type> extended;
    private final MethodDefinition archetypeMethod;
    private final Set<String> delegatedMethodsSignatures = new HashSet<String>();

    /**
     * Constructs from AST.
     * @param node syntax tree
     * @throws GrammarException wrong tree
     */
    public InterfaceDefinition(final SyntaxTree node) throws GrammarException {
        super(node);
        Assert.assertOneOf(new GrammarException("Unexpected type: " + node.getType(), node),
                node.getType(), "SymqleInterfaceDeclaration");

        this.extended = node.find("ExtendsInterfaces.ClassOrInterfaceType", Type.CONSTRUCT);
        // everything is constructed; apply archetype (by syntax the loop is
        // executed 0 or 1 times
        try {
            Archetype.verify(this);
        } catch (ModelException e) {
            throw new GrammarException(e, node);
        }
        List<SyntaxTree> archetypeNodes = node.find("Archetype");
        try {
            final Archetype archetype = archetypeNodes.isEmpty()
                    ? Archetype.NONE
                    : Archetype.create(archetypeNodes.get(0));
            archetypeMethod = archetype.createArchetypeMethod(this);
            if (archetypeMethod != null) {
                this.addMethod(archetypeMethod);
            }
            this.addImportLines(archetype.getRequiredImports());
        } catch (ModelException e) {
            e.printStackTrace();
            throw new GrammarException(e, node);
        }
    }

    /**
     * Add method, which has default implementation as static method of Symqle.
     * Methods are currently implemented in derived classes; waiting for Java 8.
     * @param method method, which can have default implementation
     * @throws ModelException duplicate method
     */
    public final void addDelegateMethod(final MethodDefinition method) throws ModelException {
        addMethod(method);
        delegatedMethodsSignatures.add(method.signature());
    }

    @Override
    protected final String getTypeKeyword() {
        return "interface";
    }

    @Override
    public final Set<Type> getAllAncestors(final Model model) throws ModelException {
        final Set<Type> ancestors = new HashSet<Type>();
        for (Type type: extended) {
            ancestors.add(type);
            ancestors.addAll(getInheritedAncestors(type, model));
        }
        return ancestors;
    }

    /**
     * Archetype method.
     * @return the method, null if it is not archetyped interface
     * @throws ModelException
     */
    public final MethodDefinition getArchetypeMethod() {
        return archetypeMethod;
    }

    @Override
    protected final Map<String, MethodDefinition> getAllMethodsMap(final Model model) throws ModelException {
        // Map of methods by signature
        Map<String, MethodDefinition> allMethods = new HashMap<String, MethodDefinition>();
        for (MethodDefinition method: getDeclaredMethods()) {
            allMethods.put(method.signature(), method);
        }
        for (Type type: extended) {
            addInheritedMethodsToMap(model, allMethods, type);
        }
        return allMethods;
    }

    @Override
    protected final Type getAncestorTypeByName(final String ancestorName) {
        for (Type t: extended) {
            if (ancestorName.equals(t.getSimpleName())) {
                return t;
            }
        }
        throw new IllegalArgumentException(getName() + " does not implement " + ancestorName);
    }

    @Override
    protected final String getExtendsImplements() {
        return Utils.format(extended, "extends ", ", ", "");
    }

    /**
     * Function, which converts SyntaxTree to InterfaceDefinition.
     */
    public static final F<SyntaxTree, InterfaceDefinition, GrammarException> CONSTRUCT =
            new F<SyntaxTree, InterfaceDefinition, GrammarException>() {
                @Override
                public InterfaceDefinition apply(final SyntaxTree syntaxTree) throws GrammarException {
                    return new InterfaceDefinition(syntaxTree);
                }
            };

    @Override
    public final String implicitMethodAccessModifier(final MethodDefinition methodDefinition) {
        // just copy
        return methodDefinition.getAccessModifier();
    }

    @Override
    public final Set<String> implicitMethodModifiers(final MethodDefinition methodDefinition) {
        // just copy
        return methodDefinition.getOtherModifiers();
    }

    @Override
    public final boolean methodIsAbstract(final Set<String> modifiers) {
        return true;
    }

    @Override
    public final boolean methodIsPublic(final String explicitAccessModifier) {
        return true;
    }
}


