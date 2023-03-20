package org.teachfx.antlr4.ep19.visitor;

import org.antlr.v4.runtime.tree.ParseTree;
import org.teachfx.antlr4.ep19.misc.Util;
import org.teachfx.antlr4.ep19.parser.CymbolBaseVisitor;
import org.teachfx.antlr4.ep19.parser.CymbolParser.*;

public abstract class CymbolASTVisitor<T> extends CymbolBaseVisitor<T> {
    public String tab = "";
    CymbolASTVisitor() { 

    }
    
    @Override
    public T visitExprFuncCall(ExprFuncCallContext ctx) {
        System.out.println(tab + "enter expr func calling " + ctx.getText());
        super.visitExprFuncCall(ctx);
        return null;
    }

    @Override
    public T visitBlock(BlockContext ctx) {
        tab+= " ";
        super.visitBlock(ctx);
        tab = tab.substring(0,tab.length()-1);
        return null;
    }

    @Override
    public T visitStructDecl(StructDeclContext ctx) {
        tab += " ";
        System.out.println(tab + "enter struct " + Util.name(ctx));
        super.visitStructDecl(ctx);
        System.out.println(tab + "exit struct with "+ Util.name(ctx));
        tab = tab.substring(0,tab.length()-1);
        return null;
    }

    @Override
    public T visitFunctionDecl(FunctionDeclContext ctx) {
        tab += " ";
        System.out.println(tab + "enter func " + Util.name(ctx));
        super.visitFunctionDecl(ctx);
        System.out.println(tab + "exit func with "+ Util.name(ctx));
        tab = tab.substring(0,tab.length()-1);
        return null;
    }

    @Override
    public T visitCompilationUnit(CompilationUnitContext ctx) {
        System.out.println(tab + "begin visit compileUnit");

        tab += " ";
        for (ParseTree rule : ctx.children) {
            System.out.println(tab + "rule clz " + rule.getClass().toString());
            visit(rule);
       }
       tab = tab.substring(0,tab.length()-1);
       System.out.println(tab + "end visit compileUnit");
       return null;
    }

}
