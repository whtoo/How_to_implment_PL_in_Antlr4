package org.teachfx.antlr4.ep02;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ManualLexer — a hand-coded lexer for Cymbol.
 *
 * PURPOSE: Before ANTLR4 can generate a lexer for you,
 * it's essential to understand HOW lexing works under
 * the hood. This class implements a simple state-machine
 * lexer that reads characters one by one and produces
 * tokens — just like a real lexer generator would.
 *
 * COMPARE: Run this against ManualLexer.main(), then
 * compare with the ANTLR4-generated CymbolLexer in
 * Compiler.java. Same tokens, same result.
 */
public class ManualLexer {

    /** All token types our lexer can produce. */
    public enum TokenType {
        ID, INT, MUL, DIV, ADD, SUB, ASSIGN, SEMI,
        LPAREN, RPAREN, IF, ELSE, WHILE, RETURN,
        INT_KW, FLOAT_KW, VOID, STRUCT, EOF
    }

    /** A single token: type, text, and source position. */
    public static class Token {
        public final TokenType type;
        public final String text;
        public final int line;

        public Token(TokenType type, String text, int line) {
            this.type = type;
            this.text = text;
            this.line = line;
        }

        @Override
        public String toString() {
            return "TOKEN[" + type + "]='" + text + "' at line " + line;
        }
    }

    /** Maps keyword strings to their token types. */
    private static final java.util.Map<String, TokenType> KEYWORDS =
        java.util.Map.of(
            "if",     TokenType.IF,
            "else",   TokenType.ELSE,
            "while",  TokenType.WHILE,
            "return", TokenType.RETURN,
            "int",    TokenType.INT_KW,
            "float",  TokenType.FLOAT_KW,
            "void",   TokenType.VOID,
            "struct", TokenType.STRUCT
        );

    private final String source;
    private int pos;       // current position in source
    private int line;      // current line number

    public ManualLexer(String source) {
        this.source = source;
        this.pos = 0;
        this.line = 1;
    }

    /** Are we at end of file? */
    private boolean eof() {
        return pos >= source.length();
    }

    /** Peek at current character without consuming. */
    private char peek() {
        return eof() ? '\0' : source.charAt(pos);
    }

    /** Consume current character and return it. */
    private char advance() {
        char c = source.charAt(pos++);
        if (c == '\n') line++;
        return c;
    }

    /** Peek at next character (one ahead). */
    private char peekNext() {
        return pos + 1 >= source.length() ? '\0' : source.charAt(pos + 1);
    }

    /** Check if character is a letter or underscore. */
    private static boolean isLetter(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    /** Check if character is a digit. */
    private static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    /** Check if character is whitespace. */
    private static boolean isWhitespace(char c) {
        return c == ' ' || c == '\t' || c == '\r' || c == '\n';
    }

    /** Produce the next token from the source. */
    public Token nextToken() {
        // Skip whitespace
        while (!eof() && isWhitespace(peek())) {
            advance();
        }

        if (eof()) {
            return new Token(TokenType.EOF, "<EOF>", line);
        }

        char c = peek();
        int startLine = line;

        // --- Single-character tokens ---
        switch (c) {
            case '*': advance(); return new Token(TokenType.MUL, "*", startLine);
            case '/': {
                advance();
                // Check for line comment "//"
                if (!eof() && peek() == '/') {
                    while (!eof() && peek() != '\n') advance();
                    return nextToken(); // skip comment
                }
                // Check for block comment "/*"
                if (!eof() && peek() == '*') {
                    advance(); // consume '*'
                    while (!eof()) {
                        if (advance() == '*' && !eof() && peek() == '/') {
                            advance(); // consume '/'
                            break;
                        }
                    }
                    return nextToken(); // skip comment
                }
                return new Token(TokenType.DIV, "/", startLine);
            }
            case '+': advance(); return new Token(TokenType.ADD, "+", startLine);
            case '-': advance(); return new Token(TokenType.SUB, "-", startLine);
            case '=': advance(); return new Token(TokenType.ASSIGN, "=", startLine);
            case ';': advance(); return new Token(TokenType.SEMI, ";", startLine);
            case '(': advance(); return new Token(TokenType.LPAREN, "(", startLine);
            case ')': advance(); return new Token(TokenType.RPAREN, ")", startLine);
        }

        // --- Identifiers & keywords ---
        if (isLetter(c)) {
            StringBuilder sb = new StringBuilder();
            while (!eof() && (isLetter(peek()) || isDigit(peek()))) {
                sb.append(advance());
            }
            String word = sb.toString();
            // Check if it's a keyword
            TokenType kwType = KEYWORDS.get(word);
            if (kwType != null) {
                return new Token(kwType, word, startLine);
            }
            return new Token(TokenType.ID, word, startLine);
        }

        // --- Integers ---
        if (isDigit(c)) {
            StringBuilder sb = new StringBuilder();
            while (!eof() && isDigit(peek())) {
                sb.append(advance());
            }
            return new Token(TokenType.INT, sb.toString(), startLine);
        }

        // --- Unknown character ---
        System.err.println("Warning: skipping unknown char '" + c + "' at line " + line);
        advance();
        return nextToken();
    }

    /** Lex the entire source and return all tokens. */
    public List<Token> lexAll() {
        List<Token> tokens = new ArrayList<>();
        Token t;
        while ((t = nextToken()).type != TokenType.EOF) {
            tokens.add(t);
        }
        tokens.add(t); // include EOF
        return tokens;
    }

    // ========================================
    // Demo: run the manual lexer on a sample
    // ========================================
    public static void main(String[] args) throws IOException {
        String source;
        if (args.length > 0) {
            source = readFile(args[0]);
        } else {
            // Built-in sample for demonstration
            source = "x = 42;\n"
                   + "y = x + 7;\n"
                   + "// this is a comment\n"
                   + "result = (x * y) / 2;\n";
        }

        System.out.println("=== Manual Lexer Output ===");
        System.out.println("Source:\n" + source);
        System.out.println("---");

        ManualLexer lexer = new ManualLexer(source);
        for (Token t : lexer.lexAll()) {
            System.out.println(t);
        }
    }

    /** Read entire file into a String. */
    private static String readFile(String path) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader r = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = r.readLine()) != null) {
                sb.append(line).append('\n');
            }
        }
        return sb.toString();
    }
}
