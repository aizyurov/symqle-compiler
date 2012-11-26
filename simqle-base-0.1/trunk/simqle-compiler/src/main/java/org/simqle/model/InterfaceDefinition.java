/*
* Copyright Alexander Izyurov 2010
*/
package org.simqle.model;

import org.simqle.parser.SyntaxTree;
import org.simqle.processor.GrammarException;
import org.simqle.util.Assert;
import org.simqle.util.Utils;

import java.util.Collection;
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

    public InterfaceDefinition(SyntaxTree node) throws GrammarException {
        super(node);
        Assert.assertOneOf(new GrammarException("Unexpected type: "+node.getType(), node), node.getType(), "SimqleInterfaceDeclaration");

        this.extended = node.find("ExtendsInterfaces.ClassOrInterfaceType", Type.CONSTRUCT);
        // everything is constructed; apply archetype (by syntax the loo pis
        // executed 0 or 1 times
        for (SyntaxTree archetypeNode: node.find("Archetype")) {
            try {
                Archetype.create(archetypeNode).apply(this);
            } catch (ModelException e) {
                throw new GrammarException(e.getMessage(), archetypeNode);
            }
        }
    }


    @Override
    public Collection<MethodDefinition> getAllMethods(final Model model) throws ModelException {
        // Map of methods by signature
        Map<String, MethodDefinition> allMethods = collectAllMethods(model);
        return allMethods.values();
    }

    @Override
    protected Type getAncestorTypeByName(final String name) {
        for (Type t: extended) {
            if (name.equals(t.getSimpleName())) {
                return t;
            }
        }
        throw new IllegalArgumentException(getName() + " does not implement " + name);
    }

    private Map<String, MethodDefinition> collectAllMethods(final Model model) throws ModelException {
        Map<String, MethodDefinition> allMethods = new HashMap<String, MethodDefinition>();
        for (MethodDefinition method: getDeclaredMethods()) {
            allMethods.put(method.signature(), method);
        }
        for (Type type: extended) {
            InterfaceDefinition parent = model.getInterface(type);
            for (MethodDefinition parentMethod: parent.getAllMethods(model)) {
                MethodDefinition candidate = parentMethod.override(this);
                MethodDefinition myMethod = allMethods.get(candidate.signature());
                if (myMethod == null) {
                    allMethods.put(candidate.signature(), candidate);
                } else {
                    // check that methods are the same
                    if (!myMethod.matches(candidate)) {
                        throw new ModelException("Name clash in " + getName()+"#" +
                                myMethod.declaration() + " and " + candidate.declaration());
                    }
                }
            }
        }
        return allMethods;
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
    public String implicitMethodAccessModifier(final MethodDefinition methodDefinition) {
        // just copy
        return methodDefinition.getAccessModifier();
    }

    @Override
    public Set<String> addImplicitMethodModifiers(final MethodDefinition methodDefinition) {
        // just copy
        return methodDefinition.getOtherModifiers();
    }

    @Override
    public boolean makeMethodAbstract(final Set<String> modifiers) {
        return true;
    }

    @Override
    public boolean makeMethodPublic(final String explicitAccessModifier) {
        return false;
    }
}


