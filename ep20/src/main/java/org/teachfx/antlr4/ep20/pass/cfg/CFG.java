package org.teachfx.antlr4.ep20.pass.cfg;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.teachfx.antlr4.ep20.ir.IRNode;

import java.util.*;
import java.util.LinkedList;

public class CFG<I extends IRNode> implements Iterable<BasicBlock<I>> {
    public final List<BasicBlock<I>> nodes;

    public final List<Pair<Integer,Integer>> edges;

    private List<Pair<Set<Integer>, Set<Integer>>> links;

    public CFG(List<BasicBlock<I>> nodes, List<Pair<Integer, Integer>> edges) {
        // Generate init
        this.nodes = new LinkedList<>(nodes); ///.stream().sorted(Comparator.comparingInt(BasicBlock::getId)).toList();
        this.edges = edges;

        links = new ArrayList<>();
        for (var i = 0;i < nodes.size();i++) {
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


    @Override
    public String toString() {
        var graphRenderBuffer = new StringBuffer();

        return graphRenderBuffer.toString();
    }
}
