package org.symqle.processor;

import org.symqle.model.*;
import org.symqle.util.Utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: aizyurov
 * Date: 02.12.2012
 * Time: 19:53:08
 * To change this template use File | Settings | File Templates.
 */
public class InheritanceProcessor extends ModelProcessor {

    @Override
    protected Processor predecessor() {
        return new ImplicitConversionProcessor();
    }

    /**
     * Add interfaces, reachable via a chain of implicit conversions,
     * to classes. Adds implementation of methods declared by the interfaces
     * based on delegation to a new object constructed by implicit convertion.
     * @param model should have all interfaces and classes before the call to this method
     * @throws ModelException
     */
    @Override
    protected void process(Model model) throws ModelException {
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
            if (methodDef.getOtherModifiers().contains("abstract") && methodDef.getOtherModifiers().contains("volatile")) {
                throw new ModelException("Not implemented in " + classDef + ": " + methodDef.declaration());
            }
        }

        final Set<Type> unexplored = new HashSet<Type>();

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
                if (!allAncestors.contains(entry.getValue())) {
                    classDef.addImplementedInterface(entry.getValue());
                    final Set<Type> newAncestors = classDef.getAllAncestors(model);
                    newAncestors.removeAll(allAncestors);
                    unexplored.addAll(newAncestors);
                    for (Type newAncestor: newAncestors) {
                        classDef.addPath(newAncestor, type);
                    }
                    implementNewMethods(classDef, entry.getKey(), model);
                }
            }
            unexplored.remove(type);
        }
    }

    /**
     * Avaliable conversions. Key is conversion method, value is returned type.
     * @param type the type of conversion argument
     * @param model
     * @return
     */
    public static Map<MethodDefinition, Type> findAvailableConversions(Type type, Model model) throws ModelException {
        final Map<MethodDefinition, Type> map = new HashMap<MethodDefinition, Type>();
        for (MethodDefinition conversion : model.getImplicitSymqleMethods()) {
            final Type arg0Type = conversion.getFormalParameters().get(0).getType();
            if (arg0Type.getSimpleName().equals(type.getSimpleName())) {
                final Map<String, TypeArgument> replacementMap = conversion.getTypeParameters().inferTypeArguments(arg0Type, type);
                final Type resultType = conversion.getResultType().replaceParams(replacementMap);
                final Type argType = arg0Type.replaceParams(replacementMap);
                if (argType.equals(type)) {
                    map.put(conversion, resultType);
                }
            }
        }
        return map;
    }

    private void implementNewMethods(ClassDefinition classDef, MethodDefinition conversionMethod, Model model) throws ModelException {
        for (MethodDefinition methodToImplement: classDef.getAllMethods(model)) {
            if (methodToImplement.getOtherModifiers().contains("volatile")
                    && methodToImplement.getOtherModifiers().contains("abstract")
                    ) {
                    methodToImplement.implement("public",
                            " {" + Utils.LINE_BREAK +
                            "                " +
                            (methodToImplement.getResultType()==Type.VOID ? "" : "return ") +
                            methodToImplement.delegationInvocation(
                                    conversionMethod.invoke("Symqle", Collections.singletonList("this"))) +
                            ";" + Utils.LINE_BREAK+"            "+"}"+Utils.LINE_BREAK,
                            true, true);
            }
        }

    }

}
