package org.teachfx.antlr4.ep20.ast.stmt;

import org.antlr.v4.runtime.ParserRuleContext;
import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep20.ast.expr.IntExprNode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ReturnStmtNodeTest {

    @Test
    void testReturnStmtNodeWithRetVal() {
        // Arrange
        IntExprNode retNode = new IntExprNode(42, mock(ParserRuleContext.class));
        ParserRuleContext ctx = mock(ParserRuleContext.class);

        // Act
        ReturnStmtNode returnStmtNode = new ReturnStmtNode(retNode, ctx);

        // Assert
        assertThat(returnStmtNode).isNotNull();
        assertThat(returnStmtNode.hasRetVal()).isTrue();
        assertThat(returnStmtNode.getRetNode()).isEqualTo(retNode);
    }

    @Test
    void testReturnStmtNodeWithoutRetVal() {
        // Arrange
        ParserRuleContext ctx = mock(ParserRuleContext.class);

        // Act
        ReturnStmtNode returnStmtNode = new ReturnStmtNode(null, ctx);

        // Assert
        assertThat(returnStmtNode).isNotNull();
        assertThat(returnStmtNode.hasRetVal()).isFalse();
        assertThat(returnStmtNode.getRetNode()).isNull();
    }

    @Test
    void testSetters() {
        // Arrange
        IntExprNode retNode = new IntExprNode(42, mock(ParserRuleContext.class));
        ParserRuleContext ctx = mock(ParserRuleContext.class);

        ReturnStmtNode returnStmtNode = new ReturnStmtNode(null, ctx);

        // Act
        returnStmtNode.setRetNode(retNode);

        // Assert
        assertThat(returnStmtNode.getRetNode()).isEqualTo(retNode);
        assertThat(returnStmtNode.hasRetVal()).isTrue();
    }

    @Test
    void testSetRetNodeToNull() {
        // Arrange
        IntExprNode retNode = new IntExprNode(42, mock(ParserRuleContext.class));
        ParserRuleContext ctx = mock(ParserRuleContext.class);

        ReturnStmtNode returnStmtNode = new ReturnStmtNode(retNode, ctx);

        // Act
        returnStmtNode.setRetNode(null);

        // Assert
        assertThat(returnStmtNode.getRetNode()).isNull();
        assertThat(returnStmtNode.hasRetVal()).isFalse();
    }
}