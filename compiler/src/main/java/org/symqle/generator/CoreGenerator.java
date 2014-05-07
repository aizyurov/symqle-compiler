package org.symqle.generator;

import org.symqle.model.AbstractTypeDefinition;
import org.symqle.model.Model;

import java.util.Collection;

/**
 * Generates production Java sources for Symqle core.
 * @author lvovich
 */
public class CoreGenerator extends WriterGenerator {

    /**
     * Constructs with given package name.
     * @param packageName full package name, like org.symqle.core
     */
    public CoreGenerator(final String packageName) {
        super(packageName);
    }

    @Override
    protected final Collection<? extends AbstractTypeDefinition> processedTypes(final Model model) {
        return model.getAllTypes();
    }
}
