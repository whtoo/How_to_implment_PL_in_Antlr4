package org.teachfx.antlr4.ep16.visitor;

import org.teachfx.antlr4.ep16.parser.CymbolParser.BlockContext;

public class TypeChecker extends CymbolASTVisitor<Object> {
    @Override
    public Object visitBlock(BlockContext ctx) {
        return super.visitBlock(ctx);
    }
}
