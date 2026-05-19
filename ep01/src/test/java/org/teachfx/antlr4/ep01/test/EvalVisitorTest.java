package org.teachfx.antlr4.ep01.test;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep01.EvalVisitor;
import org.teachfx.antlr4.ep01.parser.CymbolLexer;
import org.teachfx.antlr4.ep01.parser.CymbolParser;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EP01 tests: parsing and evaluating simple Cymbol expressions.
 */
public class EvalVisitorTest {

    private CymbolParser parse(String input) {
        var charStream = CharStreams.fromString(input);
        var lexer = new CymbolLexer(charStream);
        var tokens = new CommonTokenStream(lexer);
        return new CymbolParser(tokens);
    }

    @Test
    void testParseSimpleExpression() {
        var parser = parse("3 + 4;");
        var tree = parser.prog();
        assertNotNull(tree);
        assertTrue(tree.toStringTree(parser).contains("+"));
    }

    @Test
    void testEvaluateAddition() {
        var parser = parse("3 + 4;");
        var tree = parser.prog();
        var eval = new EvalVisitor();
        Integer result = eval.visit(tree);
        assertEquals(7, result);
    }

    @Test
    void testEvaluateMultiplicationPrecedence() {
        var parser = parse("3 + 4 * 5;");
        var tree = parser.prog();
        var eval = new EvalVisitor();
        Integer result = eval.visit(tree);
        assertEquals(23, result);
    }

    @Test
    void testVariableAssignmentAndLookup() {
        var parser = parse("x = 10; x + 5;");
        var tree = parser.prog();
        var eval = new EvalVisitor();
        eval.visit(tree);
        // The last expression result should be 15
        assertEquals(15, eval.visit(tree));
    }

    @Test
    void testParenthesesOverridePrecedence() {
        var parser = parse("(1 + 2) * (3 + 4);");
        var tree = parser.prog();
        var eval = new EvalVisitor();
        Integer result = eval.visit(tree);
        assertEquals(21, result);
    }

    @Test
    void testEvaluateSubtraction() {
        var parser = parse("10 - 3;");
        var tree = parser.prog();
        var eval = new EvalVisitor();
        assertEquals(7, eval.visit(tree));
    }

    @Test
    void testEvaluateDivision() {
        var parser = parse("12 / 3;");
        var tree = parser.prog();
        var eval = new EvalVisitor();
        assertEquals(4, eval.visit(tree));
    }

    @Test
    void testEvaluateDivisionWithRemainder() {
        var parser = parse("10 / 3;");
        var tree = parser.prog();
        var eval = new EvalVisitor();
        assertEquals(3, eval.visit(tree));
    }

    @Test
    void testEvaluateComplexExpression() {
        var parser = parse("10 - 2 * 3 + 8 / 2;");
        var tree = parser.prog();
        var eval = new EvalVisitor();
        assertEquals(8, eval.visit(tree));
    }

    @Test
    void testEvaluateDeeplyNestedParens() {
        var parser = parse("((1 + 2) * (3 + 4)) - ((5 - 2) * 2);");
        var tree = parser.prog();
        var eval = new EvalVisitor();
        assertEquals(15, eval.visit(tree));
    }

    @Test
    void testEvaluateLeftAssociative() {
        var parser = parse("20 - 5 - 3;");
        var tree = parser.prog();
        var eval = new EvalVisitor();
        assertEquals(12, eval.visit(tree));
    }

    @Test
    void testEvaluateMultiplicationWithZero() {
        var parser = parse("42 * 0;");
        var tree = parser.prog();
        var eval = new EvalVisitor();
        assertEquals(0, eval.visit(tree));
    }

    @Test
    void testEvaluateMultipleStatements() {
        var parser = parse("a = 5; b = 10; a + b;");
        var tree = parser.prog();
        var eval = new EvalVisitor();
        eval.visit(tree);
        assertEquals(15, eval.visit(tree));
    }

    @Test
    void testEvaluateVariableReassignment() {
        var parser = parse("x = 3; x = x + 4; x * 2;");
        var tree = parser.prog();
        var eval = new EvalVisitor();
        eval.visit(tree);
        assertEquals(14, eval.visit(tree));
    }

    @Test
    void testEvaluateMultipleVariables() {
        var parser = parse("a = 1; b = 2; c = 3; a + b * c;");
        var tree = parser.prog();
        var eval = new EvalVisitor();
        eval.visit(tree);
        assertEquals(7, eval.visit(tree));
    }

    @Test
    void testEvaluateVarInAssignment() {
        var parser = parse("x = 10; y = x + 5; y;");
        var tree = parser.prog();
        var eval = new EvalVisitor();
        eval.visit(tree);
        assertEquals(15, eval.visit(tree));
    }

    @Test
    void testEvaluateUninitializedVarDefaultsToZero() {
        var parser = parse("x + 5;");
        var tree = parser.prog();
        var eval = new EvalVisitor();
        assertEquals(5, eval.visit(tree));
    }

    @Test
    void testEvaluateAllOperatorsInSingleExpr() {
        var parser = parse("2 + 3 * 4 - 6 / 2;");
        var tree = parser.prog();
        var eval = new EvalVisitor();
        assertEquals(11, eval.visit(tree));
    }

    @Test
    void testEvaluateLargeNumbers() {
        var parser = parse("100000 + 200000;");
        var tree = parser.prog();
        var eval = new EvalVisitor();
        assertEquals(300000, eval.visit(tree));
    }

    @Test
    void testEvaluateChainedAssignments() {
        var parser = parse("x = 1; y = x; z = y; z + 2;");
        var tree = parser.prog();
        var eval = new EvalVisitor();
        eval.visit(tree);
        assertEquals(3, eval.visit(tree));
    }

    @Test
    void testEvaluateDivisionByOne() {
        var parser = parse("42 / 1;");
        var tree = parser.prog();
        var eval = new EvalVisitor();
        assertEquals(42, eval.visit(tree));
    }
}
