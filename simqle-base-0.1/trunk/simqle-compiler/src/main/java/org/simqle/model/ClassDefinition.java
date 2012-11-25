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

import java.util.*;

/**
 * <br/>19.11.2011
 *
 * @author Alexander Izyurov
 */
public class ClassDefinition {

    private final String comment;
    private final String accessModifier;
    private final Set<String> otherModifiers;
    private final List<Annotation> annotations;
    private final Set<String> imports;


    // the class name
    private final String name;

    private final TypeParameters typeParameters;

    // null if does not extend nothing but Object
    private final Type extendedClass;

    private final List<Type> implementedInterfaces;


    private final Body body;

    public static ClassDefinition parse(String source) {
        try {
            final SimpleNode simpleNode = Utils.createParser(source).NormalClassDeclaration();
            SyntaxTree syntaxTree = new SyntaxTree(simpleNode, source);
            return new ClassDefinition(syntaxTree);
        } catch (ParseException e) {
            throw new RuntimeException("Internal error", e);
        } catch (GrammarException e) {
            throw new RuntimeException("Internal error", e);
        }
    }

    public ClassDefinition(SyntaxTree node) throws GrammarException {
        Assert.assertOneOf(new GrammarException("Unexpected type: "+node.getType(), node), node.getType(), "NormalClassDeclaration");
        final List<SyntaxTree> modifiers = node.find("ClassModifiers.ClassModifier");
        this.accessModifier = Utils.getAccessModifier(modifiers);
        this.otherModifiers = Utils.getNonAccessModifiers(modifiers);
        this.annotations = Utils.convertChildren(node, "ClassModifiers.Annotation", Annotation.class);
        this.name = node.find("Identifier").get(0).getValue();
        this. typeParameters = node.find("TypeParameters", TypeParameters.CONSTRUCT).get(0);
        final List<SyntaxTree> extendedTypes = node.find("Super.ClassOrInterfaceType");
        if (extendedTypes.isEmpty()) {
            this.extendedClass = null;
        } else {
            this.extendedClass = new Type(extendedTypes.get(0));
        }
        this.implementedInterfaces = Utils.convertChildren(node, "Interfaces.ClassOrInterfaceType", Type.class);
        this.body = new Body(node.find("ClassBody").get(0));
        imports = new HashSet<String>(Utils.bodies(node.getParent().getParent().find("ImportDeclaration")));
        comment = node.getComments();

    }

    public String getAccessModifier() {
        return accessModifier;
    }

    public List<String> getOtherModifiers() {
        return new ArrayList<String>(otherModifiers);
    }

    public List<Annotation> getAnnotations() {
        return new ArrayList<Annotation>(annotations);
    }

    public final String getName() {
        return name;
    }

    public TypeParameters getTypeParameters() {
        return typeParameters;
    }

    public Type getExtendedClass() {
        return extendedClass;
    }

    public List<Type> getImplementedInterfaces() {
        return new ArrayList<Type>(implementedInterfaces);
    }

    public Body getBody() {
        return body;
    }

    public void addImplementedInterface(final Type interfaceType) {
        // TODO check for duplicates
        implementedInterfaces.add(interfaceType);
    }

    public String getImplementsStatement() {
        return Utils.formatList(implementedInterfaces, "implements ", ", ", "", new Function<String, Type>() {
            @Override
            public String apply(final Type type) {
                return type.toString();
            }
        });
    }

    public String getExtendsStatement() {
        return extendedClass == null ? "" :
                "extends "+extendedClass.toString();
    }

    public String getTypeParametersString() {
        return typeParameters.toString();
    }

    public Set<String> getImports() {
        return new TreeSet<String>(imports);
    }

    public String getComment() {
        return comment;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(Utils.format(new ArrayList<String>(imports), "", Utils.LINE_BREAK, ""));
        builder.append(comment);
        List<String> modifiers = new ArrayList<String>();
        modifiers.add(accessModifier);
        modifiers.addAll(otherModifiers);
        builder.append(Utils.format(modifiers, "", " ", ""));
        builder.append(name);
        builder.append(typeParameters);
        if (extendedClass!=null) {
            builder.append("extends ").append(extendedClass);
        }
        if (!implementedInterfaces.isEmpty()) {
            builder.append("implements ").append(Utils.format(implementedInterfaces, "", ", ", ""));
        }
        builder.append(body);
        return builder.toString();
    }

    public Collection<MethodDefinition> getImplementedNonStaticMethods(Model model) throws ModelException {
        final Map<String, MethodDefinition> methodMap = new HashMap<String, MethodDefinition>();
        for (MethodDefinition method: body.getMethods().values()) {
            if (!method.getOtherModifiers().contains("abstract") && !method.getOtherModifiers().contains("static")) {
                methodMap.put(method.getSignature(), method);
            }
        }
        if (extendedClass!=null) {
            ClassDefinition parent = model.getClassDef(extendedClass);
            for (MethodDefinition method: parent.getImplementedNonStaticMethods(model)) {
                if (!method.getOtherModifiers().contains("abstract")
                        && !method.getOtherModifiers().contains("static")
                        && !"private".equals(method.getAccessModifier())) {
                    MethodDeclaration declaration = method.getDeclaration()
                            .override(parent.getTypeParameters(), extendedClass.getTypeArguments());
                    String signature = declaration.signature();
                    MethodDefinition myMethod = methodMap.get(signature);
                    if (myMethod == null) {
                        // add fake method if possible: we do not care about body
                        methodMap.put(signature, new MethodDefinition("", method.getAccessModifier(), method.getOtherModifiers(),
                                declaration, " { throw new RuntimeException(\"Implemented elsewhere; should never get here\"); }"));
                    } else {
                        // make sure it is Ok to override
                        if (!myMethod.getDeclaration().equals(declaration)) {
                            throw new ModelException("Name clash in " + name + ": local "+myMethod.getDeclaration() + " and "+declaration);
                        } else {
                            // do not add: it isoverridden.
                            // leaving decrease of access check to Java compiler
                        }
                    }
                }
            }
        }
        return methodMap.values();
    }

    /**
     * All methods declared in directly or indirectly implemented interfaces
     * but not implemented in this class or any ancestor (may be declared abstract
     * in this class or ancestor). The returned methods have "abstract" modifier
     * @return
     */
    public Collection<MethodDefinition> getAllUnimplementedNonStaticMethods(final Model model) throws ModelException {
        final Map<String, MethodDefinition> methodMap = new HashMap<String, MethodDefinition>();
        for (MethodDefinition method: body.getMethods().values()) {
            if (method.getOtherModifiers().contains("abstract") && !method.getOtherModifiers().contains("static")) {
                methodMap.put(method.getSignature(), method);
            }
        }
        if (extendedClass!=null) {
            ClassDefinition parent = model.getClassDef(extendedClass);
            for (MethodDefinition method: parent.getImplementedNonStaticMethods(model)) {
                if (method.getOtherModifiers().contains("abstract")
                        && !method.getOtherModifiers().contains("static")
                        && !"private".equals(method.getAccessModifier())) {
                    MethodDeclaration declaration = method.getDeclaration()
                            .override(parent.getTypeParameters(), extendedClass.getTypeArguments());
                    String signature = declaration.signature();
                    MethodDefinition myMethod = methodMap.get(signature);
                    if (myMethod == null) {
                        // add fake method if possible: we do not care about body
                        methodMap.put(signature, new MethodDefinition("", method.getAccessModifier(), method.getOtherModifiers(),
                                declaration, " { throw new RuntimeException(\"Implemented elsewhere; should never get here\"); }"));
                    } else {
                        // make sure it is Ok to override
                        if (!myMethod.getDeclaration().equals(declaration)) {
                            throw new ModelException("Name clash in " + name + ": local "+myMethod.getDeclaration() + " and "+declaration);
                        } else {
                            // do not add: it isoverridden.
                            // leaving decrease of access check to Java compiler
                        }
                    }
                }
            }
        }
        // TODO interfaces
        throw new RuntimeException("Not implemented");
    }

}
