/*
* Copyright Alexander Izyurov 2010
*/
package org.simqle;

import java.sql.SQLException;

/**
 * <br/>07.12.2010
 *
 * @author Alexander Izyurov
 */
public interface Database {

    /**
     * Executes a query, transforms the returned tuples to type T and calls callback for each tuple.
     * @param query the query to execute
     * @param callback called for each row. The result set position is advanced by the value returned by the callback.
     * @param <T> The type to which tuples are transformed.
     */
    <T> void query(Sql query, IteratorCallback<Row> callback) throws SQLException;

    /**
     * Executes insert, update or delete statement.
     * @param statement the Sql statement to execute
     * @return number of affected rows
     */
    int update(Sql statement) throws SQLException;

    /**
     * The dialect to use with this Database. Available dialects are defined in Simqle configuration.
     * @return the name of dialect. If the Database returns null, Generic dialect may be used.
     */
    String getDialect();

}
