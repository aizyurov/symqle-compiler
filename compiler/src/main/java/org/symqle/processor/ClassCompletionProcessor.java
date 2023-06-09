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

import org.symqle.model.*;
import org.symqle.util.Log;
import org.symqle.util.Utils;

import java.util.HashSet;
import java.util.Set;

import static org.symqle.util.Utils.LINE_BREAK;

/**
 * Should be called after classes are loaded to Model, before class enhancement.
 * At this point the class implements only its primary interface(s) explicitly declared in
 * implements clause in SDL file.
 * It adds generated javadoc comment, which lists abstract methods to implement in derived classes;
 * generated adapt() method; adds required import lines from primary interfaces
 * @author lvovich
 */
public class ClassCompletionProcessor extends ModelProcessor {

    @Override
    protected final void process(final Model model) throws ModelException {
        for (ClassDefinition classDef: model.getAllClasses()) {
            finalizeClass(model, classDef);
        }

    }

    @Override
    protected final Processor predecessor() {
        return new ClassDeclarationProcessor();
    }

    private void finalizeClass(final Model model, final ClassDefinition classDefinition) throws ModelException {
        StringBuilder javadocBuilder = new StringBuilder();
        javadocBuilder.append("/**").append(LINE_BREAK);
        javadocBuilder.append(" * Sql building block.").append(LINE_BREAK);
        javadocBuilder.append(" * Subclasses must implement:").append(LINE_BREAK);
        javadocBuilder.append(" *<ul>").append(LINE_BREAK);
        final Set<String> abstractMethodsSignatures = new HashSet<String>();
        final Set<MethodDefinition> abstractMethods = new HashSet<MethodDefinition>();
        for (final MethodDefinition method: classDefinition.getAllMethods(model)) {
            if (method.getOtherModifiers().contains("volatile") && method.getOtherModifiers().contains("abstract")) {
                method.declareAbstract("public");
                javadocBuilder.append(" * <li>{@link #").append(method.getName());
                javadocBuilder.append(
                        Utils.format(method.getFormalParameters(), "(", ", ", ")",
                        new F<FormalParameter, String, RuntimeException>() {
                    @Override
                    public String apply(final FormalParameter formalParameter) {
                        return formalParameter.getType().getSimpleName();
                    }
                }));
                javadocBuilder.append("}</li>").append(LINE_BREAK);
                abstractMethodsSignatures.add(method.signature());
                abstractMethods.add(method);
            }
        }
        javadocBuilder.append(" *</ul>").append(LINE_BREAK);
        final TypeParameters typeParameters = classDefinition.getTypeParameters();
        // it typeParameters.size() > 1, need manual javadoc - actually we have no such classes.
        if (typeParameters.size() == 1) {
            javadocBuilder.append(" * @param ")
                    .append(typeParameters.toString())
                    .append(" the type of associated Java objects");
        }
        javadocBuilder.append(" */").append(LINE_BREAK);
        if (!abstractMethodsSignatures.isEmpty()) {
            classDefinition.replaceComment(javadocBuilder.toString());
        }
        for (Type type : classDefinition.getImplementedInterfaces()) {
            classDefinition.addImportLines(model.getInterface(type).getImportLines());
        }


        if (!abstractMethodsSignatures.isEmpty()) {
            for (Type ancestor: classDefinition.getAllAncestors(model)) {
                Log.debug("Trying to adapt to " + classDefinition.getName() + " " + ancestor.getSimpleName());
                final AbstractTypeDefinition ancestorClass = model.getAbstractType(ancestor.getSimpleName());
                if (ancestorClass.getClass().equals(InterfaceDefinition.class)) {
                    final Set<String> ancestorMethodsSignatures = new HashSet<String>();
                    for (MethodDefinition method: ancestorClass.getAllMethods(model)) {
                        ancestorMethodsSignatures.add(method.signature());
                    }
                    if (ancestorMethodsSignatures.containsAll(abstractMethodsSignatures)) {
                        // can implement by delegation
                        // first check that the method is not already implemented
                        boolean alreadyHaveAdaptMethod = false;
                        for  (MethodDefinition existingMethod : classDefinition.getAllMethods(model)) {
                            if (existingMethod.getName().equals("adapt")) {
                                alreadyHaveAdaptMethod = true;
                                break;
                            }
                        }
                        if (alreadyHaveAdaptMethod) {
                            // adapt() already implemented
                            continue;
                        }
                        StringBuilder adaptBuilder = new StringBuilder();
                        adaptBuilder.append("    /**").append(LINE_BREAK)
                                .append("     * Wraps a ").append(ancestor.getSimpleName())
                                .append(" creating new ").append(classDefinition.getType())
                                .append(".").append(LINE_BREAK)
                                .append("     * @param adaptee the object to adapt").append(LINE_BREAK);
                        if (typeParameters.size() == 1) {
                                adaptBuilder.append("     * @param ")
                                        .append(typeParameters)
                                        .append(" adaptee type argument")
                                        .append(LINE_BREAK);
                        }
                        adaptBuilder.append("     * @return new instance of AbstractFactor").append(LINE_BREAK)
                                .append("     */").append(LINE_BREAK);
                        adaptBuilder.append("    public static ")
                                .append(typeParameters)
                                .append(" ")
                                .append(classDefinition.getType())
                                .append(" adapt(final ")
                                .append(ancestor).append(" adaptee) {").append(LINE_BREAK)
                                .append("        return new ").append(classDefinition.getType()).append("() {")
                                .append(LINE_BREAK);
                        for (MethodDefinition method: abstractMethods) {
                            adaptBuilder.append("            public ").append(method.getTypeParameters()).append(" ")
                                    .append(method.getResultType()).append(" ").append(method.getName()).append("(")
                                    .append(Utils.format(method.getFormalParameters(), "", ", ", "",
                                            new F<FormalParameter, String, RuntimeException>() {
                                        @Override
                                        public String apply(final FormalParameter formalParameter) {
                                            return formalParameter.getModifiers().contains("final")
                                                    ? formalParameter.toString()
                                                    : "final " + formalParameter;
                                        }
                                    }))
                                    .append(") {").append(LINE_BREAK)
                                    .append("                ")
                                    .append(method.getResultType().equals(Type.VOID) ? "" : "return ")
                                    .append(method.delegationInvocation("adaptee"))
                                    .append(";").append(LINE_BREAK)
                                    .append("            }").append(LINE_BREAK);
                        }
                        adaptBuilder.append("        };").append(LINE_BREAK)
                                .append("    }");
                        final MethodDefinition adaptMethod =
                                MethodDefinition.parse(adaptBuilder.toString(), classDefinition);
                        classDefinition.addMethod(adaptMethod);
                    } else {
                        Set<String> remaining = new HashSet<String>(abstractMethodsSignatures);
                        remaining.removeAll(ancestorMethodsSignatures);
                        Log.debug(classDefinition.getName() + ": cannot adapt("
                                + ancestor.getSimpleName() + "), cannot delegate " + remaining);
                    }
                }
            }
        } else {
            Log.debug("No abstract methods in " + classDefinition.getName());
        }
    }

}
