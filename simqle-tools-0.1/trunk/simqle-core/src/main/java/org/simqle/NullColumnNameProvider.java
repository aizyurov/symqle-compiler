package org.simqle;

import org.simqle.ColumnNameProvider;
import org.simqle.Identifier;

/**
 * Created by IntelliJ IDEA.
 * User: lvovich
 * Date: 07.11.11
 * Time: 17:10
 * To change this template use File | Settings | File Templates.
 */
public class NullColumnNameProvider extends ColumnNameProvider {
    @Override
    public Identifier getUniqueName() {
        return null;
    }
}
