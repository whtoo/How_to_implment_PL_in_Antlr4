package org.teachfx.antlr4.ep20.ast.stmt;


import org.antlr.v4.runtime.ParserRuleContext;
import org.teachfx.antlr4.ep20.ast.ASTVisitor;
import org.teachfx.antlr4.ep20.debugger.ast.Dumper;

public class BreakStmtNode extends StmtNode {
    public BreakStmtNode(ParserRuleContext ctx) {
        this.ctx = ctx;
    }

    @Override
    protected void _dump(Dumper d) {
        d.printMember("label", "break");
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

}
