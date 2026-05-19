package org.teachfx.antlr4.ep04.test;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep04.ast.*;
import org.teachfx.antlr4.ep04.parser.CymbolLexer;
import org.teachfx.antlr4.ep04.parser.CymbolParser;
import org.teachfx.antlr4.ep04.visitor.ASTBuilder;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EP04 tests: AST construction from parse tree.
 */
public class ASTBuilderTest {

    private ASTNode build(String input) {
        var lexer = new CymbolLexer(CharStreams.fromString(input));
        var tokens = new CommonTokenStream(lexer);
        var parser = new CymbolParser(tokens);
        var tree = parser.prog();
        return new ASTBuilder().visit(tree);
    }

    @Test
    void testBuildIntNode() {
        ASTNode node = build("42;");
        assertTrue(node instanceof PrintNode);
        assertTrue(((PrintNode) node).expr instanceof IntNode);
        assertEquals(42, ((IntNode) ((PrintNode) node).expr).value);
    }

    @Test
    void testBuildBinaryOpNode() {
        ASTNode node = build("1 + 2;");
        assertTrue(node instanceof PrintNode);
        var bin = (BinaryOpNode) ((PrintNode) node).expr;
        assertEquals("+", bin.op);
        assertTrue(bin.left instanceof IntNode);
        assertTrue(bin.right instanceof IntNode);
    }

    @Test
    void testBuildAssignNode() {
        ASTNode node = build("x = 10;");
        assertTrue(node instanceof AssignNode);
        var assign = (AssignNode) node;
        assertEquals("x", assign.name);
        assertTrue(assign.value instanceof IntNode);
    }

    @Test
    void testBuildIdNode() {
        ASTNode node = build("x;");
        assertTrue(node instanceof PrintNode);
        assertTrue(((PrintNode) node).expr instanceof IdNode);
        assertEquals("x", ((IdNode) ((PrintNode) node).expr).name);
    }

    @Test
    void testOperatorPrecedenceInAST() {
        ASTNode node = build("1 + 2 * 3;");
        assertTrue(node instanceof PrintNode);
        var add = (BinaryOpNode) ((PrintNode) node).expr;
        assertEquals("+", add.op);
        assertNotNull(add.left);
        assertNotNull(add.right);
    }
}
