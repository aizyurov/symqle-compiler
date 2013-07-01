package org.symqle.model;

/**
 * Created by IntelliJ IDEA.
 * User: aizyurov
 * Date: 25.11.2012
 * Time: 20:26:18
 * To change this template use File | Settings | File Templates.
 */
public class Either<L, R> {
    private final L l;
    private final R r;
    private boolean isLeft;

    private Either(L l, R r, boolean left) {
        this.l = l;
        this.r = r;
        isLeft = left;
    }

    public L asLeft() {
        if (!isLeft) {
            throw new IllegalStateException("is not left");
        }
        return l;
    }
    public R asRight() {
        if (isLeft) {
            throw new IllegalStateException("is left");
        }
        return r;
    }

    public static <L, R> Either<L, R> left(L l) {
        return new Either<L,R>(l, null, true);
    }

    public static <L, R> Either<L, R> right(R r) {
        return new Either<L,R>(null, r, false);
    }
}
