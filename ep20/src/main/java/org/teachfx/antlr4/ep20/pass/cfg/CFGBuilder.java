package org.teachfx.antlr4.ep20.pass.cfg;

import org.apache.commons.lang3.tuple.Pair;
import org.teachfx.antlr4.ep20.ir.IRNode;

import java.util.*;

public class CFGBuilder {
    private static final Set<String> cachedEdgeLinks = new HashSet<>();
    private final CFG<IRNode> cfg;
    private final Map<Integer, BasicBlock<IRNode>> basicBlocks;
    private final List<Pair<Integer, Integer>> edges;

    public CFGBuilder(List<LinearIRBlock> blockList) {
        basicBlocks = new HashMap<>();
        edges = new ArrayList<>();
        for (var funcLabelBlock : blockList) {
            build(funcLabelBlock);
        }
        cfg = new CFG<>(basicBlocks, edges);
    }

    private void build(LinearIRBlock block) {
        basicBlocks.put(block.getLabel().getSeq(), BasicBlock.buildFromLinearBlock(block));

    }

    public CFG<IRNode> getCFG() {
        return cfg;
    }
}
