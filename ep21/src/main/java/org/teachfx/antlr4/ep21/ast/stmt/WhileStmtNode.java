package org.teachfx.antlr4.ep21.ast.stmt;

import org.antlr.v4.runtime.ParserRuleContext;
import org.teachfx.antlr4.ep21.ast.ASTVisitor;
import org.teachfx.antlr4.ep21.ast.expr.ExprNode;
import org.teachfx.antlr4.ep21.debugger.ast.Dumper;

public class WhileStmtNode extends StmtNode{

    private ExprNode conditionNode;
    private BlockStmtNode blockNode;

    public WhileStmtNode(ExprNode conditionNode, BlockStmtNode blockNode, ParserRuleContext ctx) {
        this.conditionNode = conditionNode;
        this.blockNode = blockNode;
        this.ctx = ctx;
    }

    public ExprNode getConditionNode() {
        return conditionNode;
    }

    public void setConditionNode(ExprNode conditionNode) {
        this.conditionNode = conditionNode;
    }

    public BlockStmtNode getBlockNode() {
        return blockNode;
    }

    public void setBlockNode(BlockStmtNode blockNode) {
        this.blockNode = blockNode;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    protected void _dump(Dumper d) {
        super._dump(d);
        d.printMember("cond",conditionNode);
        d.printMember("then",blockNode);
    }
}
