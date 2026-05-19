package org.teachfx.antlr4.ep10.stackvm;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.teachfx.antlr4.ep10.parser.VMAssemblerLexer;
import org.teachfx.antlr4.ep10.parser.VMAssemblerParser;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * VMInterpreter — reads a .vmasm file, assembles it to bytecode, and interprets it.
 * This is the main entry point for EP10.
 */
public class VMInterpreter {
    // Execution state
    private byte[] code;
    private Object[] constPool;
    private int dataSize;
    private int globalPointer;
    private Map<String, Integer> globalVariables;

    // Runtime data areas
    private int[] globals;
    private java.util.Stack<Object> stack = new java.util.Stack<>();
    private java.util.Stack<StackFrame> callStack = new java.util.Stack<>();

    public static void main(String[] args) throws Exception {
        String fileName = "src/main/resources/demo.vmasm";
        if (args.length > 0) fileName = args[0];

        System.out.println("=== EP10 Stack Machine ===");

        InputStream is;
        try { is = new FileInputStream(fileName); }
        catch (Exception e) { is = System.in; }

        VMInterpreter vm = new VMInterpreter();
        vm.assemble(is);
        vm.execute();

        System.out.println("\n=== Execution Complete ===");
    }

    public void assemble(InputStream is) throws Exception {
        CharStream cs = CharStreams.fromStream(is);
        VMAssemblerLexer lexer = new VMAssemblerLexer(cs);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        VMAssemblerParser parser = new VMAssemblerParser(tokens);

        ByteCodeAssembler assembler = new ByteCodeAssembler(BytecodeDefinition.instructions);
        ParseTreeWalker.DEFAULT.walk(assembler, parser.program());

        if (assembler.hasErrors()) {
            System.err.println("Assembly errors detected.");
            return;
        }

        this.code = assembler.getMachineCode();
        this.constPool = assembler.getConstantPool();
        this.dataSize = assembler.getDataSize();
        this.globalVariables = assembler.getGlobalVariables();
        this.globals = new int[dataSize > 0 ? dataSize : 0];
        this.globalPointer = dataSize > 0 ? 0 : -1;

        System.out.println("Assembled " + code.length + " bytes of bytecode.");
        System.out.println("Constant pool: " + constPool.length + " entries.");
        if (dataSize > 0) {
            System.out.println("Global data: " + dataSize + " slots.");
        }
    }

    public void execute() {
        FunctionSymbol mainFunc = findMainFunction();
        if (mainFunc == null) {
            // No main function — just execute from address 0
            System.out.println("No main() function. Executing from address 0.");
            interpret(0);
        } else {
            System.out.println("Executing main()...");
            callStack.push(new StackFrame(mainFunc, -1));
            interpret(mainFunc.address);
        }
    }

    private FunctionSymbol findMainFunction() {
        for (Object o : constPool) {
            if (o instanceof FunctionSymbol f && f.name.equals("main")) {
                return f;
            }
        }
        return null;
    }

    private void interpret(int startIp) {
        int ip = startIp;
        int codeSize = code.length;

        while (ip < codeSize) {
            int opcode = code[ip++] & 0xff;

            switch (opcode) {
                case BytecodeDefinition.INSTR_ICONST: {
                    int val = ByteCodeAssembler.getInt(code, ip); ip += 4;
                    stack.push(val);
                    break;
                }
                case BytecodeDefinition.INSTR_FCONST: {
                    int poolIdx = ByteCodeAssembler.getInt(code, ip); ip += 4;
                    stack.push(constPool[poolIdx]);
                    break;
                }
                case BytecodeDefinition.INSTR_SCONST: {
                    int poolIdx = ByteCodeAssembler.getInt(code, ip); ip += 4;
                    stack.push(constPool[poolIdx]);
                    break;
                }
                case BytecodeDefinition.INSTR_IADD: {
                    int b = (Integer) stack.pop();
                    int a = (Integer) stack.pop();
                    stack.push(a + b);
                    break;
                }
                case BytecodeDefinition.INSTR_ISUB: {
                    int b = (Integer) stack.pop();
                    int a = (Integer) stack.pop();
                    stack.push(a - b);
                    break;
                }
                case BytecodeDefinition.INSTR_IMUL: {
                    int b = (Integer) stack.pop();
                    int a = (Integer) stack.pop();
                    stack.push(a * b);
                    break;
                }
                case BytecodeDefinition.INSTR_IDIV: {
                    int b = (Integer) stack.pop();
                    int a = (Integer) stack.pop();
                    stack.push(a / b);
                    break;
                }
                case BytecodeDefinition.INSTR_INEG: {
                    int a = (Integer) stack.pop();
                    stack.push(-a);
                    break;
                }
                case BytecodeDefinition.INSTR_FADD: {
                    float b = ((Number) stack.pop()).floatValue();
                    float a = ((Number) stack.pop()).floatValue();
                    stack.push(a + b);
                    break;
                }
                case BytecodeDefinition.INSTR_FSUB: {
                    float b = ((Number) stack.pop()).floatValue();
                    float a = ((Number) stack.pop()).floatValue();
                    stack.push(a - b);
                    break;
                }
                case BytecodeDefinition.INSTR_FMUL: {
                    float b = ((Number) stack.pop()).floatValue();
                    float a = ((Number) stack.pop()).floatValue();
                    stack.push(a * b);
                    break;
                }
                case BytecodeDefinition.INSTR_ILT: {
                    int b = (Integer) stack.pop();
                    int a = (Integer) stack.pop();
                    stack.push(a < b ? 1 : 0);
                    break;
                }
                case BytecodeDefinition.INSTR_IEQ: {
                    int b = (Integer) stack.pop();
                    int a = (Integer) stack.pop();
                    stack.push(a == b ? 1 : 0);
                    break;
                }
                case BytecodeDefinition.INSTR_LOAD: {
                    int addr = ByteCodeAssembler.getInt(code, ip); ip += 4;
                    StackFrame frame = callStack.peek();
                    stack.push(frame.getLocal(addr));
                    break;
                }
                case BytecodeDefinition.INSTR_STORE: {
                    int addr = ByteCodeAssembler.getInt(code, ip); ip += 4;
                    Object val = stack.pop();
                    StackFrame frame = callStack.peek();
                    frame.setLocal(addr, val);
                    break;
                }
                case BytecodeDefinition.INSTR_GLOAD: {
                    int addr = ByteCodeAssembler.getInt(code, ip); ip += 4;
                    stack.push(globals[addr]);
                    break;
                }
                case BytecodeDefinition.INSTR_GSTORE: {
                    int addr = ByteCodeAssembler.getInt(code, ip); ip += 4;
                    globals[addr] = (Integer) stack.pop();
                    break;
                }
                case BytecodeDefinition.INSTR_PRINT: {
                    Object val = stack.pop();
                    if (val instanceof Float) {
                        System.out.println("  >> " + val);
                    } else {
                        System.out.println("  >> " + val);
                    }
                    break;
                }
                case BytecodeDefinition.INSTR_POP: {
                    stack.pop();
                    break;
                }
                case BytecodeDefinition.INSTR_BR: {
                    int target = ByteCodeAssembler.getInt(code, ip); ip += 4;
                    ip = target;
                    break;
                }
                case BytecodeDefinition.INSTR_BRT: {
                    int target = ByteCodeAssembler.getInt(code, ip); ip += 4;
                    int cond = (Integer) stack.pop();
                    if (cond != 0) ip = target;
                    break;
                }
                case BytecodeDefinition.INSTR_BRF: {
                    int target = ByteCodeAssembler.getInt(code, ip); ip += 4;
                    int cond = (Integer) stack.pop();
                    if (cond == 0) ip = target;
                    break;
                }
                case BytecodeDefinition.INSTR_CALL: {
                    int poolIdx = ByteCodeAssembler.getInt(code, ip); ip += 4;
                    FunctionSymbol func = (FunctionSymbol) constPool[poolIdx];
                    // Pop args from stack into new frame
                    StackFrame newFrame = new StackFrame(func, ip);
                    for (int i = func.nargs - 1; i >= 0; i--) {
                        newFrame.setLocal(i, stack.pop());
                    }
                    callStack.push(newFrame);
                    ip = func.address;
                    break;
                }
                case BytecodeDefinition.INSTR_RET: {
                    StackFrame frame = callStack.pop();
                    ip = frame.getReturnAddress();
                    if (ip == -1) return; // initial call returning
                    break;
                }
                case BytecodeDefinition.INSTR_HALT: {
                    System.out.println("  HALT");
                    return;
                }
                default: {
                    System.err.println("Unknown opcode: " + opcode + " at ip=" + (ip - 1));
                    return;
                }
            }
        }
    }
}
