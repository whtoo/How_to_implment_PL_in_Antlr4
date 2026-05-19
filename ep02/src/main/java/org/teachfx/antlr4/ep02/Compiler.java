package org.teachfx.antlr4.ep02;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;
import org.teachfx.antlr4.ep02.parser.CymbolLexer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * EP02 — Tokens: Text→Words
 *
 * Demonstrates two approaches to lexical analysis:
 *   1. ManualLexer — hand-coded state machine (for learning)
 *   2. ANTLR4 CymbolLexer — generated from grammar
 *
 * Both produce the same token stream!
 *
 * Usage:
 *   mvn exec:java -pl ep02 -Dexec.args="src/main/resources/t.cymbol"
 */
public class Compiler {
    public static void main(String[] args) throws IOException {
        String fileName = (args.length > 0) ? args[0] : "src/main/resources/t.cymbol";

        // Read source once
        String source = Files.readString(Path.of(fileName));

        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║   EP02 — Tokens: Text → Words       ║");
        System.out.println("║   Lexical Analysis: Step 1 of       ║");
        System.out.println("║   compilation pipeline              ║");
        System.out.println("╚══════════════════════════════════════╝");
        System.out.println();

        // ==========================================
        // Approach 1: Hand-coded lexer
        // ==========================================
        System.out.println("===== Approach 1: ManualLexer (hand-coded) =====");
        System.out.println();
        ManualLexer manual = new ManualLexer(source);
        List<ManualLexer.Token> manualTokens = manual.lexAll();
        for (ManualLexer.Token t : manualTokens) {
            System.out.println("  " + t);
        }
        System.out.println();

        // ==========================================
        // Approach 2: ANTLR4-generated lexer
        // ==========================================
        System.out.println("===== Approach 2: ANTLR4 CymbolLexer (generated) =====");
        System.out.println();
        var charStream = CharStreams.fromString(source);
        var lexer = new CymbolLexer(charStream);
        List<? extends Token> antlrTokens = lexer.getAllTokens();
        for (Token t : antlrTokens) {
            String typeName = lexer.getVocabulary().getSymbolicName(t.getType());
            System.out.println("  TOKEN[" + typeName + "]='" + t.getText() + "' at line " + t.getLine());
        }
        System.out.println();

        // ==========================================
        // Comparison summary
        // ==========================================
        System.out.println("===== Comparison =====");
        System.out.println("Both approaches produce the SAME token stream.");
        System.out.println("The ANTLR4 lexer is generated from Cymbol.g4 —");
        System.out.println("no manual state-machine coding needed!");
        System.out.println();
        System.out.println("Next: EP03 — Grammar: Words → Trees");
    }
}
