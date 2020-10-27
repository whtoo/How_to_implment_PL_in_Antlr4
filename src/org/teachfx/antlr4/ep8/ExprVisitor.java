package org.teachfx.antlr4.ep8;

import org.teachfx.antlr4.ep8.VecMathParser.AssignContext;
import org.teachfx.antlr4.ep8.VecMathParser.ExprContext;
import org.teachfx.antlr4.ep8.VecMathParser.StatContext;
import org.teachfx.antlr4.ep8.VecMathParser.StatlistContext;

public class ExprVisitor extends VecMathBaseVisitor<ExprNode> {
    @Override
    public ExprNode visitStatlist(StatlistContext ctx) {
        // TODO Auto-generated method stub
        return super.visitStatlist(ctx);
    }

    @Override
    public ExprNode visitAssign(AssignContext ctx) {
        // TODO Auto-generated method stub
        return super.visitAssign(ctx);
    }

    @Override
    public ExprNode visitStat(StatContext ctx) {
        // TODO Auto-generated method stub
        return super.visitStat(ctx);
    }

    @Override
    public ExprNode visitExpr(ExprContext ctx) {
        // TODO Auto-generated method stub
        return super.visitExpr(ctx);
    }
}
