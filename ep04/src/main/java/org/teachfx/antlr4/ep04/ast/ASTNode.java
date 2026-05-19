package org.teachfx.antlr4.ep04.ast;

/**
 * ASTNode — abstract base for all AST nodes.
 *
 * The AST (Abstract Syntax Tree) is a simplified tree
 * that captures the ESSENCE of the program.
 *
 * Unlike a parse tree:
 *   - No punctuation tokens ( ; = etc.)
 *   - No intermediate rule nodes (stat, expr)
 *   - Only semantic constructs: values, operations, assignments
 *
 * This design follows the Visitor pattern so we can
 * easily add new operations (pretty-print, evaluate,
 * type-check, code-gen) without modifying node classes.
 */
public abstract class ASTNode {

    /** Accept a visitor — the core of the Visitor pattern. */
    public abstract <T> T accept(ASTVisitor<T> visitor);

    /** Get a human-readable label for this node type. */
    public String nodeType() {
        return this.getClass().getSimpleName();
    }

    /** Pretty-print this node with indentation. */
    public void print(String indent) {
        System.out.println(indent + nodeType());
    }
}
