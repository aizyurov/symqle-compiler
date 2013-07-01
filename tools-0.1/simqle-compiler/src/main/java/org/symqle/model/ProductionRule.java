package org.symqle.model;

import org.symqle.parser.SyntaxTree;
import org.symqle.processor.GrammarException;
import org.symqle.util.Assert;
import org.symqle.util.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: lvovich
 * Date: 26.06.12
 * Time: 17:05
 * To change this template use File | Settings | File Templates.
 */
public class ProductionRule {
    private final List<FormalParameter> formalParameters = new ArrayList<FormalParameter>();
    // this is the name of associated method
    private final String name;
    private final String syntax;
    private final List<String> elementNames;
    private final String targetTypeName;

    public ProductionRule(SyntaxTree node) throws GrammarException {
        Assert.assertOneOf(new GrammarException("Unexpected type: "+node.getType(), node), node.getType(), "ProductionRule");
        StringBuilder nameBuilder = new StringBuilder();
        StringBuilder syntaxBuilder = new StringBuilder();
        elementNames = new ArrayList<String>();
        // exactly one
        Type targetType = node.find("^.^.ClassOrInterfaceType", Type.CONSTRUCT).get(0);
        targetTypeName = targetType.getSimpleName();
        syntaxBuilder.append(targetTypeName).append(" ::=");
        nameBuilder.append(targetType.getSimpleName()).append("_is");
        final Type sqlType = new Type("Sql");
        // at most one type by syntax; no type if implicit (in this case it is targetType)
        for (SyntaxTree element: node.find("ProductionElement")) {
            final List<Type> typeList = element.find("ClassOrInterfaceType", Type.CONSTRUCT);
            final Type type = typeList.isEmpty() ? null : typeList.get(0);
            final String name = element.find("Identifier").get(0).getValue();
            final String descriptiveName = type != null ? type.getSimpleName() : name;
            final String syntaxElement = type != null ? name + ":" + descriptiveName : descriptiveName;
            nameBuilder.append("_").append(descriptiveName);
            syntaxBuilder.append(" ").append(syntaxElement);
            if (type != null) {
                formalParameters.add(
                        new FormalParameter(sqlType, name, Collections.singletonList("final"), false));
            }
            elementNames.add(name);
        }
        name = nameBuilder.toString();
        syntax = syntaxBuilder.toString();
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
                    "    * @return Sql constructed according to the rule" + Utils.LINE_BREAK +
                    "    */"  + Utils.LINE_BREAK;

    }

    public String getName() {
        return name;
    }

    public String toString() {
        return syntax;
    }

    public String asAbstractMethodDeclaration() {
        return generatedComment()+"Sql " + name
                + "(" + Utils.format(formalParameters, "", ", ", "") +")";
    }

    public String asMethodArguments() {
        return Utils.format(elementNames, "", ", ", "");
    }

    public List<FormalParameter> getFormalParameters() {
        return new ArrayList<FormalParameter>(formalParameters);
    }

}
