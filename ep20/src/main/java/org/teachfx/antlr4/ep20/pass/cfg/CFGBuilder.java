package org.teachfx.antlr4.ep20.pass.cfg;

import org.apache.commons.lang3.tuple.Pair;
import org.teachfx.antlr4.ep20.ir.IRNode;

import java.util.*;

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

    private static final Set<String> cachedEdgeLinks = new HashSet<>();

    private void build(LinearIRBlock block){

        basicBlocks.add(BasicBlock.buildFromLinearBlock(block));
        block.getJumpEntries().ifPresent(entries -> {
            for(var dest : entries){
                var key = "%d-%d".formatted(block.getOrd(), dest);
                if (!cachedEdgeLinks.contains(key)) {
                    edges.add(Pair.of(block.getOrd(), dest));
                    cachedEdgeLinks.add("%d-%d".formatted(block.getOrd(), dest));
                }
            }
        });
        for(var successor : block.getSuccessors()){
            var key = "%d-%d".formatted(block.getOrd(), successor.getOrd());
            if (!cachedEdgeLinks.contains(key)) {
                cachedEdgeLinks.add(key);
                edges.add(Pair.of(block.getOrd(), successor.getOrd()));
            }
            build(successor);
        }
    }

    public CFG<IRNode> getCFG() {
        return cfg;
    }
}
