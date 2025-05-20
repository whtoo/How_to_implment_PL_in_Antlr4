package org.teachfx.antlr4.ep19.pass;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.teachfx.antlr4.ep19.misc.Util;
import org.teachfx.antlr4.ep19.parser.CymbolBaseVisitor;
import org.teachfx.antlr4.ep19.parser.CymbolParser.BlockContext;
import org.teachfx.antlr4.ep19.parser.CymbolParser.ExprFuncCallContext;
import org.teachfx.antlr4.ep19.parser.CymbolParser.FunctionDeclContext;
import org.teachfx.antlr4.ep19.parser.CymbolParser.StructDeclContext;

public abstract class CymbolASTVisitor<T> extends CymbolBaseVisitor<T> {
    private static final Logger logger = LoggerFactory.getLogger(CymbolASTVisitor.class);
    public String tab = "";

    CymbolASTVisitor() {
    }

    @Override
    public T visitExprFuncCall(ExprFuncCallContext ctx) {
        logger.trace("{}enter expr func calling {}", tab, ctx.getText());
        super.visitExprFuncCall(ctx);
        return null;
    }

    @Override
    public T visitBlock(BlockContext ctx) {
        tab += " ";
        super.visitBlock(ctx);
        tab = tab.substring(0, tab.length() - 1);
        return null;
    }

    @Override
    public T visitStructDecl(StructDeclContext ctx) {
        tab += " ";
        logger.trace("{}enter struct {}", tab, Util.name(ctx));
        super.visitStructDecl(ctx);
        logger.trace("{}exit struct with {}", tab, Util.name(ctx));
        tab = tab.substring(0, tab.length() - 1);
        return null;
    }

    @Override
    public T visitFunctionDecl(FunctionDeclContext ctx) {
        tab += " ";
        logger.trace("{}enter function {}", tab, ctx.ID().getText());
        super.visitFunctionDecl(ctx);
        logger.trace("{}exit function {}", tab, ctx.ID().getText());
        tab = tab.substring(0, tab.length() - 1);
        return null;
    }
}
