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

            hasErrors = parser.getNumberOfSyntaxErrors() > 0;
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
        cpu();
    }

    // simulation by software
    protected void cpu() {
        Object v = null;
        int a, b;
        float e, f;
        boolean p,q;
        int addr = 0;
        short opcode = code[ip];
        while (opcode != BytecodeDefinition.INSTR_HALT && ip < codeSize) {
            if (trace)
                trace();
            ip++;
            switch (opcode) {
                case BytecodeDefinition.INSTR_IADD:
                    a = (Integer) operands[sp - 1];
                    b = (Integer) operands[sp];
                    sp -= 2;
                    operands[++sp] = a + b;
                    break;
                case BytecodeDefinition.INSTR_ISUB:
                    a = (Integer) operands[sp - 1];
                    b = (Integer) operands[sp];
                    sp -= 2;
                    operands[++sp] = a - b;
                    break;
                case BytecodeDefinition.INSTR_IMUL:
                    a = (Integer) operands[sp - 1];
                    b = (Integer) operands[sp];
                    sp -= 2;
                    operands[++sp] = a * b;
                    break;
                case BytecodeDefinition.INSTR_ILT:
                    a = (Integer) operands[sp - 1];
                    b = (Integer) operands[sp];
                    sp -= 2;
                    operands[++sp] = a < b;
                    break;
                case BytecodeDefinition.INSTR_IGT:
                    a = (Integer) operands[sp - 1];
                    b = (Integer) operands[sp];
                    sp -= 2;
                    operands[++sp] = a > b;
                    break;
                case BytecodeDefinition.INSTR_IDIV:
                    a = (Integer) operands[sp - 1];
                    b = (Integer) operands[sp];
                    sp -= 2;
                    operands[++sp] = a / b;
                    break;
                case BytecodeDefinition.INSTR_INEG:
                    a = (Integer) operands[sp--];
                    operands[++sp] = -a;
                    break;
                case BytecodeDefinition.INSTR_IEQ:
                    a = (Integer) operands[sp - 1];
                    b = (Integer) operands[sp];
                    sp -= 2;
                    operands[++sp] = a == b;
                    break;
                case BytecodeDefinition.INSTR_ILE:
                    a = (Integer) operands[sp - 1];
                    b = (Integer) operands[sp];
                    sp -= 2;
                    operands[++sp] = a <= b;
                    break;
                case BytecodeDefinition.INSTR_IGE:
                    a = (Integer) operands[sp - 1];
                    b = (Integer) operands[sp];
                    sp -= 2;
                    operands[++sp] = (a >= b);
                    break;
                case BytecodeDefinition.INSTR_INE:
                    a = (Integer) operands[sp - 1];
                    b = (Integer) operands[sp];
                    sp -= 2;
                    operands[++sp] = a != b;
                    break;
                case BytecodeDefinition.INSTR_INOT:
                    p = (boolean)operands[sp--];
                    operands[++sp] = !p;
                    break;
                case BytecodeDefinition.INSTR_IAND:
                    p = (boolean) operands[sp - 1];
                    q = (boolean) operands[sp];
                    sp -= 2;
                    operands[++sp] = p && q;
                    break;
                case BytecodeDefinition.INSTR_IOR:
                    p = (boolean) operands[sp - 1];
                    q = (boolean) operands[sp];
                    sp -= 2;
                    operands[++sp] = p || q;
                    break;
                case BytecodeDefinition.INSTR_FADD:
                    e = (Float) operands[sp - 1];
                    f = (Float) operands[sp];
                    sp -= 2;
                    operands[++sp] = e + f;
                    break;
                case BytecodeDefinition.INSTR_FSUB:
                    e = (Float) operands[sp - 1];
                    f = (Float) operands[sp];
                    sp -= 2;
                    operands[++sp] = e - f;
                    break;
                case BytecodeDefinition.INSTR_FMUL:
                    e = (Float) operands[sp - 1];
                    f = (Float) operands[sp];
                    sp -= 2;
                    operands[++sp] = e * f;
                    break;
                case BytecodeDefinition.INSTR_FLT:
                    e = (Float) operands[sp - 1];
                    f = (Float) operands[sp];
                    sp -= 2;
                    operands[++sp] = e < f;
                    break;
                case BytecodeDefinition.INSTR_FEQ:
                    e = (Float) operands[sp - 1];
                    f = (Float) operands[sp];
                    sp -= 2;
                    operands[++sp] = e == f;
                    break;
                case BytecodeDefinition.INSTR_ITOF:
                    a = (Integer) operands[sp--];
                    operands[++sp] = (float) a;
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
                    if (operands[sp--].equals(true))
                        ip = addr;
                    break;
                case BytecodeDefinition.INSTR_BRF:
                    addr = getIntOperand();
                    if (operands[sp--].equals(false))
                        ip = addr;
                    break;
                case BytecodeDefinition.INSTR_CALL:
                    int functionConstPoolIndex = getIntOperand();
                    call(functionConstPoolIndex);
                    break;
                case BytecodeDefinition.INSTR_CCONST:
                    operands[++sp] = (char) getIntOperand();
                    break;
                case BytecodeDefinition.INSTR_ICONST:
                    operands[++sp] = getIntOperand(); // push operand
                    break;
                case BytecodeDefinition.INSTR_FCONST:
                case BytecodeDefinition.INSTR_SCONST:
                    int constPoolIndex = getIntOperand();
                    operands[++sp] = constPool[constPoolIndex];
                    break;

                case BytecodeDefinition.INSTR_LOAD: // load from call stack
                    addr = getIntOperand();
                    operands[++sp] = calls[fp].locals[addr];
                    break;

                case BytecodeDefinition.INSTR_STORE:
                    addr = getIntOperand();
                    calls[fp].locals[addr] = operands[sp--];
                    break;

                case BytecodeDefinition.INSTR_GLOAD:// load from global memory
                    addr = getIntOperand();
                    operands[++sp] = globals[addr];
                    break;
                case BytecodeDefinition.INSTR_FLOAD:
                    StructSpace struct = (StructSpace) operands[sp--];
                    int fieldOffset = getIntOperand();
                    operands[++sp] = struct.fields[fieldOffset];
                    break;
                case BytecodeDefinition.INSTR_GSTORE:
                    addr = getIntOperand();
                    globals[addr] = operands[sp--];
                    break;
                case BytecodeDefinition.INSTR_FSTORE:
                    struct = (StructSpace) operands[sp--];
                    v = operands[sp--];
                    fieldOffset = getIntOperand();
                    struct.fields[fieldOffset] = v;
                    break;
                case BytecodeDefinition.INSTR_PRINT:
                    System.out.println(operands[sp--]);
                    break;
                case BytecodeDefinition.INSTR_STRUCT:
                    int nfields = getIntOperand();
                    operands[++sp] = new StructSpace(nfields);
                    break;
                case BytecodeDefinition.INSTR_NULL:
                    operands[++sp] = null;
                    break;
                case BytecodeDefinition.INSTR_POP:
                    --sp;
                    break;
                default:
                    throw new Error("invalid opcode: " + opcode + " at ip=" + (ip - 1));
            }
            opcode = code[ip];
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
