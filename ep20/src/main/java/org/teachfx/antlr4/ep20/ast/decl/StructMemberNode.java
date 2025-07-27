package org.teachfx.antlr4.ep20.ast.decl;

import org.antlr.v4.runtime.ParserRuleContext;
import org.teachfx.antlr4.ep20.ast.ASTVisitor;
import org.teachfx.antlr4.ep20.ast.type.TypeNode;
import org.teachfx.antlr4.ep20.debugger.ast.Dumper;

public class StructMemberNode extends DeclNode {
    private TypeNode memberType;
    private String memberName;
    private int arraySize;

    public StructMemberNode(TypeNode memberType, String memberName, int arraySize, ParserRuleContext ctx) {
        super(memberName);
        this.memberType = memberType;
        this.memberName = memberName;
        this.arraySize = arraySize;
        this.ctx = ctx;
    }

    public StructMemberNode(TypeNode memberType, String memberName, ParserRuleContext ctx) {
        this(memberType, memberName, 0, ctx);
    }

    public TypeNode getMemberType() {
        return memberType;
    }

    public void setMemberType(TypeNode memberType) {
        this.memberType = memberType;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public int getArraySize() {
        return arraySize;
    }

    public void setArraySize(int arraySize) {
        this.arraySize = arraySize;
    }

    public boolean isArray() {
        return arraySize > 0;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    protected void _dump(Dumper d) {
        super._dump(d);
        d.printMember("memberType", memberType);
        d.printMember("memberName", memberName);
        d.printMember("arraySize", arraySize);
    }

    @Override
    public String toString() {
        return "StructMemberNode{" +
                "memberType=" + memberType +
                ", memberName='" + memberName + '\'' +
                ", arraySize=" + arraySize +
                '}';
    }
}