package org.teachfx.antlr4.ep04;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.teachfx.antlr4.ep04.ast.ASTNode;
import org.teachfx.antlr4.ep04.parser.CymbolLexer;
import org.teachfx.antlr4.ep04.parser.CymbolParser;
import org.teachfx.antlr4.ep04.visitor.ASTBuilder;
import org.teachfx.antlr4.ep04.visitor.ASTEvaluator;
import org.teachfx.antlr4.ep04.visitor.ASTPrinter;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * EP04 — AST: Meaningful Trees
 *
 * Full pipeline:
 *   Source → Tokens → ParseTree → AST → Print → Evaluate
 *
 * Key insight: ParseTree has punctuation and rule nodes.
 * AST has only what matters — values, operations, assignments.
 */
public class Compiler {
    public static void main(String[] args) throws IOException {
        String fileName = (args.length > 0) ? args[0] : "src/main/resources/t.cymbol";
        InputStream is = new FileInputStream(fileName);

        // Phase 1: Lex + Parse (same as EP01)
        var charStream = CharStreams.fromStream(is);
        var lexer      = new CymbolLexer(charStream);
        var tokens     = new CommonTokenStream(lexer);
        var parser     = new CymbolParser(tokens);
        var parseTree  = parser.prog();

        // Phase 2: Build AST (NEW!)
        var builder = new ASTBuilder();
        var astRoot = builder.visit(parseTree);

        // But we want ALL statements, not just the first.
        // Walk the parse tree to get all statements.
        System.out.println("=== Parse Tree ===");
        System.out.println(parseTree.toStringTree(parser));
        System.out.println();

        // Build full AST by visiting each statement
        System.out.println("=== AST ===");
        for (var stmt : parseTree.stat()) {
            ASTNode node = builder.visit(stmt);
            var printer = new ASTPrinter();
            printer.visit(node);
        }
        System.out.println();

        // Phase 3: Evaluate AST (separate pass!)
        System.out.println("=== Evaluation ===");
        var evaluator = new ASTEvaluator();
        for (var stmt : parseTree.stat()) {
            ASTNode node = builder.visit(stmt);
            evaluator.visit(node);
        }
    }
}
