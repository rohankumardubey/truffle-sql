package com.fivetran.truffle.compile;

import com.oracle.truffle.api.dsl.TypeSystemReference;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.nodes.UnexpectedResultException;

/**
 * Base of all SQL expressions.
 */
@TypeSystemReference(SqlTypes.class)
@NodeInfo(description = "The abstract base node for all expressions")
abstract class ExprBase extends Node {
    /**
     * Compute the value of the expression. Called once for each expression in each row of a query.
     *
     * @param frame One row of data. Each FrameSlot corresponds to one column.
     * @return Result of evaluating the expression
     */
    abstract Object executeGeneric(VirtualFrame frame);

    boolean executeBoolean(VirtualFrame frame) throws UnexpectedResultException {
        return SqlTypesGen.expectBoolean(executeGeneric(frame));
    }

    long executeLong(VirtualFrame frame) throws UnexpectedResultException {
        return SqlTypesGen.expectLong(executeGeneric(frame));
    }

    double executeDouble(VirtualFrame frame) throws UnexpectedResultException {
        return SqlTypesGen.expectDouble(executeGeneric(frame));
    }

}
