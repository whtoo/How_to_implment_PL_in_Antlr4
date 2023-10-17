package org.teachfx.antlr4.ep20.ast.stmt;


import org.antlr.v4.runtime.ParserRuleContext;
import org.teachfx.antlr4.ep20.ast.ASTVisitor;
import org.teachfx.antlr4.ep20.ast.expr.ExprNode;
import org.teachfx.antlr4.ep20.debugger.ast.Dumper;

import java.util.Optional;

public class IfStmtNode extends StmtNode {
    private ExprNode conditionalNode;
    private StmtNode thenBlock;

    private StmtNode elseBlock;

    public IfStmtNode(ExprNode exprNode, StmtNode then, StmtNode elseBlock, ParserRuleContext ctx) {
        this.conditionalNode = exprNode;
        this.thenBlock = then;
        this.elseBlock = elseBlock;
        this.ctx = ctx;
    }

    public ExprNode getCondExpr() {
        return conditionalNode;
    }

    public void setConditionalNode(ExprNode conditionalNode) {
        this.conditionalNode = conditionalNode;
    }

    public StmtNode getThenBlock() {
        return thenBlock;
    }

    public void setThenBlock(StmtNode thenBlock) {
        this.thenBlock = thenBlock;
    }

    public Optional<StmtNode> getElseBlock() {
        return Optional.ofNullable(elseBlock);
    }

    public void setElseBlock(StmtNode elseBlock) {
        this.elseBlock = elseBlock;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    protected void _dump(Dumper d) {
        super._dump(d);
        d.printMember("cond",conditionalNode);
        d.printMember("then",thenBlock);
        if(elseBlock != null) d.printMember("else",elseBlock);
    }
}
