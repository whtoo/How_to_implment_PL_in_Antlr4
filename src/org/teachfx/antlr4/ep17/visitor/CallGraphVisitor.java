package org.teachfx.antlr4.ep17.visitor;

import org.teachfx.antlr4.ep17.misc.Graph;
import org.teachfx.antlr4.ep17.parser.CymbolBaseVisitor;
import org.teachfx.antlr4.ep17.parser.CymbolParser.ExprFuncCallContext;
import org.teachfx.antlr4.ep17.parser.CymbolParser.FunctionDeclContext;

public class CallGraphVisitor extends CymbolBaseVisitor<Object>{
    public Graph callGraph;
    private String currentFunctionName = null;

    public CallGraphVisitor() { 
        super();
        this.callGraph = new Graph();
    }

    @Override
    public Object visitExprFuncCall(ExprFuncCallContext ctx) {
        super.visitExprFuncCall(ctx);
        if (currentFunctionName != null) {
            String funcName = ctx.ID().getText();
            callGraph.edge(currentFunctionName,funcName);
        }
    
        return null;
    }

    @Override
    public Object visitFunctionDecl(FunctionDeclContext ctx) {
        currentFunctionName = ctx.ID().getText();
        callGraph.nodes.add(currentFunctionName);
        super.visitFunctionDecl(ctx);
        currentFunctionName = null;
        return null;
    }
}
