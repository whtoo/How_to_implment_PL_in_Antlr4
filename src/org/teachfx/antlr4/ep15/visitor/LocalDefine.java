package org.teachfx.antlr4.ep15.visitor;

import org.teachfx.antlr4.ep15.symtab.*;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.teachfx.antlr4.ep15.parser.*;
import org.teachfx.antlr4.ep15.parser.CymbolParser.*;

/*
* @author Arthur.Bltiz
* @description 变量消解-每个变量解决归属问题.
*/
public class LocalDefine extends CymbolBaseVisitor<Object> {
    private Scope currentScope = null;
    public ParseTreeProperty<Scope> scopes;

    public LocalDefine() {
        BaseScope globalScope = new GlobalScope();
        currentScope = globalScope;
        scopes = new ParseTreeProperty<Scope>();
    }
    @Override
    public Object visitCompilationUnit(CompilationUnitContext ctx) {
       stashScope(ctx);
       return super.visitCompilationUnit(ctx);
    }

    @Override
    public Object visitFunctionDecl(FunctionDeclContext ctx) {
        Scope methodScope = new MethodSymbol(ctx.getText(), currentScope, ctx);
        stashScope(ctx);
        pushScope(methodScope);
        visitBlock(ctx.block());
        popScope();
        return super.visitFunctionDecl(ctx);
    }

    @Override
    public Object visitBlock(BlockContext ctx) {
        Scope local = new LocalScope(currentScope);
        stashScope(ctx);
        pushScope(local);
        for (StatContext stat : ctx.stat()) {
            visit(stat);
        }
        popScope();
        return super.visitBlock(ctx);
    }

    @Override
    public Object visitStat(StatContext ctx) {
        return super.visitStat(ctx);
    }

    @Override
    public Object visitVarDecl(VarDeclContext ctx) {
        stashScope(ctx);
        return super.visitVarDecl(ctx);
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
