package org.teachfx.antlr4.ep20.visitor;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.teachfx.antlr4.ep20.misc.Util;
import org.teachfx.antlr4.ep20.parser.CymbolParser.*;
import org.teachfx.antlr4.ep20.symtab.*;

/*
 * @author Arthur.Bltiz
 * @description 变量消解-标记每个ast节点的作用域归属问题.
 * @purpose 解决变量的定位问题--属于哪个作用域
 */
public class LocalDefine extends CymbolASTVisitor<Object> {
    private Scope currentScope = null;
    private final ParseTreeProperty<Scope> scopes;

    public LocalDefine() {
        BaseScope globalScope = new GlobalScope();
        currentScope = globalScope;
        MethodSymbol printFuncSymbol = new MethodSymbol("print", globalScope, null);
        printFuncSymbol.builtin = true;
        printFuncSymbol.getMemebers().put("value", TypeTable.OBJECT);
        globalScope.define(printFuncSymbol);
        scopes = new ParseTreeProperty<Scope>();
    }

    public ParseTreeProperty<Scope> getScopes() {
        return scopes;
    }

    @Override
    public Object visitCompilationUnit(CompilationUnitContext ctx) {
        stashScope(ctx);
        super.visitCompilationUnit(ctx);

        return null;
    }

    @Override
    public Object visitVarDecl(VarDeclContext ctx) {
        System.out.println(tab + "enter var decl " + ctx.getText());
        stashScope(ctx);
        return super.visitVarDecl(ctx);
    }

    @Override
    public Object visitStatVarDecl(StatVarDeclContext ctx) {
        System.out.println(tab + "enter stat var decl " + ctx.getText());
        return super.visitStatVarDecl(ctx);
    }

    @Override
    public Object visitFunctionDecl(FunctionDeclContext ctx) {
        MethodSymbol methodScope = new MethodSymbol(Util.name(ctx), currentScope, ctx);
        methodScope.blockStmt = ctx.blockDef;
        methodScope.callee = (ParserRuleContext) ctx.parent;
        currentScope.define(methodScope);
        stashScope(ctx);
        pushScope(methodScope);
        super.visitFunctionDecl(ctx);
        popScope();
        System.out.println("enter scope with " + currentScope.getScopeName());
        return null;
    }

    @Override
    public Object visitExprFuncCall(ExprFuncCallContext ctx) {
        super.visitExprFuncCall(ctx);
        stashScope(ctx);
        return null;
    }

    @Override
    public Object visitFormalParameter(FormalParameterContext ctx) {
        super.visitFormalParameter(ctx);
        System.out.println(tab + "collect param with " + ctx.getText());
        stashScope(ctx);
        return null;
    }

    @Override
    public Object visitBlock(BlockContext ctx) {
        System.out.println(tab + "enter block " + ctx.getText());

        Scope local = new LocalScope(currentScope);
        stashScope(ctx);
        pushScope(local);
        super.visitBlock(ctx);
        popScope();
        System.out.println(tab + "exit block " + ctx.getText());

        return null;
    }

    @Override
    public Object visitExprBinary(ExprBinaryContext ctx) {
        System.out.println(tab + "enter binary expr " + ctx.getText());
        stashScope(ctx);
        return super.visitExprBinary(ctx);
    }

    @Override
    public Object visitExprUnary(ExprUnaryContext ctx) {
        System.out.println(tab + "enter unary expr " + ctx.getText());
        stashScope(ctx);
        return super.visitExprUnary(ctx);
    }

    @Override
    public Object visitType(TypeContext ctx) {
        System.out.println(tab + "enter type " + ctx.getText());
        stashScope(ctx);
        return null;
    }

    @Override
    public Object visitPrimaryFLOAT(PrimaryFLOATContext ctx) {
        System.out.println(tab + "enter float constant");
        stashScope(ctx);
        return null;
    }

    @Override
    public Object visitPrimaryID(PrimaryIDContext ctx) {
        System.out.println(tab + "enter id  " + ctx.getText());

        stashScope(ctx);
        return null;
    }

    @Override
    public Object visitPrimaryINT(PrimaryINTContext ctx) {
        System.out.println(tab + "enter int constant");

        stashScope(ctx);
        return null;
    }

    public void stashScope(ParserRuleContext ctx) {
        scopes.put(ctx, currentScope);
    }

    public void pushScope(Scope scope) {
        currentScope = scope;
    }

    public void popScope() {
        currentScope = currentScope.getEnclosingScope();
    }

}
