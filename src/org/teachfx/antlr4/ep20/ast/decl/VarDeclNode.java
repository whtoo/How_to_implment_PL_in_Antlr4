package org.teachfx.antlr4.ep20.ast.decl;

import org.antlr.v4.runtime.ParserRuleContext;
import org.teachfx.antlr4.ep20.ast.ASTVisitor;
import org.teachfx.antlr4.ep20.ast.expr.ExprNode;
import org.teachfx.antlr4.ep20.debugger.Dumper;
import org.teachfx.antlr4.ep20.symtab.VariableSymbol;

public class VarDeclNode extends DeclNode {
    private ExprNode assignExprNode;

    public VarDeclNode(VariableSymbol variableSymbol,ExprNode assignExprNode, ParserRuleContext ctx) {
        this.refSymbol = variableSymbol;
        this.assignExprNode = assignExprNode;
        this.ctx = ctx;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    public ExprNode getAssignExprNode() {
        return assignExprNode;
    }

    public void setAssignExprNode(ExprNode assignExprNode) {
        this.assignExprNode = assignExprNode;
    }

    @Override
    public String getDeclName() {
        return getRefSymbol().getName();
    }

    @Override
    protected void _dump(Dumper d) {
        super._dump(d);
        d.printMember("name",getDeclName());
        d.printMember("type",getRefSymbol().getType());
    }
}
