package org.teachfx.antlr4.ep21.pass.cfg;

import org.apache.commons.lang3.tuple.Triple;
import org.teachfx.antlr4.ep21.ir.IRNode;
import org.teachfx.antlr4.ep21.ir.stmt.CJMP;
import org.teachfx.antlr4.ep21.ir.stmt.JMP;

import java.util.*;

public class CFGBuilder {
    private static final Set<String> cachedEdgeLinks = new HashSet<>();
    private final CFG<IRNode> cfg;
    private final List<BasicBlock<IRNode>> basicBlocks;
    private final List<Triple<Integer, Integer,Integer>> edges;
    private final Set<LinearIRBlock> visitedBlocks;

    public CFGBuilder(LinearIRBlock startBlock) {
        basicBlocks = new ArrayList<>();
        edges = new ArrayList<>();
        visitedBlocks = new HashSet<>();

        var cachedEdgeLink = new HashSet<String>();

        build(startBlock,cachedEdgeLink);

        cfg = new CFG<>(basicBlocks, edges);
    }

    private void build(LinearIRBlock block,Set<String> cachedEdgeLinks) {
        // 检查块是否已经被访问过，防止无限递归
        if (visitedBlocks.contains(block)) {
            System.out.println("DEBUG CFGBuilder: Block ord=" + block.getOrd() + " already visited, skipping");
            return;
        }
        
        visitedBlocks.add(block);
        
        var currentBlock = BasicBlock.buildFromLinearBlock(block,basicBlocks);
        basicBlocks.add(currentBlock);
        
        // 日志验证：检查stmts列表状态
        var stmts = block.getStmts();
        System.out.println("DEBUG CFGBuilder: Processing block ord=" + block.getOrd() + ", stmts size=" + stmts.size());
        
        // 修复：提取currentOrd到方法作用域
        var currentOrd = block.getOrd();
        
        // 边界检查：确保stmts列表不为空再访问最后一个元素
        if (stmts.isEmpty()) {
            System.out.println("DEBUG CFGBuilder: Empty stmts list, but still need to check for control flow");
        } else {
            var lastInstr = stmts.get(stmts.size() - 1);
            System.out.println("DEBUG CFGBuilder: Last instruction type=" + lastInstr.getClass().getSimpleName() + ", ord=" + currentOrd);

            if (lastInstr instanceof JMP jmp) {
                var destBlock = jmp.getNext();
                var destOrd = destBlock.getOrd();
                String key = currentOrd + "-" + destOrd + "-" + 5;
                System.out.println("DEBUG CFGBuilder: JMP instruction - from " + currentOrd + " to " + destOrd);
                if (!cachedEdgeLinks.contains(key)) {
                    cachedEdgeLinks.add(key);
                    edges.add(Triple.of(currentOrd, destOrd,5));
                }
                // 递归构建跳转目标block
                build(destBlock, cachedEdgeLinks);
            } else if (lastInstr instanceof CJMP cjmp) {
                var thenBlock = cjmp.getThenBlock();
                var elseBlock = cjmp.getElseBlock();
                
                // 处理then分支
                var thenOrd = thenBlock.getOrd();
                String thenKey = currentOrd + "-" + thenOrd + "-" + 5;
                System.out.println("DEBUG CFGBuilder: CJMP instruction - from " + currentOrd + " to then " + thenOrd);
                if (!cachedEdgeLinks.contains(thenKey)) {
                    cachedEdgeLinks.add(thenKey);
                    edges.add(Triple.of(currentOrd, thenOrd,5));
                }
                build(thenBlock, cachedEdgeLinks);
                
                // 处理else分支
                var elseOrd = elseBlock.getOrd();
                String elseKey = currentOrd + "-" + elseOrd + "-" + 5;
                System.out.println("DEBUG CFGBuilder: CJMP instruction - from " + currentOrd + " to else " + elseOrd);
                if (!cachedEdgeLinks.contains(elseKey)) {
                    cachedEdgeLinks.add(elseKey);
                    edges.add(Triple.of(currentOrd, elseOrd,5));
                }
                build(elseBlock, cachedEdgeLinks);
            }
        }

        for (var successor : block.getSuccessors()){
            String key = currentOrd + "-" + successor.getOrd() + "-" + 10;
            System.out.println("DEBUG CFGBuilder: Successor edge - from " + currentOrd + " to " + successor.getOrd());
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
