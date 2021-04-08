package org.teachfx.antlr4.ep20.visitor;

import org.teachfx.antlr4.ep20.parser.CymbolParser.BlockContext;
/**
 * 类型检查 -- 对于目前的脚本来说并没有必要，因为我是typed scirpt。
 * 而且，在解释器中有runtime check。
 * 但是，我马上要加入VM替换掉解释器，所以这个类存在的意义就在于此。
 */
public class TypeChecker extends CymbolASTVisitor<Object> {
    @Override
    public Object visitBlock(BlockContext ctx) {
        return super.visitBlock(ctx);
    }
}
