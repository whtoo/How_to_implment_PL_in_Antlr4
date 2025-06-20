package org.teachfx.antlr4.ep19;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class StructAndTypedefTest {

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

    // 嵌套结构体测试
    @Test
    void testNestedStructDeclaration() {
        String code = "struct Inner { int value; } struct Outer { Inner inner; }";
        assertCompilesNoError(code);
    }

    @Test
    void testNestedStructFieldAccess() {
        String code = "struct Inner { int value; } struct Outer { Inner inner; } Outer o; o.inner.value = 10;";
        assertCompilesNoError(code);
    }

    @Test
    void testMultipleLevelNestedStruct() {
        String code = "struct Level3 { int data; } struct Level2 { Level3 l3; } struct Level1 { Level2 l2; } Level1 l1; l1.l2.l3.data = 42;";
        assertCompilesNoError(code);
    }

    @Test
    void testNestedStructWithTypedef() {
        String code = "struct Inner { int value; } typedef Inner MyInner; struct Outer { MyInner inner; } Outer o; o.inner.value = 10;";
        assertCompilesNoError(code);
    }

    @Test
    void testNestedStructMethodCall() {
        String code = "struct Inner { int value; int getValue() { return value; } } struct Outer { Inner inner; } Outer o; int x; x = (o.inner).getValue();";
        assertCompilesNoError(code);
    }

    @Test
    void testNestedStructUndefinedFieldError() {
        String code = "struct Inner { int value; } struct Outer { Inner inner; } Outer o; o.inner.nonexistent = 10;";
        assertCompilationError(code, "没有名为 nonexistent 的成员");
    }

    @Test
    void testNestedStructTypeMismatchError() {
        String code = "struct Inner { int value; } struct Outer { Inner inner; } Outer o; float f; f = 1.0; o.inner.value = f;";
        assertCompilationError(code, "类型不兼容");
    }

    @Test
    void testValidStructDeclaration() {
        String code = "struct Point { int x; float y; }";
        assertCompilesNoError(code);
    }

    @Test
    void testValidStructInstanceAndFieldAccess() {
        String code = "struct Point { int x; float y; } Point p; p.x = 1; p.y = 1.0;";
        assertCompilesNoError(code);
    }

    @Test
    void testStructFieldAccessTypeError() {
        String code = "struct Point { int x; } Point p; float f; f=1.0; p.x = f;";
        assertCompilationError(code, "类型不兼容: 不能将 float 类型赋值给 int 类型");
    }

    @Test
    void testStructFieldAccessUndefinedFieldError() {
        String code = "struct Point { int x; } Point p; p.z = 1;";
        // This error is from TypeChecker.checkStructFieldAccess
        assertCompilationError(code, "没有名为 z 的字段");
    }

    @Test
    void testAccessFieldOnNonStructTypeError() {
        String code = "int i; i.x = 10;";
        // Error from TypeChecker.checkStructFieldAccess when structType is not StructSymbol
        assertCompilationError(code, "不是结构体类型");
    }

    @Test
    void testValidTypedef() {
        String code = "typedef int MyInt; MyInt i; i = 10;";
        assertCompilesNoError(code);
    }

    @Test
    void testTypedefForStruct() {
        String code = "struct Point { int x; } typedef Point MyPoint; MyPoint p; p.x = 1;";
        assertCompilesNoError(code);
    }

    @Test
    void testTypedefChaining() {
        String code = "typedef int MyInt1; typedef MyInt1 MyInt2; MyInt2 i; i = 5;";
        assertCompilesNoError(code);
    }

    @Test
    void testUndefinedTypeInTypedefError() {
        String code = "typedef UnknownType MyType;";
        // Error comes from LocalResolver during lookup for UnknownType
        assertCompilationError(code, "未知的类型: UnknownType");
    }

    @Test
    void testStructWithMethodCall() {
        String code = "struct Calc { int val; int getVal() { return val; } } Calc c; c.val=1; int x; x=c.getVal();";
        assertCompilesNoError(code);
    }

    @Test
    void testStructMethodCallWrongArgCount() {
        String code = "struct Calc { int getVal(int x) { return x; } } Calc c; int x; x=c.getVal();"; // missing arg
        assertCompilationError(code, "参数数量不匹配");
    }

    @Test
    void testStructMethodCallWrongArgType() {
        String code = "struct Calc { int getVal(int x) { return x; } } Calc c; float f; f=1.0; int x; x=c.getVal(f);";
        assertCompilationError(code, "参数类型不匹配");
    }

    @Test
    void testStructMethodCallUndefinedMethod() {
        String code = "struct Calc { int val; } Calc c; c.undefinedMethod();";
        assertCompilationError(code, "没有名为 undefinedMethod 的方法");
    }

    @Test
    void testStructWithMethodReturningStruct() {
        String code = "struct Coord { int x; } struct Factory { Coord makeCoord() { Coord c; c.x=1; return c;}} Factory f; Coord res; res = f.makeCoord();";
        assertCompilesNoError(code);
    }

    @Test
    void testStructWithMethodAcceptingStruct() {
        String code = "struct Coord { int x; } struct Printer { void printCoord(Coord c) { int val; val = c.x; } } Printer p; Coord c1; c1.x=10; p.printCoord(c1);";
        assertCompilesNoError(code);
    }
} 
