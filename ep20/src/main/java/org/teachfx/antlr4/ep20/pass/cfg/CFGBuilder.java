package org.teachfx.antlr4.ep20.pass.cfg;

import org.apache.commons.lang3.tuple.Pair;
import org.teachfx.antlr4.ep20.ir.IRNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CFGBuilder {
    private CFG<IRNode> cfg;
    private List<BasicBlock<IRNode>> basicBlocks;
    private List<Pair<Integer,Integer>> edges;
    public CFGBuilder(List<LinearIRBlock> blockList){
        basicBlocks =new ArrayList<>();
        edges = new ArrayList<>();
        for (var funcLabelBlock : blockList) {
            build(funcLabelBlock);
        }
        cfg = new CFG<>(basicBlocks, edges);
    }

    private void build(LinearIRBlock block){
        basicBlocks.add(BasicBlock.buildFromLinearBlock(block));
        for(var successor : block.getSuccessors()){
            edges.add(Pair.of(block.getOrd(), successor.getOrd()));
            build(successor);
        }
    }

    public CFG<IRNode> getCFG() {
        return cfg;
    }
}
