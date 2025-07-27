package org.teachfx.antlr4.ep20.ast.expr;

import org.antlr.v4.runtime.ParserRuleContext;
import org.teachfx.antlr4.ep20.ast.ASTVisitor;
import org.teachfx.antlr4.ep20.debugger.ast.Dumper;

public class FieldAccessNode extends ExprNode {
    private ExprNode object;
    private String fieldName;

    public FieldAccessNode(ExprNode object, String fieldName, ParserRuleContext ctx) {
        super();
        this.object = object;
        this.fieldName = fieldName;
        this.ctx = ctx;
    }

    public ExprNode getObject() {
        return object;
    }

    public void setObject(ExprNode object) {
        this.object = object;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    protected void _dump(Dumper d) {
        super._dump(d);
        d.printMember("object", object);
        d.printMember("fieldName", fieldName);
    }

    @Override
    public String toString() {
        return "FieldAccessNode{" +
                "object=" + object +
                ", fieldName='" + fieldName + '\'' +
                '}';
    }
}