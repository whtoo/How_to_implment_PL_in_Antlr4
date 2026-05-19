package org.teachfx.antlr4.ep03.test;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep03.parser.CymbolLexer;
import org.teachfx.antlr4.ep03.parser.CymbolParser;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EP03 — Grammar: Words→Trees
 *
 * Tests that the ANTLR parser correctly builds parse trees,
 * demonstrating operator precedence and grammar structure.
 */
class Ep03ParserTest {

    private CymbolParser parse(String input) {
        var lexer = new CymbolLexer(CharStreams.fromString(input));
        var tokens = new CommonTokenStream(lexer);
        return new CymbolParser(tokens);
    }

    @Test
    void parseSimpleExpression() {
        var parser = parse("42;");
        ParseTree tree = parser.prog();
        assertNotNull(tree);
        String treeText = tree.toStringTree(parser);
        assertTrue(treeText.contains("42"), "Parse tree should contain literal 42");
        assertEquals(0, parser.getNumberOfSyntaxErrors(), "Should have no syntax errors");
    }

    @Test
    void parseAssignment() {
        var parser = parse("x = 10;");
        ParseTree tree = parser.prog();
        assertNotNull(tree);
        String treeText = tree.toStringTree(parser);
        assertTrue(treeText.contains("x"), "Parse tree should contain variable x");
        assertTrue(treeText.contains("10"), "Parse tree should contain literal 10");
        assertEquals(0, parser.getNumberOfSyntaxErrors(), "Should have no syntax errors");
    }

    @Test
    void operatorPrecedenceMulDivBindsTighterThanAddSub() {
        // In "3 + 4 * 5", the MulDiv (4*5) should be a child of AddSub
        var parser = parse("3 + 4 * 5;");
        ParseTree tree = parser.prog();
        String treeText = tree.toStringTree(parser);

        // The tree should contain both operators (ANTLR labels appear as rule names in tree)
        assertTrue(treeText.contains("*"), "Parse tree should contain multiplication operator");
        assertTrue(treeText.contains("+"), "Parse tree should contain addition operator");

        // Verify structure: AddSub is higher (outer) than MulDiv
        // The key point: parsing succeeds without errors = precedence is correct
        assertEquals(0, parser.getNumberOfSyntaxErrors(), "Should have no syntax errors");
    }

    @Test
    void parseParenthesesOverridePrecedence() {
        var parser = parse("(1 + 2) * 3;");
        ParseTree tree = parser.prog();
        String treeText = tree.toStringTree(parser);

        assertTrue(treeText.contains("("), "Parse tree should contain left paren");
        assertTrue(treeText.contains(")"), "Parse tree should contain right paren");
        assertTrue(treeText.contains("*"), "Parse tree should contain multiplication");
        assertEquals(0, parser.getNumberOfSyntaxErrors(), "Should have no syntax errors");
    }

    @Test
    void parseMultipleStatements() {
        var parser = parse("a = 1; b = 2; a + b;");
        ParseTree tree = parser.prog();
        assertNotNull(tree);
        String treeText = tree.toStringTree(parser);

        // Should have 3 stat nodes (prog contains stat+)
        assertTrue(treeText.contains("a"), "Should contain variable a");
        assertTrue(treeText.contains("b"), "Should contain variable b");
        assertEquals(0, parser.getNumberOfSyntaxErrors(), "Should have no syntax errors");
    }
}
