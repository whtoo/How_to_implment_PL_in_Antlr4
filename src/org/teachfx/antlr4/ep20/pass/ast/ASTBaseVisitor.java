package org.teachfx.antlr4.ep20.pass.ast;

import org.teachfx.antlr4.ep20.ast.ASTVisitor;
import org.teachfx.antlr4.ep20.ast.CompileUnit;
import org.teachfx.antlr4.ep20.ast.decl.FuncDeclNode;
import org.teachfx.antlr4.ep20.ast.decl.VarDeclNode;
import org.teachfx.antlr4.ep20.ast.expr.*;
import org.teachfx.antlr4.ep20.ast.stmt.*;
import org.teachfx.antlr4.ep20.ast.type.TypeNode;

public class ASTBaseVisitor implements ASTVisitor {

    @Override
    public void visit(CompileUnit rootNode) {

        // for-each varDeclNode in varDeclarations of rootNode
        for (var varDeclNode : rootNode.getVarDeclarations()) {
            varDeclNode.accept(this);
        }
        // for-each funcDeclNode in funcDeclarations of rootNode
        for (var funcDeclNode : rootNode.getFuncDeclarations()) {
            funcDeclNode.accept(this);
        }

    }

    @Override
    public void visit(VarDeclNode varDeclNode) {

    }

    @Override
    public void visit(FuncDeclNode funcDeclNode) {
        for( var param : funcDeclNode.getParamSlots().getVarDeclNodeList()) {
            param.accept(this);
        }

        funcDeclNode.getBody().accept(this);
    }

    @Override
    public void visit(VarDeclStmtNode varDeclStmtNode) {
        varDeclStmtNode.getVarDeclNode().accept(this);
    }

    @Override
    public void visit(TypeNode typeNode) {
        typeNode.accept(this);
    }

    @Override
    public void visit(BinaryExprNode binaryExprNode) {
        binaryExprNode.getLhs().accept(this);
        binaryExprNode.getRhs().accept(this);
    }

    @Override
    public void visit(IDExprNode idExprNode) {

    }

    @Override
    public void visit(BoolExprNode boolExprNode) {

    }

    @Override
    public void visit(IntExprNode intExprNode) {

    }

    @Override
    public void visit(FloatExprNode floatExprNode) {

    }

    @Override
    public void visit(NullExprNode nullExprNode) {

    }

    @Override
    public void visit(StringExprNode stringExprNode) {

    }

    @Override
    public void visit(UnaryExprNode unaryExprNode) {
        unaryExprNode.getValExpr().accept(this);
    }

    @Override
    public void visit(IfStmtNode ifStmtNode) {
        ifStmtNode.getConditionalNode().accept(this);
        ifStmtNode.getThenBlock().accept(this);
        ifStmtNode.getElseBlock().accept(this);
    }

    @Override
    public void visit(ExprStmtNode exprStmtNode) {
        exprStmtNode.getExprNode().accept(this);
    }

    @Override
    public void visit(BlockStmtNode blockStmtNode) {
        blockStmtNode.getStmtNodes().forEach(stmt -> stmt.accept(this));
    }

    @Override
    public void visit(ReturnStmtNode returnStmtNode) {
        returnStmtNode.getRetNode().accept(this);
    }

    @Override
    public void visit(WhileStmtNode whileStmtNode) {
        whileStmtNode.getConditionNode().accept(this);
        whileStmtNode.getBlockNode().accept(this);
    }

    @Override
    public void visit(AssignStmtNode assignStmtNode) {
        assignStmtNode.getRhs().accept(this);
        assignStmtNode.getLhs().accept(this);
    }
}
