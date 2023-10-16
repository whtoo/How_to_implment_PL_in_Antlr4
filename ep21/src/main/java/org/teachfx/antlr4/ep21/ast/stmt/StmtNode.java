package org.teachfx.antlr4.ep21.ast.stmt;

import org.teachfx.antlr4.ep21.ast.ASTNode;
import org.teachfx.antlr4.ep21.debugger.ast.Dumper;
import org.teachfx.antlr4.ep21.symtab.scope.Scope;

public abstract class StmtNode extends ASTNode {
    private Scope scope;

    @Override
    protected void _dump(Dumper d) {

    }

    public Scope getScope() {
        return scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }
}
