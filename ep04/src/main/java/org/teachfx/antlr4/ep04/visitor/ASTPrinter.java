package org.teachfx.antlr4.ep04.visitor;

import org.teachfx.antlr4.ep04.ast.*;

/**
 * ASTPrinter — pretty-prints the AST with indentation.
 */
public class ASTPrinter implements ASTVisitor<Void> {

    @Override
    public Void visit(IntNode node) {
        node.print("");
        return null;
    }

    @Override
    public Void visit(IdNode node) {
        node.print("");
        return null;
    }

    @Override
    public Void visit(BinaryOpNode node) {
        node.print("");
        return null;
    }

    @Override
    public Void visit(AssignNode node) {
        node.print("");
        return null;
    }

    @Override
    public Void visit(PrintNode node) {
        node.print("");
        return null;
    }

    @Override
    public Void visit(ASTNode node) {
        return node.accept(this);
    }
}
