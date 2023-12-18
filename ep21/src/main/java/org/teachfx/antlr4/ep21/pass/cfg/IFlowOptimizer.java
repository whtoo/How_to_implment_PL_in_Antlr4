package org.teachfx.antlr4.ep21.pass.cfg;

import org.teachfx.antlr4.ep21.ir.IRNode;

public interface IFlowOptimizer<I extends IRNode> {
    public void onHandle(CFG<I> cfg);
}
