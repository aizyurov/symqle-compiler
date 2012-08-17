package org.simqle.generator;

/**
 * Created by IntelliJ IDEA.
 * User: lvovich
 * Date: 27.06.12
 * Time: 14:57
 * To change this template use File | Settings | File Templates.
 */
class HierarchyTest$1 {
    private final String s;

    HierarchyTest$1(final String s) {
        this.s = s;
    }
}


public class HierarchyTest extends HierarchyTest$1 {
    private Comparable<String> cmp;

    public HierarchyTest(final String s) {
        super(s);
        cmp = new Comparable<String>() {
            @Override
            public int compareTo(final String o) {
                return 1;
            }
        };
    }


}

