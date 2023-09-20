package org.teachfx.antlr4.ep20.pass;

import org.teachfx.antlr4.ep20.parser.CymbolBaseVisitor;
import org.teachfx.antlr4.ep20.parser.CymbolParser;

public class CymbolAssembler extends CymbolBaseVisitor<Void> {

    @Override
    public Void visitFunctionDecl(CymbolParser.FunctionDeclContext ctx) {
        super.visitFunctionDecl(ctx);
        return null;
    }

    @Override
    public Void visitVarDecl(CymbolParser.VarDeclContext ctx) {
        return super.visitVarDecl(ctx);
    }

    @Override
    public Void visitStat(CymbolParser.StatContext ctx) {
        return super.visitStat(ctx);
    }
}
