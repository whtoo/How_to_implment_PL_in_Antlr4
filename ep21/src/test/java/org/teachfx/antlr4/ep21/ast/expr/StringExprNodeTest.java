package org.teachfx.antlr4.ep21.ast.expr;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StringExprNodeTest {

    @Test
    void testRawVal() {
        var strNode = new StringExprNode("Hello",null);
        assertEquals("Hello",strNode.getRawValue());
    }
}