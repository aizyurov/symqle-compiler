package org.simqle.model;

import org.simqle.parser.SyntaxTree;
import org.simqle.processor.GrammarException;
import org.simqle.util.Assert;
import org.simqle.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: lvovich
 * Date: 26.06.12
 * Time: 17:05
 * To change this template use File | Settings | File Templates.
 */
public class ProductionRule {
    private final List<RuleElement> ruleElements;
    private final List<FormalParameter> formalParameters = new ArrayList<FormalParameter>();
    private final TypeParameters typeParameters;
    private final Type targetType;
    private final Type returnType;
    // this is the name of associated method
    private final String name;
    private final boolean implicit;

    public ProductionRule(SyntaxTree node) throws GrammarException {
        Assert.assertOneOf(new GrammarException("Unexpected type: "+node.getType(), node), node.getType(), "ProductionRule");
        targetType = node.find("^.^.ClassOrInterfaceType", Type.CONSTRUCT).get(0);
        final List<Type> returnTypes = node.find("^.ProductionImplementation.ClassOrInterfaceType", Type.CONSTRUCT);
        // at most one type by syntax; no type if implicit (in this case it is targetType)
        returnType = returnTypes.isEmpty() ? targetType : returnTypes.get(0);
        implicit = returnTypes.isEmpty();
        ruleElements = node.find("ProductionElement", new F<SyntaxTree, RuleElement, GrammarException>() {
            @Override
            public RuleElement apply(final SyntaxTree syntaxTree) throws GrammarException {
                final List<Type> types = syntaxTree.find("ClassOrInterfaceType", Type.CONSTRUCT);
                // mandatory and unique
                final String name = syntaxTree.find("Identifier").get(0).getValue();
                if (types.isEmpty()) {
                    try {
                        return new RuleElement(name);
                    } catch (ModelException e) {
                        throw new GrammarException(e, syntaxTree);
                    }
                } else {
                    final Type type = types.get(0);
                    formalParameters.add(new FormalParameter(type, name));
                    return new RuleElement(type, name);
                }
            }
        });
        typeParameters = new TypeParameters(node.find("^.^.TypeParameters.TypeParameter", TypeParameter.CONSTRUCT));
        name = node.find("^.ProductionImplementation.Identifier", SyntaxTree.VALUE).get(0);
    }

    public List<RuleElement> getElements() {
        return new ArrayList<RuleElement>(ruleElements);
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return typeParameters + (typeParameters.isEmpty() ? "" : " " ) + targetType + " ::= " + Utils.format(ruleElements, "", " ", "");
    }

    public String asAbstractMethodDeclaration() {
        return "abstract "+ typeParameters + (typeParameters.isEmpty() ? "" : " " ) + returnType + " " + name
                + "(" + Utils.format(formalParameters, "", ", ", "") +")";
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

    public static class RuleElement {
        private final Type type;
        private final boolean isConst;
        private final String name;

        private RuleElement(final Type type, final String name) {
            this.type = type;
            this.name = name;
            this.isConst = false;
        }

        private RuleElement(final String constant) throws ModelException {
            if (Constants.isConstant(constant)) {
                this.type = null;
                this.name = constant;
                this.isConst = true;
            } else {
                throw new ModelException(constant+" is not a constant non-terminal");
            }
        }

        public Type getType() {
            return type;
        }

        public boolean isConst() {
            return isConst;
        }

        public String getName() {
            return name;
        }

        public String toString() {
            return isConst() ? name : name+":"+type;
        }

        public String asMethodArgument(final Model model) throws ModelException {
            if (this.type==null) {
                return name;
            } else {
                return model.getInterface(type).getArchetypeMethod().invoke(name);
            }
        }

    }

}
