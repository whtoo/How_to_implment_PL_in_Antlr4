package org.teachfx.antlr4.ep20.pass;

import org.teachfx.antlr4.ep20.ast.ASTNode;
import org.teachfx.antlr4.ep20.parser.CymbolBaseVisitor;
import org.teachfx.antlr4.ep20.parser.CymbolParser;
import org.teachfx.antlr4.ep20.parser.CymbolVisitor;

public class CymbolASTBuilder extends CymbolBaseVisitor<ASTNode> implements CymbolVisitor<ASTNode> {
    @Override
    public ASTNode visitCompilationUnit(CymbolParser.CompilationUnitContext ctx) {

        return super.visitCompilationUnit(ctx);
    }

    @Override
    public ASTNode visitVarDecl(CymbolParser.VarDeclContext ctx) {
        return super.visitVarDecl(ctx);
    }

    @Override
    public ASTNode visitPrimaryType(CymbolParser.PrimaryTypeContext ctx) {
        return super.visitPrimaryType(ctx);
    }

    @Override
    public ASTNode visitFunctionDecl(CymbolParser.FunctionDeclContext ctx) {
        return super.visitFunctionDecl(ctx);
    }

    @Override
    public ASTNode visitFormalParameters(CymbolParser.FormalParametersContext ctx) {
        return super.visitFormalParameters(ctx);
    }

    @Override
    public ASTNode visitFormalParameter(CymbolParser.FormalParameterContext ctx) {
        return super.visitFormalParameter(ctx);
    }

    @Override
    public ASTNode visitBlock(CymbolParser.BlockContext ctx) {
        return super.visitBlock(ctx);
    }

    @Override
    public ASTNode visitStatBlock(CymbolParser.StatBlockContext ctx) {
        return super.visitStatBlock(ctx);
    }

    @Override
    public ASTNode visitStatVarDecl(CymbolParser.StatVarDeclContext ctx) {
        return super.visitStatVarDecl(ctx);
    }

    @Override
    public ASTNode visitStatReturn(CymbolParser.StatReturnContext ctx) {
        return super.visitStatReturn(ctx);
    }

    @Override
    public ASTNode visitStateCondition(CymbolParser.StateConditionContext ctx) {
        return super.visitStateCondition(ctx);
    }

    @Override
    public ASTNode visitStateWhile(CymbolParser.StateWhileContext ctx) {
        return super.visitStateWhile(ctx);
    }

    @Override
    public ASTNode visitStatAssign(CymbolParser.StatAssignContext ctx) {
        return super.visitStatAssign(ctx);
    }

    @Override
    public ASTNode visitStat(CymbolParser.StatContext ctx) {
        return super.visitStat(ctx);
    }

    @Override
    public ASTNode visitExprBinary(CymbolParser.ExprBinaryContext ctx) {
        return super.visitExprBinary(ctx);
    }

    @Override
    public ASTNode visitExprGroup(CymbolParser.ExprGroupContext ctx) {
        return super.visitExprGroup(ctx);
    }

    @Override
    public ASTNode visitExprUnary(CymbolParser.ExprUnaryContext ctx) {
        return super.visitExprUnary(ctx);
    }

    @Override
    public ASTNode visitExprPrimary(CymbolParser.ExprPrimaryContext ctx) {
        return super.visitExprPrimary(ctx);
    }

    @Override
    public ASTNode visitExprFuncCall(CymbolParser.ExprFuncCallContext ctx) {
        return super.visitExprFuncCall(ctx);
    }

    @Override
    public ASTNode visitPrimaryID(CymbolParser.PrimaryIDContext ctx) {
        return super.visitPrimaryID(ctx);
    }

    @Override
    public ASTNode visitPrimaryINT(CymbolParser.PrimaryINTContext ctx) {
        return super.visitPrimaryINT(ctx);
    }

    @Override
    public ASTNode visitPrimaryFLOAT(CymbolParser.PrimaryFLOATContext ctx) {
        return super.visitPrimaryFLOAT(ctx);
    }

    @Override
    public ASTNode visitPrimaryCHAR(CymbolParser.PrimaryCHARContext ctx) {
        return super.visitPrimaryCHAR(ctx);
    }

    @Override
    public ASTNode visitPrimarySTRING(CymbolParser.PrimarySTRINGContext ctx) {
        return super.visitPrimarySTRING(ctx);
    }

    @Override
    public ASTNode visitPrimaryBOOL(CymbolParser.PrimaryBOOLContext ctx) {
        return super.visitPrimaryBOOL(ctx);
    }
}
