package org.simqle.generator;

import org.simqle.model.*;

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
    private final ClassDefinition classDef;
    private final String packageName;
    private final Model model;


    public ClassModel(final ClassDefinition classPair, final String packageName, final Model model) {
        this.classDef = classPair;
        this.packageName = packageName;
        this.model = model;
    }

    public ClassDefinition getClassDef() {
        return classDef;
    }

    public String getPackageName() {
        return packageName;
    }
}
