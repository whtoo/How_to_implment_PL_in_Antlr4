package org.teachfx.antlr4.ep4;

import org.teachfx.antlr4.ep4.LabeledExprParser.*;

import java.util.HashMap;
import java.util.Map;

public class EvalVisitor extends LabeledExprBaseVisitor<Integer>{
    Map<String,Integer> memory = new HashMap<>();
    
    /** ID '=' expr NEWLINE */
    @Override
    public Integer visitAssign(AssignContext ctx) {
        String id = ctx.ID().getText();
        int value =  visit(ctx.expr());
        memory.put(id, value);
        return value;
    }    
    /** expr NEWLINE */
    @Override
    public Integer visitPrintExpr(PrintExprContext ctx) {
        Integer value = visit(ctx.expr());
        System.out.println(value);
        return 0;
    }
    /** INT */
    @Override
    public Integer visitInt(IntContext ctx) {
        return Integer.valueOf(ctx.INT().getText());
    }
    /** ID */
    @Override
    public Integer visitId(IdContext ctx) {
        String id = ctx.ID().getText();
        if (memory.containsKey(id)) return memory.get(id);
        return 0;
    }
    /** expr op=('*'|'/') expr */
    @Override
    public Integer visitMulDiv(MulDivContext ctx) {
        int left = visit(ctx.expr(0));
        int right = visit(ctx.expr(1));
        if (ctx.op.getType() == LabeledExprParser.MUL) return left * right;
        return left / right;
    }

    @Override
    public Integer visitAddSub(AddSubContext ctx) {
        int left = visit(ctx.expr(0));
        int right = visit(ctx.expr(1));
        if (ctx.op.getType() == LabeledExprParser.ADD) return left + right;
        return left - right;
    }

    @Override
    public Integer visitParens(ParensContext ctx) {
        return visit(ctx.expr());
    }

}
