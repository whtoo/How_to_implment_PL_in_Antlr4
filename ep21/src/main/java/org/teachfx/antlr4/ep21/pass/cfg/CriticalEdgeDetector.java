package org.teachfx.antlr4.ep21.pass.cfg;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.teachfx.antlr4.ep21.ir.IRNode;

import java.util.*;

/**
 * 关键边检测器（Critical Edge Detector）- 识别CFG中的所有关键边
 *
 * <p>关键边（Critical Edge）是指入度>1且出度>1的边。
 * 拆分关键边对SSA形式转换和其他优化Pass很重要，因为它简化了
 * PHI节点的插入和支配边界的计算。</p>
 *
 * <h3>关键边定义</h3>
 * <p>一条边 e = (u, v) 是关键边，如果：</p>
 * <ul>
 *   <li>u的入度 > 1（多个前驱）</li>
 *   <li>v的出度 > 1（多个后继）</li>
 * </ul>
 *
 * <h3>关键边的意义</h3>
 * <ul>
 *   <li>PHI节点插入：简化PHI节点插入点选择</li>
 *   <li>支配边界计算：简化支配边界的确定</li>
 *   <li>数据流分析：提高数据流分析的收敛速度</li>
 *   <li>优化Pass：简化循环优化、常量传播等Pass的实现</li>
 * </ul>
 *
 * <h3>检测算法</h3>
 * <p>时间复杂度：O(V + E)，其中V是基本块数量，E是边数量</p>
 * <ol>
 *   <li>遍历所有边，检查每条边的源节点和目标节点</li>
 *   <li>计算每个节点的入度和出度</li>
 *   <li>判断：入度>1 且 出度>1的边为关键边</li>
 * </ol>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 创建关键边检测器
 * CriticalEdgeDetector<IRNode> detector = new CriticalEdgeDetector<>(cfg);
 *
 * // 检测所有关键边
 * List<CFGEdge<IRNode>> criticalEdges = detector.detect();
 * System.out.println("检测到 " + criticalEdges.size() + " 条关键边");
 *
 * // 检查特定边是否为关键边
 * CFGEdge<IRNode> edge = CFGEdge.of(0, 1, CFGConstants.EdgeType.JUMP);
 * boolean isCritical = detector.isCriticalEdge(edge);
 * }</pre>
 *
 * @param <I> IR节点类型参数
 * @see CFGEdge
 * @see CFGConstants.EdgeType
 * @author EP21 Development Team
 * @version 1.0
 * @since 2026-01-19
 */
public class CriticalEdgeDetector<I extends IRNode> {

    private static final Logger logger = LogManager.getLogger(CriticalEdgeDetector.class);

    /**
     * CFG实例
     */
    private final CFG<I> cfg;

    /**
     * 节点入度缓存 - blockId -> 入度
     */
    private Map<Integer, Integer> inDegreeCache;

    /**
     * 节点出度缓存 - blockId -> 出度
     */
    private Map<Integer, Integer> outDegreeCache;

    /**
     * 从CFG构造关键边检测器
     *
     * @param cfg CFG实例，不能为null
     * @throws NullPointerException 当cfg为null时抛出
     */
    public CriticalEdgeDetector(@NotNull CFG<I> cfg) {
        this.cfg = Objects.requireNonNull(cfg, "CFG cannot be null");
        this.inDegreeCache = new HashMap<>();
        this.outDegreeCache = new HashMap<>();

        // 计算入度和出度
        computeDegrees();

        logger.debug("CriticalEdgeDetector created: {} blocks, {} edges",
                   cfg.nodes.size(), cfg.edges.size());
    }

    /**
     * 计算所有基本块的入度和出度
     */
    private void computeDegrees() {
        // 计算入度
        for (var block : cfg.nodes) {
            int blockId = block.getId();
            int inDeg = cfg.getInDegree(blockId);
            inDegreeCache.put(blockId, inDeg);
        }

        // 计算出度
        for (var block : cfg.nodes) {
            int blockId = block.getId();
            int outDeg = cfg.getOutDegree(blockId);
            outDegreeCache.put(blockId, outDeg);
        }

        logger.debug("Degrees computed - inDegree cache: {}, outDegree cache: {}",
                    inDegreeCache.size(), outDegreeCache.size());
    }

    /**
     * 检测所有关键边
     *
     * @return 关键边列表（不重复）
     */
    @NotNull
    public List<CFGEdge<I>> detect() {
        Set<String> detectedEdges = new HashSet<>();
        List<CFGEdge<I>> criticalEdges = new ArrayList<>();

        // 遍历所有边，检测关键边
        for (var edgeTriple : cfg.edges) {
            int sourceId = edgeTriple.getLeft();
            int targetId = edgeTriple.getMiddle();
            String edgeKey = sourceId + "->" + targetId;

            // 避免重复检测
            if (detectedEdges.contains(edgeKey)) {
                continue;
            }

            // 转换为CFGEdge
            CFGConstants.EdgeType edgeType = CFGConstants.EdgeType.fromWeight(edgeTriple.getRight());
            if (edgeType == null) {
                logger.warn("Edge with invalid weight: {} -> {}, weight: {}",
                            sourceId, targetId, edgeTriple.getRight());
                continue;
            }

            CFGEdge<I> edge = CFGEdge.fromTriple(edgeTriple);

            // 检查是否为关键边
            if (isCriticalEdge(sourceId, targetId)) {
                // 创建关键边版本（isCritical=true）
                CFGEdge<I> criticalEdge = edge.withCritical(true);

                criticalEdges.add(criticalEdge);
                detectedEdges.add(edgeKey);

                logger.debug("Critical edge detected: {} -> {} ({})",
                            sourceId, targetId, edgeType);
            }
        }

        logger.info("Critical edge detection completed: {} critical edges found",
                   criticalEdges.size());

        return Collections.unmodifiableList(criticalEdges);
    }

    /**
     * 判断指定边是否为关键边
     *
     * <p>关键边定义：入度>1且出度>1的边</p>
     *
     * @param sourceId 源节点ID
     * @param targetId 目标节点ID
     * @return true如果该边是关键边，false otherwise
     */
    public boolean isCriticalEdge(int sourceId, int targetId) {
        Integer inDegree = inDegreeCache.get(targetId);
        Integer outDegree = outDegreeCache.get(sourceId);

        if (inDegree == null || outDegree == null) {
            logger.warn("Degree cache missing for edge: {} -> {}", sourceId, targetId);
            return false;
        }

        boolean isCritical = inDegree > 1 && outDegree > 1;

        if (isCritical) {
            logger.debug("Edge {} -> {} is critical (inDegree: {}, outDegree: {})",
                        sourceId, targetId, inDegree, outDegree);
        }

        return isCritical;
    }

    /**
     * 检查CFGEdge是否为关键边
     *
     * @param edge CFGEdge实例
     * @return true如果该边是关键边，false otherwise
     */
    public boolean isCriticalEdge(@NotNull CFGEdge<I> edge) {
        Objects.requireNonNull(edge, "Edge cannot be null");
        return isCriticalEdge(edge.getSourceId(), edge.getTargetId());
    }

    /**
     * 获取检测统计信息
     *
     * @return 统计信息对象
     */
    @NotNull
    public DetectionStatistics getStatistics() {
        int totalEdges = cfg.edges.size();
        int criticalEdgeCount = 0;
        int nonCriticalEdgeCount = 0;

        // 统计关键边和非关键边
        for (var edgeTriple : cfg.edges) {
            if (isCriticalEdge(edgeTriple.getLeft(), edgeTriple.getMiddle())) {
                criticalEdgeCount++;
            } else {
                nonCriticalEdgeCount++;
            }
        }

        double criticalRatio = totalEdges > 0
                ? (double) criticalEdgeCount / totalEdges * 100.0
                : 0.0;

        return new DetectionStatistics(
                totalEdges,
                criticalEdgeCount,
                nonCriticalEdgeCount,
                criticalRatio
        );
    }

    /**
     * 检测统计信息
     */
    public static class DetectionStatistics {
        private final int totalEdges;
        private final int criticalEdgeCount;
        private final int nonCriticalEdgeCount;
        private final double criticalEdgePercentage;

        public DetectionStatistics(int totalEdges, int criticalEdgeCount,
                              int nonCriticalEdgeCount,
                              double criticalEdgePercentage) {
            this.totalEdges = totalEdges;
            this.criticalEdgeCount = criticalEdgeCount;
            this.nonCriticalEdgeCount = nonCriticalEdgeCount;
            this.criticalEdgePercentage = criticalEdgePercentage;
        }

        public int getTotalEdges() {
            return totalEdges;
        }

        public int getCriticalEdgeCount() {
            return criticalEdgeCount;
        }

        public int getNonCriticalEdgeCount() {
            return nonCriticalEdgeCount;
        }

        public double getCriticalEdgePercentage() {
            return criticalEdgePercentage;
        }

        @NotNull
        @Override
        public String toString() {
            return String.format(
                    "DetectionStatistics{totalEdges=%d, critical=%d (%.1f%%), nonCritical=%d}",
                    totalEdges,
                    criticalEdgeCount,
                    criticalEdgePercentage,
                    nonCriticalEdgeCount
            );
        }
    }
}
