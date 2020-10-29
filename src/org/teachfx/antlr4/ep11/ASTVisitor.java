package org.teachfx.antlr4.ep11;

public abstract class ASTVisitor<T>
{
    public abstract T visit(AdditionNode node);
    public abstract T visit(SubtractionNode node);
    public abstract T visit(MultiplicationNode node);
    public abstract T visit(DivisionNode node);
    public abstract T visit(NegateNode node);
    public abstract T visit(NumberNode node);

    public abstract T visit(ExpressionNode node);
}
