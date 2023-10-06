package org.teachfx.antlr4.ep11;

public class EvalExprVisitor extends ASTVisitor<Double> {

    @Override
    public Double visit(AdditionNode node) {
        return visit(node.left) + visit(node.right);
    }

    @Override
    public Double visit(SubtractionNode node) {
        return visit(node.left) - visit(node.right);
    }

    @Override
    public Double visit(MultiplicationNode node) {
        return visit(node.left) * visit(node.right);
    }

    @Override
    public Double visit(DivisionNode node) {
        return visit(node.left) / visit(node.right);
    }

    @Override
    public Double visit(NegateNode node) {
        return visit(node.innerNode) * (-1);
    }

    @Override
    public Double visit(NumberNode node) {
        return node.value;
    }

    @Override
    public Double visit(ExpressionNode node) {
        if (node.getClass().equals(AdditionNode.class)) {
            return visit((AdditionNode) node);
        } else if (node.getClass().equals(SubtractionNode.class)) {
            return visit((SubtractionNode) node);
        } else if (node.getClass().equals(MultiplicationNode.class)) {
            return visit((MultiplicationNode) node);
        } else if (node.getClass().equals(DivisionNode.class)) {
            return visit((DivisionNode) node);
        } else if (node.getClass().equals(NegateNode.class)) {
            return visit((NegateNode) node);
        } else if (node.getClass().equals(NumberNode.class)) {
            return visit((NumberNode) node);
        }
        return null;
    }


}
