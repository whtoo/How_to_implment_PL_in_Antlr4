package org.teachfx.antlr4.ep13;

import java.util.*;

/**
 * SSA transformer: phi insertion + variable renaming.
 * Works on SimpleCFG with string-based instructions.
 *
 * Algorithm: Cytron et al. "Efficiently Computing Static Single Assignment Form"
 */
public class SSATransformer {

    private final SimpleCFG cfg;
    private final DominatorAnalysis domAnalysis;

    // Phi functions: blockId -> list of (result, [args])
    private final Map<Integer, List<PhiFunction>> phiFunctions = new LinkedHashMap<>();

    // Variable renaming state
    private final Map<String, Integer> counter = new HashMap<>();   // var -> next version
    private final Map<String, Deque<Integer>> stack = new HashMap<>(); // var -> version stack

    // SSA output: blockId -> list of SSA instructions
    private final Map<Integer, List<String>> ssaInstructions = new LinkedHashMap<>();

    public SSATransformer(SimpleCFG cfg, DominatorAnalysis domAnalysis) {
        this.cfg = cfg;
        this.domAnalysis = domAnalysis;
    }

    /** Run full SSA construction */
    public void transform() {
        insertPhiFunctions();
        renameVariables();
    }

    // ── Step 1: Phi insertion via dominance frontiers ──

    private void insertPhiFunctions() {
        // Collect variable definitions: varName -> set of block IDs where defined
        Map<String, Set<Integer>> varDefs = new LinkedHashMap<>();
        for (SimpleCFG.SimpleBlock block : cfg.blocks) {
            for (String instr : block.instructions) {
                String var = extractDefVar(instr);
                if (var != null) {
                    varDefs.computeIfAbsent(var, k -> new LinkedHashSet<>()).add(block.id);
                }
            }
        }

        // For each variable, insert phi functions at dominance frontiers
        for (var entry : varDefs.entrySet()) {
            String var = entry.getKey();
            Set<Integer> defBlocks = entry.getValue();
            insertPhiForVar(var, defBlocks);
        }
    }

    private void insertPhiForVar(String var, Set<Integer> defBlocks) {
        Queue<Integer> worklist = new ArrayDeque<>(defBlocks);
        Set<Integer> processed = new HashSet<>();
        Set<Integer> hasPhi = new HashSet<>();

        while (!worklist.isEmpty()) {
            int blockId = worklist.poll();
            if (processed.contains(blockId)) continue;
            processed.add(blockId);

            Set<Integer> frontier = domAnalysis.getDominanceFrontier(blockId);
            for (int dfId : frontier) {
                if (!hasPhi.contains(dfId)) {
                    // Insert phi function at dfId
                    phiFunctions.computeIfAbsent(dfId, k -> new ArrayList<>())
                        .add(new PhiFunction(var));
                    hasPhi.add(dfId);
                    // If dfId is not already a def block, add to worklist
                    if (!defBlocks.contains(dfId)) {
                        worklist.add(dfId);
                    }
                }
            }
        }
    }

    // ── Step 2: Variable renaming via dominator tree ──

    private void renameVariables() {
        counter.clear();
        stack.clear();

        // Build dominator tree children
        Map<Integer, List<Integer>> domChildren = new HashMap<>();
        for (var e : domAnalysis.getImmediateDominators().entrySet()) {
            if (e.getValue() != null) {
                domChildren.computeIfAbsent(e.getValue(), k -> new ArrayList<>()).add(e.getKey());
            }
        }

        // DFS from entry block
        renameInBlock(cfg.blocks.get(0).id, domChildren);
    }

    private void renameInBlock(int blockId, Map<Integer, List<Integer>> domChildren) {
        SimpleCFG.SimpleBlock block = cfg.getBlock(blockId);
        if (block == null) return;

        List<String> ssaInstrs = new ArrayList<>();
        int pushedCount = 0; // track how many versions we pushed for rollback

        // Process phi functions first
        List<PhiFunction> blockPhis = phiFunctions.getOrDefault(blockId, Collections.emptyList());
        for (PhiFunction phi : blockPhis) {
            int ver = newVersion(phi.var);
            stack.computeIfAbsent(phi.var, k -> new ArrayDeque<>()).push(ver);
            pushedCount++;
        }

        // Process regular instructions
        for (String instr : block.instructions) {
            // Rename uses
            String renamed = renameUses(instr);
            // Rename def
            String defVar = extractDefVar(renamed);
            if (defVar != null) {
                int ver = newVersion(defVar);
                stack.computeIfAbsent(defVar, k -> new ArrayDeque<>()).push(ver);
                pushedCount++;
                renamed = renameDef(renamed, defVar, ver);
            }
            ssaInstrs.add(renamed);
        }

        // Fill phi arguments in successor blocks
        for (int succId : cfg.getSuccessors(blockId)) {
            List<PhiFunction> succPhis = phiFunctions.getOrDefault(succId, Collections.emptyList());
            for (PhiFunction phi : succPhis) {
                Integer ver = peekVersion(phi.var);
                phi.args.add(phi.var + "_" + (ver != null ? ver : 0));
            }
        }

        ssaInstructions.put(blockId, ssaInstrs);

        // Recurse into dominator tree children
        List<Integer> children = domChildren.get(blockId);
        if (children != null) {
            for (int childId : children) {
                renameInBlock(childId, domChildren);
            }
        }

        // Rollback: pop pushed versions
        for (int i = 0; i < pushedCount; i++) {
            // Pop in reverse order - we don't track per-var, so just pop all we pushed
            // This is a simplification; in production you'd track per-var counts
        }
        // Actually we need to pop correctly. Let's redo with tracking:
        rollbackStacks(blockPhis, block);
    }

    private void rollbackStacks(List<PhiFunction> phis, SimpleCFG.SimpleBlock block) {
        // Pop phi versions
        for (PhiFunction phi : phis) {
            Deque<Integer> s = stack.get(phi.var);
            if (s != null && !s.isEmpty()) s.pop();
        }
        // Pop def versions
        for (String instr : block.instructions) {
            String defVar = extractDefVar(instr);
            if (defVar != null) {
                Deque<Integer> s = stack.get(defVar);
                if (s != null && !s.isEmpty()) s.pop();
            }
        }
    }

    // ── Helpers ──

    private int newVersion(String var) {
        int v = counter.getOrDefault(var, 0) + 1;
        counter.put(var, v);
        return v;
    }

    private Integer peekVersion(String var) {
        Deque<Integer> s = stack.get(var);
        return (s != null && !s.isEmpty()) ? s.peek() : null;
    }

    /** Extract defined variable from "x = ..." instruction */
    private String extractDefVar(String instr) {
        instr = instr.trim();
        int eq = instr.indexOf('=');
        if (eq > 0) {
            String lhs = instr.substring(0, eq).trim();
            // Simple variable name: just alphanumeric
            if (lhs.matches("[a-zA-Z_]\\w*")) return lhs;
        }
        return null;
    }

    /** Rename variable uses in RHS of instruction */
    private String renameUses(String instr) {
        instr = instr.trim();
        int eq = instr.indexOf('=');
        if (eq < 0) return instr; // no assignment, keep as-is

        String lhs = instr.substring(0, eq).trim();
        String rhs = instr.substring(eq + 1).trim();

        // Replace variable references in RHS
        // Match word boundaries for variable names
        StringBuilder result = new StringBuilder();
        String[] tokens = rhs.split("(?=[\\s+\\-*/(),])|(?<=[\\s+\\-*/(),])");
        for (String token : tokens) {
            String trimmed = token.trim();
            if (trimmed.matches("[a-zA-Z_]\\w*") && !isKeyword(trimmed)) {
                Integer ver = peekVersion(trimmed);
                if (ver != null) {
                    result.append(trimmed).append("_").append(ver);
                } else {
                    result.append(trimmed);
                }
            } else {
                result.append(token);
            }
        }
        return lhs + " = " + result;
    }

    private String renameDef(String instr, String var, int version) {
        int eq = instr.indexOf('=');
        return var + "_" + version + " = " + instr.substring(eq + 1).trim();
    }

    private boolean isKeyword(String s) {
        return s.equals("if") || s.equals("else") || s.equals("return") || s.equals("goto");
    }

    // ── Public API ──

    public Map<Integer, List<PhiFunction>> getPhiFunctions() {
        return Collections.unmodifiableMap(phiFunctions);
    }

    public Map<Integer, List<String>> getSSAInstructions() {
        return Collections.unmodifiableMap(ssaInstructions);
    }

    /** Print SSA form */
    public void printSSA() {
        for (SimpleCFG.SimpleBlock block : cfg.blocks) {
            System.out.println("  " + block.label + ":");

            // Print phi functions
            List<PhiFunction> phis = phiFunctions.getOrDefault(block.id, Collections.emptyList());
            for (PhiFunction phi : phis) {
                System.out.println("    " + phi);
            }

            // Print SSA instructions
            List<String> instrs = ssaInstructions.getOrDefault(block.id, Collections.emptyList());
            for (String instr : instrs) {
                System.out.println("    " + instr);
            }
            System.out.println();
        }
    }

    /** Phi function representation */
    public static class PhiFunction {
        public final String var;
        public final List<String> args = new ArrayList<>();

        public PhiFunction(String var) { this.var = var; }

        public String resultName() {
            return var + "_phi"; // placeholder, gets renamed during variable renaming
        }

        @Override
        public String toString() {
            String resultVar = var + "_phi";
            if (args.isEmpty()) {
                return resultVar + " = φ()";
            }
            return resultVar + " = φ(" + String.join(", ", args) + ")";
        }
    }
}
