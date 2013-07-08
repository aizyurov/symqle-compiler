package org.symqle.processor;

import org.symqle.model.*;
import org.symqle.util.Utils;

import java.io.BufferedReader;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author lvovich
 */
public class ClassEnhancer implements ModelProcessor {

    @Override
    public void process(final Model model) throws ModelException {
        for (ClassDefinition classDef: model.getAllClasses()) {
            enhanceClass(classDef, model);
        }
    }

    private void enhanceClass(final ClassDefinition classDef, final Model model) throws ModelException {
        Map<String, MethodTemplate> generatedMethods = new HashMap<String, MethodTemplate>();
        Set<String> ambiguousMethods = new HashSet<String>();
        for (MethodDefinition method: model.getExplicitSymqleMethods()) {
            final MethodTemplate methodTemplate = tryAddMethod(classDef, method, model);
            if (methodTemplate != null) {
                final String signature = methodTemplate.myAbstractMethod.signature();
                if (generatedMethods.containsKey(signature)) {
                    final MethodTemplate existing = generatedMethods.get(signature);

                    final MethodTemplate keep;
                    final MethodTemplate throwAway;
                    // less priority wins, if equal, first wins
                    if (existing.priority <= methodTemplate.priority) {
                        keep = existing;
                        throwAway = methodTemplate;
                    } else {
                        keep = methodTemplate;
                        throwAway = existing;
                    }
                    System.err.println("WARN: conflicting methods; keep: " +
                            keep.myAbstractMethod.declaration() + " ["+keep.priority+"]" +
                    " throw away: " +
                            throwAway.myAbstractMethod.declaration() + " ["+throwAway.priority+"]" +
                            " in "+ classDef.getName());
                    ambiguousMethods.add(signature);
                    generatedMethods.put(signature, keep);
                } else {
                    generatedMethods.put(signature, methodTemplate);
                }
                // add imports only if you are declaring the method
                classDef.addImportLines(model.getImportsForExplicitMethod(method));
            }
        }
        // generate real methods
        for (Map.Entry<String, MethodTemplate> entry: generatedMethods.entrySet()) {
                final MethodDefinition myAbstractMethod = entry.getValue().myAbstractMethod;
                List<String> parameters = new ArrayList<String>(Utils.map(myAbstractMethod.getFormalParameters(), FormalParameter.NAME));
                if (ambiguousMethods.contains(entry.getKey())) {
                    // must explicitly cast to myType
                    parameters.add(0, "("+entry.getValue().myType+") this");
                } else {
                    parameters.add(0, "this");
                }

                ((MethodDefinition) myAbstractMethod).implement(myAbstractMethod.getAccessModifier(), " {" + Utils.LINE_BREAK +
                        "        " +
                        (myAbstractMethod.getResultType().equals(Type.VOID) ? "" : "return ") +
                        "Symqle." +
                        myAbstractMethod.getName() +
                        "(" +
                        Utils.format(parameters, "", ", ", "") +
                        ");" + Utils.LINE_BREAK +
                        "    }", true, true
                );
        }

        // finally, make sure that imports from ancestors go to this class
        classDef.ensureRequiredImports(model);

    }

    private MethodTemplate tryAddMethod(final ClassDefinition classDef, final MethodDefinition method, final Model model) {
        String accessModifier = method.getAccessModifier();
        if (accessModifier.equals("private") || accessModifier.equals("protected")) {
            return null;
        }
        final List<FormalParameter> formalParameters = method.getFormalParameters();
        if (formalParameters.isEmpty()) {
            return null;
        }
        final Type firstArgType = formalParameters.get(0).getType();

        for (Type myType: classDef.getImplementedInterfaces()) {
            // names must match
            if (!myType.getSimpleName().equals(firstArgType.getSimpleName())) {
                continue;
            }
            final Map<String, TypeArgument> mapping;
            try {
                mapping = method.getTypeParameters().inferTypeArguments(firstArgType, myType);
            } catch (ModelException e) {
                // cannot infer type parameter values; skip this method
                return null;
            }
            if (firstArgType.replaceParams(mapping).equals(myType) ||
            (firstArgType.getTypeArguments().getArguments().size()==1
                    && firstArgType.getTypeArguments().getArguments().get(0).isWildCardArgument()
                    && myType.getTypeArguments().getArguments().size() == 1)) {
                // special case: both have a single parameter, which is wildcard in firstArg,
                // so types match
                return new MethodTemplate(createMyMethod(classDef, method, myType, mapping), myType, classDef.getPriority(myType));

            } else {
                return null;
            }
        }
        // no type matches
        return null;
    }

    private static class MethodTemplate {
        private final MethodDefinition myAbstractMethod;
        private final Type myType;
        private final int priority;

        private MethodTemplate(final MethodDefinition myAbstractMethod, final Type myType, final int priority) {
            this.myAbstractMethod = myAbstractMethod;
            this.myType = myType;
            this.priority = priority;
        }
    }


    private MethodDefinition createMyMethod(final ClassDefinition classDef, MethodDefinition symqleMethod, Type myType, final Map<String, TypeArgument> mapping) {
        final List<TypeParameter> myTypeParameterList = new ArrayList<TypeParameter>();
        // skip parameters, which are in mapping: they are inferred
        for (TypeParameter typeParameter: symqleMethod.getTypeParameters().list()) {
            if (mapping.get(typeParameter.getName()) == null) {
                // TODO new type parameter may hide class type parameter; rename if necessary
                myTypeParameterList.add(typeParameter);
            }
        }
        final TypeParameters myTypeParameters = new TypeParameters(myTypeParameterList);
        List<FormalParameter> myFormalParameters = new ArrayList<FormalParameter>();
        final List<FormalParameter> symqleFormalParameters = symqleMethod.getFormalParameters();
        for (int i=1; i< symqleFormalParameters.size(); i++) {
            final FormalParameter symqleFormalParameter = symqleFormalParameters.get(i);
            myFormalParameters.add(symqleFormalParameter.replaceParams(mapping));
        }
        Set<String> myModifiers = new HashSet<String>(symqleMethod.getOtherModifiers());
        myModifiers.add("abstract");
        myModifiers.remove("static");
        final BufferedReader reader = new BufferedReader(new StringReader(symqleMethod.getComment()));
        final CharArrayWriter charArrayWriter = new CharArrayWriter();
        final PrintWriter writer = new PrintWriter(charArrayWriter);
        try {
            for (String s = reader.readLine(); s != null; s = reader.readLine()) {
                // expecting at most one class type parameter
                if (!classDef.getTypeParameters().isEmpty() && s.contains("@param "+classDef.getTypeParameters().toString())) {
                    // skip this line
                    continue;
                }
                writer.println(s.replace("@param "+symqleMethod.getFormalParameters().get(0).getName(), "{@code this}"));
            }
            writer.close();
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException("Internal error", e);
        }

        final Pattern TRAILING_WHITESPACE = Pattern.compile("\\s$", Pattern.MULTILINE);

        final StringBuilder builder = new StringBuilder();
        builder.append(TRAILING_WHITESPACE.matcher(charArrayWriter.toString()).replaceAll(""));
        builder.append("    ");
        builder.append(symqleMethod.getAccessModifier())
                .append(" ")
                .append(Utils.format(myModifiers, "", " ", " "))
                .append(myTypeParameters)
                .append(symqleMethod.getResultType().replaceParams(mapping))
                .append(" ")
                .append(symqleMethod.getName())
                .append("(")
                .append(Utils.format(myFormalParameters, "", ", ", ""))
                .append(")")
                .append(Utils.format(symqleMethod.getThrownExceptions(), " throws ", ", ", ""))
                .append(";");
        final String body = builder.toString();
        final MethodDefinition method = MethodDefinition.parse(body, classDef);
        method.setSourceRef(symqleMethod.getSourceRef());
        return method;
    }


}
