package org.teachfx.antlr4.ep04.visitor;

import org.teachfx.antlr4.ep04.ast.*;

import java.util.HashMap;
import java.util.Map;

/**
 * ASTEvaluator — evaluates the AST by walking it.
 *
 * Separate from ASTBuilder: build once, evaluate many times.
 * This separation is the key insight of multi-pass compilers.
 */
public class ASTEvaluator implements ASTVisitor<Integer> {

    private final Map<String, Integer> memory = new HashMap<>();

    @Override
    public Integer visit(IntNode node) {
        return node.value;
    }

    @Override
    public Integer visit(IdNode node) {
        return memory.getOrDefault(node.name, 0);
    }

    @Override
    public Integer visit(BinaryOpNode node) {
        int left = node.left.accept(this);
        int right = node.right.accept(this);
        return switch (node.op) {
            case "+" -> left + right;
            case "-" -> left - right;
            case "*" -> left * right;
            case "/" -> left / right;
            default -> throw new IllegalStateException("Unknown op: " + node.op);
        };
    }

    @Override
    public Integer visit(AssignNode node) {
        int value = node.value.accept(this);
        memory.put(node.name, value);
        return value;
    }

    @Override
    public Integer visit(PrintNode node) {
        int value = node.expr.accept(this);
        System.out.println(value);
        return value;
    }

    @Override
    public Integer visit(ASTNode node) {
        return node.accept(this);
    }
}
