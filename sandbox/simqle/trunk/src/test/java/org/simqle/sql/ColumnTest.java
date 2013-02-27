package org.simqle.sql;

import junit.framework.TestCase;
import org.simqle.CompositeSql;
import org.simqle.Element;
import org.simqle.Sql;
import org.simqle.SqlContext;
import org.simqle.SqlParameters;
import org.simqle.SqlTerminal;

import java.sql.SQLException;

/**
 * Created by IntelliJ IDEA.
 * User: lvovich
 * Date: 21.11.12
 * Time: 20:50
 * To change this template use File | Settings | File Templates.
 */
public class ColumnTest extends TestCase {


    public void testValueFunctionality() throws Exception {
        final LongColumn col = createId();
        Element element = new ElementAdapter() {
            @Override
            public Long getLong() throws SQLException {
                return 1L;
            }
        };
        assertEquals(Long.valueOf(1), col.value(element));
        assertEquals("SELECT T0.id AS C0 FROM person AS T0", col.show());

    }

    public void testSelectStatementFunctionality() throws Exception {
        final LongColumn col = createId();
        assertEquals("SELECT T0.id AS C0 FROM person AS T0", col.show());
    }

    private LongColumn createId() {
        return new LongColumn("id", person);
    }

    private LongColumn createAge() {
        return new LongColumn("age", person);
    }

    public void testSelectAll() throws Exception {
        final LongColumn col = createId();
        assertEquals("SELECT ALL T0.id AS C0 FROM person AS T0", col.all().show());

    }

    public void testSelectDistinct() throws Exception {
        final LongColumn col = createId();
        assertEquals("SELECT DISTINCT T0.id AS C0 FROM person AS T0", col.distinct().show());
    }

    public void testAsFunctionArgument() throws Exception {
        final String sql = new Function<Long>("abs") {
            @Override
            public Long value(final Element element) throws SQLException {
                return element.getLong();
            }
        }.apply(createId()).show();
        assertEquals("SELECT abs(T0.id) AS C0 FROM person AS T0", sql);
    }

    public void testAsFunctionMultipleArguments() throws Exception {
        final LongColumn column = createId();
        final String sql = new Function<Long>("max") {
            @Override
            public Long value(final Element element) throws SQLException {
                return element.getLong();
            }
        }.apply(column, column).show();
        assertEquals("SELECT max(T0.id, T0.id) AS C0 FROM person AS T0", sql);
    }

    public void testAsCondition() throws Exception {
        final LongColumn id = createId();
        final String sql = id.where(id.asCondition()).show();
        assertEquals("SELECT T0.id AS C0 FROM person AS T0 WHERE T0.id", sql);
    }

    public void testEq() throws Exception {
        final LongColumn id = createId();
        final LongColumn age = createAge();
        final String sql = id.where(id.eq(age)).show();
        assertEquals("SELECT T0.id AS C0 FROM person AS T0 WHERE T0.id = T0.age", sql);
    }

    public void testNe() throws Exception {
        final LongColumn column = createId();
        final LongColumn age = createAge();
        final String sql = column.where(column.ne(age)).show();
        assertEquals("SELECT T0.id AS C0 FROM person AS T0 WHERE T0.id <> T0.age", sql);
    }

    public void testGt() throws Exception {
        final LongColumn column = createId();
        final LongColumn age = createAge();
        final String sql = column.where(column.gt(age)).show();
        assertEquals("SELECT T0.id AS C0 FROM person AS T0 WHERE T0.id > T0.age", sql);
    }

    public void testGe() throws Exception {
        final LongColumn column = createId();
        final LongColumn age = createAge();
        final String sql = column.where(column.ge(age)).show();
        assertEquals("SELECT T0.id AS C0 FROM person AS T0 WHERE T0.id >= T0.age", sql);
    }

    public void testLt() throws Exception {
        final LongColumn column = createId();
        final LongColumn age = createAge();
        final String sql = column.where(column.lt(age)).show();
        assertEquals("SELECT T0.id AS C0 FROM person AS T0 WHERE T0.id < T0.age", sql);
    }

    public void testLe() throws Exception {
        final LongColumn column = createId();
        final LongColumn age = createAge();
        final String sql = column.where(column.le(age)).show();
        assertEquals("SELECT T0.id AS C0 FROM person AS T0 WHERE T0.id <= T0.age", sql);
    }

    public void testExceptAll() throws Exception {
        final LongColumn column = createId();
        final String sql = column.exceptAll(new LongColumn("age", person2)).show();
        assertEquals("SELECT T0.id AS C0 FROM person AS T0 EXCEPT ALL SELECT T1.age AS C0 FROM person AS T1", sql);
    }

    public void testExceptDistinct() throws Exception {
        final LongColumn column = createId();
        final String sql = column.exceptDistinct(new LongColumn("age", person2)).show();
        assertEquals("SELECT T0.id AS C0 FROM person AS T0 EXCEPT DISTINCT SELECT T1.age AS C0 FROM person AS T1", sql);
    }

    public void testUnionAll() throws Exception {
        final LongColumn column = createId();
        final String sql = column.unionAll(new LongColumn("age", person2)).show();
        assertEquals("SELECT T0.id AS C0 FROM person AS T0 UNION ALL SELECT T1.age AS C0 FROM person AS T1", sql);
    }

    public void testUnionDistinct() throws Exception {
        final LongColumn column = createId();
        final String sql = column.unionDistinct(new LongColumn("age", person2)).show();
        assertEquals("SELECT T0.id AS C0 FROM person AS T0 UNION DISTINCT SELECT T1.age AS C0 FROM person AS T1", sql);
    }

    public void testIntersectAll() throws Exception {
        final LongColumn column = createId();
        final String sql = column.intersectAll(new LongColumn("age", person2)).show();
        assertEquals("SELECT T0.id AS C0 FROM person AS T0 INTERSECT ALL SELECT T1.age AS C0 FROM person AS T1", sql);

    }

    public void testIntersectDistinct() throws Exception {
        final LongColumn column = createId();
        final String sql = column.intersectDistinct(new LongColumn("age", person2)).show();
        assertEquals("SELECT T0.id AS C0 FROM person AS T0 INTERSECT DISTINCT SELECT T1.age AS C0 FROM person AS T1", sql);

    }

    public void testUseSameTableInDistinct() throws Exception {
        final LongColumn column = createId();
        final LongColumn age = createAge();
        final String sql = column.intersectDistinct(age).show();
        assertEquals("SELECT T0.id AS C0 FROM person AS T0 INTERSECT DISTINCT SELECT T1.age AS C0 FROM person AS T1", sql);

    }

    private static class Person extends Table {
        private Person() {
            super("person");
        }
    }

    private static class Employee extends Table {
        private Employee() {
            super("employee");
        }
    }

    private static Person person = new Person();

    private static Person person2 = new Person();

    private static Employee employee = new Employee();

    private static class LongColumn extends Column<Long> {
        private LongColumn(final String name, final Table owner) {
            super(name, owner);
        }

        @Override
        public Long value(final Element element) throws SQLException {
            return element.getLong();
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

    public void testSelectForUpdate() throws Exception {
        final LongColumn col = createId();
        assertEquals("SELECT T0.id AS C0 FROM person AS T0 FOR UPDATE", col.forUpdate().show());
    }

    public void testSelectForReadOnly() throws Exception {
        final LongColumn col = createId();
        assertEquals("SELECT T0.id AS C0 FROM person AS T0 FOR READ ONLY", col.forReadOnly().show());
    }

    public void testExists() throws Exception {
        final LongColumn id  =  createId();
        // find all but the most old
        final LongColumn age2 = new LongColumn("age", person2);
        String sql = id.where(age2.exists()).show();
        assertEquals("SELECT T0.id AS C0 FROM person AS T0 WHERE EXISTS(SELECT T1.age FROM person AS T1)", sql);

    }

    public void testExistsWithCondition() throws Exception {
        final LongColumn id  =  createId();
        final LongColumn age = createAge();
        // find all but the most old
        final LongColumn age2 = new LongColumn("age", person2);
        String sql = id.where(age2.where(age2.gt(age)).exists()).show();
        assertEquals("SELECT T0.id AS C0 FROM person AS T0 WHERE EXISTS(SELECT T1.age FROM person AS T1 WHERE T1.age > T0.age)", sql);

    }

    public void testInAll() throws Exception {
        final LongColumn id  =  createId();
        // find all but the most old
        final LongColumn id2 = new LongColumn("id", employee);
        String sql = id.where(id.in(id2.all())).show();
        assertEquals("SELECT T0.id AS C0 FROM person AS T0 WHERE T0.id IN(SELECT ALL T1.id FROM employee AS T1)", sql);
    }

    public void testIn() throws Exception {
        final LongColumn id  =  createId();
        // find all but the most old
        final LongColumn id2 = new LongColumn("id", employee);
        String sql = id.where(id.in(id2)).show();
        assertEquals("SELECT T0.id AS C0 FROM person AS T0 WHERE T0.id IN(SELECT T1.id FROM employee AS T1)", sql);
    }

    public void testNotInAll() throws Exception {
        final LongColumn id  =  createId();
        // find all but the most old
        final LongColumn id2 = new LongColumn("id", employee);
        String sql = id.where(id.notIn(id2.all())).show();
        assertEquals("SELECT T0.id AS C0 FROM person AS T0 WHERE T0.id NOT IN(SELECT ALL T1.id FROM employee AS T1)", sql);
    }

    public void testInList() throws Exception {
        final LongColumn id  =  createId();
        // find all but the most old

        final zRowValueExpression<Long> expr = new LongParameter(1L);
        final zRowValueExpression<Long> expr2 = new LongParameter(2L);
        final zRowValueExpression<Long> expr3 = new LongParameter(3L);
        String sql = id.where(id.in(expr, expr2, expr3)).show();
        assertEquals("SELECT T0.id AS C0 FROM person AS T0 WHERE T0.id IN(?, ?, ?)", sql);
   }

    public void testNotInList() throws Exception {
        final LongColumn id  =  createId();
        // find all but the most old

        final zRowValueExpression<Long> expr = new LongParameter(1L);
        final zRowValueExpression<Long> expr2 = new LongParameter(2L);
        final zRowValueExpression<Long> expr3 = new LongParameter(3L);
        String sql = id.where(id.notIn(expr, expr2, expr3)).show();
        assertEquals("SELECT T0.id AS C0 FROM person AS T0 WHERE T0.id NOT IN(?, ?, ?)", sql);
   }

    public void testIsNull() throws Exception {
        final LongColumn id  =  createId();
        final LongColumn age = createAge();
        String sql = id.where(age.isNull()).show();
        assertEquals("SELECT T0.id AS C0 FROM person AS T0 WHERE T0.age IS NULL", sql);
   }

    public void testIsNotNull() throws Exception {
        final LongColumn id  =  createId();
        final LongColumn age = createAge();
        String sql = id.where(age.isNotNull()).show();
        assertEquals("SELECT T0.id AS C0 FROM person AS T0 WHERE T0.age IS NOT NULL", sql);
   }

    public void testOrderBy() throws Exception {
        final LongColumn id  =  createId();
        final LongColumn age = createAge();
        String sql = id.orderBy(age).show();
        assertEquals("SELECT T0.id AS C0 FROM person AS T0 ORDER BY T0.age", sql);
    }

    public void testOrderByNullsFirst() throws Exception {
        final LongColumn id  =  createId();
        final LongColumn age = createAge();
        String sql = id.orderBy(age.nullsFirst()).show();
        assertEquals("SELECT T0.id AS C0 FROM person AS T0 ORDER BY T0.age NULLS FIRST", sql);
    }

    public void testOrderByNullsLast() throws Exception {
        final LongColumn id  =  createId();
        final LongColumn age = createAge();
        String sql = id.orderBy(age.nullsLast()).show();
        assertEquals("SELECT T0.id AS C0 FROM person AS T0 ORDER BY T0.age NULLS LAST", sql);
    }

    public void testOrderByDesc() throws Exception {
        final LongColumn id  =  createId();
        final LongColumn age = createAge();
        String sql = id.orderBy(age.desc()).show();
        assertEquals("SELECT T0.id AS C0 FROM person AS T0 ORDER BY T0.age DESC", sql);
    }

    public void testOrderByAsc() throws Exception {
        final LongColumn id  =  createId();
        final LongColumn age = createAge();
        String sql = id.orderBy(age.asc()).show();
        assertEquals("SELECT T0.id AS C0 FROM person AS T0 ORDER BY T0.age ASC", sql);
    }

    public void testOperation() throws Exception {
        final LongColumn id  =  createId();
        final LongColumn age = createAge();
        String sql = mult(id, age).show();
        assertEquals("SELECT T0.id * T0.age AS C0 FROM person AS T0", sql);
    }



    private class LongParameter extends DynamicParameter<Long> {
        private final Long value;

        private LongParameter(final Long value) {
            this.value = value;
        }

        @Override
        protected void setParameter(final SqlParameters p) {
            p.setLong(value);
        }

        @Override
        public Long value(final Element element) throws SQLException {
            return element.getLong();
        }
    }

    private Value<Long> mult(final zValueExpressionPrimary<Long> v1, final zValueExpressionPrimary<Long> v2) {
        return new Value<Long>(
                new zValueExpression<Long>() {
                    @Override
                    public Sql z$create$zValueExpression(final SqlContext context) {
                        return new CompositeSql(v1.z$create$zValueExpressionPrimary(context),
                                SqlTerminal.ASTERISK,
                                v2.z$create$zValueExpressionPrimary(context));
                    }

                    @Override
                    public void z$prepare$zValueExpression(final SqlContext context) {
                        v1.z$prepare$zValueExpressionPrimary(context);
                        v2.z$prepare$zValueExpressionPrimary(context);
                    }

                    @Override
                    public Long value(final Element element) throws SQLException {
                        // TODO implement
                        throw new RuntimeException("Not implemented");
                    }
                }
        );
    }

}
