package org.teachfx.antlr4.ep21.ast.expr;

import org.antlr.v4.runtime.ParserRuleContext;
import org.teachfx.antlr4.ep21.ast.ASTVisitor;
import org.teachfx.antlr4.ep21.debugger.ast.Dumper;
import org.teachfx.antlr4.ep21.symtab.symbol.Symbol;

import java.util.Objects;

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
        d.printMember("refSymbol",getRefSymbol());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IDExprNode that)) return false;
        return Objects.equals(getImage(), that.getImage()) && Objects.equals(getRefSymbol(), that.getRefSymbol());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getImage(), getRefSymbol());
    }
}
