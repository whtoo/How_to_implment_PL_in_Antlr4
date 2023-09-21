package org.teachfx.antlr4.ep20.ast.expr;

import org.antlr.v4.runtime.ParserRuleContext;
import org.teachfx.antlr4.ep20.symtab.OperatorType;

public class BinaryNode extends ExprNode{

    public BinaryNode(OperatorType.BinaryOpType opType,ExprNode lhs,ExprNode rhs,ParserRuleContext ctx) {
        this.lhs = lhs;
        this.rhs = rhs;
        this.ctx = ctx;
        this.opType = opType;
    }

    public static BinaryNode createAddNode(ExprNode lhs,ExprNode rhs,ParserRuleContext ctx) {
        return new BinaryNode(OperatorType.BinaryOpType.ADD,lhs,rhs,ctx);
    }

    public static BinaryNode createMinNode(ExprNode lhs,ExprNode rhs,ParserRuleContext ctx) {
        return new BinaryNode(OperatorType.BinaryOpType.MIN,lhs,rhs,ctx);
    }

    public static BinaryNode createMulNode(ExprNode lhs,ExprNode rhs,ParserRuleContext ctx) {
        return new BinaryNode(OperatorType.BinaryOpType.MUL,lhs,rhs,ctx);
    }

    public static BinaryNode createDivNode(ExprNode lhs,ExprNode rhs,ParserRuleContext ctx) {
        return new BinaryNode(OperatorType.BinaryOpType.DIV,lhs,rhs,ctx);
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
    public void setCtx(ParserRuleContext ctx) {
        this.ctx = ctx;
    }
}
