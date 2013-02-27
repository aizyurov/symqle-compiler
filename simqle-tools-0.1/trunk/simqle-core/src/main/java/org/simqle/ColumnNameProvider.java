package org.simqle;

/**
 * Created by IntelliJ IDEA.
 * User: lvovich
 * Date: 20.10.11
 * Time: 19:54
 * To change this template use File | Settings | File Templates.
 */
public class ColumnNameProvider {

    private int counter = 0;

    public Identifier getUniqueName() {
        return new Identifier("C"+(counter++));
    }
}
