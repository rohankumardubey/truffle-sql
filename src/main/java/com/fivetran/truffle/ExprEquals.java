package com.fivetran.truffle;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.NodeInfo;

import java.util.Objects;

@NodeInfo(shortName = "=")
public abstract class ExprEquals extends ExprBinary {


    @Specialization
    protected boolean eq(boolean left, boolean right) {
        return left == right;
    }

    @Specialization
    protected boolean eq(long left, long right) {
        return left == right;
    }

    @Specialization
    protected boolean eq(double left, double right) {
        return left == right;
    }

    @Specialization
    protected boolean eq(SqlNull left, SqlNull right) {
        return false;
    }

    @Specialization
    protected boolean eq(String left, String right) {
        return Objects.equals(left, right);
    }

    @Specialization
    @CompilerDirectives.TruffleBoundary
    protected boolean eq(Object left, Object right) {
        return left == right;
    }
}
