package org.simqle.model;

import org.simqle.parser.SyntaxTree;
import org.simqle.processor.GrammarException;
import org.simqle.util.Assert;

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
    private final String returnedInterfaceName;

    public ProductionRule(SyntaxTree node) throws GrammarException {
        Assert.assertOneOf(new GrammarException("Unexpected type: "+node.getType(), node), node.getType(), "ProductionRule");
        final SyntaxTree productionDeclaration = node.getParent().getParent();
        Type returnType = productionDeclaration.find("ClassOrInterfaceType", Type.CONSTRUCT).get(0);
        returnedInterfaceName = returnType.getNameChain().get(0).getName();

        ruleElements = new ArrayList<RuleElement>();
        for (SyntaxTree element: node.find("ProductionElement")) {
            final List<TypeNameWithTypeArguments> classOrInterfaceTypeNodes = element.find("IdentifierWithTypeArguments", TypeNameWithTypeArguments.CONSTRUCT);
            // mandatory and unique
            final String name = element.find("Identifier").get(0).getValue();
            if (classOrInterfaceTypeNodes.isEmpty()) {
                try {
                    ruleElements.add(new RuleElement(name));
                } catch (ModelException e) {
                    throw new GrammarException(e, node);
                }
            } else {
                ruleElements.add(new RuleElement(new Type(classOrInterfaceTypeNodes, 0), name));
            }
        }
    }

    public String getName() {
        StringBuilder builder = new StringBuilder();
        builder.append(returnedInterfaceName)
                .append("_IS");
        for (RuleElement element: ruleElements) {
            builder.append("_");
            builder.append(element.isConst ? element.name : element.type.getNameChain().get(0).getName());
        }
        return builder.toString();
    }

    public List<RuleElement> getElements() {
        return new ArrayList<RuleElement>(ruleElements);
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
    }

}
