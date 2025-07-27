package org.teachfx.antlr4.ep20.ast.expr;

import org.antlr.v4.runtime.ParserRuleContext;
import org.teachfx.antlr4.ep20.ast.ASTVisitor;
import org.teachfx.antlr4.ep20.ast.type.TypeNode;
import org.teachfx.antlr4.ep20.debugger.ast.Dumper;

public class CastExprNode extends ExprNode {
    private TypeNode targetType;
    private ExprNode expr;

    public CastExprNode(TypeNode targetType, ExprNode expr, ParserRuleContext ctx) {
        super();
        this.targetType = targetType;
        this.expr = expr;
        this.ctx = ctx;
    }

    public TypeNode getTargetType() {
        return targetType;
    }

    public void setTargetType(TypeNode targetType) {
        this.targetType = targetType;
    }

    public ExprNode getExpr() {
        return expr;
    }

    public void setExpr(ExprNode expr) {
        this.expr = expr;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    protected void _dump(Dumper d) {
        super._dump(d);
        d.printMember("targetType", targetType);
        d.printMember("expr", expr);
    }

    @Override
    public String toString() {
        return "CastExprNode{" +
                "targetType=" + targetType +
                ", expr=" + expr +
                '}';
    }
}