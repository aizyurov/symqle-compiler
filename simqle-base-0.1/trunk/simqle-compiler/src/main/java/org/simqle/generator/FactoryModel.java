package org.simqle.generator;

import org.simqle.model.FactoryMethodModel;

import java.util.List;

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

    public List<FactoryMethodModel> getFactoryMethods() {
        return factoryMethods;
    }

    public String getPackageName() {
        return packageName;
    }
}
