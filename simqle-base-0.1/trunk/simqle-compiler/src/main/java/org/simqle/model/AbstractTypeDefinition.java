/*
* Copyright Alexander Izyurov 2010
*/
package org.simqle.model;

import org.simqle.parser.SyntaxTree;
import org.simqle.processor.GrammarException;
import org.simqle.util.Assert;
import org.simqle.util.Utils;

import java.util.*;

/**
 * <br/>13.11.2011
 *
 * @author Alexander Izyurov
 */
public abstract class AbstractTypeDefinition {
    private final Set<String> importLines;
    private final String name;
    private final String accessModifier;
    private final Set<String> otherModifiers;
    private final TypeParameters typeParameters;
    private final Map<String, MethodDefinition> methods = new TreeMap<String, MethodDefinition>();
    private final List<String> otherDeclarations = new ArrayList<String>();
    private final List<String> annotations;

    // presentation part
    private final String comment;

    protected AbstractTypeDefinition(SyntaxTree node) throws GrammarException {
        Assert.assertOneOf(new GrammarException("Unexpected type: "+node.getType(), node), node.getType(), "SimqleInterfaceDeclaration", "NormalClassDeclaration");

        this.importLines = new TreeSet<String>(node.getParent().getParent().find("ImportDeclaration", SyntaxTree.BODY));
        // modifiers ma be of interface or class; one of collections is empty
        final List<SyntaxTree> modifierNodes = node.find("InterfaceModifiers.InterfaceModifier");
        modifierNodes.addAll(node.find("ClassModifiers.ClassModifier"));
        this.annotations = node.find("ClassModifiers.Annotation", SyntaxTree.BODY);
        this.annotations.addAll(node.find("InterfaceModifiers.Annotation", SyntaxTree.BODY));
        this.accessModifier = Utils.getAccessModifier(modifierNodes);
        this.otherModifiers = Utils.getNonAccessModifiers(modifierNodes);
        this.name = node.find("Identifier").get(0).getValue();
        this.typeParameters = new TypeParameters(node.find("TypeParameters.TypeParameter", TypeParameter.CONSTRUCT));
        // exactly one body guaranteed by syntax - either InterfaceBody or ClassBody
        final List<SyntaxTree> bodies = node.find("InterfaceBody");
        bodies.addAll(node.find("ClassBody"));
        final SyntaxTree bodyNode = bodies.get(0);
        final List<SyntaxTree> members = bodyNode.find("InterfaceMemberDeclaration");
        members.addAll(bodyNode.find("ClassBodyDeclaration"));
        for (SyntaxTree member: members) {
            final SyntaxTree child = member.getChildren().get(0);
            String type = child.getType();
            if (type.equals("AbstractMethodDeclaration") ||
                    type.equals("MethodDeclaration")) {
                MethodDefinition methodDefinition = new MethodDefinition(child, this);
                if (methodDefinition.getOtherModifiers().contains("static")) {
                    // static methods are not processed by Simqle
                    otherDeclarations.add(child.getImage());
                } else {
                    try {
                        addMethod(methodDefinition);
                    } catch (ModelException e) {
                        throw new GrammarException(e, child);
                    }
                }
            } else {
                // just copy to other otherDeclarations
                otherDeclarations.add(child.getImage());
            }
        }
        comment = node.getComments();
    }

    public abstract String implicitMethodAccessModifier(MethodDefinition methodDefinition);
    public abstract Set<String> addImplicitMethodModifiers(MethodDefinition methodDefinition);

    public abstract boolean methodIsAbstract(Set<String> modifiers);
    public abstract boolean methodIsPublic(String explicitAccessModifier);



    public void addMethod(MethodDefinition methodDefinition) throws ModelException {
        if (null != methods.put(methodDefinition.signature(), methodDefinition)) {
            throw new ModelException("Duplicate method: "+methodDefinition.signature());
        }
    }

    public String getName() {
        return name;
    }

    public TypeParameters getTypeParameters() {
        return typeParameters;
    }

    public Collection<MethodDefinition> getDeclaredMethods() {
        return methods.values();
    }

    public final Collection<MethodDefinition> getAllMethods(Model model) throws ModelException {
        return getAllMethodsMap(model).values();
    }

    public final MethodDefinition getMethodBySignature(String signature, Model model) throws ModelException {
        return getAllMethodsMap(model).get(signature);
    }

    protected abstract Map<String, MethodDefinition> getAllMethodsMap(Model model) throws ModelException;

    protected abstract String getExtendsImplements();

    protected abstract Type getAncestorTypeByName(String name);

    public final MethodDefinition getDeclaredMethodBySignature(String signature) {
        return methods.get(signature);
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(Utils.format(new ArrayList<String>(importLines), "", Utils.LINE_BREAK, ""));
        builder.append(comment);
        List<String> modifiers = new ArrayList<String>();
        modifiers.add(accessModifier);
        modifiers.addAll(otherModifiers);
        builder.append(Utils.format(modifiers, "", " ", " "));
        builder.append(Utils.format(annotations, "", " ", " "));
        builder.append(name);
        builder.append(typeParameters);
        builder.append(" ");
        builder.append(getExtendsImplements());
        builder.append(" {").append(Utils.LINE_BREAK);
        // we are not expecting inner classes (which should go after methods by convention
        // so we are putting everything but methods before methods
        for (String otherDeclaration: otherDeclarations) {
            builder.append(otherDeclaration).append(Utils.LINE_BREAK);
        }
        builder.append("}").append(Utils.LINE_BREAK);
        for (MethodDefinition method: methods.values()) {
            builder.append(method);
            builder.append(Utils.LINE_BREAK);
        }
        builder.append("}");
        builder.append(Utils.LINE_BREAK);
        return builder.toString();
    }

    protected void addInheritedMethodsToMap(final Model model, final Map<String, MethodDefinition> methodMap, final Type parentType) throws ModelException {
        AbstractTypeDefinition parent = model.getAbstractType(parentType.getSimpleName());
        for (MethodDefinition method: parent.getAllMethods(model)) {
            if (!"private".equals(method.getAccessModifier())) {
                final MethodDefinition candidate = method.override(this, model);
                String signature = candidate.signature();
                MethodDefinition myMethod = methodMap.get(signature);
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
}


