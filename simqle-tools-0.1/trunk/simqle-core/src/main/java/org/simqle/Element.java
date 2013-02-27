/*
* Copyright Alexander Izyurov 2010
*/
package org.simqle;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;

/**
 * An abstraction of a single column in a single row of result set. It isolates the value consumer from
 * details of position or label of the column and scrolling over result set. Unlike {@link java.sql.ResultSet}},
 * its methods return object wrappers rather than primitive values.
 * @author Alexander Izyurov
 * TODO add more methods (Blob etc.)
 */
public interface Element {
    /**
     * gets the value of Element as Boolean.
     * @return the value; <code>null</code> if the column value was SQL <code>NULL</code>
     * @throws SQLException the value is not convertable to Boolean
     */
    Boolean getBoolean() throws SQLException;

    /**
     * gets the value of Element as Byte.
     * @return the value; <code>null</code> if the column value was SQL <code>NULL</code>
     * @throws SQLException the value is not convertable to Byte
     */
    Byte getByte() throws SQLException;

    /**
     * gets the value of Element as Short.
     * @return the value; <code>null</code> if the column value was SQL <code>NULL</code>
     * @throws SQLException the value is not convertable to Short
     */
    Short getShort() throws SQLException;

    /**
     * gets the value of Element as Integer.
     * @return the value; <code>null</code> if the column value was SQL <code>NULL</code>
     * @throws SQLException the value is not convertable to Integer
     */
    Integer getInt() throws SQLException;

    /**
     * gets the value of Element as Long.
     * @return the value; <code>null</code> if the column value was SQL <code>NULL</code>
     * @throws SQLException the value is not convertable to Long
     */
    Long getLong() throws SQLException;

    /**
     * gets the value of Element as Float.
     * @return the value; <code>null</code> if the column value was SQL <code>NULL</code>
     * @throws SQLException the value is not convertable to Float
     */
    Float getFloat() throws SQLException;

    /**
     * gets the value of Element as Double.
     * @return the value; <code>null</code> if the column value was SQL <code>NULL</code>
     * @throws SQLException the value is not convertable to Double
     */
    Double getDouble() throws SQLException;

    /**
     * gets the value of Element as BigDecimal.
     * @return the value; <code>null</code> if the column value was SQL <code>NULL</code>
     * @throws SQLException the value is not convertable to BigDecimal
     */
    BigDecimal getBigDecimal() throws SQLException;

    /**
     * gets the value of Element as String.
     * @return the value; <code>null</code> if the column value was SQL <code>NULL</code>
     * @throws SQLException the value is not convertable to String
     */
    String getString() throws SQLException;

    /**
     * gets the value of Element as Date.
     * @return the value; <code>null</code> if the column value was SQL <code>NULL</code>
     * @throws SQLException the value is not convertable to Date
     */
    Date getDate() throws SQLException;

    /**
     * gets the value of Element as Time.
     * @return the value; <code>null</code> if the column value was SQL <code>NULL</code>
     * @throws SQLException the value is not convertable to Time
     */
    Time getTime() throws SQLException;

    /**
     * gets the value of Element as Timestamp.
     * @return the value; <code>null</code> if the column value was SQL <code>NULL</code>
     * @throws SQLException the value is not convertable to Timestamp
     */
    Timestamp getTimestamp() throws SQLException;

}
