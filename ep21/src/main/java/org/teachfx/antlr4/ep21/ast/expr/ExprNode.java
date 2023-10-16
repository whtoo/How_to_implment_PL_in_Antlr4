package org.teachfx.antlr4.ep21.ast.expr;

import org.teachfx.antlr4.ep21.ast.ASTNode;
import org.teachfx.antlr4.ep21.ast.type.TypeNode;
import org.teachfx.antlr4.ep21.debugger.ast.Dumper;
import org.teachfx.antlr4.ep21.symtab.type.Type;

abstract public class ExprNode extends ASTNode {

    protected TypeNode exprType;
    protected boolean isLValue;

    public ExprNode() {
        exprType = new TypeNode();
        isLValue = false;
    }

    public boolean isEqual(Type types) {
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

    public Type getExprType() {
        return exprType.getBaseType();
    }

    @Override
    protected void _dump(Dumper d) {
    }
}
