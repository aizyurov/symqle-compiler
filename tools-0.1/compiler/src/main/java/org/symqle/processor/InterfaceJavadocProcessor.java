package org.symqle.processor;

import org.symqle.model.InterfaceDefinition;
import org.symqle.model.Model;
import org.symqle.model.ModelException;
import org.symqle.util.Utils;

import java.util.List;

import static org.symqle.util.Utils.LINE_BREAK;

/**
 * TODO move to ProductionProcessor
 * @author lvovich
 */
public class InterfaceJavadocProcessor extends ModelProcessor {

    @Override
    protected Processor predecessor() {
        return new ClassEnhancer();
    }

    @Override
    protected void process(final Model model) throws ModelException {
        for (InterfaceDefinition def : model.getAllInterfaces()) {
            final String name = def.getType().getSimpleName();
            final List<String> rules = model.getRules(name);
            if (rules != null) {
                final String javadoc = Utils.format(rules,
                        "/**" + LINE_BREAK + " *<pre> " + LINE_BREAK + " * " + name + " ::=" + LINE_BREAK + " *          ",
                        LINE_BREAK + " *        | ",
                        LINE_BREAK + " *</pre>" + LINE_BREAK + " */" + LINE_BREAK);
                def.replaceComment(javadoc);
            }
        }
    }
}
