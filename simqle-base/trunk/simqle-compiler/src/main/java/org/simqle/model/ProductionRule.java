package org.simqle.model;

import org.simqle.parser.SyntaxTree;

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

    public ProductionRule(SyntaxTree node) {
        if (!node.getType().equals("ProductionRule")) {
            throw new IllegalArgumentException("Illegal argument: "+node);
        }
        ruleElements = new ArrayList<RuleElement>();
        for (SyntaxTree element: node.find("ProductionElement")) {
            final List<TypeNameWithTypeArguments> classOrInterfaceTypeNodes = Utils.convertChildren(element, "IdentifierWithTypeArguments", TypeNameWithTypeArguments.class);
            // mandatory and unique
            final String name = element.find("Identifier").get(0).getValue();
            if (classOrInterfaceTypeNodes.isEmpty()) {
                ruleElements.add(new RuleElement(name));
            } else {
                ruleElements.add(new RuleElement(new Type(classOrInterfaceTypeNodes, 0), name));
            }
        }
    }

    public String getName() {
        StringBuilder builder = new StringBuilder();
        for (RuleElement element: ruleElements) {
            if (builder.length()==0) {
                builder.append("_");
            }
            builder.append(element.isConst ? element.name : element.type.getNameChain().get(0).getText());
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

        private RuleElement(final String constant) {
            this.type = null;
            this.name = constant;
            this.isConst = true;
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
