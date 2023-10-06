package org.teachfx.antlr4.ep13;

import org.teachfx.antlr4.ep13.ast.*;

public interface ASTVisitor<T> {
    
    // Stmt

    T visit(AssignNode node);

    T visit(VarNode node);

    // Expr
    T visit(AdditionNode node);

    T visit(SubtractionNode node);

    T visit(MultiplicationNode node);

    T visit(DivisionNode node);

    T visit(NegateNode node);

    T visit(NumberNode node);

    T visit(ExpressionNode node);
}
