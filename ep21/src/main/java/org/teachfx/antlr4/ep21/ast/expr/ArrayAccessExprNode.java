package org.teachfx.antlr4.ep21.ast.expr;

import org.antlr.v4.runtime.ParserRuleContext;
import org.teachfx.antlr4.ep21.ast.ASTVisitor;
import org.teachfx.antlr4.ep21.debugger.ast.Dumper;

/**
 * 数组访问表达式节点 - 表示形如 arr[index] 的表达式
 *
 * <p>例如：
 * <pre>
 *   arr[i]
 *   matrix[3][4]  (嵌套访问)
 * </pre>
 * </p>
 */
public class ArrayAccessExprNode extends ExprNode {
    private ExprNode array;      // 数组表达式（可能是变量名或嵌套数组访问）
    private ExprNode index;       // 索引表达式

    public ArrayAccessExprNode(ExprNode array, ExprNode index, ParserRuleContext ctx) {
        this.array = array;
        this.index = index;
        this.ctx = ctx;
    }

    public ExprNode getArray() {
        return array;
    }

    public ExprNode getIndex() {
        return index;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    protected void _dump(Dumper d) {
        super._dump(d);
        d.printMember("array", array);
        d.printMember("index", index);
    }
}
