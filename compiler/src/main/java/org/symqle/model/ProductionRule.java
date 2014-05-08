package org.symqle.model;

import org.symqle.parser.SyntaxTree;
import org.symqle.processor.GrammarException;
import org.symqle.util.AssertNodeType;
import org.symqle.util.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * All detail of syntax production rule.
 * Each rule has associated method of Dialect interface.
 */
public class ProductionRule {
    private final List<FormalParameter> formalParameters = new ArrayList<FormalParameter>();
    // this is the name of associated method
    private final String name;
    private final String syntax;
    private final List<String> elementNames;
    private final String targetTypeName;
    private final String shortRule;

    /**
     * Construct from AST.
     * @param node syntax tree
     * @throws GrammarException wrong tree
     */
    public ProductionRule(final SyntaxTree node) throws GrammarException {
        AssertNodeType.assertOneOf(node, "ProductionRule");
        final StringBuilder nameBuilder = new StringBuilder();
        final StringBuilder syntaxBuilder = new StringBuilder();
        final StringBuilder shortFormBuilder = new StringBuilder();
        elementNames = new ArrayList<String>();
        // exactly one
        Type targetType = node.find("^.^.ClassOrInterfaceType", Type.CONSTRUCT).get(0);
        targetTypeName = targetType.getSimpleName();
        syntaxBuilder.append(targetTypeName).append(" ::=");
        nameBuilder.append(targetType.getSimpleName()).append("_is");
        final Type sqlType = new Type("SqlBuilder");
        // at most one type by syntax; no type if implicit (in this case it is targetType)
        for (SyntaxTree element: node.find("ProductionElement")) {
            final List<Type> typeList = element.find("ClassOrInterfaceType", Type.CONSTRUCT);
            final Type type = typeList.isEmpty() ? null : typeList.get(0);
            final String identifier = element.find("Identifier").get(0).getValue();
            final String descriptiveName = type != null ? type.getSimpleName() : identifier;
            final String syntaxElement = type != null ? identifier + ":" + descriptiveName : descriptiveName;
            nameBuilder.append("_").append(descriptiveName);
            shortFormBuilder.append(" ").append(descriptiveName);
            syntaxBuilder.append(" ").append(syntaxElement);
            if (type != null) {
                formalParameters.add(
                        new FormalParameter(sqlType, identifier, Collections.singletonList("final"), false));
            }
            elementNames.add(identifier);
        }
        name = nameBuilder.toString();
        syntax = syntaxBuilder.toString();
        shortRule = shortFormBuilder.toString();
    }

    /**
     * Javadoc for Dialect method for this rule.
     * @return formatted javadoc text
     */
    public final String generatedComment() {
        return
            "    /**" + Utils.LINE_BREAK
        +   "    * {@code " + toString() + "}." + Utils.LINE_BREAK
        + Utils.format(formalParameters, "", Utils.LINE_BREAK, Utils.LINE_BREAK,
                new F<FormalParameter, String, RuntimeException>() {
                    @Override
                    public String apply(final FormalParameter ruleElement) {
                        return "    * @param " + ruleElement.getName() + " see rule above";
                    }
                })
        +   "    * @return Sql constructed according to the rule" + Utils.LINE_BREAK
        +   "    */"  + Utils.LINE_BREAK;
    }

    /**
     * Name of this rule, also name of associated Dialect method.
     * @return name
     */
    public final String getName() {
        return name;
    }

    @Override
    public final String toString() {
        return syntax;
    }

    /**
     * Method declaration for Dialect interface.
     * @return formatted declaration
     */
    public final String asAbstractMethodDeclaration() {
        return generatedComment() + "SqlBuilder " + name
                + "(" + Utils.format(formalParameters, "", ", ", "") + ")";
    }

    /**
     * Comma-separated list of element names (for use in Dialect and GenericDialect).
     * @return argument list
     */
    public final String asMethodArguments() {
        return Utils.format(elementNames, "", ", ", "");
    }

    /**
     * Formal parameters of associated method.
     * @return formal parameters
     */
    public final List<FormalParameter> getFormalParameters() {
        return new ArrayList<FormalParameter>(formalParameters);
    }

    /**
     * Rule target.
     * @return target
     */
    public String getTargetTypeName() {
        return targetTypeName;
    }

    /**
     * Rule right part with java semantic information skipped.
     * @return stripped right part
     */
    public final String getShortRule() {
        return shortRule;
    }
}
