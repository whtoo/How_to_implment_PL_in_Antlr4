package org.teachfx.antlr4.ep12.visitor;

import org.teachfx.antlr4.ep12.ast.*;

public interface ASTVisitor<T> {
    T visit(AdditionNode node);

    T visit(SubtractionNode node);

    T visit(MultiplicationNode node);

    T visit(DivisionNode node);

    T visit(NegateNode node);

    T visit(NumberNode node);

    T visit(AssignNode node);

    T visit(VarNode node);

    T visit(ExpressionNode node);
}
