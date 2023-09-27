package org.teachfx.antlr4.ep20.ast;

import org.teachfx.antlr4.ep20.ast.decl.FuncDeclNode;
import org.teachfx.antlr4.ep20.ast.decl.VarDeclNode;
import org.teachfx.antlr4.ep20.ast.expr.*;
import org.teachfx.antlr4.ep20.ast.stmt.*;
import org.teachfx.antlr4.ep20.ast.type.TypeNode;

public interface ASTVisitor {
    /// Root Node
    void visit(CompileUnit rootNode);

    /// Decl
    void visit(VarDeclNode varDeclNode);
    void visit(FuncDeclNode funcDeclNode);
    void visit(VarDeclStmtNode varDeclStmtNode);
    /// Type
    void visit(TypeNode typeNode);
    // Expr
    void visit(BinaryExprNode binaryExprNode);
    void visit(IDExprNode idExprNode);

    /// literal value
    void visit(BoolExprNode boolExprNode);
    void visit(IntExprNode intExprNode);
    void visit(FloatExprNode floatExprNode);
    void visit(NullExprNode nullExprNode);
    void visit(StringExprNode stringExprNode);
    void visit(UnaryExprNode unaryExprNode);

    /// Stmt
    void visit(IfStmtNode ifStmtNode);
    void visit(ExprStmtNode exprStmtNode);
    void visit(BlockStmtNode blockStmtNode);
    void visit(ReturnStmtNode returnStmtNode);
    void visit(WhileStmtNode whileStmtNode);
    void visit(AssignStmtNode assignStmtNode);
}
