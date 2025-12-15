package org.teachfx.antlr4.ep18r.stackvm;

import org.antlr.v4.runtime.Token;
import org.teachfx.antlr4.ep18r.parser.VMAssemblerBaseListener;
import org.teachfx.antlr4.ep18r.parser.VMAssemblerParser;
import org.teachfx.antlr4.ep18r.parser.VMAssemblerParser.FunctionDeclarationContext;
import org.teachfx.antlr4.ep18r.parser.VMAssemblerParser.GlobalsContext;
import org.teachfx.antlr4.ep18r.parser.VMAssemblerParser.GlobalVariableContext;
import org.teachfx.antlr4.ep18r.parser.VMAssemblerParser.InstrContext;
import org.teachfx.antlr4.ep18r.parser.VMAssemblerParser.LabelContext;
import org.teachfx.antlr4.ep18r.parser.VMAssemblerParser.TempContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegisterByteCodeAssembler extends VMAssemblerBaseListener {
    public static final int INITIAL_CODE_SIZE = 2048;
    private static final int INT = VMAssemblerParser.INT;
    private static final int CHAR = VMAssemblerParser.CHAR;
    private static final int FLOAT = VMAssemblerParser.FLOAT;
    private static final int STRING = VMAssemblerParser.STRING;
    private static final int BOOL = VMAssemblerParser.BOOL;
    private static final int ID = VMAssemblerParser.ID;
    private static final int FUNC = VMAssemblerParser.FUNC;
    private static final int REG = VMAssemblerParser.REG;
    protected int ip = 0; // next instruction address to be executed;
    protected int dataSize = 0;
    protected FunctionSymbol mainFunction;
    protected List<Object> constPool = new ArrayList<Object>();
    protected Map<String, Integer> instructionOpcodeMapping = new HashMap<String, Integer>();
    protected Map<String, LabelSymbol> labels = new HashMap<String, LabelSymbol>();
    protected Map<String, Integer> globalVariables = new HashMap<String, Integer>(); // Map of global variable names to addresses
    private byte[] code = new byte[INITIAL_CODE_SIZE];
    protected boolean hasErrors = false;
    private String currentInstruction;
    private boolean processingOperands = false;

    // 用于32位固定长度指令编码
    private int currentOpcode = 0;
    private int currentInstructionWord = 0;
    private int currentOperandIndex = 0;
    private RegisterBytecodeDefinition.Instruction currentInstructionDef = null;


    public RegisterByteCodeAssembler(RegisterBytecodeDefinition.Instruction[] instructions) {
        for (int i = 1; i < instructions.length; ++i) {
            if (instructions[i] != null) {
                String name = instructions[i].name.toLowerCase();
                instructionOpcodeMapping.put(name, i);
                // 调试输出
                System.out.println("Mapped instruction: " + name + " -> " + i);
            }
        }
        System.out.println("Total instructions mapped: " + instructionOpcodeMapping.size());
    }

    public static int getInt(byte[] memory, int index) {
        int b1 = memory[index++] & 0xff;
        int b2 = memory[index++] & 0xff;
        int b3 = memory[index++] & 0xff;
        int b4 = memory[index++] & 0xff;
        int word = b1 << (3 * 8) | b2 << (2 * 8) | b3 << 8 | b4;
        return word;
    }

    public static void writeInt(byte[] bytes, int index, int value) {
        bytes[index] = (byte) ((value >> (3 * 8)) & 0xff);
        bytes[index + 1] = (byte) ((value >> (2 * 8)) & 0xff);
        bytes[index + 2] = (byte) ((value >> (8)) & 0xff);
        bytes[index + 3] = (byte) ((value & 0xff));
    }

    public byte[] getMachineCode() {
        // Return a properly-sized array instead of the potentially oversized internal buffer
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

    /**
     * 开始新指令编码
     */
    private void startNewInstruction(String instructionName, Token instrToken) {
        String key = instructionName.toLowerCase();
        currentInstruction = key;
        Integer opCodeI = instructionOpcodeMapping.get(key);
        if (opCodeI == null) {
            System.err.println("line " + instrToken.getLine() + ": Unknown instruction: " + instructionName);
            System.err.println("  Available instructions: " + instructionOpcodeMapping.keySet());
            hasErrors = true;
            currentInstruction = null;
            currentInstructionDef = null;
            return;
        }

        currentOpcode = opCodeI.intValue();
        currentInstructionDef = RegisterBytecodeDefinition.instructions[currentOpcode];
        currentInstructionWord = 0;
        currentOperandIndex = 0;

        // 将操作码放入bits 31-26 (6位)
        currentInstructionWord |= (currentOpcode << 26) & 0xFC000000;
        System.out.println("[DEBUG] startNewInstruction: " + instructionName + " -> opcode=" + currentOpcode + " instructionWord=" + Integer.toHexString(currentInstructionWord));
    }

    /**
     * 完成当前指令编码并写入代码内存
     */
    private void completeCurrentInstruction() {
        if (currentInstructionDef == null || hasErrors) {
            return;
        }

        ensureCapacity(ip + 4);
        // 写入32位指令字（大端序）
        code[ip] = (byte) ((currentInstructionWord >> 24) & 0xFF);
        code[ip + 1] = (byte) ((currentInstructionWord >> 16) & 0xFF);
        code[ip + 2] = (byte) ((currentInstructionWord >> 8) & 0xFF);
        code[ip + 3] = (byte) (currentInstructionWord & 0xFF);
        System.out.println("[DEBUG] completeCurrentInstruction: ip=" + ip + " instructionWord=" + Integer.toHexString(currentInstructionWord) + " bytes=" +
            String.format("%02x %02x %02x %02x", code[ip] & 0xFF, code[ip+1] & 0xFF, code[ip+2] & 0xFF, code[ip+3] & 0xFF));
        ip += 4;

        // 重置状态
        currentInstruction = null;
        currentInstructionDef = null;
        currentOpcode = 0;
        currentInstructionWord = 0;
        currentOperandIndex = 0;
    }

    /**
     * 添加操作数到当前指令
     */
    private void addOperand(int value, Token operandToken) {
        if (currentInstructionDef == null || hasErrors) {
            return;
        }

        if (currentOperandIndex >= currentInstructionDef.n) {
            System.err.println("line " + operandToken.getLine() +
                ": Too many operands for instruction " + currentInstruction);
            hasErrors = true;
            return;
        }

        int format = currentInstructionDef.getFormat();
        int operandType = currentInstructionDef.getOperandType(currentOperandIndex);

        // 根据指令格式和操作数位置设置字段
        if (format == RegisterBytecodeDefinition.FORMAT_R) {
            // R类型: op rd, rs1, rs2
            // 字段位置: rd在bits 25-21, rs1在20-16, rs2在15-11
            if (currentOperandIndex == 0) {
                // rd: bits 25-21
                currentInstructionWord |= (value & 0x1F) << 21;
            } else if (currentOperandIndex == 1) {
                // rs1: bits 20-16
                currentInstructionWord |= (value & 0x1F) << 16;
            } else if (currentOperandIndex == 2) {
                // rs2: bits 15-11
                currentInstructionWord |= (value & 0x1F) << 11;
            }
        } else if (format == RegisterBytecodeDefinition.FORMAT_I) {
            // I类型: op rd, rs1, imm
            // 字段位置: rd在25-21, rs1在20-16, imm在15-0
            if (currentOperandIndex == 0) {
                // rd: bits 25-21
                currentInstructionWord |= (value & 0x1F) << 21;
            } else if (currentOperandIndex == 1) {
                // 第二个操作数：可能是rs1或立即数
                if (operandType == RegisterBytecodeDefinition.REG) {
                    // rs1: bits 20-16
                    currentInstructionWord |= (value & 0x1F) << 16;
                } else {
                    // 立即数: bits 15-0 (如LI指令)
                    currentInstructionWord |= (value & 0xFFFF) << 0;
                }
            } else if (currentOperandIndex == 2) {
                // 第三个操作数：立即数（对于lw/sw等）
                currentInstructionWord |= (value & 0xFFFF) << 0;
            }
        } else if (format == RegisterBytecodeDefinition.FORMAT_J) {
            // J类型: op imm
            // 字段位置: imm在25-0
            if (currentOperandIndex == 0) {
                currentInstructionWord |= (value & 0x3FFFFFF) << 0;
            }
        }

        currentOperandIndex++;

        // 检查是否所有操作数都已添加
        if (currentInstructionDef != null && currentOperandIndex == currentInstructionDef.n) {
            completeCurrentInstruction();
        }
    }

    protected void gen(Token instrToken) {
        startNewInstruction(instrToken.getText(), instrToken);
        if (hasErrors) {
            return;
        }
        // 无操作数指令，直接完成
        if (currentInstructionDef != null && currentInstructionDef.n == 0) {
            completeCurrentInstruction();
        }
    }

    protected void gen(Token instrToken, Token operandToken) {
        processingOperands = true;
        gen(instrToken);
        genOperand(operandToken);
        processingOperands = false;
        currentInstruction = null;
    }

    protected void gen(Token instrToken, Token oToken1, Token oToken2) {
        processingOperands = true;
        gen(instrToken);
        genOperand(oToken1);
        genOperand(oToken2);
        processingOperands = false;
        currentInstruction = null;
    }

    protected void gen(Token instrToken, Token oToken1, Token oToken2, Token oToken3) {
        processingOperands = true;
        gen(instrToken);
        genOperand(oToken1);
        genOperand(oToken2);
        genOperand(oToken3);
        processingOperands = false;
        currentInstruction = null;
    }

    public void genOperand(Token operandToken) {
        String text = operandToken.getText();
        int v = 0;
        switch (operandToken.getType()) {
            case INT:
                v = Integer.valueOf(text);
                break;
            case CHAR:
                v = Character.valueOf(text.charAt(1));
                break;
            case FLOAT:
                v = getConstantPoolIndex(Float.valueOf(text));
                break;
            case BOOL:
                v = getConstantPoolIndex(Boolean.parseBoolean(text));
                break;
            case STRING:
                v = getConstantPoolIndex(String.valueOf(text));
                break;
            case ID:
                // If current instruction is "call", treat as function reference
                if (currentInstruction != null && currentInstruction.equals("call")) {
                    v = getFunctionIndex(text);
                    break;
                }
                // First check if it's a global variable
                Integer globalAddr = globalVariables.get(text);
                if (globalAddr != null) {
                    v = globalAddr;
                } else {
                    // Otherwise treat it as a label
                    v = getLabelAddress(text);
                }
                break;
            case FUNC:
                v = getFunctionIndex(text);
                break;
            case REG:
                v = getRegisterNumber(operandToken);
                break;
        }
        addOperand(v, operandToken);
    }

    protected int getConstantPoolIndex(Object o) {
        if (constPool.contains(o)) return constPool.indexOf(o);
        constPool.add(o);
        return constPool.size() - 1;
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

    protected int getRegisterNumber(Token rToken) {
        String rs = rToken.getText();
        rs = rs.substring(1);
        return Integer.valueOf(rs);
    }

    protected int getLabelAddress(String id) {
        LabelSymbol sym = labels.get(id);
        if (sym == null) {
            sym = new LabelSymbol(id, ip, true);
            sym.isDefined = false;
            labels.put(id, sym);
        } else {
            if (sym.isForwardRef) {
                sym.addForwardRef(ip);
            } else {
                return sym.address;
            }
        }

        return 0;
    }

    protected int getFunctionIndex(String id) {
        int i = constPool.indexOf(new FunctionSymbol(id));
        if (i >= 0) return i;
        return getConstantPoolIndex(new FunctionSymbol(id));
    }

    protected void checkForUnresolvedReferences() {
        for (String name : labels.keySet()) {
            LabelSymbol sym = labels.get(name);
            if (!sym.isDefined) {
                System.err.println("unresolved reference: " + name);
            }
        }
    }

    protected void defineFunction(Token idToken, int args, int locals) {
        String name = idToken.getText();
        FunctionSymbol f = new FunctionSymbol(name, args, locals, ip);

        // Set mainFunction as soon as we encounter main
        if (name.equals("main")) {
            mainFunction = f;
        }

        if (constPool.contains(f)) constPool.set(constPool.indexOf(f), f);
        else getConstantPoolIndex(f);
    }

    protected void defineDataSize(int n) {
        dataSize = n;
    }

    protected void defineLabel(Token idToken) {
        String id = idToken.getText();
        LabelSymbol sym = labels.get(id);
        if (sym == null) {
            LabelSymbol csym = new LabelSymbol(id, ip, false);
            labels.put(id, csym);
        } else {
            if (sym.isForwardRef) {
                sym.isDefined = true;
                sym.address = ip;
                sym.resolveForwardReferences(code);
            } else {
                // redefinition of symbol
                System.err.println("line " + idToken.getLine() +
                        ": redefinition of symbol " + id);
            }
        }
    }

    public String generate() {
        return "";
    }

    @Override
    public void exitProgram(VMAssemblerParser.ProgramContext ctx) {
        checkForUnresolvedReferences();
    }

    @Override
    public void exitGlobals(GlobalsContext ctx) {
        if (ctx.intVal != null) {
            defineDataSize(Integer.valueOf(ctx.intVal.getText()));
        }
    }

    @Override
    public void exitGlobalVariable(GlobalVariableContext ctx) {
        // Allocate space for global variable
        String varName = ctx.name.getText();
        globalVariables.put(varName, dataSize++);
    }

    @Override
    public void exitFunctionDeclaration(FunctionDeclarationContext ctx) {
        defineFunction(ctx.name, Integer.valueOf(ctx.a.getText()), Integer.valueOf(ctx.lo.getText()));
    }

    @Override
    public void exitInstr(InstrContext ctx) {
        if (ctx.op == null) return;

        List<TempContext> temps = ctx.temp();
        switch (temps.size()) {
            case 0:
                gen(ctx.op);
                break;
            case 1:
                gen(ctx.op, temps.get(0).start);
                break;
            case 2:
                gen(ctx.op, temps.get(0).start, temps.get(1).start);
                break;
            case 3:
                gen(ctx.op, temps.get(0).start, temps.get(1).start, temps.get(2).start);
                break;
        }
    }

    @Override
    public void enterLabel(LabelContext ctx) {
        defineLabel(ctx.start);
    }

    protected void ensureCapacity(int index) {
        // Double strategy
        if (index >= code.length) {
            int newSize = Math.max(index, code.length) * 2;
            byte[] newer = new byte[newSize];
            System.arraycopy(code, 0, newer, 0, code.length);
            code = newer;
        }
    }
}
