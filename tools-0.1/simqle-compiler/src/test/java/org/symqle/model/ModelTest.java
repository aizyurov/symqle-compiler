package org.symqle.model;

import junit.framework.TestCase;
import org.symqle.util.ModelUtils;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: aizyurov
 * Date: 02.02.2013
 * Time: 11:52:02
 * To change this template use File | Settings | File Templates.
 */
public class ModelTest extends TestCase {

    public void testConstructor() throws Exception {
        final Model model = ModelUtils.prepareModel();
        assertEquals(2, model.getAllClasses().size());
        assertEquals(1, model.getAllInterfaces().size());
        final ClassDefinition simqle = model.getClassDef("Simqle");
        assertNotNull(simqle);
        final ClassDefinition genericDialect = model.getClassDef("GenericDialect");
        final InterfaceDefinition dialect = model.getInterface("Dialect");
        final List<Type> interfaces = genericDialect.getImplementedInterfaces();
        assertEquals(1, interfaces.size());
        assertEquals(dialect.getType(), interfaces.get(0));

    }

}
