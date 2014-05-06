package org.symqle.model;

import org.symqle.parser.ParseException;
import org.symqle.parser.SimpleNode;
import org.symqle.parser.SyntaxTree;
import org.symqle.processor.GrammarException;
import org.symqle.util.Assert;
import org.symqle.util.Utils;

import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: aizyurov
 * Date: 01.01.2013
 * Time: 22:17:46
 * To change this template use File | Settings | File Templates.
 */
public class FieldDeclaration {
    public final String accessModifier;
    public final Set<String> otherModifiers;
    private final Type type;
    private final List<VariableDeclarator> variables;
    private final String comment;

    public static FieldDeclaration parse(String source) {
        try {
            final SimpleNode simpleNode = Utils.createParser(source).FieldDeclaration();
            SyntaxTree syntaxTree = new SyntaxTree(simpleNode, source);
            return new FieldDeclaration(syntaxTree);
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        } catch (GrammarException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public FieldDeclaration(SyntaxTree node) throws GrammarException {
        Assert.assertOneOf(new GrammarException("Unexpected type: "+node.getType(), node), node.getType(), "FieldDeclaration");
        List<SyntaxTree> modifierNodes = node.find("FieldModifiers");
        this.accessModifier = Utils.getAccessModifier(modifierNodes);
        this.otherModifiers = Utils.getNonAccessModifiers(modifierNodes);
        // mandatory and only one
        this.type = node.find("Type", Type.CONSTRUCT).get(0);
        this.variables = node.find("VariableDeclarator", VariableDeclarator.CONSTRUCT);
        this.comment = node.getComments();
    }

    public String toString() {
        return comment + accessModifier + " " + Utils.format(otherModifiers, "", " ", " ")
                + type + " " +Utils.format(variables, "", ", ", ";");
    }

}
