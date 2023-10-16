package org.teachfx.antlr4.ep21.ast.decl;


import org.antlr.v4.runtime.ParserRuleContext;
import org.teachfx.antlr4.ep21.debugger.ast.Dumper;

import java.util.List;

public class VarDeclListNode extends DeclNode {

    private List<VarDeclNode> varDeclNodeList;

    public VarDeclListNode(List<VarDeclNode> varDeclNodeList, ParserRuleContext ctx) {
        this.varDeclNodeList = varDeclNodeList;
        this.ctx = ctx;
    }

    public List<VarDeclNode> getVarDeclNodeList() {
        return varDeclNodeList;
    }

    public void setVarDeclNodeList(List<VarDeclNode> varDeclNodeList) {
        this.varDeclNodeList = varDeclNodeList;
    }

    @Override
    protected void _dump(Dumper d) {
        super._dump(d);
        d.printNodeList("decl_list",varDeclNodeList);
    }

    @Override
    public String getDeclName() {
        return "VarDeclList";
    }
}
