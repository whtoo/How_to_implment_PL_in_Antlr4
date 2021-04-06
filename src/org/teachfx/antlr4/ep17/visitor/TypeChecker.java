package org.teachfx.antlr4.ep17.visitor;

import org.teachfx.antlr4.ep17.parser.CymbolParser.BlockContext;

public class TypeChecker extends CymbolASTVisitor<Object> {
    @Override
    public Object visitBlock(BlockContext ctx) {
        return super.visitBlock(ctx);
    }
}
