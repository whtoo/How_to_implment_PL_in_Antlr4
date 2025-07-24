package org.teachfx.antlr4.ep20.ast.expr;

import org.antlr.v4.runtime.ParserRuleContext;
import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep20.symtab.type.OperatorType;
import org.teachfx.antlr4.ep20.symtab.type.TypeTable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class BinaryExprNodeTest {

    @Test
    void testBinaryExprNodeCreation() {
        // Arrange
        OperatorType.BinaryOpType opType = OperatorType.BinaryOpType.ADD;
        ExprNode lhs = new IntExprNode(5, mock(ParserRuleContext.class));
        ExprNode rhs = new IntExprNode(3, mock(ParserRuleContext.class));
        ParserRuleContext ctx = mock(ParserRuleContext.class);

        // Act
        BinaryExprNode binaryExprNode = new BinaryExprNode(opType, lhs, rhs, ctx);

        // Assert
        assertThat(binaryExprNode).isNotNull();
        assertThat(binaryExprNode.getOpType()).isEqualTo(opType);
        assertThat(binaryExprNode.getLhs()).isEqualTo(lhs);
        assertThat(binaryExprNode.getRhs()).isEqualTo(rhs);
        assertThat(binaryExprNode.getExprType()).isEqualTo(TypeTable.INT);
    }

    @Test
    void testStaticFactoryMethods() {
        // Arrange
        ExprNode lhs = new IntExprNode(5, mock(ParserRuleContext.class));
        ExprNode rhs = new IntExprNode(3, mock(ParserRuleContext.class));
        ParserRuleContext ctx = mock(ParserRuleContext.class);

        // Act
        BinaryExprNode addNode = BinaryExprNode.createAddNode(lhs, rhs, ctx);
        BinaryExprNode subNode = BinaryExprNode.createMinNode(lhs, rhs, ctx);
        BinaryExprNode mulNode = BinaryExprNode.createMulNode(lhs, rhs, ctx);
        BinaryExprNode divNode = BinaryExprNode.createDivNode(lhs, rhs, ctx);

        // Assert
        assertThat(addNode.getOpType()).isEqualTo(OperatorType.BinaryOpType.ADD);
        assertThat(subNode.getOpType()).isEqualTo(OperatorType.BinaryOpType.SUB);
        assertThat(mulNode.getOpType()).isEqualTo(OperatorType.BinaryOpType.MUL);
        assertThat(divNode.getOpType()).isEqualTo(OperatorType.BinaryOpType.DIV);
    }

    @Test
    void testSetters() {
        // Arrange
        OperatorType.BinaryOpType initialOpType = OperatorType.BinaryOpType.ADD;
        OperatorType.BinaryOpType newOpType = OperatorType.BinaryOpType.MUL;
        ExprNode lhs = new IntExprNode(5, mock(ParserRuleContext.class));
        ExprNode rhs = new IntExprNode(3, mock(ParserRuleContext.class));
        ExprNode newLhs = new IntExprNode(10, mock(ParserRuleContext.class));
        ExprNode newRhs = new IntExprNode(2, mock(ParserRuleContext.class));
        ParserRuleContext ctx = mock(ParserRuleContext.class);

        BinaryExprNode binaryExprNode = new BinaryExprNode(initialOpType, lhs, rhs, ctx);

        // Act
        binaryExprNode.setOpType(newOpType);
        binaryExprNode.setLhs(newLhs);
        binaryExprNode.setRhs(newRhs);

        // Assert
        assertThat(binaryExprNode.getOpType()).isEqualTo(newOpType);
        assertThat(binaryExprNode.getLhs()).isEqualTo(newLhs);
        assertThat(binaryExprNode.getRhs()).isEqualTo(newRhs);
    }

    @Test
    void testEqualsAndHashCode() {
        // Arrange
        OperatorType.BinaryOpType opType = OperatorType.BinaryOpType.ADD;
        ExprNode lhs = new IntExprNode(5, mock(ParserRuleContext.class));
        ExprNode rhs = new IntExprNode(3, mock(ParserRuleContext.class));
        ParserRuleContext ctx1 = mock(ParserRuleContext.class);
        ParserRuleContext ctx2 = mock(ParserRuleContext.class);

        // Act
        BinaryExprNode node1 = new BinaryExprNode(opType, lhs, rhs, ctx1);
        BinaryExprNode node2 = new BinaryExprNode(opType, lhs, rhs, ctx2);

        // Assert
        assertThat(node1).isEqualTo(node2);
        assertThat(node1.hashCode()).isEqualTo(node2.hashCode());
    }

    @Test
    void testNotEquals() {
        // Arrange
        ExprNode lhs = new IntExprNode(5, mock(ParserRuleContext.class));
        ExprNode rhs = new IntExprNode(3, mock(ParserRuleContext.class));
        ParserRuleContext ctx = mock(ParserRuleContext.class);

        BinaryExprNode addNode = new BinaryExprNode(OperatorType.BinaryOpType.ADD, lhs, rhs, ctx);
        BinaryExprNode mulNode = new BinaryExprNode(OperatorType.BinaryOpType.MUL, lhs, rhs, ctx);

        // Assert
        assertThat(addNode).isNotEqualTo(mulNode);
    }
}