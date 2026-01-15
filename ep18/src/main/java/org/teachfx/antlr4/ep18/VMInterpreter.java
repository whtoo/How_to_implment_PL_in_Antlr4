package org.teachfx.antlr4.ep18;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.teachfx.antlr4.ep18.parser.VMAssemblerLexer;
import org.teachfx.antlr4.ep18.parser.VMAssemblerParser;
import org.teachfx.antlr4.ep18.stackvm.*;
import org.teachfx.antlr4.ep18.stackvm.ABIConvention.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class VMInterpreter {
    public static final int DEFAULT_OPERAND_STACK_SIZE = 128;
    public static final int DEFAULT_CALL_STACK_SIZE = 1024;
    protected Object[] constPool;
    DisAssembler disasm;
    int ip;
    byte[] code;
    int codeSize;
    Object[] globals;
    Object[] operands = new Object[DEFAULT_OPERAND_STACK_SIZE];
    int sp = -1;

    StackFrame[] calls = new StackFrame[DEFAULT_CALL_STACK_SIZE];
    int fp = -1;
    FunctionSymbol mainFunction;

    boolean trace = false;

    public static void main(String[] args) throws Exception {
        boolean trace = false;
        boolean disassemble = false;
        boolean dump = false;
        String fileName = "t.vm";
        int i = 0;
        while (i < args.length) {
            switch (args[i]) {
                case "-trace" -> {
                    trace = true;
                    i++;
                }
                case "-dis" -> {
                    disassemble = true;
                    i++;
                }
                case "-dump" -> {
                    dump = true;
                    i++;
                }
                default -> {
                    fileName = args[i];
                    i++;
                }
            }
        }

        InputStream input = null;
        if (fileName != null) {
            // 优先从classpath加载资源
            input = VMInterpreter.class.getClassLoader().getResourceAsStream(fileName);
            if (input == null) {
                // 如果classpath找不到，再尝试从文件系统加载
                String resourcePath = "src/main/resources/" + fileName;
                File file = new File(resourcePath);
                
                if (file.exists()) {
                    input = new FileInputStream(file);
                }
            }
        }
        if (input == null) {
            input = System.in;
        }

        VMInterpreter interpreter = new VMInterpreter();
        load(interpreter, input);
        interpreter.trace = trace;
        interpreter.exec();
        if (disassemble)
            interpreter.disassemble();
        if (dump)
            interpreter.coredump();
    }

    public static boolean load(VMInterpreter interp, InputStream input) throws Exception {
        boolean hasErrors = false;
        try (input) {
            VMAssemblerLexer assemblerLexer = new VMAssemblerLexer(CharStreams.fromStream(input));
            CommonTokenStream tokenStream = new CommonTokenStream(assemblerLexer);
            VMAssemblerParser parser = new VMAssemblerParser(tokenStream);
            ParseTree parseTree = parser.program();
            ParseTreeWalker walker = new ParseTreeWalker();
            ByteCodeAssembler assembler = new ByteCodeAssembler(BytecodeDefinition.instructions);
            walker.walk(assembler, parseTree);

            interp.code = assembler.getMachineCode();
            interp.codeSize = assembler.getCodeMemorySize();
            interp.constPool = assembler.getConstantPool();
            interp.mainFunction = assembler.getMainFunction();
            interp.globals = new Object[assembler.getDataSize()];
            interp.disasm = new DisAssembler(interp.code, interp.codeSize, interp.constPool);

            hasErrors = parser.getNumberOfSyntaxErrors() > 0 || assembler.hasErrors();
        }
        return hasErrors;
    }

    /**
     * Execute the bytecodes in code memory starting at mainAddr
     */
    public void exec() throws Exception {
        // SIMULATE "call main()"; set up stack as if we'd called main()
        if (mainFunction == null) {
            mainFunction = new FunctionSymbol("main", 0, 0, 0);
        }
        StackFrame f = new StackFrame(mainFunction, -1);
        calls[++fp] = f;
        ip = mainFunction.address;
        // 初始化所有局部变量为0
        for (int i = 0; i < mainFunction.nlocals; i++) {
            f.getLocals()[i] = 0;
        }
        cpu();
    }

    // simulation by software
    protected void cpu() {
        int addr = 0; // Declare addr here for branch instructions
        while (ip < codeSize) {
            short opcode = code[ip];

            if (trace) {
                trace();
            }

            ip++; // Advance to next byte position after opcode

            switch (opcode) {
                case BytecodeDefinition.INSTR_IADD:
                    int a1 = (Integer) operands[sp - 1];
                    int a2 = (Integer) operands[sp];
                    sp -= 2;
                    operands[++sp] = a1 + a2;
                    break;
                case BytecodeDefinition.INSTR_ISUB:
                    int b1 = (Integer) operands[sp - 1];
                    int b2 = (Integer) operands[sp];
                    sp -= 2;
                    operands[++sp] = b1 - b2;
                    break;
                case BytecodeDefinition.INSTR_IMUL:
                    int c1 = (Integer) operands[sp - 1];
                    int c2 = (Integer) operands[sp];
                    sp -= 2;
                    operands[++sp] = c1 * c2;
                    break;
                case BytecodeDefinition.INSTR_ILT:
                    int d1 = (Integer) operands[sp - 1];
                    int d2 = (Integer) operands[sp];
                    sp -= 2;
                    operands[++sp] = d1 < d2;
                    break;
                case BytecodeDefinition.INSTR_IGT:
                    int e1 = (Integer) operands[sp - 1];
                    int e2 = (Integer) operands[sp];
                    sp -= 2;
                    operands[++sp] = e1 > e2;
                    break;
                case BytecodeDefinition.INSTR_IDIV:
                    int f1 = (Integer) operands[sp - 1];
                    int f2 = (Integer) operands[sp];
                    sp -= 2;
                    if (f2 == 0) {
                        throw new RuntimeException("Division by zero at PC=" + (ip - 1) + " (instruction=IDIV)");
                    }
                    operands[++sp] = f1 / f2;
                    break;
                case BytecodeDefinition.INSTR_INEG:
                    int g = (Integer) operands[sp--];
                    operands[++sp] = -g;
                    break;
                case BytecodeDefinition.INSTR_IEQ:
                    int h1 = (Integer) operands[sp - 1];
                    int h2 = (Integer) operands[sp];
                    sp -= 2;
                    operands[++sp] = h1 == h2;
                    break;
                case BytecodeDefinition.INSTR_ILE:
                    int i1 = (Integer) operands[sp - 1];
                    int i2 = (Integer) operands[sp];
                    sp -= 2;
                    operands[++sp] = i1 <= i2;
                    break;
                case BytecodeDefinition.INSTR_IGE:
                    int j1 = (Integer) operands[sp - 1];
                    int j2 = (Integer) operands[sp];
                    sp -= 2;
                    operands[++sp] = j1 >= j2;
                    break;
                case BytecodeDefinition.INSTR_INE:
                    int k1 = (Integer) operands[sp - 1];
                    int k2 = (Integer) operands[sp];
                    sp -= 2;
                    operands[++sp] = k1 != k2;
                    break;
                case BytecodeDefinition.INSTR_INOT:
                    boolean l = (Boolean) operands[sp--];
                    operands[++sp] = !l;
                    break;
                case BytecodeDefinition.INSTR_IXOR:
                    int m1 = (Integer) operands[sp - 1];
                    int m2 = (Integer) operands[sp];
                    sp -= 2;
                    operands[++sp] = m1 ^ m2;
                    break;
                case BytecodeDefinition.INSTR_IAND:
                    int n1 = (Integer) operands[sp - 1];
                    int n2 = (Integer) operands[sp];
                    sp -= 2;
                    operands[++sp] = n1 & n2;
                    break;
                case BytecodeDefinition.INSTR_IOR:
                    int o1 = (Integer) operands[sp - 1];
                    int o2 = (Integer) operands[sp];
                    sp -= 2;
                    operands[++sp] = o1 | o2;
                    break;
                case BytecodeDefinition.INSTR_FADD:
                    float p1 = (Float) operands[sp - 1];
                    float p2 = (Float) operands[sp];
                    sp -= 2;
                    operands[++sp] = p1 + p2;
                    break;
                case BytecodeDefinition.INSTR_FSUB:
                    float q1 = (Float) operands[sp - 1];
                    float q2 = (Float) operands[sp];
                    sp -= 2;
                    operands[++sp] = q1 - q2;
                    break;
                case BytecodeDefinition.INSTR_FMUL:
                    float r1 = (Float) operands[sp - 1];
                    float r2 = (Float) operands[sp];
                    sp -= 2;
                    operands[++sp] = r1 * r2;
                    break;
                case BytecodeDefinition.INSTR_FDIV:
                    float s1 = (Float) operands[sp - 1];
                    float s2 = (Float) operands[sp];
                    sp -= 2;
                    if (s2 == 0.0f) {
                        throw new RuntimeException("Division by zero");
                    }
                    operands[++sp] = s1 / s2;
                    break;
                case BytecodeDefinition.INSTR_FLT:
                    float t1 = (Float) operands[sp - 1];
                    float t2 = (Float) operands[sp];
                    sp -= 2;
                    operands[++sp] = t1 < t2;
                    break;
                case BytecodeDefinition.INSTR_FEQ:
                    float u1 = (Float) operands[sp - 1];
                    float u2 = (Float) operands[sp];
                    sp -= 2;
                    operands[++sp] = u1 == u2;
                    break;
                case BytecodeDefinition.INSTR_ITOF:
                    int v = (Integer) operands[sp--];
                    operands[++sp] = (float) v;
                    break;
                case BytecodeDefinition.INSTR_RET:
                    handleReturn();
                    break;
                case BytecodeDefinition.INSTR_BR:
                    ip = getIntOperand();
                    break;
                case BytecodeDefinition.INSTR_BRT:
                    addr = getIntOperand();
                    if ((Boolean) operands[sp--]) {
                        ip = addr;
                    }
                    break;
                case BytecodeDefinition.INSTR_BRF:
                    addr = getIntOperand();
                    if (!(Boolean) operands[sp--]) {
                        ip = addr;
                    }
                    break;
                case BytecodeDefinition.INSTR_CALL:
                    int functionConstPoolIndex = getIntOperand();
                    call(functionConstPoolIndex);
                    break;
                case BytecodeDefinition.INSTR_CCONST:
                    operands[++sp] = (char) getIntOperand();
                    break;
                case BytecodeDefinition.INSTR_ICONST:
                    operands[++sp] = getIntOperand();
                    break;
                case BytecodeDefinition.INSTR_FCONST:
                case BytecodeDefinition.INSTR_SCONST:
                    int constPoolIndex = getIntOperand();
                    operands[++sp] = constPool[constPoolIndex];
                    break;
                case BytecodeDefinition.INSTR_LOAD:
                    int loadAddr = getIntOperand();
                    StackFrame currentFrame = calls[fp];
                    // 首先检查是否是局部变量
                    if (loadAddr >= 0 && loadAddr < currentFrame.getLocals().length) {
                        operands[++sp] = currentFrame.getLocals()[loadAddr];
                    }
                    // 然后检查是否是参数（当loadAddr在参数范围内且不是局部变量时）
                    else if (loadAddr >= 0 && currentFrame.getParameters() != null &&
                             loadAddr < currentFrame.getParameters().length) {
                        operands[++sp] = currentFrame.getParameters()[loadAddr];
                    } else {
                        operands[++sp] = null; // 或者抛出异常
                    }
                    break;
                case BytecodeDefinition.INSTR_STORE:
                    int storeAddr = getIntOperand();
                    StackFrame currentFrameStore = calls[fp];
                    // 首先检查是否是局部变量
                    if (storeAddr >= 0 && storeAddr < currentFrameStore.getLocals().length) {
                        currentFrameStore.getLocals()[storeAddr] = operands[sp--];
                    }
                    // 然后检查是否是参数（当storeAddr在参数范围内且不是局部变量时）
                    else if (storeAddr >= 0 && currentFrameStore.getParameters() != null &&
                             storeAddr < currentFrameStore.getParameters().length) {
                        currentFrameStore.getParameters()[storeAddr] = operands[sp--];
                    } else {
                        sp--; // 丢弃值
                    }
                    break;
                case BytecodeDefinition.INSTR_GLOAD:
                    int gloadAddr = getIntOperand();
                    operands[++sp] = globals[gloadAddr];
                    break;
                case BytecodeDefinition.INSTR_FLOAD:
                    Object obj = operands[sp--];
                    if (!(obj instanceof StructValue)) {
                        throw new ClassCastException("Expected StructValue but got " + (obj == null ? "null" : obj.getClass()) + " value: " + obj);
                    }
                    StructValue structVal = (StructValue) obj;
                    int fieldOffset = getIntOperand();
                    operands[++sp] = structVal.getField(fieldOffset);
                    break;
                case BytecodeDefinition.INSTR_GSTORE:
                    int gstoreAddr = getIntOperand();
                    globals[gstoreAddr] = operands[sp--];
                    break;
                case BytecodeDefinition.INSTR_FSTORE:
                    Object val = operands[sp--];  // 先弹出值
                    Object obj2 = operands[sp--];  // 再弹出结构体引用
                    if (!(obj2 instanceof StructValue)) {
                        throw new ClassCastException("Expected StructValue but got " + (obj2 == null ? "null" : obj2.getClass()) + " value: " + obj2);
                    }
                    StructValue struct2 = (StructValue) obj2;
                    int fieldOffset2 = getIntOperand();
                    struct2.setField(fieldOffset2, val);
                    break;
                case BytecodeDefinition.INSTR_PRINT:
                    System.out.println(operands[sp--]);
                    break;
                case BytecodeDefinition.INSTR_STRUCT:
                    int nfields = getIntOperand();
                    StructValue struct = new StructValue(nfields);
                    operands[++sp] = struct;
                    if (trace) System.out.println("STRUCT: created " + struct + " at operands[" + sp + "]");
                    break;
                case BytecodeDefinition.INSTR_NULL:
                    operands[++sp] = null;
                    break;
                case BytecodeDefinition.INSTR_POP:
                    --sp;
                    break;
                case BytecodeDefinition.INSTR_HALT:
                    return;
                default:
                    throw new Error("invalid opcode: " + opcode + " at ip=" + (ip - 1));
            }
        }
    }

    protected void call(int functionConstPoolIndex) {
        FunctionSymbol fs = (FunctionSymbol) constPool[functionConstPoolIndex];

        // 创建符合ABI规范的调用上下文
        Object[] arguments = extractArgumentsFromStack(fs);
        StackFrame frame = new StackFrame(fs, ip, null); // 使用新的构造函数，保持previousFrame为null以兼容

        // 创建调用上下文并验证
        CallContext callContext = new CallContext(fs, ip, arguments, frame);
        if (!callContext.validate()) {
            throw new RuntimeException("ABI validation failed for call to " + fs.name);
        }

        // 保存当前栈深度（用于返回时清理参数）
        int savedStackDepth = sp;
        if (fp >= 0 && calls[fp] != null) {
            calls[fp].setDebugData("savedStackDepth", savedStackDepth);
        }

        // 将参数存储到栈帧中
        storeArgumentsInFrame(frame, arguments);

        // 压入新的栈帧
        calls[++fp] = frame;

        // 跳转到函数入口地址
        ip = fs.address;

        if (trace) {
            System.out.println("[ABI] CALL " + fs.name + " with " + fs.nargs + " args, saved stack depth=" + savedStackDepth);
        }
    }

    /**
     * 从操作数栈中提取函数参数（符合ABI规范）
     */
    private Object[] extractArgumentsFromStack(FunctionSymbol function) {
        if (function.nargs <= 0) {
            return new Object[0];
        }

        Object[] args = new Object[function.nargs];
        // 参数从右到左压栈，所以从栈顶开始反向提取
        for (int i = function.nargs - 1; i >= 0; i--) {
            if (sp < 0) {
                throw new RuntimeException("Stack underflow while extracting arguments for " + function.name);
            }
            args[i] = operands[sp--];
        }
        return args;
    }

    /**
     * 将参数存储到栈帧中（符合ABI规范）
     */
    private void storeArgumentsInFrame(StackFrame frame, Object[] arguments) {
        FunctionSymbol function = frame.getSymbol();

        // 根据ABI规范，参数可以存储在局部变量区或专门的参数区
        // 当前实现：所有参数存储在局部变量数组中（如果空间足够）
        // 如果局部变量不足，剩余参数存储在参数数组中
        int localsToFill = Math.min(function.nargs, function.nlocals);
        for (int i = 0; i < localsToFill; i++) {
            frame.getLocals()[i] = arguments[i];
        }

        // 剩余参数存储在参数数组中
        if (function.nargs > localsToFill && frame.getParameters() != null) {
            for (int i = localsToFill; i < function.nargs; i++) {
                frame.getParameters()[i - localsToFill] = arguments[i];
            }
        }
    }

    /**
     * ABI兼容的函数返回处理
     */
    protected void handleReturn() {
        if (fp < 0) {
            throw new RuntimeException("RET called without active frame");
        }

        StackFrame frame = calls[fp--];
        int returnAddress = frame.getReturnAddress();

        // 获取返回值（如果有）
        Object returnValue = null;
        if (sp >= 0) {
            returnValue = operands[sp--]; // 返回值应在栈顶
        }

        // 获取保存的栈深度并恢复栈状态
        Integer savedDepth = null;
        if (fp >= 0 && calls[fp] != null) {
            savedDepth = (Integer) calls[fp].getDebugData("savedStackDepth");
        }

        // 清理参数：恢复到调用前的栈深度
        if (savedDepth != null) {
            sp = savedDepth;
        }

        // 压入返回值（如果有）
        if (returnValue != null) {
            operands[++sp] = returnValue;
        }

        // 恢复程序计数器
        ip = returnAddress;

        if (trace) {
            System.out.println("[ABI] RET to " + returnAddress + ", return value=" + returnValue);
        }
    }

    protected int getIntOperand() {
        int word = ByteCodeAssembler.getInt(code, ip);
        ip += 4;
        return word;
    }

    // Tracing, dumping, ...

    public void disassemble() {
        disasm.disassemble();
    }

    protected void trace() {
        disasm.disassembleInstruction(ip);
        System.out.print("\tstack=[");
        for (int i = 0; i <= sp; i++) {
            Object o = operands[i];
            System.out.print(" " + o);
        }
        System.out.print(" ]");
        if (fp >= 0) {
            System.out.print(", calls=[");
            for (int i = 0; i <= fp; i++) {
                System.out.print(" " + calls[i].getSymbol().name);
            }
            System.out.print(" ]");
        }
        System.out.println();
    }

    public void coredump() {
        if (constPool.length > 0)
            dumpConstantPool();
        if (globals.length > 0)
            dumpDataMemory();
        dumpCodeMemory();
    }

    protected void dumpConstantPool() {
        System.out.println("Constant pool:");
        int addr = 0;
        for (Object o : constPool) {
            if (o instanceof String) {
                System.out.printf("%04d: \"%s\"\n", addr, o);
            } else {
                System.out.printf("%04d: %s\n", addr, o);
            }
            addr++;
        }
        System.out.println();
    }

    protected void dumpDataMemory() {
        System.out.println("Data memory:");
        int addr = 0;
        for (Object o : globals) {
            if (o != null) {
                System.out.printf("%04d: %s <%s>\n", addr, o, o.getClass().getSimpleName());
            } else {
                System.out.printf("%04d: <null>\n", addr);
            }
            addr++;
        }
        System.out.println();
    }

    public void dumpCodeMemory() {
        System.out.println("Code memory:");
        for (int i = 0; code != null && i < codeSize; i++) {
            if (i % 8 == 0 && i != 0)
                System.out.println();
            if (i % 8 == 0)
                System.out.printf("%04d:", i);
            System.out.printf(" %3d", ((int) code[i]));
        }
        System.out.println();
    }

    /**
     * 获取字节码数组
     * @return 字节码数组
     */
    public byte[] getCode() {
        return code;
    }

    /**
     * 获取字节码大小
     * @return 字节码大小
     */
    public int getCodeSize() {
        return codeSize;
    }

    /**
     * 获取程序计数器（指令指针）
     * @return 当前指令指针位置
     */
    public int getProgramCounter() {
        return ip;
    }

    /**
     * 获取操作数栈快照
     * @return 操作数栈的副本（从栈底到栈顶）
     */
    public Object[] getOperandStack() {
        if (sp < 0) {
            return new Object[0];
        }
        Object[] snapshot = new Object[sp + 1];
        System.arraycopy(operands, 0, snapshot, 0, sp + 1);
        return snapshot;
    }

    /**
     * 获取栈指针
     * @return 当前栈指针位置
     */
    public int getStackPointer() {
        return sp;
    }

    /**
     * 获取调用栈帧快照
     * @return 活动调用栈帧的列表（从底部到顶部）
     */
    public StackFrame[] getCallStackFrames() {
        if (fp < 0) {
            return new StackFrame[0];
        }
        StackFrame[] snapshot = new StackFrame[fp + 1];
        System.arraycopy(calls, 0, snapshot, 0, fp + 1);
        return snapshot;
    }

    /**
     * 获取帧指针
     * @return 当前帧指针位置
     */
    public int getFramePointer() {
        return fp;
    }

    /**
     * 获取全局变量快照
     * @return 全局变量数组的副本
     */
    public Object[] getGlobalVariables() {
        if (globals == null) {
            return new Object[0];
        }
        return globals.clone();
    }

    /**
     * 获取常量池快照
     * @return 常量池数组的副本
     */
    public Object[] getConstantPool() {
        if (constPool == null) {
            return new Object[0];
        }
        return constPool.clone();
    }

    /**
     * 获取反汇编器实例
     * @return 反汇编器实例
     */
    public DisAssembler getDisAssembler() {
        return disasm;
    }

    /**
     * 获取主函数符号
     * @return 主函数符号
     */
    public FunctionSymbol getMainFunction() {
        return mainFunction;
    }

    /**
     * 检查是否正在跟踪执行
     * @return 是否启用跟踪模式
     */
    public boolean isTraceEnabled() {
        return trace;
    }

    /**
     * 设置跟踪模式
     * @param trace 是否启用跟踪模式
     */
    public void setTrace(boolean trace) {
        this.trace = trace;
    }
}
