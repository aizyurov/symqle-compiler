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
     * Executes a query, calling callback for each row.
     * @param query the query to execute
     * @param callback called for each row. The result set position is advanced by the value returned by the callback.
     */
    void query(Sql query, Callback<Row, SQLException> callback) throws SQLException;

    /**
     * Executes insert, update or delete statement.
     * @param statement the Sql statement to execute
     * @return number of affected rows
     */
    int execute(Sql statement) throws SQLException;

}
