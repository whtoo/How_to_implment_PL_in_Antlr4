package org.teachfx.antlr4.ep19.pass;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.teachfx.antlr4.ep19.misc.Util;
import org.teachfx.antlr4.ep19.parser.CymbolParser.*;
import org.teachfx.antlr4.ep19.symtab.TypeTable;
import org.teachfx.antlr4.ep19.symtab.scope.GlobalScope;
import org.teachfx.antlr4.ep19.symtab.symbol.MethodSymbol;

/**
 *
 */
/*
 * @author Arthur.Blitz
 * @description 变量消解-标记每个ast节点的作用域归属问题.
 * @purpose 解决变量的定位问题--属于哪个作用域
 */
public class LocalDefine extends CymbolASTVisitor<Object> {
    private final ParseTreeProperty<org.teachfx.antlr4.ep19.symtab.scope.Scope> scopes;
    private org.teachfx.antlr4.ep19.symtab.scope.Scope currentScope = null;

    public LocalDefine() {
        org.teachfx.antlr4.ep19.symtab.scope.BaseScope globalScope = new GlobalScope();
        currentScope = globalScope;
        org.teachfx.antlr4.ep19.symtab.symbol.MethodSymbol printFuncSymbol = new org.teachfx.antlr4.ep19.symtab.symbol.MethodSymbol("print", globalScope, null);
        printFuncSymbol.builtin = true;
        printFuncSymbol.getMembers().put("value", TypeTable.OBJECT);

        globalScope.define(printFuncSymbol);

        /// Define main entry
        org.teachfx.antlr4.ep19.symtab.symbol.MethodSymbol mainFuncSymbol = new org.teachfx.antlr4.ep19.symtab.symbol.MethodSymbol("main", globalScope, null);
        mainFuncSymbol.builtin = true;

        globalScope.define(mainFuncSymbol);

        scopes = new ParseTreeProperty<org.teachfx.antlr4.ep19.symtab.scope.Scope>();
    }

    public ParseTreeProperty<org.teachfx.antlr4.ep19.symtab.scope.Scope> getScopes() {
        return scopes;
    }

    @Override
    public Object visitVarDecl(VarDeclContext ctx) {
        stashScope(ctx);
        return super.visitVarDecl(ctx);
    }

    @Override
    public Object visitStatVarDecl(StatVarDeclContext ctx) {
        return super.visitStatVarDecl(ctx);
    }

    @Override
    public Object visitStructDecl(StructDeclContext ctx) {
        org.teachfx.antlr4.ep19.symtab.symbol.StructSymbol structScope = new org.teachfx.antlr4.ep19.symtab.symbol.StructSymbol(Util.name(ctx), currentScope, ctx);
        // mark struct symbol to current scope
        currentScope.define(structScope);
        // mark ctx to scope
        stashScope(ctx);
        // change scope
        pushScope(structScope);
        // visit sub-nodes
        super.visitStructDecl(ctx);
        // restore scope
        popScope();
        return null;
    }

    @Override
    public Object visitFunctionDecl(FunctionDeclContext ctx) {
        org.teachfx.antlr4.ep19.symtab.symbol.MethodSymbol methodScope = new MethodSymbol(Util.name(ctx), currentScope, ctx);
        methodScope.blockStmt = ctx.blockDef;
        methodScope.callee = (ParserRuleContext) ctx.parent;
        currentScope.define(methodScope);
        stashScope(ctx);
        pushScope(methodScope);
        super.visitFunctionDecl(ctx);
        popScope();
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
        stashScope(ctx);
        return null;
    }

    @Override
    public Object visitBlock(BlockContext ctx) {

        org.teachfx.antlr4.ep19.symtab.scope.Scope local = new org.teachfx.antlr4.ep19.symtab.scope.LocalScope(currentScope);
        stashScope(ctx);
        pushScope(local);
        super.visitBlock(ctx);
        popScope();

        return null;
    }

    @Override
    public Object visitStructMemeber(StructMemeberContext ctx) {
        if (ctx.ID() != null) {
            stashScope(ctx);
            org.teachfx.antlr4.ep19.symtab.symbol.VariableSymbol member = new org.teachfx.antlr4.ep19.symtab.symbol.VariableSymbol(Util.name(ctx));
            System.out.println("get struct - " + Util.name(ctx));
            currentScope.define(member);
        }
        return super.visitStructMemeber(ctx);
    }

    @Override
    public Object visitExprBinary(ExprBinaryContext ctx) {
        stashScope(ctx);
        return super.visitExprBinary(ctx);
    }

    @Override
    public Object visitExprUnary(ExprUnaryContext ctx) {
        stashScope(ctx);
        return super.visitExprUnary(ctx);
    }

    @Override
    public Object visitType(TypeContext ctx) {
        stashScope(ctx);
        return null;
    }

    @Override
    public Object visitPrimaryFLOAT(PrimaryFLOATContext ctx) {
        stashScope(ctx);
        return null;
    }

    @Override
    public Object visitPrimaryID(PrimaryIDContext ctx) {

        stashScope(ctx);
        return null;
    }

    @Override
    public Object visitPrimaryINT(PrimaryINTContext ctx) {
        stashScope(ctx);
        return null;
    }

    /**
     * bind ctx to current scope
     *
     * @param ctx
     */
    public void stashScope(ParserRuleContext ctx) {
        scopes.put(ctx, currentScope);
    }

    /**
     * change scope
     *
     * @param scope
     */
    public void pushScope(org.teachfx.antlr4.ep19.symtab.scope.Scope scope) {
        currentScope = scope;
    }

    public void popScope() {
        currentScope = currentScope.getEnclosingScope();
    }

}
