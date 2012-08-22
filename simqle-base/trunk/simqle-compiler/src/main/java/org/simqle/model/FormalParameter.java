/*
* Copyright Alexander Izyurov 2010
*/
package org.simqle.model;

import org.simqle.parser.SyntaxTree;
import org.simqle.processor.GrammarException;
import org.simqle.util.Assert;
import org.simqle.util.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.simqle.util.Utils.convertChildren;


/**
 * <br/>13.11.2011
 *
 * @author Alexander Izyurov
 */
public class FormalParameter {

    private final String image;

    private final Type type;

    private final String name;

    private final List<String> modifiers;

    private final boolean ellipsis;
    

    public FormalParameter(SyntaxTree node) throws GrammarException {
        final Type rawType = convertChildren(node, "Type", Type.class).get(0);
        Assert.assertOneOf(node.getType(), "FormalParameter", "FormalParameterWithEllipsis");
        if (node.getType().equals("FormalParameter")) {
            type = rawType;
            ellipsis = false;
        } else  {
            // must be FormalParameterWithEllipsis due to asserttion above
            type = rawType.arrayOf();
            ellipsis = true;
        }
        image = node.getImage();
        name = node.find("VariableDeclaratorId.Identifier").get(0).getValue();
        modifiers = Utils.bodies(node.find("VariableModifiers.VariableModifier"));
    }

    public FormalParameter(Type type, String name) {
        this(type, name, Collections.<String>emptyList());
    }

    public FormalParameter(final Type type, final String name, final List<String> modifiers) {
        this.type = type;
        this.name = name;
        this.modifiers = new ArrayList<String>(modifiers);
        this.image = (modifiers.isEmpty() ? "" : Utils.concat(modifiers, " ")+" ")+type.getImage()+" "+name;
        this.ellipsis = false;
    }

    public String getImage() {
        return image;
    }

    public Type getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getImage();
    }

    public List<String> getModifiers() {
        return modifiers;
    }

    public boolean isEllipsis() {
        return ellipsis;
    }
}
