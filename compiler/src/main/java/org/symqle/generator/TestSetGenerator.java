package org.symqle.generator;

import org.symqle.model.AbstractTypeDefinition;
import org.symqle.model.Model;

import java.util.Collection;

/**
 * Generates test interfaces from Model.
 * @author lvovich
 */
public class TestSetGenerator extends WriterGenerator {

    /**
     * Constructs with given package name.
     * @param packageName full package name, like org.symqle.core
     */
    public TestSetGenerator(final String packageName) {
        super(packageName);
    }

    @Override
    protected final Collection<? extends AbstractTypeDefinition> processedTypes(final Model model) {
        return model.getTestInterfaces();
    }
}
