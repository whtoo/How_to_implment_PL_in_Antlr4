package org.teachfx.antlr4.ep20.ast.decl;

import org.antlr.v4.runtime.ParserRuleContext;
import org.teachfx.antlr4.ep20.ast.ASTVisitor;
import org.teachfx.antlr4.ep20.ast.type.TypeNode;
import org.teachfx.antlr4.ep20.debugger.ast.Dumper;

public class TypedefDeclNode extends DeclNode {
    private TypeNode originalType;
    private String aliasName;

    public TypedefDeclNode(TypeNode originalType, String aliasName, ParserRuleContext ctx) {
        super(aliasName);
        this.originalType = originalType;
        this.aliasName = aliasName;
        this.ctx = ctx;
    }

    public TypeNode getOriginalType() {
        return originalType;
    }

    public void setOriginalType(TypeNode originalType) {
        this.originalType = originalType;
    }

    public String getAliasName() {
        return aliasName;
    }

    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    protected void _dump(Dumper d) {
        super._dump(d);
        d.printMember("originalType", originalType);
        d.printMember("aliasName", aliasName);
    }

    @Override
    public String toString() {
        return "TypedefDeclNode{" +
                "originalType=" + originalType +
                ", aliasName='" + aliasName + '\'' +
                '}';
    }
}