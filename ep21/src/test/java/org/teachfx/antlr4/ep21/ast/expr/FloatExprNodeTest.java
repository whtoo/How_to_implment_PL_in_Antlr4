package org.teachfx.antlr4.ep21.ast.expr;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FloatExprNodeTest {
    @Test
    void floatRawExtract() {

        var node = new FloatExprNode(1.2,null);
        assertEquals((double) 1.2, node.getRawValue());
    }


}