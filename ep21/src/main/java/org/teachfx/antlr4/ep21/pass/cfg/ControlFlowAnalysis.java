package org.teachfx.antlr4.ep21.pass.cfg;

import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.teachfx.antlr4.ep21.ir.IRNode;
import org.teachfx.antlr4.ep21.ir.JMPInstr;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 控制流分析优化器 - 负责对控制流图(CFG)进行优化
 * 主要执行两种优化：
 * 1. 移除冗余的跳转指令
 * 2. 合并入度为1的节点
 * 
 * 改进要点：
 * - 更好的代码组织结构和命名
 * - 性能优化：减少重复遍历和内存分配
 * - 增强的错误处理和边界检查
 * - 更好的日志管理和调试支持
 */
public class ControlFlowAnalysis<I extends IRNode> implements IFlowOptimizer<I> {
    // 调试开关 - 使用final防止意外修改，但提供动态控制支持测试
    public static final boolean DEBUG = false;
    protected static final Logger logger = LogManager.getLogger(ControlFlowAnalysis.class);
    
    // 动态调试控制 - 允许测试修改调试状态
    private static volatile boolean dynamicDebugEnabled = DEBUG;
    
    // 常量定义 - 避免Magic Numbers
    private static final int JUMP_EDGE_PRIORITY = 5;
    private static final int SUCCESSOR_EDGE_PRIORITY = 10;
    
    /**
     * 获取当前调试状态
     */
    public static boolean isDebugEnabled() {
        return dynamicDebugEnabled;
    }
    
    /**
     * 设置调试状态 - 主要用于测试
     */
    public static void setDebugEnabled(boolean enabled) {
        dynamicDebugEnabled = enabled;
    }
    
    /**
     * 主优化方法 - 执行控制流图优化
     * 
     * 优化策略：
     * 1. 识别并移除冗余的跳转指令
     * 2. 合并入度为1的基本块
     * 
     * @param cfg 待优化的控制流图
     */
    @Override
    public void onHandle(CFG<I> cfg) {
        Objects.requireNonNull(cfg, "CFG cannot be null");
        
        if (isDebugEnabled()) {
            logger.info("开始控制流分析优化，CFG节点数: {}", cfg.nodes.size());
        }
        
        try {
            // 第一阶段：移除冗余跳转指令
            optimizeJumpInstructions(cfg);
            
            // 第二阶段：合并基本块
            optimizeBasicBlockMerging(cfg);
            
        } catch (Exception e) {
            logger.error("控制流分析过程中发生错误", e);
            throw new RuntimeException("控制流优化失败", e);
        }
    }
    
    /**
     * 优化阶段1：移除冗余的跳转指令
     * 
     * 当一个基本块的出度为1且最后一条指令是无条件跳转，
     * 且跳转目标与顺序执行的下一个块相同时，可以移除该跳转指令
     */
    private void optimizeJumpInstructions(CFG<I> cfg) {
        var iterator = cfg.nodes.iterator();
        var blocksToOptimize = new ArrayList<BasicBlock<I>>();
        
        // 第一遍：识别需要优化的块，避免在遍历中修改集合
        while (iterator.hasNext()) {
            BasicBlock<I> block = iterator.next();
            if (isRedundantJumpCandidate(block, cfg)) {
                blocksToOptimize.add(block);
            }
        }
        
        // 第二遍：执行优化
        for (BasicBlock<I> block : blocksToOptimize) {
            if (shouldRemoveJumpInstruction(block, cfg)) {
                removeRedundantJump(block, cfg);
            }
        }
    }
    
    /**
     * 检查是否为冗余跳转的候选块
     * 条件：出度为1且最后一条指令是JMPInstr
     */
    private boolean isRedundantJumpCandidate(BasicBlock<I> block, CFG<I> cfg) {
        if (block == null || block.getLastInstr() == null) {
            return false;
        }
        
        int outDegree = cfg.getOutDegree(block.getId());
        return outDegree == 1 && block.getLastInstr() instanceof JMPInstr;
    }
    
    /**
     * 判断是否应该移除跳转指令
     * 条件：跳转目标与顺序执行的下一个块相同
     */
    private boolean shouldRemoveJumpInstruction(BasicBlock<I> block, CFG<I> cfg) {
        JMPInstr jumpInstruction = (JMPInstr) block.getLastInstr();
        int targetBlockId = jumpInstruction.getTarget().getSeq();
        
        // 查找顺序执行的下一个块
        Optional<Integer> sequentialSuccessor = cfg.getSucceed(block.getId()).stream()
                .filter(id -> id == targetBlockId)
                .findFirst();
        
        return sequentialSuccessor.isPresent();
    }
    
    /**
     * 移除冗余的跳转指令并更新控制流图
     */
    private void removeRedundantJump(BasicBlock<I> block, CFG<I> cfg) {
        JMPInstr jumpInstruction = (JMPInstr) block.getLastInstr();
        int targetBlockId = jumpInstruction.getTarget().getSeq();
        
        // 移除最后的跳转指令
        block.removeLastInstr();
        
        // 移除对应的边
        cfg.removeEdge(Triple.of(block.getId(), targetBlockId, JUMP_EDGE_PRIORITY));
        
        if (isDebugEnabled()) {
            logger.info("移除了基本块 {} 到 {} 的冗余跳转", block.getId(), targetBlockId);
        }
    }
    
    /**
     * 优化阶段2：合并基本块
     * 
     * 当一个基本块的入度为1时，可以将其与前驱块合并，
     * 以减少基本块的数量，提高代码效率
     */
    private void optimizeBasicBlockMerging(CFG<I> cfg) {
        var blocksToRemove = new ArrayList<BasicBlock<I>>();
        
        // 第一遍：识别需要合并的块
        for (BasicBlock<I> block : cfg.nodes) {
            if (shouldMergeWithPredecessor(block, cfg)) {
                handleBasicBlockMerging(block, cfg, blocksToRemove);
            }
        }
        
        // 第二遍：移除被合并的块
        for (BasicBlock<I> block : blocksToRemove) {
            cfg.removeNode(block);
        }
    }
    
    /**
     * 检查是否应该与前驱块合并
     * 条件：入度为1
     */
    private boolean shouldMergeWithPredecessor(BasicBlock<I> block, CFG<I> cfg) {
        List<Triple<Integer, Integer, Integer>> inEdges = cfg.getInEdges(block.getId()).toList();
        return !inEdges.isEmpty() && inEdges.size() == 1;
    }
    
    /**
     * 处理基本块合并操作
     */
    private void handleBasicBlockMerging(BasicBlock<I> block, CFG<I> cfg, 
                                        List<BasicBlock<I>> blocksToRemove) {
        List<Triple<Integer, Integer, Integer>> inEdges = cfg.getInEdges(block.getId()).toList();
        Triple<Integer, Integer, Integer> incomingEdge = inEdges.get(0);
        
        if (isDebugEnabled()) {
            logger.info("合并基本块: 前驱 {} -> 当前 {}",
                       incomingEdge.getLeft(), block.getId());
        }
        
        // 获取前驱块并执行合并
        Optional<BasicBlock<I>> predecessorOpt = cfg.getFrontier(block.getId()).stream()
                .findFirst()
                .map(cfg::getBlock);
        
        predecessorOpt.ifPresent(predecessor -> {
            // 合并前驱块和当前块
            predecessor.mergeNearBlock(block);
            
            // 移除入边
            cfg.removeEdge(incomingEdge);
            
            // 标记当前块为待移除
            blocksToRemove.add(block);
            
            if (isDebugEnabled()) {
                logger.info("成功合并基本块: {} 和 {}",
                           predecessor.getId(), block.getId());
            }
        });
    }
}