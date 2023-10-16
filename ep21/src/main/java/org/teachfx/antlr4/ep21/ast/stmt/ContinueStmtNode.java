package org.teachfx.antlr4.ep21.ast.stmt;

import org.antlr.v4.runtime.ParserRuleContext;
import org.teachfx.antlr4.ep21.ast.ASTVisitor;
import org.teachfx.antlr4.ep21.debugger.ast.Dumper;

public class ContinueStmtNode extends StmtNode {
    public ContinueStmtNode(ParserRuleContext ctx) {
        this.ctx = ctx;
    }
    @Override
    protected void _dump(Dumper d) {
        d.printMember("label", "continue");
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
