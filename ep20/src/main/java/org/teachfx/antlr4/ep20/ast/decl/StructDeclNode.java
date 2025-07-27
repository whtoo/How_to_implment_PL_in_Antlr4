package org.teachfx.antlr4.ep20.ast.decl;

import org.antlr.v4.runtime.ParserRuleContext;
import org.teachfx.antlr4.ep20.ast.ASTVisitor;
import org.teachfx.antlr4.ep20.debugger.ast.Dumper;

import java.util.ArrayList;
import java.util.List;

public class StructDeclNode extends DeclNode {
    private String structName;
    private List<StructMemberNode> members;

    public StructDeclNode(String structName, List<StructMemberNode> members, ParserRuleContext ctx) {
        super(structName);
        this.structName = structName;
        this.members = members != null ? members : new ArrayList<>();
        this.ctx = ctx;
    }

    public String getStructName() {
        return structName;
    }

    public void setStructName(String structName) {
        this.structName = structName;
    }

    public List<StructMemberNode> getMembers() {
        return members;
    }

    public void setMembers(List<StructMemberNode> members) {
        this.members = members;
    }

    public void addMember(StructMemberNode member) {
        if (members == null) {
            members = new ArrayList<>();
        }
        members.add(member);
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    protected void _dump(Dumper d) {
        super._dump(d);
        d.printMember("structName", structName);
        d.printNodeList("members", members);
    }

    @Override
    public String toString() {
        return "StructDeclNode{" +
                "structName='" + structName + '\'' +
                ", members=" + members +
                '}';
    }
}