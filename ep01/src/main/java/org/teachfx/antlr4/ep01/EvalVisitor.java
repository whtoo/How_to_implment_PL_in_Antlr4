package org.teachfx.antlr4.ep01;

import org.teachfx.antlr4.ep01.parser.CymbolBaseVisitor;
import org.teachfx.antlr4.ep01.parser.CymbolLexer;
import org.teachfx.antlr4.ep01.parser.CymbolParser;

import java.util.HashMap;
import java.util.Map;

/**
 * Walks the parse tree and evaluates expressions.
 * Uses a simple HashMap as the "memory" for variables.
 */
public class EvalVisitor extends CymbolBaseVisitor<Integer> {

    /** Variable memory — maps name → value. */
    private final Map<String, Integer> memory = new HashMap<>();

    // === Statements ===

    @Override
    public Integer visitPrintExpr(CymbolParser.PrintExprContext ctx) {
        int value = visit(ctx.expr());
        System.out.println(value);
        return value;
    }

    @Override
    public Integer visitAssign(CymbolParser.AssignContext ctx) {
        String name = ctx.ID().getText();
        int value = visit(ctx.expr());
        memory.put(name, value);
        return value;
    }

    // === Expressions ===

    @Override
    public Integer visitInt(CymbolParser.IntContext ctx) {
        return Integer.parseInt(ctx.INT().getText());
    }

    @Override
    public Integer visitId(CymbolParser.IdContext ctx) {
        String name = ctx.ID().getText();
        return memory.getOrDefault(name, 0);
    }

    @Override
    public Integer visitParens(CymbolParser.ParensContext ctx) {
        return visit(ctx.expr());
    }

    @Override
    public Integer visitMulDiv(CymbolParser.MulDivContext ctx) {
        int left = visit(ctx.expr(0));
        int right = visit(ctx.expr(1));
        return ctx.op.getType() == CymbolLexer.MUL ? left * right : left / right;
    }

    @Override
    public Integer visitAddSub(CymbolParser.AddSubContext ctx) {
        int left = visit(ctx.expr(0));
        int right = visit(ctx.expr(1));
        return ctx.op.getType() == CymbolLexer.ADD ? left + right : left - right;
    }
}
