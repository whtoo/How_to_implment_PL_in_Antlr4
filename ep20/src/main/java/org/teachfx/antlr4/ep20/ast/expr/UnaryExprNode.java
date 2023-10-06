package org.teachfx.antlr4.ep20.ast.expr;

import org.antlr.v4.runtime.ParserRuleContext;
import org.teachfx.antlr4.ep20.ast.ASTVisitor;
import org.teachfx.antlr4.ep20.debugger.ast.Dumper;
import org.teachfx.antlr4.ep20.symtab.type.OperatorType.UnaryOpType;

import java.util.List;

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
}
