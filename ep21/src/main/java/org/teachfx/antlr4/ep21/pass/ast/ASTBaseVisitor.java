package org.teachfx.antlr4.ep21.pass.ast;

import org.teachfx.antlr4.ep21.ast.ASTVisitor;
import org.teachfx.antlr4.ep21.ast.CompileUnit;
import org.teachfx.antlr4.ep21.ast.decl.FuncDeclNode;
import org.teachfx.antlr4.ep21.ast.decl.VarDeclNode;
import org.teachfx.antlr4.ep21.ast.expr.*;
import org.teachfx.antlr4.ep21.ast.stmt.*;
import org.teachfx.antlr4.ep21.ast.type.TypeNode;

import java.util.Optional;

public class ASTBaseVisitor implements ASTVisitor<Void,Void> {
    // rewrite below visit method to accept Void as return type
    @Override
    public Void visit(CompileUnit rootNode) {

        // for-each varDeclNode in varDeclarations of rootNode
        for (var varDeclNode : rootNode.getVarDeclarations()) {
            varDeclNode.accept(this);
        }
        // for-each funcDeclNode in funcDeclarations of rootNode
        for (var funcDeclNode : rootNode.getFuncDeclarations()) {
            funcDeclNode.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(VarDeclNode varDeclNode) {
        Optional.ofNullable(varDeclNode.getAssignExprNode()).ifPresent(exprNode -> exprNode.accept(this));
        return null;
    }

    @Override
    public Void visit(FuncDeclNode funcDeclNode) {
        for( var param : funcDeclNode.getParamSlots().getVarDeclNodeList()) {
            param.accept(this);
        }

        funcDeclNode.getBody().accept(this);
        return null;
    }

    @Override
    public Void visit(VarDeclStmtNode varDeclStmtNode) {
        varDeclStmtNode.getVarDeclNode().accept(this);
        return null;
    }

    @Override
    public Void visit(TypeNode typeNode) {
        typeNode.accept(this);
        return null;
    }

    @Override
    public Void visit(BinaryExprNode binaryExprNode) {
        binaryExprNode.getLhs()
                .accept(this);
        binaryExprNode.getRhs()
                .accept(this);
        return null;
    }

    @Override
    public Void visit(IDExprNode idExprNode) {

        return null;
    }

    @Override
    public Void visit(BoolExprNode boolExprNode) {

        return null;
    }

    @Override
    public Void visit(CallFuncNode callExprNode) {
        callExprNode.getArgsNode().forEach(arg -> arg.accept(this));
        return null;
    }

    @Override
    public Void visit(IntExprNode intExprNode) {

        return null;
    }

    @Override
    public Void visit(FloatExprNode floatExprNode) {

        return null;
    }

    @Override
    public Void visit(NullExprNode nullExprNode) {

        return null;
    }

    @Override
    public Void visit(StringExprNode stringExprNode) {

        return null;
    }

    @Override
    public Void visit(UnaryExprNode unaryExprNode) {
        unaryExprNode.getValExpr().accept(this);
        return null;
    }

    @Override
    public Void visit(IfStmtNode ifStmtNode) {
        ifStmtNode.getCondExpr().accept(this);
        ifStmtNode.getThenBlock().accept(this);

        ifStmtNode.getElseBlock().ifPresent(block -> block.accept(this));
        return null;
    }

    @Override
    public Void visit(ExprStmtNode exprStmtNode) {
        exprStmtNode.getExprNode().accept(this);
        return null;
    }

    @Override
    public Void visit(BlockStmtNode blockStmtNode) {
        blockStmtNode.getStmtNodes().forEach(stmt -> stmt.accept(this));
        return null;
    }

    @Override
    public Void visit(ReturnStmtNode returnStmtNode) {
        if (returnStmtNode.getRetNode() != null) {
            returnStmtNode.getRetNode().accept(this);
        }
        return null;
    }

    @Override
    public Void visit(WhileStmtNode whileStmtNode) {
        whileStmtNode.getConditionNode().accept(this);
        whileStmtNode.getBlockNode().accept(this);
        return null;
    }

    @Override
    public Void visit(AssignStmtNode assignStmtNode) {
        assignStmtNode.getRhs().accept(this);
        assignStmtNode.getLhs().accept(this);
        return null;
    }


    @Override
    public Void visit(BreakStmtNode breakStmtNode) {
        return null;
    }

    @Override
    public Void visit(ContinueStmtNode continueStmtNode) {
        return null;
    }
}
