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
    private final Map<Type, String> mimics = new HashMap<Type, String>();



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
        return new HashSet<Type>(mimics.keySet());
    }

    public void addPublishedImports(Collection<String> imports) {
        publishedImports.addAll(imports);
    }
    public void addInternalImports(Collection<String> imports) {
        internalImports.addAll(imports);
    }

    /**
     *
     * @param ancestor the virtual ancestor
     * @param ruleName the rule, which defines mimics. May be null/
     * @throws ModelException
     */
    public void addMimics(final Type ancestor, final String ruleName)  throws ModelException {
        // we can add type if it is the same or if its pairName is different;
            // cannot mimic the same class with different type parameters
            for (Type m: mimics.keySet()) {
                if (m.getNameChain().get(0).getName().equals(ancestor.getNameChain().get(0).getName()) && (!m.equals(ancestor))) {
                    throw new ModelException("Cannot mimic one class with different type parameters");
                }
            }
        // in case of double mimics the last one overwrites (thus ruleName will be set in ProductionDeclarationsProcessor if not set in ClassDeclarationsProcessor)
        mimics.put(ancestor, ruleName);
    }

    public String getRuleNameForMimics(Type type) {
        return mimics.get(type);
    }

}
