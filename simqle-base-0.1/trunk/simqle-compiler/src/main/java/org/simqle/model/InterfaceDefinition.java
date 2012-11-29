/*
* Copyright Alexander Izyurov 2010
*/
package org.simqle.model;

import org.simqle.parser.SyntaxTree;
import org.simqle.processor.GrammarException;
import org.simqle.util.Assert;
import org.simqle.util.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <br/>13.11.2011
 *
 * @author Alexander Izyurov
 */
public class InterfaceDefinition extends AbstractTypeDefinition {
    private final List<Type> extended;
    private final MethodDefinition archetypeMethod;

    public InterfaceDefinition(SyntaxTree node) throws GrammarException {
        super(node);
        Assert.assertOneOf(new GrammarException("Unexpected type: "+node.getType(), node), node.getType(), "SimqleInterfaceDeclaration");

        this.extended = node.find("ExtendsInterfaces.ClassOrInterfaceType", Type.CONSTRUCT);
        // everything is constructed; apply archetype (by syntax the loo pis
        // executed 0 or 1 times
        try {
            Archetype.verify(this);
        } catch (ModelException e) {
            throw new GrammarException(e, node);
        }
        List<SyntaxTree> archetypeNodes = node.find("Archetype");
        try {
            final Archetype archetype = archetypeNodes.isEmpty() ? Archetype.NONE : Archetype.create(archetypeNodes.get(0));
            archetypeMethod = archetype.createArchetypeMethod(this);
            this.addMethod(archetypeMethod);
        } catch (ModelException e) {
            e.printStackTrace();
            throw new GrammarException(e, node);
        }
    }

    public MethodDefinition getArchetypeMethod() throws ModelException {
        if (archetypeMethod==null) {
            throw new ModelException("Interface "+getName()+" does not have archetype");
        }
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
    protected final Type getAncestorTypeByName(final String name) {
        for (Type t: extended) {
            if (name.equals(t.getSimpleName())) {
                return t;
            }
        }
        throw new IllegalArgumentException(getName() + " does not implement " + name);
    }

    @Override
    protected String getExtendsImplements() {
        return Utils.format(extended, "extends ", ", ", "");
    }

    public static F<SyntaxTree, InterfaceDefinition, GrammarException> CONSTRUCT =
            new F<SyntaxTree, InterfaceDefinition, GrammarException>() {
                @Override
                public InterfaceDefinition apply(SyntaxTree syntaxTree) throws GrammarException {
                    return new InterfaceDefinition(syntaxTree);
                }
            };

    @Override
    public final String implicitMethodAccessModifier(final MethodDefinition methodDefinition) {
        // just copy
        return methodDefinition.getAccessModifier();
    }

    @Override
    public final Set<String> addImplicitMethodModifiers(final MethodDefinition methodDefinition) {
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


