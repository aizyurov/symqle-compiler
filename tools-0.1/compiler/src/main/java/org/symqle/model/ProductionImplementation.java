package org.symqle.model;

import org.symqle.parser.SyntaxTree;
import org.symqle.processor.GrammarException;
import org.symqle.util.Assert;
import org.symqle.util.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: lvovich
 * Date: 26.06.12
 * Time: 17:05
 * To change this template use File | Settings | File Templates.
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

    public ProductionImplementation(SyntaxTree node) throws GrammarException {
        Assert.assertOneOf(new GrammarException("Unexpected type: "+node.getType(), node), node.getType(), "ProductionImplementation");
        targetType = node.find("^.^.ClassOrInterfaceType", Type.CONSTRUCT).get(0);
        final List<Type> returnTypes = node.find("ClassOrInterfaceType", Type.CONSTRUCT);
        // exactry one type by syntax; no type if implicit (in this case it is targetType)
        returnType = returnTypes.isEmpty() ? targetType : returnTypes.get(0);
        implementationType = returnType;
        implicit = node.find("Identifier").isEmpty();
        ruleElements = node.find("^.ProductionRule.ProductionElement", new F<SyntaxTree, RuleElement, GrammarException>() {
            @Override
            public RuleElement apply(final SyntaxTree syntaxTree) throws GrammarException {
                final List<Type> types = syntaxTree.find("ClassOrInterfaceType", Type.CONSTRUCT);
                // mandatory and unique
                final String name = syntaxTree.find("Identifier").get(0).getValue();
                if (types.isEmpty()) {
                    try {
                        return new ConstantElement(name);
                    } catch (ModelException e) {
                        throw new GrammarException(e, syntaxTree);
                    }
                } else {
                    final Type type = types.get(0);
                    formalParameters.add(new FormalParameter(type, name));
                    // make sure it is known Symqle interface
                    return new VariableElement(type, name);
                }
            }
        });
        // check that implicit conversion has a single parameter
        if (implicit && formalParameters.size()!=1) {
            throw new GrammarException("Implicit conversion must have one parameter, found: "
                    +formalParameters.toString(), node);
        }
        typeParameters = new TypeParameters(node.find("^.^.TypeParameters.TypeParameter", TypeParameter.CONSTRUCT));
        name =  implicit ?
                "z$"+targetType.getSimpleName()+"$from$"+formalParameters.get(0).getType().getSimpleName() :
                node.find("Identifier", SyntaxTree.VALUE).get(0);
        // implicit methods are package scope; others may have any access modifier
        accessModifier = implicit ? "" : Utils.getAccessModifier(node.find("MethodModifiers.MethodModifier"));
        comment = node.find("^.ProductionRule").get(0).getComments();
        this.sourceRef = new File(node.getFileName()).getName() + ":" + node.getLine();
    }



    public String generatedComment() {
        return
            "    /**" + Utils.LINE_BREAK +
            "    * {@code " + toString() +"}" + Utils.LINE_BREAK +
            Utils.format(formalParameters, "", Utils.LINE_BREAK, Utils.LINE_BREAK,
                new F<FormalParameter, String, RuntimeException>() {
                    @Override
                    public String apply(final FormalParameter ruleElement) {
                        return "    * @param "+ruleElement.getName() +" see rule above";
                    }
                }) +
                    "    * @return "+ returnType.getSimpleName() + " constructed according to the rule" + Utils.LINE_BREAK +
                    "    */"  + Utils.LINE_BREAK;

    }

    public List<RuleElement> getElements() {
        return new ArrayList<RuleElement>(ruleElements);
    }

    public List<RuleElement> getVariableElements() {
        final List<RuleElement> vars = new ArrayList<RuleElement>();
        for (RuleElement element : getElements()) {
            if (!element.isConstant()) {
                vars.add(element);
            }
        }
        return vars;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return typeParameters + (typeParameters.isEmpty() ? "" : " " ) + targetType + " ::= " + Utils.format(ruleElements, "", " ", "");
    }

    public String asStaticMethodDeclaration() {
        return accessModifier+" "+"static "+ typeParameters + (typeParameters.isEmpty() ? "" : " " ) + returnType + " " + name
                + "(" + Utils.format(formalParameters, "", ", ", "", new F<FormalParameter, String, RuntimeException>() {
                            @Override
                            public String apply(final FormalParameter formalParameter) {
                                return "final "+formalParameter.toString();
                            }
                        }) +")";
    }

    public Type getReturnType() {
        return returnType;
    }

    public Type getImplementationType() {
        return implementationType;
    }

    public String asMethodDeclaration() {
        return typeParameters + (typeParameters.isEmpty() ? "" : " " ) + targetType + " " + name
                + "(" + Utils.format(formalParameters, "", ", ", "", new F<FormalParameter, String, RuntimeException>() {
            @Override
            public String apply(final FormalParameter formalParameter) {
                return "final "+formalParameter.toString();
            }
        }) +")";
    }

    public boolean isImplicit() {
        return implicit;
    }

    public List<FormalParameter> getFormalParameters() {
        return new ArrayList<FormalParameter>(formalParameters);
    }

    public interface RuleElement {
        String asMethodArgument(final Model model) throws ModelException;
        boolean isConstant();
    }

    private static class ConstantElement implements RuleElement {
        private final String name;

        @Override
        public boolean isConstant() {
            return true;
        }

        private ConstantElement(final String name) throws ModelException {
            this.name = name;
            if (!Constants.isConstant(name)) {
                throw new ModelException(name + " is not SqlTerm; is type missing?");
            }
        }

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

        private VariableElement(final Type type, final String name) {
            this.type = type;
            this.name = name;
        }

        @Override
        public boolean isConstant() {
            return false;
        }

        public String toString() {
            return name+":"+type;
        }

        public String asMethodArgument(final Model model) throws ModelException {
            try {
                return model.getInterface(type).getArchetypeMethod().delegationInvocation(name);
            } catch (NullPointerException e) {
                throw e;
            }
        }

    }

    public String getComment() {
        return comment;
    }

    public String getSourceRef() {
        return sourceRef;
    }
}