package com.fivetran.truffle.compile;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;

/**
 * An expression that receives rows.
 *
 * Could transform rows, send them back to the user, or write them to a file somewhere.
 */
public abstract class RowSink extends Node implements LateBind {
    public abstract void executeVoid(VirtualFrame frame);
}