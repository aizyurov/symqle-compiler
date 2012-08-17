/*
* Copyright Alexander Izyurov 2010
*/
package org.simqle.processor;

import org.simqle.model.*;
import org.simqle.parser.ParseException;
import org.simqle.parser.SimpleNode;
import org.simqle.parser.SyntaxTree;

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
                ClassDefinition baseClassDefinition = new BaseClassDefinition(simqleClassDeclaration, importDeclarations);
                try {
                    addFields(simqleClassDeclaration.find("SimqleInterfaces.ImplementedInterface"), baseClassDefinition.getBody(), baseClassDefinition.getPairName(), model);
                    ClassDefinition extensionClassDefinition = createExtensionClass(baseClassDefinition);
                    model.addClass(new ClassPair(baseClassDefinition, extensionClassDefinition));
                } catch (ModelException e) {
                    throw new RuntimeException("Unhandled exception", e);
                }
            }
        }
    }

    private final static String EXTENSION_CLASS_FORMAT = "%s class %s extends %s {}";
    private final static String EXTENSION_CONSTRUCTOR_FORMAT = "public %s(%s) { super(%s); }";

    private ClassDefinition createExtensionClass(ClassDefinition baseClassDefinition) {
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
        try {
            final SimpleNode node = Utils.createParser(source).SimqleClassDeclaration();

            final ClassDefinition extensionClassDefinition = new ClassDefinition(new SyntaxTree(node, "EXTENSION_CLASS_FORMAT"), Collections.<SyntaxTree>emptyList());
            // transfer 'mimics' to extension
            for (Type type: baseClassDefinition.getMimics()) {
                extensionClassDefinition.addMimics(type);
            }
            // transfer all constructors
            final List<ConstructorDeclaration> constructors = baseClassDefinition.getBody().getConstructors();
            for (final ConstructorDeclaration constructor : constructors) {
                final List<FormalParameter> formalParameters = constructor.getFormalParameters();
                StringBuilder argumentsBuilder = new StringBuilder();
                for (FormalParameter formalParameter: formalParameters) {
                    if (argumentsBuilder.length()>0) {
                        argumentsBuilder.append(", ");
                    }
                    argumentsBuilder.append(formalParameter.getName());
                }
                final String argumentsString = argumentsBuilder.toString();

                StringBuilder parametersBuilder = new StringBuilder();
                for (FormalParameter formalParameter: formalParameters) {
                    if (parametersBuilder.length()>0) {
                        parametersBuilder.append(", ");
                    }
                    parametersBuilder.append(formalParameter.getImage());
                }
                final String parametersString = parametersBuilder.toString();
                final String constructorSource = String.format(EXTENSION_CONSTRUCTOR_FORMAT, baseClassDefinition.getPairName(), parametersString, argumentsString);
                final SimpleNode constructorNode = Utils.createParser(constructorSource).ConstructorDeclaration();
                final ConstructorDeclaration constructorDeclaration = new ConstructorDeclaration(new SyntaxTree(constructorNode, "EXTENSION_CONSTRUCTOR_FORMAT"));
                extensionClassDefinition.addConstructorDeclaration(constructorDeclaration);

            }
            return extensionClassDefinition;
        } catch (ParseException e) {
            throw new RuntimeException("Internal error", e);
        } catch (ModelException e) {
            throw new RuntimeException("Internal error", e);
        } catch (GrammarException e) {
            throw new RuntimeException("Internal error", e);
        }
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
            if (!variables.isEmpty()) {
                final Type previousValue = specialVariablesMap.put(variables.get(0).getValue(), type);
                if (null!= previousValue) {
                    throw new GrammarException("Duplicate variable "+variables.get(0).getValue(), interfaceNode);
                }
            }
        }
        // process the variables: for each variable we generate private final field;
        // we also generate a constructor with all variables;
        // and methods of each interface, which delegate to these variables (if not exist yet)
        // Note: if interfaces have the same method, it will be implemented by delegation to the first
        // variable without any warning
        for (final Map.Entry<String, Type> entry: specialVariablesMap.entrySet()) {

            try {
                final String variableName = entry.getKey();
                final Type interfaceType = entry.getValue();
                body.addFieldDeclaration(createFieldDeclaration(variableName, interfaceType));
                final InterfaceDefinition anInterface = model.getInterface(interfaceType.getNameChain().get(0).getName());
                if (anInterface==null) {
                    throw new GrammarException("Unknown interface: "+interfaceType.getImage(), interfaceNodes.get(0));
                }
                final List<MethodDeclaration> methods = anInterface.getBody().getMethods();
                final List<TypeArgument> typeArgumentsActual = interfaceType.getNameChain().get(0).getTypeArguments();
                final List<TypeParameter> typeParameters = anInterface.getTypeParameters();
                // we should substitute formal type arguments of the method with actual type parameters where appropriate
                // if unknown name, it is from outer context (not parameter name)
                for (MethodDeclaration interfaceMethod: methods) {
                    final Type methodResultType = interfaceMethod.getResultType();
                    final Type resultType;
                    if (methodResultType!=null) {
                        final List<TypeNameWithTypeArguments> resutlTypeInternal = Utils.substituteTypeArguments(methodResultType.getNameChain(), typeParameters, typeArgumentsActual);
                        resultType = new Type(resutlTypeInternal, methodResultType.getArrayDimensions());
                    } else {
                        resultType = null;
                    }
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

                    final String generatedBody = interfaceMethod.getResultType()==null ?
                        createVoidDelegatedMethodBody(variableName, name, newFormalParameters) :
                        createDelegatedMethodBody(variableName, name, newFormalParameters);
                    if (!interfaceMethod.isStatic()) {
                        final MethodDeclaration methodDeclaration = new MethodDeclaration(false, "public", false, false, interfaceMethod.getTypeParameters(), resultType, name, newFormalParameters, interfaceMethod.getThrowsClause(), interfaceMethod.getComment(), generatedBody);
                        try {
                            body.addMethod(methodDeclaration);
                        } catch (ModelException e) {
                            throw new GrammarException(e.getMessage(), interfaceNodes.get(0));
                        }
                    }
                }
                final ConstructorDeclaration constructor = createConstructor(specialVariablesMap, className);
                // we know that the constructor name matches class name for sure; can use unsafeAdd
                body.unsafeAddConstructorDeclaration(constructor);

            } catch (ModelException e) {
                throw new GrammarException(e.getMessage(), interfaceNodes.get(0));
            }
        }
    }

    private final String FIELD_FORMAT = Utils.join(8,
            "private final %s %s;");

    private FieldDeclaration createFieldDeclaration(String name, Type type) {
        final String declarationSource = String.format(FIELD_FORMAT, type.getImage(), name);
        try {
            final SimpleNode node = Utils.createParser(declarationSource).FieldDeclaration();
            return new FieldDeclaration(new SyntaxTree(node, "FIELD_FORMAT"));
        } catch (ParseException e) {
            throw new RuntimeException("Internal Error", e);
        } catch (GrammarException e) {
            throw new RuntimeException("Internal Error", e);
        }
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

    private ConstructorDeclaration createConstructor(Map<String, Type> variables, String className) {
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
        try {
            final SimpleNode node = Utils.createParser(constructorSource).ConstructorDeclaration();
            return new ConstructorDeclaration(new SyntaxTree(node, "CONSTRUCTOR_FORMAT"));
        } catch (ParseException e) {
            throw new RuntimeException("Internal error", e);
        } catch (GrammarException e) {
            throw new RuntimeException("Internal error", e);
        }
    }

}
