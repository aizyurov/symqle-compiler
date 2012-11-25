/*
* Copyright Alexander Izyurov 2010
*/
package org.simqle.model;

import org.simqle.parser.SyntaxTree;
import org.simqle.processor.GrammarException;
import org.simqle.util.Assert;
import org.simqle.util.Utils;

import java.util.*;

import static org.simqle.util.Utils.convertChildren;

/**
 * <br/>13.11.2011
 *
 * @author Alexander Izyurov
 */
public class InterfaceDefinition {
    private final Set<String> importLines;
    private final String name;
    private final String accessModifier;
    private final Set<String> otherModifiers;
    private final TypeParameters typeParameters;
    private final List<Type> extended;
    private final Body body;

    // presentation part
    private final String comment;

    public InterfaceDefinition(SyntaxTree node) throws GrammarException {
        Assert.assertOneOf(new GrammarException("Unexpected type: "+node.getType(), node), node.getType(), "SimqleInterfaceDeclaration");

        this.importLines = new TreeSet<String>(node.getParent().getParent().find("ImportDeclaration", SyntaxTree.BODY));
        final List<SyntaxTree> modifierNodes = node.find("InterfaceModifiers.InterfaceModifier");
        this.accessModifier = Utils.getAccessModifier(modifierNodes);
        this.otherModifiers = Utils.getNonAccessModifiers(modifierNodes);
        this.name = node.find("Identifier").get(0).getValue();
        this.typeParameters = node.find("TypeParameters", TypeParameters.CONSTRUCT).get(0);
        this.extended = node.find("ExtendsInterfaces.ClassOrInterfaceType", Type.CONSTRUCT);
        boolean isSql = false;
        boolean isQuery = false;
        TypeParameter queryParameter = null;
        // exactly one body guaranteed by syntax
        body = Utils.convertChildren(node, "InterfaceBody", Body.class).get(0);
        comment = node.getComments();
        // everything is constructed; apply archetype (by syntax the loopis
        // executed 0 or 1 times
        for (SyntaxTree archetypeNode: node.find("Archetype")) {
            Archetype.create(archetypeNode).apply(this);
        }
    }

    public String getName() {
        return name;
    }

    public TypeParameters getTypeParameters() {
        return typeParameters;
    }

    public List<Type> getExtended() {
        return Collections.unmodifiableList(extended);
    }

    public Body getBody() {
        return body;
    }

    public String getComment() {
        return comment;
    }

    /**
     * Returns all methods - declared and inherited
     * @param model we need model to have access to declarations of inherited methods
     * @return
     */
    public Collection<MethodDefinition> getAllMethods(final Model model) throws ModelException {
        // Map of methods by signature
        Map<String, MethodDefinition> allMethods = collectAllMethods(model);
        return allMethods.values();
    }

    private Map<String, MethodDefinition> collectAllMethods(final Model model) throws ModelException {
        Map<String, MethodDefinition> allMethods = new HashMap<String, MethodDefinition>(body.getMethods());
        for (Type type: extended) {
            InterfaceDefinition parent = model.getInterface(type);
            for (MethodDefinition parentMethod: parent.getAllMethods(model)) {
                MethodDeclaration candidate =
                        parentMethod.getDeclaration()
                                .override(parent.getTypeParameters(), type.getTypeArguments());
                MethodDefinition myMethod = allMethods.get(candidate.signature());
                if (myMethod == null) {
                    MethodDefinition overrideDefinition = new MethodDefinition(parentMethod.getComment(),
                            "", Collections.<String>emptySet(), candidate, ";");
                    allMethods.put(candidate.signature(), overrideDefinition);
                } else {
                    // check that methods are the same
                    if (!myMethod.getDeclaration().equals(candidate)) {
                        throw new ModelException("Name clash in " + getName()+":" +
                                myMethod.getDeclaration() + " != " + candidate);
                    }
                }
            }
        }
        return allMethods;
    }

    public static F<SyntaxTree, InterfaceDefinition, GrammarException> CONSTRUCT =
            new F<SyntaxTree, InterfaceDefinition, GrammarException>() {
                @Override
                public InterfaceDefinition apply(SyntaxTree syntaxTree) throws GrammarException {
                    return new InterfaceDefinition(syntaxTree);
                }
            };

}


