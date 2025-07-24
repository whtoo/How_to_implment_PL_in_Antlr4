package org.teachfx.antlr4.ep20.ast.expr;

import org.antlr.v4.runtime.ParserRuleContext;
import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep20.symtab.scope.GlobalScope;
import org.teachfx.antlr4.ep20.symtab.symbol.MethodSymbol;
import org.teachfx.antlr4.ep20.symtab.type.TypeTable;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class CallFuncNodeTest {

    @Test
    void testCallFuncNodeCreation() {
        // Arrange
        String funcName = "testFunc";
        List<ExprNode> args = new ArrayList<>();
        args.add(new IntExprNode(1, mock(ParserRuleContext.class)));
        args.add(new IntExprNode(2, mock(ParserRuleContext.class)));
        ParserRuleContext ctx = mock(ParserRuleContext.class);

        // Act
        CallFuncNode callFuncNode = new CallFuncNode(funcName, args, ctx);

        // Assert
        assertThat(callFuncNode).isNotNull();
        assertThat(callFuncNode.getFuncName()).isEqualTo(funcName);
        assertThat(callFuncNode.getArgsNode()).isEqualTo(args);
    }

    @Test
    void testSetters() {
        // Arrange
        String funcName = "testFunc";
        List<ExprNode> args = new ArrayList<>();
        args.add(new IntExprNode(1, mock(ParserRuleContext.class)));
        ParserRuleContext ctx = mock(ParserRuleContext.class);

        CallFuncNode callFuncNode = new CallFuncNode(funcName, args, ctx);

        // Additional setup
        String newFuncName = "newFunc";
        List<ExprNode> newArgs = new ArrayList<>();
        newArgs.add(new IntExprNode(3, mock(ParserRuleContext.class)));
        newArgs.add(new IntExprNode(4, mock(ParserRuleContext.class)));
        newArgs.add(new IntExprNode(5, mock(ParserRuleContext.class)));

        GlobalScope globalScope = new GlobalScope();
        MethodSymbol methodSymbol = new MethodSymbol(newFuncName, TypeTable.INT, globalScope, null);

        // Act
        callFuncNode.setFuncName(newFuncName);
        callFuncNode.setArgsNode(newArgs);
        callFuncNode.setCallFuncSymbol(methodSymbol);

        // Assert
        assertThat(callFuncNode.getFuncName()).isEqualTo(newFuncName);
        assertThat(callFuncNode.getArgsNode()).isEqualTo(newArgs);
        assertThat(callFuncNode.getCallFuncSymbol()).isEqualTo(methodSymbol);
    }

    @Test
    void testGetExprType() {
        // Arrange
        String funcName = "testFunc";
        List<ExprNode> args = new ArrayList<>();
        ParserRuleContext ctx = mock(ParserRuleContext.class);

        CallFuncNode callFuncNode = new CallFuncNode(funcName, args, ctx);

        GlobalScope globalScope = new GlobalScope();
        MethodSymbol methodSymbol = new MethodSymbol(funcName, TypeTable.INT, globalScope, null);
        callFuncNode.setCallFuncSymbol(methodSymbol);

        // Act
        var exprType = callFuncNode.getExprType();

        // Assert
        assertThat(exprType).isEqualTo(methodSymbol);
    }

    @Test
    void testEqualsAndHashCode() {
        // Arrange
        String funcName = "testFunc";
        List<ExprNode> args = new ArrayList<>();
        args.add(new IntExprNode(1, mock(ParserRuleContext.class)));
        ParserRuleContext ctx1 = mock(ParserRuleContext.class);
        ParserRuleContext ctx2 = mock(ParserRuleContext.class);

        // Act
        CallFuncNode node1 = new CallFuncNode(funcName, args, ctx1);
        CallFuncNode node2 = new CallFuncNode(funcName, args, ctx2);

        // Assert
        assertThat(node1).isEqualTo(node2);
        assertThat(node1.hashCode()).isEqualTo(node2.hashCode());
    }

    @Test
    void testNotEquals() {
        // Arrange
        List<ExprNode> args1 = new ArrayList<>();
        args1.add(new IntExprNode(1, mock(ParserRuleContext.class)));
        List<ExprNode> args2 = new ArrayList<>();
        args2.add(new IntExprNode(2, mock(ParserRuleContext.class)));
        ParserRuleContext ctx = mock(ParserRuleContext.class);

        CallFuncNode node1 = new CallFuncNode("func1", args1, ctx);
        CallFuncNode node2 = new CallFuncNode("func2", args2, ctx);

        // Assert
        assertThat(node1).isNotEqualTo(node2);
    }
}