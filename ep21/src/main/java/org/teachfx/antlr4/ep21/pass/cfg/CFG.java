package org.teachfx.antlr4.ep21.pass.cfg;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.teachfx.antlr4.ep21.ir.IRNode;
import org.teachfx.antlr4.ep21.utils.StreamUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/*
    One graph bind to a function.
 */
public class CFG<I extends IRNode> implements Iterable<BasicBlock<I>> {
    private final static Logger logger = LogManager.getLogger(CFG.class);
    // index: 第几号节点 -> BasicBlock<I> : 第几号节点对应的BasicBlock<I>节点
    public final List<BasicBlock<I>> nodes;
    // <from,to,weight> : <起始节点，终止节点,权重>
    public final List<Triple<Integer, Integer,Integer>> edges;
    // index: 第几号节点 ->   <prev,successors> : <前驱节点的集合，后继节点的集合>
    private final List<Pair<Set<Integer>, Set<Integer>>> links;
    private final List<IFlowOptimizer<I>> optimizers = new ArrayList<>();

    public CFG(List<BasicBlock<I>> nodes, List<Triple<Integer, Integer,Integer>> edges) {
        // Generate init
        if (nodes.isEmpty()) {
            logger.debug("Empty nodes list - handling gracefully");
            this.nodes = nodes;
            this.edges = edges;
            this.links = new ArrayList<>();
            return;
        }

        // 计算最大索引，确保数组大小足够容纳所有节点
        int maxNodeId = nodes.stream().mapToInt(BasicBlock::getId).max().orElse(0);
        int maxEdgeFrom = edges.stream().mapToInt(Triple::getLeft).max().orElse(0);
        int maxEdgeTo = edges.stream().mapToInt(Triple::getMiddle).max().orElse(0);
        int maxOrd = Math.max(Math.max(maxNodeId, maxEdgeFrom), maxEdgeTo) + 1;

        logger.debug("CFG initialized: {} nodes, maxNodeId={}, maxOrd={}",
                    nodes.size(), maxNodeId, maxOrd);
        this.nodes = nodes;
        
        // 重复边检测和去重逻辑 - 基于节点对（源节点->目标节点）去重
        Set<String> seenEdgePairs = new HashSet<>();
        List<Triple<Integer, Integer, Integer>> deduplicatedEdges = new ArrayList<>();
        Set<String> duplicateEdgePairs = new HashSet<>();
        
        for (var edge : edges) {
            String edgePair = edge.getLeft() + "->" + edge.getMiddle();
            if (seenEdgePairs.add(edgePair)) {
                // 第一次看到这个节点对，添加到去重后的列表
                deduplicatedEdges.add(edge);
            } else {
                // 已经存在这个节点对，记录重复边并跳过
                duplicateEdgePairs.add(edgePair);
                logger.warn("检测到重复边，将被忽略: {} -> {} (类型: {})",
                           edge.getLeft(), edge.getMiddle(), edge.getRight());
            }
        }
        
        if (!duplicateEdgePairs.isEmpty()) {
            logger.info("CFG构造函数去重完成: 原始边数={}, 去重后边数={}, 移除重复边={}",
                       edges.size(), deduplicatedEdges.size(), edges.size() - deduplicatedEdges.size());
            logger.info("移除的重复边节点对: {}", duplicateEdgePairs);
        }
        
        this.edges = deduplicatedEdges;

        links = new ArrayList<>();
        for (var i = 0; i < maxOrd; i++) {
            links.add(Pair.of(new TreeSet<>(), new TreeSet<>()));
        }

        for (var edge : deduplicatedEdges) {
            var u = edge.getLeft();
            var v = edge.getMiddle();
            if (u < links.size() && v < links.size()) {
                links.get(u).getRight().add(v);
                links.get(v).getLeft().add(u);
            } else {
                logger.warn("Edge indices out of bounds - u={}, v={}, links.size={}", u, v, links.size());
            }
        }
    }

    public BasicBlock<I> getBlock(int id) {
        // 使用流而不是数组索引来查找block
        return nodes.stream()
                .filter(block -> block.getId() == id)
                .findFirst()
                .orElse(null);
    }

    public String toDOT() {
        StringBuilder buf = new StringBuilder();
        buf.append("digraph G {\n");
        buf.append("  ranksep=.25;\n");
        buf.append("  edge [arrowsize=.5]\n");
        buf.append("  node [shape=circle, fontname=\"ArialNarrow\",\n");
        buf.append("        fontsize=12, fixedsize=true, height=.45];\n");
        
        // 添加所有节点
        buf.append("  ");
        for (BasicBlock<I> node : nodes) {
            buf.append(node.id);
            buf.append("; ");
        }
        buf.append("\n");
        
        // 添加所有边，并检测重复边
        Set<String> seenEdges = new HashSet<>();
        Set<String> duplicateEdges = new HashSet<>();
        
        for (Triple<Integer, Integer, Integer> edge : edges) {
            String edgeKey = edge.getLeft() + "->" + edge.getMiddle();
            buf.append("  ");
            buf.append(edge.getLeft());
            buf.append(" -> ");
            buf.append(edge.getMiddle());
            buf.append(";\n");
            
            // 检测重复边
            if (!seenEdges.add(edgeKey)) {
                duplicateEdges.add(edgeKey);
                logger.warn("发现重复边: {} -> {}", edge.getLeft(), edge.getMiddle());
            }
        }
        
        if (!duplicateEdges.isEmpty()) {
            logger.error("CFG中检测到 {} 个重复边: {}", duplicateEdges.size(), duplicateEdges);
        }
        
        buf.append("}\n");
        return buf.toString();
    }

    /**
     * 获取前驱节点集合，也就是该节点的前驱节点集合。
     * @param id 节点的id
     * @return 前驱节点集合。
     */
    public Set<Integer> getFrontier(int id) {
        return links.get(id).getLeft();
    }

    /**
     * 获取后继节点集合，也就是该节点的后继节点集合。
     * @param id 节点的id
     * @return 后继节点集合。
     */
    public Set<Integer> getSucceed(int id) {
        return links.get(id).getRight();
    }


    // 这个方法是用来获取节点的入度的，入度就是该节点的前驱节点的数量。
    public int getInDegree(int id) {
        return links.get(id).getLeft().size();
    }


    /*
        @desc 这个方法是用来获取节点的入度的，入度就是该节点的前驱节点的数量。
        @param key 节点的id
     */
    public Stream<Triple<Integer,Integer,Integer>> getInEdges(int key) {
        return edges.stream().filter(edge -> edge.getMiddle() == key);
    }

    // 这个方法是用来获取节点的出度的，出度就是该节点的后继节点的数量。
    public int getOutDegree(int id) {
        return links.get(id).getRight().size();
    }

    /**
     * 获取CFG的链接关系列表（前驱/后继映射）
     * 
     * <p>返回的列表中，每个位置的索引对应节点ID，
     * 值是对应的Pair，前驱集合在left，后继集合在right。</p>
     * 
     * @return 链接关系列表（不可修改视图）
     */
    public List<Pair<Set<Integer>, Set<Integer>>> getLinks() {
        return Collections.unmodifiableList(links);
    }

    /**
     * 确保指定节点ID的链接条目存在
     * 
     * @param id 节点ID
     */
    public void ensureLinkExists(int id) {
        while (links.size() <= id) {
            links.add(org.apache.commons.lang3.tuple.Pair.of(new TreeSet<>(), new TreeSet<>()));
        }
    }

    @NotNull
    @Override
    public Iterator<BasicBlock<I>> iterator() {
        return nodes.iterator();
    }


    // 写个注释
    // 这个方法是用来生成dot文件的，这个文件可以用来生成图形的。
    @Override
    public String toString() {
        var graphRenderBuffer = new StringBuilder("graph TD\n");
        AtomicInteger i = new AtomicInteger();

        for (var node : nodes.stream().sorted((b1,b2) -> b2.id - b1.id).toList()) {

            graphRenderBuffer.append("subgraph ").append(node.getOrdLabel()).append("\n");
            node.dropLabelSeq().stream().map(x -> x.instr.toString()).map(x -> "Q" + (i.getAndIncrement()) + "[\"" + x + ";\"]\n").forEach(graphRenderBuffer::append);
            graphRenderBuffer.append("end").append("\n");

        }

        for (var edge : edges) {
            graphRenderBuffer.append("L").append(edge.getLeft()).append(" --> ").append("L").append(edge.getMiddle()).append("\n");
        }

        return graphRenderBuffer.toString();
    }


    /**
     * remove edge from edges and links
     * @param edge edge to remove, Triple<Integer,Integer,Integer> : <起始节点，终止节点,权重>
     *
     */
    public boolean removeEdge(Triple<Integer,Integer,Integer> edge) {
        logger.debug("尝试移除边: {} -> {}, 权重: {}", edge.getLeft(), edge.getMiddle(), edge.getRight());
        
        // 精确匹配要移除的边
        boolean edgeFound = false;
        Triple<Integer, Integer, Integer> edgeToRemove = null;
        
        for (var existingEdge : edges) {
            if (existingEdge.getLeft() == edge.getLeft() &&
                existingEdge.getMiddle() == edge.getMiddle() &&
                existingEdge.getRight() == edge.getRight()) {
                edgeFound = true;
                edgeToRemove = existingEdge;
                break;
            }
        }
        
        if (!edgeFound) {
            logger.warn("边不存在于edges列表中: {} -> {}, 权重: {}", edge.getLeft(), edge.getMiddle(), edge.getRight());
            return false;
        }
        
        boolean removed = edges.remove(edgeToRemove);
        logger.debug("从edges列表中移除结果: {}", removed);
        
        var srcBlockId = edge.getLeft();
        var destBlockId = edge.getMiddle();
        
        // 检查是否还有其他从srcBlockId到destBlockId的边（忽略权重）
        var otherEdges = edges.stream()
            .filter(e -> e.getLeft() == srcBlockId && e.getMiddle() == destBlockId)
            .count();
        
        logger.debug("移除后，从{}到{}还有其他 {} 条边", srcBlockId, destBlockId, otherEdges);
        
        // 只有当没有其他边时，才移除links中的连接
        if (otherEdges == 0) {
            logger.debug("移除links中的连接: {} -> {}", srcBlockId, destBlockId);
            if (srcBlockId < links.size() && destBlockId < links.size()) {
                links.get(srcBlockId).getRight().remove(destBlockId);
                links.get(destBlockId).getLeft().remove(srcBlockId);
                logger.debug("成功移除links连接");
                return true;
            } else {
                logger.warn("links索引越界: srcBlockId={}, destBlockId={}, links.size={}",
                           srcBlockId, destBlockId, links.size());
                return false;
            }
        } else {
            logger.debug("由于还有其他边存在，不移除links连接");
            return true;
        }
    }

    public void removeNode(BasicBlock<I> node) {
        /// 2. remove node from nodes by its id
        nodes.removeIf(bb -> bb.equals(node));
    }
    /// 1. accept a IFlowOptimizer<I>
    /// 2. add it to optimizers
    public void addOptimizer(IFlowOptimizer<I> optimizer) {
        optimizers.add(optimizer);
    }

    /// 1. apply all optimizers
    public void applyOptimizers() {
        // 1. apply all optimizers
        for (var optimizer : optimizers) {
            optimizer.onHandle(this);
        }
    }


    public List<I> getIRNodes() {
        return StreamUtils.flatMap(nodes.stream(), BasicBlock::getIRNodes).toList();
    }
}
