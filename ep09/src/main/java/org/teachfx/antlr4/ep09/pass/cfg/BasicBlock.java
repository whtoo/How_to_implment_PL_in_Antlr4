package org.teachfx.antlr4.ep09.pass.cfg;

import org.teachfx.antlr4.ep09.ir.*;
import java.util.*;

public class BasicBlock {
    public int id;
    public String label;
    public List<IRNode> instructions;
    public List<BasicBlock> predecessors = new ArrayList<>();
    public List<BasicBlock> successors = new ArrayList<>();
    public BasicBlock(int id, List<IRNode> instructions) { this.id = id; this.instructions = instructions; }
}
