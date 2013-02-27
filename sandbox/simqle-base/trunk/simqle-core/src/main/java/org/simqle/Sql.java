package org.simqle;

/**
 * This interface represents text of a syntax element of SQL language, which may contain dynamic parameters.
 * The interface also provides values for the parameters.
 * T
 */
public interface Sql {

    /**
     * The text of this Sql, may contain dynamic parameters (?).
      * @return the text
     */
    String getSqlText();

    /**
     * Provide values for dynamic parameters.
     * @param p SqlParameters interface to write parameter values into
     */
    void setParameters(SqlParameters p);

}
