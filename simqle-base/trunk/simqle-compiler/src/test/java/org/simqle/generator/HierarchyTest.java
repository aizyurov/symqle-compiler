package org.simqle.generator;

/**
 * Created by IntelliJ IDEA.
 * User: lvovich
 * Date: 27.06.12
 * Time: 14:57
 * To change this template use File | Settings | File Templates.
 */
class HierarchyTest$ {
    private final String s;

    HierarchyTest$(final String s) {
        this.s = s;
    }
}


public class HierarchyTest extends HierarchyTest$ {

    public HierarchyTest(final String s) {
        super(s);
    }
}

