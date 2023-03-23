package org.teachfx.antlr4.ep19.visitor;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.teachfx.antlr4.ep19.misc.Util;
import org.teachfx.antlr4.ep19.parser.CymbolParser.*;
import org.teachfx.antlr4.ep19.symtab.*;

/**
 *
 */
/*
* @author Arthur.Bltiz
* @description 变量消解-标记每个ast节点的作用域归属问题.
* @purpose 解决变量的定位问题--属于哪个作用域
*/
public class LocalDefine extends CymbolASTVisitor<Object> {
    private Scope currentScope = null;
    private ParseTreeProperty<Scope> scopes;

    public ParseTreeProperty<Scope> getScopes() {
        return scopes;
    }
    
    public LocalDefine() {
        BaseScope globalScope = new GlobalScope();
        currentScope = globalScope;
        MethodSymbol printFuncSymbol = new MethodSymbol("print",globalScope,null);
        printFuncSymbol.builtin = true;
        printFuncSymbol.getMembers().put("value", TypeTable.OBJECT);

        globalScope.define(printFuncSymbol);

        /// Define main entry
        MethodSymbol mainFuncSymbol = new MethodSymbol("main",globalScope,null);
        mainFuncSymbol.builtin = true;

        globalScope.define(mainFuncSymbol);

        scopes = new ParseTreeProperty<Scope>();
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
        StructSymbol structScope = new StructSymbol(Util.name(ctx), currentScope,ctx);
        currentScope.define(structScope);
        stashScope(ctx);
        pushScope(structScope);
        super.visitStructDecl(ctx);
        popScope();
        return null;
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

        Scope local = new LocalScope(currentScope);
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
            VariableSymbol member = new VariableSymbol(Util.name(ctx));
            System.out.println("get struct - "+Util.name(ctx));
            currentScope.define(member);
        }
        return  null;
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

    public void stashScope(ParserRuleContext ctx) {
        scopes.put(ctx,currentScope);
    }

    public void pushScope(Scope scope) {
        currentScope = scope;
    }

    public void popScope() { 
        currentScope = currentScope.getEnclosingScope();
    }
    
}
