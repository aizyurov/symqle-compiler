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
    private final String sourceRef;

    private static int anonymousClassCounter = 0;

    // presentation part
    private String comment;

    protected AbstractTypeDefinition(SyntaxTree node) throws GrammarException {
        Assert.assertOneOf(new GrammarException("Unexpected type: "+node.getType(), node), node.getType(), "SymqleInterfaceDeclaration", "NormalClassDeclaration", "ImplementationHint");

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
                return "$$"+syntaxTree.getValue();
            }
        }));
        this.name = names.isEmpty() ? ("anonymous$"+ anonymousClassCounter++ ) : names.get(0);

        final List<TypeParameter> typeParams = node.find("TypeParameters.TypeParameter", TypeParameter.CONSTRUCT);
        // one level up for ProductionRule
        typeParams.addAll(node.find("^.TypeParameters.TypeParameter", TypeParameter.CONSTRUCT));
        this.typeParameters = new TypeParameters(typeParams);
        // exactly one body guaranteed by syntax - either InterfaceBody or ClassBody
        // except for ImplementationHiht, which can have no body
        final List<SyntaxTree> bodies = node.find("InterfaceBody");
        bodies.addAll(node.find("ClassBody"));
        if (node.getType().equals("ImplementationHint") && bodies.isEmpty()) {
            try {
                bodies.add(new SyntaxTree(Utils.createParser("{}").ClassBody(), node.getFileName()));
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
        final SyntaxTree bodyNode = bodies.get(0);
        final List<SyntaxTree> members = bodyNode.find("InterfaceMemberDeclaration");
        members.addAll(bodyNode.find("ClassBodyDeclaration"));
        for (SyntaxTree member: members) {
            final SyntaxTree child = member.getChildren().get(0);
            String type = child.getType();
            if (type.equals("AbstractMethodDeclaration") ||
                    type.equals("MethodDeclaration")) {
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

    public void addFieldDeclaration(FieldDeclaration declaration) {
        // no check for duplicate valiable names!
        otherDeclarations.add(declaration.toString());
    }

    public abstract String implicitMethodAccessModifier(MethodDefinition methodDefinition);
    public abstract Set<String> addImplicitMethodModifiers(MethodDefinition methodDefinition);

    public abstract boolean methodIsAbstract(Set<String> modifiers);
    public abstract boolean methodIsPublic(String explicitAccessModifier);

    public Type getType() {
        return new Type(name, typeParameters.asTypeArguments(), 0);
    }



    public void addMethod(MethodDefinition methodDefinition) throws ModelException {
        if (null != methods.put(methodDefinition.signature(), methodDefinition)) {
            throw new ModelException("Duplicate method: "+methodDefinition.signature() + " in " + getName());
        }
    }

    public void addImportLines(Collection<String> addedImports) {
        this.importLines.addAll(addedImports);
    }

    public String getName() {
        return name;
    }

    protected final void makeAbstract() {
        otherModifiers.add("abstract");
    }

    public TypeParameters getTypeParameters() {
        return typeParameters;
    }

    /**
     * Actually returns only non-static methods
     * @return
     */
    public Collection<MethodDefinition> getNonStaticMethods() {
        final Collection<MethodDefinition> methods = this.methods.values();
        final List<MethodDefinition> nonStaticMethods = new ArrayList<MethodDefinition>();
        for (MethodDefinition method : methods) {
            if (!method.getOtherModifiers().contains("static")) {
                nonStaticMethods.add(method);
            }
        }
        return nonStaticMethods;
    }

    public Collection<MethodDefinition> getDeclaredMethods() {
        return Collections.unmodifiableCollection(methods.values());
    }

    public Collection<MethodDefinition> getStaticMethods() {
        final Collection<MethodDefinition> methods = this.methods.values();
        final List<MethodDefinition> staticMethods = new ArrayList<MethodDefinition>();
        for (MethodDefinition method : methods) {
            if (method.getOtherModifiers().contains("static")) {
                staticMethods.add(method);
            }
        }
        return staticMethods;
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

    public String getSourceRef() {
        return sourceRef;
    }

    public String toString() {
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

    protected final String declarationString() {
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

    protected abstract String getTypeKeyword();

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

    public Set<String> getImportLines() {
        return Collections.unmodifiableSet(importLines);
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

    protected abstract Set<AbstractTypeDefinition> getAllAncestors(Model model) throws ModelException;

    public void replaceComment(final String newComment) {
        this.comment = newComment;
    }
}


