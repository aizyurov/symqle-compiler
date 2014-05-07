/*
* Copyright Alexander Izyurov 2010
*/
package org.symqle.model;

import org.symqle.parser.ParseException;
import org.symqle.parser.SyntaxTree;
import org.symqle.processor.GrammarException;
import org.symqle.util.Assert;
import org.symqle.util.Utils;

import java.io.File;
import java.util.*;

/**
 * Class or interface definition.
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
    private final String sourceRef;

    private static int anonymousClassCounter = 0;

    // presentation part
    private String comment;

    /**
     * Constructs from AST. The tree shoud be of type
     * SymqleInterfaceDeclaration, NormalClassDeclaration or ProductionImplementation.
     * @param node the syntax tree
     * @throws GrammarException wrong tree type or semantic error (duplicated methods etc.)
     */
    protected AbstractTypeDefinition(final SyntaxTree node) throws GrammarException {
        Assert.assertOneOf(new GrammarException("Unexpected type: " + node.getType(), node), node.getType(),
                "SymqleInterfaceDeclaration", "NormalClassDeclaration", "ProductionImplementation");

        this.importLines = new TreeSet<String>(node.find("^.^.ImportDeclaration", SyntaxTree.BODY));
        // modifiers may be of interface or class; one of collections is empty
        // for ProductionChoice both are empty
        final List<SyntaxTree> modifierNodes = node.find("InterfaceModifiers.InterfaceModifier");
        modifierNodes.addAll(node.find("ClassModifiers.ClassModifier"));
        this.annotations = node.find("ClassModifiers.Annotation", SyntaxTree.BODY);
        this.annotations.addAll(node.find("InterfaceModifiers.Annotation", SyntaxTree.BODY));
        this.accessModifier = Utils.getAccessModifier(modifierNodes);
        this.otherModifiers = Utils.getNonAccessModifiers(modifierNodes);
        final List<String> names = node.find("Identifier", SyntaxTree.VALUE);
        // for ProductionImplementation class name is generated from method name
        names.addAll(node.find("^.ProductionImplementation.Identifier", new F<SyntaxTree, String, RuntimeException>() {
            @Override
            public String apply(final SyntaxTree syntaxTree) {
                return "$$" + syntaxTree.getValue();
            }
        }));
        this.name = names.isEmpty()
                ? "anonymous$" + anonymousClassCounter++
                : names.get(0);

        final List<TypeParameter> typeParams = node.find("TypeParameters.TypeParameter", TypeParameter.CONSTRUCT);
        // one level up for ProductionRule
        typeParams.addAll(node.find("^.TypeParameters.TypeParameter", TypeParameter.CONSTRUCT));
        this.typeParameters = new TypeParameters(typeParams);
        // exactly one body guaranteed by syntax - either InterfaceBody or ClassBody
        // except for ImplementationHiht, which can have no body
        final List<SyntaxTree> bodies = node.find("InterfaceBody");
        bodies.addAll(node.find("ClassBody"));
        if (node.getType().equals("ProductionImplementation") && bodies.isEmpty()) {
            try {
                bodies.add(new SyntaxTree(Utils.createParser("{}").ClassBody(), node.getFileName()));
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
        final SyntaxTree bodyNode = bodies.get(0);
        final List<SyntaxTree> members = bodyNode.find("InterfaceMemberDeclaration");
        members.addAll(bodyNode.find("ClassBodyDeclaration"));
        for (SyntaxTree member : members) {
            final SyntaxTree child = member.getChildren().get(0);
            String type = child.getType();
            if (type.equals("AbstractMethodDeclaration")
                    || type.equals("MethodDeclaration")) {
                MethodDefinition methodDefinition = new MethodDefinition(child, this);
                try {
                    addMethod(methodDefinition);
                } catch (ModelException e) {
                    throw new GrammarException(e, child);
                }
            } else {
                // just copy to other otherDeclarations
                otherDeclarations.add(child.getImage());
            }
        }
        comment = node.getComments();
        sourceRef = new File(node.getFileName()).getName() + ":" + node.getLine();
    }

    /**
     * Adds field declaration to {@code this}.
     * No check for duplicate fields; generated sources will be non-compilable if there are any.
     * @param declaration field(s) to add
     */
    public final void addFieldDeclaration(final FieldDeclaration declaration) {
        // no check for duplicate valiable names!
        otherDeclarations.add(declaration.toString());
    }

    /**
     * Access modifier to be used in overridden/implemented or delegated method.
     * @param methodDefinition the source method (from other class/interface)
     * @return access modifier required for this class/interface
     */
    public abstract String implicitMethodAccessModifier(MethodDefinition methodDefinition);

    /**
     * Modifiers to be used in overridden/implemented or delegated method.
     * @param methodDefinition the source method (from other class/interface)
     * @return modifiers required for this class/interface
     */
    public abstract Set<String> implicitMethodModifiers(MethodDefinition methodDefinition);

    /**
     * Is a method with these modifiers abstract if appears in {@code this}.
     * @param modifiers the method modifiers
     * @return true if abstract (explicitly or interface method).
     */
    public abstract boolean methodIsAbstract(Set<String> modifiers);

    /**
     * Is a method with these modifiers public if appears in {@code this}.
     * @param explicitAccessModifier access modifier
     * @return true if public (explicitly or interface method).
     */
    public abstract boolean methodIsPublic(String explicitAccessModifier);

    /**
     * Type of {@code this}.
     * @return the type.
     */
    public final Type getType() {
        return new Type(name, typeParameters.asTypeArguments(), 0);
    }


    /**
     * Adds a method to {@code this}.
     * @param methodDefinition the method to add
     * @throws ModelException duplicate method
     */
    public final void addMethod(final MethodDefinition methodDefinition) throws ModelException {
        if (null != methods.put(methodDefinition.signature(), methodDefinition)) {
            throw new ModelException("Duplicate method: " + methodDefinition.signature() + " in " + getName());
        }
    }

    /**
     * Adds import lines to {@code this}.
     * Each line must be full inport statement, like "import org.symqle.common.SqlBuilder;".
     * Syntax is not checked.
     * @param addedImports import lines to add
     */
    public final void addImportLines(final Collection<String> addedImports) {
        this.importLines.addAll(addedImports);
    }

    /**
     * Name of this type.
     * @return type name
     */
    public final String getName() {
        return name;
    }

    /**
     * Makes this class abstract. Should be applied to ClassDefinition only.
     * Does not check for correct usage, may be erroneously applied to an interface,
     * producing uncompilable code.
     */
    protected final void makeAbstract() {
        otherModifiers.add("abstract");
    }

    /**
     * Type parameters of this type definition.
     * @return type parameters.
     */
    public final TypeParameters getTypeParameters() {
        return typeParameters;
    }

    /**
     * Declared methods of this type definition.
     * @return declared methods.
     */
    public final Collection<MethodDefinition> getDeclaredMethods() {
        return Collections.unmodifiableCollection(methods.values());
    }

    /**
     * All methods of this type definition, including inherited.
     * Methods, which are not declared, have "volatile" modifier.
     * @param model the model to consult for inherited methods.
     * @return all methods.
     * @throws ModelException wrong model (e.g. name clash of declared and inherited methods).
     */
    public final Collection<MethodDefinition> getAllMethods(final Model model) throws ModelException {
        return getAllMethodsMap(model).values();
    }

    /**
     * Find method (declared or inherited) by signature.
     * @param signature the signature to seek. see {@link #getDeclaredMethodBySignature(String)} for signature format.
     * @param model the model to consult for inherited methods.
     * @return the method; null if not found
     * @throws ModelException wrong model (e.g. name clash of declared and inherited methods).
     */
    public final MethodDefinition getMethodBySignature(final String signature, final Model model)
            throws ModelException {
        return getAllMethodsMap(model).get(signature);
    }

    /**
     * Subclasses should provide implementation, which returns (signature:method) map.
     * All declared and inherited methods are expected in the map.
     * @param model the model to consult for inherited methods.
     * @return map of all methods by signature
     * @throws ModelException wrong model (e.g. name clash of declared and inherited methods).
     */
    protected abstract Map<String, MethodDefinition> getAllMethodsMap(Model model) throws ModelException;

    /**
     * ExtendsImplements clause for {@code this}.
     * E.g. "extends Column implements Serializable"
     * @return the extends implements clause
     */
    protected abstract String getExtendsImplements();

    /**
     * Gets superclass/superinterface type by name.
     * @param ancestorName short name
     * @return the type
     * @throws IllegalArgumentException no such superclass/superinterface
     */
    protected abstract Type getAncestorTypeByName(String ancestorName);

    /**
     * Declared method by signature.
     * @param signature Format is different from JVM format! return type is not included.
     * primitive types are just their names, not special abbreviations. Arrays are marked with [] after type name.
     * Example: equal(int[],int[])
     * @return method; null if not found
     */
    public final MethodDefinition getDeclaredMethodBySignature(final String signature) {
        return methods.get(signature);
    }

    /**
     * Location of definition of {@code this} in the source (file, line).
     * @return location in filename:line format
     */
    public final String getSourceRef() {
        return sourceRef;
    }

    @Override
    public final String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append(Utils.format(importLines, "", Utils.LINE_BREAK, Utils.LINE_BREAK + Utils.LINE_BREAK))
                .append(comment)
                .append(declarationString())
                .append(" {")
                .append(Utils.LINE_BREAK)
                .append(bodyStringWithoutBraces())
                .append(Utils.LINE_BREAK)
                .append("}")
                .append(Utils.LINE_BREAK);
        return builder.toString();

    }

    private String declarationString() {
        StringBuilder builder = new StringBuilder();
        List<String> modifiers = new ArrayList<String>();
        modifiers.add(accessModifier);
        modifiers.addAll(otherModifiers);
        builder.append(Utils.format(modifiers, "", " ", " "));
        builder.append(Utils.format(annotations, "", " ", " "));
        builder.append(getTypeKeyword()).append(" ");
        builder.append(name);
        builder.append(typeParameters);
        builder.append(" ");
        builder.append(getExtendsImplements());
        return builder.toString();
    }

    /**
     * Is this type public.
     * @return true if public.
     */
    public final boolean isPublic() {
        return "public".equals(accessModifier);
    }

    /**
     * "class" or "interface".
     * @return one of these
     */
    protected abstract String getTypeKeyword();

    /**
     * The type body, without enclosing braces.
     * @return the body
     */
    protected final String bodyStringWithoutBraces() {
        final StringBuilder builder = new StringBuilder();
        // we are not expecting inner classes (which should go after methods by convention
        // so we are putting everything but methods before methods
        for (String otherDeclaration: otherDeclarations) {
            builder.append(otherDeclaration).append(Utils.LINE_BREAK);
       }
        for (MethodDefinition method: methods.values()) {
            builder.append(method);
            builder.append(Utils.LINE_BREAK);
        }
        return builder.toString();
    }

    /**
     * Import lines for this type.
     * @return import lines
     */
    public final Set<String> getImportLines() {
        return Collections.unmodifiableSet(importLines);
    }

    /**
     * Helper method for implementation of {@link #getAllMethodsMap(Model)} in derived classes.
     * Finds inherited methods from a given superclass/superinterface (including transitive inheritance)
     * and adds them to a map.
     * @param model the model
     * @param methodMap the map to add to
     * @param parentType parent to take inherited methods from
     * @throws ModelException something is wrong: no such parent, methods name clash.
     */
    protected final void addInheritedMethodsToMap(final Model model,
                                                  final Map<String, MethodDefinition> methodMap,
                                                  final Type parentType) throws ModelException {
        AbstractTypeDefinition parent = model.getAbstractType(parentType.getSimpleName());
        if (parent == null) {
            throw new ModelException("parentType not found : " + parentType.getSimpleName());
        }
        for (MethodDefinition method: parent.getAllMethods(model)) {
            if (!"private".equals(method.getAccessModifier())) {
                final MethodDefinition candidate = method.override(this, model);
                String signature = candidate.signature();
                MethodDefinition myMethod = methodMap.get(signature);
                if (myMethod == null) {
                    // add fake method if possible: we do not care about body
                    methodMap.put(signature, candidate);
                } else {
                    // is overridden explicitly; make sure it is Ok to override
                    if (!myMethod.matches(candidate)) {
                        throw new ModelException("Name clash in " + getName() + "#"
                                + myMethod.declaration() + " and " + candidate.declaration());
                    }
                }
            }
        }
    }

    /**
     * Find all ancestors of {@code this}.
     * @param model the model to scan
     * @return all ancestors
     * @throws ModelException wrong model (e.g. same interface inherited twice with different type parameters)
     */
    public abstract Set<Type> getAllAncestors(Model model) throws ModelException;

    /**
     * Replaces class comment with a new one.
     * @param newComment replacement
     */
    public final void replaceComment(final String newComment) {
        this.comment = newComment;
    }

    /**
     * Finds inherited transitively via parentType ancestors. parentType is not included to the result.
     * @param parentType the parent to inspect
     * @param model model to consult for inheritance relations
     * @return inherited from parentType ancestors
     * @throws ModelException wrong model (e.g. same interface inherited twice with different type parameters)
     */
    protected final Set<Type> getInheritedAncestors(final Type parentType, final Model model) throws ModelException {
        final AbstractTypeDefinition parent = model.getAbstractType(parentType.getSimpleName());
        final Set<Type> parentAncestors = parent.getAllAncestors(model);
        final Set<Type> myAncestors = new HashSet<Type>();
        for (Type parentAncestor: parentAncestors) {
            final Map<String, TypeArgument> replacementMap =
                    parent.getTypeParameters().inferTypeArguments(parent.getType(), parentType);
            myAncestors.add(parentAncestor.replaceParams(replacementMap));
        }
        return myAncestors;
    }

}


