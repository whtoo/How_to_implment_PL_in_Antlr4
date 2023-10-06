package org.teachfx.antlr4.ep12.visitor;

import org.teachfx.antlr4.ep12.ast.*;
import org.teachfx.antlr4.ep12.parser.MathBaseVisitor;
import org.teachfx.antlr4.ep12.parser.MathLexer;
import org.teachfx.antlr4.ep12.parser.MathParser.*;

public class BuildAstVisitor extends MathBaseVisitor<ExpressionNode> {
    @Override
    public ExpressionNode visitCompileUnit(CompileUnitContext ctx) {
        if (ctx.assign() != null) return visit(ctx.assign());

        return visit(ctx.expr());
    }

    @Override
    public ExpressionNode visitNumberExpr(NumberExprContext ctx) {
        return new NumberNode(Double.parseDouble(ctx.value.getText()));
    }

    @Override
    public ExpressionNode visitParensExpr(ParensExprContext ctx) {
        return visit(ctx.expr());
    }

    @Override
    public ExpressionNode visitInfixExpr(InfixExprContext ctx) {
        InfixExpressionNode node;
        switch (ctx.op.getType()) {
            case MathLexer.OP_ADD:
                node = new AdditionNode();
                break;
            case MathLexer.OP_DIV:
                node = new DivisionNode();
                break;
            case MathLexer.OP_MUL:
                node = new MultiplicationNode();
                break;
            case MathLexer.OP_SUB:
                node = new SubtractionNode();
                break;
            default:
                node = null;
        }
        node.left = visit(ctx.left);
        node.right = visit(ctx.right);

        return node;
    }

    @Override
    public ExpressionNode visitUnaryExpr(UnaryExprContext ctx) {
        switch (ctx.op.getType()) {
            case MathLexer.OP_ADD:
                return visit(ctx.expr());
            case MathLexer.OP_SUB:
                return new NegateNode(visit(ctx.expr()));
        }
        return null;
    }

    @Override
    public ExpressionNode visitAssignExpr(AssignExprContext ctx) {
        return new AssignNode(ctx.name.getText(), visit(ctx.value));
    }

    @Override
    public ExpressionNode visitVarExpr(VarExprContext ctx) {
        return new VarNode(ctx.var.getText());
    }
}