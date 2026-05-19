package org.teachfx.antlr4.ep04.visitor;

import org.teachfx.antlr4.ep04.ast.*;
import org.teachfx.antlr4.ep04.parser.CymbolBaseVisitor;
import org.teachfx.antlr4.ep04.parser.CymbolParser;

/**
 * ASTBuilder — converts ANTLR ParseTree into custom AST.
 *
 * This is where we strip parser artifacts (punctuation, rule nodes)
 * and build a clean semantic tree.
 */
public class ASTBuilder extends CymbolBaseVisitor<ASTNode> {

    @Override
    public ASTNode visitProg(CymbolParser.ProgContext ctx) {
        // Return the last statement — the Compiler will walk children
        return visit(ctx.stat(0));
    }

    @Override
    public ASTNode visitPrintExpr(CymbolParser.PrintExprContext ctx) {
        return new PrintNode(visit(ctx.expr()));
    }

    @Override
    public ASTNode visitAssign(CymbolParser.AssignContext ctx) {
        return new AssignNode(ctx.ID().getText(), visit(ctx.expr()));
    }

    @Override
    public ASTNode visitInt(CymbolParser.IntContext ctx) {
        return new IntNode(Integer.parseInt(ctx.INT().getText()));
    }

    @Override
    public ASTNode visitId(CymbolParser.IdContext ctx) {
        return new IdNode(ctx.ID().getText());
    }

    @Override
    public ASTNode visitParens(CymbolParser.ParensContext ctx) {
        return visit(ctx.expr());
    }

    @Override
    public ASTNode visitMulDiv(CymbolParser.MulDivContext ctx) {
        return new BinaryOpNode(visit(ctx.expr(0)), ctx.op.getText(), visit(ctx.expr(1)));
    }

    @Override
    public ASTNode visitAddSub(CymbolParser.AddSubContext ctx) {
        return new BinaryOpNode(visit(ctx.expr(0)), ctx.op.getText(), visit(ctx.expr(1)));
    }
}
