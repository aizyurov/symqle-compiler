package org.simqle.model;

import junit.framework.TestCase;
import org.simqle.util.ModelUtils;

/**
 * @author lvovich
 */
public class TestMethodDefinition extends TestCase {

    public void testEllipsis() throws Exception {
        final Model model = ModelUtils.prepareModel();
        ClassDefinition simqle = model.getClassDef("Simqle");
        MethodDefinition.parse("public abstract RoutineInvocation<T> apply(ValueExpression<?>... arg);", simqle);
    }
}
