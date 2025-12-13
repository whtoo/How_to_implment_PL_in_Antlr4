package org.teachfx.antlr4.ep18;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.teachfx.antlr4.ep18.parser.VMAssemblerLexer;
import org.teachfx.antlr4.ep18.parser.VMAssemblerParser;
import org.teachfx.antlr4.ep18.stackvm.*;

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
            f.locals[i] = 0;
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
                    StackFrame fr = calls[fp--];
                    ip = fr.returnAddress;
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
                    operands[++sp] = calls[fp].locals[loadAddr];
                    break;
                case BytecodeDefinition.INSTR_STORE:
                    int storeAddr = getIntOperand();
                    calls[fp].locals[storeAddr] = operands[sp--];
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
        StackFrame f = new StackFrame(fs, ip);
        calls[++fp] = f;
        for (int a = fs.nargs - 1; a >= 0; a--) {
            f.locals[a] = operands[sp--];
        }
        ip = fs.address;
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
                System.out.print(" " + calls[i].symbol.name);
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
}
