package org.simqle.model;

import org.simqle.parser.ParseException;
import org.simqle.parser.SimpleNode;
import org.simqle.parser.SyntaxTree;
import org.simqle.processor.GrammarException;
import org.simqle.util.Assert;
import org.simqle.util.Utils;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: aizyurov
 * Date: 25.11.2012
 * Time: 8:32:28
 * To change this template use File | Settings | File Templates.
 */
public class MethodDefinition {
    private final String comment;
    private final String accessModifier;
    private final Set<String> otherModifiers;
    private final TypeParameters typeParameters;
    // null for void methods
    private final Type resultType;

    private final String name;
    private final List<FormalParameter> formalParameters;

    private final Set<Type> thrownExceptions;
    private final String body;

    private final AbstractTypeDefinition owner;

    private final boolean isPublic;

    private final boolean isAbstract;

    public boolean isPublic() {
        return isPublic;
    }

    protected boolean isAbstract() {
        return isAbstract;
    }

    public static MethodDefinition parseAbstract(final String source, final AbstractTypeDefinition owner) {
        try {
            final SimpleNode simpleNode = Utils.createParser(source).AbstractMethodDeclaration();
            SyntaxTree syntaxTree = new SyntaxTree(simpleNode, source);
            return new MethodDefinition(syntaxTree, owner);
        } catch (ParseException e) {
            throw new RuntimeException("Internal error", e);
        } catch (GrammarException e) {
            throw new RuntimeException("Internal error", e);
        }
    }


    private MethodDefinition(final String comment,
                             final String accessModifier,
                             final Set<String> otherModifiers,
                             final TypeParameters typeParameters,
                             final Type resultType,
                             final String name,
                             final List<FormalParameter> formalParameters,
                             final Set<Type> thrownExceptions,
                             final String body,
                             final AbstractTypeDefinition owner,
                             final boolean aPublic,
                             final boolean anAbstract) {
        this.comment = comment;
        this.accessModifier = accessModifier;
        this.otherModifiers = new TreeSet<String>(otherModifiers);
        this.typeParameters = typeParameters;
        this.resultType = resultType;
        this.name = name;
        this.formalParameters = new ArrayList<FormalParameter>(formalParameters);
        this.thrownExceptions = new HashSet<Type>(thrownExceptions);
        this.body = body;
        this.owner = owner;
        isPublic = aPublic;
        isAbstract = anAbstract;
    }

    public MethodDefinition(SyntaxTree node, final AbstractTypeDefinition owner) throws GrammarException {
        final String nodeType = node.getType();
        Assert.assertOneOf(new GrammarException("Unexpected type: " + nodeType, node), nodeType, "MethodDeclaration", "AbstractMethodDeclaration");
        this.comment = node.getComments();
        List<SyntaxTree> modifierNodes = node.find("MethodModifiers.MethodModifier");
        modifierNodes.addAll(node.find("AbstractMethodModifiers.AbstractMethodModifier"));
        this.accessModifier = Utils.getAccessModifier(modifierNodes);
        this.otherModifiers = Utils.getNonAccessModifiers(modifierNodes);
        final List<SyntaxTree> bodies = node.find("MethodBody");
        this.body = bodies.isEmpty() ? ";" : bodies.get(0).getImage();
        typeParameters = new TypeParameters(node.find("TypeParameters.TypeParameter", TypeParameter.CONSTRUCT));
        final List<SyntaxTree> resultTypes = node.find("ResultType.Type");
        if (resultTypes.isEmpty()) {
            // void
            resultType = Type.VOID;
        } else {
            resultType = new Type(resultTypes.get(0));
        }

        final List<SyntaxTree> nodes = node.find("MethodDeclarator.Identifier");
        name = nodes.get(0).getValue();
        formalParameters = node.find("MethodDeclarator.FormalParameterList.FormalParameter", FormalParameter.CONSTRUCT);
        formalParameters.addAll
                (node.find("MethodDeclarator.FormalParameterList.FormalParameterWithEllipsis", FormalParameter.CONSTRUCT));

        final List<String> throwClauses = node.find("Throws", SyntaxTree.BODY);
        // may be at most one by grammar
        this.thrownExceptions = new HashSet<Type>(node.find("Throws.ExceptionType", Type.CONSTRUCT));
        this.owner = owner;
        this.isAbstract = owner.makeMethodAbstract(otherModifiers);
        this.isPublic = owner.makeMethodPublic(accessModifier);

    }

    public String getName() {
        return name;
    }

    public String signature() {
        // TODO
        HashSet<String> typeParameterNames = new HashSet<String>(typeParameters.names());
        typeParameterNames.addAll(owner.getTypeParameters().names());
        Collection<String> formalParameterErasures =
                Utils.map(formalParameters, FormalParameter.f_erasure(typeParameterNames));
        return name+"("+Utils.format(formalParameterErasures, "", ",", "")+")";
    }

    public String getAccessModifier() {
        return accessModifier;
    }

    public Set<String> getOtherModifiers() {
        return otherModifiers;
    }

    public String declaration() {
        StringBuilder builder = new StringBuilder();
        builder.append(accessModifier);
        if (!"".equals(accessModifier)) {
            builder.append(" ");
        }
        builder.append(Utils.format(new ArrayList<String>(otherModifiers), " ", " ", ""));
        if (!typeParameters.isEmpty()) {
            builder.append(" ");
        }
        builder.append(resultType).append(" ");
        builder.append(name)
                .append("(")
                .append(Utils.format(formalParameters, "", ", ", ""))
                .append(")")
                .append(Utils.format(thrownExceptions, " throws ", ", ", ""));
        return builder.toString();
    }

    public String toString() {
        return comment + declaration() + body;
    }

    public MethodDefinition override(final AbstractTypeDefinition targetOwner, final Model model) throws ModelException {
        final Type type = targetOwner.getAncestorTypeByName(owner.getName());
        final AbstractTypeDefinition abstractType = model.getAbstractType(type.getSimpleName());
        TypeParameters typeParameters1 = abstractType.getTypeParameters();
        TypeArguments typeArguments = type.getTypeArguments();
        return replaceParameters(targetOwner, typeParameters1, typeArguments);
    }

    private MethodDefinition replaceParameters(final AbstractTypeDefinition targetOwner, final TypeParameters typeParameters, final TypeArguments typeArguments) throws ModelException {
        final List<FormalParameter> newFormalParameters = new ArrayList<FormalParameter>(formalParameters.size());
        for (FormalParameter formalParameter: formalParameters) {
            newFormalParameters.add(formalParameter.substituteParameters(typeParameters, typeArguments));
        }
        final Type newResultType = resultType.substituteParameters(typeParameters, typeArguments);
        final Set<Type> newThrownExceptions = new HashSet<Type>();
        for (Type exceptionType: thrownExceptions) {
            newThrownExceptions.add(exceptionType.substituteParameters(typeParameters, typeArguments));
        }
        final Set<String> newModifiers = new HashSet<String>(otherModifiers);
        newModifiers.add("transient");
        newModifiers.addAll(targetOwner.addImplicitMethodModifiers(this));
        String newAccessModifier = targetOwner.implicitMethodAccessModifier(this);
        return new MethodDefinition(
                comment,
                newAccessModifier,
                newModifiers,
                typeParameters,
                newResultType,
                name,
                newFormalParameters,
                newThrownExceptions,
                ";",
                targetOwner,
                targetOwner.makeMethodPublic(newAccessModifier),
                targetOwner.makeMethodAbstract(newModifiers));
    }

    /**
     * A method matches another method, if they have the same
     * return type, name, formal parameters, owner and thrown exceptions.
     * Type parameters may be different, this is taken into account:
     * e.g if the return type is the first type parameter for both, is it OK etc.
     * @param other the method to compare with
     * @return
     */
    public boolean matches(MethodDefinition other) {
        throw new RuntimeException("Not implemented");
    }

    public void implement(final String newAccessModifier, final String newBody) throws ModelException {
        final Set<String> newModifiers = new HashSet<String>(otherModifiers);
        newModifiers.remove("abstract");
        newModifiers.remove("transient");
        owner.addMethod(
        new MethodDefinition(
                comment,
                newAccessModifier,
                newModifiers,
                typeParameters,
                resultType,
                name,
                formalParameters,
                thrownExceptions,
                newBody,
                owner,
                owner.makeMethodPublic(newAccessModifier),
                false)
        );
    }

    public void declareAbstract(final String newAccessModifier) throws ModelException {
        final Set<String> newModifiers = new HashSet<String>(otherModifiers);
        newModifiers.add("abstract");
        newModifiers.remove("transient");
        owner.addMethod(
            new MethodDefinition(
                    comment,
                    newAccessModifier,
                    newModifiers,
                    typeParameters,
                    resultType,
                    name,
                    formalParameters,
                    thrownExceptions,
                    ";",
                    owner,
                    owner.makeMethodPublic(newAccessModifier),
                    true)
        );
    }

}
