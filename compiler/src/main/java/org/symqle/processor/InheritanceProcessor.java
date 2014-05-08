package org.symqle.processor;

import org.symqle.model.*;
import org.symqle.util.Log;
import org.symqle.util.Utils;

import java.util.*;

/**
 * Processes "virtual inheritance". Adds to each class new implemented interfaces - those to which this class
 * can be converted via a chain of implicit conversions.
 * Adds implementation of methods declared by the interfaces
 * based on delegation to a new object constructed by implicit convertion.
 */
public class InheritanceProcessor extends ModelProcessor {

    @Override
    protected final Processor predecessor() {
        return new SymqleMethodProcessor();
    }

    @Override
    protected final void process(final Model model) throws ModelException {
        for (ClassDefinition classDef: model.getSortedClasses()) {
            addInterfaces(classDef, model);
            classDef.makeAbstractIfNeeded(model);
            classDef.ensureRequiredImports(model);
        }
    }


    private void addInterfaces(final ClassDefinition classDef,
                               final Model model) throws ModelException {
        // verify that all interface methods are implemented by the time if this call
        for (MethodDefinition methodDef : classDef.getAllMethods(model)) {
            if (methodDef.getOtherModifiers().contains("abstract")
                    && methodDef.getOtherModifiers().contains("volatile")) {
                throw new ModelException("Not implemented in " + classDef + ": " + methodDef.declaration());
            }
        }

        final Set<Type> unexplored = new LinkedHashSet<Type>();

        for (Type type: classDef.getAllAncestors(model)) {
            final AbstractTypeDefinition ancestorClass = model.getAbstractType(type.getSimpleName());
            if (ancestorClass.getClass().equals(InterfaceDefinition.class)) {
                InterfaceDefinition interfaceDefinition = (InterfaceDefinition) ancestorClass;
                if (interfaceDefinition.getArchetypeMethod() != null) {
                    unexplored.add(type);
                }
            }
        }

        while (!unexplored.isEmpty()) {
            final Set<Type> allAncestors = new HashSet<Type>(classDef.getAllAncestors(model));
            final Type type = unexplored.iterator().next();
            // find suitable implicit conversions
            final Map<MethodDefinition, Type> availableConversions = findAvailableConversions(type, model);
            for (Map.Entry<MethodDefinition, Type> entry : availableConversions.entrySet()) {
                final Type implementedType = entry.getValue();
                if (!allAncestors.contains(implementedType)) {
                    classDef.addImplementedInterface(implementedType);
                    Log.debug(classDef.getName() + " now directly implements " + implementedType.getSimpleName());
                    final Set<Type> newAncestors = classDef.getAllAncestors(model);
                    newAncestors.removeAll(allAncestors);
                    unexplored.addAll(newAncestors);
                    for (Type newAncestor: newAncestors) {
                        classDef.addPath(newAncestor, type);
                        Log.debug(classDef.getName() + " now implements "
                                + newAncestor.getSimpleName() + " via " + type.getSimpleName()
                                + " using " + entry.getKey().getName() + entry.getKey().signature());
                    }
                    classDef.removeRedundantInterfaces(model);
                    implementNewMethods(classDef, entry.getKey(), model);
                }
            }
            unexplored.remove(type);
        }
    }

    /**
     * Avaliable conversions. Key is conversion method, value is returned type.
     * @param type the type of conversion argument
     * @param model should contain all classes and interfaces
     * @throws ModelException wrong model
     * @return map of available conversions
     */
    public static Map<MethodDefinition, Type> findAvailableConversions(final Type type,
                                                                             final Model model) throws ModelException {
        final Map<MethodDefinition, Type> map = new HashMap<MethodDefinition, Type>();
        for (ImplicitConversion conversion : model.getConversions()) {
            final Type fromType = conversion.getFrom();
            if (fromType.getSimpleName().equals(type.getSimpleName())) {
                final Map<String, TypeArgument> replacementMap =
                        conversion.getTypeParameters().inferTypeArguments(fromType, type);
                final Type resultType = conversion.getTo().replaceParams(replacementMap);
                final Type argType = fromType.replaceParams(replacementMap);
                if (argType.equals(type)) {
                    map.put(conversion.getConversionMethod(), resultType);
                }
            }
        }
        return map;
    }

    private void implementNewMethods(final ClassDefinition classDef,
                                     final MethodDefinition conversionMethod,
                                     final Model model) throws ModelException {
        for (MethodDefinition methodToImplement: classDef.getAllMethods(model)) {
            if (methodToImplement.getOtherModifiers().contains("volatile")
                    && methodToImplement.getOtherModifiers().contains("abstract")
                    ) {
                Log.debug(classDef.getName() + " implementing new method " + methodToImplement.signature());
                final String invocation = conversionMethod.invoke("Symqle", Collections.singletonList("this"));
                final String delegationCall =
                        methodToImplement.delegationInvocation(invocation + Utils.LINE_BREAK + "            ");

                final String newBody = " {" + Utils.LINE_BREAK
                        + "        " + (methodToImplement.getResultType() == Type.VOID ? "" : "return ")
                        + delegationCall + ";" + Utils.LINE_BREAK + "    " + "}" + Utils.LINE_BREAK;

                methodToImplement.implement("public", newBody, true, true);
            }
        }

    }

}
