package org.simqle.model;

import java.util.*;

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
    private final Set<String> publishedImports = new HashSet<String>();
    private final Set<String> internalImports = new HashSet<String>();
    private final Set<Type> mimics = new HashSet<Type>();



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

    public List<String> getPublishedImports() {
        return new ArrayList<String>(publishedImports);
    }

    public List<String> getInternalImports() {
        return new ArrayList<String>(internalImports);
    }

    public Set<Type> getMimics() {
        return new HashSet<Type>(mimics);
    }

    public void addPublishedImports(Collection<String> imports) {
        publishedImports.addAll(imports);
    }
    public void addInternalImports(Collection<String> imports) {
        internalImports.addAll(imports);
    }
    public void addMimics(Collection<Type> addedMimics)  throws ModelException {
        for (Type t: addedMimics) {
            // we can add type if it is the same or if its pairName is different;
            // cannot mimic the same class with different type parameters
            if (mimics.contains(t)) {
                return;
            }
            for (Type m: mimics) {
                if (m.getNameChain().get(0).getName().equals(t.getNameChain().get(0).getName())) {
                    throw new ModelException("Cannot mimic one class with different type parameters");
                }
            }
            mimics.add(t);
        }
    }

}
