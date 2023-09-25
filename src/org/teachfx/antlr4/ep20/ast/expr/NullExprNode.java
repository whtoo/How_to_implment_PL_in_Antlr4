package org.teachfx.antlr4.ep20.ast.expr;

import org.antlr.v4.runtime.ParserRuleContext;
import org.teachfx.antlr4.ep20.ast.ASTVisitor;
import org.teachfx.antlr4.ep20.ast.type.TypeNode;
import org.teachfx.antlr4.ep20.debugger.Dumper;
import org.teachfx.antlr4.ep20.symtab.TypeTable;

public class NullExprNode extends LiteralNode<Object> {
    public NullExprNode(ParserRuleContext ctx, Object nullVal) {
        this.ctx = ctx;
        this.rawValue = nullVal;
        this.exprType = new TypeNode(TypeTable.NULL);
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    protected void _dump(Dumper d) {
        super._dump(d);
        d.printMember("raw","null");
    }
}
