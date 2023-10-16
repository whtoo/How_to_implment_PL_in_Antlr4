package org.teachfx.antlr4.ep21.ast.stmt;

import org.antlr.v4.runtime.ParserRuleContext;
import org.teachfx.antlr4.ep21.ast.ASTVisitor;
import org.teachfx.antlr4.ep21.ast.expr.ExprNode;
import org.teachfx.antlr4.ep21.debugger.ast.Dumper;

public class ReturnStmtNode extends StmtNode{
    private ExprNode retNode;

    public ReturnStmtNode(ExprNode retNode, ParserRuleContext ctx){
        this.retNode = retNode;
        this.ctx = ctx;
    }

    public boolean hasRetVal() {
        return  retNode != null;
    }

    public ExprNode getRetNode() {
        return retNode;
    }

    public void setRetNode(ExprNode retNode) {
        this.retNode = retNode;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    protected void _dump(Dumper d) {
        super._dump(d);
        if(retNode != null) d.printMember("retExpr",retNode);
    }
}
