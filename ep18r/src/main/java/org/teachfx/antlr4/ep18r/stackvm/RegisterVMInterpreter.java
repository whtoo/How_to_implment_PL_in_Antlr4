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
    // 寄存器文件：16个寄存器，r0-r15
    private final int[] registers = new int[RegisterBytecodeDefinition.NUM_REGISTERS];

    // 内存和运行时数据结构
    private Object[] constPool;
    private byte[] code;
    private int codeSize;
    private Object[] globals;
    private StackFrame[] callStack = new StackFrame[1024];
    private int framePointer = -1;
    private FunctionSymbol mainFunction;

    // 程序计数器和执行状态
    private int programCounter;
    private boolean running;
    private boolean trace = false;

    // 特殊用途寄存器别名
    private static final int SP = RegisterBytecodeDefinition.R13; // 栈指针
    private static final int FP = RegisterBytecodeDefinition.R14; // 帧指针
    private static final int LR = RegisterBytecodeDefinition.R15; // 链接寄存器

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
        while (running && programCounter < codeSize) {
            // 提取指令（假设每条指令5字节：1字节操作码 + 4字节操作数）
            // 实际指令格式可能不同，这里需要根据RegisterBytecodeDefinition调整
            int opcode = code[programCounter] & 0xFF;
            int operand = 0;
            if (programCounter + 4 < codeSize) {
                operand = ((code[programCounter + 1] & 0xFF) << 24) |
                          ((code[programCounter + 2] & 0xFF) << 16) |
                          ((code[programCounter + 3] & 0xFF) << 8) |
                          (code[programCounter + 4] & 0xFF);
            }

            if (trace) {
                System.out.printf("PC=%04d: opcode=%02x operand=%08x ", programCounter, opcode, operand);
                // 反汇编显示
                disassembleInstruction(opcode, operand);
            }

            // 根据操作码执行指令
            executeInstruction(opcode, operand);

            // 更新程序计数器（假设每条指令5字节）
            programCounter += 5;
        }
    }

    /**
     * 执行单条指令
     */
    private void executeInstruction(int opcode, int operand) throws Exception {
        // TODO: 实现寄存器指令集执行逻辑
        // 基于RegisterBytecodeDefinition中的指令定义
        // 需要处理R类型、I类型、J类型指令格式
        switch (opcode) {
            case RegisterBytecodeDefinition.INSTR_ADD:
                // add rd, rs1, rs2
                // 需要从指令中提取寄存器编号
                // 暂时未实现
                break;
            case RegisterBytecodeDefinition.INSTR_SUB:
                break;
            case RegisterBytecodeDefinition.INSTR_MUL:
                break;
            case RegisterBytecodeDefinition.INSTR_DIV:
                break;
            case RegisterBytecodeDefinition.INSTR_LI:
                // li rd, immediate
                break;
            case RegisterBytecodeDefinition.INSTR_CALL:
                break;
            case RegisterBytecodeDefinition.INSTR_RET:
                break;
            case RegisterBytecodeDefinition.INSTR_J:
                break;
            case RegisterBytecodeDefinition.INSTR_JT:
                break;
            case RegisterBytecodeDefinition.INSTR_JF:
                break;
            case RegisterBytecodeDefinition.INSTR_PRINT:
                break;
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
}