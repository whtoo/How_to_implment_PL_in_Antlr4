package org.teachfx.antlr4.ep20.ast.expr;

import org.antlr.v4.runtime.ParserRuleContext;
import org.teachfx.antlr4.ep20.ast.ASTVisitor;
import org.teachfx.antlr4.ep20.debugger.ast.Dumper;

import java.util.ArrayList;
import java.util.List;

public class ArrayLiteralNode extends ExprNode {
    private List<ExprNode> elements;

    public ArrayLiteralNode(List<ExprNode> elements, ParserRuleContext ctx) {
        super();
        this.elements = elements != null ? elements : new ArrayList<>();
        this.ctx = ctx;
    }

    public List<ExprNode> getElements() {
        return elements;
    }

    public void setElements(List<ExprNode> elements) {
        this.elements = elements;
    }

    public void addElement(ExprNode element) {
        if (elements == null) {
            elements = new ArrayList<>();
        }
        elements.add(element);
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    protected void _dump(Dumper d) {
        super._dump(d);
        d.printNodeList("elements", elements);
    }

    @Override
    public String toString() {
        return "ArrayLiteralNode{" +
                "elements=" + elements +
                '}';
    }
}