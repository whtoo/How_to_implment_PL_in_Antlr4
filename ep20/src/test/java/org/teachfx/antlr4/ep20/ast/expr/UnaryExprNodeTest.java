package org.teachfx.antlr4.ep20.ast.expr;

import org.antlr.v4.runtime.ParserRuleContext;
import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep20.symtab.type.OperatorType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class UnaryExprNodeTest {

    @Test
    void testUnaryExprNodeCreation() {
        // Arrange
        OperatorType.UnaryOpType opType = OperatorType.UnaryOpType.NEG;
        ExprNode valExpr = new IntExprNode(5, mock(ParserRuleContext.class));
        ParserRuleContext ctx = mock(ParserRuleContext.class);

        // Act
        UnaryExprNode unaryExprNode = new UnaryExprNode(opType, valExpr, ctx);

        // Assert
        assertThat(unaryExprNode).isNotNull();
        assertThat(unaryExprNode.getOpType()).isEqualTo(opType);
        assertThat(unaryExprNode.getValExpr()).isEqualTo(valExpr);
    }

    @Test
    void testSetters() {
        // Arrange
        OperatorType.UnaryOpType opType = OperatorType.UnaryOpType.NEG;
        ExprNode valExpr = new IntExprNode(5, mock(ParserRuleContext.class));
        ExprNode newValExpr = new IntExprNode(10, mock(ParserRuleContext.class));
        ParserRuleContext ctx = mock(ParserRuleContext.class);

        UnaryExprNode unaryExprNode = new UnaryExprNode(opType, valExpr, ctx);

        // Act
        unaryExprNode.setValExpr(newValExpr);

        // Assert
        assertThat(unaryExprNode.getValExpr()).isEqualTo(newValExpr);
    }

    @Test
    void testEqualsAndHashCode() {
        // Arrange
        OperatorType.UnaryOpType opType = OperatorType.UnaryOpType.NEG;
        ExprNode valExpr = new IntExprNode(5, mock(ParserRuleContext.class));
        ParserRuleContext ctx1 = mock(ParserRuleContext.class);
        ParserRuleContext ctx2 = mock(ParserRuleContext.class);

        // Act
        UnaryExprNode node1 = new UnaryExprNode(opType, valExpr, ctx1);
        UnaryExprNode node2 = new UnaryExprNode(opType, valExpr, ctx2);

        // Assert
        assertThat(node1).isEqualTo(node2);
        assertThat(node1.hashCode()).isEqualTo(node2.hashCode());
    }

    @Test
    void testNotEquals() {
        // Arrange
        ExprNode valExpr = new IntExprNode(5, mock(ParserRuleContext.class));
        ParserRuleContext ctx = mock(ParserRuleContext.class);

        UnaryExprNode negNode = new UnaryExprNode(OperatorType.UnaryOpType.NEG, valExpr, ctx);
        UnaryExprNode notNode = new UnaryExprNode(OperatorType.UnaryOpType.NOT, valExpr, ctx);

        // Assert
        assertThat(negNode).isNotEqualTo(notNode);
    }
}