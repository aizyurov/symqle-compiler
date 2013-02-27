package org.simqle;

import java.sql.SQLException;

/**
 * Represents a factory of Java Objects of JavaType class, which can construct them from a single
 * {@link Element}}.
 * @author Alexander Izyurov
 * @param <JavaType> the type of associated Java objects
  */
public interface DataExtractor<JavaType> {
    /**
     * Creates a JAvaType object frm Row data.
     * @param row the Row providing the data
     * @return constructed JAvaType object
     */
    JavaType extract(Row row) throws SQLException ;
}
