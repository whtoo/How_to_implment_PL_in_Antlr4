package org.teachfx.antlr4.ep21.ast.expr;

import org.antlr.v4.runtime.ParserRuleContext;
import org.teachfx.antlr4.ep21.ast.ASTVisitor;
import org.teachfx.antlr4.ep21.ast.type.TypeNode;
import org.teachfx.antlr4.ep21.debugger.ast.Dumper;
import org.teachfx.antlr4.ep21.symtab.type.TypeTable;

public class BoolExprNode extends LiteralNode<Boolean> {
    @Override
    protected void _dump(Dumper d) {
        super._dump(d);
        d.printMember("raw",rawValue);
    }

    public BoolExprNode(Boolean literalInt, ParserRuleContext ctx) {
        this.ctx = ctx;
        this.rawValue = literalInt;
        this.exprType = new TypeNode(TypeTable.BOOLEAN);
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
