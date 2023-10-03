package org.teachfx.antlr4.ep20.ast.expr;

import org.antlr.v4.runtime.ParserRuleContext;
import org.teachfx.antlr4.ep20.ast.ASTVisitor;
import org.teachfx.antlr4.ep20.debugger.ast.Dumper;
import org.teachfx.antlr4.ep20.symtab.Symbol;

public class IDExprNode extends ExprNode {

    protected String image;

    protected Symbol refSymbol;

    public IDExprNode(String image,ParserRuleContext ctx) {
        this.image = image;
        this.ctx = ctx;

    }


    public Symbol getRefSymbol() {
        return refSymbol;
    }

    public void setRefSymbol(Symbol refSymbol) {
        this.refSymbol = refSymbol;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    protected void _dump(Dumper d) {
        super._dump(d);
        d.printMember("id",image);
    }
}
