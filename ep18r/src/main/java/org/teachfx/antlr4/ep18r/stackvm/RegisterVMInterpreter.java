package org.teachfx.antlr4.ep18r.stackvm;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.teachfx.antlr4.ep18r.parser.VMAssemblerLexer;
import org.teachfx.antlr4.ep18r.parser.VMAssemblerParser;

import java.io.InputStream;

/**
 * 寄存器虚拟机解释器
 * 执行寄存器字节码指令，使用寄存器文件而非操作数栈
 */
public class RegisterVMInterpreter {
    // 虚拟机配置
    private final VMConfig config;

    // 寄存器文件：16个寄存器，r0-r15
    private final int[] registers = new int[RegisterBytecodeDefinition.NUM_REGISTERS];

    // 内存和运行时数据结构
    private Object[] constPool;
    private byte[] code;
    private int codeSize;
    private Object[] globals;
    private final int[] heap; // 现在由配置控制
    private final int[] locals; // 现在由配置控制
    private int heapAllocPointer = 0;          // 堆分配指针
    private final StackFrame[] callStack; // 现在由配置控制
    private int framePointer = -1;
    private FunctionSymbol mainFunction;

    // 程序计数器和执行状态
    private int programCounter;
    private boolean running;
    private boolean trace = false;

    // 循环检测和安全机制
    private final int maxExecutionSteps; // 现在由配置控制
    private int executionSteps = 0;

    // 跳转标志：指示是否发生了跳转
    private boolean didJump = false;

    // 指令映射器（策略模式）
    private final InstructionMapper instructionMapper = new InstructionMapper();

    // 特殊用途寄存器别名
    private static final int SP = RegisterBytecodeDefinition.R13; // 栈指针
    private static final int FP = RegisterBytecodeDefinition.R14; // 帧指针
    private static final int LR = RegisterBytecodeDefinition.R15; // 链接寄存器

    /**
     * 使用默认配置创建虚拟机实例
     * @deprecated 使用 RegisterVMInterpreter(VMConfig) 替代
     */
    @Deprecated
    public RegisterVMInterpreter() {
        this(new VMConfig.Builder().build());
    }

    /**
     * 使用指定配置创建虚拟机实例
     * @param config 虚拟机配置
     */
    public RegisterVMInterpreter(VMConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("VMConfig cannot be null");
        }
        this.config = config;
        this.heap = new int[config.getHeapSize()];
        this.locals = new int[config.getLocalsSize()];
        this.callStack = new StackFrame[config.getMaxCallStackDepth()];
        this.maxExecutionSteps = config.getMaxExecutionSteps();
        this.programCounter = 0;
        this.running = false;
    }

    /**
     * 从输入流加载寄存器汇编代码
     */
    public static boolean load(RegisterVMInterpreter interp, InputStream input) throws Exception {
        boolean hasErrors = false;
        try (input) {
            VMAssemblerLexer assemblerLexer = new VMAssemblerLexer(CharStreams.fromStream(input));
            CommonTokenStream tokenStream = new CommonTokenStream(assemblerLexer);
            VMAssemblerParser parser = new VMAssemblerParser(tokenStream);
            ParseTree parseTree = parser.program();
            ParseTreeWalker walker = new ParseTreeWalker();
            RegisterByteCodeAssembler assembler = new RegisterByteCodeAssembler(RegisterBytecodeDefinition.instructions);
            walker.walk(assembler, parseTree);

            interp.code = assembler.getMachineCode();
            interp.codeSize = assembler.getCodeMemorySize();
            interp.constPool = assembler.getConstantPool();
            interp.mainFunction = assembler.getMainFunction();
            interp.globals = new Object[assembler.getDataSize()];

            hasErrors = parser.getNumberOfSyntaxErrors() > 0 || assembler.hasErrors();
        }
        return hasErrors;
    }

    /**
     * 执行加载的字节码
     */
    public void exec() throws Exception {
        if (mainFunction == null) {
            mainFunction = new FunctionSymbol("main", 0, 0, 0);
        }

        // 初始化寄存器：r0恒为0，其他寄存器初始化为0
        // FP (R14) 初始化为0，作为main函数的栈帧基址
        for (int i = 1; i < registers.length; i++) {
            registers[i] = 0;
        }

        // 设置初始栈帧（main函数）
        // frameBasePointer = 0，表示main函数的局部变量从heap[0]开始
        StackFrame frame = new StackFrame(mainFunction, -1, 0);
        callStack[++framePointer] = frame;
        programCounter = mainFunction.address;
        running = true;

        // 执行循环
        cpu();
    }

    /**
     * 主执行循环 - 解码并执行寄存器指令
     */
    private void cpu() throws Exception {
        executionSteps = 0; // 重置执行步数计数器
        
        while (running && programCounter < codeSize) {
            // 循环检测 - 防止无限循环
            if (executionSteps++ > maxExecutionSteps) {
                throw new RuntimeException("Maximum execution steps exceeded. Possible infinite loop detected at PC=" + programCounter);
            }

            // 提取32位固定长度指令
            if (programCounter < 0 || programCounter + 4 > codeSize) {
                throw new Exception("Instruction access out of bounds at PC=" + programCounter + ", codeSize=" + codeSize);
            }

            // 读取32位指令字（大端序）
            int instructionWord = ((code[programCounter] & 0xFF) << 24) |
                                  ((code[programCounter + 1] & 0xFF) << 16) |
                                  ((code[programCounter + 2] & 0xFF) << 8) |
                                  (code[programCounter + 3] & 0xFF);

            // 提取操作码（bits 31-26）
            int opcode = (instructionWord >> 26) & 0x3F;
            
            // 验证操作码范围
            if (opcode < 0 || opcode >= RegisterBytecodeDefinition.instructions.length) {
                throw new IllegalArgumentException("Invalid opcode: " + opcode + " at PC=" + programCounter);
            }
            
            // 整个指令字作为操作数传递给执行逻辑
            int operand = instructionWord;

            // 添加PC追踪调试输出（RET之后的关键指令）
            if (programCounter == 16 || programCounter == 20 || programCounter == 24) {
                System.out.printf("[CPU TRACE] 执行指令: PC=%d, opcode=%d, didJump=%b, 寄存器: a0=%d, a1=%d, a2=%d, s0=%d, s1=%d, s2=%d%n",
                    programCounter, opcode, didJump,
                    registers[2], registers[3], registers[4], registers[8], registers[9], registers[10]);
            }

            // 根据操作码执行指令
            executeInstruction(opcode, operand);

            // 更新程序计数器（每条指令4字节）
            // 注意：只有在没有跳转的情况下才自动增加PC
            // 跳转指令会在executeInstruction中直接设置PC
            if (!didJump) {
                programCounter += 4;
            } else if (programCounter == 16 || programCounter == 20 || programCounter == 24) {
                System.out.printf("[CPU TRACE] 跳转后PC=%d, didJump重置前=%b%n", programCounter, didJump);
            }
            didJump = false; // 重置跳转标志
        }
    }

    /**
     * 从操作数中提取寄存器编号（5位字段）
     * 假设操作数编码：rd在bits 21-25, rs1在bits 16-20, rs2在bits 11-15, 立即数在低16位
     */
    private int extractRd(int operand) {
        return (operand >> 21) & 0x1F;
    }

    private int extractRs1(int operand) {
        return (operand >> 16) & 0x1F;
    }

    private int extractRs2(int operand) {
        return (operand >> 11) & 0x1F;
    }

    private int extractImm16(int operand) {
        int imm = operand & 0xFFFF;
        // 符号扩展：如果最高位为1，则扩展为负数
        if ((imm & 0x8000) != 0) {
            imm |= 0xFFFF0000;
        }
        return imm;
    }

    private int extractImm26(int operand) {
        int imm = operand & 0x3FFFFFF;
        // 符号扩展：如果最高位为1，则扩展为负数
        if ((imm & 0x2000000) != 0) {
            imm |= 0xFC000000;
        }
        return imm;
    }

    /**
     * 执行单条指令（使用策略模式）
     *
     * 大部分指令通过InstructionMapper委托给相应的执行器处理，
     * 特殊指令（CALL, RET, HALT, J, JT, JF）由于需要访问VM内部状态（如codeSize），在此处直接处理。
     */
    private void executeInstruction(int opcode, int operand) throws Exception {
        // 特殊指令：需要访问VM调用栈、codeSize或控制running状态
        switch (opcode) {
            case RegisterBytecodeDefinition.INSTR_J: {
                // j target: 无条件跳转
                int target = extractImm26(operand);

                // 验证跳转目标
                if (target < 0 || target >= codeSize || target % 4 != 0) {
                    throw new IllegalArgumentException("Invalid jump target: " + target + " at PC=" + programCounter);
                }

                programCounter = target;
                didJump = true;
                return;
            }
            case RegisterBytecodeDefinition.INSTR_JT: {
                // jt rs1, target: 条件为真跳转
                int rs1 = extractRs1(operand);
                int target = extractImm16(operand);

                if (getRegister(rs1) != 0) {
                    // 验证跳转目标
                    if (target < 0 || target >= codeSize || target % 4 != 0) {
                        throw new IllegalArgumentException("Invalid conditional jump target: " + target + " at PC=" + programCounter);
                    }

                    programCounter = target;
                    didJump = true;
                }
                return;
            }
            case RegisterBytecodeDefinition.INSTR_JF: {
                // jf rs1, target: 条件为假跳转
                int rs1 = extractRs1(operand);
                int target = extractImm16(operand);

                if (getRegister(rs1) == 0) {
                    // 验证跳转目标
                    if (target < 0 || target >= codeSize || target % 4 != 0) {
                        throw new IllegalArgumentException("Invalid conditional jump target: " + target + " at PC=" + programCounter);
                    }

                    programCounter = target;
                    didJump = true;
                }
                return;
            }
            case RegisterBytecodeDefinition.INSTR_HALT:
                running = false;
                return;
            case RegisterBytecodeDefinition.INSTR_LF: {
                // lf rd, pool_index: 需要访问constPool
                int rd = extractRd(operand);
                int poolIndex = extractImm16(operand);
                if (poolIndex < 0 || poolIndex >= constPool.length) {
                    throw new IndexOutOfBoundsException("Constant pool index out of bounds: " + poolIndex);
                }
                Object constant = constPool[poolIndex];
                if (constant instanceof Float) {
                    setRegister(rd, Float.floatToIntBits((Float) constant));
                } else {
                    throw new ClassCastException("Expected Float constant at pool index " + poolIndex);
                }
                return;
            }
            case RegisterBytecodeDefinition.INSTR_LS: {
                // ls rd, pool_index: 需要访问constPool和heap分配
                int rd = extractRd(operand);
                int poolIndex = extractImm16(operand);
                if (poolIndex < 0 || poolIndex >= constPool.length) {
                    throw new IndexOutOfBoundsException("Constant pool index out of bounds: " + poolIndex);
                }
                Object constant = constPool[poolIndex];
                if (constant instanceof String) {
                    String str = (String) constant;
                    int address = heapAllocPointer;
                    if (address + str.length() > heap.length) {
                        throw new OutOfMemoryError("Not enough heap space for string");
                    }
                    for (int i = 0; i < str.length(); i++) {
                        heap[address + i] = str.charAt(i);
                    }
                    heapAllocPointer += str.length();
                    setRegister(rd, address);
                } else {
                    throw new ClassCastException("Expected String constant at pool index " + poolIndex);
                }
                return;
            }
        }

        // 使用策略模式执行其他指令
        ExecutionContext context = new ExecutionContext(this, programCounter);
        InstructionExecutor executor = instructionMapper.getExecutor(opcode);

        if (executor != null) {
            executor.execute(operand, context);
        } else {
            throw new UnsupportedOperationException("Unsupported opcode: " + opcode);
        }
    }

    /**
     * 反汇编单条指令用于跟踪
     */
    private void disassembleInstruction(int opcode, int operand) {
        // TODO: 实现反汇编
        System.out.println("[" + opcode + "]");
    }

    /**
     * 获取寄存器值
     */
    public int getRegister(int regNum) {
        if (regNum < 0 || regNum >= registers.length) {
            throw new IllegalArgumentException("Invalid register number: " + regNum);
        }
        return registers[regNum];
    }

    /**
     * 设置寄存器值
     */
    public void setRegister(int regNum, int value) {
        if (regNum < 0 || regNum >= registers.length) {
            throw new IllegalArgumentException("Invalid register number: " + regNum);
        }
        if (regNum == 0) {
            // r0是零寄存器，忽略写入
            return;
        }
        registers[regNum] = value;
    }

    /**
     * 启用/禁用跟踪模式
     */
    public void setTrace(boolean trace) {
        this.trace = trace;
    }

    /**
     * 获取虚拟机配置
     */
    public VMConfig getConfig() {
        return config;
    }

    // ==================== 堆和栈管理 ====================

    /**
     * 获取堆分配指针
     */
    int getHeapAllocPointer() {
        return heapAllocPointer;
    }

    /**
     * 设置堆分配指针
     */
    void setHeapAllocPointer(int pointer) {
        this.heapAllocPointer = pointer;
    }

    /**
     * 获取调用栈
     */
    StackFrame[] getCallStack() {
        return callStack;
    }

    /**
     * 获取帧指针
     */
    int getFramePointer() {
        return framePointer;
    }

    /**
     * 获取当前栈帧
     */
    StackFrame getCurrentFrame() {
        if (framePointer < 0) {
            return null;
        }
        return callStack[framePointer];
    }

    /**
     * 设置帧指针
     */
    void setFramePointer(int framePointer) {
        this.framePointer = framePointer;
    }

    /**
     * 获取代码大小
     */
    int getCodeSize() {
        return codeSize;
    }

    /**
     * 获取寄存器数组（仅供内部使用）
     */
    int[] getRegisters() {
        return registers;
    }

    /**
     * 设置跳转目标（内部使用）
     */
    void setJumpTarget(int target) {
        this.programCounter = target;
        this.didJump = true;
    }

    /**
     * 检查是否发生了跳转（内部使用）
     */
    boolean didJump() {
        return didJump;
    }

    /**
     * 读取堆内存（内部使用）
     */
    int readHeap(int address) {
        if (address < 0 || address >= heap.length) {
            throw new IndexOutOfBoundsException("Heap address out of bounds: " + address);
        }
        return heap[address];
    }

    /**
     * 写入堆内存（内部使用）
     */
    void writeHeap(int address, int value) {
        if (address < 0 || address >= heap.length) {
            throw new IndexOutOfBoundsException("Heap address out of bounds: " + address);
        }
        heap[address] = value;
    }

    /**
     * 读取内存（内部使用）
     */
    int readMemory(int address) {
        // 简化实现：使用堆作为内存
        return readHeap(address);
    }

    /**
     * 写入内存（内部使用）
     */
    void writeMemory(int address, int value) {
        // 简化实现：使用堆作为内存
        writeHeap(address, value);
    }

    /**
     * 获取堆大小
     */
    public int getHeapSize() {
        return heap.length;
    }

    /**
     * 获取局部变量数组大小
     */
    public int getLocalsSize() {
        return locals.length;
    }

    /**
     * 获取最大调用栈深度
     */
    public int getMaxCallStackDepth() {
        return callStack.length;
    }

    /**
     * 获取最大执行步数
     */
    public int getMaxExecutionSteps() {
        return maxExecutionSteps;
    }

    /**
     * 直接加载字节码（测试用）
     */
    public void loadCode(byte[] bytecode) {
        this.code = bytecode;
        this.codeSize = bytecode.length;
    }

    /**
     * 获取当前字节码（测试用）
     */
    public byte[] getCode() {
        return code;
    }

    /**
     * 获取常量池（用于查找函数符号）
     */
    Object[] getConstantPool() {
        return constPool;
    }

    /**
     * 根据代码地址查找函数符号
     * @param address 函数入口地址
     * @return 函数符号，如果找不到返回null
     */
    FunctionSymbol findFunctionByAddress(int address) {
        if (constPool == null) {
            return null;
        }
        for (Object obj : constPool) {
            if (obj instanceof FunctionSymbol) {
                FunctionSymbol func = (FunctionSymbol) obj;
                if (func.address == address) {
                    return func;
                }
            }
        }
        return null;
    }
}