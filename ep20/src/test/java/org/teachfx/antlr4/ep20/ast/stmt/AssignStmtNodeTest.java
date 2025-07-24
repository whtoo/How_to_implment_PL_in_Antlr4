package org.teachfx.antlr4.ep20.ast.stmt;

import org.antlr.v4.runtime.ParserRuleContext;
import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep20.ast.expr.IntExprNode;
import org.teachfx.antlr4.ep20.symtab.type.TypeTable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class AssignStmtNodeTest {

    @Test
    void testAssignStmtNodeCreation() {
        // Arrange
        IntExprNode lhs = new IntExprNode(1, mock(ParserRuleContext.class));
        IntExprNode rhs = new IntExprNode(2, mock(ParserRuleContext.class));
        ParserRuleContext ctx = mock(ParserRuleContext.class);

        // Act
        AssignStmtNode assignStmtNode = new AssignStmtNode(lhs, rhs, ctx);

        // Assert
        assertThat(assignStmtNode).isNotNull();
        assertThat(assignStmtNode.getLhs()).isEqualTo(lhs);
        assertThat(assignStmtNode.getRhs()).isEqualTo(rhs);
    }

    @Test
    void testSetters() {
        // Arrange
        IntExprNode lhs = new IntExprNode(1, mock(ParserRuleContext.class));
        IntExprNode rhs = new IntExprNode(2, mock(ParserRuleContext.class));
        ParserRuleContext ctx = mock(ParserRuleContext.class);

        AssignStmtNode assignStmtNode = new AssignStmtNode(lhs, rhs, ctx);

        // New values
        IntExprNode newLhs = new IntExprNode(3, mock(ParserRuleContext.class));
        IntExprNode newRhs = new IntExprNode(4, mock(ParserRuleContext.class));
        var newType = TypeTable.INT;

        // Act
        assignStmtNode.setLhs(newLhs);
        assignStmtNode.setRhs(newRhs);
        assignStmtNode.setType(newType);

        // Assert
        assertThat(assignStmtNode.getLhs()).isEqualTo(newLhs);
        assertThat(assignStmtNode.getRhs()).isEqualTo(newRhs);
        assertThat(assignStmtNode.getType()).isEqualTo(newType);
    }
}