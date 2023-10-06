package org.teachfx.antlr4.ep8;

import java.util.List;

public class StatsNode extends ASTNode {

    List<StatNode> stats;

    public StatsNode(String rawText) {
        super(rawText);
        // TODO Auto-generated constructor stub
    }

    public StatsNode(List<StatNode> stats) {
        super("");
        this.stats = stats;
    }


}
