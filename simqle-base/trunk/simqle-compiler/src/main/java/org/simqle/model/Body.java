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

    private final Map<String, MethodDeclaration> methods = new TreeMap<String, MethodDeclaration>();
    // interface constants are not included here: they go to otherDeclarations
    private final List<FieldDeclaration> fields = new ArrayList<FieldDeclaration>();
    private final Set<String> fieldNames = new HashSet<String>();
    private final List<String> otherDeclarations = new ArrayList<String>();
    private final List<ConstructorDeclaration> constructors = new ArrayList<ConstructorDeclaration>();

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
            for (SyntaxTree member: members) {
                final SyntaxTree child = member.getChildren().get(0);
                if (child.getType().equals("AbstractMethodDeclaration")) {
                    MethodDeclaration methodDeclaration = new MethodDeclaration(child);
                    if (methods.containsKey(methodDeclaration.getName())) {
                        throw new GrammarException("Method overloading is not allowed in Simqle: "+methodDeclaration.getName(), child);
                    } else {
                        methods.put(methodDeclaration.getName(), methodDeclaration);
                    }
                } else {
                    // just copy to other otherDeclarations
                    otherDeclarations.add(child.getImage());
                }
            }
        }
        // suppose it is ClassBody
        {
            final List<SyntaxTree> members = node.find("ClassBodyDeclaration");
            for (SyntaxTree member: members) {
                final SyntaxTree child = member.getChildren().get(0);
                final String type = child.getType();
                if (type.equals("MethodDeclaration")) {
                    final MethodDeclaration methodDeclaration = new MethodDeclaration(child);
                    try {
                        addMethod(methodDeclaration);
                    } catch (ModelException e) {
                        throw new GrammarException(e.getMessage(), child);
                    }
                } else if (type.equals("FieldDeclaration")){
                    try {
                        addFieldDeclaration(new FieldDeclaration(child));
                    } catch (ModelException e) {
                        throw new GrammarException(e.getMessage(), child);
                    }
                } else if (type.equals("ConstructorDeclaration")){
                    unsafeAddConstructorDeclaration(new ConstructorDeclaration(child));
                } else {
                    // just copy to other otherDeclarations         ClassDef
                    otherDeclarations.add(child.getImage());
                }
            }
        }
    }

    public void addFieldDeclaration(FieldDeclaration fieldDeclaration) throws ModelException {
        final List<VariableDeclarator> declarators = fieldDeclaration.getDeclarators();
        for (VariableDeclarator declarator: declarators) {
            final String name = declarator.getName();
            if (fieldNames.contains(name)) {
                throw new ModelException("Duplicate field: "+name);
            } else {
                fieldNames.add(name);
            }
        }
        fields.add(fieldDeclaration);
    }

    public void addMethod(MethodDeclaration methodDeclaration) throws ModelException {
        final String signature = methodDeclaration.getSignature();
        if (methods.containsKey(signature)) {
            throw new ModelException("Duplicate method: "+signature);
        }
        methods.put(signature, methodDeclaration);
    }

    // the name of constructor is nor verified against class name (not available here)
    // leaving for Java compiler
    public void unsafeAddConstructorDeclaration(ConstructorDeclaration declaration) {
        constructors.add(declaration);
    }

    public boolean hasMethod(String methodName) {
        return methods.containsKey(methodName);
    }

    public MethodDeclaration getMethod(String signature) {
        return methods.get(signature);
    }

    public List<String> getOtherDeclarations() {
        return new ArrayList<String>(otherDeclarations);
    }

    public void addOtherDeclaration(String otherDeclaration) {
        otherDeclarations.add(otherDeclaration);
    }


    public List<MethodDeclaration> getMethods() {
        return new ArrayList<MethodDeclaration>(methods.values());
    }

    public List<FieldDeclaration> getFields() {
        return new ArrayList<FieldDeclaration>(fields);
    }

    public List<ConstructorDeclaration> getConstructors() {
        return Collections.unmodifiableList(constructors);
    }

    public void merge(Body another) throws ModelException {
        for (FieldDeclaration field: another.fields) {
            addFieldDeclaration(field);
        }
        for (MethodDeclaration method: another.methods.values()) {
            addMethod(method);
        }
        for (ConstructorDeclaration constructor: another.constructors) {
            unsafeAddConstructorDeclaration(constructor);
        }
        for (String otherDeclaration: another.otherDeclarations) {
            addOtherDeclaration(otherDeclaration);
        }
    }

    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{").append(Utils.LINE_BREAK);
        for (FieldDeclaration field: fields) {
            builder.append(field.getImage()).append(Utils.LINE_BREAK);
        }
        builder.append(Utils.LINE_BREAK);
        for (ConstructorDeclaration ctr: constructors) {
            builder.append(ctr.getImage());
        }
        builder.append(Utils.LINE_BREAK);
        for (MethodDeclaration method: methods.values()) {
            final String methodBody = method.getMethodBody();
            builder.append(method.getDeclaration()).append(" ").append(methodBody == null ? ";" : methodBody);
            builder.append(Utils.LINE_BREAK);
        }
        builder.append(Utils.LINE_BREAK);
        for (String otherDeclaration: otherDeclarations) {
            builder.append(otherDeclaration).append(Utils.LINE_BREAK);
        }
        builder.append("}").append(Utils.LINE_BREAK);
        return builder.toString();
    }

}
