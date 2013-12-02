package org.symqle.processor;

import org.symqle.model.*;
import org.symqle.util.Utils;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: aizyurov
 * Date: 02.12.2012
 * Time: 19:53:08
 * To change this template use File | Settings | File Templates.
 */
public class InheritanceProcessor extends ModelProcessor {

    /**
     * Add interfaces, reachable via a chain of implicit conversions,
     * to classes. Adds implementation of methods declared by the interfaces
     * based on delegation to a new object constructed by implicit convertion.
     * @param model should have all interfaces and classes before the call to this method
     * @throws ModelException
     */
    @Override
    public void process(Model model) throws ModelException {
        final Map<String, Map<String, MethodDefinition>> implicitConversions =
                makeImplicitConversionsMap(model);
        for (ClassDefinition classDef: model.getAllClasses()) {
            addInterfaces(classDef, implicitConversions, model);
            classDef.makeAbstractIfNeeded(model);
            classDef.ensureRequiredImports(model);
        }
    }



    private void addInterfaces(final ClassDefinition classDef,
                               final Map<String, Map<String, MethodDefinition>> implicitConversions,
                               final Model model) throws ModelException {
        // verify that all interface methods are implemented by the time if this call
        for (MethodDefinition methodDef : classDef.getAllMethods(model)) {
            if (methodDef.getOtherModifiers().contains("abstract") && methodDef.getOtherModifiers().contains("volatile")) {
                throw new ModelException("Not implemented in " + classDef + ": " + methodDef.declaration());
            }
        }

        // Names of all interfaces implemented by classDef
        final Set<Type> allInterfaces = new HashSet<Type>();
        // All interfaces reachable by N implicit conversions
        final Set<Type> nHopsReachable = new HashSet<Type>();
        // All interfaces reachable by N+1 implicit conversions
        // as map (interfaceName, last method in conversion chain)
        final Map<Type, MethodDefinition> nPlusOneHopsReachable = new HashMap<Type, MethodDefinition>();
        // holds current number of hops for the two Sets above
        int nHops = 0;
        // cycle invariant:
        // all interfaces reachable in N hops and less are implemented by the class
        // all their names are in allInterfaceNames
        // nHopsReachable contains all interfaces to which classDef can be converted in N hops
        // all methods to be implemented in classDef are either implemented or explicitly declared abstract
        //
        // initialize the variables:
        for (Type interfaceType: classDef.getImplementedInterfaces()) {
            allInterfaces.add(interfaceType);
            nHopsReachable.add(interfaceType);
        }
        // invariant is true
        while (!nHopsReachable.isEmpty()) {
            System.err.println(classDef.getName() + "[" + nHops +"]: " + Utils.format(nHopsReachable, "(", ", ", ")"));
            for (Type argType: nHopsReachable) {
                Map<String, MethodDefinition> conversionsByArg = implicitConversions.get(argType.getSimpleName());
                if (conversionsByArg==null) {
                    continue;
                }
                for (Map.Entry<String, MethodDefinition> entry: conversionsByArg.entrySet()) {
                    final String resName = entry.getKey();
                    MethodDefinition methodDef = entry.getValue();
                    final TypeParameters methodTypeParams = methodDef.getTypeParameters();
                    final Map<String, TypeArgument> paramMapping =
                            methodTypeParams.inferTypeArguments(methodDef.getFormalParameters().get(0).getType(), argType);

                    final Type resType = methodDef.getResultType().replaceParams(paramMapping);
                    System.err.println(classDef.getName() + "[" + nHops +"]: " + "adding " + resType);
                    if (!allInterfaces.contains(resType)) {
                        // this a new one reachable in N+1 hops; not reachable in N or less hops
                        if (nPlusOneHopsReachable.containsKey(resType)) {
                            System.err.println("WARN: multiple ways to reach " +
                                    resName + " from " + classDef.getName() +
                                    " last step is " +
                                    nPlusOneHopsReachable.get(resType).getName() +
                                    " or "+ methodDef.getName());
                            // do not replace: first conversion wins
                        } else if (isCompatible(resType, classDef, paramMapping, model)) {
                            nPlusOneHopsReachable.put(resType, methodDef);
                            classDef.addImplementedInterface(resType, nHops);
                            allInterfaces.add(resType);
                            System.err.println(classDef.getName() + "[" + nHops +"]: " + "added " + resType);
                            // now it is implemented but its methods may be not
                            InterfaceDefinition interfaceOwner = model.getInterface(resType);
                            classDef.addImportLines(interfaceOwner.getImportLines());
                            // methods will be added in ClassEnhancer
                            for (MethodDefinition methodToImplement: classDef.getAllMethods(model)) {
                                if (methodToImplement.getOtherModifiers().contains("volatile")
                                        && methodToImplement.getOtherModifiers().contains("abstract")
                                        ) {
                                    if (interfaceOwner.canDelegateToSymqle(methodToImplement)) {
                                        final Collection<String> params = Utils.map(methodToImplement.getFormalParameters(), FormalParameter.NAME);
                                        final List<String> augmentedParams = new ArrayList<String>();
                                        augmentedParams.add("this");
                                        augmentedParams.addAll(params);
                                        methodToImplement.implement("public",
                                                " {" + Utils.LINE_BREAK +
                                                "                " +
                                                (methodToImplement.getResultType()==Type.VOID ? "" : "return ") + "Symqle." +
                                                 methodToImplement.getName() + "(" +
                                                 Utils.format(augmentedParams, "", ", ", "") +
                                                 ");" + Utils.LINE_BREAK+"            "+"}"+Utils.LINE_BREAK,
                                                true, true);
                                    } else {
                                        methodToImplement.implement("public",
                                                " {" + Utils.LINE_BREAK +
                                                "                " +
                                                (methodToImplement.getResultType()==Type.VOID ? "" : "return ") +
                                                methodToImplement.delegationInvocation(
                                                        methodDef.invoke("Symqle"+Utils.LINE_BREAK+"                    ",
                                                                Collections.singletonList("this"))+Utils.LINE_BREAK+"                    ") +
                                                ";" + Utils.LINE_BREAK+"            "+"}"+Utils.LINE_BREAK,
                                                true, true);
                                        }
                                }
                            }
                        } else {
                            System.err.println("Interface not compatible to " + classDef.getName()+ ", skipping: " + resType.getSimpleName());
                        }
                    } else {
                        System.err.println(classDef.getName() + "[" + nHops +"]: " + "already has " + resType.getSimpleName());
                    }
                }
            }
            // now nPlusOneHopsReachable contains all possible interfaces to implement with methods to use
            // for last step conversion
            // add interfaces to classDef and implement everything it must implement
            nHops += 1;
            // all interfaces added and method implemented: proceed to next number of hops
            nHopsReachable.clear();
            nHopsReachable.addAll(nPlusOneHopsReachable.keySet());
            nPlusOneHopsReachable.clear();
        }
    }

    /**
     * Creates a map of implicit conversions
     * key is argument interface name
     * valye is map of (result interface name, conversion method)
     * @param model
     * @return
     */
    private final Map<String, Map<String, MethodDefinition>> makeImplicitConversionsMap(final Model model) {
        final Map<String, Map<String, MethodDefinition>> conversionsMap = new HashMap<String, Map<String, MethodDefinition>>();
        for (MethodDefinition methodDef: model.getImplicitSymqleMethods()) {
            final String argName = methodDef.getFormalParameters().get(0).getType().getSimpleName();
            final String resName = methodDef.getResultType().getSimpleName();
            Map<String, MethodDefinition> conversionsFromArg = conversionsMap.get(argName);
            if (conversionsFromArg==null) {
                conversionsFromArg = new HashMap<String, MethodDefinition>();
                conversionsMap.put(argName, conversionsFromArg);
            }
            conversionsFromArg.put(resName, methodDef);
        }
        return conversionsMap;
    }

    private boolean isCompatible(Type interfaceType, ClassDefinition classDef, Map<String, TypeArgument> paramMapping, Model model) throws ModelException {
        final InterfaceDefinition anInterface = model.getInterface(interfaceType);
        for (MethodDefinition interfaceMethod : anInterface.getAllMethods(model)) {
            for (MethodDefinition classMethod: classDef.getAllMethods(model)) {
                if (classMethod.signature().equals(interfaceMethod.signature())) {
                    if (!classMethod.getResultType().getSimpleName().equals(interfaceMethod.getResultType().getSimpleName())) {
                        System.err.println(classDef.getName() + " incompatible to " + anInterface.getName() +" : " + interfaceMethod.signature() + " returns " +classMethod.getResultType() + "/" + interfaceMethod.getResultType());
                        return false;
                    }
//                    final MethodDefinition adjusted = interfaceMethod.replaceParams(classDef, paramMapping);
//                    if (!adjusted.matches(classMethod)) {
//                        return false;
//                    }
                }
            }
        }
        return true;
    }

}
