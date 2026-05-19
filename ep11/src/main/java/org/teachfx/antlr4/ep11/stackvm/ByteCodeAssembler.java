package org.teachfx.antlr4.ep11.stackvm;

import java.util.*;

/**
 * ByteCodeAssembler — programmatic bytecode emission for the stack VM.
 * Provides methods to emit opcodes, operands, define labels, and manage
 * the constant pool. Used by CodeGenerator to translate IR to bytecode.
 */
public class ByteCodeAssembler {
    public static final int INITIAL_CODE_SIZE = 4096;

    protected int ip = 0;          // next instruction address
    protected int dataSize = 0;    // global data size
    protected FunctionSymbol mainFunction;

    protected List<Object> constPool = new ArrayList<>();
    protected Map<String, Integer> instructionOpcodeMapping = new HashMap<>();
    protected Map<String, LabelSymbol> labels = new HashMap<>();
    protected Map<String, Integer> globalVariables = new HashMap<>();

    private byte[] code = new byte[INITIAL_CODE_SIZE];
    protected boolean hasErrors = false;

    public ByteCodeAssembler(BytecodeDefinition.Instruction[] instructions) {
        for (int i = 1; i < instructions.length; ++i) {
            if (instructions[i] != null) {
                instructionOpcodeMapping.put(instructions[i].name.toLowerCase(), i);
            }
        }
    }

    // === Integer encoding (big-endian, 4 bytes) ===
    public static int getInt(byte[] memory, int index) {
        int b1 = memory[index++] & 0xff;
        int b2 = memory[index++] & 0xff;
        int b3 = memory[index++] & 0xff;
        int b4 = memory[index++] & 0xff;
        return b1 << (3 * 8) | b2 << (2 * 8) | b3 << 8 | b4;
    }

    public static void writeInt(byte[] bytes, int index, int value) {
        bytes[index]     = (byte) ((value >> (3 * 8)) & 0xff);
        bytes[index + 1] = (byte) ((value >> (2 * 8)) & 0xff);
        bytes[index + 2] = (byte) ((value >> 8) & 0xff);
        bytes[index + 3] = (byte) (value & 0xff);
    }

    // === Accessors ===
    public byte[] getMachineCode() {
        byte[] result = new byte[ip];
        System.arraycopy(code, 0, result, 0, ip);
        return result;
    }

    public int getCodeMemorySize() {
        return ip;
    }

    public int getDataSize() {
        return dataSize;
    }

    public FunctionSymbol getMainFunction() {
        return mainFunction;
    }

    public Object[] getConstantPool() {
        return constPool.toArray();
    }

    public Map<String, Integer> getGlobalVariables() {
        return new HashMap<>(globalVariables);
    }

    public boolean hasErrors() {
        checkForUnresolvedReferences();
        return hasErrors;
    }

    // === Programmatic code emission ===

    /** Emit a raw opcode byte (no operand). */
    public void emitOpcode(int opcode) {
        ensureCapacity(ip + 1);
        code[ip++] = (byte) (opcode & 0xff);
    }

    /** Emit an opcode followed by a 4-byte integer operand. */
    public void emitWithOperand(int opcode, int operand) {
        ensureCapacity(ip + 5);
        code[ip++] = (byte) (opcode & 0xff);
        writeInt(code, ip, operand);
        ip += 4;
    }

    /** Emit just a 4-byte integer operand (at current ip). */
    public void emitIntOperand(int operand) {
        ensureCapacity(ip + 4);
        writeInt(code, ip, operand);
        ip += 4;
    }

    /** Get the current instruction pointer (useful for label addresses). */
    public int currentIp() {
        return ip;
    }

    /** Patch a 4-byte value at a previously recorded address. */
    public void patchInt(int addr, int value) {
        writeInt(code, addr, value);
    }

    // === Constant pool ===
    public int getConstantPoolIndex(Object o) {
        if (constPool.contains(o)) return constPool.indexOf(o);
        constPool.add(o);
        return constPool.size() - 1;
    }

    // === Function handling ===
    public int getFunctionIndex(String id) {
        int i = constPool.indexOf(new FunctionSymbol(id));
        if (i >= 0) return i;
        return getConstantPoolIndex(new FunctionSymbol(id));
    }

    public void defineFunction(String name, int nargs, int nlocals, int address) {
        FunctionSymbol f = new FunctionSymbol(name, nargs, nlocals, address);
        if (name.equals("main")) {
            mainFunction = f;
        }
        if (constPool.contains(f)) {
            constPool.set(constPool.indexOf(f), f);
        } else {
            getConstantPoolIndex(f);
        }
    }

    public void updateFunctionAddress(String name, int address) {
        for (int i = 0; i < constPool.size(); i++) {
            Object o = constPool.get(i);
            if (o instanceof FunctionSymbol && ((FunctionSymbol) o).name.equals(name)) {
                FunctionSymbol old = (FunctionSymbol) o;
                constPool.set(i, new FunctionSymbol(name, old.nargs, old.nlocals, address));
                return;
            }
        }
    }

    public FunctionSymbol getFunctionByName(String name) {
        for (Object o : constPool) {
            if (o instanceof FunctionSymbol && ((FunctionSymbol) o).name.equals(name)) {
                return (FunctionSymbol) o;
            }
        }
        return null;
    }

    // === Label handling ===
    public int getLabelAddress(String id) {
        LabelSymbol sym = labels.get(id);
        if (sym == null) {
            sym = new LabelSymbol(id, ip, true);
            sym.isDefined = false;
            labels.put(id, sym);
        } else {
            if (!sym.isForwardRef) {
                return sym.address;
            }
            sym.addForwardRef(ip);
        }
        return 0;
    }

    public void defineLabel(String id) {
        LabelSymbol sym = labels.get(id);
        if (sym == null) {
            labels.put(id, new LabelSymbol(id, ip, false));
        } else {
            if (sym.isForwardRef) {
                sym.isDefined = true;
                sym.address = ip;
                sym.resolveForwardReferences(code);
            }
        }
    }

    protected void checkForUnresolvedReferences() {
        for (LabelSymbol sym : labels.values()) {
            if (!sym.isDefined) {
                System.err.println("unresolved reference: " + sym.getName());
                hasErrors = true;
            }
        }
    }

    // === Global data ===
    public void defineDataSize(int n) {
        dataSize = n;
    }

    public int defineGlobalVariable(String name) {
        int addr = dataSize;
        globalVariables.put(name, addr);
        dataSize++;
        return addr;
    }

    // === Disassembly for debugging ===
    public String disassemble() {
        StringBuilder sb = new StringBuilder();
        BytecodeDefinition.Instruction[] instrs = BytecodeDefinition.instructions;
        int i = 0;
        byte[] mc = getMachineCode();
        int size = mc.length;

        while (i < size) {
            int opcode = mc[i] & 0xff;
            sb.append(String.format("%04d: ", i));
            if (opcode < instrs.length && instrs[opcode] != null) {
                BytecodeDefinition.Instruction instr = instrs[opcode];
                sb.append(instr.name);
                i++;
                for (int j = 0; j < instr.n; j++) {
                    if (i + 3 < size) {
                        int operand = getInt(mc, i);
                        sb.append(" ").append(formatOperand(instr.type[j], operand));
                        i += 4;
                    }
                }
            } else {
                sb.append("UNKNOWN ").append(opcode);
                i++;
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private String formatOperand(int type, int value) {
        return switch (type) {
            case BytecodeDefinition.INT, BytecodeDefinition.POOL -> String.valueOf(value);
            case BytecodeDefinition.REG -> "r" + value;
            case BytecodeDefinition.FUNC -> {
                if (value < constPool.size() && constPool.get(value) instanceof FunctionSymbol f) {
                    yield f.name;
                }
                yield "func#" + value;
            }
            default -> String.valueOf(value);
        };
    }

    // === Helpers ===
    protected void ensureCapacity(int index) {
        if (index >= code.length) {
            int newSize = Math.max(index, code.length) * 2;
            byte[] newer = new byte[newSize];
            System.arraycopy(code, 0, newer, 0, code.length);
            code = newer;
        }
    }
}
