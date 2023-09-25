package org.teachfx.antlr4.ep20.ast.decl;

import org.antlr.v4.runtime.ParserRuleContext;
import org.teachfx.antlr4.ep20.ast.ASTVisitor;
import org.teachfx.antlr4.ep20.ast.expr.IDExprNode;
import org.teachfx.antlr4.ep20.ast.stmt.BlockStmtNode;
import org.teachfx.antlr4.ep20.ast.type.TypeNode;
import org.teachfx.antlr4.ep20.debugger.Dumper;

import java.util.List;

public class FuncDeclNode extends DeclNode {
    /// pairs of formal param with its type
    protected TypeNode retTypeNode;
    protected VarDeclListNode paramSlots;
    protected BlockStmtNode body;
    public FuncDeclNode(TypeNode retTypeNode, String funcName, VarDeclListNode params, BlockStmtNode body, ParserRuleContext ctx) {
        this.retTypeNode = retTypeNode;
        this.declName = funcName;
        this.paramSlots = params;
        this.body = body;
        this.ctx = ctx;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    protected void _dump(Dumper d) {
        super._dump(d);
        d.printMember("func",declName);
        d.printMember("retType",retTypeNode.getBaseType());
        d.printNodeList("args",paramSlots.getVarDeclNodeList());
        if(body != null) d.printNodeList("body",body.getStmtNodes());
    }
}
