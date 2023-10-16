package org.teachfx.antlr4.ep21.ast.decl;

import org.teachfx.antlr4.ep21.ast.ASTNode;
import org.teachfx.antlr4.ep21.debugger.ast.Dumper;
import org.teachfx.antlr4.ep21.symtab.symbol.Symbol;

public abstract class DeclNode extends ASTNode {
    protected String declName;
    protected Symbol refSymbol;

    public String getDeclName() {
        return declName;
    }

    public Symbol getRefSymbol() {
        return refSymbol;
    }

    public void setRefSymbol(Symbol refSymbol) {
        this.declName = refSymbol.getName();
        this.refSymbol = refSymbol;
    }
    
    public DeclNode() {

    }

    public DeclNode(String name) {
        this.declName = name;
    }

    @Override
    protected void _dump(Dumper d) {

    }
}
