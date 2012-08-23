package org.simqle.util;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: lvovich
 * Date: 23.08.12
 * Time: 16:12
 * To change this template use File | Settings | File Templates.
 */
public class CallbackIteratorTest extends TestCase {

    public void testRegular() {
        final List<String> source = Arrays.asList("first", "second");
        runPositive(source);
    }

    public void testEmpty() {
        runPositive(Collections.<String>emptyList());
    }

    public void testStop() {
        final List<String> source = Arrays.asList("first", "second", "third");
        final List<String> result = new ArrayList<String>();
        new CallbackIterator<String, RuntimeException>(source).iterate(new Callback<String, RuntimeException>() {
            @Override
            public void call(final String arg) throws StopException {
                if ("third".equals(arg)) {
                    throw new StopException();
                } else {
                    result.add(arg);
                }
            }
        });
        assertEquals(Arrays.asList("first", "second"), result);

    }

    private void runPositive(final List<String> source) {
        final List<String> result = new ArrayList<String>();
        new CallbackIterator<String, RuntimeException>(source).iterate(new Callback<String, RuntimeException>() {
            @Override
            public void call(final String arg) throws StopException {
                result.add(arg);
            }
        });
        assertEquals(source, result);
    }

}
