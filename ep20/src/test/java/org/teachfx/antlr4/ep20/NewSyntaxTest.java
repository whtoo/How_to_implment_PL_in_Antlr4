package org.teachfx.antlr4.ep20;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep20.ast.ASTNode;
import org.teachfx.antlr4.ep20.parser.CymbolLexer;
import org.teachfx.antlr4.ep20.parser.CymbolParser;
import org.teachfx.antlr4.ep20.pass.ast.CymbolASTBuilder;
import org.teachfx.antlr4.ep20.pass.symtab.LocalDefine;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

public class NewSyntaxTest {
    
    @Test
    public void testTypeCastExpression() {
        String source = "int test() { float f = 3.14; int i = (int)f; return i; }";
        assertTrue(canParse(source), "Should parse type cast expression");
    }
    
    @Test
    public void testTypedefDeclaration() {
        String source = "typedef int MyInt; int test() { MyInt x = 10; return x; }";
        assertTrue(canParse(source), "Should parse typedef declaration");
    }
    
    @Test
    public void testStructDeclaration() {
        String source = "struct Person { int age; string name; } int test() { Person p; p.age = 25; return p.age; }";
        assertTrue(canParse(source), "Should parse struct declaration");
    }
    
    @Test
    public void testFieldAccess() {
        String source = "struct Point { int x; int y; } int test() { Point p; p.x = 10; p.y = 20; return p.x + p.y; }";
        assertTrue(canParse(source), "Should parse field access");
    }
    
    @Test
    public void testArrayLiteral() {
        String source = "int test() { int arr[5] = {1, 2, 3, 4, 5}; return arr[0]; }";
        assertTrue(canParse(source), "Should parse array literal");
    }
    
    @Test
    public void testComplexStructWithTypedef() {
        String source = "typedef int Age; struct Person { Age age; string name; } int test() { Person p; p.age = 30; return p.age; }";
        assertTrue(canParse(source), "Should parse complex struct with typedef");
    }
    
    @Test
    public void testNestedFieldAccess() {
        String source = "struct Point { int x; int y; } struct Line { Point start; Point end; } int test() { Line l; l.start.x = 10; l.end.y = 20; return l.start.x + l.end.y; }";
        assertTrue(canParse(source), "Should parse nested field access");
    }
    
    @Test
    public void testTypeCastInExpression() {
        String source = "int test() { float f = 3.7; int result = (int)f + 5; return result; }";
        assertTrue(canParse(source), "Should parse type cast in expression");
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
    
    private boolean canBuildAST(String source) {
        try {
            InputStream is = new ByteArrayInputStream(source.getBytes());
            var charStream = CharStreams.fromStream(is);
            var lexer = new CymbolLexer(charStream);
            var tokenStream = new CommonTokenStream(lexer);
            var parser = new CymbolParser(tokenStream);
            ParseTree parseTree = parser.file();
            
            if (parser.getNumberOfSyntaxErrors() > 0) {
                return false;
            }
            
            var astBuilder = new CymbolASTBuilder();
            ASTNode astRoot = parseTree.accept(astBuilder);
            astRoot.accept(new LocalDefine());
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}