package org.teachfx.antlr4.ep21.ast.expr;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BoolExprNodeTest {

    @Test
    void testToString() {
        var boolVal = new BoolExprNode(true,null);
        assertEquals("true",boolVal.getRawValue().toString());
    }
}