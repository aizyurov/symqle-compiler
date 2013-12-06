package org.symqle.processor;

import org.symqle.model.ClassDefinition;
import org.symqle.model.FormalParameter;
import org.symqle.model.MethodDefinition;
import org.symqle.model.Model;
import org.symqle.model.ModelException;
import org.symqle.model.Type;
import org.symqle.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: aizyurov
 * Date: 02.12.2013
 * Time: 6:24:14
 * To change this template use File | Settings | File Templates.
 */
public class InitialImplementationProcessor extends ModelProcessor {

    @Override
    void process(Model model) throws ModelException {
        for (ClassDefinition classDef : model.getAllClasses()) {
            System.err.println("Initial implementation of " + classDef.getName());
            for (MethodDefinition myMethod: classDef.getAllMethods(model)) {
                final Set<String> modifiers = myMethod.getOtherModifiers();
                if (modifiers.contains("abstract") && modifiers.contains("volatile")) {
                    List<String> parameters = new ArrayList<String>();
                    parameters.add("this");
                    parameters.addAll(Utils.map(myMethod.getFormalParameters(), FormalParameter.NAME));
                    StringBuilder builder = new StringBuilder();
                    builder.append("{ ");
                    if (!myMethod.getResultType().equals(Type.VOID)) {
                        builder.append("return ");
                    }
                    builder.append("Symqle.").append(myMethod.getName()).append("(")
                            .append(Utils.format(parameters, "", ", ", ""))
                            .append("); }");
                    myMethod.implement("public", builder.toString(), true, true);
                }
            }
        }
    }
}
