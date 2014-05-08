package org.symqle.model;

import org.symqle.parser.SyntaxTree;
import org.symqle.processor.GrammarException;
import org.symqle.util.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Full information for syntax production implementation detail.
 * Most ProductionImplementations have associated static method of Symqle class,
 * which is generated.
 */
public class ProductionImplementation {
    private final List<RuleElement> ruleElements;
    private final List<FormalParameter> formalParameters = new ArrayList<FormalParameter>();
    private final TypeParameters typeParameters;
    private final Type targetType;
    private final Type returnType;
    private final Type implementationType;
    // this is the name of associated method
    private final String name;
    private final boolean implicit;
    private final String accessModifier;
    private final String comment;
    private final String sourceRef;

    /**
     * Construct from AST.
     * @param node syntax tree
     * @throws GrammarException wrong tree
     */
    public ProductionImplementation(final SyntaxTree node) throws GrammarException {
        AssertNodeType.assertOneOf(node, "ProductionImplementation");
        targetType = node.find("^.^.ClassOrInterfaceType", Type.CONSTRUCT).get(0);
        final List<Type> returnTypes = node.find("ClassOrInterfaceType", Type.CONSTRUCT);
        // exactry one type by syntax; no type if implicit (in this case it is targetType)
        returnType = returnTypes.isEmpty() ? targetType : returnTypes.get(0);
        implicit = node.find("Identifier").isEmpty();
        implementationType = returnType;
        ruleElements = node.find("^.ProductionRule.ProductionElement",
                new F<SyntaxTree, RuleElement, GrammarException>() {
            @Override
            public RuleElement apply(final SyntaxTree syntaxTree) throws GrammarException {
                final List<Type> types = syntaxTree.find("ClassOrInterfaceType", Type.CONSTRUCT);
                // mandatory and unique
                final String identifier = syntaxTree.find("Identifier").get(0).getValue();
                if (types.isEmpty()) {
                    try {
                        return new ConstantElement(identifier);
                    } catch (ModelException e) {
                        throw new GrammarException(e, syntaxTree);
                    }
                } else {
                    final Type type = types.get(0);
                    formalParameters.add(new FormalParameter(type, identifier));
                    // make sure it is known Symqle interface
                    return new VariableElement(type, identifier);
                }
            }
        });
        // check that implicit conversion has a single parameter
        if (implicit && formalParameters.size() != 1) {
            throw new GrammarException("Implicit conversion must have one parameter, found: "
                    + formalParameters.toString(), node);
        }
        typeParameters = new TypeParameters(node.find("^.^.TypeParameters.TypeParameter", TypeParameter.CONSTRUCT));
        name =  implicit
                ? "z$" + targetType.getSimpleName() + "$from$" + formalParameters.get(0).getType().getSimpleName()
                : node.find("Identifier", SyntaxTree.VALUE).get(0);
        // implicit methods are package scope; others may have any access modifier
        accessModifier = implicit ? "" : Utils.getAccessModifier(node.find("MethodModifiers.MethodModifier"));
        comment = node.find("^.ProductionRule").get(0).getComments();
        this.sourceRef = new File(node.getFileName()).getName() + ":" + node.getLine();
    }


    /**
     * Syntax rule elements.
     * @return elements in order
     */
    public final List<RuleElement> getElements() {
        return new ArrayList<RuleElement>(ruleElements);
    }

    /**
     * Syntax rule elements, which are variables, in the order they appear in the rule.
     * @return variables
     */
    public final List<RuleElement> getVariableElements() {
        final List<RuleElement> vars = new ArrayList<RuleElement>();
        for (RuleElement element : getElements()) {
            if (!element.isConstant()) {
                vars.add(element);
            }
        }
        return vars;
    }

    /**
     * Name of the rule is also name of associated Symqle static method.
     * @return name
     */
    public final String getName() {
        return name;
    }

    @Override
    public final String toString() {
        return typeParameters + (typeParameters.isEmpty() ? "" : " ") + targetType
                + " ::= " + Utils.format(ruleElements, "", " ", "");
    }

    /**
     * Formal return type required by syntax rule.
     * @return return type
     */
    public final Type getReturnType() {
        return returnType;
    }

    /**
     * Return type of associated Symqle method. It is same as {@link #getReturnType()} or its subtype.
     * @return return type of associated Symqle method
     */
    public final Type getImplementationType() {
        return implementationType;
    }

    /**
     * Find out whether this rule defines implicit conversion.
     * @return true if implicit conversion, false if factory method.
     */
    public final boolean isImplicit() {
        return implicit;
    }

    /**
     * Formal parameters of rule and associated Symqle method.
     * @return formal parameters
     */
    public final List<FormalParameter> getFormalParameters() {
        return new ArrayList<FormalParameter>(formalParameters);
    }

    /**
     * Rule element. May be variable or constant.
     */
    public interface RuleElement {
        /**
         * Convert this element to a representation of SqlBuilder.
         * This is argument type of all methods of Dialect interface.
         * Constants are SqlBuilders itself; variables use archetype method
         * to create SqlBuilder.
         * @param model is consulted for interface definitions to find archetype method.
         * @return text as described
         * @throws ModelException wrong model
         */
        String asMethodArgument(Model model) throws ModelException;

        /**
         * True if this is a constant element.
         * @return true if constant, false if variable
         */
        boolean isConstant();
    }

    private static class ConstantElement implements RuleElement {
        private final String name;

        @Override
        public boolean isConstant() {
            return true;
        }

        /**
         * Construct with given name.
         * @param name should be one of terminal symbols defined in Constants class.
         * @throws ModelException no constant with this name
         */
        public ConstantElement(final String name) throws ModelException {
            this.name = name;
            if (!Constants.isConstant(name)) {
                throw new ModelException(name + " is not SqlTerm; is type missing?");
            }
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public String asMethodArgument(final Model model) throws ModelException {
            return name;
        }
    }

    private static class VariableElement implements RuleElement {
        private final Type type;
        private final String name;

        /**
         * Construct from given type and name.
         * @param type variable type
         * @param name variable name
         */
        public VariableElement(final Type type, final String name) {
            this.type = type;
            this.name = name;
        }

        @Override
        public boolean isConstant() {
            return false;
        }

        @Override
        public String toString() {
            return name + ":" + type;
        }

        @Override
        public String asMethodArgument(final Model model) throws ModelException {
            final InterfaceDefinition definition = model.getInterface(type);
            if (definition == null) {
                throw new ModelException("Unknown interface " + type);
            }
            final MethodDefinition archetypeMethod = definition.getArchetypeMethod();
            if (archetypeMethod == null) {
                throw new IllegalStateException("No archetype method in " + type);
            }
            return archetypeMethod.delegationInvocation(name);
        }

    }

    /**
     * Java-style comment from SDL source.
     * @return comment
     */
    public final String getComment() {
        return comment;
    }

    /**
     * Location in SDL source.
     * @return fileName:line
     */
    public final String getSourceRef() {
        return sourceRef;
    }

    /**
     * Type parameters of this production rule.
     * @return type parameters
     */
    public final TypeParameters getTypeParameters() {
        return typeParameters;
    }

    /**
     * Access modifier of associated Symqle method.
     * @return access modifier. Empty string for package scope.
     */
    public final String getAccessModifier() {
        return accessModifier;
    }
}
