package org.simqle.model;

/**
 * Simqle classes are always in pairs of (base class, extension)
 * Base class extends interfaces as declared in language definition
 * and implements methods declared there.
 * extension implements interfaces and methods accessible by 'mimics'
 * statements
 * User: lvovich
 * Date: 27.06.12
 * Time: 15:41
 */
public class ClassPair {
    private final ClassDefinition base;
    private final ClassDefinition extension;

    public ClassPair(final ClassDefinition base, final ClassDefinition extension) {
        this.base = base;
        this.extension = extension;
    }

    public ClassDefinition getBase() {
        return base;
    }

    public ClassDefinition getExtension() {
        return extension;
    }
}
