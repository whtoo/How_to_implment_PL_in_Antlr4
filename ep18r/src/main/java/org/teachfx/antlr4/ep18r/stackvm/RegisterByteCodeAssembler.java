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
import org.teachfx.antlr4.ep18r.stackvm.instructions.model.RegisterBytecodeDefinition;
import org.teachfx.antlr4.ep18r.stackvm.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegisterByteCodeAssembler extends VMAssemblerBaseListener {
    private final Logger logger = Logger.getLogger(RegisterByteCodeAssembler.class);
    
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
            }
        }
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
            logger.error("line %d: Unknown instruction: %s", instrToken.getLine(), instructionName);
            logger.error("  Available instructions: %s", instructionOpcodeMapping.keySet());
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
            logger.error("line %d: Too many operands for instruction %s", 
                operandToken.getLine(), currentInstruction);
            hasErrors = true;
            return;
        }

        int format = currentInstructionDef.getFormat();
        int operandType = currentInstructionDef.getOperandType(currentOperandIndex);

        // 特殊处理：jt/jf 指令格式为 (REG, INT)，但第一个寄存器应放入 rs1 字段
        // 因为 jt/jf 没有目标寄存器 rd，只有条件寄存器和跳转目标
        boolean isConditionalJump = currentInstruction != null &&
            (currentInstruction.equals("jt") || currentInstruction.equals("jf"));

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
            if (isConditionalJump) {
                // jt/jf 特殊处理: 第一个操作数是条件寄存器，放入 rs1 字段
                if (currentOperandIndex == 0) {
                    // rs1: bits 20-16 (条件寄存器)
                    currentInstructionWord |= (value & 0x1F) << 16;
                } else if (currentOperandIndex == 1) {
                    // 立即数: bits 15-0 (跳转目标)
                    currentInstructionWord |= (value & 0xFFFF) << 0;
                }
            } else {
                // 标准 I 类型处理
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
                    v = getFunctionAddress(text);
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
                v = getFunctionAddress(text);
                break;
            case REG:
            case VMAssemblerParser.ZERO:
            case VMAssemblerParser.RA:
            case VMAssemblerParser.A0:
            case VMAssemblerParser.A1:
            case VMAssemblerParser.A2:
            case VMAssemblerParser.A3:
            case VMAssemblerParser.A4:
            case VMAssemblerParser.A5:
            case VMAssemblerParser.S0:
            case VMAssemblerParser.S1:
            case VMAssemblerParser.S2:
            case VMAssemblerParser.S3:
            case VMAssemblerParser.S4:
            case VMAssemblerParser.SP:
            case VMAssemblerParser.FP:
            case VMAssemblerParser.LR:
            case VMAssemblerParser.T0:
            case VMAssemblerParser.T1:
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
        String rs = rToken.getText().toLowerCase();
        int tokenType = rToken.getType();

        // 数字寄存器：r0-r15
        if (tokenType == VMAssemblerParser.REG) {
            rs = rs.substring(1);
            return Integer.valueOf(rs);
        }

        // ABI寄存器别名映射
        switch (tokenType) {
            case VMAssemblerParser.ZERO: return 0;
            case VMAssemblerParser.RA:   return 1;
            case VMAssemblerParser.A0:   return 2;
            case VMAssemblerParser.A1:   return 3;
            case VMAssemblerParser.A2:   return 4;
            case VMAssemblerParser.A3:   return 5;
            case VMAssemblerParser.A4:   return 6;
            case VMAssemblerParser.A5:   return 7;
            case VMAssemblerParser.S0:   return 8;
            case VMAssemblerParser.S1:   return 9;
            case VMAssemblerParser.S2:   return 10;
            case VMAssemblerParser.S3:   return 11;
            case VMAssemblerParser.S4:   return 12;
            case VMAssemblerParser.SP:   return 13;
            case VMAssemblerParser.FP:   return 14;
            case VMAssemblerParser.LR:   return 15;

            // 临时寄存器别名（映射到调用者保存寄存器）
            case VMAssemblerParser.T0:   return 2; // t0 -> a0
            case VMAssemblerParser.T1:   return 3; // t1 -> a1

            default:
                throw new IllegalArgumentException("Unknown register token: " + rs + " (type=" + tokenType + ")");
        }
    }

    protected int getLabelAddress(String id) {
        // 判断当前指令是否为J类型（call、j指令）
        boolean isJType = currentInstructionDef != null &&
            currentInstructionDef.getFormat() == RegisterBytecodeDefinition.FORMAT_J;

        LabelSymbol sym = labels.get(id);
        if (sym == null) {
            sym = new LabelSymbol(id, ip, true);
            sym.isDefined = false;
            // 修正：添加前向引用时需要传递类型信息
            sym.forwardRefs.clear(); // 清除构造函数中添加的默认引用
            sym.addForwardRef(ip, isJType);
            labels.put(id, sym);
        } else {
            if (sym.isForwardRef) {
                sym.addForwardRef(ip, isJType);
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

    /**
     * 获取函数的代码地址（用于call指令）
     * 支持前向引用：如果函数尚未定义，使用标签系统处理
     */
    protected int getFunctionAddress(String funcName) {
        // 首先检查常量池中是否有该函数的定义
        for (Object obj : constPool) {
            if (obj instanceof FunctionSymbol) {
                FunctionSymbol fs = (FunctionSymbol) obj;
                if (fs.name.equals(funcName) && fs.address > 0) {
                    return fs.address;
                }
            }
        }
        // 函数未定义，使用标签系统处理前向引用
        return getLabelAddress(funcName);
    }

    protected void checkForUnresolvedReferences() {
        for (String name : labels.keySet()) {
            LabelSymbol sym = labels.get(name);
            if (!sym.isDefined) {
                logger.error("unresolved reference: %s", name);
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

        // 同时定义一个同名标签，用于解析函数的前向引用（call指令）
        defineLabel(idToken);
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
                logger.error("line %d: redefinition of symbol %s", 
                        idToken.getLine(), id);
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
