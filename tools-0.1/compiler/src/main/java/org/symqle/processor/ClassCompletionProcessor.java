package org.symqle.processor;

import org.symqle.model.*;
import org.symqle.util.Utils;

import java.util.HashSet;
import java.util.Set;

import static org.symqle.util.Utils.LINE_BREAK;

/**
 * @author lvovich
 */
public class ClassCompletionProcessor extends ModelProcessor {

    @Override
    protected void process(final Model model) throws ModelException {
        for (ClassDefinition classDef: model.getAllClasses()) {
            finalizeClass(model, classDef);
        }

    }

    @Override
    protected Processor predecessor() {
        return new ClassDeclarationProcessor();
    }

    private void finalizeClass(Model model, ClassDefinition classDefinition) throws ModelException {
        StringBuilder javadocBuilder = new StringBuilder();
        javadocBuilder.append("/**").append(LINE_BREAK);
        javadocBuilder.append(" * Basic implementation of interface methods." ).append(LINE_BREAK);
        javadocBuilder.append(" * Subclasses must implement:").append(LINE_BREAK);
        javadocBuilder.append(" *<ul>").append(LINE_BREAK);
        final Set<String> abstractMethodsSignatures = new HashSet<String>();
        final Set<MethodDefinition> abstractMethods = new HashSet<MethodDefinition>();
        for (final MethodDefinition method: classDefinition.getAllMethods(model)) {
            if (method.getOtherModifiers().contains("volatile") && method.getOtherModifiers().contains("abstract")) {
                method.declareAbstract("public");
                javadocBuilder.append(" * <li>{@link #").append(method.getName());
                javadocBuilder.append(Utils.format(method.getFormalParameters(), "(", ", ", ")", new F<FormalParameter, String, RuntimeException>() {
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
        ;
        javadocBuilder.append(" *</ul>").append(LINE_BREAK);
        javadocBuilder.append(" */").append(LINE_BREAK);
        if (!abstractMethodsSignatures.isEmpty()) {
            classDefinition.replaceComment(javadocBuilder.toString());
        }
        for (Type type : classDefinition.getImplementedInterfaces()) {
            classDefinition.addImportLines(model.getInterface(type).getImportLines());
        }


        if (!abstractMethodsSignatures.isEmpty()) {
            for (Type ancestor: classDefinition.getAllAncestors(model)) {
                System.err.println("Trying to adapt to " + classDefinition.getName() + " "+ancestor.getSimpleName());
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
                        adaptBuilder.append("public static ")
                                .append(classDefinition.getTypeParameters())
                                .append(" ")
                                .append(classDefinition.getType())
                                .append(" adapt(final ")
                                .append(ancestor).append(" adaptee) {").append(Utils.LINE_BREAK)
                                .append("    return new ").append(classDefinition.getType()).append("() {")
                                .append(Utils.LINE_BREAK);
                        for (MethodDefinition method: abstractMethods) {
                            adaptBuilder.append("        public ").append(method.getTypeParameters()).append(" ")
                                    .append(method.getResultType()).append(" ").append(method.getName()).append("(")
                                    .append(Utils.format(method.getFormalParameters(), "", ", ", ""))
                                    .append(") {").append(Utils.LINE_BREAK)
                                    .append("            ")
                                    .append(method.getResultType().equals(Type.VOID) ? "" : "return ")
                                    .append(method.delegationInvocation("adaptee"))
                                    .append(";").append(Utils.LINE_BREAK)
                                    .append("        }").append(Utils.LINE_BREAK);
                        }
                        adaptBuilder.append("    };").append(LINE_BREAK)
                                .append("}");
                        final MethodDefinition adaptMethod = MethodDefinition.parse(adaptBuilder.toString(), classDefinition);
                        classDefinition.addMethod(adaptMethod);
                    } else {
                        Set<String> remaining = new HashSet<String>(abstractMethodsSignatures);
                        remaining.removeAll(ancestorMethodsSignatures);
                        System.err.println(classDefinition.getName() + ": cannot adapt(" +ancestor.getSimpleName() +"), cannot delegate " + remaining);
                    }
                }
            }
        } else {
            System.err.println("No abstract methods in " + classDefinition.getName());
        }
    }

}
