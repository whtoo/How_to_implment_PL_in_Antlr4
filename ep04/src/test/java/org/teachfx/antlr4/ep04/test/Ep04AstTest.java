package org.teachfx.antlr4.ep04.test;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep04.ast.*;
import org.teachfx.antlr4.ep04.parser.CymbolLexer;
import org.teachfx.antlr4.ep04.parser.CymbolParser;
import org.teachfx.antlr4.ep04.visitor.ASTBuilder;
import org.teachfx.antlr4.ep04.visitor.ASTEvaluator;
import org.teachfx.antlr4.ep04.visitor.ASTPrinter;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * EP04 — AST: Meaningful Trees
 *
 * Tests that the ASTBuilder correctly converts parse trees to AST nodes,
 * and that the ASTEvaluator computes correct results.
 */
class Ep04AstTest {

    private ASTNode buildAst(String input) {
        var lexer = new CymbolLexer(CharStreams.fromString(input));
        var tokens = new CommonTokenStream(lexer);
        var parser = new CymbolParser(tokens);
        var tree = parser.prog();
        var builder = new ASTBuilder();
        return builder.visit(tree);
    }

    private int evaluate(String input) {
        var node = buildAst(input);
        var evaluator = new ASTEvaluator();
        return evaluator.visit(node);
    }

    @Test
    void buildIntNode() {
        ASTNode node = buildAst("42;");
        assertInstanceOf(PrintNode.class, node, "Expression statement should become PrintNode");
        PrintNode print = (PrintNode) node;
        assertInstanceOf(IntNode.class, print.expr, "Print should wrap an IntNode");
        assertEquals(42, ((IntNode) print.expr).value);
    }

    @Test
    void buildBinaryOpNode() {
        ASTNode node = buildAst("3 + 4;");
        assertInstanceOf(PrintNode.class, node);
        BinaryOpNode binOp = (BinaryOpNode) ((PrintNode) node).expr;
        assertEquals("+", binOp.op);
        assertInstanceOf(IntNode.class, binOp.left);
        assertInstanceOf(IntNode.class, binOp.right);
        assertEquals(3, ((IntNode) binOp.left).value);
        assertEquals(4, ((IntNode) binOp.right).value);
    }

    @Test
    void buildAssignNode() {
        ASTNode node = buildAst("x = 10;");
        assertInstanceOf(AssignNode.class, node);
        AssignNode assign = (AssignNode) node;
        assertEquals("x", assign.name);
        assertInstanceOf(IntNode.class, assign.value);
        assertEquals(10, ((IntNode) assign.value).value);
    }

    @Test
    void evaluateSimpleExpression() {
        assertEquals(42, evaluate("42;"));
    }

    @Test
    void evaluateArithmetic() {
        assertEquals(7, evaluate("3 + 4;"));
        assertEquals(12, evaluate("3 * 4;"));
        assertEquals(1, evaluate("4 - 3;"));
        assertEquals(2, evaluate("6 / 3;"));
    }

    @Test
    void evaluateAssignmentAndLookup() {
        // Build AST for assignment, then evaluate a program with both assign and expr
        var lexer = new CymbolLexer(CharStreams.fromString("x = 5; x + 3;"));
        var tokens = new CommonTokenStream(lexer);
        var parser = new CymbolParser(tokens);
        var tree = parser.prog();
        var builder = new ASTBuilder();
        var evaluator = new ASTEvaluator();

        // Evaluate each statement in order
        int result = 0;
        for (var stmt : tree.stat()) {
            ASTNode node = builder.visit(stmt);
            result = evaluator.visit(node);
        }
        assertEquals(8, result, "x + 3 where x = 5 should equal 8");
    }

    @Test
    void buildSubtractionNode() {
        ASTNode node = buildAst("10 - 3;");
        BinaryOpNode binOp = (BinaryOpNode) ((PrintNode) node).expr;
        assertEquals("-", binOp.op);
        assertEquals(10, ((IntNode) binOp.left).value);
        assertEquals(3, ((IntNode) binOp.right).value);
    }

    @Test
    void buildDivisionNode() {
        ASTNode node = buildAst("10 / 2;");
        BinaryOpNode binOp = (BinaryOpNode) ((PrintNode) node).expr;
        assertEquals("/", binOp.op);
        assertEquals(10, ((IntNode) binOp.left).value);
        assertEquals(2, ((IntNode) binOp.right).value);
    }

    @Test
    void buildDeeplyNestedAST() {
        ASTNode node = buildAst("((1 + 2) * (3 - 4));");
        assertInstanceOf(PrintNode.class, node);
        BinaryOpNode mul = (BinaryOpNode) ((PrintNode) node).expr;
        assertEquals("*", mul.op);
        // Left should be (1+2), right should be (3-4)
        assertInstanceOf(BinaryOpNode.class, mul.left);
        assertInstanceOf(BinaryOpNode.class, mul.right);
    }

    @Test
    void evaluateSubtractionAndDivision() {
        assertEquals(7, evaluate("10 - 3;"));
        assertEquals(4, evaluate("12 / 3;"));
        assertEquals(3, evaluate("10 / 3;"));
    }

    @Test
    void evaluateComplexExpression() {
        assertEquals(8, evaluate("10 - 2 * 3 + 8 / 2;"));
    }

    @Test
    void evaluateAllOperators() {
        assertEquals(11, evaluate("2 + 3 * 4 - 6 / 2;"));
    }

    @Test
    void evaluateVariableReassignment() {
        var lexer = new CymbolLexer(CharStreams.fromString("x = 3; x = x + 4; x * 2;"));
        var tokens = new CommonTokenStream(lexer);
        var parser = new CymbolParser(tokens);
        var tree = parser.prog();
        var builder = new ASTBuilder();
        var evaluator = new ASTEvaluator();

        int result = 0;
        for (var stmt : tree.stat()) {
            ASTNode node = builder.visit(stmt);
            result = evaluator.visit(node);
        }
        assertEquals(14, result, "x=3, x=7, x*2=14");
    }

    @Test
    void buildASTForIdLookup() {
        ASTNode node = buildAst("myVar;");
        assertInstanceOf(PrintNode.class, node);
        assertInstanceOf(IdNode.class, ((PrintNode) node).expr);
        assertEquals("myVar", ((IdNode) ((PrintNode) node).expr).name);
    }

    @Test
    void astNodeTypeNames() {
        assertEquals("IntNode", new IntNode(42).nodeType());
        assertEquals("IdNode", new IdNode("x").nodeType());
        assertEquals("BinaryOpNode", new BinaryOpNode(new IntNode(1), "+", new IntNode(2)).nodeType());
        assertEquals("AssignNode", new AssignNode("x", new IntNode(10)).nodeType());
        assertEquals("PrintNode", new PrintNode(new IntNode(0)).nodeType());
    }

    @Test
    void astPrinterDoesNotThrow() {
        var lexer = new CymbolLexer(CharStreams.fromString("3 + 4;"));
        var tokens = new CommonTokenStream(lexer);
        var parser = new CymbolParser(tokens);
        var tree = parser.prog();
        var builder = new ASTBuilder();
        ASTNode node = builder.visit(tree);

        ASTPrinter printer = new ASTPrinter();
        assertDoesNotThrow(() -> printer.visit(node));
    }

    @Test
    void astVisitorFallbackDispatches() {
        ASTNode node = new IntNode(99);
        ASTEvaluator evaluator = new ASTEvaluator();
        assertEquals(99, evaluator.visit(node)); // visit(ASTNode) dispatches to visit(IntNode)
    }
}
