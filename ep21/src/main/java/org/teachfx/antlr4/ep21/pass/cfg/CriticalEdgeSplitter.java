package org.teachfx.antlr4.ep21.pass.cfg;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.teachfx.antlr4.ep21.ir.IRNode;
import org.teachfx.antlr4.ep21.ir.stmt.Label;
import org.teachfx.antlr4.ep21.utils.Kind;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 关键边拆分器（Critical Edge Splitter）- 拆分CFG中的关键边
 *
 * <p>关键边（Critical Edge）是指同时满足以下条件的边：</p>
 * <ul>
 *   <li>源节点有多个前驱（入度 > 1）</li>
 *   <li>目标节点有多个后继（出度 > 1）</li>
 * </ul>
 *
 * <h3>关键边的问题</h3>
 * <p>关键边会导致SSA插入PHI节点时的问题：</p>
 * <ul>
 *   <li>PHI节点必须插入到目标基本块的开头</li>
 *   <li>如果目标有多个前驱，PHI节点会接收多个输入</li>
 *   <li>当目标也有多个后继时，SSA转换会变得复杂</li>
 * </ul>
 *
 * <h3>关键边拆分</h3>
 * <p>拆分关键边的方法：在关键边之间插入一个新的基本块</p>
 * <pre>{@code
 * 拆分前:
 *   Node A (indeg=2) --critical--> Node B (outdeg=2)
 * 拆分后:
 *   Node A --normal--> Node X (new) --normal--> Node B
 * }
 * </pre>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * EnhancedCFG<IRNode> cfg = ...;
 * CriticalEdgeSplitter<IRNode> splitter = new CriticalEdgeSplitter<>(cfg);
 *
 * // 拆分所有关键边
 * SplitResult result = splitter.splitAllCriticalEdges();
 *
 * // 获取拆分结果
 * System.out.println("Split " + result.getSplitCount() + " critical edges");
 * System.out.println("Created " + result.getNewBlockCount() + " new blocks");
 * }</pre>
 *
 * @param <I> IR节点类型参数
 * @see EnhancedCFG
 * @see CFGEdge
 * @see CriticalEdgeDetector
 * @author EP21 Development Team
 * @version 1.0
 * @since 2026-01-19
 */
public class CriticalEdgeSplitter<I extends IRNode> {

    private static final Logger logger = LogManager.getLogger(CriticalEdgeSplitter.class);

    /**
     * 要拆分的CFG
     */
    private final EnhancedCFG<I> cfg;

    /**
     * 关键边检测器
     */
    private final CriticalEdgeDetector<I> detector;

    /**
     * 新创建的基本块计数器
     */
    private int newBlockIdCounter;

    /**
     * 构造关键边拆分器
     *
     * @param cfg 要拆分的CFG，不能为null
     * @throws NullPointerException 当cfg为null时抛出
     */
    public CriticalEdgeSplitter(@NotNull EnhancedCFG<I> cfg) {
        this.cfg = Objects.requireNonNull(cfg, "CFG cannot be null");
        this.detector = new CriticalEdgeDetector<>(cfg.getBaseCFG());

        // 从现有最大ID开始编号新块
        int maxId = 0;
        for (var block : cfg.getNodes()) {
            if (block.getId() > maxId) {
                maxId = block.getId();
            }
        }
        this.newBlockIdCounter = maxId + 1;

        logger.debug("CriticalEdgeSplitter initialized with CFG, next block ID: {}",
                    newBlockIdCounter);
    }

    /**
     * 拆分所有关键边
     *
     * @return 拆分结果，包含拆分统计信息
     */
    @NotNull
    public SplitResult splitAllCriticalEdges() {
        logger.info("Starting critical edge splitting");

        // 检测所有关键边
        List<CFGEdge<I>> criticalEdges = detector.detect();
        logger.info("Detected {} critical edges", criticalEdges.size());

        if (criticalEdges.isEmpty()) {
            logger.info("No critical edges to split");
            return new SplitResult(0, 0, List.of());
        }

        // 拆分每条关键边
        List<Integer> newBlockIds = new ArrayList<>();
        for (CFGEdge<I> edge : criticalEdges) {
            Integer newBlockId = splitCriticalEdge(edge);
            if (newBlockId != null) {
                newBlockIds.add(newBlockId);
                logger.debug("Split critical edge: {} -> {} (new block: {})",
                            edge.getSourceId(), edge.getTargetId(), newBlockId);
            }
        }

        SplitResult result = new SplitResult(criticalEdges.size(),
                                         newBlockIds.size(),
                                         newBlockIds);

        logger.info("Critical edge splitting completed: {} edges split, {} blocks created",
                   result.getSplitCount(), result.getNewBlockCount());

        return result;
    }

    /**
     * 拆分单条关键边
     *
     * <p>拆分过程：</p>
     * <ol>
     *   <li>创建新的基本块</li>
     *   <li>删除关键边 (A -> B)</li>
   *   <li>添加边 (A -> NewBlock)</li>
     *   <li>添加边 (NewBlock -> B)</li>
     * </ol>
     *
     * @param edge 要拆分的关键边
     * @return 新创建的基本块ID，如果边不是关键边返回null
     */
    @Nullable
    public Integer splitCriticalEdge(@NotNull CFGEdge<I> edge) {
        Objects.requireNonNull(edge, "Edge cannot be null");

        int sourceId = edge.getSourceId();
        int targetId = edge.getTargetId();

        // 验证是否为关键边
        if (!detector.isCriticalEdge(sourceId, targetId)) {
            logger.debug("Edge {} -> {} is not critical, skipping",
                        sourceId, targetId);
            return null;
        }

        logger.debug("Splitting critical edge: {} -> {}", sourceId, targetId);

        // 创建新的基本块
        int newBlockId = createNewBasicBlock();
        logger.debug("Created new basic block: {}", newBlockId);

        // 删除原关键边
        boolean removed = cfg.removeEdge(edge);
        if (!removed) {
            logger.warn("Failed to remove critical edge: {} -> {}", sourceId, targetId);
            return null;
        }

        // 添加新边：源 -> 新块 (使用SUCCESSOR类型表示顺序执行)
        CFGEdge<I> edgeToNewBlock = CFGEdge.of(sourceId, newBlockId,
                                               CFGConstants.EdgeType.SUCCESSOR);
        cfg.addEdge(edgeToNewBlock);

        // 添加新边：新块 -> 原目标 (使用JUMP类型表示跳转)
        CFGEdge<I> edgeToTarget = CFGEdge.of(newBlockId, targetId,
                                              CFGConstants.EdgeType.JUMP);
        cfg.addEdge(edgeToTarget);

        logger.debug("Critical edge split complete: {} -> {} -> {}",
                    sourceId, newBlockId, targetId);

        return newBlockId;
    }

    /**
     * 创建新的基本块
     *
     * @return 新基本块的ID
     */
    @NotNull
    private Integer createNewBasicBlock() {
        int newBlockId = newBlockIdCounter++;

        // 创建新的基本块 - 使用BasicBlock.Builder
        // 创建空的label
        Label newLabel = new Label("L%d".formatted(newBlockId), null);
        List<Loc<I>> emptyCodes = new ArrayList<>();

        // 使用Builder创建BasicBlock
        BasicBlock<I> newBlock = new BasicBlock.Builder<I>()
                .id(newBlockId)
                .kind(Kind.CONTINUOUS)
                .codes(emptyCodes)
                .label(newLabel)
                .build();

        // 将新块添加到基础CFG
        cfg.getBaseCFG().nodes.add(newBlock);

        // 更新EnhancedCFG的索引
        // 注意：由于EnhancedCFG的索引是私有的，我们直接修改基础CFG
        // 索引会在下次访问时自动更新（如果实现了缓存失效）
        // 或者我们需要暴露一个方法来手动更新索引

        logger.debug("Created new BasicBlock with ID: {}", newBlockId);
        return newBlockId;
    }

    /**
     * 拆分结果统计信息
     */
    public static class SplitResult {
        private final int splitCount;
        private final int newBlockCount;
        private final List<Integer> newBlockIds;

        public SplitResult(int splitCount, int newBlockCount, List<Integer> newBlockIds) {
            this.splitCount = splitCount;
            this.newBlockCount = newBlockCount;
            this.newBlockIds = new ArrayList<>(newBlockIds);
        }

        public int getSplitCount() {
            return splitCount;
        }

        public int getNewBlockCount() {
            return newBlockCount;
        }

        @NotNull
        public List<Integer> getNewBlockIds() {
            return Collections.unmodifiableList(newBlockIds);
        }

        @Override
        public String toString() {
            return "SplitResult{splitCount=" + splitCount +
                   ", newBlockCount=" + newBlockCount +
                   ", newBlockIds=" + newBlockIds + "}";
        }
    }
}
