/*
* Copyright Alexander Izyurov 2010
*/
package org.simqle.model;

import org.simqle.parser.ParseException;
import org.simqle.parser.SimpleNode;
import org.simqle.parser.SyntaxTree;
import org.simqle.processor.GrammarException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.simqle.model.Utils.convertChildren;

/**
 * <br/>13.11.2011
 *
 * @author Alexander Izyurov
 */
public class FieldDeclaration {

    private final String image;

    private final Set<String> otherModifiers;

    private final String accessModifier;

    private final List<Annotation> annotations;

    private final Type type;

    private final List<VariableDeclarator> declarators;


    public static FieldDeclaration parse(String source) {
        try {
            final SimpleNode node = Utils.createParser(source).FieldDeclaration();
            return new FieldDeclaration(new SyntaxTree(node, source));
        } catch (ParseException e) {
            throw new RuntimeException("Internal error", e);
        } catch (GrammarException e) {
            throw new RuntimeException("Internal error", e);
        }
    }

    public FieldDeclaration(SyntaxTree node) throws GrammarException {
        if (!node.getType().equals("FieldDeclaration")) {
            throw new IllegalArgumentException("Illegal argument: "+node);
        }
        image = node.getImage();
        type = convertChildren(node, "Type", Type.class).get(0);
        declarators = convertChildren(node, "VariableDeclarator", VariableDeclarator.class);
        annotations = convertChildren(node, "FieldModifiers.Annotation", Annotation.class);
        final List<SyntaxTree> modifiers = node.find("FieldModifiers.FieldModifier");
        accessModifier = Utils.getAccessModifier(modifiers);
        otherModifiers = Utils.getNonAccessModifiers(modifiers);
    }

    public String getImage() {
        return image;
    }

    public List<String> getOtherModifiers() {
        return new ArrayList<String>(otherModifiers);
    }

    public String getAccessModifier() {
        return accessModifier;
    }

    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public Type getType() {
        return type;
    }

    public List<VariableDeclarator> getDeclarators() {
        return declarators;
    }
}
