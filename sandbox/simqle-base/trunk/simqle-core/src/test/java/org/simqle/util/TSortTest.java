package org.simqle.util;

import junit.framework.TestCase;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: lvovich
 * Date: 27.10.11
 * Time: 19:14
 * To change this template use File | Settings | File Templates.
 */
public class TSortTest extends TestCase {
    public void testPlain() throws Exception {
        TSort<String> sort = new TSort<String>();
        sort.add("c", "a");
        sort.add("b", "a");
        sort.add("d", "b");
        sort.add("d", "c");
        final List<String> sorted = sort.sort();
        System.out.println(sorted);
        assertEquals(sorted.get(0), "a");
        assertEquals(sorted.get(3), "d");
    }

    public void testReverse() throws Exception {
        TSort<String> sort = new TSort<String>();
        sort.add("d", "c");
        sort.add("d", "b");
        sort.add("b", "a");
        sort.add("c", "a");
        final List<String> sorted = sort.sort();
        System.out.println(sorted);
        assertEquals(sorted.get(0), "a");
        assertEquals(sorted.get(3), "d");
    }

    public void testRandom() throws Exception {
        TSort<String> sort = new TSort<String>();
        sort.add("c", "a");
        sort.add("d", "c");
        sort.add("d", "b");
        sort.add("b", "a");
        final List<String> sorted = sort.sort();
        System.out.println(sorted);
        assertEquals(sorted.get(0), "a");
        assertEquals(sorted.get(3), "d");
    }

    public void testCycle() throws Exception {
        TSort<String> sort = new TSort<String>();
        sort.add("c", "a");
        sort.add("c", "b");
        sort.add("d", "b");
        sort.add("b", "d");
        final List<String> sorted;
        try {
            sorted = sort.sort();
            fail("Exception expected but not thrown");
        } catch (IllegalStateException e) {
            System.out.println(e.getMessage());
        }
    }

    public void testReSort() throws Exception {
        TSort<String> sort = new TSort<String>();
        sort.add("c", "a");
        sort.add("b", "a");
        sort.add("d", "b");
        sort.add("d", "c");
        final List<String> sorted = sort.sort();
        System.out.println(sorted);
        assertEquals(sorted.get(0), "a");
        assertEquals(sorted.get(3), "d");
        if (sorted.get(1).equals("c")) {
            sort.add("c", "b");
            final List<String> reSorted = sort.sort();
            assertEquals(reSorted.get(1), "b");
        } else {
            sort.add("b", "c");
            final List<String> reSorted = sort.sort();
            assertEquals(reSorted.get(1), "c");
        }
    }
    
}
