package org.teachfx.antlr4.ep20.pass.cfg;

import org.apache.commons.lang3.tuple.Pair;
import org.teachfx.antlr4.ep20.ir.IRNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class CFG {
    public final List<IRNode> nodes;

    public final List<Pair<Integer,Integer>> edges;

    private List<Pair<Set<Integer>, Set<Integer>>> links;

    public CFG(List<IRNode> nodes, List<Pair<Integer, Integer>> edges) {
        // Generate init
        this.nodes = nodes;
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
}
