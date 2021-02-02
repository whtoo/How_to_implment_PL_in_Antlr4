package org.teachfx.antlr4.ep16.visitor;

import org.antlr.v4.runtime.tree.ParseTree;
import org.teachfx.antlr4.ep16.parser.CymbolBaseVisitor;
import org.teachfx.antlr4.ep16.parser.CymbolParser.*;

public abstract class CymbolASTVisitor<T> extends CymbolBaseVisitor<T> {
    public String tab = "";
    CymbolASTVisitor() { 

    }

    @Override
    public T visitExprFuncCall(ExprFuncCallContext ctx) {
        System.out.println(tab + "enter expr func calling " + ctx.getText());
        return null;
    }

    @Override
    public T visitBlock(BlockContext ctx) {
        tab+= " ";
        for (StatatmentContext stat : ctx.statatment()) {
            visit(stat);
        }
        tab = tab.substring(0,tab.length()-1);
        return null;
    }
    @Override
    public T visitFunctionDecl(FunctionDeclContext ctx) {
        tab += " ";
        System.out.println(tab + "enter func " + ctx.getText());
        visit(ctx.formalParameters());
        visit(ctx.block());
        tab = tab.substring(0,tab.length()-1);
        System.out.println(tab + "exit func with "+ ctx.getText());
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
