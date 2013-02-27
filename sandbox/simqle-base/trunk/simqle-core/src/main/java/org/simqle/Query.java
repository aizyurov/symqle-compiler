package org.simqle;

/**
 * This interface extends Sql and DataExtractor<JavaType>.
 * Instances of this interface are associated with Sql syntax elements, which can appear in
 * SELECT clause. These elements provide Query, which can construct objects of JavaType class
 * from the values returned in result set.
 * @author Alexander Izyurov
 * @version 0.1
 * @param <JavaType> the type of associated Java objects
 */
public interface Query<JavaType> extends Sql, DataExtractor<JavaType> {
}
