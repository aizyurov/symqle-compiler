package org.simqle.sql;

import junit.framework.TestCase;
import org.simqle.Element;

import java.sql.SQLException;

/**
 * Created by IntelliJ IDEA.
 * User: lvovich
 * Date: 21.11.12
 * Time: 18:37
 * To change this template use File | Settings | File Templates.
 */
public class AliveTest extends TestCase {


    public void testFunction() throws Exception {
        final Person person = new Person();
        final StringColumn name = new StringColumn("name", person);
        final StringColumn surname = new StringColumn("surname", person);
        assertEquals("SELECT concat(T0.name, T0.surname) AS C0 FROM person AS T0", concat(name, surname).show());

    }

    private static class Person extends Table {
        private Person() {
            super("person");
        }


    }

    private static class StringColumn extends Column<String> {
        private StringColumn(final String name, final Table owner) {
            super(name, owner);
        }

        @Override
        public String value(final Element element) throws SQLException {
            return element.getString();
        }
    }

    private RoutineInvocation<String> concat(zValueExpression<String> v1, zValueExpression<String> v2) {
        return new Function<String>("concat") {
            @Override
            public String value(final Element element) throws SQLException {
                return element.getString();
            }
        }.apply(v1, v2);
    }
}
