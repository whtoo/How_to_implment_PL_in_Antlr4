package org.teachfx.antlr4.ep18.stackvm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep18.stackvm.instructions.arithmetic.IAddInstruction;
import org.teachfx.antlr4.ep18.stackvm.VMMemoryException.MemoryAccessType;

import static org.assertj.core.api.Assertions.*;

/**
 * VMExceptionHandler 测试类
 */
class VMExceptionHandlerTest {

    private VMExceptionHandler exceptionHandler;
    private VMExecutionContext context;

    @BeforeEach
    void setUp() {
        exceptionHandler = new VMExceptionHandler();
        context = new VMExecutionContext();
        context.setExceptionHandler(exceptionHandler);
    }

    @Test
    @DisplayName("Should handle arithmetic overflow exception")
    void testHandleArithmeticOverflow() {
        // Given
        VMOverflowException overflowException = new VMOverflowException(
            "Integer overflow", 10, "iadd"
        );

        // When
        boolean handled = exceptionHandler.handleException(overflowException, context);

        // Then - 内置处理器应该处理这个异常
        assertThat(handled).isTrue();
    }

    @Test
    @DisplayName("Should not handle division by zero exception")
    void testHandleDivisionByZero() {
        // Given
        VMDivisionByZeroException divException = new VMDivisionByZeroException(15, "idiv");

        // When
        boolean handled = exceptionHandler.handleException(divException, context);

        // Then - 除零异常无法恢复，应该继续传播
        assertThat(handled).isFalse();
    }

    @Test
    @DisplayName("Should handle custom exception with custom handler")
    void testHandleCustomException() {
        // Given
        VMRuntimeException customException = new VMRuntimeException("Custom error", 20, "custom");
        
        // 注册自定义处理器
        exceptionHandler.registerHandler(VMRuntimeException.class, (ex, ctx) -> {
            // 自定义处理逻辑
            return true; // 表示已处理
        });

        // When
        boolean handled = exceptionHandler.handleException(customException, context);

        // Then
        assertThat(handled).isTrue();
    }

    @Test
    @DisplayName("Should use default handler for unregistered exception types")
    void testDefaultHandler() {
        // Given
        VMInstructionException instructionException = new VMInstructionException("Invalid instruction", 25, "invalid", 999);
        
        // 设置默认处理器
        exceptionHandler.setDefaultHandler((ex, ctx) -> {
            // 默认处理逻辑
            return ex instanceof VMInstructionException; // 只处理指令异常
        });

        // When
        boolean handled = exceptionHandler.handleException(instructionException, context);

        // Then
        assertThat(handled).isTrue();
    }

    @Test
    @DisplayName("Should disable and enable exception handling")
    void testEnableDisable() {
        // Given
        VMOverflowException overflowException = new VMOverflowException("Overflow", 30, "iadd");
        
        // When - 禁用处理器
        exceptionHandler.disable();
        boolean handledWhenDisabled = exceptionHandler.handleException(overflowException, context);

        // Then
        assertThat(handledWhenDisabled).isFalse();
        assertThat(exceptionHandler.isEnabled()).isFalse();

        // When - 重新启用处理器
        exceptionHandler.enable();
        boolean handledWhenEnabled = exceptionHandler.handleException(overflowException, context);

        // Then
        assertThat(handledWhenEnabled).isTrue();
        assertThat(exceptionHandler.isEnabled()).isTrue();
    }

    @Test
    @DisplayName("Should handle handler exceptions gracefully")
    void testHandlerExceptionHandling() {
        // Given
        VMRuntimeException testException = new VMRuntimeException("Test error", 35, "test");
        
        // 注册会抛出异常的处理器
        exceptionHandler.registerHandler(VMRuntimeException.class, (ex, ctx) -> {
            throw new RuntimeException("Handler error");
        });

        // When
        boolean handled = exceptionHandler.handleException(testException, context);

        // Then - 处理器异常应该被捕获，原始异常继续传播
        assertThat(handled).isFalse();
    }

    @Test
    @DisplayName("Should find most specific handler")
    void testHandlerInheritance() {
        // Given
        VMMemoryAccessException memoryException = new VMMemoryAccessException(40, "load", 0x1000, MemoryAccessType.READ);
        
        // 注册父类处理器
        exceptionHandler.registerHandler(VMMemoryException.class, (ex, ctx) -> {
            // 父类处理逻辑
            return true;
        });

        // When
        boolean handled = exceptionHandler.handleException(memoryException, context);

        // Then - 应该使用父类处理器
        assertThat(handled).isTrue();
    }

    @Test
    @DisplayName("Should clear custom handlers")
    void testClearCustomHandlers() {
        // Given
        VMRuntimeException testException = new VMRuntimeException("Test error", 45, "test");
        
        // 注册自定义处理器
        exceptionHandler.registerHandler(VMRuntimeException.class, (ex, ctx) -> true);
        assertThat(exceptionHandler.getHandlerCount()).isGreaterThan(0);

        // When
        exceptionHandler.clearCustomHandlers();

        // Then - 应该只保留内置处理器
        assertThat(exceptionHandler.getHandlerCount()).isEqualTo(countBuiltinHandlers());
    }

    private int countBuiltinHandlers() {
        // 内置处理器数量：溢出、除零、栈下溢、内存访问
        return 4;
    }
}