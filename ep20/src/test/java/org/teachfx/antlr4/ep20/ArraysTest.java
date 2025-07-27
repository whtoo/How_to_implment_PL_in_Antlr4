package org.teachfx.antlr4.ep20;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep20.parser.CymbolLexer;
import org.teachfx.antlr4.ep20.parser.CymbolParser;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

public class ArraysTest {
    
    @Test
    public void testArrayDeclaration() {
        String source = "int test() { int arr[5]; arr[0] = 10; return arr[0]; }";
        assertTrue(canParse(source), "Should parse array declaration");
    }
    
    @Test
    public void testArrayWithInitialization() {
        String source = "int test() { int numbers[3] = {1, 2, 3}; return numbers[0]; }";
        assertTrue(canParse(source), "Should parse array with initialization");
    }
    
    @Test
    public void testArrayAccess() {
        String source = "int test() { int arr[3] = {10, 20, 30}; return arr[1]; }";
        assertTrue(canParse(source), "Should parse array access");
    }
    
    @Test
    public void testArrayIndexExpression() {
        String source = "int test() { int arr[5] = {1, 2, 3, 4, 5}; int index = 2; return arr[index + 1]; }";
        assertTrue(canParse(source), "Should parse array index expression");
    }
    
    @Test
    public void testDifferentTypes() {
        assertTrue(canParse("int test() { float floats[2]; floats[0] = 1.5; return (int)floats[0]; }"));
        assertTrue(canParse("int test() { bool flags[3]; flags[0] = true; return 1; }"));
    }
    
    @Test
    public void testArrayInFunctionParameters() {
        String source = "int sum(int arr[3]) { return arr[0] + arr[1] + arr[2]; }";
        assertTrue(canParse(source), "Should parse array in function parameters");
    }
    
    @Test
    public void testArrayAssignment() {
        String source = "int test() { int arr[4]; arr[0] = 100; arr[1] = arr[0] + 50; return arr[1]; }";
        assertTrue(canParse(source), "Should parse array assignment");
    }
    
    @Test
    public void testComplexArrayUsage() {
        String source = "int test() { int matrix[9]; matrix[0] = 1; matrix[4] = 5; return matrix[0] + matrix[4]; }";
        assertTrue(canParse(source), "Should parse complex array usage");
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
}