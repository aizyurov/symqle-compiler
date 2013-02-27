package org.simqle.core;

import junit.framework.TestCase;
import org.simqle.SqlTerminal;

/**
 * Created by IntelliJ IDEA.
 * User: lvovich
 * Date: 27.06.12
 * Time: 20:14
 * To change this template use File | Settings | File Templates.
 */
public class SqlTerminalTest extends TestCase {

    public void test() throws Exception {
        final SqlTerminal[] values = SqlTerminal.values();
        for (SqlTerminal value: values) {
            System.out.println("\""+value+"\",");
        }
    }
}
