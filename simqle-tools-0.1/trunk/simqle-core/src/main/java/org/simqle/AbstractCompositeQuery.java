package org.simqle;

/**
 * Base class for Queries, which are constructed from Sql elements
 * Derived classes must implement {@link #extract(Row)} 
 */
public abstract class AbstractCompositeQuery<T> extends CompositeSql implements Query<T> {
    public AbstractCompositeQuery(Sql first, Sql... other) {
        super(first, other);
    }
}
