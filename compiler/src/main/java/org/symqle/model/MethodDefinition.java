package org.symqle.model;

import org.symqle.parser.ParseException;
import org.symqle.parser.SimpleNode;
import org.symqle.parser.SymqleParser;
import org.symqle.parser.SyntaxTree;
import org.symqle.processor.GrammarException;
import org.symqle.util.AssertNodeType;
import org.symqle.util.Utils;

import java.io.File;
import java.util.*;

/**
 * Java method definition.
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

    /**
     * Is this method public.
     * @return true if public (declared or interface method)
     */
    public final boolean isPublic() {
        return isPublic;
    }

    /**
     * Is this method abstract.
     * @return true if abstract (declared or interface method)
     */
    protected final boolean isAbstract() {
        return isAbstract;
    }

    /**
     * Formal parameters of the method.
     * @return formal parameters
     */
    public final List<FormalParameter> getFormalParameters() {
        return formalParameters;
    }

    /**
     * Type parameters of the method.
     * @return type parameters
     */
    public final TypeParameters getTypeParameters() {
        return typeParameters;
    }

    /**
     * Class or interface to which thid method belong.
     * @return owner
     */
    public final AbstractTypeDefinition getOwner() {
        return owner;
    }

    /**
     * Parse abstract method (no body).
     * @param source valid text for interface method or abstract method
     * @param owner class or interface to which this method belongs
     * @return constructed method
     */
    public static MethodDefinition parseAbstract(final String source, final AbstractTypeDefinition owner) {
        try {
            final SimpleNode simpleNode = SymqleParser.createParser(source).AbstractMethodDeclaration();
            SyntaxTree syntaxTree = new SyntaxTree(simpleNode, source);
            return new MethodDefinition(syntaxTree, owner);
        } catch (ParseException e) {
            throw new RuntimeException("Internal error", e);
        } catch (GrammarException e) {
            throw new RuntimeException("Internal error", e);
        }
    }

    /**
     * Parse class method.
     * @param source valid text method
     * @param owner class or interface to which this method belongs
     * @return constructed method
     */
    public static MethodDefinition parse(final String source, final AbstractTypeDefinition owner) {
        try {
            final SimpleNode simpleNode = SymqleParser.createParser(source).MethodDeclaration();
            SyntaxTree syntaxTree = new SyntaxTree(simpleNode, source);
            return new MethodDefinition(syntaxTree, owner);
        } catch (ParseException e) {
            throw new RuntimeException("Internal error in " + Utils.LINE_BREAK + source, e);
        } catch (GrammarException e) {
            throw new RuntimeException("Internal error in " + Utils.LINE_BREAK + source, e);
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

    /**
     * Construct from AST.
     * @param node syntax tree
     * @param owner class or interface, which owns the method
     * @throws GrammarException wrong tree
     */
    public MethodDefinition(final SyntaxTree node, final AbstractTypeDefinition owner) throws GrammarException {
        final String nodeType = node.getType();
        AssertNodeType.assertOneOf(node, "MethodDeclaration", "AbstractMethodDeclaration");
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
        formalParameters =
                node.find("MethodDeclarator.FormalParameterList.FormalParameter", FormalParameter.CONSTRUCT);
        formalParameters.addAll(node.find("MethodDeclarator.FormalParameterList.FormalParameterWithEllipsis",
                        FormalParameter.CONSTRUCT));
        // may be at most one by grammar
        this.thrownExceptions = new HashSet<Type>(node.find("Throws.ExceptionType", Type.CONSTRUCT));
        this.owner = owner;
        this.isAbstract = owner.methodIsAbstract(otherModifiers);
        this.isPublic = owner.methodIsPublic(accessModifier);

    }

    /**
     * Method name.
     * @return name
     */
    public final String getName() {
        return name;
    }

    /**
     * Method signature.
     * @return signature. Format is different from JVM format! return type is not included.
     * primitive types are just their names, not special abbreviations. Arrays are marked with [] after type name.
     * Example: equal(int[],int[])
     */
    public final String signature() {
        // TODO
        HashSet<String> typeParameterNames = new HashSet<String>(typeParameters.names());
        typeParameterNames.addAll(owner.getTypeParameters().names());
        Collection<String> formalParameterErasures =
                Utils.map(formalParameters, FormalParameter.f_erasure(typeParameterNames));
        return name + "(" + Utils.format(formalParameterErasures, "", ",", "") + ")";
    }

    /**
     * Access modifier.
     * @return miodifier, Empty string for package scope.
     */
    public final String getAccessModifier() {
        return accessModifier;
    }

    /**
     * Modifiers other than access modifier: static, final etc.
     * @return modifiers
     */
    public final Set<String> getOtherModifiers() {
        return otherModifiers;
    }

    /**
     * Nethod declaration - from modifiers to throws clause.
     * @return declaration
     */
    public final String declaration() {
        final String firstAttempt = formatDeclaration(", ");
        if (firstAttempt.length() < 100) {
            return firstAttempt;
        } else {
            return formatDeclaration("," + Utils.LINE_BREAK + "            ");
        }
    }

    private String formatDeclaration(final String parameterSeparator) {
        Set<String> sortedExceptions =
                new TreeSet<String>(Utils.map(thrownExceptions, new F<Type, String, RuntimeException>() {
            @Override
            public String apply(final Type type) {
                return type.toString();  //To change body of implemented methods use File | Settings | File Templates.
            }
        }));
        StringBuilder builder = new StringBuilder();
        // a hack here: protected static methods make little sense (they are not inherited); show them as
        // package scope
        if (!(otherModifiers.contains("static") && accessModifier.equals("protected"))) {
            builder.append(accessModifier);
            if (!"".equals(accessModifier)) {
                builder.append(" ");
            }
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

    @Override
    public final String toString() {
        final String sourceRefComment =
                sourceRef == null
                        ? ""
                        : Utils.LINE_BREAK + "// " + sourceRef + Utils.LINE_BREAK;
        return sourceRefComment + comment + declaration() + body;
    }

    /**
     * Create a method for targetOwner, which properly overrides declaration of {@code this}.
     * The created method has empty body (semicolon) and "volatile" modifier.
     * All type arguments are adjusted for targetOwner. For example, if targetOwner implements {@code List<String>},
     * and {@code this} is method {@code boolean add(E e)} where E is declared in List definition:
     * {@code public interface List<E>},
     * then the resulting method would be {@code public abstract volatile boolean add(String e);}.
     * The method is not added to targetOwner; it may be used for further modifications e.g. add body) before adding.
     * @param targetOwner the recipient of the method
     * @param model collection of known classes and interfaces
     * @return constructed method
     * @throws ModelException wrong model
     */
    public final MethodDefinition override(final AbstractTypeDefinition targetOwner, final Model model)
            throws ModelException {
        final Type type = targetOwner.getAncestorTypeByName(owner.getName());
        final AbstractTypeDefinition abstractType = model.getAbstractType(type.getSimpleName());
        final Map<String, TypeArgument> mapping =
                abstractType.getTypeParameters().inferTypeArguments(abstractType.getType(), type);
        return replaceParams(targetOwner, mapping);
    }

    /**
     * Replaces type parameters as necessary for targetOwner.
     * See example in {@link #override(AbstractTypeDefinition, Model)}
     * @param targetOwner new owner
     * @param mapping type paremeters map
     * @return new method with replaced parameters
     */
    public final MethodDefinition replaceParams(final AbstractTypeDefinition targetOwner,
                                                final Map<String, TypeArgument> mapping) {
    // does not change the owner (this is not correct!)
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
        newModifiers.addAll(targetOwner.implicitMethodModifiers(this));
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

    /**
     * A method matches another method, if they have the same
     * return type, name, formal parameters, owner and thrown exceptions.
     * Type parameters may be different, this is taken into account:
     * e.g if the return type is the first type parameter for both, is it OK etc.
     * @param other the method to compare with
     * @return true if matches
     */
    public final boolean matches(final MethodDefinition other) {
        if (!signature().equals(other.signature())) {
            return false;
        }
        final List<TypeParameter> myParamList = typeParameters.list();
        final List<TypeParameter> otherParamList = other.typeParameters.list();
        if (myParamList.size() != otherParamList.size()) {
            return false;
        }
        final Map<String, TypeArgument> mapping = new HashMap<String, TypeArgument>();
        for (int i = 0; i < myParamList.size(); i++) {
            mapping.put(otherParamList.get(i).getName(), new TypeArgument(myParamList.get(i).getName()));
        }
        final MethodDefinition adjusted = other.replaceParams(owner, mapping);
            return adjusted.resultType.equals(resultType)
                    && types(adjusted.formalParameters).equals(types(formalParameters))
                    && adjusted.thrownExceptions.equals(thrownExceptions);
    }


    private static Collection<Type> types(final List<FormalParameter> parameters) {
        return Utils.map(parameters, new F<FormalParameter, Type, RuntimeException>() {
            @Override
            public Type apply(final FormalParameter formalParameter) {
                return formalParameter.getType();
            }
        });
    }

    /**
     * Result type of this method.
     * @return result type, {@link Type#VOID} for void methods.
     */
    public final Type getResultType() {
        return resultType;
    }

    /**
     * Constructs a string, which calls {@code this} on an object, providing parameter names as arguments.
     * @param objectName the object
     * @return the constructed text
     */
    public final String delegationInvocation(final String objectName) {
        final Collection<String> parameterNames =
                Utils.map(formalParameters, new F<FormalParameter, String, RuntimeException>() {
            @Override
            public String apply(final FormalParameter formalParameter) {
                return formalParameter.getName();
            }
        });
        return invoke(objectName, parameterNames);

    }

    /**
     * Constructs a string, which calls {@code this} on an object with given arguments.
     * @param objectName the object
     * @param arguments arguments to call with
     * @return the constructed text
     */
    public final String invoke(final String objectName, final Collection<String> arguments) {
        StringBuilder builder = new StringBuilder();
        builder.append(objectName);
        builder.append(".");
        builder.append(getName());
        builder.append("(");
        builder.append(Utils.format(arguments, "", ", ", ""));
        builder.append(")");
        return builder.toString();
    }

    /**
     * Implement a method and add the result to the owner of this.
     * It is expected that {@code this} is not attached to the owner yet, for example,
     * it was created by {@link #parse(String, AbstractTypeDefinition)}
     * or {@link #override(AbstractTypeDefinition, Model)}.
     * @param newAccessModifier new access modifier
     * @param newBody implementation
     * @param makeParametersFinal if true, make all parameters of implemented method final
     * @param makeMethodFinal if true, make the implemented method final
     * @throws ModelException duplicate method
     */
    public final void implement(final String newAccessModifier,
                                final String newBody,
                                final boolean makeParametersFinal,
                                final boolean makeMethodFinal) throws ModelException {
        final Collection<FormalParameter> newFormalParameters = makeParametersFinal
                ? Utils.map(formalParameters, new F<FormalParameter, FormalParameter, RuntimeException>() {
                    @Override
                    public FormalParameter apply(final FormalParameter formalParameter) {
                        return formalParameter.makeFinal(true);
                    }
                })
                : formalParameters;
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

    /**
     * Make method static. Modifies {@code this}.
     * @throws ModelException conflicting modifiers
     */
    public final void makeStatic() throws ModelException {
        if (isAbstract()) {
            throw new ModelException("Abstract method cannot be static");
        }
        otherModifiers.add("static");
    }

     /**
      * Create a copy of current method, which is explicitly abstract, and add it
      * to the owner.
      * It is expected that {@code this} is not attached to the owner yet, for example,
      * it was created by {@link #parse(String, AbstractTypeDefinition)}
      * or {@link #override(AbstractTypeDefinition, Model)}.
     * @param newAccessModifier new access modifier
     * @throws ModelException duplicate method
     */
    public final void declareAbstract(final String newAccessModifier) throws ModelException {
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

    /**
     * Thrown exceptions.
     * @return thrown exceptions
     */
    public final Set<Type> getThrownExceptions() {
        return thrownExceptions;
    }

    /**
     * Method comment.
     * @return comment
     */
    public final String getComment() {
        return comment;
    }

    /**
     * Sets location of sdl source - file:line.
     * @param node the node where the method is defined. For auto-generated methods it may be the node
     * corresponding to syntax rule or something else.
     */
    public final void setSourceRef(final SyntaxTree node) {
        final String fileName = new File(node.getFileName()).getName();
        this.sourceRef = fileName + ":" + node.getLine();
    }

     /**
      * Sets location of sdl source - file:line.
     * @param sourceRef reference in file:line format
     */
    public final void setSourceRef(final String sourceRef) {
        this.sourceRef = sourceRef;
    }

    /**
     * Location of sdl source - file:line.
     * @return location
     */
    public final String getSourceRef() {
        return sourceRef;
    }

    /**
     * Replaces a method comment with a new one. Modifies {@code this}.
     * @param newComment replacement
     */
    public final void replaceComment(final String newComment) {
        comment = newComment;
    }
}
