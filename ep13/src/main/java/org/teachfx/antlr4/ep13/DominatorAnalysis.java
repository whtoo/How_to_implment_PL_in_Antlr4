package org.teachfx.antlr4.ep13;

import java.util.*;

/**
 * Dominator analysis for SimpleCFG.
 * Computes dominator sets, immediate dominators, and dominance frontiers.
 *
 * Algorithm: Classic iterative dataflow from Cooper, Harvey & Kennedy.
 */
public class DominatorAnalysis {

    private final SimpleCFG cfg;
    private final Map<Integer, Set<Integer>> dom = new HashMap<>();   // block -> dominators
    private final Map<Integer, Integer> idom = new HashMap<>();       // block -> immediate dom
    private final Map<Integer, Set<Integer>> df = new HashMap<>();    // block -> dominance frontier

    public DominatorAnalysis(SimpleCFG cfg) {
        this.cfg = Objects.requireNonNull(cfg);
    }

    /** Run full analysis: dominators → idom tree → dominance frontiers */
    public void analyze() {
        computeDominators();
        computeImmediateDominators();
        computeDominanceFrontiers();
    }

    // ── Step 1: Dominator sets (iterative) ──

    private void computeDominators() {
        // Collect all block IDs
        Set<Integer> allIds = new LinkedHashSet<>();
        for (SimpleCFG.SimpleBlock b : cfg.blocks) allIds.add(b.id);

        // Entry block: only dominates itself
        Set<Integer> entryDom = new LinkedHashSet<>();
        entryDom.add(cfg.blocks.get(0).id);
        dom.put(cfg.blocks.get(0).id, entryDom);

        // Initialize all other blocks to the full set
        for (int i = 1; i < cfg.blocks.size(); i++) {
            dom.put(cfg.blocks.get(i).id, new LinkedHashSet<>(allIds));
        }

        // Iterate until fixed point
        boolean changed;
        int maxIter = allIds.size() * allIds.size();
        int iter = 0;
        do {
            changed = false;
            for (SimpleCFG.SimpleBlock block : cfg.blocks) {
                int bid = block.id;
                if (bid == cfg.blocks.get(0).id) continue; // skip entry

                Set<Integer> newDom = null;
                for (int pred : cfg.getPredecessors(bid)) {
                    Set<Integer> predDom = dom.get(pred);
                    if (predDom == null) continue;
                    if (newDom == null) {
                        newDom = new LinkedHashSet<>(predDom);
                    } else {
                        newDom.retainAll(predDom);
                    }
                }
                if (newDom == null) newDom = new LinkedHashSet<>();
                newDom.add(bid);

                if (!newDom.equals(dom.get(bid))) {
                    dom.put(bid, newDom);
                    changed = true;
                }
            }
            iter++;
        } while (changed && iter < maxIter);
    }

    // ── Step 2: Immediate dominators (deepest dominator ≠ self) ──

    private void computeImmediateDominators() {
        for (SimpleCFG.SimpleBlock block : cfg.blocks) {
            int bid = block.id;
            if (bid == cfg.blocks.get(0).id) {
                idom.put(bid, null);
                continue;
            }

            Set<Integer> dominators = dom.get(bid);
            if (dominators == null) { idom.put(bid, null); continue; }

            // Candidates = dominators - {self}
            Set<Integer> candidates = new LinkedHashSet<>(dominators);
            candidates.remove(bid);

            // idom = deepest candidate (dominates all other candidates)
            Integer deepest = null;
            for (int c : candidates) {
                boolean isDeepest = true;
                for (int other : candidates) {
                    if (other != c) {
                        Set<Integer> otherDom = dom.get(other);
                        if (otherDom != null && otherDom.contains(c)) {
                            isDeepest = false;
                            break;
                        }
                    }
                }
                if (isDeepest) { deepest = c; break; }
            }
            idom.put(bid, deepest);
        }
    }

    // ── Step 3: Dominance frontiers ──

    private void computeDominanceFrontiers() {
        for (SimpleCFG.SimpleBlock block : cfg.blocks) {
            df.put(block.id, new LinkedHashSet<>());
        }

        // Local DF: for each block n, for each successor s,
        // if idom[s] ≠ n then s ∈ DF[n]
        for (SimpleCFG.SimpleBlock block : cfg.blocks) {
            int n = block.id;
            for (int s : cfg.getSuccessors(n)) {
                Integer sIdom = idom.get(s);
                if (sIdom == null || sIdom != n) {
                    df.get(n).add(s);
                }
            }
        }

        // Propagate: DF[n] += DF[c] for c ∈ DF[n]
        boolean changed;
        do {
            changed = false;
            for (SimpleCFG.SimpleBlock block : cfg.blocks) {
                int n = block.id;
                Set<Integer> curDF = df.get(n);
                Set<Integer> newDF = new LinkedHashSet<>(curDF);
                for (int c : curDF) {
                    Set<Integer> cDF = df.get(c);
                    if (cDF != null) newDF.addAll(cDF);
                }
                if (!newDF.equals(curDF)) {
                    df.put(n, newDF);
                    changed = true;
                }
            }
        } while (changed);
    }

    // ── Public API ──

    public Map<Integer, Set<Integer>> getDominators() { return Collections.unmodifiableMap(dom); }
    public Map<Integer, Integer> getImmediateDominators() { return Collections.unmodifiableMap(idom); }
    public Map<Integer, Set<Integer>> getDominanceFrontiers() { return Collections.unmodifiableMap(df); }

    public Set<Integer> getDominanceFrontier(int blockId) {
        return Collections.unmodifiableSet(df.getOrDefault(blockId, Collections.emptySet()));
    }

    public boolean dominates(int a, int b) {
        Set<Integer> domB = dom.get(b);
        return domB != null && domB.contains(a);
    }

    /** Print dominator tree as DOT */
    public String dominatorTreeToDot() {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph DominatorTree {\n");
        sb.append("  node [shape=circle];\n");
        for (SimpleCFG.SimpleBlock b : cfg.blocks) {
            sb.append("  B").append(b.id).append(";\n");
        }
        for (var e : idom.entrySet()) {
            if (e.getValue() != null) {
                sb.append("  B").append(e.getValue()).append(" -> B").append(e.getKey()).append(";\n");
            }
        }
        sb.append("}\n");
        return sb.toString();
    }
}
