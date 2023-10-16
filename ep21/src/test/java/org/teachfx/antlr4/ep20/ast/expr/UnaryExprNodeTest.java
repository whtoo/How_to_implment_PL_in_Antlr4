package org.teachfx.antlr4.ep21.ast.expr;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.teachfx.antlr4.ep21.symtab.type.OperatorType;

class UnaryExprNodeTest {
    @Test
    public void testNegInt() {
        var node = new UnaryExprNode(OperatorType.UnaryOpType.NEG, new IntExprNode(1, null),null);

        assertEquals(node.getOpType(),OperatorType.UnaryOpType.NEG);
        assertEquals(node.getValExpr(), new IntExprNode(1, null));
    }
}