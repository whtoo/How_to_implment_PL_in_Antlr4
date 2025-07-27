package org.teachfx.antlr4.ep20;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep20.ast.ASTNode;
import org.teachfx.antlr4.ep20.ir.Prog;
import org.teachfx.antlr4.ep20.parser.CymbolLexer;
import org.teachfx.antlr4.ep20.parser.CymbolParser;
import org.teachfx.antlr4.ep20.pass.ast.CymbolASTBuilder;
import org.teachfx.antlr4.ep20.pass.codegen.CymbolAssembler;
import org.teachfx.antlr4.ep20.pass.ir.CymbolIRBuilder;
import org.teachfx.antlr4.ep20.pass.symtab.LocalDefine;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

public class OperatorsTest {
    
    @Test
    public void testModuloOperator() {
        String source = "int test() { int a = 10; int b = 3; return a % b; }";
        assertTrue(canParse(source), "Should parse modulo operator");
    }
    
    @Test
    public void testLogicalAndOperator() {
        String source = "int test() { bool a = true; bool b = false; return a && b; }";
        assertTrue(canParse(source), "Should parse logical and operator");
    }
    
    @Test
    public void testComplexExpressionWithNewOperators() {
        String source = "int test() { int x = 15; int y = 4; bool flag = true; return (x % y) * 2 && flag; }";
        assertTrue(canParse(source), "Should parse complex expression with new operators");
    }
    
    @Test
    public void testModuloEdgeCases() {
        assertTrue(canParse("int test() { return 5 % 5; }"));
        assertTrue(canParse("int test() { return 0 % 5; }"));
        assertTrue(canParse("int test() { return 7 % 4; }"));
    }
    
    @Test
    public void testLogicalAndInCondition() {
        assertTrue(canParse("int test() { int x = 5; int y = 10; if (x > 0 && y > 0) return 1; else return 0; }"));
    }
    
    private boolean canParse(String source) {
        try {
            InputStream is = new ByteArrayInputStream(source.getBytes());
            var charStream = CharStreams.fromStream(is);
            var lexer = new CymbolLexer(charStream);
            var tokenStream = new CommonTokenStream(lexer);
            var parser = new CymbolParser(tokenStream);
            parser.file();
            return parser.getNumberOfSyntaxErrors() == 0;
        } catch (Exception e) {
            return false;
        }
    }
    
    private String compileToAssembly(String source) {
        try {
            InputStream is = new ByteArrayInputStream(source.getBytes());
            var charStream = CharStreams.fromStream(is);
            var lexer = new CymbolLexer(charStream);
            var tokenStream = new CommonTokenStream(lexer);
            var parser = new CymbolParser(tokenStream);
            ParseTree parseTree = parser.file();

            var astBuilder = new CymbolASTBuilder();
            ASTNode astRoot = parseTree.accept(astBuilder);
            astRoot.accept(new LocalDefine());

            var irBuilder = new CymbolIRBuilder();
            astRoot.accept(irBuilder);
            
            irBuilder.prog.optimizeBasicBlock();
            
            var assembler = new CymbolAssembler();
            assembler.visit(irBuilder.prog);
            return assembler.getAsmInfo();
            
        } catch (Exception e) {
            throw new RuntimeException("Compilation failed: " + e.getMessage(), e);
        }
    }
}