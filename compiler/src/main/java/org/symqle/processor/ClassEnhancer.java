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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Final step of class setup. For each class find all "ambiguous" Symqle static methods, which cna be converted
 * to instance methods of this class. Choose the best. First, use shortest path of implicit conversions. If there are
 * still multiple candidates, choose one which returns "more rich" object - having more methods.
 * Add the chosen method to the class, implement by delegation to Symqle
 * (using cast of {@code this} to proper interface to resolve ambiguity).
 * @author lvovich
 */
public class ClassEnhancer extends ModelProcessor {

    @Override
    protected final Processor predecessor() {
        return new InterfaceEnhancer();
    }

    @Override
    protected final void process(final Model model) throws ModelException {
        final Map<MethodDefinition, String> commentReplacements = new HashMap<MethodDefinition, String>();
        for (ClassDefinition classDef: model.getSortedClasses()) {
            enhanceClass(classDef, model, commentReplacements);
        }
        for (final Map.Entry<MethodDefinition, String> entry : commentReplacements.entrySet()) {
            entry.getKey().replaceComment(entry.getValue());
        }
    }

    private void enhanceClass(final ClassDefinition classDef,
                              final Model model,
                              final Map<MethodDefinition, String> commentReplacements) throws ModelException {
        Map<String, List<MethodDefinition>> ambiguousMethodsByReducedSignature = new HashMap<>();
        for (MethodDefinition method: model.getExplicitSymqleMethods()) {
            if (!model.isUnambiguous(method)
                    && !method.getAccessModifier().equals("private")
                    && !method.getAccessModifier().equals("protected")) {
                final String key = model.reducedSignature(method);
                List<MethodDefinition> list = ambiguousMethodsByReducedSignature.get(key);
                if (list == null) {
                    list = new ArrayList<MethodDefinition>();
                    ambiguousMethodsByReducedSignature.put(key, list);
                }
                list.add(method);
            }
        }

        for (List<MethodDefinition> list: ambiguousMethodsByReducedSignature.values()) {
            List<MethodDefinition> acceptableMethods = new ArrayList<MethodDefinition>();
            for (MethodDefinition method : list) {
                if (getMapping(classDef, method, model) != null) {
                    acceptableMethods.add(method);
                }
            }

            if (acceptableMethods.size() > 1) {
                // filter by distance first
                int minDistance = Integer.MAX_VALUE;
                for (MethodDefinition method : acceptableMethods) {
                    final Map<String, TypeArgument> mapping = getMapping(classDef, method, model);
                    final int distance = classDef.distance(
                            method.getFormalParameters().get(0).getType().replaceParams(mapping), model);
                    if (distance < minDistance) {
                        minDistance = distance;
                    }
                }
                for (Iterator<MethodDefinition> iterator = acceptableMethods.iterator(); iterator.hasNext();) {
                    final MethodDefinition method = iterator.next();
                    final Map<String, TypeArgument> mapping = getMapping(classDef, method, model);
                    final Type actualArg0Type = method.getFormalParameters().get(0).getType().replaceParams(mapping);
                    if (classDef.distance(actualArg0Type, model) != minDistance) {
                        iterator.remove();
                    }
                }
            }

            if (acceptableMethods.size() > 1) {
                // filter by richness first
                int maxRichness = -1;
                for (MethodDefinition method : acceptableMethods) {
                    final int richness = richness(method, model);
                    if (richness > maxRichness) {
                        maxRichness = richness;
                    }
                }
                for (Iterator<MethodDefinition> iterator = acceptableMethods.iterator(); iterator.hasNext();) {
                    if (richness(iterator.next(), model) != maxRichness) {
                        iterator.remove();
                    }
                }
            }


            if (acceptableMethods.size() > 1) {
                //give up
                throw new ModelException(classDef.getName() + " cannot choose one of "
                        + Utils.map(acceptableMethods, new F<MethodDefinition, Object, RuntimeException>() {
                                        @Override
                                        public Object apply(final MethodDefinition methodDefinition) {
                                            return methodDefinition.getResultType() + " "
                                                    + methodDefinition.signature();
                                        }
                }));
            } else if (acceptableMethods.size() == 1) {
                final MethodDefinition accepted = acceptableMethods.get(0);
                final MethodDefinition myMethod = createMyMethod(classDef, accepted, model);
                classDef.addMethod(myMethod);
                // comment moved to class and is scheduled for remove from Symqle
                commentReplacements.put(accepted,  "    /**" + Utils.LINE_BREAK
                        + "     *  see e.g. {@link " + classDef.getName() + "#" + myMethod.signature() + "}"
                        + Utils.LINE_BREAK
                        + "     */" + Utils.LINE_BREAK);
            }
            // else continue for other candidates

        }


        // finally, make sure that imports from ancestors go to this class
        classDef.ensureRequiredImports(model);
        // make class abstract if necessary

    }

    private int richness(final MethodDefinition method, final Model model) throws ModelException {
        final Type resultType = method.getResultType();
        final AbstractTypeDefinition abstractType;
        abstractType = model.getAbstractType(resultType.getSimpleName());
        if (abstractType == null) {
            Log.debug("Non-symqle result type in " + method.getResultType() + " " + method.signature());
            return -1;
        }
        return abstractType.getAllMethods(model).size();
    }

    private Map<String, TypeArgument> getMapping(final ClassDefinition classDef,
                                                 final MethodDefinition method,
                                                 final Model model) throws ModelException {
        for (Type type: classDef.getAllAncestors(model)) {
            final Type arg0Type = method.getFormalParameters().get(0).getType();
            if (!arg0Type.getSimpleName().equals(type.getSimpleName())) {
                continue;
            }
            final Map<String, TypeArgument> replacements =
                    method.getTypeParameters().inferTypeArguments(arg0Type, type);
            final Type requiredType = arg0Type.replaceParams(replacements);
            if (requiredType.equals(type)) {
                return replacements;
            }
        }
        return null;
    }

    private String findFreeName(final Map<String, TypeArgument> mapping, final String candidate) {
        if (mapping.get(candidate) == null) {
            return candidate;
        }
        for (char c = 'A'; c <= 'Z'; c++) {
            final String name = c + "";
            if (mapping.get(name) == null) {
                return name;
            }
        }
        for (int i = 0; i < 100; i++) {
            for (char c = 'A'; c <= 'Z'; c++) {
                final String name = c + "" + i;
                if (mapping.get(name) == null) {
                    return name;
                }
            }
        }
        throw new IllegalStateException("Shit happens");
    }

    private MethodDefinition createMyMethod(final ClassDefinition classDef,
                                            final MethodDefinition method,
                                            final Model model) throws ModelException {
        final Map<String, TypeArgument> mapping = getMapping(classDef, method, model);

        final List<TypeParameter> myTypeParameterList = new ArrayList<TypeParameter>();
        // skip parameters, which are in mapping: they are inferred
        for (TypeParameter typeParameter: method.getTypeParameters().list()) {
            final String typeParameterName = typeParameter.getName();
            if (mapping.get(typeParameterName) == null) {
                final String newParameterName = findFreeName(mapping, typeParameterName);
                TypeParameter renamedTypeParameter = typeParameter.rename(newParameterName);
                mapping.put(typeParameterName, new TypeArgument(false, null, new Type(newParameterName)));
                myTypeParameterList.add(renamedTypeParameter);
            }
        }
        TypeParameters myTypeParameters = new TypeParameters(myTypeParameterList);
        List<FormalParameter> myFormalParameters = new ArrayList<FormalParameter>();
        final List<FormalParameter> symqleFormalParameters = method.getFormalParameters();
        for (int i = 1; i < symqleFormalParameters.size(); i++) {
            final FormalParameter symqleFormalParameter = symqleFormalParameters.get(i);
            myFormalParameters.add(symqleFormalParameter.replaceParams(mapping));
        }
        Set<String> myModifiers = new HashSet<String>(method.getOtherModifiers());
        myModifiers.remove("abstract");
        myModifiers.remove("static");
        myModifiers.add("final");
        final StringBuilder builder = new StringBuilder();
        builder.append(method.getComment());
        builder.append("    ");
        // always public method!
        builder.append("public")
                .append(" ")
                .append(Utils.format(myModifiers, "", " ", " "))
                .append(myTypeParameters)
                .append(method.getResultType().replaceParams(mapping))
                .append(" ")
                .append(method.getName())
                .append("(")
                .append(Utils.format(myFormalParameters, "", ", ", ""))
                .append(")")
                .append(Utils.format(method.getThrownExceptions(), " throws ", ", ", ""))
                .append(" {")
                .append(Utils.LINE_BREAK)
                .append("        return Symqle.")
                .append(method.getName()).append("")
                // always cast explicitly to reqiured interface for potentially ambiguous methods
                // the cast may be redundant sometimes...
                .append("((").append(method.getFormalParameters().get(0).getType().replaceParams(mapping)).append(") ")
                .append("this").append(Utils.format(myFormalParameters, ", ", ", ", "", FormalParameter.NAME))
                .append(");").append(Utils.LINE_BREAK).append("    }");
        final String body = builder.toString();
        final MethodDefinition myMethod = MethodDefinition.parse(body, classDef);
        myMethod.setSourceRef(method.getSourceRef());
        return myMethod;
    }


}
