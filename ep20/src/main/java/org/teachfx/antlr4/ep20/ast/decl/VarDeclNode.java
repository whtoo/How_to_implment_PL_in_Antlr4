package org.teachfx.antlr4.ep20.ast.decl;

import org.antlr.v4.runtime.ParserRuleContext;
import org.teachfx.antlr4.ep20.ast.ASTVisitor;
import org.teachfx.antlr4.ep20.ast.expr.ExprNode;
import org.teachfx.antlr4.ep20.ast.expr.IDExprNode;
import org.teachfx.antlr4.ep20.debugger.ast.Dumper;
import org.teachfx.antlr4.ep20.symtab.symbol.VariableSymbol;

import java.util.Objects;

public class VarDeclNode extends DeclNode {
    private IDExprNode idExprNode;

    private ExprNode assignExprNode;

    public ExprNode initializerExpr() {
        return assignExprNode;
    }

    public boolean hasInitializer() {
        return Objects.nonNull(assignExprNode);
    }


    public VarDeclNode(VariableSymbol variableSymbol,ExprNode assignExprNode,IDExprNode idExprNode, ParserRuleContext ctx) {
        this.refSymbol = variableSymbol;
        this.assignExprNode = assignExprNode;
        this.ctx = ctx;
        this.idExprNode = idExprNode;
        this.declName = variableSymbol.getName();
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
        if (Objects.nonNull(assignExprNode)) d.printMember("assignee",assignExprNode);
    }

    public IDExprNode getIdExprNode() {
        return idExprNode;
    }

    public void setIdExprNode(IDExprNode idExprNode) {
        this.idExprNode = idExprNode;
    }
}
