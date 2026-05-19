package org.teachfx.antlr4.ep09.pass.cfg;

import java.util.*;

public class CFG {
    public List<BasicBlock> blocks;
    public String entryLabel = "";
    public CFG(List<BasicBlock> blocks) { this.blocks = blocks; }
}
