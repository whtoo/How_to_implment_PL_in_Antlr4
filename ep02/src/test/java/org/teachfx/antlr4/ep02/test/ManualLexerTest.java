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
}
