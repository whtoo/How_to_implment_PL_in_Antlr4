package org.teachfx.antlr4.ep20.ast.expr;

import org.antlr.v4.runtime.ParserRuleContext;
import org.teachfx.antlr4.ep20.ast.ASTVisitor;
import org.teachfx.antlr4.ep20.debugger.ast.Dumper;

public class ArrayAccessNode extends ExprNode {
    private ExprNode array;
    private ExprNode index;

    public ArrayAccessNode(ExprNode array, ExprNode index, ParserRuleContext ctx) {
        this.array = array;
        this.index = index;
        this.ctx = ctx;
    }

    public ExprNode getArray() {
        return array;
    }

    public ExprNode getIndex() {
        return index;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    protected void _dump(Dumper d) {
        super._dump(d);
        d.printMember("array", array);
        d.printMember("index", index);
    }
}