package org.teachfx.antlr4.ep21.analysis.dataflow;

import org.teachfx.antlr4.ep21.ir.IRNode;
import org.teachfx.antlr4.ep21.pass.cfg.BasicBlock;

import java.util.*;

/**
 * 自然循环表示
 *
 * 自然循环的定义：
 * 1. 有一个唯一的入口节点（循环头）
 * 2. 至少有一个回边指向循环头
 * 3. 循环头支配循环中的所有节点
 */
public class NaturalLoop<I extends IRNode> {

    private final BasicBlock<I> header;
    private final Set<Integer> loopNodes;        // 循环中的所有节点ID（循环体）
    private final Set<Integer> backEdgeSources;  // 回边的源节点
    private final Map<Integer, Set<Integer>> predecessors;  // 前驱关系
    private NaturalLoop<I> parentLoop;          // 父循环（用于嵌套循环）

    public NaturalLoop(BasicBlock<I> header) {
        this.header = header;
        this.loopNodes = new HashSet<>();
        this.backEdgeSources = new HashSet<>();
        this.predecessors = new HashMap<>();
        this.parentLoop = null;
    }

    /**
     * 获取循环头节点
     */
    public BasicBlock<I> getHeader() {
        return header;
    }

    /**
     * 获取循环头节点ID
     */
    public int getHeaderId() {
        return header.getId();
    }

    /**
     * 添加循环节点
     */
    public void addLoopNode(int nodeId) {
        loopNodes.add(nodeId);
    }

    /**
     * 添加回边源节点
     */
    public void addBackEdgeSource(int nodeId) {
        backEdgeSources.add(nodeId);
        loopNodes.add(nodeId);
    }

    /**
     * 获取循环的所有节点ID（循环体）
     */
    public Set<Integer> getLoopNodes() {
        return Collections.unmodifiableSet(loopNodes);
    }

    /**
     * 获取循环体（与getLoopNodes()同义，为兼容性提供）
     */
    public Set<Integer> getBody() {
        return getLoopNodes();
    }

    /**
     * 设置父循环（用于嵌套循环）
     */
    public void setParentLoop(NaturalLoop<I> parent) {
        this.parentLoop = parent;
    }

    /**
     * 获取父循环
     */
    public NaturalLoop<I> getParentLoop() {
        return parentLoop;
    }

    /**
     * 获取回边源节点
     */
    public Set<Integer> getBackEdgeSources() {
        return Collections.unmodifiableSet(backEdgeSources);
    }

    /**
     * 检查节点是否在循环中
     */
    public boolean contains(int nodeId) {
        return loopNodes.contains(nodeId);
    }

    /**
     * 添加前驱关系
     */
    public void addPredecessor(int nodeId, int predId) {
        predecessors.computeIfAbsent(nodeId, k -> new HashSet<>()).add(predId);
    }

    /**
     * 获取节点的前驱
     */
    public Set<Integer> getPredecessors(int nodeId) {
        return predecessors.getOrDefault(nodeId, Collections.emptySet());
    }

    /**
     * 检查循环是否为空
     */
    public boolean isEmpty() {
        return loopNodes.isEmpty();
    }

    /**
     * 获取循环大小
     */
    public int size() {
        return loopNodes.size();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof NaturalLoop)) return false;
        NaturalLoop<?> that = (NaturalLoop<?>) obj;
        return header.equals(that.header) && loopNodes.equals(that.loopNodes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(header, loopNodes);
    }

    @Override
    public String toString() {
        return "NaturalLoop{header=L%d, nodes=%s, backEdges=%s}".formatted(
            header.getId(), loopNodes, backEdgeSources);
    }
}
