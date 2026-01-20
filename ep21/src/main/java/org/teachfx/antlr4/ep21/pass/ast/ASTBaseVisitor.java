package org.teachfx.antlr4.ep21.pass.ast;

import org.teachfx.antlr4.ep21.ast.ASTVisitor;
import org.teachfx.antlr4.ep21.ast.CompileUnit;
import org.teachfx.antlr4.ep21.ast.decl.FuncDeclNode;
import org.teachfx.antlr4.ep21.ast.decl.VarDeclNode;
import org.teachfx.antlr4.ep21.ast.expr.*;
import org.teachfx.antlr4.ep21.ast.stmt.*;
import org.teachfx.antlr4.ep21.ast.type.TypeNode;

import java.util.Optional;

/**
 * AST访问者基类，为AST遍历提供默认的空实现。
 *
 * 架构说明：
 * - CymbolASTBuilder (继承 CymbolBaseVisitor) 负责将 ANTLR 解析树转换为 AST
 * - ASTBaseVisitor 的子类 (如 TypeChecker) 负责对已构建的 AST 进行分析和转换
 *
 * 这种两阶段设计的原因：
 * 1. CymbolASTBuilder 需要实现 CymbolVisitor 接口来处理 ANTLR 特定的 visitXxx 方法
 * 2. ASTBaseVisitor 提供 Void 返回类型的统一访问接口，简化 AST 遍历逻辑
 *
 * @see CymbolASTBuilder
 * @see org.teachfx.antlr4.ep21.pass.sematic.TypeChecker
 */
public class ASTBaseVisitor implements ASTVisitor<Void,Void> {

    /**
     * 工厂方法：创建一个新的ASTBaseVisitor实例。
     * 子类可以通过覆盖此方法返回特化的访问者。
     *
     * @return 新的 ASTBaseVisitor 实例
     */
    public static ASTBaseVisitor create() {
        return new ASTBaseVisitor();
    }

    /**
     * 工厂方法：从已构建的CompileUnit创建访问者并执行遍历。
     * 这是一个便捷方法，封装了创建和执行的过程。
     *
     * @param rootNode AST根节点
     * @param <T> 返回类型
     * @return 访问者实现类（可用于链式调用）
     * @deprecated 请直接创建具体访问者类实例并调用visit方法
     */
    @Deprecated
    public static <T extends ASTBaseVisitor> T buildAndTraverse(CompileUnit rootNode, T visitor) {
        rootNode.accept(visitor);
        return visitor;
    }
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
    public Void visit(ArrayAccessExprNode arrayAccessExprNode) {
        // 数组访问：遍历数组和索引表达式
        arrayAccessExprNode.getArray().accept(this);
        arrayAccessExprNode.getIndex().accept(this);
        return null;
    }

    @Override
    public Void visit(ArrayInitializerExprNode arrayInitializerExprNode) {
        // 数组初始化：遍历所有初始化元素
        for (var element : arrayInitializerExprNode.getElements()) {
            element.accept(this);
        }
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
