package org.teachfx.antlr4.ep21.ast.decl;

import org.antlr.v4.runtime.ParserRuleContext;
import org.teachfx.antlr4.ep21.ast.ASTVisitor;
import org.teachfx.antlr4.ep21.ast.stmt.BlockStmtNode;
import org.teachfx.antlr4.ep21.ast.type.TypeNode;
import org.teachfx.antlr4.ep21.debugger.ast.Dumper;

public class FuncDeclNode extends DeclNode {
    /// pairs of formal param with its type
    private TypeNode retTypeNode;
    private VarDeclListNode paramSlots;
    private BlockStmtNode body;
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

    public TypeNode getRetTypeNode() {
        return retTypeNode;
    }

    public void setRetTypeNode(TypeNode retTypeNode) {
        this.retTypeNode = retTypeNode;
    }

    public VarDeclListNode getParamSlots() {
        return paramSlots;
    }

    public void setParamSlots(VarDeclListNode paramSlots) {
        this.paramSlots = paramSlots;
    }

    public BlockStmtNode getBody() {
        return body;
    }

    public void setBody(BlockStmtNode body) {
        this.body = body;
    }

    // Generate every private property's getter and setter


}
