/*
   Copyright 2011-2014 Alexander Izyurov

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.package org.symqle.common;
*/

package org.symqle.util;

import junit.framework.TestCase;

import java.util.List;

public class TSortTest extends TestCase {
    public void testPlain() throws Exception {
        TSort<String> sort = new TSort<String>();
        sort.add("c", "a");
        sort.add("b", "a");
        sort.add("d", "b");
        sort.add("d", "c");
        final List<String> sorted = sort.sort();
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
            // expected
        }
    }

    public void testReSort() throws Exception {
        TSort<String> sort = new TSort<String>();
        sort.add("c", "a");
        sort.add("b", "a");
        sort.add("d", "b");
        sort.add("d", "c");
        final List<String> sorted = sort.sort();
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
