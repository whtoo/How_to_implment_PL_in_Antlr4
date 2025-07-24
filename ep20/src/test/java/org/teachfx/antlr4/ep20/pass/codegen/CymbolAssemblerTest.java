package org.teachfx.antlr4.ep20.pass.codegen;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep20.ir.expr.CallFunc;
import org.teachfx.antlr4.ep20.ir.expr.val.ConstVal;
import org.teachfx.antlr4.ep20.ir.stmt.Assign;
import org.teachfx.antlr4.ep20.symtab.symbol.MethodSymbol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@DisplayName("Cymbol汇编器测试")
public class CymbolAssemblerTest {
    private CymbolAssembler cymbolAssembler;

    @BeforeEach
    public void setUp() {
        cymbolAssembler = new CymbolAssembler();
    }

    @Test
    @DisplayName("应正确生成常量加载指令")
    public void testEmitLoadConstantInstruction() {
        // Arrange
        ConstVal<Integer> intConst = new ConstVal<>(42);
        
        // Act
        cymbolAssembler.visit(intConst);
        
        // Assert
        assertEquals("iconst 42\n", cymbolAssembler.getAsmInfo());
    }
    
    @Test
    @DisplayName("应正确生成字符串常量加载指令")
    public void testEmitLoadStringConstantInstruction() {
        // Arrange
        ConstVal<String> stringConst = new ConstVal<>("hello");
        
        // Act
        cymbolAssembler.visit(stringConst);
        
        // Assert
        assertEquals("sconst \"hello\"\n", cymbolAssembler.getAsmInfo());
    }
    
    @Test
    @DisplayName("应正确生成布尔常量加载指令")
    public void testEmitLoadBooleanConstantInstruction() {
        // Arrange
        ConstVal<Boolean> trueConst = new ConstVal<>(true);
        ConstVal<Boolean> falseConst = new ConstVal<>(false);
        
        // Act
        cymbolAssembler.visit(trueConst);
        
        // Assert
        assertEquals("bconst 1\n", cymbolAssembler.getAsmInfo());
        
        // Reset assembler
        cymbolAssembler = new CymbolAssembler();
        
        // Act
        cymbolAssembler.visit(falseConst);
        
        // Assert
        assertEquals("bconst 0\n", cymbolAssembler.getAsmInfo());
    }

    @Test
    public void testEmitWhenCalledThenAssembleCmdBufferContainsCommand() {
        // Arrange
        String command = "iconst 5";

        // Act
        cymbolAssembler.emit(command);

        // Assert
        assertEquals( command + "\n", cymbolAssembler.getAsmInfo());
    }

    @Test
    public void testEmitWhenCalledMultipleTimesThenAssembleCmdBufferContainsCommands() {
        // Arrange
        String command1 = "iconst 5";
        String command2 = "istore 1";

        // Act
        cymbolAssembler.emit(command1);
        cymbolAssembler.emit(command2);

        // Assert
        assertEquals(command1 + "\n" + command2 + "\n", cymbolAssembler.getAsmInfo());
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
}
