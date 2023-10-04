package org.teachfx.antlr4.ep20.ast;

import org.teachfx.antlr4.ep20.ast.decl.FuncDeclNode;
import org.teachfx.antlr4.ep20.ast.decl.VarDeclNode;
import org.teachfx.antlr4.ep20.ast.expr.*;
import org.teachfx.antlr4.ep20.ast.stmt.*;
import org.teachfx.antlr4.ep20.ast.type.TypeNode;
import org.teachfx.antlr4.ep20.ir.IRNode;

public interface ASTVisitor<T> {
    /// Root Node
    T visit(CompileUnit rootNode);

    /// Decl
    T visit(VarDeclNode varDeclNode);

    T visit(FuncDeclNode funcDeclNode);

    T visit(VarDeclStmtNode varDeclStmtNode);

    /// Type
    T visit(TypeNode typeNode);

    // Expr
    default T visit(ExprNode node) {
        if (node instanceof BinaryExprNode) {
            return visit((BinaryExprNode) node);
        } else if (node instanceof IDExprNode) {
            return visit((IDExprNode) node);
        } else if (node instanceof BoolExprNode) {
            return visit((BoolExprNode) node);
        } else if (node instanceof CallFuncNode) {
            return visit((CallFuncNode) node);
        } else if (node instanceof IntExprNode) {
            return visit((IntExprNode) node);
        } else if (node instanceof FloatExprNode) {
            return visit((FloatExprNode) node);
        } else if (node instanceof NullExprNode) {
            return visit((NullExprNode) node);
        } else if (node instanceof StringExprNode) {
            return visit((StringExprNode) node);
        } else if (node instanceof UnaryExprNode) {
            return visit((UnaryExprNode) node);
        }
        return null;
    }
    T visit(BinaryExprNode binaryExprNode);

    T visit(IDExprNode idExprNode);

    /// literal value
    T visit(BoolExprNode boolExprNode);

    T visit(CallFuncNode callExprNode);

    T visit(IntExprNode intExprNode);

    T visit(FloatExprNode floatExprNode);

    T visit(NullExprNode nullExprNode);

    T visit(StringExprNode stringExprNode);

    T visit(UnaryExprNode unaryExprNode);

    /// Stmt
    T visit(IfStmtNode ifStmtNode);

    T visit(ExprStmtNode exprStmtNode);

    T visit(BlockStmtNode blockStmtNode);

    T visit(ReturnStmtNode returnStmtNode);

    T visit(WhileStmtNode whileStmtNode);

    T visit(AssignStmtNode assignStmtNode);

    default T visit(StmtNode node) {
        if (node instanceof IfStmtNode) {
            return visit((IfStmtNode) node);
        } else if (node instanceof WhileStmtNode) {
            return visit((WhileStmtNode) node);
        } else if (node instanceof BlockStmtNode) {
            return visit((BlockStmtNode) node);
        } else if (node instanceof ExprStmtNode) {
            return visit((ExprStmtNode) node);
        } else if (node instanceof ReturnStmtNode) {
            return visit((ReturnStmtNode) node);
        } else if (node instanceof AssignStmtNode) {
            return visit((AssignStmtNode) node);
        } else {
            return visit((VarDeclStmtNode) node);
        }
    }
}
