package org.teachfx.antlr4.ep02.test;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;
import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep02.ManualLexer;
import org.teachfx.antlr4.ep02.parser.CymbolLexer;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EP02 tests: manual lexer vs ANTLR-generated lexer comparison.
 */
public class ManualLexerTest {

    @Test
    void testManualLexerTokenizesBasicInput() {
        String source = "x = 42;";
        ManualLexer lexer = new ManualLexer(source);
        List<ManualLexer.Token> tokens = lexer.lexAll();

        assertEquals(5, tokens.size()); // ID, ASSIGN, INT, SEMI, EOF
        assertEquals(ManualLexer.TokenType.ID, tokens.get(0).type);
        assertEquals("x", tokens.get(0).text);
        assertEquals(ManualLexer.TokenType.ASSIGN, tokens.get(1).type);
        assertEquals(ManualLexer.TokenType.INT, tokens.get(2).type);
        assertEquals("42", tokens.get(2).text);
        assertEquals(ManualLexer.TokenType.SEMI, tokens.get(3).type);
    }

    @Test
    void testManualLexerSkipsLineComment() {
        String source = "x = 42; // this is a comment\ny = 10;";
        ManualLexer lexer = new ManualLexer(source);
        List<ManualLexer.Token> tokens = lexer.lexAll();

        // Should have: x, =, 42, ;, y, =, 10, ;, EOF
        assertEquals(9, tokens.size());
        assertEquals(ManualLexer.TokenType.ID, tokens.get(4).type);
        assertEquals("y", tokens.get(4).text);
    }

    @Test
    void testManualLexerRecognizesKeywords() {
        String source = "int while if else return";
        ManualLexer lexer = new ManualLexer(source);
        List<ManualLexer.Token> tokens = lexer.lexAll();

        assertEquals(ManualLexer.TokenType.INT_KW, tokens.get(0).type);
        assertEquals(ManualLexer.TokenType.WHILE, tokens.get(1).type);
        assertEquals(ManualLexer.TokenType.IF, tokens.get(2).type);
        assertEquals(ManualLexer.TokenType.ELSE, tokens.get(3).type);
        assertEquals(ManualLexer.TokenType.RETURN, tokens.get(4).type);
    }

    @Test
    void testAntlrLexerProducesSameTokenCount() {
        String source = "x = 42 + y;";
        ManualLexer manual = new ManualLexer(source);
        List<ManualLexer.Token> manualTokens = manual.lexAll();

        var antlrLexer = new CymbolLexer(CharStreams.fromString(source));
        List<? extends Token> antlrTokens = antlrLexer.getAllTokens();

        // Both should produce the same number of non-EOF tokens
        assertEquals(manualTokens.size() - 1, antlrTokens.size());
    }

    @Test
    void testManualLexerTokenTypesMatchAntlr() {
        String source = "a = 10 * 5;";
        ManualLexer manual = new ManualLexer(source);
        List<ManualLexer.Token> manualTokens = manual.lexAll();

        var antlrLexer = new CymbolLexer(CharStreams.fromString(source));
        List<? extends Token> antlrTokens = antlrLexer.getAllTokens();

        assertEquals(manualTokens.size() - 1, antlrTokens.size());
        for (int i = 0; i < antlrTokens.size(); i++) {
            assertEquals(manualTokens.get(i).text, antlrTokens.get(i).getText());
        }
    }

    @Test
    void testManualLexerAllOperators() {
        String source = "a + b - c * d / e;";
        ManualLexer lexer = new ManualLexer(source);
        List<ManualLexer.Token> tokens = lexer.lexAll();

        assertEquals(ManualLexer.TokenType.ID, tokens.get(0).type);
        assertEquals("a", tokens.get(0).text);
        assertEquals(ManualLexer.TokenType.ADD, tokens.get(1).type);
        assertEquals(ManualLexer.TokenType.ID, tokens.get(2).type);
        assertEquals("b", tokens.get(2).text);
        assertEquals(ManualLexer.TokenType.SUB, tokens.get(3).type);
        assertEquals(ManualLexer.TokenType.ID, tokens.get(4).type);
        assertEquals("c", tokens.get(4).text);
        assertEquals(ManualLexer.TokenType.MUL, tokens.get(5).type);
        assertEquals(ManualLexer.TokenType.ID, tokens.get(6).type);
        assertEquals("d", tokens.get(6).text);
        assertEquals(ManualLexer.TokenType.DIV, tokens.get(7).type);
        assertEquals(ManualLexer.TokenType.ID, tokens.get(8).type);
        assertEquals("e", tokens.get(8).text);
        assertEquals(ManualLexer.TokenType.SEMI, tokens.get(9).type);
    }

    @Test
    void testManualLexerParens() {
        String source = "(x + y) * z;";
        ManualLexer lexer = new ManualLexer(source);
        List<ManualLexer.Token> tokens = lexer.lexAll();

        assertEquals(ManualLexer.TokenType.LPAREN, tokens.get(0).type);
        assertEquals(ManualLexer.TokenType.RPAREN, tokens.get(4).type);
    }

    @Test
    void testManualLexerBlockComment() {
        String source = "x = 10; /* ignore this */ y = 20;";
        ManualLexer lexer = new ManualLexer(source);
        List<ManualLexer.Token> tokens = lexer.lexAll();

        // Should skip the block comment entirely
        boolean foundIgnore = false;
        for (var t : tokens) {
            if ("ignore".equals(t.text) || "ignore this".contains(t.text)) foundIgnore = true;
        }
        assertFalse(foundIgnore, "Block comment content should be skipped");
        // Should still have both statements
        assertEquals(ManualLexer.TokenType.ID, tokens.get(0).type);
        assertEquals("y", tokens.get(4).text);
    }

    @Test
    void testManualLexerMultiLineBlockComment() {
        String source = "x = 10; /* line1\nline2\nline3 */ y = 20;";
        ManualLexer lexer = new ManualLexer(source);
        List<ManualLexer.Token> tokens = lexer.lexAll();

        assertEquals("y", tokens.get(4).text);
    }

    @Test
    void testManualLexerAllKeywords() {
        String source = "int if else while return float void struct";
        ManualLexer lexer = new ManualLexer(source);
        List<ManualLexer.Token> tokens = lexer.lexAll();

        assertEquals(ManualLexer.TokenType.INT_KW, tokens.get(0).type);
        assertEquals(ManualLexer.TokenType.IF, tokens.get(1).type);
        assertEquals(ManualLexer.TokenType.ELSE, tokens.get(2).type);
        assertEquals(ManualLexer.TokenType.WHILE, tokens.get(3).type);
        assertEquals(ManualLexer.TokenType.RETURN, tokens.get(4).type);
        assertEquals(ManualLexer.TokenType.FLOAT_KW, tokens.get(5).type);
        assertEquals(ManualLexer.TokenType.VOID, tokens.get(6).type);
        assertEquals(ManualLexer.TokenType.STRUCT, tokens.get(7).type);
    }

    @Test
    void testManualLexerMultiDigitInteger() {
        String source = "12345;";
        ManualLexer lexer = new ManualLexer(source);
        List<ManualLexer.Token> tokens = lexer.lexAll();

        assertEquals(ManualLexer.TokenType.INT, tokens.get(0).type);
        assertEquals("12345", tokens.get(0).text);
    }

    @Test
    void testManualLexerIdentifierWithDigits() {
        String source = "var123 foo_bar x1 y2;";
        ManualLexer lexer = new ManualLexer(source);
        List<ManualLexer.Token> tokens = lexer.lexAll();

        assertEquals("var123", tokens.get(0).text);
        assertEquals("foo_bar", tokens.get(1).text);
        assertEquals("x1", tokens.get(2).text);
        assertEquals("y2", tokens.get(3).text);
    }

    @Test
    void testManualLexerMultipleLines() {
        String source = "x = 42;\ny = 10;\nz = x + y;";
        ManualLexer lexer = new ManualLexer(source);
        List<ManualLexer.Token> tokens = lexer.lexAll();

        // Verify line numbers
        assertTrue(tokens.get(0).line >= 1);
    }

    @Test
    void testManualLexerEmptySource() {
        String source = "";
        ManualLexer lexer = new ManualLexer(source);
        List<ManualLexer.Token> tokens = lexer.lexAll();

        // Should only have EOF
        assertEquals(1, tokens.size());
        assertEquals(ManualLexer.TokenType.EOF, tokens.get(0).type);
    }

    @Test
    void testManualLexerSingleToken() {
        String source = ";";
        ManualLexer lexer = new ManualLexer(source);
        List<ManualLexer.Token> tokens = lexer.lexAll();

        assertEquals(2, tokens.size()); // SEMI + EOF
        assertEquals(ManualLexer.TokenType.SEMI, tokens.get(0).type);
    }

    @Test
    void testManualLexerMixedWhitespace() {
        String source = "  x \t = \n 42 \r\n ;";
        ManualLexer lexer = new ManualLexer(source);
        List<ManualLexer.Token> tokens = lexer.lexAll();

        assertEquals("x", tokens.get(0).text);
        assertEquals(ManualLexer.TokenType.ASSIGN, tokens.get(1).type);
        assertEquals("42", tokens.get(2).text);
        assertEquals(ManualLexer.TokenType.SEMI, tokens.get(3).type);
    }

    @Test
    void testManualLexerCommentAtEndOfFile() {
        String source = "x = 42; // trailing comment";
        ManualLexer lexer = new ManualLexer(source);
        List<ManualLexer.Token> tokens = lexer.lexAll();

        assertEquals("x", tokens.get(0).text);
        assertEquals(ManualLexer.TokenType.ASSIGN, tokens.get(1).type);
        assertEquals("42", tokens.get(2).text);
        assertEquals(ManualLexer.TokenType.SEMI, tokens.get(3).type);
    }

    @Test
    void testManualLexerDivNotComment() {
        String source = "a / b;";
        ManualLexer lexer = new ManualLexer(source);
        List<ManualLexer.Token> tokens = lexer.lexAll();

        // '/' alone should be DIV token, not start of comment
        assertEquals(ManualLexer.TokenType.DIV, tokens.get(1).type);
    }
}
