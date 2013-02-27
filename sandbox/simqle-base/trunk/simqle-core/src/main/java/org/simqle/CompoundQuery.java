/*
* Copyright Alexander Izyurov 2010
*/
package org.simqle;


import java.sql.SQLException;

/**
 * An Query element as a sequence of Sql sub-elements.
 * @author Alexander Izyurov
 * @param <JavaType> the class of associated Java objects
 */
public class CompoundQuery<JavaType> implements Query<JavaType> {
    /**
     * The object factory of the composite.
     */
    private final DataExtractor<JavaType> dataExtractor;

    private final Sql sql;

    /**
     * Constructs Query from a sequence of Sql elements.
     * @param dataExtractor the DataExtractor to delegate extraction
     * @param sql the Sql to delegate Sql construction
     */
    public CompoundQuery(final DataExtractor<JavaType> dataExtractor,
                         final Sql sql) {
        this.dataExtractor = dataExtractor;
        this.sql = sql;
    }

    public String getSqlText() {
        return sql.getSqlText();
    }

    public void setParameters(final SqlParameters p) {
        sql.setParameters(p);
    }

    public JavaType extract(final Row row) throws SQLException {
        return dataExtractor.extract(row);
    }
}
