package org.teachfx.antlr4.ep21.ast.expr;

import org.antlr.v4.runtime.ParserRuleContext;
import org.teachfx.antlr4.ep21.ast.ASTVisitor;
import org.teachfx.antlr4.ep21.debugger.ast.Dumper;

import java.util.List;
import java.util.ArrayList;

/**
 * 数组初始化表达式节点 - 表示形如 {expr, expr, ...} 的数组初始化语法
 *
 * <p>例如：
 * <pre>
 *   int arr[] = {1, 2, 3, 4, 5};
 *   float nums[] = {1.0, 2.5, 3.7};
 * </pre>
 * </p>
 */
public class ArrayInitializerExprNode extends ExprNode {
    private final List<ExprNode> elements;
    private final int size;

    /**
     * Creates a new array initializer with the specified elements.
     *
     * @param elements list of initializer expressions
     * @param ctx parser rule context
     */
    public ArrayInitializerExprNode(List<ExprNode> elements, ParserRuleContext ctx) {
        this.elements = elements != null ? elements : new ArrayList<>();
        this.size = this.elements.size();
        this.ctx = ctx;
        // Array initializers are RValues, not LValues
        this.isLValue = false;
    }

    /**
     * Gets the list of initializer elements.
     *
     * @return list of element expressions
     */
    public List<ExprNode> getElements() {
        return elements;
    }

    /**
     * Gets the number of elements in this initializer.
     *
     * @return size of the array initializer
     */
    public int getSize() {
        return size;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    protected void _dump(Dumper d) {
        super._dump(d);
        d.printMember("size", size);
        d.printNodeList("elements", elements);
    }
}
