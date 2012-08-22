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

/**
 * <br/>13.11.2011
 *
 * @author Alexander Izyurov
 */
public class InterfaceDefinition {
    private final Set<String> importLines;
    private final String name;
    private final String accessModifier;
    private final List<String> otherModifiers;
    private final List<TypeParameter> typeParameters;
    private final List<Type> extended;
    private final boolean isQuery;
    private final TypeParameter queryTypeParameter;
    private final Body body;

    // presentation part
    private final String comment;
    /**
     * Declaration as in appeared in the source
     */
    private final String declaration;

    public InterfaceDefinition(SyntaxTree node, List<String> importLines) throws GrammarException {
        Assert.assertOneOf(node.getType(), "SimqleInterfaceDeclaration");

        this.importLines = new TreeSet<String>(importLines);
        {
            String accessModifier = "";
            List<String> otherModifiers = new ArrayList<String>();
            for (SyntaxTree modifier: node.find("InterfaceModifiers.InterfaceModifier")) {
                final String value = modifier.getValue();
                if ("public".equals(value) || "protected".equals(value) || "private".equals(value)) {
                    if (accessModifier.equals("")) {
                        accessModifier = value;
                    } else {
                        throw new GrammarException("Invalid access modifiers: "+accessModifier+", "+value, modifier);
                    }
                } else {
                    otherModifiers.add(value);
                }
            }
            this.accessModifier = accessModifier;
            this.otherModifiers = otherModifiers;
        }
        this.name = node.find("Identifier").get(0).getValue();
        this.typeParameters = convertChildren(node, "TypeParameters.TypeParameter", TypeParameter.class);
        this.extended = convertChildren(node, "ExtendsInterfaces.ClassOrInterfaceType", Type.class);
        validateScalarExtension(node);
        final List<SyntaxTree> archetypes = node.find("Archetypes.Archetype");
        boolean isSql = false;
        boolean isQuery = false;
        TypeParameter queryParameter = null;
        for (SyntaxTree archetype: archetypes) {
            // mandatory and unique by syntax
            final SyntaxTree name = archetype.find("Identifier").get(0);
            if ("Query".equals(name.getValue())) {
                if (isSql) {
                    throw new GrammarException("Incompatible archetypes", node);
                }
                if (isQuery) {
                    throw new GrammarException("Multiple Query archetype declaration", node);
                }
                final List<SyntaxTree> typeParameterNodes = archetype.find("TypeParameters.TypeParameter");
                if (typeParameterNodes.size()!=1) {
                    throw new GrammarException("Query archetype requires one type parameter", node);
                }
                isQuery = true;
                queryParameter = new TypeParameter(typeParameterNodes.get(0));
            } else if ("Sql".equals(name.getValue())) {
                if (isSql) {
                    throw new GrammarException("Multiple Sql archetype declaration", node);
                }
                if (isQuery) {
                    throw new GrammarException("Incompatible archetypes", node);
                }
                isSql = true;
            } else {
                throw new GrammarException("Unknown archetype: "+name.getValue(), name );
            }
        }
        if (isQuery) {
            this.importLines.add("import org.simqle.Query;");
        } else {
            this.importLines.add("import org.simqle.Sql;");
        }
        if (isScalar()) {
            this.importLines.add("import org.simqle.Scalar;");
        }
        this.importLines.add("import org.simqle.SqlContext;");
        this.isQuery = isQuery;
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
        return Collections.unmodifiableList(otherModifiers);
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

    public TypeParameter getQueryTypeParameter() {
        return queryTypeParameter;
    }

    public boolean isScalar() {
        for (Type extendedType: extended) {
            if (extendedType.getNameChain().size()==1 && extendedType.getNameChain().get(0).getName().equals("Scalar")) {
                return true;
            }
        }
        return false;
    }

    public TypeArgument getScalarTypeArgument() {
        for (Type extendedType: extended) {
            if (extendedType.getNameChain().size()==1 && extendedType.getNameChain().get(0).getName().equals("Scalar")) {
                return extendedType.getNameChain().get(0).getTypeArguments().get(0);
            }
        }
        return null;
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
}


