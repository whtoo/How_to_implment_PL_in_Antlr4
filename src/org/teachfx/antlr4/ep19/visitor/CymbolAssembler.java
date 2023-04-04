package org.teachfx.antlr4.ep19.visitor;

import org.teachfx.antlr4.ep19.parser.CymbolBaseVisitor;
import org.teachfx.antlr4.ep19.parser.CymbolParser;

public class CymbolAssembler extends CymbolBaseVisitor<Void> {

    @Override
    public Void visitStructDecl(CymbolParser.StructDeclContext ctx) {
        return super.visitStructDecl(ctx);
    }

    @Override
    public Void visitFunctionDecl(CymbolParser.FunctionDeclContext ctx) {
        return super.visitFunctionDecl(ctx);
    }

    @Override
    public Void visitVarDecl(CymbolParser.VarDeclContext ctx) {
        return super.visitVarDecl(ctx);
    }
}
