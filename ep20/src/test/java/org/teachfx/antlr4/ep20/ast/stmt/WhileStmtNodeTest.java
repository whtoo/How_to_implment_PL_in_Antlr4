package org.teachfx.antlr4.ep20.ast.stmt;

import org.antlr.v4.runtime.ParserRuleContext;
import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep20.ast.expr.IntExprNode;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class WhileStmtNodeTest {

    @Test
    void testWhileStmtNodeCreation() {
        // Arrange
        IntExprNode conditionNode = new IntExprNode(1, mock(ParserRuleContext.class));
        BlockStmtNode blockNode = new BlockStmtNode(new ArrayList<>(), mock(ParserRuleContext.class));
        ParserRuleContext ctx = mock(ParserRuleContext.class);

        // Act
        WhileStmtNode whileStmtNode = new WhileStmtNode(conditionNode, blockNode, ctx);

        // Assert
        assertThat(whileStmtNode).isNotNull();
        assertThat(whileStmtNode.getConditionNode()).isEqualTo(conditionNode);
        assertThat(whileStmtNode.getBlockNode()).isEqualTo(blockNode);
    }

    @Test
    void testSetters() {
        // Arrange
        IntExprNode conditionNode = new IntExprNode(1, mock(ParserRuleContext.class));
        BlockStmtNode blockNode = new BlockStmtNode(new ArrayList<>(), mock(ParserRuleContext.class));
        ParserRuleContext ctx = mock(ParserRuleContext.class);

        WhileStmtNode whileStmtNode = new WhileStmtNode(conditionNode, blockNode, ctx);

        // New values
        IntExprNode newConditionNode = new IntExprNode(0, mock(ParserRuleContext.class));
        BlockStmtNode newBlockNode = new BlockStmtNode(new ArrayList<>(), mock(ParserRuleContext.class));

        // Act
        whileStmtNode.setConditionNode(newConditionNode);
        whileStmtNode.setBlockNode(newBlockNode);

        // Assert
        assertThat(whileStmtNode.getConditionNode()).isEqualTo(newConditionNode);
        assertThat(whileStmtNode.getBlockNode()).isEqualTo(newBlockNode);
    }
}