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
    
}
