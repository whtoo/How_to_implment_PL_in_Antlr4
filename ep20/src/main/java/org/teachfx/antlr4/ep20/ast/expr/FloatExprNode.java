package org.teachfx.antlr4.ep20.ast.expr;

import org.antlr.v4.runtime.ParserRuleContext;
import org.teachfx.antlr4.ep20.ast.ASTVisitor;
import org.teachfx.antlr4.ep20.ast.type.TypeNode;
import org.teachfx.antlr4.ep20.debugger.ast.Dumper;
import org.teachfx.antlr4.ep20.symtab.type.TypeTable;

public class FloatExprNode extends LiteralNode<Double>{
    public FloatExprNode(Double literalInt, ParserRuleContext ctx) {
        this.ctx = ctx;
        this.rawValue = literalInt;
        this.exprType = new TypeNode(TypeTable.FLOAT);
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    protected void _dump(Dumper d) {
        super._dump(d);
        d.printMember("raw",rawValue.toString());
    }
}
