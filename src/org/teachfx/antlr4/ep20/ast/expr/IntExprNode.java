package org.teachfx.antlr4.ep20.ast.expr;

import org.antlr.v4.runtime.ParserRuleContext;
import org.teachfx.antlr4.ep20.ast.ASTVisitor;
import org.teachfx.antlr4.ep20.ast.type.TypeNode;
import org.teachfx.antlr4.ep20.debugger.ast.Dumper;
import org.teachfx.antlr4.ep20.symtab.TypeTable;

public class IntExprNode extends LiteralNode<Integer> {

    public IntExprNode(Integer literalInt,ParserRuleContext ctx) {
        this.ctx = ctx;
        this.rawValue = literalInt;
        this.exprType = new TypeNode(TypeTable.INT);
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    protected void _dump(Dumper d) {
        super._dump(d);
        d.printMember("raw",rawValue);
    }
}
