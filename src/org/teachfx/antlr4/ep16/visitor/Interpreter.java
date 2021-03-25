package org.teachfx.antlr4.ep16.visitor;

import java.util.*;

import org.teachfx.antlr4.ep16.parser.CymbolBaseVisitor;
import org.teachfx.antlr4.ep16.parser.CymbolParser.ExprBinaryContext;
import org.teachfx.antlr4.ep16.parser.CymbolParser.ExprFuncCallContext;
import org.teachfx.antlr4.ep16.parser.CymbolParser.ExprGroupContext;
import org.teachfx.antlr4.ep16.parser.CymbolParser.ExprPrimaryContext;
import org.teachfx.antlr4.ep16.parser.CymbolParser.ExprUnaryContext;

public class Interpreter extends CymbolBaseVisitor<Object> {
    private static final Map<String,Object> memory;
    
    static { 
        memory = new HashMap<String,Object>();
    }

    //< Expression evaluation
    @Override
    public Object visitExprBinary(ExprBinaryContext ctx) {
        // TODO Auto-generated method stub
        return super.visitExprBinary(ctx);
    }

    @Override
    public Object visitExprPrimary(ExprPrimaryContext ctx) {
        // TODO Auto-generated method stub
        return super.visitExprPrimary(ctx);
    }

    @Override
    public Object visitExprFuncCall(ExprFuncCallContext ctx) {
        // TODO Auto-generated method stub
        return super.visitExprFuncCall(ctx);
    }

    @Override
    public Object visitExprGroup(ExprGroupContext ctx) {
        // TODO Auto-generated method stub
        return super.visitExprGroup(ctx);
    }

    @Override
    public Object visitExprUnary(ExprUnaryContext ctx) {
        // TODO Auto-generated method stub
        return super.visitExprUnary(ctx);
    }
    //> Expression evaluation

}
