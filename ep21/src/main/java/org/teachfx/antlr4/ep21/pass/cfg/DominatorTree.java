package org.teachfx.antlr4.ep21.pass.cfg;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.teachfx.antlr4.ep21.ir.IRNode;

import java.util.*;

/**
 * 支配树分析器
 * 
 * <p>支配树是CFG分析中的重要数据结构，用于确定每个块的支配关系。
 * 如果从入口块到块B的所有路径都必须经过块A，则称A支配B。</p>
 * 
 * <p>此实现使用经典的数据流迭代算法计算支配集合。</p>
 * 
 * <h2>算法说明</h2>
 * <pre>
 * 初始化:
 *   D(entry) = {entry}
 *   D(n) = ALL_NODES (n != entry)
 * 
 * 迭代:
 *   D(n) = {n} ∪ (∩_{p ∈ pred(n)} D(p))
 * 
 * 直到所有D(n)不再变化
 * </pre>
 * 
 * <h2>使用示例</h2>
 * <pre>{@code
 * CFG<IRNode> cfg = ...;
 * DominatorTree<IRNode> domTree = new DominatorTree<>(cfg);
 * domTree.compute(entryBlockId);
 * 
 * // 获取支配边界
 * Set<Integer> df = domTree.getDominanceFrontier(blockId);
 * 
 * // 检查A是否支配B
 * boolean aDominatesB = domTree.dominates(aId, bId);
 * }</pre>
 * 
 * @param <I> IR节点类型，必须扩展IRNode
 * @author EP21 Team
 * @version 1.0
 */
public class DominatorTree<I extends IRNode> {

    private static final Logger logger = LogManager.getLogger(DominatorTree.class);
    
    private final CFG<I> cfg;
    private List<Set<Integer>> dominators;  // 每个节点的支配集合
    private List<Set<Integer>> immediateDominators;  // 每个节点的直接支配者
    private int entryNode = -1;
    private boolean computed = false;
    
    /**
     * 创建支配树分析器
     * 
     * @param cfg 要分析的CFG
     */
    public DominatorTree(@NotNull CFG<I> cfg) {
        this.cfg = cfg;
        this.dominators = new ArrayList<>();
        this.immediateDominators = new ArrayList<>();
    }

    /**
     * 计算支配树
     *
     * @param entryNodeId 入口节点的ID
     * @return 计算是否收敛（对于不可达节点可能会提前终止）
     */
    public boolean compute(int entryNodeId) {
        this.entryNode = entryNodeId;
        // 使用CFG的links大小而不是nodes.size()，因为节点ID可能不连续
        int maxOrd = cfg.getLinks().size();

        logger.debug("Computing dominator tree for CFG with {} maxOrd, entry={}", maxOrd, entryNodeId);

        // 初始化支配集合
        dominators = new ArrayList<>();
        immediateDominators = new ArrayList<>();

        Set<Integer> allNodes = new HashSet<>();
        for (int i = 0; i < maxOrd; i++) {
            allNodes.add(i);
            dominators.add(new HashSet<>());
            immediateDominators.add(new HashSet<>());
        }

        // 初始化
        dominators.set(entryNode, new HashSet<>(Set.of(entryNode)));
        for (int i = 0; i < maxOrd; i++) {
            if (i != entryNode) {
                dominators.set(i, new HashSet<>(allNodes));
            }
        }

        // 迭代计算
        boolean changed = true;
        int iterations = 0;
        int maxIterations = maxOrd * maxOrd;  // 防止无限循环

        while (changed && iterations < maxIterations) {
            changed = false;
            iterations++;

            for (int i = 0; i < maxOrd; i++) {
                if (i == entryNode) continue;

                Set<Integer> preds = cfg.getFrontier(i);

                if (preds.isEmpty()) {
                    // 不可达节点，跳过
                    continue;
                }

                // D(n) = {n} ∪ (∩_{p ∈ pred(n)} D(p))
                // 先取前驱支配集合的交集，然后添加自身
                Set<Integer> newDom = null;
                for (int pred : preds) {
                    if (newDom == null) {
                        newDom = new HashSet<>(dominators.get(pred));
                    } else {
                        newDom.retainAll(dominators.get(pred));
                    }
                }
                if (newDom == null) {
                    newDom = new HashSet<>();
                }
                newDom.add(i);

                if (!newDom.equals(dominators.get(i))) {
                    dominators.set(i, newDom);
                    changed = true;
                }
            }
        }

        // 计算直接支配者
        computeImmediateDominators();
        
        computed = true;
        
        logger.info("Dominator tree computed in {} iterations", iterations);
        
        return !changed;
    }
    
    /**
     * 计算直接支配者
     */
    private void computeImmediateDominators() {
        int n = dominators.size();

        for (int i = 0; i < n; i++) {
            if (i == entryNode) continue;

            // 不可达节点：没有前驱或支配集合不包含入口节点
            Set<Integer> preds = cfg.getFrontier(i);
            if (preds.isEmpty() || !dominators.get(i).contains(entryNode)) {
                continue;
            }

            if (dominators.get(i).size() <= 1) continue;

            Set<Integer> doms = new HashSet<>(dominators.get(i));
            doms.remove(i);  // 移除自身

            int idom = -1;
            for (int candidate : doms) {
                boolean isImmediate = true;
                for (int other : doms) {
                    if (candidate != other && doms.contains(other)) {
                        // candidate支配other，所以candidate不是i的直接支配者
                        if (dominators.get(candidate).contains(other)) {
                            isImmediate = false;
                            break;
                        }
                    }
                }
                if (isImmediate) {
                    idom = candidate;
                    break;
                }
            }

            if (idom >= 0) {
                immediateDominators.set(i, new HashSet<>(Set.of(idom)));
            }
        }
    }

    /**
     * 重新计算（使用已保存的入口节点）
     * 
     * @return 计算是否收敛
     */
    public boolean recompute() {
        if (entryNode < 0) {
            throw new IllegalStateException("No entry node set. Call compute(entryNodeId) first.");
        }
        return compute(entryNode);
    }

    /**
     * 获取节点的支配集合
     * 
     * @param nodeId 节点ID
     * @return 支配该节点的所有节点集合（包括自身）
     */
    public Set<Integer> getDominators(int nodeId) {
        if (nodeId < 0 || nodeId >= dominators.size()) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(dominators.get(nodeId));
    }

    /**
     * 获取节点的直接支配者
     * 
     * @param nodeId 节点ID
     * @return 直接支配者集合（通常只有一个元素，除了入口节点）
     */
    public Set<Integer> getImmediateDominators(int nodeId) {
        if (nodeId < 0 || nodeId >= immediateDominators.size()) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(immediateDominators.get(nodeId));
    }

    /**
     * 获取支配边界
     * 
     * <p>支配边界是满足以下条件的节点集合：
     * 节点被某个支配者支配，但该支配者的某个后继不被该节点支配。</p>
     * 
     * @param nodeId 节点ID
     * @return 支配边界集合
     */
    public Set<Integer> getDominanceFrontier(int nodeId) {
        Set<Integer> df = new HashSet<>();
        Set<Integer> succs = cfg.getSucceed(nodeId);
        
        for (int succ : succs) {
            if (!dominates(nodeId, succ)) {
                df.add(succ);
            }
        }
        
        // 对于每个被nodeId支配的子节点
        for (int i = 0; i < dominators.size(); i++) {
            if (i == nodeId) continue;
            if (dominators.get(i).contains(nodeId)) {
                Set<Integer> childSuccs = cfg.getSucceed(i);
                for (int childSucc : childSuccs) {
                    if (!dominates(nodeId, childSucc)) {
                        df.add(childSucc);
                    }
                }
            }
        }
        
        return df;
    }

    /**
     * 检查A是否支配B
     * 
     * @param a A的节点ID
     * @param b B的节点ID
     * @return 如果A支配B则返回true
     */
    public boolean dominates(int a, int b) {
        if (a < 0 || a >= dominators.size() || b < 0 || b >= dominators.size()) {
            return false;
        }
        return dominators.get(b).contains(a);
    }

    /**
     * 检查A是否严格支配B（支配但不相等）
     * 
     * @param a A的节点ID
     * @param b B的节点ID
     * @return 如果A严格支配B则返回true
     */
    public boolean strictlyDominates(int a, int b) {
        return a != b && dominates(a, b);
    }

    /**
     * 获取支配树深度
     * 
     * @param nodeId 节点ID
     * @return 从入口到该节点的支配路径长度
     */
    public int getDepth(int nodeId) {
        if (nodeId == entryNode) return 0;
        
        Set<Integer> idoms = getImmediateDominators(nodeId);
        if (idoms.isEmpty()) return -1;  // 不可达
        
        int maxDepth = 0;
        for (int idom : idoms) {
            maxDepth = Math.max(maxDepth, getDepth(idom) + 1);
        }
        return maxDepth;
    }

    /**
     * 获取所有被指定节点支配的节点
     * 
     * @param nodeId 节点ID
     * @return 被支配的节点集合
     */
    public Set<Integer> getDominatedNodes(int nodeId) {
        Set<Integer> dominated = new HashSet<>();
        
        for (int i = 0; i < dominators.size(); i++) {
            if (dominates(nodeId, i)) {
                dominated.add(i);
            }
        }
        
        return dominated;
    }

    /**
     * 获取支配树的孩子节点
     * 
     * @param nodeId 节点ID
     * @return 直接被该节点支配的节点集合
     */
    public Set<Integer> getTreeChildren(int nodeId) {
        Set<Integer> children = new HashSet<>();
        
        for (int i = 0; i < immediateDominators.size(); i++) {
            if (immediateDominators.get(i).contains(nodeId)) {
                children.add(i);
            }
        }
        
        return children;
    }

    /**
     * 检查CFG是否可规约（所有回边都有唯一的循环头）
     * 
     * <p>一个CFG是可规约的，如果所有循环都有唯一的入口点。
     * 对于每个回边指向的循环头，检查是否有多个前驱从循环外部指向它。</p>
     * 
     * @return 如果CFG可规约则返回true
     */
    public boolean isReducible() {
        // 获取所有循环头
        Set<Integer> loopHeaders = getLoopHeaders();
        
        // 检查每个回边的支配关系
        for (var edge : cfg.edges) {
            int from = edge.getLeft();
            int to = edge.getMiddle();
            
            // 如果边指向祖先节点（回边）
            if (dominates(to, from)) {
                // 检查循环头是否有多个前驱从循环外部指向它
                Set<Integer> preds = cfg.getFrontier(to);
                int externalPreds = 0;
                for (int pred : preds) {
                    // 只计算从循环外部来的前驱
                    if (!dominates(to, pred)) {
                        externalPreds++;
                    }
                }
                if (externalPreds > 1) {
                    logger.debug("Node {} has {} external predecessors but is a loop header: {}", to, externalPreds, preds);
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 获取自然循环的头节点集合
     * 
     * @return 所有循环头节点ID集合
     */
    public Set<Integer> getLoopHeaders() {
        Set<Integer> loopHeaders = new HashSet<>();
        
        for (var edge : cfg.edges) {
            int from = edge.getLeft();
            int to = edge.getMiddle();
            
            // 回边：指向祖先节点
            if (dominates(to, from)) {
                loopHeaders.add(to);
            }
        }
        
        return loopHeaders;
    }

    /**
     * 获取入口节点ID
     * 
     * @return 入口节点ID
     */
    public int getEntryNode() {
        return entryNode;
    }

    /**
     * 检查是否已计算
     * 
     * @return 如果已计算支配树则返回true
     */
    public boolean isComputed() {
        return computed;
    }

    /**
     * 获取支配树字符串表示（用于调试）
     * 
     * @return 支配树字符串
     */
    public String toTreeString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Dominator Tree (entry=").append(entryNode).append("):\n");
        
        for (int i = 0; i < immediateDominators.size(); i++) {
            Set<Integer> idoms = immediateDominators.get(i);
            if (idoms.isEmpty() && i != entryNode) {
                continue;  // 不可达节点
            }
            sb.append("  L").append(i).append(" -> ");
            if (i == entryNode) {
                sb.append("(entry)");
            } else {
                sb.append(idoms);
            }
            sb.append(" | doms: ").append(dominators.get(i)).append("\n");
        }
        
        return sb.toString();
    }

    /**
     * 验证支配树的正确性
     * 
     * @return 验证结果
     */
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        
        if (!computed) {
            result.addError("Dominator tree has not been computed");
            return result;
        }
        
        // 检查入口节点的支配者只有自身
        if (!dominators.get(entryNode).equals(Set.of(entryNode))) {
            result.addError("Entry node " + entryNode + " should dominate only itself, but dominates: " + dominators.get(entryNode));
        }
        
        // 检查每个节点的支配者包含自身
        for (int i = 0; i < dominators.size(); i++) {
            if (!dominators.get(i).contains(i)) {
                result.addError("Node " + i + " does not dominate itself");
            }
        }
        
        // 检查支配关系的单调性
        for (int i = 0; i < dominators.size(); i++) {
            for (int j = 0; j < dominators.size(); j++) {
                if (dominates(i, j) && i != j) {
                    // 如果i支配j且i != j，则j的支配者应该是i的支配者的超集
                    Set<Integer> iDoms = dominators.get(i);
                    for (int idom : iDoms) {
                        if (!dominates(idom, j)) {
                            result.addError("Dominance relation violated: " + idom + " dominates " + i + " but not " + j);
                        }
                    }
                }
            }
        }
        
        // 检查直接支配者关系
        for (int i = 0; i < immediateDominators.size(); i++) {
            Set<Integer> idoms = immediateDominators.get(i);
            if (idoms.size() > 1) {
                result.addWarning("Node " + i + " has multiple immediate dominators: " + idoms);
            }
        }
        
        logger.info("Dominator tree validation: {} errors, {} warnings", 
            result.getErrors().size(), result.getWarnings().size());
        
        return result;
    }

    /**
     * 验证结果
     */
    public static class ValidationResult {
        private final List<String> errors = new ArrayList<>();
        private final List<String> warnings = new ArrayList<>();
        
        public void addError(String error) {
            errors.add(error);
        }
        
        public void addWarning(String warning) {
            warnings.add(warning);
        }
        
        public List<String> getErrors() {
            return Collections.unmodifiableList(errors);
        }
        
        public List<String> getWarnings() {
            return Collections.unmodifiableList(warnings);
        }
        
        public boolean isValid() {
            return errors.isEmpty();
        }
    }
}
