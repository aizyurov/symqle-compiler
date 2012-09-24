package org.simqle.generator;

import org.simqle.model.FactoryMethodModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by IntelliJ IDEA.
 * User: lvovich
 * Date: 24.09.12
 * Time: 15:18
 * To change this template use File | Settings | File Templates.
 */
public class FactoryModel {
    private final List<FactoryMethodModel> factoryMethods;
    private final String packageName;

    public FactoryModel(final List<FactoryMethodModel> factoryMethods, final String packageName) {
        this.factoryMethods = factoryMethods;
        this.packageName = packageName;
    }

    public List<String> getImports() {
        Set<String> imports = new TreeSet<String>();
        for (FactoryMethodModel method: factoryMethods) {
            imports.addAll(method.getImports());
        }
        return new ArrayList<String>(imports);
    }

    public List<String> getImplementationImports() {
        Set<String> imports = new TreeSet<String>();
        for (FactoryMethodModel method: factoryMethods) {
            imports.addAll(method.getImplementationImports());
        }
        return new ArrayList<String>(imports);
    }

    public List<FactoryMethodModel> getFactoryMethods() {
        return factoryMethods;
    }

    public String getPackageName() {
        return packageName;
    }
}
