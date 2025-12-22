package org.teachfx.antlr4.ep21.analysis.ssa;

import org.teachfx.antlr4.ep21.ir.IRNode;
import org.teachfx.antlr4.ep21.pass.cfg.BasicBlock;
import org.teachfx.antlr4.ep21.pass.cfg.CFG;

import java.util.*;

/**
 * 支配关系分析器 - 计算控制流图的支配树和支配边界
 *
 * 实现经典的迭代数据流算法来计算支配关系，用于SSA形式转换。
 *
 * 算法步骤：
 * 1. 计算支配集合（dominance sets）
 * 2. 构建支配树（dominator tree）
 * 3. 计算支配边界（dominance frontier）
 *
 * 基于《现代编译器实现》和《Engineering a Compiler》中的算法。
 */
public class DominatorAnalysis<I extends IRNode> {

    private final CFG<I> cfg;
    private final Map<Integer, Set<Integer>> dom; // 节点ID -> 支配节点集合
    private final Map<Integer, Integer> idom;     // 节点ID -> 直接支配者ID
    private final Map<Integer, Set<Integer>> df;  // 节点ID -> 支配边界集合

    public DominatorAnalysis(CFG<I> cfg) {
        this.cfg = Objects.requireNonNull(cfg, "CFG cannot be null");
        this.dom = new HashMap<>();
        this.idom = new HashMap<>();
        this.df = new HashMap<>();
    }

    /**
     * 执行完整的支配关系分析
     */
    public void analyze() {
        computeDominators();
        computeDominatorTree();
        computeDominanceFrontier();
    }

    /**
     * 计算支配集合（迭代算法）
     *
     * 算法：对于所有节点n：
     *   dom[n] = {n} ∪ ∩(dom[p] for p in predecessors(n))
     * 迭代直到收敛。
     */
    private void computeDominators() {
        // 初始化：所有节点支配自己
        for (BasicBlock<I> block : cfg.nodes) {
            Set<Integer> selfSet = new HashSet<>();
            selfSet.add(block.getId());
            dom.put(block.getId(), selfSet);
        }

        // 入口基本块（假设ID为0）只支配自己
        Set<Integer> entryDom = new HashSet<>();
        entryDom.add(0); // 假设入口块ID为0
        dom.put(0, entryDom);

        boolean changed;
        int iteration = 0;
        int maxIterations = cfg.nodes.size() * cfg.nodes.size(); // 安全限制

        do {
            changed = false;

            // 按拓扑顺序处理节点（提高收敛速度）
            for (BasicBlock<I> block : cfg.nodes) {
                int blockId = block.getId();
                if (blockId == 0) continue; // 入口块已初始化

                Set<Integer> oldSet = dom.get(blockId);
                Set<Integer> newSet = new HashSet<>();

                // 初始化为所有节点的集合
                for (BasicBlock<I> b : cfg.nodes) {
                    newSet.add(b.getId());
                }

                // 与前驱支配集合求交集
                Set<Integer> predecessors = cfg.getFrontier(blockId);
                if (predecessors.isEmpty()) {
                    // 没有前驱（不可达节点），保持原样
                    newSet = new HashSet<>(oldSet);
                } else {
                    for (int predId : predecessors) {
                        Set<Integer> predDom = dom.get(predId);
                        if (predDom != null) {
                            newSet.retainAll(predDom);
                        }
                    }
                    // 添加自己
                    newSet.add(blockId);
                }

                if (!newSet.equals(oldSet)) {
                    dom.put(blockId, newSet);
                    changed = true;
                }
            }

            iteration++;
            if (iteration > maxIterations) {
                throw new IllegalStateException("支配集合计算未收敛，可能存在循环或无效CFG");
            }

        } while (changed);
    }

    /**
     * 构建支配树（计算直接支配者）
     *
     * 直接支配者idom[n]是dom[n]中除n外的最深节点，
     * 即支配n且不被dom[n]中其他节点支配的节点。
     */
    private void computeDominatorTree() {
        // 初始化所有节点的直接支配者为null（表示未知）
        for (BasicBlock<I> block : cfg.nodes) {
            idom.put(block.getId(), null);
        }

        // 入口块没有直接支配者
        idom.put(0, null);

        // 对于每个节点n，找到dom[n]中除n外的最深节点
        for (BasicBlock<I> block : cfg.nodes) {
            int blockId = block.getId();
            if (blockId == 0) continue;

            Set<Integer> dominators = dom.get(blockId);
            if (dominators == null || dominators.isEmpty()) {
                continue;
            }

            // 候选直接支配者：dom[n]中除n外的节点
            Set<Integer> candidates = new HashSet<>(dominators);
            candidates.remove(blockId);

            // 找到最深节点：对于每个候选c，检查是否被其他候选支配
            // 如果不存在其他候选d使得d支配c，则c是直接支配者
            Integer immediateDominator = null;
            for (int candidate : candidates) {
                boolean isDeepest = true;

                for (int other : candidates) {
                    if (other == candidate) continue;

                    Set<Integer> otherDom = dom.get(other);
                    if (otherDom != null && otherDom.contains(candidate)) {
                        // other支配candidate，所以candidate不是最深
                        isDeepest = false;
                        break;
                    }
                }

                if (isDeepest) {
                    immediateDominator = candidate;
                    break;
                }
            }

            idom.put(blockId, immediateDominator);
        }
    }

    /**
     * 计算支配边界
     *
     * 支配边界DF[n] = {x | n支配x的某个前驱，但不严格支配x}
     * 即：x在n的支配边界中，如果n支配x的某个前驱，但n不支配x。
     *
     * 使用标准算法：对于每个节点n，遍历其后继s，
     * 对于每个后继s，如果idom[s] ≠ n，则将s加入DF[n]。
     * 然后对于每个c ∈ DF[n]，将其DF[c]加入DF[n]。
     */
    private void computeDominanceFrontier() {
        // 初始化
        for (BasicBlock<I> block : cfg.nodes) {
            df.put(block.getId(), new HashSet<>());
        }

        // 第一步：计算局部支配边界
        for (BasicBlock<I> block : cfg.nodes) {
            int n = block.getId();

            // 获取后继
            Set<Integer> successors = cfg.getSucceed(n);
            if (successors == null) continue;

            for (int s : successors) {
                // 如果n不是s的直接支配者，则s在n的支配边界中
                if (idom.get(s) == null || idom.get(s) != n) {
                    df.get(n).add(s);
                }
            }
        }

        // 第二步：传播支配边界（迭代直到收敛）
        boolean changed;
        do {
            changed = false;

            for (BasicBlock<I> block : cfg.nodes) {
                int n = block.getId();
                Set<Integer> currentDF = df.get(n);
                Set<Integer> newDF = new HashSet<>(currentDF);

                // 对于当前DF中的每个节点c，将c的DF加入n的DF
                for (int c : currentDF) {
                    Set<Integer> dfOfC = df.get(c);
                    if (dfOfC != null) {
                        newDF.addAll(dfOfC);
                    }
                }

                if (!newDF.equals(currentDF)) {
                    df.put(n, newDF);
                    changed = true;
                }
            }
        } while (changed);
    }

    // 公共访问方法

    public Map<Integer, Set<Integer>> getDominators() {
        return Collections.unmodifiableMap(dom);
    }

    public Map<Integer, Integer> getImmediateDominators() {
        return Collections.unmodifiableMap(idom);
    }

    public Map<Integer, Set<Integer>> getDominanceFrontier() {
        return Collections.unmodifiableMap(df);
    }

    /**
     * 获取指定节点的支配边界
     */
    public Set<Integer> getDominanceFrontier(int blockId) {
        return Collections.unmodifiableSet(df.getOrDefault(blockId, Collections.emptySet()));
    }

    /**
     * 获取指定节点的直接支配者
     */
    public Integer getImmediateDominator(int blockId) {
        return idom.get(blockId);
    }

    /**
     * 检查节点a是否支配节点b
     */
    public boolean dominates(int a, int b) {
        Set<Integer> domB = dom.get(b);
        return domB != null && domB.contains(a);
    }

    /**
     * 检查节点a是否严格支配节点b（a ≠ b）
     */
    public boolean strictlyDominates(int a, int b) {
        return a != b && dominates(a, b);
    }

    /**
     * 将支配树输出为DOT格式（用于可视化）
     */
    public String toDOT() {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph DominatorTree {\n");
        sb.append("  node [shape=circle];\n");

        // 添加节点
        for (BasicBlock<I> block : cfg.nodes) {
            int id = block.getId();
            sb.append("  ").append(id).append(";\n");
        }

        // 添加边（直接支配关系）
        for (Map.Entry<Integer, Integer> entry : idom.entrySet()) {
            if (entry.getValue() != null) {
                sb.append("  ").append(entry.getValue())
                  .append(" -> ").append(entry.getKey())
                  .append(" [style=solid];\n");
            }
        }

        sb.append("}\n");
        return sb.toString();
    }

    /**
     * 静态工具方法：快速计算支配边界
     */
    public static <I extends IRNode> Map<Integer, Set<Integer>> computeDominanceFrontier(CFG<I> cfg) {
        DominatorAnalysis<I> analysis = new DominatorAnalysis<>(cfg);
        analysis.analyze();
        return analysis.getDominanceFrontier();
    }

    /**
     * 静态工具方法：快速计算直接支配者
     */
    public static <I extends IRNode> Map<Integer, Integer> computeImmediateDominators(CFG<I> cfg) {
        DominatorAnalysis<I> analysis = new DominatorAnalysis<>(cfg);
        analysis.analyze();
        return analysis.getImmediateDominators();
    }
}