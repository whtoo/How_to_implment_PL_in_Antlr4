package org.teachfx.antlr4.ep20.pass.sematic;

import org.teachfx.antlr4.ep20.ast.decl.FuncDeclNode;
import org.teachfx.antlr4.ep20.ast.decl.VarDeclListNode;
import org.teachfx.antlr4.ep20.ast.decl.VarDeclNode;
import org.teachfx.antlr4.ep20.ast.expr.*;
import org.teachfx.antlr4.ep20.ast.stmt.*;
import org.teachfx.antlr4.ep20.ast.type.TypeNode;
import org.teachfx.antlr4.ep20.pass.ast.ASTBaseVisitor;

public class TypeChecker extends ASTBaseVisitor
{
    public TypeChecker()
    {

    }

    @Override
    public Void visit(IDExprNode idExprNode) {
        return super.visit(idExprNode);
    }

    @Override
    public Void visit(VarDeclNode varDeclNode) {
        return super.visit(varDeclNode);
    }

    @Override
    public Void visit(FuncDeclNode funcDeclNode) {
        return super.visit(funcDeclNode);
    }

    @Override
    public Void visit(VarDeclStmtNode varDeclStmtNode) {
        return super.visit(varDeclStmtNode);
    }

    @Override
    public Void visit(TypeNode typeNode) {
        return super.visit(typeNode);
    }

    @Override
    public Void visit(BinaryExprNode binaryExprNode) {
        return super.visit(binaryExprNode);
    }

    @Override
    public Void visit(BoolExprNode boolExprNode) {
        return super.visit(boolExprNode);
    }

    @Override
    public Void visit(CallFuncNode callExprNode) {
        return super.visit(callExprNode);
    }

    @Override
    public Void visit(UnaryExprNode unaryExprNode) {
        return super.visit(unaryExprNode);
    }

    @Override
    public Void visit(IfStmtNode ifStmtNode) {
        return super.visit(ifStmtNode);
    }

    @Override
    public Void visit(ExprStmtNode exprStmtNode) {
        return super.visit(exprStmtNode);
    }

    @Override
    public Void visit(BlockStmtNode blockStmtNode) {
        return super.visit(blockStmtNode);
    }

    @Override
    public Void visit(ReturnStmtNode returnStmtNode) {
        return super.visit(returnStmtNode);
    }

    @Override
    public Void visit(WhileStmtNode whileStmtNode) {
        return super.visit(whileStmtNode);
    }

    @Override
    public Void visit(AssignStmtNode assignStmtNode) {
        return super.visit(assignStmtNode);
    }
}
