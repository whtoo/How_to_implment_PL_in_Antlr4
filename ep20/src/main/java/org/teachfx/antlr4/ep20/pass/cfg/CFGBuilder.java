package org.teachfx.antlr4.ep20.pass.cfg;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.teachfx.antlr4.ep20.ir.IRNode;
import org.teachfx.antlr4.ep20.ir.JMPInstr;
import org.teachfx.antlr4.ep20.ir.stmt.CJMP;
import org.teachfx.antlr4.ep20.ir.stmt.JMP;

import java.util.*;

public class CFGBuilder {
    private static final Set<String> cachedEdgeLinks = new HashSet<>();
    private final CFG<IRNode> cfg;
    private final List<BasicBlock<IRNode>> basicBlocks;
    private final List<Triple<Integer, Integer,Integer>> edges;

    public CFGBuilder(LinearIRBlock startBlock) {
        basicBlocks = new ArrayList<>();
        edges = new ArrayList<>();

        var cachedEdgeLink = new HashSet<String>();

        build(startBlock,cachedEdgeLink);

        cfg = new CFG<>(basicBlocks, edges);
    }

    private void build(LinearIRBlock block,Set<String> cachedEdgeLinks) {
        var currentBlock = BasicBlock.buildFromLinearBlock(block,basicBlocks);
        basicBlocks.add(currentBlock);
        var lastInstr = block.getStmts().get(block.getStmts().size() - 1);
        var currentOrd = block.getOrd();

        if (lastInstr instanceof JMP jmp) {
            var destOrd = jmp.getNext().getOrd();
            var key = currentOrd + "-" + destOrd + "-" + 5;
            if (!cachedEdgeLinks.contains(key)) {
                cachedEdgeLinks.add(key);
                edges.add(Triple.of(currentOrd, destOrd,5));
            }
        } else if (lastInstr instanceof CJMP cjmp) {
            var elseOrd = cjmp.getElseBlock().getOrd();
            var key = currentOrd + "-" + elseOrd + "-" + 5;
            if (!cachedEdgeLinks.contains(key)) {
                cachedEdgeLinks.add(key);
                edges.add(Triple.of(currentOrd, elseOrd,5));
            }
        }

        for (var successor : block.getSuccessors()){
            var key = currentOrd + "-" + successor.getOrd() + "-" + 10;
            if (!cachedEdgeLinks.contains(key)) {
                cachedEdgeLinks.add(key);
                edges.add(Triple.of(currentOrd, successor.getOrd(),10));
            }
            build(successor,cachedEdgeLinks);
        }
    }


    public CFG<IRNode> getCFG() {
        return cfg;
    }
}
