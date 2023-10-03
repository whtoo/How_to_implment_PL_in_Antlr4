package org.teachfx.antlr4.ep20.ast.decl;

import org.teachfx.antlr4.ep20.ast.ASTNode;
import org.teachfx.antlr4.ep20.debugger.ast.Dumper;
import org.teachfx.antlr4.ep20.symtab.Symbol;

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
