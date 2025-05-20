package org.teachfx.antlr4.ep19;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

public class TypeSystemTest {

    private void assertCompilesNoError(String code) {
        CompilerTestUtil.CompilationResult result = CompilerTestUtil.compile(code);
        assertTrue(result.success, "Code should compile without errors. Errors: " + String.join("\n", result.errors) + "\nCode:\n" + code);
        assertTrue(result.errors.isEmpty(), "Error list should be empty. Errors: " + String.join("\n", result.errors) + "\nCode:\n" + code);
    }

    private void assertCompilationError(String code, String expectedErrorMessageSubstring) {
        CompilerTestUtil.CompilationResult result = CompilerTestUtil.compile(code);
        assertFalse(result.success, "Code should fail compilation for: \n" + code);
        assertFalse(result.errors.isEmpty(), "Error list should not be empty for: \n" + code);
        assertTrue(result.errors.stream().anyMatch(err -> err.contains(expectedErrorMessageSubstring)), 
            "Expected error containing '" + expectedErrorMessageSubstring + "'. Got: " + String.join("\n", result.errors) + "\nCode:\n" + code);
    }

    @Test
    void testValidIntAssignment() {
        String code = "int a; a = 5;";
        assertCompilesNoError(code);
    }

    @Test
    void testValidFloatAssignment() {
        String code = "float a; a = 5.0;";
        assertCompilesNoError(code);
    }

    @Test
    void testValidIntToFloatAssignment() {
        String code = "int a; float b; a = 5; b = a;";
        assertCompilesNoError(code);
    }

    @Test
    void testInvalidFloatToIntAssignment() {
        String code = "int a; float b; b = 5.0; a = b;";
        assertCompilationError(code, "类型不兼容: 不能将 float 类型赋值给 int 类型");
    }

    @Test
    void testValidBoolAssignment() {
        String code = "bool b; b = true;";
        assertCompilesNoError(code);
    }

    @Test
    void testValidStringAssignment() {
        String code = "String s; s = \"hello\";"; // Note: String literal escaping for Java
        assertCompilesNoError(code);
    }

    @Test
    void testIntAddition() {
        String code = "int a; int b; int c; a=1;b=2;c=a+b;";
        assertCompilesNoError(code);
    }

    @Test
    void testFloatAndIntAddition() {
        // Result should be float, assignment to float is OK
        String code = "int a; float b; float c; a=1;b=2.0;c=a+b;"; 
        assertCompilesNoError(code);
    }
    
    @Test
    void testFloatAndIntAdditionToIntError() {
        String code = "int a; float b; int c; a=1;b=2.0;c=a+b;"; 
        assertCompilationError(code, "类型不兼容: 不能将 float 类型赋值给 int 类型");
    }

    @Test
    void testStringConcatenation() {
        String code = "String s1; String s2; String s3; s1=\"a\"; s2=\"b\"; s3=s1+s2;";
        assertCompilesNoError(code);
    }

    @Test
    void testInvalidAdditionIntString() {
        String code = "int a; String s; int res; a=1;s=\"a\"; res = a+s;";
        // TypeChecker currently allows string + any -> string. Assignment to int is the error.
        assertCompilationError(code, "类型不兼容: 不能将 String 类型赋值给 int 类型");
    }

    @Test
    void testValidComparison() {
        String code = "int a; int b; bool c; a=1;b=2;c=(a<b);";
        assertCompilesNoError(code);
    }

    @Test
    void testIfConditionBool() {
        String code = "bool b; b=true; if (b) { int x; x=1; }";
        assertCompilesNoError(code);
    }

    @Test
    void testIfConditionNotBoolError() {
        String code = "int i; i=0; if (i) { int x; x=1; }";
        assertCompilationError(code, "if条件表达式必须是布尔类型");
    }

    @Test
    void testWhileConditionBool() {
        String code = "bool b; b=true; while (b) { b=false; }";
        assertCompilesNoError(code);
    }

    @Test
    void testWhileConditionNotBoolError() {
        String code = "int i; i=0; while (i) { i=1; }";
        assertCompilationError(code, "while条件表达式必须是布尔类型");
    }
    
    @Test
    void testReturnVoidInVoidFunction() {
        String code = "void f() { return; }";
        assertCompilesNoError(code);
    }

    @Test
    void testReturnNonVoidInVoidFunctionError() {
        String code = "void f() { return 1; }";
        assertCompilationError(code, "void函数不应返回值");
    }

    @Test
    void testReturnValueInIntFunction() {
        String code = "int f() { return 1; }";
        assertCompilesNoError(code);
    }

    @Test
    void testReturnVoidInIntFunctionError() {
        String code = "int f() { return; }";
        assertCompilationError(code, "函数应返回 int 类型的值");
    }
    
    @Test
    void testReturnWrongTypeInIntFunctionError() {
        String code = "int f() { float x; x=1.0; return x; }";
        assertCompilationError(code, "返回值类型 float 与函数返回类型 int 不兼容");
    }
} 