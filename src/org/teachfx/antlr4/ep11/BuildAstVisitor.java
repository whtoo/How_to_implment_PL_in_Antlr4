package org.teachfx.antlr4.ep11;

import org.teachfx.antlr4.ep11.MathParser.*;

// public abstract class ExpressionNode
// {
// }

// public abstract class InfixExpressionNode : ExpressionNode
// {
//     public ExpressionNode Left { get; set; }
//     public ExpressionNode Right { get; set; }
// }

// public class AdditionNode : InfixExpressionNode
// {
// }

// public class SubtractionNode : InfixExpressionNode
// {
// }

// public class MultiplicationNode : InfixExpressionNode
// {
// }

// public class DivisionNode : InfixExpressionNode
// {
// }

// public class NegateNode : ExpressionNode
// {
//     public ExpressionNode InnerNode { get; set; }
// }

// public class FunctionNode : ExpressionNode
// {
//     public Func<double, double> Function { get; set; }
//     public ExpressionNode Argument { get; set; }
// }

// public class NumberNode : ExpressionNode
// {
//     public double Value { get; set; }
// }

public class BuildAstVisitor extends MathBaseVisitor<ExpressionNode> {
   @Override
   public ExpressionNode visitCompileUnit(CompileUnitContext ctx) {
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
        switch(ctx.op.getType()){
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
        switch(ctx.op.getType()){
            case MathLexer.OP_ADD:
                return visit(ctx.expr());
            case MathLexer.OP_SUB:
                return new NegateNode(visit(ctx.expr()));
        }
        return null;
    }
}