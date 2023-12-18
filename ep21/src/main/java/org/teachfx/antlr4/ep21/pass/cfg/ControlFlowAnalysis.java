package org.teachfx.antlr4.ep21.pass.cfg;

import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.teachfx.antlr4.ep21.ir.IRNode;
import org.teachfx.antlr4.ep21.ir.JMPInstr;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

public class ControlFlowAnalysis<I extends IRNode> implements IFlowOptimizer<I> {
    public static Boolean DEBUG = false;
    protected static final Logger logger = LogManager.getLogger(ControlFlowAnalysis.class);

    @Override
    public void onHandle(CFG<I> cfg) {
        List<Triple<Integer,Integer,Integer>> needRemovedLink = new ArrayList<>();

        // 1. 遍历所有的控制图的节点
        // 2. 如果一个节点的入度为1，则该节点的前一个节点合并到该节点上
        // 3. 如果一个节点的出度为1，并且是JMP指令同时满足JMP的next和此节点的后续相同，则该节点最后一句跳转语句应该删除。
        // 根据上面三个条件，可以得到下面的代码

        for(var block : cfg.nodes) {
            var key = block.getId();
            var outDeg = cfg.getOutDegree(key);

            if (outDeg == 1 && block.getLastInstr() instanceof JMPInstr jmpInstr) {
                var targetBlockId = jmpInstr.getTarget().getSeq();
                AtomicBoolean needRemoveLastInstr = new AtomicBoolean(false);
                cfg.getSucceed(key).stream().filter(x -> x == targetBlockId).findFirst().ifPresent(next -> {
                    needRemoveLastInstr.set(true);
                });

                if (needRemoveLastInstr.get()) {
                    block.removeLastInstr();
                    cfg.removeEdge(Triple.of(key,targetBlockId, 5));
                }
            }
        }
        var removeQueue = new LinkedList<BasicBlock<I>>();

        for(var block : cfg.nodes) {
            var key = block.getId();

            var inDeg = cfg.getInEdges(key).toList();
            var isSrcSoloLink = (long) cfg.getFrontier(key).size() == 1;
            var isDestSoloLink = isSrcSoloLink && cfg.getOutDegree(inDeg.get(0).getLeft()) == 1;
            if (inDeg.size() == 1 && isDestSoloLink) {

                cfg.getFrontier(key).stream().findFirst().ifPresent(frontier -> {
                    var prevBlock = cfg.getBlock(frontier);
                    prevBlock.mergeNearBlock(block);
                    cfg.removeEdge(inDeg.get(0));
                    removeQueue.add(block);
                });

            }
        }

        for (var block : removeQueue) {
            cfg.removeNode(block);
        }
    }
}
