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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * <br/>19.11.2011
 *
 * @author Alexander Izyurov
 */
public class ClassDefinition {

    private final String accessModifier;
    private final Set<String> otherModifiers;
    private final List<Annotation> annotations;


    // the class pairName
    private final String pairName;

    private final List<TypeParameter> typeParameters;

    // null if does not extend nothing but Object
    private final Type extendedClass;

    private final List<Type> implementedInterfaces;


    private final Body body;

    public static ClassDefinition parse(String source) {
        try {
            final SimpleNode simpleNode = Utils.createParser(source).SimqleClassDeclaration();
            SyntaxTree syntaxTree = new SyntaxTree(simpleNode, source);
            return new ClassDefinition(syntaxTree);
        } catch (ParseException e) {
            throw new RuntimeException("Internal error", e);
        } catch (GrammarException e) {
            throw new RuntimeException("Internal error", e);
        }
    }

    public ClassDefinition(SyntaxTree node) throws GrammarException {
        Assert.assertOneOf(new GrammarException("Unexpected type: "+node.getType(), node), node.getType(), "SimqleClassDeclaration");
        final List<SyntaxTree> modifiers = node.find("ClassModifiers.ClassModifier");
        this.accessModifier = Utils.getAccessModifier(modifiers);
        this.otherModifiers = Utils.getNonAccessModifiers(modifiers);
        this.annotations = Utils.convertChildren(node, "ClassModifiers.Annotation", Annotation.class);
        this.pairName = node.find("Identifier").get(0).getValue();
        this. typeParameters = Utils.convertChildren(node, "TypeParameters.TypeParameter", TypeParameter.class);
        final List<SyntaxTree> extendedTypes = node.find("Super.ClassOrInterfaceType");
        if (extendedTypes.isEmpty()) {
            this.extendedClass = null;
        } else {
            this.extendedClass = new Type(extendedTypes.get(0));
        }
        this.implementedInterfaces = Utils.convertChildren(node, "SimqleInterfaces.ImplementedInterface.ClassOrInterfaceType", Type.class);
//        this.mimics = Utils.convertChildren(node, "Mimics.ClassOrInterfaceType", Type.class);
        this.body = new Body(node.find("ClassBody").get(0));

    }

    public String getAccessModifier() {
        return accessModifier;
    }

    public List<String> getOtherModifiers() {
        return new ArrayList<String>(otherModifiers);
    }

    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public final String getPairName() {
        return pairName;
    }

    public List<TypeParameter> getTypeParameters() {
        return typeParameters;
    }

    public Type getExtendedClass() {
        return extendedClass;
    }

    public List<Type> getImplementedInterfaces() {
        return implementedInterfaces;
    }

    public Body getBody() {
        return body;
    }

    public String getClassName() {
        return getPairName();
    }

}
