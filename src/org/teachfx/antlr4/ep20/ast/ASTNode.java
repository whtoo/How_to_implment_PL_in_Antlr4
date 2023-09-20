package org.teachfx.antlr4.ep20.ast;
import org.antlr.v4.runtime.ParserRuleContext;

abstract public class ASTNode {
    public ParserRuleContext ctx;

    public void accept(ASTVisitor visitor){}
    abstract public void setCtx(ParserRuleContext ctx);
}

