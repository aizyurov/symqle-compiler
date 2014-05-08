package org.symqle.processor;

import org.symqle.model.InterfaceDefinition;
import org.symqle.model.Model;
import org.symqle.model.ModelException;
import org.symqle.model.TypeParameters;
import org.symqle.util.Utils;

import java.util.List;

import static org.symqle.util.Utils.LINE_BREAK;

/**
 * Adds generated Javadoc to interfaces.
 * @author lvovich
 */
public class InterfaceJavadocProcessor extends ModelProcessor {

    @Override
    protected final Processor predecessor() {
        return new ImplementationProcessor();
    }

    @Override
    protected final void process(final Model model) throws ModelException {
        for (InterfaceDefinition def : model.getAllInterfaces()) {
            final String name = def.getType().getSimpleName();
            final List<String> rules = model.getRules(name);
            if (rules != null) {
                final StringBuilder javadocBuilder = new StringBuilder();
                javadocBuilder.append("/**").append(LINE_BREAK)
                        .append(" * Represents a ").append(name).append(" symbol of SQL grammar.");
                javadocBuilder.append(LINE_BREAK);
                javadocBuilder.append(Utils.format(rules,
                        " *<pre>" + LINE_BREAK + " * " + name + " ::=" + LINE_BREAK + " *          ",
                        LINE_BREAK + " *        | ",
                        LINE_BREAK + " *</pre>" + LINE_BREAK));
                final TypeParameters typeParameters = def.getTypeParameters();
                if (typeParameters.size() == 1) {
                    javadocBuilder.append("* @param ")
                            .append(typeParameters)
                            .append(" associated Java type").append(LINE_BREAK);
                }
                javadocBuilder.append(" */").append(LINE_BREAK);
                def.replaceComment(javadocBuilder.toString());
            }
        }
    }
}
