package org.simqle;

/**
 * Created by IntelliJ IDEA.
 * User: lvovich
 * Date: 11.10.11
 * Time: 15:43
 * To change this template use File | Settings | File Templates.
 */
public class Pair<First,Second> {
    private final First first;
    private final Second second;

    public Pair(final First first, final Second second) {
        this.first = first;
        this.second = second;
    }

    public First getFirst() {
        return first;
    }

    public Second getSecond() {
        return second;
    }

    public static <First,Second> Pair<First,Second> of(final First first, final Second second) {
        return new Pair<First, Second>(first, second);
    }
}
