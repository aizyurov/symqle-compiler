package org.symqle.processor;

import org.symqle.model.ClassDefinition;
import org.symqle.model.InterfaceDefinition;
import org.symqle.model.Model;
import org.symqle.model.ModelException;
import org.symqle.util.Log;

import java.util.List;

/**
 * Last step in compilation. Just counts compilation statistics.
 * @author lvovich
 */
public class FinalizationProcessor extends ModelProcessor {

    @Override
    protected final void process(final Model model) throws ModelException {

        final List<InterfaceDefinition> allInterfaces = model.getAllInterfaces();
        int interfaceMethods = 0;
        for (final InterfaceDefinition definition : allInterfaces) {
            interfaceMethods += definition.getDeclaredMethods().size();
        }
        Log.info("Interfaces: " + allInterfaces.size());
        Log.info("Interface methods: " + interfaceMethods);
        final List<ClassDefinition> allClasses = model.getAllClasses();

        int classMethods = 0;
        int inheritanceRelations = 0;
        for (ClassDefinition classDefinition : allClasses) {
            classMethods += classDefinition.getDeclaredMethods().size();
            inheritanceRelations += classDefinition.getAllAncestors(model).size();
        }
        Log.info("Classes: " + allClasses.size());
        Log.info("Class methods: " + classMethods);
        Log.info("Inheritance relations: " + inheritanceRelations);
    }

    @Override
    protected final Processor predecessor() {
        return new TestClassesProcessor();
    }

}
