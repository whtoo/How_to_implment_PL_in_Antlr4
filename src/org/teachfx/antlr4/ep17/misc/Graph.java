package org.teachfx.antlr4.ep17.misc;

import org.antlr.v4.runtime.misc.MultiMap;
import org.antlr.v4.runtime.misc.OrderedHashSet;
import org.stringtemplate.v4.ST;

import java.util.Set;

public class Graph {

    // I'm using org.antlr.v4.runtime.misc: OrderedHashSet, MultiMap
    public Set<String> nodes = new OrderedHashSet<String>(); // list of functions
    MultiMap<String, String> edges =                  // caller->callee
            new MultiMap<String, String>();
    public Graph() {

    }

    public void edge(String source, String target) {
        edges.map(source, target);
    }

    public String toString() {
        return "edges: " + edges.toString() + ", functions: " + nodes;
    }

    public String toDOT() {
        StringBuilder buf = new StringBuilder();
        buf.append("digraph G {\n");
        buf.append("  ranksep=.25;\n");
        buf.append("  edge [arrowsize=.5]\n");
        buf.append("  node [shape=circle, fontname=\"ArialNarrow\",\n");
        buf.append("        fontsize=12, fixedsize=true, height=.45];\n");
        buf.append("  ");
        for (String node : nodes) { // print all nodes first
            buf.append(node);
            buf.append("; ");
        }
        buf.append("\n");
        for (String src : edges.keySet()) {
            for (String trg : edges.get(src)) {
                buf.append("  ");
                buf.append(src);
                buf.append(" -> ");
                buf.append(trg);
                buf.append(";\n");
            }
        }
        buf.append("}\n");
        return buf.toString();
    }

    /**
     * Fill StringTemplate:
     * digraph G {
     * rankdir=LR;
     * <edgePairs:{edge| <edge.a> -> <edge.b>;}; separator="\n">
     * <childless:{f | <f>;}; separator="\n">
     * }
     * <p>
     * Just as an example. Much cleaner than buf.append method
     */
    public ST toST() {
        ST st = new ST(
                "digraph G {\n" +
                        "  ranksep=.25; \n" +
                        "  edge [arrowsize=.5]\n" +
                        "  node [shape=circle, fontname=\"ArialNarrow\",\n" +
                        "        fontsize=12, fixedsize=true, height=.45];\n" +
                        "  <funcs:{f | <f>; }>\n" +
                        "  <edgePairs:{edge| <edge.a> -> <edge.b>;}; separator=\"\\n\">\n" +
                        "}\n"
        );
        st.add("edgePairs", edges.getPairs());
        st.add("funcs", nodes);
        return st;
    }
}
