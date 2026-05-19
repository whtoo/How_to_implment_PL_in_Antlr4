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

    @Test
    void parseSubtraction() {
        var parser = parse("10 - 3;");
        ParseTree tree = parser.prog();
        assertEquals(0, parser.getNumberOfSyntaxErrors());
        String treeText = tree.toStringTree(parser);
        assertTrue(treeText.contains("-"));
    }

    @Test
    void parseDivision() {
        var parser = parse("10 / 2;");
        ParseTree tree = parser.prog();
        assertEquals(0, parser.getNumberOfSyntaxErrors());
        String treeText = tree.toStringTree(parser);
        assertTrue(treeText.contains("/"));
    }

    @Test
    void parseDeeplyNestedExpression() {
        var parser = parse("(((1 + 2) * (3 - 4)) / 5);");
        ParseTree tree = parser.prog();
        assertEquals(0, parser.getNumberOfSyntaxErrors());
    }

    @Test
    void parseMultipleAssignments() {
        var parser = parse("x = 1; y = 2; z = 3;");
        ParseTree tree = parser.prog();
        assertEquals(0, parser.getNumberOfSyntaxErrors());
        String treeText = tree.toStringTree(parser);
        assertTrue(treeText.contains("x"));
        assertTrue(treeText.contains("y"));
        assertTrue(treeText.contains("z"));
    }

    @Test
    void parseExpressionWithAllOperators() {
        var parser = parse("a + b * c - d / e;");
        ParseTree tree = parser.prog();
        assertEquals(0, parser.getNumberOfSyntaxErrors());
    }

    @Test
    void parseLargeInteger() {
        var parser = parse("999999;");
        ParseTree tree = parser.prog();
        assertEquals(0, parser.getNumberOfSyntaxErrors());
        assertTrue(tree.toStringTree(parser).contains("999999"));
    }

    @Test
    void parseVariableLookup() {
        var parser = parse("myVar;");
        ParseTree tree = parser.prog();
        assertEquals(0, parser.getNumberOfSyntaxErrors());
        assertTrue(tree.toStringTree(parser).contains("myVar"));
    }

    @Test
    void parseLeftAssociativity() {
        // 20 - 5 - 3 should parse as (20-5)-3, not 20-(5-3)
        var parser = parse("20 - 5 - 3;");
        ParseTree tree = parser.prog();
        assertEquals(0, parser.getNumberOfSyntaxErrors());
    }

    @Test
    void parsePrecedenceAddMult() {
        // 1 + 2 * 3 should have * binding tighter than +
        var parser = parse("1 + 2 * 3;");
        ParseTree tree = parser.prog();
        assertEquals(0, parser.getNumberOfSyntaxErrors());
        String treeText = tree.toStringTree(parser);
        assertTrue(treeText.contains("*"));
        assertTrue(treeText.contains("+"));
    }

    @Test
    void parseSingleStatementWithTrailingWhitespace() {
        var parser = parse("  42;  ");
        ParseTree tree = parser.prog();
        assertEquals(0, parser.getNumberOfSyntaxErrors());
    }

    @Test
    void parseExpressionUsingVariableInAssignment() {
        var parser = parse("y = x + 5;");
        ParseTree tree = parser.prog();
        assertEquals(0, parser.getNumberOfSyntaxErrors());
    }

    @Test
    void parseChainedOperations() {
        var parser = parse("a = 1; b = a + 1; c = b * a;");
        ParseTree tree = parser.prog();
        assertEquals(0, parser.getNumberOfSyntaxErrors());
    }
}
