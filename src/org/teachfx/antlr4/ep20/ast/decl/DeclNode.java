package org.teachfx.antlr4.ep20.ast.decl;

import org.teachfx.antlr4.ep20.ast.ASTNode;

public abstract class DeclNode extends ASTNode {
    protected String declName;

    public String getDeclName() {
        return declName;
    }

    public DeclNode() {

    }

    public DeclNode(String name) {
        this.declName = name;
    }
}
