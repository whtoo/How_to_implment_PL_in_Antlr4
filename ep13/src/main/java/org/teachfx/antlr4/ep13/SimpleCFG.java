package org.teachfx.antlr4.ep13;

import java.util.*;

/**
 * Simple control-flow graph for SSA teaching demo.
 * Uses string-based instructions instead of complex IR types.
 */
public class SimpleCFG {

    public final List<SimpleBlock> blocks = new ArrayList<>();
    public final List<int[]> edges = new ArrayList<>(); // [from, to]

    private final Map<Integer, Set<Integer>> predecessors = new HashMap<>();
    private final Map<Integer, Set<Integer>> successors = new HashMap<>();

    public SimpleBlock addBlock(int id, String label) {
        SimpleBlock block = new SimpleBlock(id, label);
        blocks.add(block);
        predecessors.put(id, new LinkedHashSet<>());
        successors.put(id, new LinkedHashSet<>());
        return block;
    }

    public void addEdge(int from, int to) {
        edges.add(new int[]{from, to});
        successors.computeIfAbsent(from, k -> new LinkedHashSet<>()).add(to);
        predecessors.computeIfAbsent(to, k -> new LinkedHashSet<>()).add(from);
    }

    public Set<Integer> getPredecessors(int id) {
        return predecessors.getOrDefault(id, Collections.emptySet());
    }

    public Set<Integer> getSuccessors(int id) {
        return successors.getOrDefault(id, Collections.emptySet());
    }

    public SimpleBlock getBlock(int id) {
        for (SimpleBlock b : blocks) {
            if (b.id == id) return b;
        }
        return null;
    }

    /** A basic block containing string instructions */
    public static class SimpleBlock {
        public final int id;
        public String label;
        public final List<String> instructions = new ArrayList<>();

        public SimpleBlock(int id, String label) {
            this.id = id;
            this.label = label;
        }

        public SimpleBlock add(String instruction) {
            instructions.add(instruction);
            return this;
        }

        @Override
        public String toString() {
            return label + " (B" + id + ")";
        }
    }
}
