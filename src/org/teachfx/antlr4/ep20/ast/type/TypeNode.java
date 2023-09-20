package org.teachfx.antlr4.ep20.ast.type;

import org.antlr.v4.runtime.ParserRuleContext;
import org.teachfx.antlr4.ep20.ast.ASTNode;
import org.teachfx.antlr4.ep20.ast.ASTVisitor;

public class TypeNode extends ASTNode {
    protected BuiltInTypes baseType;
    protected int dim;

    public BuiltInTypes getBaseType() {
        return baseType;
    }

    public void setBaseType(BuiltInTypes baseType) {
        this.baseType = baseType;
    }

    public int getDim() {
        return dim;
    }

    public void setDim(int dim) {
        this.dim = dim;
    }

    public TypeNode() {
        baseType = BuiltInTypes.NUll;
        dim = 0;
    }

    public TypeNode(BuiltInTypes baseType) {
        this.baseType = baseType;
        this.dim = 0;
    }

    public TypeNode(BuiltInTypes type,ParserRuleContext ctx){
        this.baseType = type;
        this.dim = 0;
        this.ctx = ctx;
    }

    public boolean isEqual(TypeNode obj) {
        return  (baseType == obj.baseType && dim == obj.dim);
    }

    public boolean isEqual(BuiltInTypes types){
        return this.isEqual(new TypeNode(types));
    }

    @Override
    public void accept(ASTVisitor v) {
        v.visit(this);
    }

    @Override
    public void setCtx(ParserRuleContext ctx) {
        this.ctx = ctx;
    }
}
