package org.teachfx.antlr4.ep20.ast.stmt;

import org.antlr.v4.runtime.ParserRuleContext;
import org.teachfx.antlr4.ep20.ast.ASTVisitor;
import org.teachfx.antlr4.ep20.ast.decl.VarDeclNode;
import org.teachfx.antlr4.ep20.debugger.ast.Dumper;

public class VarDeclStmtNode extends StmtNode{
    private VarDeclNode varDeclNode;

    public VarDeclStmtNode(VarDeclNode varDeclNode, ParserRuleContext ctx) {
        this.varDeclNode = varDeclNode;
        this.ctx = ctx;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    public VarDeclNode getVarDeclNode() {
        return varDeclNode;
    }

    public void setVarDeclNode(VarDeclNode varDeclNode) {
        this.varDeclNode = varDeclNode;
    }

    @Override
    protected void _dump(Dumper d) {
        super._dump(d);
        d.printMember("var",varDeclNode);
    }
}
