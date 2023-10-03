package org.teachfx.antlr4.ep20.ast.expr;

import org.antlr.v4.runtime.ParserRuleContext;
import org.teachfx.antlr4.ep20.ast.ASTVisitor;
import org.teachfx.antlr4.ep20.debugger.ast.Dumper;
import org.teachfx.antlr4.ep20.symtab.OperatorType;
import org.teachfx.antlr4.ep20.symtab.Type;

public class BinaryExprNode extends ExprNode{

    public BinaryExprNode(OperatorType.BinaryOpType opType, ExprNode lhs, ExprNode rhs, ParserRuleContext ctx) {
        this.lhs = lhs;
        this.rhs = rhs;
        this.ctx = ctx;
        this.opType = opType;
    }

    public static BinaryExprNode createAddNode(ExprNode lhs, ExprNode rhs, ParserRuleContext ctx) {
        return new BinaryExprNode(OperatorType.BinaryOpType.ADD,lhs,rhs,ctx);
    }

    public static BinaryExprNode createMinNode(ExprNode lhs, ExprNode rhs, ParserRuleContext ctx) {
        return new BinaryExprNode(OperatorType.BinaryOpType.SUB,lhs,rhs,ctx);
    }

    public static BinaryExprNode createMulNode(ExprNode lhs, ExprNode rhs, ParserRuleContext ctx) {
        return new BinaryExprNode(OperatorType.BinaryOpType.MUL,lhs,rhs,ctx);
    }

    public static BinaryExprNode createDivNode(ExprNode lhs, ExprNode rhs, ParserRuleContext ctx) {
        return new BinaryExprNode(OperatorType.BinaryOpType.DIV,lhs,rhs,ctx);
    }

    @Override
    public Type getExprType() {
        return lhs.getExprType();
    }

    public OperatorType.BinaryOpType getOpType() {
        return opType;
    }

    public void setOpType(OperatorType.BinaryOpType opType) {
        this.opType = opType;
    }

    private OperatorType.BinaryOpType opType;
    private ExprNode lhs;

    private ExprNode rhs;
    public ExprNode getLhs() {
        return lhs;
    }

    public void setLhs(ExprNode lhs) {
        this.lhs = lhs;
    }

    public ExprNode getRhs() {
        return rhs;
    }

    public void setRhs(ExprNode rhs) {
        this.rhs = rhs;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    protected void _dump(Dumper d) {
        super._dump(d);
        d.printMember("lhs",lhs);

        d.printMember("operator",opType.getOpRawVal());

        d.printMember("rhs",rhs);

    }
}
