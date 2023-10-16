package org.teachfx.antlr4.ep21.ast.stmt;

import org.antlr.v4.runtime.ParserRuleContext;
import org.teachfx.antlr4.ep21.ast.ASTVisitor;
import org.teachfx.antlr4.ep21.debugger.ast.Dumper;

import java.util.List;

public class BlockStmtNode extends StmtNode {

    private List<StmtNode> stmtNodes;

    private ScopeType scopeType;

    public BlockStmtNode(List<StmtNode> childrenNodes, ParserRuleContext ctx) {
        this.stmtNodes = childrenNodes;
        this.ctx = ctx;
    }

    public ScopeType getParentScopeType() {
        return scopeType;
    }

    public void setParentScopeType(ScopeType scopeType) {
        this.scopeType = scopeType;
    }

    public List<StmtNode> getStmtNodes() {
        return stmtNodes;
    }

    public void setStmtNodes(List<StmtNode> stmtNodes) {
        this.stmtNodes = stmtNodes;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    protected void _dump(Dumper d) {
        super._dump(d);
        d.printMember("scopeType",scopeType.toString());
        d.printNodeList("blocks",stmtNodes);
    }
}
