package org.symqle.processor;

import org.symqle.model.*;
import org.symqle.parser.ParseException;
import org.symqle.parser.SimpleNode;
import org.symqle.parser.SymqleParser;
import org.symqle.parser.SyntaxTree;
import org.symqle.util.Log;
import org.symqle.util.Utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

/**
 * Constructs *TestSet interfaces and puts to the model.
 */
public class TestClassesProcessor extends ModelProcessor {

    @Override
    protected final void process(final Model model) throws ModelException {
        final ClassDefinition symqle = model.getClassDef("Symqle");
        int methodCount = 0;
        for (ClassDefinition classDef : model.getAllClasses()) {
            if (!classDef.isPublic()) {
                continue;
            }
            final InterfaceDefinition testInterface = newTestInterface(classDef.getName());
            for (MethodDefinition classMethod: classDef.getDeclaredMethods()) {
                if (classMethod.isPublic() && !classMethod.getOtherModifiers().contains("abstract")
                        && !classMethod.getName().startsWith("z$")) {
                    if (model.mayHaveSymqleImplementation(classMethod)) {
                    testInterface.addMethod(newClassTestMethod(classMethod, classDef, testInterface));
                    methodCount++;
                    } else {
                        Log.debug("NOT a Symqle method: " + classMethod.signature() + " in " + classDef.getName());
                    }
                }
            }
            for (MethodDefinition method : symqle.getDeclaredMethods()) {
                final String accessModifier = method.getAccessModifier();
                if ("private".equals(accessModifier) || "protected".equals(accessModifier)) {
                    continue;
                }
                final TypeParameters typeParameters = method.getTypeParameters();
                final List<FormalParameter> formalParameters = method.getFormalParameters();
                for (int i = 1; i < formalParameters.size(); i++) {
                    final FormalParameter formalParameter = formalParameters.get(i);
                    if (canBeParameter(classDef, typeParameters, formalParameter, model)) {
                        testInterface.addMethod(newSymqleTestMethod(method, i, classDef, testInterface));
                        methodCount++;
                    }
                }
            }
            if (testInterface.getDeclaredMethods().size() > 0) {
                model.addTestInterface(testInterface);
            }
        }
        Log.info("Test classes completed: " + methodCount + " tests");
    }

    @Override
    protected final Processor predecessor() {
        return new InterfaceJavadocProcessor();
    }

    private InterfaceDefinition newTestInterface(final String testedClassName) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        printWriter.println();
        printWriter.println("/**");
        printWriter.print(" * Tests "); printWriter.print(testedClassName); printWriter.println(" class");
        printWriter.println("*/");
        printWriter.println();
        printWriter.print("public interface "); printWriter.print(testedClassName); printWriter.println("TestSet {");
        printWriter.println("}");
        printWriter.flush();
        final String source = stringWriter.toString();
        try {
            final SimpleNode simpleNode = SymqleParser.createParser(source).SymqleInterfaceDeclaration();
            SyntaxTree syntaxTree = new SyntaxTree(simpleNode, source);
            return new InterfaceDefinition(syntaxTree);
        } catch (ParseException e) {
            throw new RuntimeException("Internal error, source: " + source, e);
        } catch (GrammarException e) {
            throw new RuntimeException("Internal error, source: " + source, e);
        } catch (RuntimeException e) {
            throw new RuntimeException("Internal error, source: " + source, e);
        }
    }

    private MethodDefinition newSymqleTestMethod(final MethodDefinition testedMethod,
                                                 final int parameterIndex,
                                                 final ClassDefinition testedClass,
                                                 final InterfaceDefinition target) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        printWriter.println();
        printWriter.println("/**");
        final List<FormalParameter> formalParameters = testedMethod.getFormalParameters();
        printWriter.print(" * Test ");
        printWriter.print(testedClass.getName());
        printWriter.print(" as argument " + parameterIndex + " of ");
        printWriter.print(testedMethod.getTypeParameters()); printWriter.print(" ");
        printWriter.print(formalParameters.get(0).getType().getSimpleName());
        printWriter.print("#"); printWriter.print(testedMethod.getName());
        printWriter.print("(");
        printWriter.print(Utils.format(formalParameters.subList(1, formalParameters.size()),
                    "",
                    ", ",
                    "",
                    new F<FormalParameter, String, RuntimeException>() {
                        @Override
                        public String apply(final FormalParameter formalParameter) {
                            return formalParameter.getType().toString();
                        }
                    }));
        printWriter.println(")");
        printWriter.println("*/");

        printWriter.print("void ");
        printWriter.print("test_");
        printWriter.print(testedMethod.signature().replaceAll("[(,)]", "_").replaceAll("\\[|\\]", ""));
        printWriter.print(parameterIndex);
        printWriter.println("() throws Exception;");
        printWriter.flush();
        final String source = stringWriter.toString();
        return MethodDefinition.parseAbstract(source, target);
    }

    private MethodDefinition newClassTestMethod(final MethodDefinition testedMethod,
                                                final ClassDefinition testedClass,
                                                final InterfaceDefinition target) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        printWriter.println();
        printWriter.println("/**");
        final List<FormalParameter> formalParameters = testedMethod.getFormalParameters();
        printWriter.print(" * Test ");
        printWriter.print(testedClass.getName());
        printWriter.print("#"); printWriter.print(testedMethod.getName());
        printWriter.print("(");
        printWriter.print(Utils.format(formalParameters, "", ", ", "",
                new F<FormalParameter, String, RuntimeException>() {
                        @Override
                        public String apply(final FormalParameter formalParameter) {
                            return formalParameter.getType().toString();
                        }
                    }));
        printWriter.println(")");
        printWriter.println("*/");

        printWriter.print("void ");
        printWriter.print("test_");
        printWriter.print(testedMethod.signature().replaceAll("[(,]", "_").replaceAll("\\)|\\[|\\]", ""));
        printWriter.println("() throws Exception;");
        printWriter.flush();
        final String source = stringWriter.toString();
        return MethodDefinition.parseAbstract(source, target);
    }

    private boolean canBeParameter(final ClassDefinition classDef,
                                   final TypeParameters methodTypeParameters,
                                   final FormalParameter formalParameter,
                                   final Model model) throws ModelException {
        for (Type type: classDef.getAllAncestors(model)) {
            final Type argType = formalParameter.getType();
            if (!argType.getSimpleName().equals(type.getSimpleName())) {
                continue;
            }
            final Map<String, TypeArgument> replacements = methodTypeParameters.inferTypeArguments(argType, type);
            final Type requiredType = argType.replaceParams(replacements);
            if (requiredType.equals(type)) {
                return true;
            }
        }
        return false;
    }

}
