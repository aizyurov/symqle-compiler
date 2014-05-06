package org.symqle.model;

import junit.framework.TestCase;
import org.symqle.util.ModelUtils;

/**
 * @author lvovich
 */
public class TestMethodDefinition extends TestCase {

    public void testEllipsis() throws Exception {
        final Model model = ModelUtils.prepareModel();
        ClassDefinition symqle = model.getClassDef("Symqle");
        MethodDefinition.parse("public abstract RoutineInvocation<T> apply(ValueExpression<?>... arg);", symqle);
    }
}
