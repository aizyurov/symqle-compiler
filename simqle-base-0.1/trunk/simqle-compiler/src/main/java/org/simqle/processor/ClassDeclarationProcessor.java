/*
* Copyright Alexander Izyurov 2010
*/
package org.simqle.processor;

import org.simqle.model.*;
import org.simqle.parser.SyntaxTree;
import org.simqle.util.Utils;

import java.util.*;

/**
 * <br/>19.11.2011
 *
 * @author Alexander Izyurov
 */
public class ClassDeclarationProcessor implements Processor {

    public void process(SyntaxTree tree, Model model) throws GrammarException {
        // reads declarations and puts to model imports, access modifiers, annotations, name, type parameters, bodies,
        // extends/implements and mimics. Forward references are NOT resolved: they are put here only
        // formally. Will be resolved later: references to implementing interfaces when processing WITH statements;
        // references to other classes when processing MIMICS. "extends" does not need resolutions
        for (SyntaxTree block: tree.find("SimqleDeclarationBlock")) {
            final List<SyntaxTree> simqleClassDeclarations =
                    block.find("SimqleDeclaration.SimqleClassDeclaration");
            if (!simqleClassDeclarations.isEmpty()) {
                final List<SyntaxTree> importDeclarations = block.find("ImportDeclaration");
                // only one class declaration may be inside a block
                final SyntaxTree simqleClassDeclaration = simqleClassDeclarations.get(0);
                ClassDefinition baseClassDefinition = new BaseClassDefinition(simqleClassDeclaration);
                try {
                    addFields(simqleClassDeclaration.find("SimqleInterfaces.ImplementedInterface"), baseClassDefinition.getBody(), baseClassDefinition.getClassName(), model);
                    ClassDefinition extensionClassDefinition = createExtensionClass(baseClassDefinition);
                    final ClassPair classPair = new ClassPair(baseClassDefinition, extensionClassDefinition);
                    classPair.addPublishedImports(Utils.bodies(importDeclarations));
                    classPair.addInternalImports(Utils.bodies(simqleClassDeclaration.find("ImportDeclaration")));
                    final Set<Type> allInterfaces = new HashSet<Type>();
                    final Set<Type> interfacesToInvestigate = new HashSet<Type>(baseClassDefinition.getImplementedInterfaces());
                    while (!interfacesToInvestigate.isEmpty()) {
                        final Type currentIntf = interfacesToInvestigate.iterator().next();
                        allInterfaces.add(currentIntf);
                        interfacesToInvestigate.remove(currentIntf);
                        final InterfaceDefinition currentIntfDef = model.getInterface(currentIntf.getNameChain().get(0).getName());
                        if (currentIntfDef!=null) {
                            final Set<Type> newInterfaces = new HashSet<Type>(currentIntfDef.getExtended());
                            newInterfaces.removeAll(allInterfaces);
                            interfacesToInvestigate.addAll(newInterfaces);
                        }
                    }
                    for (Type intf :  allInterfaces) {
                        if ("Scalar".equals(intf.getNameChain().get(0).getName())) {
                            classPair.addInternalImports(Collections.singletonList("import java.sql.SQLException;"));
                        }

                    }
                    final List<Type> virtualAncestors = Utils.convertChildren(simqleClassDeclaration, "Mimics.ClassOrInterfaceType", Type.class);
                    for (Type t: virtualAncestors) {
                        // no associated production, so ruleName==null
                        classPair.addMimics(t, null);
                    }
                    model.addClass(classPair);
                } catch (ModelException e) {
                    throw new GrammarException(e.getMessage(), block);
                }
            }
        }
    }

    private final static String EXTENSION_CLASS_FORMAT = "%s class %s extends %s {}";

    private ClassDefinition createExtensionClass(ClassDefinition baseClassDefinition) throws GrammarException {
        final String accessModifier = baseClassDefinition.getAccessModifier();
        final String otherModifiers = Utils.concat(baseClassDefinition.getOtherModifiers(), " ");
        final String modifiers = Utils.concat(" ", accessModifier, otherModifiers);
        final List<TypeParameter> typeParameters = baseClassDefinition.getTypeParameters();
        final String typeParametersString;
        if (typeParameters.isEmpty()) {
            typeParametersString = "";
        } else {
            StringBuilder builder = new StringBuilder();
            builder.append("<");
            for (TypeParameter typeParameter: typeParameters) {
                if (builder.length()>1) {
                    builder.append(",");
                }
                builder.append(typeParameter.getImage());
            }
            builder.append(">");
            typeParametersString = builder.toString();
        }
        final String fullExtensionName = baseClassDefinition.getPairName()+typeParametersString;
        final String fullBaseName = baseClassDefinition.getClassName()+typeParametersString;
        final String source = String.format(EXTENSION_CLASS_FORMAT, modifiers, fullExtensionName, fullBaseName);
        return ClassDefinition.parse(source);
    }


    private void addFields(List<SyntaxTree> interfaceNodes, final Body body, final String className, final Model model) throws GrammarException, ModelException {
        if (interfaceNodes.isEmpty()) {
            return;
        }
        final Map<String, Type> specialVariablesMap = new LinkedHashMap<String, Type>();
        for (final SyntaxTree interfaceNode: interfaceNodes) {
            // must have exactly one ClassOrInterfaceType
            final Type type = Utils.convertChildren(interfaceNode, "ClassOrInterfaceType", Type.class).get(0);
            // must have at most one with variable
            final List<SyntaxTree> variables = interfaceNode.find("Identifier");
            for (SyntaxTree variable: variables) {
                if (null!= specialVariablesMap.put(variable.getValue(), type)) {
                    throw new GrammarException("Duplicate variable "+variable.getValue(), interfaceNode);
                }
            }
        }


        // process the variables: for each variable we generate private final field;
        // we also generate a constructor with all variables;
        // and methods of each interface, which delegate to these variables (if not exist yet)
        // Note: if interfaces have the same method, it will be implemented by delegation to the first
        // variable without any warning
        for (final Map.Entry<String, Type> entry: specialVariablesMap.entrySet()) {

            final String variableName = entry.getKey();
            final Type interfaceType = entry.getValue();
            body.addFieldDeclaration(createFieldDeclaration(variableName, interfaceType));
            final InterfaceDefinition anInterface = model.getInterface(interfaceType.getNameChain().get(0).getName());
            if (anInterface==null) {
                throw new GrammarException("Unknown interface: "+interfaceType.getImage(), interfaceNodes.get(0));
            }
            final List<MethodDeclaration> methods = anInterface.getAllMethods(model);
            final List<TypeArgument> typeArgumentsActual = interfaceType.getNameChain().get(0).getTypeArguments();
            final List<TypeParameter> typeParameters = anInterface.getTypeParameters();
            // we should substitute formal type arguments of the method with actual type parameters where appropriate
            // if unknown name, it is from outer context (not parameter name)
            for (MethodDeclaration interfaceMethod: methods) {
                final Type methodResultType = interfaceMethod.getResultType();
                final Type resultType= Utils.substituteTypeArguments(typeArgumentsActual,typeParameters, methodResultType);
                final String name = interfaceMethod.getName();
                // do not care about access modifier: interface method is public abstract by default
                // but do not implement static methods
                final List<FormalParameter> formalParameters = interfaceMethod.getFormalParameters();
                final List<FormalParameter> newFormalParameters = new ArrayList<FormalParameter>(formalParameters.size());
                for (FormalParameter formalParameter: formalParameters) {
                    final String paramName = formalParameter.getName();
                    final Type paramType = formalParameter.getType();
                    final Type newType = Utils.substituteTypeArguments(typeArgumentsActual, typeParameters, paramType);
                    newFormalParameters.add(new FormalParameter(newType, paramName, Arrays.asList("final")));
                }

                final String generatedBody = interfaceMethod.getResultType().equals(Type.VOID) ?
                    createVoidDelegatedMethodBody(variableName, name, newFormalParameters) :
                    createDelegatedMethodBody(variableName, name, newFormalParameters);
                    final MethodDeclaration methodDeclaration = new MethodDeclaration(false, "public", false, false, interfaceMethod.getTypeParameters(), resultType, name, newFormalParameters, interfaceMethod.getThrowsClause(), interfaceMethod.getComment(), generatedBody);
                    try {
                        body.addMethod(methodDeclaration);
                    } catch (ModelException e) {
                        throw new GrammarException("Method \""+name+"\" already defined but generation requested", interfaceNodes.get(0));
                    }
            }
        }
        if (!specialVariablesMap.isEmpty()) {
            final ConstructorDeclaration constructor = createConstructor(specialVariablesMap, className);
            // we know that the constructor name matches class name for sure; can use unsafeAdd
            body.unsafeAddConstructorDeclaration(constructor);
        }
    }

    private final String FIELD_FORMAT = Utils.join(8,
            "private final %s %s;");

    private FieldDeclaration createFieldDeclaration(String name, Type type) throws GrammarException {
        final String declarationSource = String.format(FIELD_FORMAT, type.getImage(), name);
        return FieldDeclaration.parse(declarationSource);
    }

    private final static String GENERATED_METHOD_BODY_TEMPLATE = Utils.join(8,
            "{",
            "    return %s.%s(%s);",
            "}");

    private final static String GENERATED_VOID_METHOD_BODY_TEMPLATE = Utils.join(8,
            "{",
            "    %s.%s(%s);",
            "}");

    private String createDelegatedMethodBody(String variableName, String methodName, List<FormalParameter> parameters) {
        return createMethodBody(variableName, methodName, parameters, GENERATED_METHOD_BODY_TEMPLATE);
    }

    private String createVoidDelegatedMethodBody(String variableName, String methodName, List<FormalParameter> parameters) {
        return createMethodBody(variableName, methodName, parameters, GENERATED_VOID_METHOD_BODY_TEMPLATE);
    }

    private String createMethodBody(final String variableName, final String methodName, final List<FormalParameter> parameters, final String template) {
        StringBuilder argumentsBuilder = new StringBuilder();
        for (FormalParameter param: parameters) {
            if (argumentsBuilder.length()>0) {
                argumentsBuilder.append(", ");
            }
            argumentsBuilder.append(param.getName());
        }
        return String.format(template, variableName, methodName, argumentsBuilder.toString());
    }


    private final static String CONSTRUCTOR_FORMAT = Utils.join(8,
            "public %s(%s) {",
            "%s}");

    private final static String VARIABLE_ASSIGNMENT_FORMAT = "this.%1$s = %1$s;\n";
    private final static String CTR_FORMAL_ARG_FORMAT = "final %s %s";

    private ConstructorDeclaration createConstructor(Map<String, Type> variables, String className) throws GrammarException {
        StringBuilder variablesAssignmentBuilder = new StringBuilder();
        StringBuilder formalArgsBuilder = new StringBuilder();
        for (Map.Entry<String, Type> entry: variables.entrySet()) {
            final String variable = entry.getKey();
            final Type type = entry.getValue();
            variablesAssignmentBuilder.append(String.format(VARIABLE_ASSIGNMENT_FORMAT, variable));
            if (formalArgsBuilder.length()>0) {
                formalArgsBuilder.append(", ");
            }
            formalArgsBuilder.append(String.format(CTR_FORMAL_ARG_FORMAT, type.getImage(), variable));
        }
        final String constructorSource = String.format(CONSTRUCTOR_FORMAT, className, formalArgsBuilder.toString(), variablesAssignmentBuilder.toString());
        return ConstructorDeclaration.parse(constructorSource);
    }

}
