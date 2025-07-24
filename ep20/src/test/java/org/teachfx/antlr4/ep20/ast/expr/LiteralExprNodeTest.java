package org.teachfx.antlr4.ep20.ast.expr;

import org.antlr.v4.runtime.ParserRuleContext;
import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep20.ast.type.TypeNode;
import org.teachfx.antlr4.ep20.symtab.type.TypeTable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class LiteralExprNodeTest {

    @Test
    void testIntExprNodeCreation() {
        // Arrange
        Integer value = 42;
        ParserRuleContext ctx = mock(ParserRuleContext.class);

        // Act
        IntExprNode intExprNode = new IntExprNode(value, ctx);

        // Assert
        assertThat(intExprNode).isNotNull();
        assertThat(intExprNode.getRawValue()).isEqualTo(value);
        assertThat(intExprNode.getExprType()).isNotNull();
        assertThat(intExprNode.getExprType()).isEqualTo(TypeTable.INT);
    }

    @Test
    void testStringExprNodeCreation() {
        // Arrange
        String value = "hello";
        ParserRuleContext ctx = mock(ParserRuleContext.class);

        // Act
        StringExprNode stringExprNode = new StringExprNode(value, ctx);

        // Assert
        assertThat(stringExprNode).isNotNull();
        assertThat(stringExprNode.getRawValue()).isEqualTo(value);
        assertThat(stringExprNode.getExprType()).isNotNull();
        assertThat(stringExprNode.getExprType()).isEqualTo(TypeTable.STRING);
    }

    @Test
    void testBoolExprNodeCreation() {
        // Arrange
        Boolean value = true;
        ParserRuleContext ctx = mock(ParserRuleContext.class);

        // Act
        BoolExprNode boolExprNode = new BoolExprNode(value, ctx);

        // Assert
        assertThat(boolExprNode).isNotNull();
        assertThat(boolExprNode.getRawValue()).isEqualTo(value);
        assertThat(boolExprNode.getExprType()).isNotNull();
        assertThat(boolExprNode.getExprType()).isEqualTo(TypeTable.BOOLEAN);
    }

    @Test
    void testFloatExprNodeCreation() {
        // Arrange
        Double value = 3.14;
        ParserRuleContext ctx = mock(ParserRuleContext.class);

        // Act
        FloatExprNode floatExprNode = new FloatExprNode(value, ctx);

        // Assert
        assertThat(floatExprNode).isNotNull();
        assertThat(floatExprNode.getRawValue()).isEqualTo(value);
        assertThat(floatExprNode.getExprType()).isNotNull();
        assertThat(floatExprNode.getExprType()).isEqualTo(TypeTable.FLOAT);
    }

    @Test
    void testIntExprNodeEqualsAndHashCode() {
        // Arrange
        Integer value = 42;
        ParserRuleContext ctx1 = mock(ParserRuleContext.class);
        ParserRuleContext ctx2 = mock(ParserRuleContext.class);

        // Act
        IntExprNode node1 = new IntExprNode(value, ctx1);
        IntExprNode node2 = new IntExprNode(value, ctx2);

        // Assert
        assertThat(node1).isEqualTo(node2);
        assertThat(node1.hashCode()).isEqualTo(node2.hashCode());
    }

    @Test
    void testStringExprNodeEqualsAndHashCode() {
        // Arrange
        String value = "hello";
        ParserRuleContext ctx1 = mock(ParserRuleContext.class);
        ParserRuleContext ctx2 = mock(ParserRuleContext.class);

        // Act
        StringExprNode node1 = new StringExprNode(value, ctx1);
        StringExprNode node2 = new StringExprNode(value, ctx2);

        // Assert
        assertThat(node1).isEqualTo(node2);
        assertThat(node1.hashCode()).isEqualTo(node2.hashCode());
    }

    @Test
    void testBoolExprNodeEqualsAndHashCode() {
        // Arrange
        Boolean value = true;
        ParserRuleContext ctx1 = mock(ParserRuleContext.class);
        ParserRuleContext ctx2 = mock(ParserRuleContext.class);

        // Act
        BoolExprNode node1 = new BoolExprNode(value, ctx1);
        BoolExprNode node2 = new BoolExprNode(value, ctx2);

        // Assert
        assertThat(node1).isEqualTo(node2);
        assertThat(node1.hashCode()).isEqualTo(node2.hashCode());
    }

    @Test
    void testFloatExprNodeEqualsAndHashCode() {
        // Arrange
        Double value = 3.14;
        ParserRuleContext ctx1 = mock(ParserRuleContext.class);
        ParserRuleContext ctx2 = mock(ParserRuleContext.class);

        // Act
        FloatExprNode node1 = new FloatExprNode(value, ctx1);
        FloatExprNode node2 = new FloatExprNode(value, ctx2);

        // Assert
        assertThat(node1).isEqualTo(node2);
        assertThat(node1.hashCode()).isEqualTo(node2.hashCode());
    }

    @Test
    void testIntExprNodeNotEquals() {
        // Arrange
        Integer value1 = 42;
        Integer value2 = 24;
        ParserRuleContext ctx = mock(ParserRuleContext.class);

        // Act
        IntExprNode node1 = new IntExprNode(value1, ctx);
        IntExprNode node2 = new IntExprNode(value2, ctx);

        // Assert
        assertThat(node1).isNotEqualTo(node2);
    }

    @Test
    void testStringExprNodeNotEquals() {
        // Arrange
        String value1 = "hello";
        String value2 = "world";
        ParserRuleContext ctx = mock(ParserRuleContext.class);

        // Act
        StringExprNode node1 = new StringExprNode(value1, ctx);
        StringExprNode node2 = new StringExprNode(value2, ctx);

        // Assert
        assertThat(node1).isNotEqualTo(node2);
    }

    @Test
    void testBoolExprNodeNotEquals() {
        // Arrange
        Boolean value1 = true;
        Boolean value2 = false;
        ParserRuleContext ctx = mock(ParserRuleContext.class);

        // Act
        BoolExprNode node1 = new BoolExprNode(value1, ctx);
        BoolExprNode node2 = new BoolExprNode(value2, ctx);

        // Assert
        assertThat(node1).isNotEqualTo(node2);
    }

    @Test
    void testFloatExprNodeNotEquals() {
        // Arrange
        Double value1 = 3.14;
        Double value2 = 2.71;
        ParserRuleContext ctx = mock(ParserRuleContext.class);

        // Act
        FloatExprNode node1 = new FloatExprNode(value1, ctx);
        FloatExprNode node2 = new FloatExprNode(value2, ctx);

        // Assert
        assertThat(node1).isNotEqualTo(node2);
    }

    @Test
    void testSetRawValue() {
        // Arrange
        Integer initialValue = 42;
        Integer newValue = 24;
        ParserRuleContext ctx = mock(ParserRuleContext.class);
        IntExprNode intExprNode = new IntExprNode(initialValue, ctx);

        // Act
        intExprNode.setRawValue(newValue);

        // Assert
        assertThat(intExprNode.getRawValue()).isEqualTo(newValue);
    }
}