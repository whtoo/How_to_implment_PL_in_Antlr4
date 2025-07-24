package org.teachfx.antlr4.ep20.ast.stmt;

import org.antlr.v4.runtime.ParserRuleContext;
import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep20.ast.expr.IntExprNode;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class IfStmtNodeTest {

    @Test
    void testIfStmtNodeCreation() {
        // Arrange
        IntExprNode condExpr = new IntExprNode(1, mock(ParserRuleContext.class));
        StmtNode thenBlock = new ExprStmtNode(new IntExprNode(2, mock(ParserRuleContext.class)), mock(ParserRuleContext.class));
        StmtNode elseBlock = new ExprStmtNode(new IntExprNode(3, mock(ParserRuleContext.class)), mock(ParserRuleContext.class));
        ParserRuleContext ctx = mock(ParserRuleContext.class);

        // Act
        IfStmtNode ifStmtNode = new IfStmtNode(condExpr, thenBlock, elseBlock, ctx);

        // Assert
        assertThat(ifStmtNode).isNotNull();
        assertThat(ifStmtNode.getCondExpr()).isEqualTo(condExpr);
        assertThat(ifStmtNode.getThenBlock()).isEqualTo(thenBlock);
        assertThat(ifStmtNode.getElseBlock()).contains(elseBlock);
    }

    @Test
    void testIfStmtNodeWithoutElseBlock() {
        // Arrange
        IntExprNode condExpr = new IntExprNode(1, mock(ParserRuleContext.class));
        StmtNode thenBlock = new ExprStmtNode(new IntExprNode(2, mock(ParserRuleContext.class)), mock(ParserRuleContext.class));
        ParserRuleContext ctx = mock(ParserRuleContext.class);

        // Act
        IfStmtNode ifStmtNode = new IfStmtNode(condExpr, thenBlock, null, ctx);

        // Assert
        assertThat(ifStmtNode).isNotNull();
        assertThat(ifStmtNode.getCondExpr()).isEqualTo(condExpr);
        assertThat(ifStmtNode.getThenBlock()).isEqualTo(thenBlock);
        assertThat(ifStmtNode.getElseBlock()).isEmpty();
    }

    @Test
    void testSetters() {
        // Arrange
        IntExprNode condExpr = new IntExprNode(1, mock(ParserRuleContext.class));
        StmtNode thenBlock = new ExprStmtNode(new IntExprNode(2, mock(ParserRuleContext.class)), mock(ParserRuleContext.class));
        StmtNode elseBlock = new ExprStmtNode(new IntExprNode(3, mock(ParserRuleContext.class)), mock(ParserRuleContext.class));
        ParserRuleContext ctx = mock(ParserRuleContext.class);

        IfStmtNode ifStmtNode = new IfStmtNode(condExpr, thenBlock, elseBlock, ctx);

        // New values
        IntExprNode newCondExpr = new IntExprNode(0, mock(ParserRuleContext.class));
        StmtNode newThenBlock = new ExprStmtNode(new IntExprNode(4, mock(ParserRuleContext.class)), mock(ParserRuleContext.class));
        StmtNode newElseBlock = new ExprStmtNode(new IntExprNode(5, mock(ParserRuleContext.class)), mock(ParserRuleContext.class));

        // Act
        ifStmtNode.setConditionalNode(newCondExpr);
        ifStmtNode.setThenBlock(newThenBlock);
        ifStmtNode.setElseBlock(newElseBlock);

        // Assert
        assertThat(ifStmtNode.getCondExpr()).isEqualTo(newCondExpr);
        assertThat(ifStmtNode.getThenBlock()).isEqualTo(newThenBlock);
        assertThat(ifStmtNode.getElseBlock()).contains(newElseBlock);
    }

    @Test
    void testSetElseBlockToNull() {
        // Arrange
        IntExprNode condExpr = new IntExprNode(1, mock(ParserRuleContext.class));
        StmtNode thenBlock = new ExprStmtNode(new IntExprNode(2, mock(ParserRuleContext.class)), mock(ParserRuleContext.class));
        StmtNode elseBlock = new ExprStmtNode(new IntExprNode(3, mock(ParserRuleContext.class)), mock(ParserRuleContext.class));
        ParserRuleContext ctx = mock(ParserRuleContext.class);

        IfStmtNode ifStmtNode = new IfStmtNode(condExpr, thenBlock, elseBlock, ctx);

        // Act
        ifStmtNode.setElseBlock(null);

        // Assert
        assertThat(ifStmtNode.getElseBlock()).isEmpty();
    }
}