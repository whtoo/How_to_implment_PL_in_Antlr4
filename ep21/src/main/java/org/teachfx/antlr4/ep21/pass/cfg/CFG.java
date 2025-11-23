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
        // 日志验证：检查nodes列表状态
        System.out.println("DEBUG CFG: Constructor called with " + nodes.size() + " nodes and " + edges.size() + " edges");
        
        // Generate init
        if (nodes.isEmpty()) {
            System.out.println("DEBUG CFG: Empty nodes list - handling gracefully");
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
        
        System.out.println("DEBUG CFG: maxNodeId=" + maxNodeId + ", maxEdgeFrom=" + maxEdgeFrom +
                          ", maxEdgeTo=" + maxEdgeTo + ", maxOrd=" + maxOrd);
        this.nodes = nodes;
        this.edges = edges;

        links = new ArrayList<>();
        for (var i = 0; i < maxOrd; i++) {
            links.add(Pair.of(new TreeSet<>(), new TreeSet<>()));
        }

        for (var edge : edges) {
            var u = edge.getLeft();
            var v = edge.getMiddle();
            System.out.println("DEBUG CFG: Processing edge from " + u + " to " + v);
            if (u < links.size() && v < links.size()) {
                links.get(u).getRight().add(v);
                links.get(v).getLeft().add(u);
            } else {
                System.out.println("DEBUG CFG: Edge indices out of bounds - u=" + u + ", v=" + v + ", links.size=" + links.size());
            }
        }
    }

    public BasicBlock<I> getBlock(int id) {
        return nodes.get(id);
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
        
        // 添加所有边
        for (Triple<Integer, Integer, Integer> edge : edges) {
            buf.append("  ");
            buf.append(edge.getLeft());
            buf.append(" -> ");
            buf.append(edge.getMiddle());
            buf.append(";\n");
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
    public void removeEdge(Triple<Integer,Integer,Integer> edge) {
        edges.remove(edge);
        var srcBlockId = edge.getLeft();
        var destBlockId = edge.getMiddle();
        var nonRel = getInEdges(destBlockId).noneMatch(q -> q.getLeft().compareTo(srcBlockId) == 0);

        if (nonRel) {
            links.get(srcBlockId).getRight().remove(destBlockId);
            links.get(destBlockId).getLeft().remove(srcBlockId);
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
