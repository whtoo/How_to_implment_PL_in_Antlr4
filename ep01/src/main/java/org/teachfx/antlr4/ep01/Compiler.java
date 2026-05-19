package org.teachfx.antlr4.ep01;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.teachfx.antlr4.ep01.parser.CymbolLexer;
import org.teachfx.antlr4.ep01.parser.CymbolParser;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * EP01 — Hello, Compiler!
 *
 * Reads a .cymbol source file, parses it, evaluates it,
 * and prints each expression's result.
 *
 * Usage:
 *   mvn exec:java -Dexec.args="src/main/resources/t.cymbol"
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

        System.out.println("=== Parse Tree ===");
        System.out.println(tree.toStringTree(parser));
        System.out.println();

        System.out.println("=== Evaluation ===");
        var eval = new EvalVisitor();
        eval.visit(tree);
    }
}
