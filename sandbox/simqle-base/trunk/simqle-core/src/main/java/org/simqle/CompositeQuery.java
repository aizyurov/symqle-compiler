/*
* Copyright Alexander Izyurov 2010
*/
package org.simqle;

import java.sql.SQLException;

/**
 * <br/>06.11.2011
 *
 * @author Alexander Izyurov
 */
public class CompositeQuery<T> extends CompositeSql implements Query<T> {

    final DataExtractor<T> extractor;

    public CompositeQuery(Query<T> first, Sql... other) {
        super(first, other);
        extractor = first;
    }

    /**
     * Creates a JAvaType object frm Row data.
     *
     * @param row the Row providing the data
     * @return constructed JAvaType object
     */
    public T extract(Row row) throws SQLException {
        return extractor.extract(row);
    }
}
