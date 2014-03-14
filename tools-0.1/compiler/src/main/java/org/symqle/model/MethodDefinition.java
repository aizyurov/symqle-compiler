package org.symqle.model;

import org.symqle.parser.ParseException;
import org.symqle.parser.SimpleNode;
import org.symqle.parser.SyntaxTree;
import org.symqle.processor.GrammarException;
import org.symqle.util.Assert;
import org.symqle.util.Utils;

import java.io.File;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: aizyurov
 * Date: 25.11.2012
 * Time: 8:32:28
 * To change this template use File | Settings | File Templates.
 */
public class MethodDefinition {
    private String comment;
    private final String accessModifier;
    private final Set<String> otherModifiers;
    private final TypeParameters typeParameters;
    // null for void methods
    private final Type resultType;

    private final String name;
    private final List<FormalParameter> formalParameters;

    private final Set<Type> thrownExceptions;
    private String body;

    private final AbstractTypeDefinition owner;

    private final boolean isPublic;

    private final boolean isAbstract;

    private String sourceRef;

    public boolean isPublic() {
        return isPublic;
    }

    protected boolean isAbstract() {
        return isAbstract;
    }

    public List<FormalParameter> getFormalParameters() {
        return formalParameters;
    }

    public TypeParameters getTypeParameters() {
        return typeParameters;
    }

    public AbstractTypeDefinition getOwner() {
        return owner;
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

    public static MethodDefinition parse(final String source, final AbstractTypeDefinition owner) {
        try {
            final SimpleNode simpleNode = Utils.createParser(source).MethodDeclaration();
            SyntaxTree syntaxTree = new SyntaxTree(simpleNode, source);
            return new MethodDefinition(syntaxTree, owner);
        } catch (ParseException e) {
            throw new RuntimeException("Internal error in "+Utils.LINE_BREAK + source, e);
        } catch (GrammarException e) {
            throw new RuntimeException("Internal error in "+Utils.LINE_BREAK + source, e);
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
        if (!Utils.ACCESS_MODIFIERS.contains(accessModifier) && !"".equals(accessModifier)) {
            throw new IllegalArgumentException();
        }
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
        if (!Utils.ACCESS_MODIFIERS.contains(accessModifier) && !"".equals(accessModifier)) {
            throw new IllegalArgumentException();
        }
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
        this.isAbstract = owner.methodIsAbstract(otherModifiers);
        this.isPublic = owner.methodIsPublic(accessModifier);

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
        final String firstAttempt = formatDeclaration(", ");
        if (firstAttempt.length() < 100) {
            return firstAttempt;
        } else {
            return formatDeclaration("," + Utils.LINE_BREAK + "            ");
        }
    }

    private String formatDeclaration(final String parameterSeparator) {
        Set<String> sortedExceptions = new TreeSet<String>(Utils.map(thrownExceptions, new F<Type, String, RuntimeException>() {
            @Override
            public String apply(Type type) {
                return type.toString();  //To change body of implemented methods use File | Settings | File Templates.
            }
        }));
        StringBuilder builder = new StringBuilder();
        builder.append(accessModifier);
        if (!"".equals(accessModifier)) {
            builder.append(" ");
        }
        builder.append(Utils.format(new ArrayList<String>(otherModifiers), "", " ", " "));
        builder.append(typeParameters);
        if (!typeParameters.isEmpty()) {
            builder.append(" ");
        }
        builder.append(resultType).append(" ");
        builder.append(name)
                .append("(")
                .append(Utils.format(formalParameters, "", parameterSeparator, ""))
                .append(")")
                .append(Utils.format(sortedExceptions, " throws ", ", ", ""));
        return builder.toString();
    }

    public String toString() {
        final String sourceRefComment =
                sourceRef == null
                        ? ""
                        : Utils.LINE_BREAK + "// "+sourceRef +Utils.LINE_BREAK;
        return sourceRefComment + comment + declaration() + body;
    }

    public MethodDefinition override(final AbstractTypeDefinition targetOwner, final Model model) throws ModelException {
        final Type type = targetOwner.getAncestorTypeByName(owner.getName());
        final AbstractTypeDefinition abstractType = model.getAbstractType(type.getSimpleName());
        final Map<String, TypeArgument> mapping = abstractType.getTypeParameters().inferTypeArguments(abstractType.getType(), type);
        return replaceParams(targetOwner, mapping);
    }

    // does not change the owner (this is not correct!)
    public MethodDefinition replaceParams(final AbstractTypeDefinition targetOwner, final Map<String,TypeArgument> mapping) {
        final List<FormalParameter> newFormalParameters = new ArrayList<FormalParameter>(formalParameters.size());
        for (FormalParameter formalParameter: formalParameters) {
            newFormalParameters.add(formalParameter.replaceParams(mapping));
        }
        final Type newResultType = resultType.replaceParams(mapping);
        final Set<Type> newThrownExceptions = new HashSet<Type>();
        for (Type exceptionType: thrownExceptions) {
            newThrownExceptions.add(exceptionType.replaceParams(mapping));
        }
        final Set<String> newModifiers = new HashSet<String>(otherModifiers);
        newModifiers.add("volatile");
        newModifiers.addAll(targetOwner.addImplicitMethodModifiers(this));
        String newAccessModifier = targetOwner.implicitMethodAccessModifier(this);
        return new MethodDefinition(
                comment,
                newAccessModifier,
                newModifiers,
                this.typeParameters,
                newResultType,
                name,
                newFormalParameters,
                newThrownExceptions,
                ";",
                targetOwner,
                targetOwner.methodIsPublic(newAccessModifier),
                targetOwner.methodIsAbstract(newModifiers));
    }

//    private MethodDefinition replaceParameters(final AbstractTypeDefinition targetOwner, final TypeParameters typeParameters, final TypeArguments typeArguments) throws ModelException {
//        final List<FormalParameter> newFormalParameters = new ArrayList<FormalParameter>(formalParameters.size());
//        for (FormalParameter formalParameter: formalParameters) {
//            newFormalParameters.add(formalParameter.substituteParameters(typeParameters, typeArguments));
//        }
//        final Type newResultType = resultType.substituteParameters(typeParameters, typeArguments);
//        final Set<Type> newThrownExceptions = new HashSet<Type>();
//        for (Type exceptionType: thrownExceptions) {
//            newThrownExceptions.add(exceptionType.substituteParameters(typeParameters, typeArguments));
//        }
//        final Set<String> newModifiers = new HashSet<String>(otherModifiers);
//        newModifiers.add("volatile");
//        newModifiers.addAll(targetOwner.addImplicitMethodModifiers(this));
//        String newAccessModifier = targetOwner.implicitMethodAccessModifier(this);
//        return new MethodDefinition(
//                comment,
//                newAccessModifier,
//                newModifiers,
//                this.typeParameters,
//                newResultType,
//                name,
//                newFormalParameters,
//                newThrownExceptions,
//                ";",
//                targetOwner,
//                targetOwner.methodIsPublic(newAccessModifier),
//                targetOwner.methodIsAbstract(newModifiers));
//    }

    /**
     * A method matches another method, if they have the same
     * return type, name, formal parameters, owner and thrown exceptions.
     * Type parameters may be different, this is taken into account:
     * e.g if the return type is the first type parameter for both, is it OK etc.
     * @param other the method to compare with
     * @return
     */
    public boolean matches(MethodDefinition other) {
        if (!signature().equals(other.signature())) {
            return false;
        }
        final List<TypeParameter> myParamList = typeParameters.list();
        final List<TypeParameter> otherParamList = other.typeParameters.list();
        if (myParamList.size()!=otherParamList.size()) {
            return false;
        }
        final Map<String, TypeArgument> mapping = new HashMap<String, TypeArgument>();
        for (int i=0; i<myParamList.size(); i++) {
            mapping.put(otherParamList.get(i).getName(), new TypeArgument(myParamList.get(i).getName()));
        }
        final MethodDefinition adjusted = other.replaceParams(owner, mapping);
            return adjusted.resultType.equals(resultType)
                    && types(adjusted.formalParameters).equals(types(formalParameters))
                    && adjusted.thrownExceptions.equals(thrownExceptions);
    }

    private Collection<Type> types(List<FormalParameter> formalParameters) {
        return Utils.map(formalParameters, new F<FormalParameter, Type, RuntimeException>() {
            @Override
            public Type apply(FormalParameter formalParameter) {
                return formalParameter.getType();
            }
        });
    }

    public Type getResultType() {
        return resultType;
    }

    public String delegationInvocation(String objectName) {
        final Collection<String> parameterNames = Utils.map(formalParameters, new F<FormalParameter, String, RuntimeException>() {
            @Override
            public String apply(final FormalParameter formalParameter) {
                return formalParameter.getName();
            }
        });
        return invoke(objectName, parameterNames);
        
    }

    public String invoke(String objectName, Collection<String> arguments) {
        StringBuilder builder = new StringBuilder();
        builder.append(objectName);
        builder.append(".");
        builder.append(getName());
        builder.append("(");
        builder.append(Utils.format(arguments, "", ", ", ""));
        builder.append(")");
        return builder.toString();
    }

    public void implement(final String newAccessModifier, final String newBody)  throws ModelException {
        implement(newAccessModifier, newBody, false, true);
    }

    public void implement(final String newAccessModifier, final String newBody, boolean makeParametersFinal, boolean makeMethodFinal) throws ModelException {
        final Collection<FormalParameter> newFormalParameters = makeParametersFinal ?
                Utils.map(formalParameters, new F<FormalParameter, FormalParameter, RuntimeException>() {
                    @Override
                    public FormalParameter apply(final FormalParameter formalParameter) {
                        return formalParameter.makeFinal(true);
                    }
                }) :
                formalParameters;
        final Set<String> newModifiers = new HashSet<String>(otherModifiers);
        newModifiers.remove("abstract");
        newModifiers.remove("volatile");
        if (makeMethodFinal) {
            newModifiers.add("final");
        }
        final MethodDefinition implementation = new MethodDefinition(
                comment,
                newAccessModifier,
                newModifiers,
                typeParameters,
                resultType,
                name,
                new ArrayList<FormalParameter>(newFormalParameters),
                thrownExceptions,
                newBody,
                owner,
                owner.methodIsPublic(newAccessModifier),
                false);
        implementation.setSourceRef(this.getSourceRef());
        owner.addMethod(
                implementation
        );
    }

    public void replaceBody(String newBody) {
        this.body = newBody;
    }

    public void makeStatic() throws ModelException {
        if (isAbstract()) {
            throw new ModelException("Abstract method cannot be static");
        }
        otherModifiers.add("static");
    }

    public void declareAbstract(final String newAccessModifier) throws ModelException {
        final Set<String> newModifiers = new HashSet<String>(otherModifiers);
        newModifiers.add("abstract");
        newModifiers.remove("volatile");
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
                    owner.methodIsPublic(newAccessModifier),
                    true)
        );
    }

    public void pullUpAbstractMethod(final ClassDefinition newOwner) throws ModelException {
        final Set<String> newModifiers = new HashSet<String>(otherModifiers);
        newModifiers.add("abstract");
        newOwner.addMethod(
                new MethodDefinition(
                        comment,
                        accessModifier,
                        newModifiers,
                        typeParameters,
                        resultType,
                        name,
                        formalParameters,
                        thrownExceptions,
                        ";",
                        newOwner,
                        isPublic,
                        true)
        );
    }

    public Set<Type> getThrownExceptions() {
        return thrownExceptions;
    }

    public String getComment() {
        return comment;
    }

    public void setSourceRef(final SyntaxTree node) {
        final String name = new File(node.getFileName()).getName();
        this.sourceRef = name + ":" + node.getLine();
    }

    public void setSourceRef(final String sourceRef) {
        this.sourceRef = sourceRef;
    }

    public String getSourceRef() {
        return sourceRef;
    }

    public void replaceComment(String newComment) {
        comment = newComment;
    }
}
