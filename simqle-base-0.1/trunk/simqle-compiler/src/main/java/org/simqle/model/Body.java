/*
* Copyright Alexander Izyurov 2010
*/
package org.simqle.model;

import org.simqle.parser.ParseException;
import org.simqle.parser.SimpleNode;
import org.simqle.parser.SyntaxTree;
import org.simqle.processor.GrammarException;
import org.simqle.util.Assert;
import org.simqle.util.Utils;

import java.util.*;

/**
 * <br/>13.11.2011
 *
 * @author Alexander Izyurov
 */
public class Body {

    private final Map<String, MethodDefinition> methods = new TreeMap<String, MethodDefinition>();
    private final List<String> otherDeclarations = new ArrayList<String>();

    public static Body parseClassBody(String source) {
        try {
            final SimpleNode simpleNode = Utils.createParser(source).ClassBody();
            return new Body(new SyntaxTree(simpleNode, source));
        } catch (ParseException e) {
            throw new RuntimeException("Internal error", e);
        } catch (GrammarException e) {
            throw new RuntimeException("Internal error", e);
        }

    }

    public Body(SyntaxTree node) throws GrammarException {
        Assert.assertOneOf(new GrammarException("Unexpected type: "+node.getType(), node), node.getType(), "InterfaceBody", "ClassBody");

        // suppose it is InterfaceBody
        {
            final List<SyntaxTree> members = node.find("InterfaceMemberDeclaration");
            members.addAll(node.find("ClassBodyDeclaration"));
            for (SyntaxTree member: members) {
                final SyntaxTree child = member.getChildren().get(0);
                String type = child.getType();
                if (type.equals("AbstractMethodDeclaration") ||
                        type.equals("MethodDeclaration")) {
                    MethodDefinition methodDefinition = new MethodDefinition(child);
                    try {
                        addMethod(methodDefinition);
                    } catch (ModelException e) {
                        throw new GrammarException(e.getMessage(), child);
                    }
                } else {
                    // just copy to other otherDeclarations
                    otherDeclarations.add(child.getImage());
                }
            }
        }
    }

    public void addMethod(MethodDefinition methodDefinition) throws ModelException {
        if (null != methods.put(methodDefinition.getSignature(), methodDefinition)) {
            throw new ModelException("Duplicate method: "+methodDefinition.getSignature());
        }
    }

    public boolean hasMethod(String methodName) {
        return methods.containsKey(methodName);
    }

    public Map<String, MethodDefinition> getMethods() {
        return new HashMap<String, MethodDefinition>(methods);
    }

    /**
     * Finds method by signature
     * @param signature
     * @return null if not found
     */
    public MethodDefinition getMethod(String signature) {
        return methods.get(signature);
    }

    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append(" {").append(Utils.LINE_BREAK);
        // we are not expecting inner classes (which should go after methods by convention
        // so we are putting everything but methods before methods
        for (String otherDeclaration: otherDeclarations) {
            builder.append(otherDeclaration).append(Utils.LINE_BREAK);
        }
        builder.append("}").append(Utils.LINE_BREAK);
        for (MethodDefinition method: methods.values()) {
            builder.append(method);
            builder.append(Utils.LINE_BREAK);
        }
        builder.append("}");
        builder.append(Utils.LINE_BREAK);

        return builder.toString();
    }

}
