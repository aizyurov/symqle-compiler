package org.simqle.model;

import java.util.List;

/**
 * Represents a factory method, which implements production
 * To change this template use File | Settings | File Templates.
 */
public class FactoryMethodModel {
    private final List<String> imports;
    private final List<String> implementationImports;
    private final ProductionRule productionRule;
    private final MethodDeclaration methodDeclaration;

    public FactoryMethodModel(final List<String> imports, final List<String> implementationImports, final ProductionRule productionRule, final MethodDeclaration methodDeclaration) {
        this.imports = imports;
        this.implementationImports = implementationImports;
        this.productionRule = productionRule;
        this.methodDeclaration = methodDeclaration;
    }

    public String getName() {
        return methodDeclaration.getName();
    }

    public List<String> getImports() {
        return imports;
    }

    public List<String> getImplementationImports() {
        return implementationImports;
    }

    public ProductionRule getProductionRule() {
        return productionRule;
    }

    public MethodDeclaration getMethodDeclaration() {
        return methodDeclaration;
    }
}
