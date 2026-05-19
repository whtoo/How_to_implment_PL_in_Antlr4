package org.teachfx.antlr4.ep03;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.teachfx.antlr4.ep03.parser.CymbolLexer;
import org.teachfx.antlr4.ep03.parser.CymbolParser;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * EP03 — Grammar: Words→Trees
 *
 * Demonstrates parsing: tokens → parse tree.
 *
 * A parse tree is a complete, faithful record of how
 * the input was matched against the grammar. It contains
 * EVERY token (including punctuation) and EVERY rule
 * applied by the parser.
 *
 * KEY LESSON: The parse tree reveals operator precedence.
 * In '3 + 4 * 5', the '*' binds tighter — so 4*5 is a
 * subtree of the '+' node, not the other way around.
 *
 * Usage:
 *   mvn exec:java -pl ep03 -Dexec.args="src/main/resources/t.cymbol"
 */
public class Compiler {
    public static void main(String[] args) throws IOException {
        String fileName = (args.length > 0) ? args[0] : "src/main/resources/t.cymbol";
        InputStream is = new FileInputStream(fileName);

        var charStream = CharStreams.fromStream(is);
        var lexer      = new CymbolLexer(charStream);
        var tokens     = new CommonTokenStream(lexer);
        var parser     = new CymbolParser(tokens);
        var tree       = parser.prog();

        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║   EP03 — Grammar: Words → Trees     ║");
        System.out.println("║   Parsing: Tokens → Parse Tree      ║");
        System.out.println("╚══════════════════════════════════════╝");
        System.out.println();

        // ==========================================
        // View 1: Lisp-style (compact)
        // ==========================================
        System.out.println("===== View 1: Lisp-style Parse Tree =====");
        System.out.println();
        System.out.println(tree.toStringTree(parser));
        System.out.println();

        // ==========================================
        // View 2: Indented tree (hierarchical)
        // ==========================================
        System.out.println("===== View 2: Indented Parse Tree =====");
        System.out.println();
        TreePrinter.print(tree, parser);
        System.out.println();

        // ==========================================
        // Explanation
        // ==========================================
        System.out.println("===== Key Observations =====");
        System.out.println();
        System.out.println("1. OPERATOR PRECEDENCE: In '3+4*5', the MulDiv subtree");
        System.out.println("   appears as a child of the AddSub node. This means");
        System.out.println("   multiplication happens BEFORE addition.");
        System.out.println();
        System.out.println("2. PUNCTUATION IN TREE: Semicolons, parentheses —");
        System.out.println("   everything appears in the parse tree. (EP04 will");
        System.out.println("   strip these away into an AST.)");
        System.out.println();
        System.out.println("3. LABELED ALTERNATIVES: The grammar uses #MulDiv,");
        System.out.println("   #AddSub, etc. — these give names to alternative");
        System.out.println("   branches for targeted visiting.");
        System.out.println();
        System.out.println("Next: EP04 — AST: Meaningful Trees");
    }
}
