package org.simqle;

/**
 * Created by IntelliJ IDEA.
 * User: lvovich
 * Date: 21.10.11
 * Time: 12:14
 * To change this template use File | Settings | File Templates.
 */
public class NullDataExtractor<T> implements DataExtractor<T> {
    public T extract(final Row row) {
        // must never get here
        throw new RuntimeException("Not implemented");
    }
}
