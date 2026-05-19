package org.teachfx.antlr4.ep10.stackvm;

import org.antlr.v4.runtime.Token;
import org.teachfx.antlr4.ep10.parser.VMAssemblerBaseListener;
import org.teachfx.antlr4.ep10.parser.VMAssemblerParser;

import java.util.*;

/**
 * ByteCodeAssembler — walks the VMAssembler parse tree and emits bytecode.
 * Handles: function declarations, labels (with forward refs), instructions,
 * global data, and the constant pool.
 */
public class ByteCodeAssembler extends VMAssemblerBaseListener {
    public static final int INITIAL_CODE_SIZE = 2048;

    protected int ip = 0;          // next instruction address
    protected int dataSize = 0;    // global data size
    protected FunctionSymbol mainFunction;

    protected List<Object> constPool = new ArrayList<>();
    protected Map<String, Integer> instructionOpcodeMapping = new HashMap<>();
    protected Map<String, LabelSymbol> labels = new HashMap<>();
    protected Map<String, Integer> globalVariables = new HashMap<>();

    private byte[] code = new byte[INITIAL_CODE_SIZE];
    protected boolean hasErrors = false;
    private String currentInstruction;
    private boolean processingOperands = false;

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

    // === Code generation (gen) ===
    protected void gen(Token instrToken) {
        String instrName = instrToken.getText();
        currentInstruction = instrName;
        Integer opCodeI = instructionOpcodeMapping.get(instrName);
        if (opCodeI == null) {
            System.err.println("line " + instrToken.getLine() + ": Unknown instruction: " + instrName);
            hasErrors = true;
            currentInstruction = null;
            return;
        }
        ensureCapacity(ip + 1);
        code[ip++] = (byte) (opCodeI.intValue() & 0xff);
        if (!processingOperands) {
            currentInstruction = null;
        }
    }

    protected void gen(Token instrToken, Token operandToken) {
        processingOperands = true;
        gen(instrToken);
        genOperand(operandToken);
        processingOperands = false;
        currentInstruction = null;
    }

    protected void gen(Token instrToken, Token o1, Token o2) {
        processingOperands = true;
        gen(instrToken);
        genOperand(o1);
        genOperand(o2);
        processingOperands = false;
        currentInstruction = null;
    }

    protected void gen(Token instrToken, Token o1, Token o2, Token o3) {
        processingOperands = true;
        gen(instrToken);
        genOperand(o1);
        genOperand(o2);
        genOperand(o3);
        processingOperands = false;
        currentInstruction = null;
    }

    public void genOperand(Token operandToken) {
        String text = operandToken.getText();
        int v = 0;
        switch (operandToken.getType()) {
            case VMAssemblerParser.INT:
                v = Integer.parseInt(text);
                break;
            case VMAssemblerParser.CHAR:
                v = text.charAt(1);
                break;
            case VMAssemblerParser.FLOAT:
                v = getConstantPoolIndex(Float.parseFloat(text));
                break;
            case VMAssemblerParser.BOOL:
                v = getConstantPoolIndex(Boolean.parseBoolean(text));
                break;
            case VMAssemblerParser.STRING:
                v = getConstantPoolIndex(text);
                break;
            case VMAssemblerParser.ID:
                if (currentInstruction != null && currentInstruction.equals("call")) {
                    v = getFunctionIndex(text);
                    break;
                }
                Integer globalAddr = globalVariables.get(text);
                if (globalAddr != null) {
                    v = globalAddr;
                } else {
                    v = getLabelAddress(text);
                }
                break;
            case VMAssemblerParser.FUNC:
                v = getFunctionIndex(text);
                break;
            case VMAssemblerParser.REG:
                v = getRegisterNumber(operandToken);
                break;
        }
        ensureCapacity(ip + 4);
        writeInt(code, ip, v);
        ip += 4;
    }

    // === Constant pool ===
    protected int getConstantPoolIndex(Object o) {
        if (constPool.contains(o)) return constPool.indexOf(o);
        constPool.add(o);
        return constPool.size() - 1;
    }

    // === Label handling ===
    protected int getLabelAddress(String id) {
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

    protected int getFunctionIndex(String id) {
        int i = constPool.indexOf(new FunctionSymbol(id));
        if (i >= 0) return i;
        return getConstantPoolIndex(new FunctionSymbol(id));
    }

    protected int getRegisterNumber(Token rToken) {
        String rs = rToken.getText();
        return Integer.parseInt(rs.substring(1));
    }

    protected void defineFunction(Token idToken, int args, int locals) {
        String name = idToken.getText();
        FunctionSymbol f = new FunctionSymbol(name, args, locals, ip);
        if (name.equals("main")) {
            mainFunction = f;
        }
        if (constPool.contains(f)) {
            constPool.set(constPool.indexOf(f), f);
        } else {
            getConstantPoolIndex(f);
        }
    }

    protected void defineDataSize(int n) {
        dataSize = n;
    }

    protected void defineLabel(Token idToken) {
        String id = idToken.getText();
        LabelSymbol sym = labels.get(id);
        if (sym == null) {
            labels.put(id, new LabelSymbol(id, ip, false));
        } else {
            if (sym.isForwardRef) {
                sym.isDefined = true;
                sym.address = ip;
                sym.resolveForwardReferences(code);
            } else {
                System.err.println("line " + idToken.getLine() + ": redefinition of symbol " + id);
            }
        }
    }

    protected void checkForUnresolvedReferences() {
        for (LabelSymbol sym : labels.values()) {
            if (!sym.isDefined) {
                System.err.println("unresolved reference: " + sym.getName());
            }
        }
    }

    // === ANTLR Listener overrides ===
    @Override
    public void exitGlobals(VMAssemblerParser.GlobalsContext ctx) {
        if (ctx.intVal != null) {
            defineDataSize(Integer.parseInt(ctx.intVal.getText()));
        }
    }

    @Override
    public void exitFunctionDeclaration(VMAssemblerParser.FunctionDeclarationContext ctx) {
        defineFunction(ctx.name,
            Integer.parseInt(ctx.a.getText()),
            Integer.parseInt(ctx.lo.getText()));
    }

    @Override
    public void exitInstr(VMAssemblerParser.InstrContext ctx) {
        if (ctx.op == null) return;
        List<VMAssemblerParser.TempContext> temps = ctx.temp();
        switch (temps.size()) {
            case 0 -> gen(ctx.op);
            case 1 -> gen(ctx.op, temps.get(0).start);
            case 2 -> gen(ctx.op, temps.get(0).start, temps.get(1).start);
            case 3 -> gen(ctx.op, temps.get(0).start, temps.get(1).start, temps.get(2).start);
        }
    }

    @Override
    public void exitLabel(VMAssemblerParser.LabelContext ctx) {
        defineLabel(ctx.start);
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
