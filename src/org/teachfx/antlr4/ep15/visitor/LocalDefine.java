package org.teachfx.antlr4.ep15.visitor;

import org.teachfx.antlr4.ep15.symtab.*;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
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
        for (ParseTree rule : ctx.children) {
            System.out.println("rule clz " + rule.getClass().toString());
           if (rule instanceof FunctionDeclContext) {
               System.out.println("visit func " + rule.getText());
               visitFunctionDecl((FunctionDeclContext)rule);
           } else if (rule instanceof VarDeclContext) {
               System.out.println("visit var " + rule.getText());
               visitVarDecl((VarDeclContext)rule);
           }
       }
       return null;
    }

    @Override
    public Object visitFunctionDecl(FunctionDeclContext ctx) {
        Scope methodScope = new MethodSymbol(ctx.getText(), currentScope, ctx);
        stashScope(ctx);
        pushScope(methodScope);
        System.out.println("enter func with " + currentScope.getScopeName());
        visitFormalParameters(ctx.formalParameters());
        visitBlock(ctx.block());
        System.out.println("exit func with " + currentScope.getScopeName());
        popScope();
        System.out.println("enter scope with " + currentScope.getScopeName());
        return null;
    }
    @Override
    public Object visitFormalParameters(FormalParametersContext ctx) {
        System.out.println("begin collect params");

        for (FormalParameterContext param : ctx.formalParameter()) {
            visitFormalParameter(param);
        }
        return null;
    }
    
    @Override
    public Object visitFormalParameter(FormalParameterContext ctx) {
        System.out.println("collect param with "+ctx.getText());
        stashScope(ctx);
        return null;
    }

    @Override
    public Object visitBlock(BlockContext ctx) {
        Scope local = new LocalScope(currentScope);
        stashScope(ctx);
        pushScope(local);
        for (StatatmentContext stat : ctx.statatment()) {
            visit(stat);
        }
        popScope();
        return null;
    }

    @Override
    public Object visitStat(StatContext ctx) {
        System.out.println("visit stats");
        for (ParseTree expr : ctx.children) {
            System.out.println("expr clz " + expr.getClass().toString());
            if (expr instanceof PrimaryFLOATContext) {
                visitPrimaryFLOAT((PrimaryFLOATContext)expr);
            } else if (expr instanceof PrimaryINTContext) {
                visitPrimaryINT((PrimaryINTContext)expr);
            } else if (expr instanceof PrimaryIDContext) {
                visitPrimaryID((PrimaryIDContext)expr);
            } else if (expr instanceof ExprBinaryContext) {
                visitExprBinary((ExprBinaryContext)expr);
            } else if (expr instanceof ExprUnaryContext) {
                visitExprUnary((ExprUnaryContext)expr);
            } else if (expr instanceof ExprGroupContext) {
                visitExprGroup((ExprGroupContext)expr);
            }
        }
        
       
        return null;
    }
    @Override
    public Object visitStatVarDecl(StatVarDeclContext ctx) {
        System.out.println("enter stat var declaration " + ctx.getText());
        stashScope(ctx);
        return null;
    }

    @Override
    public Object visitVarDecl(VarDeclContext ctx) {
        System.out.println("enter var declaration " + ctx.getText());
        stashScope(ctx);
        return null;
    }
    
    @Override
    public Object visitExprFuncCall(ExprFuncCallContext ctx) {
        System.out.println("enter expr func calling " + ctx.getText());
        return null;
    }

    @Override
    public Object visitPrimaryFLOAT(PrimaryFLOATContext ctx) {
        System.out.println("enter float constant");
        stashScope(ctx);
        return null;
    }

    @Override
    public Object visitPrimaryID(PrimaryIDContext ctx) {
        System.out.println("enter id  "+ctx.getText());

        stashScope(ctx);
        return null;
    }

    @Override
    public Object visitPrimaryINT(PrimaryINTContext ctx) {
        System.out.println("enter int constant");

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
