package org.symqle.generator;

import org.symqle.model.AbstractTypeDefinition;
import org.symqle.model.Model;

import java.util.Collection;

/**
 * @author lvovich
 */
public class CoreGenerator extends WriterGenerator {

    public CoreGenerator(final String packageName) {
        super(packageName);
    }

    @Override
    protected Collection<? extends AbstractTypeDefinition> processedTypes(final Model model) {
        return model.getAllTypes();
    }
}
