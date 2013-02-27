package org.simqle;

/**
 * Created by IntelliJ IDEA.
 * User: lvovich
 * Date: 11.10.11
 * Time: 15:39
 * To change this template use File | Settings | File Templates.
 */
public class Identifier implements Sql {
    private final String text;

    public Identifier(final String text) {
        this.text = text;
    }

    public String getSqlText() {
        return text;
    }

    public void setParameters(final SqlParameters p) {
        // do nothing
    }

    @Override
    public String toString() {
        return text;
    }
}
