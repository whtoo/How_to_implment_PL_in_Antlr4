package org.teachfx.antlr4.ep21.analysis.dataflow;

import org.teachfx.antlr4.ep21.ir.IRNode;
import org.teachfx.antlr4.ep21.pass.cfg.BasicBlock;
import org.teachfx.antlr4.ep21.pass.cfg.CFG;

import java.util.*;

/**
 * 循环分析器
 *
 * 使用经典的自然循环识别算法：
 * 1. 使用深度优先搜索遍历CFG，构建支配树
 * 2. 识别回边（边的终点支配边的起点）
 * 3. 对于每条回边，构建对应的自然循环
 *
 * 自然循环：存在一条边 n -> d，且 d 支配 n，则由 d 和所有能到达 n 且不经过 d 的节点组成
 */
public class LoopAnalysis<I extends IRNode> {

    private final List<NaturalLoop<I>> loops;
    private final Map<Integer, Set<Integer>> backEdges;  // backEdgeSources -> headers
    private final Map<Integer, Set<Integer>> dominance;  // node -> dominators
    private final Map<Integer, Integer> immediateDom;    // node -> immediate dominator

    public LoopAnalysis() {
        this.loops = new ArrayList<>();
        this.backEdges = new HashMap<>();
        this.dominance = new HashMap<>();
        this.immediateDom = new HashMap<>();
    }

    /**
     * 执行循环分析
     */
    public void analyze(CFG<I> cfg) {
        // 清空状态
        loops.clear();
        backEdges.clear();
        dominance.clear();
        immediateDom.clear();

        if (cfg.nodes.isEmpty()) {
            return;
        }

        // 1. 计算支配关系
        computeDominance(cfg);

        // 2. 识别回边
        identifyBackEdges(cfg);

        // 3. 构建自然循环
        buildNaturalLoops(cfg);
    }

    /**
     * 计算支配关系
     *
     * 使用数据流分析方法：
     * - D(entry) = {entry}
     * - D(n) = {n} ∪ (∩ D(p) for all predecessors p of n)
     */
    private void computeDominance(CFG<I> cfg) {
        // 初始化：所有节点的支配集为全集
        Set<Integer> allNodes = new HashSet<>();
        for (BasicBlock<I> block : cfg) {
            allNodes.add(block.getId());
        }

        // 入口节点只支配自己
        BasicBlock<I> entry = cfg.getBlock(0);
        if (entry != null) {
            Set<Integer> entryDom = new HashSet<>();
            entryDom.add(entry.getId());
            dominance.put(entry.getId(), entryDom);
        }

        // 迭代计算支配集
        boolean changed = true;
        while (changed) {
            changed = false;
            for (BasicBlock<I> block : cfg) {
                int nodeId = block.getId();
                if (nodeId == 0) continue; // 入口节点已处理

                Set<Integer> newDom = new HashSet<>(allNodes);

                // D(n) = {n} ∩ (∩ D(p) for all predecessors p)
                Set<Integer> predDom = null;
                for (Integer predId : cfg.getFrontier(nodeId)) {
                    if (predDom == null) {
                        predDom = new HashSet<>(dominance.getOrDefault(predId, allNodes));
                    } else {
                        predDom.retainAll(dominance.getOrDefault(predId, allNodes));
                    }
                }

                if (predDom != null) {
                    predDom.add(nodeId);
                    Set<Integer> oldDom = dominance.getOrDefault(nodeId, new HashSet<>());
                    if (!predDom.equals(oldDom)) {
                        dominance.put(nodeId, predDom);
                        changed = true;
                    }
                }
            }
        }

        // 计算直接支配树
        computeImmediateDominators(cfg);
    }

    /**
     * 计算直接支配树
     */
    private void computeImmediateDominators(CFG<I> cfg) {
        // 对于每个节点（非入口），其直接支配器是支配它但不是其他任何支配器的超集
        for (BasicBlock<I> block : cfg) {
            int nodeId = block.getId();
            if (nodeId == 0) continue;

            Set<Integer> dominators = dominance.getOrDefault(nodeId, new HashSet<>());
            Set<Integer> candidates = new HashSet<>(dominators);
            candidates.remove(nodeId);

            int idom = -1;
            for (Integer candidate : candidates) {
                boolean isImmediate = true;
                for (Integer other : candidates) {
                    if (candidate.equals(other)) continue;
                    // 如果 candidate 支配 other，则 candidate 不是直接支配器
                    Set<Integer> otherDom = dominance.getOrDefault(other, new HashSet<>());
                    if (otherDom.contains(candidate)) {
                        isImmediate = false;
                        break;
                    }
                }
                if (isImmediate && (idom == -1 || dominates(dominators, candidate, idom))) {
                    idom = candidate;
                }
            }

            if (idom != -1) {
                immediateDom.put(nodeId, idom);
            }
        }
    }

    /**
     * 检查 a 是否比 b 更接近 dominators
     */
    private boolean dominates(Set<Integer> dominators, int a, int b) {
        // b 在 dominators 中，且 a 支配 b 的所有中间节点
        if (!dominators.contains(b)) return false;
        Set<Integer> aDom = dominance.getOrDefault(a, new HashSet<>());
        return aDom.contains(b);
    }

    /**
     * 识别回边
     *
     * 回边定义：边 n -> d，当且仅当 d 支配 n
     */
    private void identifyBackEdges(CFG<I> cfg) {
        for (BasicBlock<I> block : cfg) {
            int sourceId = block.getId();
            for (Integer succId : cfg.getSucceed(sourceId)) {
                // 检查 succ 是否支配 source
                if (dominates(succId, sourceId)) {
                    // succId -> sourceId 是回边，succId 是循环头
                    backEdges.computeIfAbsent(sourceId, k -> new HashSet<>()).add(succId);
                }
            }
        }
    }

    /**
     * 检查 d 是否支配 n
     */
    private boolean dominates(int d, int n) {
        Set<Integer> dominators = dominance.getOrDefault(n, new HashSet<>());
        return dominators.contains(d);
    }

    /**
     * 构建自然循环
     *
     * 对于回边 n -> d：
     * 1. d 是循环头
     * 2. 循环包括 d 和所有能到达 n 且不经过 d 的节点
     */
    private void buildNaturalLoops(CFG<I> cfg) {
        for (Map.Entry<Integer, Set<Integer>> entry : backEdges.entrySet()) {
            int sourceId = entry.getKey();
            for (Integer headerId : entry.getValue()) {
                NaturalLoop<I> loop = buildNaturalLoop(cfg, sourceId, headerId);
                if (!loop.isEmpty()) {
                    loops.add(loop);
                }
            }
        }
    }

    /**
     * 为回边 source -> header 构建自然循环
     */
    private NaturalLoop<I> buildNaturalLoop(CFG<I> cfg, int sourceId, int headerId) {
        BasicBlock<I> header = cfg.getBlock(headerId);
        NaturalLoop<I> loop = new NaturalLoop<>(header);

        if (header == null) {
            return loop;
        }

        // 循环必须包含循环头
        loop.addLoopNode(headerId);

        // 使用工作列表算法收集循环中的所有节点
        // 从回边源开始，向前遍历所有能到达回边源且不经过循环头的节点
        List<Integer> workList = new ArrayList<>();
        workList.add(sourceId);

        while (!workList.isEmpty()) {
            int current = workList.remove(0);

            // 如果 current 已经在循环中（除了 sourceId 初始加入的情况），跳过
            if (current != sourceId && loop.contains(current)) {
                continue;
            }

            // 添加到循环
            loop.addLoopNode(current);

            // 将 current 的前驱添加到工作列表（不经过 header）
            for (Integer predId : cfg.getFrontier(current)) {
                if (predId != headerId && !loop.contains(predId)) {
                    workList.add(predId);
                }
            }
        }

        return loop;
    }

    /**
     * 获取所有检测到的自然循环
     */
    public List<NaturalLoop<I>> getLoops() {
        return Collections.unmodifiableList(loops);
    }

    /**
     * 获取回边映射
     */
    public Map<Integer, Set<Integer>> getBackEdges() {
        return Collections.unmodifiableMap(backEdges);
    }

    /**
     * 获取支配关系
     */
    public Map<Integer, Set<Integer>> getDominance() {
        return Collections.unmodifiableMap(dominance);
    }

    /**
     * 获取直接支配关系
     */
    public Map<Integer, Integer> getImmediateDominators() {
        return Collections.unmodifiableMap(immediateDom);
    }

    /**
     * 检查节点是否是循环头
     */
    public boolean isLoopHeader(int nodeId) {
        return backEdges.values().stream().anyMatch(headers -> headers.contains(nodeId));
    }

    /**
     * 获取节点所属的循环
     */
    public List<NaturalLoop<I>> getLoopsContaining(int nodeId) {
        List<NaturalLoop<I>> result = new ArrayList<>();
        for (NaturalLoop<I> loop : loops) {
            if (loop.contains(nodeId)) {
                result.add(loop);
            }
        }
        return result;
    }

    /**
     * 获取最外层循环（不包含在任何其他循环中的循环）
     */
    public List<NaturalLoop<I>> getOuterMostLoops() {
        List<NaturalLoop<I>> outerLoops = new ArrayList<>();
        for (NaturalLoop<I> loop : loops) {
            boolean isInner = false;
            for (NaturalLoop<I> other : loops) {
                if (loop.equals(other)) continue;
                // 如果 other 包含 loop 的所有节点，且 other != loop，则 loop 是内层循环
                if (other.getLoopNodes().containsAll(loop.getLoopNodes()) &&
                    !loop.getLoopNodes().containsAll(other.getLoopNodes())) {
                    isInner = true;
                    break;
                }
            }
            if (!isInner) {
                outerLoops.add(loop);
            }
        }
        return outerLoops;
    }

    @Override
    public String toString() {
        return "LoopAnalysis{loops=%d, backEdges=%d}".formatted(loops.size(), backEdges.size());
    }
}
