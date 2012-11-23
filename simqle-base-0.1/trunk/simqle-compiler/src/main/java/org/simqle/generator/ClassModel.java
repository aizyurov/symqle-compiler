package org.simqle.generator;

import org.simqle.model.ClassPair;
import org.simqle.model.Model;
import org.simqle.model.ModelException;
import org.simqle.model.Type;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: lvovich
 * Date: 24.09.12
 * Time: 14:18
 * To change this template use File | Settings | File Templates.
 */
public class ClassModel {
    private final ClassPair classPair;
    private final String packageName;
    private final Model model;


    public ClassModel(final ClassPair classPair, final String packageName, final Model model) {
        this.classPair = classPair;
        this.packageName = packageName;
        this.model = model;
    }

    public List<String> getImports() {
        Set<String> imports = new HashSet<String>(classPair.getInternalImports());
        imports.addAll(classPair.getPublishedImports());
        for (Type virtualAncestorType: classPair.getMimics()) {
            final ClassPair ancestor;
            try {
                ancestor = model.findClassPair(virtualAncestorType);
            } catch (ModelException e) {
                throw new RuntimeException("Internal error", e);
            }
            imports.addAll(ancestor.getPublishedImports());
        }
        return new ArrayList<String>(imports);
    }

    public ClassPair getClassPair() {
        return classPair;
    }

    public String getPackageName() {
        return packageName;
    }
}
