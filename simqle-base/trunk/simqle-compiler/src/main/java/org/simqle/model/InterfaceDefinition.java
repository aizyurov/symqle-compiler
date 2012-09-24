/*
* Copyright Alexander Izyurov 2010
*/
package org.simqle.model;

import org.simqle.parser.SyntaxTree;
import org.simqle.processor.GrammarException;
import org.simqle.util.Assert;
import org.simqle.util.Utils;

import java.util.*;

import static org.simqle.util.Utils.convertChildren;
import static org.simqle.util.Utils.getChildrenImage;
import static org.simqle.util.Utils.substituteTypeArguments;

/**
 * <br/>13.11.2011
 *
 * @author Alexander Izyurov
 */
public class InterfaceDefinition {
    private final Set<String> importLines;
    private final String name;
    private final String accessModifier;
    private final Set<String> otherModifiers;
    private final List<TypeParameter> typeParameters;
    private final List<Type> extended;
    private final boolean isQuery;
    private final boolean isSql;
    private final TypeParameter queryTypeParameter;
    private final Body body;

    // presentation part
    private final String comment;
    /**
     * Declaration as in appeared in the source
     */
    private final String declaration;

    public InterfaceDefinition(SyntaxTree node, List<String> importLines) throws GrammarException {
        Assert.assertOneOf(new GrammarException("Unexpected type: "+node.getType(), node), node.getType(), "SimqleInterfaceDeclaration");

        this.importLines = new TreeSet<String>(importLines);
        final List<SyntaxTree> modifierNodes = node.find("InterfaceModifiers.InterfaceModifier");
        this.accessModifier = Utils.getAccessModifier(modifierNodes);
        this.otherModifiers = Utils.getNonAccessModifiers(modifierNodes);
        this.name = node.find("Identifier").get(0).getValue();
        this.typeParameters = convertChildren(node, "TypeParameters.TypeParameter", TypeParameter.class);
        this.extended = convertChildren(node, "ExtendsInterfaces.ClassOrInterfaceType", Type.class);
        validateScalarExtension(node);
        final List<SyntaxTree> archetypes = node.find("Archetype");
        boolean isSql = false;
        boolean isQuery = false;
        TypeParameter queryParameter = null;
        for (SyntaxTree archetype: archetypes) {
            // mandatory and unique by syntax
            final SyntaxTree name = archetype.find("Identifier").get(0);
            if ("Query".equals(name.getValue())) {
                final List<SyntaxTree> typeParameterNodes = archetype.find("TypeParameters.TypeParameter");
                if (typeParameterNodes.size()!=1) {
                    throw new GrammarException("Query archetype requires one type parameter", node);
                }
                isQuery = true;
                queryParameter = new TypeParameter(typeParameterNodes.get(0));
            } else if ("Sql".equals(name.getValue())) {
                isSql = true;
            } else {
                throw new GrammarException("Unknown archetype: "+name.getValue(), name );
            }
        }
        if (isQuery) {
            this.importLines.add("import org.simqle.Query;");
        } else if (isSql) {
            this.importLines.add("import org.simqle.Sql;");
        }
        if (isSql || isQuery) {
            this.importLines.add("import org.simqle.SqlContext;");
        }
        this.isQuery = isQuery;
        this.isSql = isSql;
        this.queryTypeParameter = queryParameter;

        // exactly one body guaranteed by syntax
        body = Utils.convertChildren(node, "InterfaceBody", Body.class).get(0);


        comment = node.getComments();
        StringBuilder declarationBuilder = new StringBuilder();
        declarationBuilder.append(getChildrenImage(node, "InterfaceModifiers"))
                .append(" interface")
                .append(getChildrenImage(node, "Identifier"))
                .append(getChildrenImage(node, "TypeParameters"))
                .append(getChildrenImage(node, "ExtendsInterfaces"));
        declaration = declarationBuilder.toString();
    }

    public String getName() {
        return name;
    }

    public List<String> getImportLines() {
        return new ArrayList<String>(importLines);
    }

    public void addImportLine(String importLine) {
        importLines.add(importLine);
    }

    public String getAccessModifier() {
        return accessModifier;
    }

    public List<String> getOtherModifiers() {
        return new ArrayList<String>(otherModifiers);
    }

    public List<TypeParameter> getTypeParameters() {
        return Collections.unmodifiableList(typeParameters);
    }

    public List<Type> getExtended() {
        return Collections.unmodifiableList(extended);
    }

    public Body getBody() {
        return body;
    }

    public String getComment() {
        return comment;
    }

    public String getDeclaration() {
        return declaration;
    }

    public boolean isQuery() {
        return isQuery;
    }

    public boolean isSql() {
        return isSql;
    }

    public TypeParameter getQueryTypeParameter() {
        return queryTypeParameter;
    }

    private void validateScalarExtension(SyntaxTree node) throws GrammarException {
        for (Type extendedType: extended) {
            if (extendedType.getNameChain().size()==1 && extendedType.getNameChain().get(0).getName().equals("Scalar")) {
                final int typeArgumentsSize = extendedType.getNameChain().get(0).getTypeArguments().size();
                if (typeArgumentsSize !=1) {
                    throw new GrammarException("Scalar interface requires 1 type argument; found "+typeArgumentsSize, node);
                }
            }
        }
    }

    /**
     * Returns all methods - declared and inherited
     * Note: a method may be returned more than once, if it is inherited from multiple parents ofr both inherited and declared
     * @param model we need model to have access to inherited methods
     * @return
     */
    public List<MethodDeclaration> getAllMethods(final Model model) throws ModelException {
        // Map of methods by signature
        Map<String, MethodDeclaration> allMethods = collectAllMethods(model);
        return new ArrayList<MethodDeclaration>(allMethods.values());
    }

    public MethodDeclaration getMethodBySignature(final String signature, final Model model) throws ModelException {
        Map<String, MethodDeclaration> allMethods = collectAllMethods(model);
        return allMethods.get(signature);
    }

    private Map<String, MethodDeclaration> collectAllMethods(final Model model) throws ModelException {
        Map<String, MethodDeclaration> allMethods = new HashMap<String, MethodDeclaration>();
        final List<MethodDeclaration> declaredMethods = body.getMethods();
        for (MethodDeclaration method: declaredMethods) {
            final String signature = method.getSignature();
            final MethodDeclaration existingMethod = allMethods.get(signature);
            // ignoring conflicting throws for now - leavng for java compiler
            if (existingMethod!=null && !existingMethod.getResultType().equals(method.getResultType())) {
                throw new ModelException("Return type conflict: "+existingMethod.getResultType().getImage()+" and "+method.getResultType().getImage()+" "+signature);
            } else {
                // overwrite
                allMethods.put(signature, method);
            }
        }

        for (Type type: extended) {
            final List<TypeNameWithTypeArguments> nameChain = type.getNameChain();
            if (nameChain.size()!=1) {
                throw new ModelException("Extended interface should have simple name");
            }
            final TypeNameWithTypeArguments typeNameWithTypeArguments = nameChain.get(0);
            final InterfaceDefinition parentInterface = model.getInterface(typeNameWithTypeArguments.getName());
            if (parentInterface==null) {
                throw new ModelException("Interface not found: "+typeNameWithTypeArguments.getName());
            }
            final List<MethodDeclaration> inheritedMethods = parentInterface.getAllMethods(model);
            for (MethodDeclaration method: inheritedMethods) {
                final Type parentResultType = method.getResultType();
                final Type resultType = substituteTypeArguments(typeNameWithTypeArguments.getTypeArguments(), parentInterface.getTypeParameters(), parentResultType);
                final List<FormalParameter> parentFormalParameters = method.getFormalParameters();
                final List<FormalParameter> formalParameters = new ArrayList<FormalParameter>(parentFormalParameters.size());
                for (FormalParameter parameter: parentFormalParameters) {
                    final List<String> modifiers = parameter.getModifiers();
                    final String paramName = parameter.getName();
                    final Type parentParamType = parameter.getType();
                    final Type paramType = substituteTypeArguments(typeNameWithTypeArguments.getTypeArguments(), parentInterface.getTypeParameters(), parentParamType);
                    formalParameters.add(new FormalParameter(paramType, paramName, modifiers));
                }
                final String throwsClause = method.getThrowsClause();
                final String methodName = method.getName();
                // TODO we potentially can have a conflict of method type parameters and this thype parameters;
                // method type parameters should better be renamed to avoid this conflict
                // leaving as is for now
                final MethodDeclaration generatedMethod = new MethodDeclaration(true, "public", false, true, method.getTypeParameters(),
                        resultType, methodName, formalParameters, throwsClause, "", null);
                final String signature = generatedMethod.getSignature();
                final MethodDeclaration existingMethod = allMethods.get(signature);
                // ignoring conflicting throws for now - leavng for java compiler
                if (existingMethod!=null && !existingMethod.getResultType().equals(generatedMethod.getResultType())) {
                    throw new ModelException("Return type conflict: "+existingMethod.getResultType().getImage()+" and "+generatedMethod.getResultType().getImage()+" "+signature);
                } else {
                    // overwrite
                    allMethods.put(signature, generatedMethod);
                }


            }
        }
        return allMethods;
    }


}


