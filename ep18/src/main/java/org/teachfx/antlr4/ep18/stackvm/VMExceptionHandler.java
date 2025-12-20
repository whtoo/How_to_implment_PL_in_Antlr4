package org.teachfx.antlr4.ep18.stackvm;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

/**
 * VMExceptionHandler - 虚拟机异常处理器
 * 提供统一的异常处理机制，支持异常恢复、日志记录和自定义处理策略
 */
public class VMExceptionHandler {
    private final Map<Class<? extends VMException>, BiFunction<VMException, VMExecutionContext, Boolean>> customHandlers;
    private final Map<Class<? extends VMException>, BiFunction<VMException, VMExecutionContext, Boolean>> builtinHandlers;
    private BiFunction<VMException, VMExecutionContext, Boolean> defaultHandler;
    private boolean enabled;

    public VMExceptionHandler() {
        this.customHandlers = new ConcurrentHashMap<>();
        this.builtinHandlers = new ConcurrentHashMap<>();
        this.enabled = true;

        // 设置默认处理器 - 默认不处理异常，让其传播
        this.defaultHandler = (exception, context) -> false;

        // 注册内置处理器
        registerBuiltinHandlers();
    }

    /**
     * 注册异常处理器
     * @param exceptionType 异常类型
     * @param handler 处理器函数，返回true表示异常已处理，false表示需要继续传播
     */
    public void registerHandler(Class<? extends VMException> exceptionType,
                               BiFunction<VMException, VMExecutionContext, Boolean> handler) {
        customHandlers.put(exceptionType, handler);
    }

    /**
     * 注册默认异常处理器
     */
    public void setDefaultHandler(BiFunction<VMException, VMExecutionContext, Boolean> handler) {
        this.defaultHandler = handler;
    }

    /**
     * 处理异常
     * @param exception 要处理的异常
     * @param context 执行上下文
     * @return true如果异常被成功处理，false如果需要继续传播
     */
    public boolean handleException(VMException exception, VMExecutionContext context) {
        if (!enabled) {
            return false; // 如果处理器被禁用，直接传播异常
        }

        try {
            // 查找最具体的处理器
            BiFunction<VMException, VMExecutionContext, Boolean> handler = findHandler(exception.getClass());
            
            if (handler != null) {
                return handler.apply(exception, context);
            }
            
            // 使用默认处理器
            return defaultHandler.apply(exception, context);
        } catch (Exception e) {
            // 异常处理器本身出错，记录日志并继续传播原始异常
            System.err.println("Exception handler failed for " + exception.getClass().getSimpleName() + 
                             ": " + e.getMessage());
            e.printStackTrace();
            return false; // 让原始异常继续传播
        }
    }

    /**
     * 查找最适合的异常处理器
     * 优先使用自定义处理器，如果没有找到则使用内置处理器
     */
    private BiFunction<VMException, VMExecutionContext, Boolean> findHandler(Class<? extends VMException> exceptionType) {
        // 首先查找自定义处理器（精确匹配）
        BiFunction<VMException, VMExecutionContext, Boolean> handler = customHandlers.get(exceptionType);
        if (handler != null) {
            return handler;
        }

        // 查找自定义处理器（父类匹配）
        for (Map.Entry<Class<? extends VMException>, BiFunction<VMException, VMExecutionContext, Boolean>> entry : customHandlers.entrySet()) {
            if (entry.getKey().isAssignableFrom(exceptionType)) {
                return entry.getValue();
            }
        }

        // 如果没有找到自定义处理器，查找内置处理器（精确匹配）
        handler = builtinHandlers.get(exceptionType);
        if (handler != null) {
            return handler;
        }

        // 查找内置处理器（父类匹配）
        for (Map.Entry<Class<? extends VMException>, BiFunction<VMException, VMExecutionContext, Boolean>> entry : builtinHandlers.entrySet()) {
            if (entry.getKey().isAssignableFrom(exceptionType)) {
                return entry.getValue();
            }
        }

        return null;
    }

    /**
     * 启用异常处理
     */
    public void enable() {
        this.enabled = true;
    }

    /**
     * 禁用异常处理
     */
    public void disable() {
        this.enabled = false;
    }

    /**
     * 检查是否启用
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 注册内置异常处理器
     */
    private void registerBuiltinHandlers() {
        // 算术溢出处理器 - 尝试恢复为默认值
        builtinHandlers.put(VMOverflowException.class, (exception, context) -> {
            System.err.println("Arithmetic overflow detected at PC=" + exception.getPC() +
                             ", instruction=" + exception.getInstruction());

            // 尝试恢复为默认值
            // 先弹出操作数（如果存在）
            int stackDepth = context.getStackDepth();
            if (stackDepth >= 2) {
                context.pop(); // 移除操作数2
                context.pop(); // 移除操作数1
                context.push(0); // 用默认值替代
            } else if (stackDepth == 1) {
                context.pop(); // 移除操作数
                context.push(0); // 用默认值替代
            }
            // 如果栈为空，不需要做任何操作

            return true; // 异常已处理
        });

        // 除零异常处理器 - 提供有意义的错误信息
        builtinHandlers.put(VMDivisionByZeroException.class, (exception, context) -> {
            System.err.println("Division by zero detected at PC=" + exception.getPC() +
                             ", instruction=" + exception.getInstruction());

            // 除零异常通常是致命的，无法恢复
            return false;
        });

        // 栈下溢处理器 - 尝试恢复栈状态
        builtinHandlers.put(VMStackUnderflowException.class, (exception, context) -> {
            System.err.println("Stack underflow detected at PC=" + exception.getPC() +
                             ", instruction=" + exception.getInstruction());

            // 栈下溢通常是致命的，无法恢复
            return false;
        });

        // 内存访问异常处理器
        builtinHandlers.put(VMMemoryAccessException.class, (exception, context) -> {
            if (exception instanceof VMMemoryException) {
                VMMemoryException memEx = (VMMemoryException) exception;
                if (memEx.getAddress() == 0) {
                    System.err.println("Null pointer access detected at PC=" + memEx.getPC());
                } else {
                    System.err.println("Invalid memory access at address 0x" +
                        Long.toHexString(memEx.getAddress()) + " at PC=" + memEx.getPC());
                }
            } else {
                System.err.println("Memory access exception at PC=" + exception.getPC());
            }

            // 内存访问异常通常是致命的
            return false;
        });
    }

    /**
     * 获取已注册的处理器数量
     */
    public int getHandlerCount() {
        return customHandlers.size() + builtinHandlers.size();
    }

    /**
     * 清除所有自定义处理器，只保留内置处理器
     */
    public void clearCustomHandlers() {
        customHandlers.clear();
    }
}