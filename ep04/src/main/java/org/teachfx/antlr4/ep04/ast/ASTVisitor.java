package org.teachfx.antlr4.ep04.ast;

/**
 * ASTVisitor — generic Visitor interface for AST nodes.
 *
 * The Visitor pattern lets us add new operations to
 * the AST without changing node classes. Each operation
 * (printing, evaluation, code generation) is a separate
 * visitor implementation.
 *
 * @param <T> the return type of visit operations
 */
public interface ASTVisitor<T> {
    T visit(IntNode node);
    T visit(IdNode node);
    T visit(BinaryOpNode node);
    T visit(AssignNode node);
    T visit(PrintNode node);

    /** Fallback — dispatches to the correct typed visit method. */
    T visit(ASTNode node);
}
