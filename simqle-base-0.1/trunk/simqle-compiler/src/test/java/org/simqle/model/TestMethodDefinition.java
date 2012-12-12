package org.simqle.model;

import junit.framework.TestCase;

/**
 * @author lvovich
 */
public class TestMethodDefinition extends TestCase {

    public void testEllipsis() throws Exception {
        Model model = new Model();
        ClassDefinition simqle = model.getClassDef("Simqle");
        MethodDefinition.parse("public abstract RoutineInvocation<T> apply(ValueExpression<?>... arg);", simqle);
    }
}
