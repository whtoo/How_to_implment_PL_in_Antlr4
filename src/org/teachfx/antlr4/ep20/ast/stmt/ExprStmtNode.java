package org.teachfx.antlr4.ep20.ast.stmt;

import org.antlr.v4.runtime.ParserRuleContext;
import org.teachfx.antlr4.ep20.ast.ASTVisitor;
import org.teachfx.antlr4.ep20.ast.expr.ExprNode;
import org.teachfx.antlr4.ep20.debugger.Dumper;

public class ExprStmtNode extends StmtNode{
    private ExprNode exprNode;

    public ExprStmtNode(ExprNode exprNode, ParserRuleContext ctx) {
        this.exprNode = exprNode;
        this.ctx = ctx;
    }

    public ExprNode getExprNode() {
        return exprNode;
    }

    public void setExprNode(ExprNode exprNode) {
        this.exprNode = exprNode;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    protected void _dump(Dumper d) {
        super._dump(d);
        d.printMember("expr",exprNode);
    }
}
