package org.teachfx.antlr4.ep18.stackvm;

import java.util.Arrays;

/**
 * CymbolStackVM - 基于栈的虚拟机实现
 * 执行字节码指令，管理堆栈和内存
 */
public class CymbolStackVM {
    // 虚拟机配置
    private final VMConfig config;
    private final VMStats stats;   // 性能统计

    // 运行时数据结构
    private int[] stack;           // 操作数栈
    private int stackPointer;      // 栈指针
    private int[] heap;            // 堆内存
    private int[] instructionCache; // 指令缓存

    // 执行状态
    private boolean running;
    private int programCounter;    // 程序计数器
    
    /**
     * 构造函数 - 使用配置创建虚拟机实例
     * @param config 虚拟机配置
     */
    public CymbolStackVM(VMConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("VMConfig cannot be null");
        }
        this.config = config;
        this.stats = new VMStats();
        initializeVM();
    }
    
    /**
     * 初始化虚拟机运行时环境
     */
    private void initializeVM() {
        // 初始化堆内存
        this.heap = new int[config.getHeapSize()];
        
        // 初始化操作数栈
        this.stack = new int[config.getStackSize()];
        this.stackPointer = 0;
        
        // 初始化指令缓存
        this.instructionCache = new int[config.getInstructionCacheSize()];
        
        // 初始化执行状态
        this.running = false;
        this.programCounter = 0;
        
        // 如果是调试模式，输出初始化信息
        if (config.isDebugMode()) {
            System.out.println("VM initialized with config: " + config);
        }
    }
    
    /**
     * 执行字节码
     * @param bytecode 字节码数组
     * @return 执行结果
     * @throws Exception 执行异常
     */
    public int execute(byte[] bytecode) throws Exception {
        if (bytecode == null || bytecode.length == 0) {
            throw new IllegalArgumentException("Bytecode cannot be null or empty");
        }

        long startTime = System.nanoTime();
        long startMemory = getUsedMemory();

        // 加载字节码到指令缓存
        loadBytecode(bytecode);

        // 开始执行
        this.running = true;
        this.programCounter = 0;

        try {
            while (running && programCounter < instructionCache.length) {
                // 获取当前指令
                int instruction = instructionCache[programCounter++];

                // 执行指令
                executeInstruction(instruction);
            }

            // 返回栈顶值作为结果
            int result = stackPointer > 0 ? stack[stackPointer - 1] : 0;

            // 记录性能统计
            long endTime = System.nanoTime();
            long endMemory = getUsedMemory();
            stats.recordExecution(startTime);
            stats.recordMemoryUsage(endMemory);

            if (config.isDebugMode()) {
                System.out.println("Execution completed successfully");
            }

            return result;
        } catch (Exception e) {
            // 记录错误统计
            stats.recordError(e);

            if (config.isVerboseErrors()) {
                System.err.println("VM execution error at PC=" + (programCounter - 1) + ": " + e.getMessage());
            }
            throw e;
        } finally {
            this.running = false;
        }
    }
    
    /**
     * 加载字节码到指令缓存
     * @param bytecode 字节码数组
     */
    private void loadBytecode(byte[] bytecode) {
        // 将字节数组转换为整数数组（4字节一个指令）
        int instructionCount = Math.min(bytecode.length / 4, instructionCache.length);
        for (int i = 0; i < instructionCount; i++) {
            int offset = i * 4;
            instructionCache[i] = ((bytecode[offset] & 0xFF) << 24) |
                                   ((bytecode[offset + 1] & 0xFF) << 16) |
                                   ((bytecode[offset + 2] & 0xFF) << 8) |
                                   (bytecode[offset + 3] & 0xFF);
        }
        
        if (config.isDebugMode()) {
            System.out.println("Loaded " + instructionCount + " instructions");
        }
    }
    
    /**
     * 执行单条指令
     * @param instruction 指令
     * @throws Exception 执行异常
     */
    private void executeInstruction(int instruction) throws Exception {
        // 提取操作码（高8位）
        int opcode = (instruction >> 24) & 0xFF;

        if (config.isTraceEnabled()) {
            System.out.println("Executing instruction at PC=" + (programCounter - 1) +
                             ": opcode=0x" + Integer.toHexString(opcode) +
                             ", instruction=0x" + Integer.toHexString(instruction));
        }

        // 根据操作码执行指令
        switch (opcode) {
            case BytecodeDefinition.INSTR_IADD:
                executeIAdd();
                break;
            case BytecodeDefinition.INSTR_ISUB:
                executeISub();
                break;
            case BytecodeDefinition.INSTR_IMUL:
                executeIMul();
                break;
            case BytecodeDefinition.INSTR_IDIV:
                executeIDiv();
                break;
            case BytecodeDefinition.INSTR_ILT:
                executeILt();
                break;
            case BytecodeDefinition.INSTR_ILE:
                executeILe();
                break;
            case BytecodeDefinition.INSTR_IGT:
                executeIGt();
                break;
            case BytecodeDefinition.INSTR_IGE:
                executeIGe();
                break;
            case BytecodeDefinition.INSTR_IEQ:
                executeIEq();
                break;
            case BytecodeDefinition.INSTR_INE:
                executeINe();
                break;
            case BytecodeDefinition.INSTR_INEG:
                executeINeg();
                break;
            case BytecodeDefinition.INSTR_INOT:
                executeINot();
                break;
            case BytecodeDefinition.INSTR_IAND:
                executeIAnd();
                break;
            case BytecodeDefinition.INSTR_IOR:
                executeIOr();
                break;
            case BytecodeDefinition.INSTR_IXOR:
                executeIXor();
                break;
            case BytecodeDefinition.INSTR_ICONST:
                executeIConst(instruction);
                break;
            case BytecodeDefinition.INSTR_HALT:
                executeHalt();
                break;
            default:
                throw new UnsupportedOperationException(
                    "Unsupported opcode: 0x" + Integer.toHexString(opcode) +
                    " at PC=" + (programCounter - 1));
        }
    }

    // 指令执行方法实现

    private void executeIAdd() {
        int b = pop();
        int a = pop();
        push(a + b);
    }

    private void executeISub() {
        int b = pop();
        int a = pop();
        push(a - b);
    }

    private void executeIMul() {
        int b = pop();
        int a = pop();
        push(a * b);
    }

    private void executeIDiv() {
        int b = pop();
        int a = pop();
        if (b == 0) {
            throw new VMDivisionByZeroException(programCounter - 1, "IDIV");
        }
        push(a / b);
    }

    private void executeILt() {
        int b = pop();
        int a = pop();
        push(a < b ? 1 : 0);
    }

    private void executeILe() {
        int b = pop();
        int a = pop();
        push(a <= b ? 1 : 0);
    }

    private void executeIGt() {
        int b = pop();
        int a = pop();
        push(a > b ? 1 : 0);
    }

    private void executeIGe() {
        int b = pop();
        int a = pop();
        push(a >= b ? 1 : 0);
    }

    private void executeIEq() {
        int b = pop();
        int a = pop();
        push(a == b ? 1 : 0);
    }

    private void executeINe() {
        int b = pop();
        int a = pop();
        push(a != b ? 1 : 0);
    }

    private void executeINeg() {
        int a = pop();
        push(-a);
    }

    private void executeINot() {
        int a = pop();
        push(a == 0 ? 1 : 0);
    }

    private void executeIAnd() {
        int b = pop();
        int a = pop();
        push(a & b);
    }

    private void executeIOr() {
        int b = pop();
        int a = pop();
        push(a | b);
    }

    private void executeIXor() {
        int b = pop();
        int a = pop();
        push(a ^ b);
    }

    private void executeIConst(int instruction) {
        // 提取常量值（低24位）
        int value = instruction & 0xFFFFFF;
        // 处理符号扩展（如果最高位是1，则为负数）
        if ((value & 0x800000) != 0) {
            value |= 0xFF000000; // 符号扩展
        }
        push(value);
    }

    private void executeHalt() {
        this.running = false;
    }
    
    /**
     * 将值压入栈
     * @param value 值
     */
    protected void push(int value) {
        if (stackPointer >= stack.length) {
            throw new StackOverflowError("Stack overflow");
        }
        stack[stackPointer++] = value;
    }
    
    /**
     * 从栈弹出值
     * @return 栈顶值
     */
    protected int pop() {
        if (stackPointer <= 0) {
            throw new IllegalStateException("Stack underflow");
        }
        return stack[--stackPointer];
    }
    
    /**
     * 查看栈顶值（不弹出）
     * @return 栈顶值
     */
    protected int peek() {
        if (stackPointer <= 0) {
            throw new IllegalStateException("Stack is empty");
        }
        return stack[stackPointer - 1];
    }
    
    
    /**
     * 检查虚拟机是否正在运行
     * @return 是否运行中
     */
    public boolean isRunning() {
        return running;
    }
    
    /**
     * 停止虚拟机执行
     */
    public void stop() {
        this.running = false;
    }

    /**
     * 获取虚拟机统计信息
     * @return 统计信息
     */
    public VMStats getStats() {
        return stats;
    }

    /**
     * 获取已使用的内存量
     * @return 已使用的内存（字节）
     */
    private long getUsedMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    /**
     * 获取栈深度
     * @return 当前栈深度
     */
    public int getStackDepth() {
        return stackPointer;
    }

    /**
     * 检查栈是否为空
     * @return 是否为空
     */
    public boolean isStackEmpty() {
        return stackPointer <= 0;
    }

    /**
     * 获取配置
     * @return 配置对象
     */
    public VMConfig getConfig() {
        return config;
    }
}
