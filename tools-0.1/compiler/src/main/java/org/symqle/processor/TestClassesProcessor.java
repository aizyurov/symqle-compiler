package org.symqle.processor;

import org.symqle.model.*;
import org.symqle.parser.ParseException;
import org.symqle.parser.SimpleNode;
import org.symqle.parser.SyntaxTree;
import org.symqle.util.Utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: aizyurov
 * Date: 07.12.2013
 * Time: 16:37:03
 * To change this template use File | Settings | File Templates.
 */
public class TestClassesProcessor extends ModelProcessor {

    @Override
    protected void process(Model model) throws ModelException {
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
                    testInterface.addMethod(newClassTestMethod(classMethod, classDef, testInterface));
                    methodCount++;
                }
            }
            for (MethodDefinition method : symqle.getDeclaredMethods()) {
                final String accessModifier = method.getAccessModifier();
                if ("private".equals(accessModifier) || "protected".equals(accessModifier)) {
                    continue;
                }
                final TypeParameters typeParameters = method.getTypeParameters();
                final List<FormalParameter> formalParameters = method.getFormalParameters();
                for (int i=1; i<formalParameters.size(); i++) {
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
        System.err.println("Test classes completed: " + methodCount + " tests");
    }

    @Override
    protected Processor predecessor() {
        return new InterfaceJavadocProcessor();
    }

    private InterfaceDefinition newTestInterface(String testedClassName) {
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
            final SimpleNode simpleNode = Utils.createParser(source).SymqleInterfaceDeclaration();
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

    private MethodDefinition newSymqleTestMethod(MethodDefinition testedMethod, int parameterIndex, ClassDefinition testedClass, final InterfaceDefinition target) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        printWriter.println();
        printWriter.println("/**");
        final List<FormalParameter> formalParameters = testedMethod.getFormalParameters();
        printWriter.print(" * Test ");
            printWriter.print(testedClass.getName());
            printWriter.print(" as argument " + parameterIndex +" of ");
            printWriter.print(testedMethod.getTypeParameters()); printWriter.print(" ");
            printWriter.print(formalParameters.get(0).getType().getSimpleName());
        printWriter.print("#"); printWriter.print(testedMethod.getName());
        printWriter.print("(");
                    printWriter.print(Utils.format(formalParameters.subList(1, formalParameters.size()), "", ", ", "", new F<FormalParameter, String, RuntimeException>() {
                        @Override
                        public String apply(final FormalParameter formalParameter) {
                            return formalParameter.getType().toString();
                        }
                    }));
        printWriter.println(")");
        printWriter.println("*/");

        printWriter.print("void "); printWriter.print("test_"); printWriter.print(testedMethod.signature().replaceAll("[(,)]", "_")); printWriter.print(parameterIndex);
        printWriter.println("();");
        printWriter.flush();
        final String source = stringWriter.toString();
        return MethodDefinition.parseAbstract(source, target);
    }

    private MethodDefinition newClassTestMethod(MethodDefinition testedMethod, ClassDefinition testedClass, final InterfaceDefinition target) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        printWriter.println();
        printWriter.println("/**");
        final List<FormalParameter> formalParameters = testedMethod.getFormalParameters();
        printWriter.print(" * Test ");
        printWriter.print(testedClass.getName());
        printWriter.print("#"); printWriter.print(testedMethod.getName());
        printWriter.print("(");
                    printWriter.print(Utils.format(formalParameters, "", ", ", "", new F<FormalParameter, String, RuntimeException>() {
                        @Override
                        public String apply(final FormalParameter formalParameter) {
                            return formalParameter.getType().toString();
                        }
                    }));
        printWriter.println(")");
        printWriter.println("*/");

        printWriter.print("void "); printWriter.print("test_"); printWriter.print(testedMethod.signature().replaceAll("[(,]", "_").replaceAll("[)]", ""));
        printWriter.println("();");
        printWriter.flush();
        final String source = stringWriter.toString();
        return MethodDefinition.parseAbstract(source, target);
    }

    private boolean canBeParameter(ClassDefinition classDef, TypeParameters methodTypeParameters, FormalParameter formalParameter, Model model) throws ModelException {
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
