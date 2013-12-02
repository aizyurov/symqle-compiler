package org.symqle.processor;

import org.symqle.model.ClassDefinition;
import org.symqle.model.F;
import org.symqle.model.FormalParameter;
import org.symqle.model.MethodDefinition;
import org.symqle.model.Model;
import org.symqle.model.ModelException;
import org.symqle.util.Utils;

import static org.symqle.util.Utils.LINE_BREAK;

/**
 * @author lvovich
 */
public class AbstractMethodsProcessor extends ModelProcessor {

    @Override
    public void process(final Model model) throws ModelException {
        for (ClassDefinition classDefinition: model.getAllClasses()) {

            StringBuilder javadocBuilder = new StringBuilder();
            javadocBuilder.append("/**").append(LINE_BREAK);
            javadocBuilder.append(" * An abstract class, implementing almost all methods of its interfaces." ).append(LINE_BREAK);
            javadocBuilder.append(" * Subclasses must implement:").append(LINE_BREAK);
            javadocBuilder.append(" *<ul>").append(LINE_BREAK);
            boolean hasAbstractMethods = false;
            for (final MethodDefinition method: classDefinition.getAllMethods(model)) {
                if (method.getOtherModifiers().contains("transient") && method.getOtherModifiers().contains("abstract")) {
                    method.declareAbstract("public");
                    javadocBuilder.append(" * <li>{@link #").append(method.getName());
                    javadocBuilder.append(Utils.format(method.getFormalParameters(), "(", ", ", ")", new F<FormalParameter, String, RuntimeException>() {
                        @Override
                        public String apply(final FormalParameter formalParameter) {
                            return formalParameter.getType().getSimpleName();
                        }
                    }));
                    javadocBuilder.append("}</li>").append(LINE_BREAK);
                    hasAbstractMethods = true;
                }
            }
            javadocBuilder.append(" *</ul>").append(LINE_BREAK);
            javadocBuilder.append(" */").append(LINE_BREAK);
            if (hasAbstractMethods) {
                classDefinition.replaceComment(javadocBuilder.toString());
            }
        }
    }


}
