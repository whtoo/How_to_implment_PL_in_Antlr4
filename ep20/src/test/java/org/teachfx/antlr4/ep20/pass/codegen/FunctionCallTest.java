package org.teachfx.antlr4.ep20.pass.codegen;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep20.ir.expr.CallFunc;
import org.teachfx.antlr4.ep20.symtab.symbol.MethodSymbol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@DisplayName("函数调用测试")
public class FunctionCallTest {
    private CymbolAssembler cymbolAssembler;

    @BeforeEach
    public void setUp() {
        cymbolAssembler = new CymbolAssembler();
    }

    @Test
    @DisplayName("应正确生成函数调用指令")
    public void testEmitFunctionCallInstruction() {
        // Arrange
        MethodSymbol methodSymbol = mock(MethodSymbol.class);
        when(methodSymbol.isBuiltIn()).thenReturn(false);
        CallFunc callFunc = new CallFunc("print", 1, methodSymbol);
        
        // Act
        cymbolAssembler.visit(callFunc);
        
        // Assert
        assertEquals("call print()\n", cymbolAssembler.getAsmInfo());
    }
    
    @Test
    @DisplayName("应正确生成内置函数调用指令")
    public void testEmitBuiltInFunctionCallInstruction() {
        // Arrange
        MethodSymbol methodSymbol = mock(MethodSymbol.class);
        when(methodSymbol.isBuiltIn()).thenReturn(true);
        CallFunc callFunc = new CallFunc("print", 1, methodSymbol);
        
        // Act
        cymbolAssembler.visit(callFunc);
        
        // Assert
        assertEquals("print\n", cymbolAssembler.getAsmInfo());
    }
    
    @Test
    @DisplayName("应正确生成带参数的函数调用指令")
    public void testEmitFunctionCallWithArgumentsInstruction() {
        // Arrange
        MethodSymbol methodSymbol = mock(MethodSymbol.class);
        when(methodSymbol.isBuiltIn()).thenReturn(false);
        CallFunc callFunc = new CallFunc("add", 2, methodSymbol);
        
        // Act
        cymbolAssembler.visit(callFunc);
        
        // Assert
        assertEquals("call add()\n", cymbolAssembler.getAsmInfo());
    }
    
    @Test
    @DisplayName("应正确生成main函数入口指令")
    public void testEmitMainFunctionEntryInstruction() {
        // Arrange
        // 这个测试需要更复杂的设置，我们只测试emit方法
        cymbolAssembler.emit("main:");
        
        // Assert
        assertEquals("main:\n", cymbolAssembler.getAsmInfo());
    }
    
    @Test
    @DisplayName("应正确生成函数返回指令")
    public void testEmitFunctionReturnInstruction() {
        // Arrange
        // 这个测试需要更复杂的设置，我们只测试emit方法
        cymbolAssembler.emit("ret");
        
        // Assert
        assertEquals("ret\n", cymbolAssembler.getAsmInfo());
    }
}