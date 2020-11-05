package org.teachfx.antlr4.ep13;
import org.teachfx.antlr4.ep13.ast.*;
public interface  ASTVisitor<T>
{
    public  T visit(AdditionNode node);
    public  T visit(SubtractionNode node);
    public  T visit(MultiplicationNode node);
    public  T visit(DivisionNode node);
    public  T visit(NegateNode node);
    public  T visit(NumberNode node);
    public  T visit(AssignNode node);
    public  T visit(VarNode node);
    public  T visit(ExpressionNode node);
}
