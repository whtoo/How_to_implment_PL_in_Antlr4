package org.teachfx.antlr4.ep20.ast.expr;

import org.teachfx.antlr4.ep20.ast.ASTNode;
import org.teachfx.antlr4.ep20.ast.ASTVisitor;
import org.teachfx.antlr4.ep20.ast.type.BuiltInTypes;
import org.teachfx.antlr4.ep20.ast.type.TypeNode;

abstract public class ExprNode extends ASTNode {

    protected TypeNode exprType;
    protected boolean isLValue;

    public ExprNode() {
        exprType = new TypeNode();
        isLValue = false;
    }

    public boolean isEqual(BuiltInTypes types) {
        return exprType.isEqual(new TypeNode(types));
    }

    public void setExprType(TypeNode exprType) {
        this.exprType = exprType;
    }

    public boolean isLValue() {
        return isLValue;
    }

    public void setLValue(boolean LValue) {
        isLValue = LValue;
    }

    public TypeNode getExprType() {
        return exprType;
    }
}
