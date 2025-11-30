package org.teachfx.antlr4.ep21.pass.cfg;

import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.teachfx.antlr4.ep21.ir.IRNode;
import org.teachfx.antlr4.ep21.ir.JMPInstr;
import org.teachfx.antlr4.ep21.utils.Kind;

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
    public static final boolean DEBUG = true;
    protected static final Logger logger = LogManager.getLogger(ControlFlowAnalysis.class);
    
    // 动态调试控制 - 允许测试修改调试状态
    private static volatile boolean dynamicDebugEnabled = DEBUG;
    
    // 使用CFGConstants中的常量，避免重复定义魔法数字
    
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
     * 移除冗余跳转指令并更新控制流图
     *
     * 改进特性：
     * - 完整的参数验证和错误处理
     * - 增强的类型安全检查
     * - 详细的日志记录和调试支持
     * - 优化的性能表现
     *
     * @param block 包含冗余跳转的基本块
     * @param cfg 控制流图实例
     * @throws IllegalArgumentException 当参数无效时抛出
     * @throws RuntimeException 当优化操作失败时抛出
     */
    private void removeRedundantJump(@NotNull BasicBlock<I> block, @NotNull CFG<I> cfg) {
        // 参数验证 - 防御性编程
        Objects.requireNonNull(block, "基本块不能为空");
        Objects.requireNonNull(cfg, "控制流图不能为空");
        
        if (block.isEmpty()) {
            throw new IllegalArgumentException("基本块不能为空");
        }
        
        try {
            // 获取并验证跳转指令
            JMPInstr jumpInstruction = getAndValidateJumpInstruction(block);
            int targetBlockId = getTargetBlockId(jumpInstruction);
            
            // 验证目标块的合法性
            BasicBlock<I> targetBlock = validateTargetBlockExists(cfg, targetBlockId);
            
            // 执行优化操作
            performJumpRemoval(block, targetBlockId, cfg);
            
            // 记录优化结果
            if (isDebugEnabled()) {
                logger.info("成功移除冗余跳转：基本块 {} -> {}, 目标块保持: {}",
                           block.getId(), targetBlockId, targetBlock.getId());
            }
            
        } catch (ClassCastException e) {
            logger.error("类型转换错误：基本块 {} 的最后指令不是 JMPInstr", block.getId(), e);
            throw new IllegalStateException("基本块的最后指令类型不正确", e);
        } catch (Exception e) {
            logger.error("移除冗余跳转时发生错误，块ID: {}", block.getId(), e);
            throw new RuntimeException("移除冗余跳转失败", e);
        }
    }
    
    /**
     * 获取并验证跳转指令的安全性
     */
    private JMPInstr getAndValidateJumpInstruction(@NotNull BasicBlock<I> block) {
        IRNode lastInstruction = block.getLastInstruction();
        
        if (!(lastInstruction instanceof JMPInstr)) {
            throw new IllegalArgumentException("基本块最后一条指令不是跳转指令");
        }
        
        return (JMPInstr) lastInstruction;
    }
    
    /**
     * 验证目标基本块存在且有效
     */
    private BasicBlock<I> validateTargetBlockExists(@NotNull CFG<I> cfg, int targetBlockId) {
        BasicBlock<I> targetBlock = cfg.getBlock(targetBlockId);
        
        if (targetBlock == null) {
            throw new IllegalArgumentException("跳转目标块不存在: " + targetBlockId);
        }
        
        return targetBlock;
    }
    
    /**
     * 获取目标块ID的辅助方法
     */
    private int getTargetBlockId(@NotNull JMPInstr jumpInstruction) {
        return jumpInstruction.getTarget().getSeq();
    }
    
    /**
     * 执行具体的跳转移除操作
     */
    private void performJumpRemoval(@NotNull BasicBlock<I> block, int targetBlockId, @NotNull CFG<I> cfg) {
        // 移除最后的跳转指令
        try {
            block.removeLastInstruction();
        } catch (Exception e) {
            logger.error("移除基本块 {} 的最后指令失败", block.getId(), e);
            throw new RuntimeException("移除跳转指令失败", e);
        }
        
        // 重要：不移除控制流边，因为控制流关系仍然存在
        // 只移除JMP指令，保留边连接，因为目标块仍然是顺序执行的下一个块
        // 将JUMP边类型转换为SUCCESSOR边类型，反映顺序执行关系
        
        // 终极修复：检查是否已存在任何类型的边，避免创建重复边
        boolean hasAnyEdge = cfg.edges.stream()
            .anyMatch(edge -> edge.getLeft() == block.getId() && edge.getMiddle() == targetBlockId);
            
        if (hasAnyEdge) {
            logger.debug("节点{}和{}之间已存在边，跳过转换操作", block.getId(), targetBlockId);
            // 更新基本块类型（如果适用）
            updateBlockKindAfterOptimization(block);
            return;
        }
        
        Triple<Integer, Integer, Integer> jumpEdgeToRemove =
            Triple.of(block.getId(), targetBlockId, CFGConstants.JUMP_EDGE_TYPE);
        
        logger.debug("准备转换跳转边为顺序边: {} -> {}, 权重: {} -> {}",
                    block.getId(), targetBlockId, CFGConstants.JUMP_EDGE_TYPE, CFGConstants.SUCCESSOR_EDGE_TYPE);
        
        // 移除JUMP边
        boolean edgeRemoved = cfg.removeEdge(jumpEdgeToRemove);
        
        if (edgeRemoved) {
            // 添加SUCCESSOR边来保持控制流连接
            Triple<Integer, Integer, Integer> successorEdgeToAdd =
                Triple.of(block.getId(), targetBlockId, CFGConstants.SUCCESSOR_EDGE_TYPE);
            
            cfg.edges.add(successorEdgeToAdd);
            logger.debug("成功添加顺序边: {} -> {}, 权重: {}",
                        block.getId(), targetBlockId, CFGConstants.SUCCESSOR_EDGE_TYPE);
        } else {
            logger.warn("尝试移除不存在的跳转边: {} -> {}", block.getId(), targetBlockId);
        }
        
        // 更新基本块类型（如果适用）
        updateBlockKindAfterOptimization(block);
    }
    
    /**
     * 优化后更新基本块类型
     */
    private void updateBlockKindAfterOptimization(@NotNull BasicBlock<I> block) {
        // 如果基本块现在没有跳转指令，更新其类型为连续执行模式
        if (!block.hasJumpInstruction()) {
            try {
                // 使用反射或直接访问来更新 kind 字段
                var kindField = BasicBlock.class.getDeclaredField("kind");
                kindField.setAccessible(true);
                kindField.set(block, Kind.CONTINUOUS);
            } catch (Exception e) {
                logger.debug("更新基本块类型失败，但这不是严重错误", e);
            }
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
            if (isDebugEnabled()) {
                logger.info("开始合并操作: 前驱块{} -> 当前块{}", predecessor.getId(), block.getId());
                logger.info("合并前 - 前驱块出边: {}", cfg.edges.stream()
                    .filter(edge -> edge.getLeft() == predecessor.getId())
                    .collect(java.util.stream.Collectors.toList()));
                logger.info("合并前 - 当前块出边: {}", cfg.edges.stream()
                    .filter(edge -> edge.getLeft() == block.getId())
                    .collect(java.util.stream.Collectors.toList()));
            }
            
            // 合并前驱块和当前块
            predecessor.mergeNearBlock(block);
            
            // 移除入边
            cfg.removeEdge(incomingEdge);
            
            // 终极修复：将当前块的出边转移到前驱块，但要检查是否已存在
            List<Triple<Integer, Integer, Integer>> outEdges = new ArrayList<>(cfg.edges.stream()
                .filter(edge -> edge.getLeft() == block.getId())
                .toList());
            
            if (isDebugEnabled()) {
                logger.info("需要转移的出边数量: {}", outEdges.size());
            }
            
            for (Triple<Integer, Integer, Integer> outEdge : outEdges) {
                // 检查前驱块是否已存在到目标块的边（任何类型）
                boolean hasExistingEdge = cfg.edges.stream()
                    .anyMatch(edge -> edge.getLeft() == predecessor.getId() &&
                                     edge.getMiddle() == outEdge.getMiddle());
                
                if (hasExistingEdge) {
                    logger.debug("前驱块{}到目标块{}的边已存在，跳过转移",
                               predecessor.getId(), outEdge.getMiddle());
                    // 只移除当前块的边，不添加新边
                    cfg.removeEdge(outEdge);
                    continue;
                }
                
                // 移除旧的边
                cfg.removeEdge(outEdge);
                // 添加新的边，源节点改为前驱块
                Triple<Integer, Integer, Integer> newEdge = Triple.of(
                    predecessor.getId(),
                    outEdge.getMiddle(),
                    outEdge.getRight()
                );
                cfg.edges.add(newEdge);
                logger.debug("转移边: {} -> {} 改为 {} -> {}",
                           block.getId(), outEdge.getMiddle(),
                           predecessor.getId(), outEdge.getMiddle());
            }
            
            // 标记当前块为待移除
            blocksToRemove.add(block);
            
            if (isDebugEnabled()) {
                logger.info("合并后 - 前驱块出边: {}", cfg.edges.stream()
                    .filter(edge -> edge.getLeft() == predecessor.getId())
                    .collect(java.util.stream.Collectors.toList()));
                logger.info("成功合并基本块: {} 和 {}",
                           predecessor.getId(), block.getId());
            }
        });
    }
}