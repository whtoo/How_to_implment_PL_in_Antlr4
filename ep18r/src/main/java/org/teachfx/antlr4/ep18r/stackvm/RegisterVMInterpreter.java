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
        for (int i = 1; i < registers.length; i++) {
            registers[i] = 0;
        }

        // 设置初始栈帧
        StackFrame frame = new StackFrame(mainFunction, -1);
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

            // 根据操作码执行指令
            executeInstruction(opcode, operand);

            // 更新程序计数器（每条指令4字节）
            // 注意：只有在没有跳转的情况下才自动增加PC
            // 跳转指令会在executeInstruction中直接设置PC
            if (!didJump) {
                programCounter += 4;
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
     * 执行单条指令
     */
    private void executeInstruction(int opcode, int operand) throws Exception {
        // TODO: 实现寄存器指令集执行逻辑
        // 基于RegisterBytecodeDefinition中的指令定义
        // 需要处理R类型、I类型、J类型指令格式
        switch (opcode) {
            case RegisterBytecodeDefinition.INSTR_ADD: {
                // add rd, rs1, rs2
                int rd = extractRd(operand);
                int rs1 = extractRs1(operand);
                int rs2 = extractRs2(operand);
                int val1 = getRegister(rs1);
                int val2 = getRegister(rs2);
                setRegister(rd, val1 + val2);
                break;
            }
            case RegisterBytecodeDefinition.INSTR_SUB: {
                int rd = extractRd(operand);
                int rs1 = extractRs1(operand);
                int rs2 = extractRs2(operand);
                setRegister(rd, getRegister(rs1) - getRegister(rs2));
                break;
            }
            case RegisterBytecodeDefinition.INSTR_MUL: {
                int rd = extractRd(operand);
                int rs1 = extractRs1(operand);
                int rs2 = extractRs2(operand);
                setRegister(rd, getRegister(rs1) * getRegister(rs2));
                break;
            }
            case RegisterBytecodeDefinition.INSTR_DIV: {
                int rd = extractRd(operand);
                int rs1 = extractRs1(operand);
                int rs2 = extractRs2(operand);
                int divisor = getRegister(rs2);
                if (divisor == 0) {
                    throw new ArithmeticException("Division by zero");
                }
                setRegister(rd, getRegister(rs1) / divisor);
                break;
            }
            case RegisterBytecodeDefinition.INSTR_LI: {
                // li rd, immediate
                int rd = extractRd(operand);
                int imm = extractImm16(operand);
                setRegister(rd, imm);
                break;
            }
            case RegisterBytecodeDefinition.INSTR_CALL: {
                // call target: 保存返回地址到调用栈，跳转到目标地址
                int target = extractImm26(operand);

                // 验证跳转目标
                if (target < 0 || target >= codeSize || target % 4 != 0) {
                    throw new IllegalArgumentException("Invalid call target: " + target + " at PC=" + programCounter);
                }

                // 保存返回地址到调用栈（而不是只保存到r15）
                int returnAddr = programCounter + 4;
                if (framePointer + 1 >= callStack.length) {
                    throw new StackOverflowError("Call stack overflow");
                }
                StackFrame newFrame = new StackFrame(null, returnAddr);
                callStack[++framePointer] = newFrame;

                // 同时保存到 LR(r15) 以保持兼容性
                setRegister(RegisterBytecodeDefinition.R15, returnAddr);

                // 跳转
                programCounter = target;
                didJump = true;
                break;
            }
            case RegisterBytecodeDefinition.INSTR_RET: {
                // ret: 从调用栈恢复返回地址
                if (framePointer < 0) {
                    // 没有调用栈帧，尝试使用 r15（兼容旧代码）
                    int returnAddr = getRegister(RegisterBytecodeDefinition.R15);
                    if (returnAddr < 0 || returnAddr >= codeSize || returnAddr % 4 != 0) {
                        throw new IllegalArgumentException("Invalid return address: " + returnAddr + " at PC=" + programCounter);
                    }
                    programCounter = returnAddr;
                } else {
                    // 从调用栈弹出返回地址
                    StackFrame frame = callStack[framePointer--];
                    int returnAddr = frame.returnAddress;

                    if (returnAddr < 0 || returnAddr >= codeSize || returnAddr % 4 != 0) {
                        throw new IllegalArgumentException("Invalid return address from stack: " + returnAddr + " at PC=" + programCounter);
                    }

                    programCounter = returnAddr;
                }
                didJump = true;
                break;
            }
            case RegisterBytecodeDefinition.INSTR_J: {
                // j target: 无条件跳转
                int target = extractImm26(operand);
                
                // 验证跳转目标
                if (target < 0 || target >= codeSize || target % 4 != 0) {
                    throw new IllegalArgumentException("Invalid jump target: " + target + " at PC=" + programCounter);
                }
                
                programCounter = target;
                didJump = true;
                break;
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
                break;
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
                break;
            }
            case RegisterBytecodeDefinition.INSTR_PRINT: {
                // print rs: 打印寄存器值
                int rs = extractRs1(operand);
                System.out.println(getRegister(rs));
                break;
            }
            case RegisterBytecodeDefinition.INSTR_LW: {
                // lw rd, base, offset: 从内存加载字
                int rd = extractRd(operand);
                int base = extractRs1(operand);
                int offset = extractImm16(operand);
                int address = getRegister(base) + offset;
                if (address < 0 || address >= heap.length) {
                    throw new IndexOutOfBoundsException("Memory address out of bounds: " + address);
                }
                setRegister(rd, heap[address]);
                break;
            }
            case RegisterBytecodeDefinition.INSTR_SW: {
                // sw rs, base, offset: 存储字到内存
                int rs = extractRd(operand); // rs 在 rd 字段位置
                int base = extractRs1(operand);
                int offset = extractImm16(operand);
                int address = getRegister(base) + offset;
                if (address < 0 || address >= heap.length) {
                    throw new IndexOutOfBoundsException("Memory address out of bounds: " + address);
                }
                heap[address] = getRegister(rs);
                break;
            }
            case RegisterBytecodeDefinition.INSTR_LW_G: {
                // lw_g rd, offset: 全局加载，假设使用固定的全局基址（如寄存器0?）
                // 简化：假设offset直接作为heap数组索引
                int rd = extractRd(operand);
                int offset = extractImm16(operand);
                if (offset < 0 || offset >= heap.length) {
                    throw new IndexOutOfBoundsException("Global memory address out of bounds: " + offset);
                }
                setRegister(rd, heap[offset]);
                break;
            }
            case RegisterBytecodeDefinition.INSTR_SW_G: {
                // sw_g rs, offset: 全局存储
                int rs = extractRd(operand);
                int offset = extractImm16(operand);
                if (offset < 0 || offset >= heap.length) {
                    throw new IndexOutOfBoundsException("Global memory address out of bounds: " + offset);
                }
                heap[offset] = getRegister(rs);
                break;
            }
            case RegisterBytecodeDefinition.INSTR_LW_F: {
                // lw_f rd, offset: 字段加载，base=对象指针（假设在rs1寄存器）
                // 简化：对象指针是heap中的地址
                int rd = extractRd(operand);
                int objPtrReg = extractRs1(operand); // 对象指针寄存器
                int offset = extractImm16(operand);
                int objPtr = getRegister(objPtrReg);
                if (objPtr < 0 || objPtr >= heap.length) {
                    throw new IndexOutOfBoundsException("Object pointer out of bounds: " + objPtr);
                }
                int fieldAddress = objPtr + offset;
                if (fieldAddress < 0 || fieldAddress >= heap.length) {
                    throw new IndexOutOfBoundsException("Field address out of bounds: " + fieldAddress);
                }
                setRegister(rd, heap[fieldAddress]);
                break;
            }
            case RegisterBytecodeDefinition.INSTR_SW_F: {
                // sw_f rs, offset: 字段存储
                int rs = extractRd(operand);
                int objPtrReg = extractRs1(operand);
                int offset = extractImm16(operand);
                int objPtr = getRegister(objPtrReg);
                if (objPtr < 0 || objPtr >= heap.length) {
                    throw new IndexOutOfBoundsException("Object pointer out of bounds: " + objPtr);
                }
                int fieldAddress = objPtr + offset;
                if (fieldAddress < 0 || fieldAddress >= heap.length) {
                    throw new IndexOutOfBoundsException("Field address out of bounds: " + fieldAddress);
                }
                heap[fieldAddress] = getRegister(rs);
                break;
            }
            case RegisterBytecodeDefinition.INSTR_SLT: {
                // slt rd, rs1, rs2: set less than
                int rd = extractRd(operand);
                int rs1 = extractRs1(operand);
                int rs2 = extractRs2(operand);
                setRegister(rd, getRegister(rs1) < getRegister(rs2) ? 1 : 0);
                break;
            }
            case RegisterBytecodeDefinition.INSTR_SLE: {
                // sle rd, rs1, rs2: set less or equal
                int rd = extractRd(operand);
                int rs1 = extractRs1(operand);
                int rs2 = extractRs2(operand);
                setRegister(rd, getRegister(rs1) <= getRegister(rs2) ? 1 : 0);
                break;
            }
            case RegisterBytecodeDefinition.INSTR_SGT: {
                // sgt rd, rs1, rs2: set greater than
                int rd = extractRd(operand);
                int rs1 = extractRs1(operand);
                int rs2 = extractRs2(operand);
                setRegister(rd, getRegister(rs1) > getRegister(rs2) ? 1 : 0);
                break;
            }
            case RegisterBytecodeDefinition.INSTR_SGE: {
                // sge rd, rs1, rs2: set greater or equal
                int rd = extractRd(operand);
                int rs1 = extractRs1(operand);
                int rs2 = extractRs2(operand);
                setRegister(rd, getRegister(rs1) >= getRegister(rs2) ? 1 : 0);
                break;
            }
            case RegisterBytecodeDefinition.INSTR_SEQ: {
                // seq rd, rs1, rs2: set equal
                int rd = extractRd(operand);
                int rs1 = extractRs1(operand);
                int rs2 = extractRs2(operand);
                setRegister(rd, getRegister(rs1) == getRegister(rs2) ? 1 : 0);
                break;
            }
            case RegisterBytecodeDefinition.INSTR_SNE: {
                // sne rd, rs1, rs2: set not equal
                int rd = extractRd(operand);
                int rs1 = extractRs1(operand);
                int rs2 = extractRs2(operand);
                setRegister(rd, getRegister(rs1) != getRegister(rs2) ? 1 : 0);
                break;
            }
            case RegisterBytecodeDefinition.INSTR_NEG: {
                // neg rd, rs1: negate
                int rd = extractRd(operand);
                int rs1 = extractRs1(operand);
                setRegister(rd, -getRegister(rs1));
                break;
            }
            case RegisterBytecodeDefinition.INSTR_NOT: {
                // not rd, rs1: logical not
                int rd = extractRd(operand);
                int rs1 = extractRs1(operand);
                setRegister(rd, getRegister(rs1) == 0 ? 1 : 0);
                break;
            }
            case RegisterBytecodeDefinition.INSTR_AND: {
                // and rd, rs1, rs2: bitwise and
                int rd = extractRd(operand);
                int rs1 = extractRs1(operand);
                int rs2 = extractRs2(operand);
                setRegister(rd, getRegister(rs1) & getRegister(rs2));
                break;
            }
            case RegisterBytecodeDefinition.INSTR_OR: {
                // or rd, rs1, rs2: bitwise or
                int rd = extractRd(operand);
                int rs1 = extractRs1(operand);
                int rs2 = extractRs2(operand);
                setRegister(rd, getRegister(rs1) | getRegister(rs2));
                break;
            }
            case RegisterBytecodeDefinition.INSTR_XOR: {
                // xor rd, rs1, rs2: bitwise xor
                int rd = extractRd(operand);
                int rs1 = extractRs1(operand);
                int rs2 = extractRs2(operand);
                setRegister(rd, getRegister(rs1) ^ getRegister(rs2));
                break;
            }
            case RegisterBytecodeDefinition.INSTR_FADD: {
                // fadd rd, rs1, rs2: floating add
                int rd = extractRd(operand);
                int rs1 = extractRs1(operand);
                int rs2 = extractRs2(operand);
                float a = Float.intBitsToFloat(getRegister(rs1));
                float b = Float.intBitsToFloat(getRegister(rs2));
                setRegister(rd, Float.floatToIntBits(a + b));
                break;
            }
            case RegisterBytecodeDefinition.INSTR_FSUB: {
                // fsub rd, rs1, rs2: floating subtract
                int rd = extractRd(operand);
                int rs1 = extractRs1(operand);
                int rs2 = extractRs2(operand);
                float a = Float.intBitsToFloat(getRegister(rs1));
                float b = Float.intBitsToFloat(getRegister(rs2));
                setRegister(rd, Float.floatToIntBits(a - b));
                break;
            }
            case RegisterBytecodeDefinition.INSTR_FMUL: {
                // fmul rd, rs1, rs2: floating multiply
                int rd = extractRd(operand);
                int rs1 = extractRs1(operand);
                int rs2 = extractRs2(operand);
                float a = Float.intBitsToFloat(getRegister(rs1));
                float b = Float.intBitsToFloat(getRegister(rs2));
                setRegister(rd, Float.floatToIntBits(a * b));
                break;
            }
            case RegisterBytecodeDefinition.INSTR_FDIV: {
                // fdiv rd, rs1, rs2: floating divide
                int rd = extractRd(operand);
                int rs1 = extractRs1(operand);
                int rs2 = extractRs2(operand);
                float a = Float.intBitsToFloat(getRegister(rs1));
                float b = Float.intBitsToFloat(getRegister(rs2));
                if (b == 0.0f) {
                    throw new ArithmeticException("Floating division by zero");
                }
                setRegister(rd, Float.floatToIntBits(a / b));
                break;
            }
            case RegisterBytecodeDefinition.INSTR_FLT: {
                // flt rd, rs1, rs2: floating less than
                int rd = extractRd(operand);
                int rs1 = extractRs1(operand);
                int rs2 = extractRs2(operand);
                float a = Float.intBitsToFloat(getRegister(rs1));
                float b = Float.intBitsToFloat(getRegister(rs2));
                setRegister(rd, a < b ? 1 : 0);
                break;
            }
            case RegisterBytecodeDefinition.INSTR_FEQ: {
                // feq rd, rs1, rs2: floating equal
                int rd = extractRd(operand);
                int rs1 = extractRs1(operand);
                int rs2 = extractRs2(operand);
                float a = Float.intBitsToFloat(getRegister(rs1));
                float b = Float.intBitsToFloat(getRegister(rs2));
                setRegister(rd, a == b ? 1 : 0);
                break;
            }
            case RegisterBytecodeDefinition.INSTR_ITOF: {
                // itof rd, rs1: integer to float
                int rd = extractRd(operand);
                int rs1 = extractRs1(operand);
                float result = (float) getRegister(rs1);
                setRegister(rd, Float.floatToIntBits(result));
                break;
            }
            case RegisterBytecodeDefinition.INSTR_LC: {
                // lc rd, immediate: load character
                int rd = extractRd(operand);
                int imm = extractImm16(operand);
                setRegister(rd, imm & 0xFFFF); // 字符是16位
                break;
            }
            case RegisterBytecodeDefinition.INSTR_LF: {
                // lf rd, pool_index: load float from constant pool
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
                break;
            }
            case RegisterBytecodeDefinition.INSTR_LS: {
                // ls rd, pool_index: load string from constant pool (returns address in heap)
                // 简化：将字符串复制到堆中，返回地址
                int rd = extractRd(operand);
                int poolIndex = extractImm16(operand);
                if (poolIndex < 0 || poolIndex >= constPool.length) {
                    throw new IndexOutOfBoundsException("Constant pool index out of bounds: " + poolIndex);
                }
                Object constant = constPool[poolIndex];
                if (constant instanceof String) {
                    String str = (String) constant;
                    // 将字符串存储到堆中（简化：每个字符一个字）
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
                break;
            }
            case RegisterBytecodeDefinition.INSTR_STRUCT: {
                // struct rd, size: allocate struct with given number of fields
                int rd = extractRd(operand);
                int size = extractImm16(operand);
                // 在堆中分配空间
                int address = heapAllocPointer;
                if (address + size > heap.length) {
                    throw new OutOfMemoryError("Not enough heap space for struct");
                }
                // 初始化为0
                for (int i = 0; i < size; i++) {
                    heap[address + i] = 0;
                }
                heapAllocPointer += size;
                setRegister(rd, address);
                break;
            }
            case RegisterBytecodeDefinition.INSTR_NULL: {
                // null rd: load null pointer (0)
                int rd = extractRd(operand);
                setRegister(rd, 0);
                break;
            }
            case RegisterBytecodeDefinition.INSTR_MOV: {
                // mov rd, rs1: move register
                int rd = extractRd(operand);
                int rs1 = extractRs1(operand);
                setRegister(rd, getRegister(rs1));
                break;
            }
            case RegisterBytecodeDefinition.INSTR_HALT:
                running = false;
                break;
            default:
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
}