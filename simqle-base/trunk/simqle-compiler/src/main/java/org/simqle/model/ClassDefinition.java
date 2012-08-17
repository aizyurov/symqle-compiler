/*
* Copyright Alexander Izyurov 2010
*/
package org.simqle.model;

import org.simqle.parser.ParseException;
import org.simqle.parser.SimpleNode;
import org.simqle.parser.SyntaxTree;
import org.simqle.processor.GrammarException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <br/>19.11.2011
 *
 * @author Alexander Izyurov
 */
public class ClassDefinition {

    private final String accessModifier;
    private final List<String> otherModifiers;
    private final List<Annotation> annotations;

    private final List<String> imports = new ArrayList<String>();

    // the class pairName
    private final String pairName;

    private final List<TypeParameter> typeParameters;

    // null if does not extend nothing but Object
    private final Type extendedClass;

    private final List<Type> implementedInterfaces;

    private final List<Type> mimics;

    private final Body body;

    public static ClassDefinition parse(String source) {
        try {
            final SimpleNode simpleNode = Utils.createParser(source).SimqleClassDeclaration();
            SyntaxTree syntaxTree = new SyntaxTree(simpleNode, source);
            return new ClassDefinition(syntaxTree, Collections.<SyntaxTree>emptyList());
        } catch (ParseException e) {
            throw new RuntimeException("Internal error", e);
        } catch (GrammarException e) {
            throw new RuntimeException("Internal error", e);
        }
    }

    public ClassDefinition(SyntaxTree node, List<SyntaxTree> importDeclarations) throws GrammarException {
        if (!node.getType().equals("SimqleClassDeclaration")) {
            throw new IllegalArgumentException("Illegal argument: "+node);
        }
        this.imports.addAll(convertAsBodies(importDeclarations));
        final List<SyntaxTree> internalImports = node.find("ImportDeclaration");
        this.imports.addAll(convertAsBodies(internalImports));
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
        this.mimics = Utils.convertChildren(node, "Mimics.ClassOrInterfaceType", Type.class);
        this.body = new Body(node.find("ClassBody").get(0));

    }

    private static List<String> convertAsBodies(List<SyntaxTree> nodes) {
        return Utils.convertToStringList(nodes, new Function<String, SyntaxTree>() {
            public String apply(SyntaxTree syntaxTree) {
                return syntaxTree.getBody();
            }
        });
    }

    public String getAccessModifier() {
        return accessModifier;
    }

    public List<String> getOtherModifiers() {
        return otherModifiers;
    }

    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public List<String> getImports() {
        return imports;
    }

    public String getPairName() {
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

    public List<Type> getMimics() {
        return mimics;
    }

    public Body getBody() {
        return body;
    }

    /**
     * Does not throw ModelException; throws RuntimeException instead
     * Use from compiler's internal code when you are absolutely sure that
     * there cannot be any conflict
     * @param t
     */
    public void addMimicsInternal(Type t) {
        try {
            addMimics(t);
        } catch (ModelException e) {
            throw new RuntimeException("Internal error", e);
        }
    }

    public void addMimics(Type t) throws ModelException {
        // we can add type if it is the same or if its pairName is different;
        // cannot mimic the same class with different type parameters
        if (mimics.contains(t)) {
            return;
        }
        for (Type m: mimics) {
            if (m.getNameChain().equals(t.getNameChain())) {
                throw new ModelException("Cannot mimic one class with different type parameters");
            }
        }
        mimics.add(t);
    }

    public String getClassName() {
        return getPairName();
    }



    public void addConstructorDeclaration(ConstructorDeclaration constructor) throws ModelException {
        if (constructor.getName().equals(getClassName()) || constructor.getName().equals(getPairName())) {
            body.unsafeAddConstructorDeclaration(constructor);
        } else {
            throw new ModelException("Constructor name \""+constructor.getName()+"\" does not match class name");
        }
    }

}
