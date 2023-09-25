package org.teachfx.antlr4.ep20.ast;

import org.teachfx.antlr4.ep20.ast.decl.*;
import org.teachfx.antlr4.ep20.ast.expr.*;
import org.teachfx.antlr4.ep20.ast.stmt.*;
import org.teachfx.antlr4.ep20.ast.type.*;

public interface ASTVisitor {
    void visit(BinaryExprNode binaryExprNode);

    void visit(ExprStmtNode exprStmtNode);

    void visit(FuncDeclNode funcDeclNode);
    void visit(IDExprNode idExprNode);
    void visit(IfStmtNode ifStmtNode);
    /// literal value
    void visit(BoolExprNode boolExprNode);
    void visit(IntExprNode intExprNode);
    void visit(FloatExprNode floatExprNode);
    void visit(NullExprNode nullExprNode);
    void visit(StringExprNode stringExprNode);
    /// type specifier
    void visit(TypeNode typeNode);

    void visit(BlockStmtNode blockStmtNode);
    void visit(ReturnStmtNode returnStmtNode);
    void visit(UnaryExprNode unaryExprNode);
    void visit(VarDeclNode varDeclNode);
    void visit(WhileStmtNode whileStmtNode);
    void visit(VarDeclStmtNode varDeclStmtNode);

    void visit(AssignStmtNode assignStmtNode);
}
