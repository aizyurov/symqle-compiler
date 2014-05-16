/*
   Copyright 2011-2014 Alexander Izyurov

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.package org.symqle.common;
*/

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
