/*
* Copyright Alexander Izyurov 2010
*/
package org.symqle.processor;

import org.symqle.model.*;
import org.symqle.util.Utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * <br/>20.11.2011
 *
 * @author Alexander Izyurov
 */
public class ProductionImplementationProcessor extends ModelProcessor {


    @Override
    public void process(final Model model) throws ModelException {

        final ClassDefinition symqle;
        try {
            symqle = model.getClassDef("Symqle");
        } catch (ModelException e) {
            throw new IllegalStateException("Internal error", e);
        }

        final ClassDefinition symqleTemplate = model.getSymqleTemplate();

        for (MethodDefinition methodToImplement : symqleTemplate.getDeclaredMethods()) {
            final MethodDefinition newMethod;
            if (methodToImplement.getOtherModifiers().contains("abstract")) {
                newMethod = implementMethod(methodToImplement, symqle, model);
            } else {
                newMethod =copyMethod(methodToImplement, symqle);
            }
            symqle.addMethod(newMethod);
        }
        symqle.addImportLines(symqleTemplate.getImportLines());
    }

    private static MethodDefinition copyMethod(final MethodDefinition method, final ClassDefinition newOwner) {
        return MethodDefinition.parse(method.toString(), newOwner);
    }

    private static MethodDefinition implementMethod(final MethodDefinition method, final ClassDefinition newOwner, final Model model) throws ModelException {
        final AnonymousClass anonymousClass = model.getAnonymousClassByMethodSignature(method.signature());
        for (MethodDefinition innerMethod: anonymousClass.getAllMethods(model)) {
            // implement non-implemented methods
            final Set<String> modifiers = innerMethod.getOtherModifiers();
            if (modifiers.contains("volatile") && modifiers.contains("abstract")) {
                if (Archetype.isArchetypeMethod(innerMethod)) {
                                    final String body = archetypeMethodBody(innerMethod, method, model);
                                    innerMethod.implement("public", body, true, false);
                } else if (method.getName().startsWith("get")
                                            && method.getName().length() > 3
                                            && method.getFormalParameters().size()==0) {
                                    final String body = implementPropertyMethod(anonymousClass, innerMethod, model);
                                    innerMethod.implement("public", body, true, false);
                } else if (!method.getFormalParameters().isEmpty()) {
                    final FormalParameter firstArg = method.getFormalParameters().get(0);
                    final Type firstArgType = firstArg.getType();
                    final AbstractTypeDefinition firstArgClass = model.getAbstractType(firstArgType.getSimpleName());
                    final MethodDefinition methodToDelegate = firstArgClass.getMethodBySignature(innerMethod.signature(), model);
                    // we have very simple method to delegate like getMapper, so no check for type parameters here
                    // signature is enough
                    if (methodToDelegate != null && methodToDelegate.getResultType().equals(innerMethod.getResultType())) {
                        innerMethod.implement("public", " { " + (innerMethod.getResultType().equals(Type.VOID) ? "" : "return ") +
                                methodToDelegate.delegationInvocation(firstArg.getName()) +
                                "; }"
                        );
                    } else {
                        // should be call to Symqle
                        innerMethod.implement("public", implementBySymqleCall(innerMethod, model), true, false);
                    }
                } else {
                    // cannot delegate; should be Symqle method with a single arg of this
                    innerMethod.implement("public", implementBySymqleCall(innerMethod, model), true, false);

                }
            }
        }
        final String accessModifier = method.getAccessModifier();
        final String newAccessModifier;
        if ("private".equals(accessModifier) || "protected".equals(accessModifier)) {
            newAccessModifier = "private ";
        } else {
            newAccessModifier = "";
        }
        final MethodDefinition implemented = MethodDefinition.parse(newAccessModifier + "static " + method.getTypeParameters() +" " +
                method.getResultType() + " " + method.getName() +
                "(" + Utils.format(method.getFormalParameters(), "", ", ", "") +")" + " {" +
                Utils.LINE_BREAK +
                        "        return new "+method.getResultType()+"()" +
                anonymousClass.instanceBodyAsString() + ";"+ Utils.LINE_BREAK +
                "    }"+Utils.LINE_BREAK                            , newOwner);
        implemented.setSourceRef(method.getSourceRef());
        return implemented;
    }

    private static String archetypeMethodBody(MethodDefinition innerMethod, MethodDefinition outerMethod, Model model) throws ModelException {
        final String resultTypeName = innerMethod.getResultType().getSimpleName();
        if (resultTypeName.equals("Sql")) {
            return createSqlMethodBody(innerMethod, outerMethod, model);
        } else {
            // Query
            return createQueryMethodBody(innerMethod, outerMethod, model);
          }
        }

    private static String createQueryMethodBody(MethodDefinition innerMethod, MethodDefinition outerMethod, final Model model) throws ModelException {
        StringBuilder builder = new StringBuilder();
        builder.append(" {").append(Utils.LINE_BREAK);
        final FormalParameter firstParameter = outerMethod.getFormalParameters().get(0);
        final InterfaceDefinition interfaceDef = model.getInterface(firstParameter.getType());
        // find corresponding archetype method of this interface
        final String firstSqlParam = interfaceDef.getArchetypeMethod().delegationInvocation(firstParameter.getName());
        builder.append("                ")
                .append("final ")
                .append(innerMethod.getResultType())
                .append(" __rowMapper = ")
                .append(firstSqlParam)
                .append(";").append(Utils.LINE_BREAK);
        builder.append("                ");
        builder.append("return ");
        builder.append("new Complex").
            append(innerMethod.getResultType())
                .append("(")
                .append("__rowMapper, ")
                .append("context.get(Dialect.class).")
                .append(model.getAssociatedDialectName(outerMethod))
                .append("(")
                .append(Utils.format(outerMethod.getFormalParameters(), "", ", ", "", new F<FormalParameter, String, ModelException>() {
                    int count = 0;
                    @Override
                    public String apply(final FormalParameter formalParameter) throws ModelException {
                        if (count++ == 0) {
                            return "__rowMapper";
                        } else {
                            final InterfaceDefinition interfaceDef = model.getInterface(formalParameter.getType());
                            // find corresponding archetype method of this interface
                            return interfaceDef.getArchetypeMethod().delegationInvocation(formalParameter.getName());
                        }
                    }
                }))
                .append(")")
                .append(")")
                .append(";")
                .append(Utils.LINE_BREAK);
        builder.append("            }");
        return builder.toString();
    }

    private static String createSqlMethodBody(MethodDefinition innerMethod, MethodDefinition outerMethod, final Model model) throws ModelException {
        StringBuilder builder = new StringBuilder();
        builder.append( " { return context.get(Dialect.class).")
                .append(model.getAssociatedDialectName(outerMethod))
                .append("(")
                .append(Utils.format(outerMethod.getFormalParameters(), "", ", ", "", new F<FormalParameter, String, ModelException>() {
                                    @Override
                                    public String apply(final FormalParameter formalParameter) throws ModelException {
                                            final InterfaceDefinition interfaceDef = model.getInterface(formalParameter.getType());
                                            // find corresponding archetype method of this interface
                                            return interfaceDef.getArchetypeMethod().delegationInvocation(formalParameter.getName());
                                    }
                }))
                .append("); } ");
        return builder.toString();
    }

    private static String implementPropertyMethod(AnonymousClass anonymousClass, MethodDefinition innerMethod, Model model) {
        final String methodName = innerMethod.getName();
        String propertyName = methodName.substring(3,4).toLowerCase() +
                methodName.substring(4, methodName.length());
        String fieldDeclarationSource = "        private final " +
                innerMethod.getResultType() + " " + propertyName + " = " +
                innerMethod.delegationInvocation(innerMethod.getFormalParameters().get(0).getName()) +
                ";";
        anonymousClass.addFieldDeclaration(FieldDeclaration.parse(fieldDeclarationSource));

        final StringBuilder bodyBuilder = new StringBuilder();
        bodyBuilder.append(" {").append(Utils.LINE_BREAK);
        bodyBuilder.append("                ");
        bodyBuilder.append("return ").append(propertyName).append(";");
        bodyBuilder.append("            }");
        return bodyBuilder.toString();
    }

    private static String implementBySymqleCall(MethodDefinition innerMethod, Model model) {
        final Collection<String> parameters = Utils.map(innerMethod.getFormalParameters(), FormalParameter.NAME);
        final List<String> symqleParameters = new ArrayList<String>();
        symqleParameters.add("this");
        symqleParameters.addAll(parameters);
        StringBuilder bodyBuilder = new StringBuilder();
        bodyBuilder.append(" {").append(Utils.LINE_BREAK)
            .append("                ");
        if (!innerMethod.getResultType().equals(Type.VOID)) {
            bodyBuilder.append("return ");
        }
        bodyBuilder.append("Symqle.").append(innerMethod.getName())
                .append("(")
                .append(Utils.format(symqleParameters, "", ", ", ""))
                .append(");");
        bodyBuilder.append("            }");
        return bodyBuilder.toString();
    }
}