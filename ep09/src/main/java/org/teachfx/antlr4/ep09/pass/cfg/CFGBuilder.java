package org.teachfx.antlr4.ep09.pass.cfg;

import org.teachfx.antlr4.ep09.ir.*;
import org.teachfx.antlr4.ep09.ir.stmt.*;
import java.util.*;

/** Builds CFG from IR nodes. */
public class CFGBuilder {

    public static CFG build(List<IRNode> instrs) {
        Set<Integer> leaders = new LinkedHashSet<>();
        leaders.add(0);
        Map<String,Integer> labelPos = new HashMap<>();
        for (int i = 0; i < instrs.size(); i++)
            if (instrs.get(i) instanceof Label lbl) {
                labelPos.put(lbl.toString(), i); leaders.add(i); }
        for (int i = 0; i < instrs.size(); i++) {
            IRNode n = instrs.get(i);
            if (n instanceof JMPInstr j) {
                Integer t = labelPos.get(j.getTarget().toString());
                if (t != null) leaders.add(t);
                if (i+1 < instrs.size()) leaders.add(i+1);
            }
        }
        List<Integer> sorted = new ArrayList<>(leaders); Collections.sort(sorted);
        List<BasicBlock> blocks = new ArrayList<>();
        Map<String,BasicBlock> labelBlock = new HashMap<>();
        for (int idx = 0; idx < sorted.size(); idx++) {
            int start = sorted.get(idx);
            int end = (idx+1 < sorted.size()) ? sorted.get(idx+1) : instrs.size();
            BasicBlock b = new BasicBlock(idx, new ArrayList<>(instrs.subList(start, end)));
            blocks.add(b);
            if (!b.instructions.isEmpty() && b.instructions.get(0) instanceof Label lbl)
                labelBlock.put(lbl.toString(), b);
        }
        for (int i = 0; i < blocks.size(); i++) {
            BasicBlock b = blocks.get(i);
            if (b.instructions.isEmpty()) continue;
            IRNode last = b.instructions.get(b.instructions.size()-1);
            if (last instanceof JMP j) {
                BasicBlock t = labelBlock.get(j.getTarget().toString());
                if (t != null) b.successors.add(t);
            } else if (i+1 < blocks.size() && !last.toString().toLowerCase().contains("ret"))
                b.successors.add(blocks.get(i+1));
        }
        for (BasicBlock b : blocks) b.predecessors = new ArrayList<>();
        for (BasicBlock b : blocks) for (BasicBlock s : b.successors) s.predecessors.add(b);
        return new CFG(blocks);
    }

    /** Always-works demo CFG for if-else pattern. */
    public static CFG demoIfElse() {
        List<BasicBlock> bs = new ArrayList<>();
        BasicBlock b0 = new BasicBlock(0, List.of()); b0.label = "B0: t0=x>5; br t0,B1,B2";
        BasicBlock b1 = new BasicBlock(1, List.of()); b1.label = "B1: y=1; jmp B3";
        BasicBlock b2 = new BasicBlock(2, List.of()); b2.label = "B2: y=0";
        BasicBlock b3 = new BasicBlock(3, List.of()); b3.label = "B3: return y";
        b0.successors.addAll(List.of(b1,b2));
        b1.predecessors.add(b0); b1.successors.add(b3);
        b2.predecessors.add(b0); b2.successors.add(b3);
        b3.predecessors.addAll(List.of(b1,b2));
        bs.addAll(List.of(b0,b1,b2,b3));
        CFG c = new CFG(bs); c.entryLabel = "if(x>5)y=1;else y=0;return y;";
        return c;
    }

    public static String toDot(CFG cfg) {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph CFG {\n  node [shape=box];\n");
        for (BasicBlock b : cfg.blocks) {
            String l = b.label != null ? b.label : "B"+b.id;
            sb.append("  B").append(b.id).append(" [label=\"").append(l).append("\"];\n");
        }
        for (BasicBlock b : cfg.blocks)
            for (BasicBlock s : b.successors)
                sb.append("  B").append(b.id).append(" -> B").append(s.id).append(";\n");
        sb.append("}\n");
        return sb.toString();
    }
}
