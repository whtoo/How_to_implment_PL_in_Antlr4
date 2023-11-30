package org.teachfx.antlr4.ep20.pass.cfg;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.teachfx.antlr4.ep20.ir.IRNode;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

// TODO: visualize cfg
/*
    One graph bind to a function.
 */
public class CFG<I extends IRNode> implements Iterable<BasicBlock<I>> {
    private final static Logger logger = LogManager.getLogger(CFG.class);
    // index: 第几号节点 -> BasicBlock<I> : 第几号节点对应的BasicBlock<I>节点
    public final List<BasicBlock<I>> nodes;
    // <from,to> : <起始节点，终止节点>
    public final List<Pair<Integer, Integer>> edges;
    // index: 第几号节点 ->   <prev,successors> : <前驱节点的集合，后继节点的集合>
    private final List<Pair<Set<Integer>, Set<Integer>>> links;

    public CFG(List<BasicBlock<I>> nodes, List<Pair<Integer, Integer>> edges) {
        // Generate init
        var maxOrd = nodes.stream().max(BasicBlock::compareTo).map(BasicBlock::getId).get() + 1;
        this.nodes = nodes;
        this.edges = edges;

        links = new ArrayList<>();
        for (var i = 0; i < maxOrd; i++) {
            links.add(Pair.of(new TreeSet<>(), new TreeSet<>()));
        }

        for (var edge : edges) {
            var u = edge.getLeft();
            var v = edge.getRight();
            links.get(u).getRight().add(v);
            links.get(v).getLeft().add(u);
        }
    }

    public BasicBlock<I> getBlock(int id) {
        return nodes.get(id);
    }

    public Set<Integer> getPrev(int id) {
        return links.get(id).getLeft();
    }

    public Set<Integer> getSucceed(int id) {
        return links.get(id).getRight();
    }

    public int getInDegree(int id) {
        return links.get(id).getLeft().size();
    }

    public int getOutDegree(int id) {
        return links.get(id).getRight().size();
    }

    @NotNull
    @Override
    public Iterator<BasicBlock<I>> iterator() {
        return nodes.iterator();
    }

    public List<I> simplifyIRInstrs() {

        return null;
    }

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
            graphRenderBuffer.append("L").append(edge.getLeft()).append(" --> ").append("L").append(edge.getRight()).append("\n");
        }

        return graphRenderBuffer.toString();
    }


}
