package org.teachfx.antlr4.ep11.stackvm;

/**
 * VMInterpreter — executes bytecode produced by ByteCodeAssembler.
 * Stand-alone interpreter with no ANTLR parser dependency.
 */
public class VMInterpreter {
    // Execution state
    private byte[] code;
    private Object[] constPool;
    private int dataSize;
    private int globalPointer;
    private java.util.Map<String, Integer> globalVariables;

    // Runtime data areas
    private int[] globals;
    private java.util.Stack<Object> stack = new java.util.Stack<>();
    private java.util.Stack<StackFrame> callStack = new java.util.Stack<>();

    public VMInterpreter() {}

    /** Initialize from a fully-populated assembler. */
    public void init(ByteCodeAssembler assembler) {
        this.code = assembler.getMachineCode();
        this.constPool = assembler.getConstantPool();
        this.dataSize = assembler.getDataSize();
        this.globalVariables = assembler.getGlobalVariables();
        this.globals = new int[dataSize > 0 ? dataSize : 0];
        this.globalPointer = dataSize > 0 ? 0 : -1;
    }

    public void execute() {
        FunctionSymbol mainFunc = findMainFunction();
        if (mainFunc == null) {
            System.out.println("No main() function. Executing from address 0.");
            interpret(0);
        } else {
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

    /** Get the top-of-stack value after execution (i.e., return value). */
    public Object getResult() {
        return stack.isEmpty() ? null : stack.peek();
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
                case BytecodeDefinition.INSTR_CCONST: {
                    int val = ByteCodeAssembler.getInt(code, ip); ip += 4;
                    stack.push((char) val);
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
                case BytecodeDefinition.INSTR_INOT: {
                    int a = (Integer) stack.pop();
                    stack.push(a == 0 ? 1 : 0);
                    break;
                }
                case BytecodeDefinition.INSTR_IAND: {
                    int b = (Integer) stack.pop();
                    int a = (Integer) stack.pop();
                    stack.push(a != 0 && b != 0 ? 1 : 0);
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
                case BytecodeDefinition.INSTR_ILE: {
                    int b = (Integer) stack.pop();
                    int a = (Integer) stack.pop();
                    stack.push(a <= b ? 1 : 0);
                    break;
                }
                case BytecodeDefinition.INSTR_IGT: {
                    int b = (Integer) stack.pop();
                    int a = (Integer) stack.pop();
                    stack.push(a > b ? 1 : 0);
                    break;
                }
                case BytecodeDefinition.INSTR_IGE: {
                    int b = (Integer) stack.pop();
                    int a = (Integer) stack.pop();
                    stack.push(a >= b ? 1 : 0);
                    break;
                }
                case BytecodeDefinition.INSTR_IEQ: {
                    int b = (Integer) stack.pop();
                    int a = (Integer) stack.pop();
                    stack.push(a == b ? 1 : 0);
                    break;
                }
                case BytecodeDefinition.INSTR_INE: {
                    int b = (Integer) stack.pop();
                    int a = (Integer) stack.pop();
                    stack.push(a != b ? 1 : 0);
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
                    System.out.println("  >> " + val);
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
                    if (ip == -1) return;
                    break;
                }
                case BytecodeDefinition.INSTR_HALT: {
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
