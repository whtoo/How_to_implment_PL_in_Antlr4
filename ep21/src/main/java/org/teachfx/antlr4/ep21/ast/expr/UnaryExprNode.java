package org.teachfx.antlr4.ep21.ast.expr;

import org.antlr.v4.runtime.ParserRuleContext;
import org.teachfx.antlr4.ep21.ast.ASTVisitor;
import org.teachfx.antlr4.ep21.debugger.ast.Dumper;
import org.teachfx.antlr4.ep21.symtab.type.OperatorType.UnaryOpType;

import java.util.List;
import java.util.Objects;

public class UnaryExprNode extends ExprNode{
    protected ExprNode valExpr;
    protected UnaryOpType opType;

    public UnaryExprNode(UnaryOpType opType, ExprNode valExpr, ParserRuleContext ctx) {
        this.opType = opType;
        this.valExpr = valExpr;
        this.ctx = ctx;
    }

    public ExprNode getValExpr() {
        return valExpr;
    }

    public void setValExpr(ExprNode valExpr) {
        this.valExpr = valExpr;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    protected void _dump(Dumper d) {
        super._dump(d);
        d.printMember("operator",opType.getOpRawVal());
        d.printNodeList("val", List.of(valExpr));
    }

    public UnaryOpType getOpType() {
        return opType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UnaryExprNode that)) return false;
        return Objects.equals(getValExpr(), that.getValExpr()) && getOpType() == that.getOpType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getValExpr(), getOpType());
    }
}
