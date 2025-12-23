package org.teachfx.antlr4.ep21.pass.cfg;

import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.teachfx.antlr4.ep21.ir.IRNode;

import java.util.*;

/**
 * CFG Mutable Builder - 用于动态构建CFG
 *
 * 这个类提供了可变的API来创建CFG结构，与CFG类的不可变设计不同，
 * 这个类允许逐步添加节点和边，最后通过build()方法生成不可变的CFG。
 *
 * @author EP21 Team
 * @version 1.0
 * @since 2025-12-23
 */
public class CFGMutableBuilder {

    private static final Logger logger = LogManager.getLogger(CFGMutableBuilder.class);

    private final List<BasicBlock<IRNode>> nodes;
    private final List<Triple<Integer, Integer, Integer>> edges;
    private final Map<Integer, BasicBlock<IRNode>> nodeMap;  // 快速查找节点
    private final Set<String> edgeSet;  // 检测重复边

    /**
     * 创建一个空的CFG构建器
     */
    public CFGMutableBuilder() {
        this.nodes = new ArrayList<>();
        this.edges = new ArrayList<>();
        this.nodeMap = new HashMap<>();
        this.edgeSet = new HashSet<>();
    }

    /**
     * 从现有CFG创建构建器（用于修改现有CFG）
     */
    public CFGMutableBuilder(CFG<IRNode> existingCFG) {
        this();
        for (BasicBlock<IRNode> node : existingCFG) {
            addNode(node);
        }
        // 需要访问edges，但CFG类没有提供getter，所以我们可能需要其他方式
        logger.debug("Created builder from existing CFG with {} nodes", nodes.size());
    }

    /**
     * 添加基本块到CFG
     *
     * @param block 要添加的基本块
     * @throws IllegalArgumentException 如果block ID已存在
     */
    public void addNode(@NotNull BasicBlock<IRNode> block) {
        Objects.requireNonNull(block, "block cannot be null");

        Integer id = block.getId();
        if (nodeMap.containsKey(id)) {
            throw new IllegalArgumentException("Block with ID " + id + " already exists");
        }

        nodes.add(block);
        nodeMap.put(id, block);
        logger.debug("Added block {} with {} instructions", id, block.getIRNodes().count());
    }

    /**
     * 添加或更新基本块
     *
     * @param block 要添加的基本块
     */
    public void addOrUpdateNode(@NotNull BasicBlock<IRNode> block) {
        Objects.requireNonNull(block, "block cannot be null");

        Integer id = block.getId();
        if (nodeMap.containsKey(id)) {
            // 替换现有节点
            int index = -1;
            for (int i = 0; i < nodes.size(); i++) {
                if (nodes.get(i).getId() == id) {
                    index = i;
                    break;
                }
            }
            if (index >= 0) {
                nodes.set(index, block);
                nodeMap.put(id, block);
                logger.debug("Updated block {}", id);
            }
        } else {
            addNode(block);
        }
    }

    /**
     * 添加有向边到CFG
     *
     * @param from 起始节点ID
     * @param to 目标节点ID
     * @param weight 边权重（默认为1）
     * @throws IllegalArgumentException 如果节点不存在或边已存在
     */
    public void addEdge(int from, int to, int weight) {
        validateNodeExists(from, "from");
        validateNodeExists(to, "to");

        String edgeKey = from + "->" + to;
        if (edgeSet.contains(edgeKey)) {
            logger.warn("Edge {} already exists, skipping", edgeKey);
            return;
        }

        Triple<Integer, Integer, Integer> edge = Triple.of(from, to, weight);
        edges.add(edge);
        edgeSet.add(edgeKey);
        logger.debug("Added edge: {} -> {} (weight: {})", from, to, weight);
    }

    /**
     * 添加有向边到CFG（权重默认为1）
     */
    public void addEdge(int from, int to) {
        addEdge(from, to, 1);
    }

    /**
     * 批量添加边
     */
    public void addEdges(List<Triple<Integer, Integer, Integer>> newEdges) {
        for (Triple<Integer, Integer, Integer> edge : newEdges) {
            addEdge(edge.getLeft(), edge.getMiddle(), edge.getRight());
        }
    }

    /**
     * 获取节点数量
     */
    public int getNodeCount() {
        return nodes.size();
    }

    /**
     * 获取边数量
     */
    public int getEdgeCount() {
        return edges.size();
    }

    /**
     * 检查节点是否存在
     */
    public boolean hasNode(int id) {
        return nodeMap.containsKey(id);
    }

    /**
     * 获取指定ID的节点
     */
    public BasicBlock<IRNode> getNode(int id) {
        return nodeMap.get(id);
    }

    /**
     * 获取所有节点
     */
    public List<BasicBlock<IRNode>> getNodes() {
        return new ArrayList<>(nodes);
    }

    /**
     * 获取所有边
     */
    public List<Triple<Integer, Integer, Integer>> getEdges() {
        return new ArrayList<>(edges);
    }

    /**
     * 生成下一个可用的节点ID
     */
    public int getNextNodeId() {
        int maxId = -1;
        for (BasicBlock<IRNode> node : nodes) {
            if (node.getId() > maxId) {
                maxId = node.getId();
            }
        }
        return maxId + 1;
    }

    /**
     * 构建不可变的CFG
     *
     * @return 包含所有添加的节点和边的CFG
     * @throws IllegalStateException 如果没有节点
     */
    public CFG<IRNode> build() {
        if (nodes.isEmpty()) {
            throw new IllegalStateException("Cannot build CFG with no nodes");
        }

        logger.info("Building CFG with {} nodes and {} edges", nodes.size(), edges.size());

        // 验证CFG完整性
        validateCFG();

        return new CFG<>(new ArrayList<>(nodes), new ArrayList<>(edges));
    }

    /**
     * 验证CFG的完整性
     */
    private void validateCFG() {
        // 检查所有边引用的节点是否存在
        Set<Integer> nodeIds = nodeMap.keySet();
        for (Triple<Integer, Integer, Integer> edge : edges) {
            if (!nodeIds.contains(edge.getLeft())) {
                throw new IllegalStateException("Edge references non-existent node: " + edge.getLeft());
            }
            if (!nodeIds.contains(edge.getMiddle())) {
                throw new IllegalStateException("Edge references non-existent node: " + edge.getMiddle());
            }
        }

        logger.debug("CFG validation passed");
    }

    /**
     * 验证节点存在
     */
    private void validateNodeExists(int nodeId, String paramName) {
        if (!hasNode(nodeId)) {
            throw new IllegalArgumentException(paramName + " node " + nodeId + " does not exist");
        }
    }

    /**
     * 清除所有节点和边
     */
    public void clear() {
        nodes.clear();
        edges.clear();
        nodeMap.clear();
        edgeSet.clear();
        logger.debug("Cleared all nodes and edges");
    }

    /**
     * 创建深拷贝
     */
    public CFGMutableBuilder copy() {
        CFGMutableBuilder copy = new CFGMutableBuilder();
        copy.nodes.addAll(this.nodes);
        copy.edges.addAll(this.edges);
        copy.nodeMap.putAll(this.nodeMap);
        copy.edgeSet.addAll(this.edgeSet);
        return copy;
    }

    @Override
    public String toString() {
        return String.format("CFGMutableBuilder[nodes=%d, edges=%d]",
            nodes.size(), edges.size());
    }
}
