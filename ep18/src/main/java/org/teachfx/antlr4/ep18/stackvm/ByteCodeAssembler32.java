package org.teachfx.antlr4.ep18.stackvm;

import org.antlr.v4.runtime.Token;
import org.teachfx.antlr4.ep18.parser.VMAssemblerBaseListener;
import org.teachfx.antlr4.ep18.parser.VMAssemblerParser;
import org.teachfx.antlr4.ep18.parser.VMAssemblerParser.FunctionDeclarationContext;
import org.teachfx.antlr4.ep18.parser.VMAssemblerParser.GlobalsContext;
import org.teachfx.antlr4.ep18.parser.VMAssemblerParser.GlobalVariableContext;
import org.teachfx.antlr4.ep18.parser.VMAssemblerParser.InstrContext;
import org.teachfx.antlr4.ep18.parser.VMAssemblerParser.LabelContext;
import org.teachfx.antlr4.ep18.parser.VMAssemblerParser.TempContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 32位定长指令汇编器
 * <p>
 * 使用统一指令格式: opcode(8) + rd(5) + rs1(5) + rs2(5) + imm(9)
 * </p>
 */
public class ByteCodeAssembler32 extends VMAssemblerBaseListener {
    public static final int INITIAL_CODE_SIZE = 2048;
    private static final int INT = VMAssemblerParser.INT;
    private static final int CHAR = VMAssemblerParser.CHAR;
    private static final int FLOAT = VMAssemblerParser.FLOAT;
    private static final int STRING = VMAssemblerParser.STRING;
    private static final int BOOL = VMAssemblerParser.BOOL;
    private static final int ID = VMAssemblerParser.ID;
    private static final int FUNC = VMAssemblerParser.FUNC;
    private static final int REG = VMAssemblerParser.REG;

    protected int ip = 0; // next instruction address
    protected int dataSize = 0;
    protected FunctionSymbol mainFunction;
    protected List<Object> constPool = new ArrayList<Object>();
    protected Map<String, Integer> instructionOpcodeMapping = new HashMap<String, Integer>();
    protected Map<String, LabelSymbol> labels = new HashMap<String, LabelSymbol>();
    protected Map<String, Integer> globalVariables = new HashMap<String, Integer>();
    private int[] code = new int[INITIAL_CODE_SIZE]; // 32位指令数组
    protected boolean hasErrors = false;
    private String currentInstruction;
    private boolean processingOperands = false;
    private VMType targetVM = VMType.EP18; // 默认EP18栈式VM

    /**
     * 目标VM类型
     */
    public enum VMType {
        EP18,   // 栈式VM
        EP18R   // 寄存器VM
    }

    public ByteCodeAssembler32(BytecodeDefinition.Instruction[] instructions) {
        for (int i = 1; i < instructions.length; ++i) {
            instructionOpcodeMapping.put(instructions[i].name.toLowerCase(), i);
        }
        initInstructionMapping();
    }

    public ByteCodeAssembler32(BytecodeDefinition.Instruction[] instructions, VMType vmType) {
        this(instructions);
        this.targetVM = vmType;
    }

    /**
     * 初始化32位指令操作码映射
     */
    private void initInstructionMapping() {
        // EP18栈式VM指令映射
        instructionOpcodeMapping.put("nop", InstructionEncoder.OP_NOP);
        instructionOpcodeMapping.put("halt", InstructionEncoder.OP_HALT);
        instructionOpcodeMapping.put("push", InstructionEncoder.OP_PUSH);
        instructionOpcodeMapping.put("pop", InstructionEncoder.OP_POP);
        instructionOpcodeMapping.put("dup", InstructionEncoder.OP_DUP);
        instructionOpcodeMapping.put("iload", InstructionEncoder.OP_ILOAD);
        instructionOpcodeMapping.put("istore", InstructionEncoder.OP_ISTORE);
        instructionOpcodeMapping.put("gload", InstructionEncoder.OP_GLOAD);
        instructionOpcodeMapping.put("gstore", InstructionEncoder.OP_GSTORE);
        instructionOpcodeMapping.put("iadd", InstructionEncoder.OP_IADD);
        instructionOpcodeMapping.put("isub", InstructionEncoder.OP_ISUB);
        instructionOpcodeMapping.put("imul", InstructionEncoder.OP_IMUL);
        instructionOpcodeMapping.put("idiv", InstructionEncoder.OP_IDIV);
        instructionOpcodeMapping.put("imod", InstructionEncoder.OP_IMOD);
        instructionOpcodeMapping.put("ineg", InstructionEncoder.OP_INEG);
        instructionOpcodeMapping.put("iand", InstructionEncoder.OP_IAND);
        instructionOpcodeMapping.put("ior", InstructionEncoder.OP_IOR);
        instructionOpcodeMapping.put("ixor", InstructionEncoder.OP_IXOR);
        instructionOpcodeMapping.put("icmp", InstructionEncoder.OP_ICMP);
        instructionOpcodeMapping.put("zcmp", InstructionEncoder.OP_ZCMP);
        instructionOpcodeMapping.put("beqz", InstructionEncoder.OP_BEQZ);
        instructionOpcodeMapping.put("bnez", InstructionEncoder.OP_BNEZ);
        instructionOpcodeMapping.put("j", InstructionEncoder.OP_J);
        instructionOpcodeMapping.put("call", InstructionEncoder.OP_CALL);
        instructionOpcodeMapping.put("ret", InstructionEncoder.OP_RET);
        instructionOpcodeMapping.put("print", InstructionEncoder.OP_PRINT);
        instructionOpcodeMapping.put("prints", InstructionEncoder.OP_PRINTS);

        // EP18R寄存器VM扩展指令映射
        instructionOpcodeMapping.put("mov", InstructionEncoder.OP_MOV);
        instructionOpcodeMapping.put("li", InstructionEncoder.OP_LI);
        instructionOpcodeMapping.put("lw", InstructionEncoder.OP_LW);
        instructionOpcodeMapping.put("sw", InstructionEncoder.OP_SW);
        instructionOpcodeMapping.put("add", InstructionEncoder.OP_ADD);
        instructionOpcodeMapping.put("sub", InstructionEncoder.OP_SUB);
        instructionOpcodeMapping.put("mul", InstructionEncoder.OP_MUL);
        instructionOpcodeMapping.put("div", InstructionEncoder.OP_DIV);
        instructionOpcodeMapping.put("slt", InstructionEncoder.OP_SLT);
        instructionOpcodeMapping.put("sle", InstructionEncoder.OP_SLE);
        instructionOpcodeMapping.put("sgt", InstructionEncoder.OP_SGT);
        instructionOpcodeMapping.put("sge", InstructionEncoder.OP_SGE);
        instructionOpcodeMapping.put("seq", InstructionEncoder.OP_SEQ);
        instructionOpcodeMapping.put("sne", InstructionEncoder.OP_SNE);
        instructionOpcodeMapping.put("jf", InstructionEncoder.OP_JF);
    }

    /**
     * 设置目标VM类型
     */
    public void setTargetVM(VMType vmType) {
        this.targetVM = vmType;
    }

    /**
     * 获取目标VM类型
     */
    public VMType getTargetVM() {
        return targetVM;
    }

    /**
     * 获取32位机器码
     *
     * @return 32位指令数组
     */
    public int[] getMachineCode() {
        int[] result = new int[ip];
        System.arraycopy(code, 0, result, 0, ip);
        return result;
    }

    /**
     * 获取字节形式的机器码（大端序）
     *
     * @return 字节数组
     */
    public byte[] getMachineCodeBytes() {
        byte[] result = new byte[ip * 4];
        for (int i = 0; i < ip; i++) {
            byte[] instrBytes = InstructionEncoder.toBytes(code[i]);
            result[i * 4] = instrBytes[0];
            result[i * 4 + 1] = instrBytes[1];
            result[i * 4 + 2] = instrBytes[2];
            result[i * 4 + 3] = instrBytes[3];
        }
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
     * 生成无操作数指令（EP18栈式VM）
     */
    protected void gen(Token instrToken) {
        String instructionName = instrToken.getText();
        currentInstruction = instructionName;
        Integer opCodeI = instructionOpcodeMapping.get(instructionName.toLowerCase());
        if (opCodeI == null) {
            System.err.println("line " + instrToken.getLine() + ": Unknown instruction: " + instructionName);
            hasErrors = true;
            currentInstruction = null;
            return;
        }

        int opcode = opCodeI.intValue();
        int instruction = InstructionEncoder.encode(opcode);
        ensureCapacity(ip + 1);
        code[ip++] = instruction;
        currentInstruction = null;
    }

    /**
     * 生成带立即数指令（EP18栈式VM）
     */
    protected void gen(Token instrToken, Token operandToken) {
        processingOperands = true;
        String instructionName = instrToken.getText();
        currentInstruction = instructionName;

        Integer opCodeI = instructionOpcodeMapping.get(instructionName.toLowerCase());
        if (opCodeI == null) {
            System.err.println("line " + instrToken.getLine() + ": Unknown instruction: " + instructionName);
            hasErrors = true;
            currentInstruction = null;
            return;
        }

        int opcode = opCodeI.intValue();
        int imm = parseImmediate(operandToken);
        int instruction = InstructionEncoder.encodeImm(opcode, imm);

        ensureCapacity(ip + 1);
        code[ip++] = instruction;
        processingOperands = false;
        currentInstruction = null;
    }

    /**
     * 生成双操作数指令（支持EP18R寄存器VM）
     */
    protected void gen(Token instrToken, Token oToken1, Token oToken2) {
        processingOperands = true;
        String instructionName = instrToken.getText();
        currentInstruction = instructionName;

        Integer opCodeI = instructionOpcodeMapping.get(instructionName.toLowerCase());
        if (opCodeI == null) {
            System.err.println("line " + instrToken.getLine() + ": Unknown instruction: " + instructionName);
            hasErrors = true;
            currentInstruction = null;
            return;
        }

        int opcode = opCodeI.intValue();
        int instruction;

        // 检查是否为寄存器指令
        if (isRegisterInstruction(instructionName)) {
            // EP18R寄存器格式: rd, rs 或 rd, imm
            int rd = parseRegister(oToken1);
            if (oToken2.getType() == REG) {
                int rs = parseRegister(oToken2);
                instruction = InstructionEncoder.encodeRegReg(opcode, rd, rs);
            } else {
                int imm = parseImmediate(oToken2);
                instruction = InstructionEncoder.encodeRegImm(opcode, rd, imm);
            }
        } else {
            // EP18栈式格式: 使用imm字段
            int imm = parseImmediate(oToken1);
            int imm2 = parseImmediate(oToken2);
            // 对于双立即数情况，组合到一个指令中
            instruction = InstructionEncoder.encodeImm(opcode, imm);
        }

        ensureCapacity(ip + 1);
        code[ip++] = instruction;
        processingOperands = false;
        currentInstruction = null;
    }

    /**
     * 生成三操作数指令（EP18R寄存器VM）
     */
    protected void gen(Token instrToken, Token oToken1, Token oToken2, Token oToken3) {
        processingOperands = true;
        String instructionName = instrToken.getText();
        currentInstruction = instructionName;

        Integer opCodeI = instructionOpcodeMapping.get(instructionName.toLowerCase());
        if (opCodeI == null) {
            System.err.println("line " + instrToken.getLine() + ": Unknown instruction: " + instructionName);
            hasErrors = true;
            currentInstruction = null;
            return;
        }

        int opcode = opCodeI.intValue();
        int instruction;

        if (isRegisterInstruction(instructionName)) {
            // EP18R寄存器格式: rd, rs1, rs2
            int rd = parseRegister(oToken1);
            int rs1 = parseRegister(oToken2);
            int rs2 = parseRegister(oToken3);
            instruction = InstructionEncoder.encodeRegRegReg(opcode, rd, rs1, rs2);
        } else {
            // EP18栈式格式
            int imm = parseImmediate(oToken1);
            int imm2 = parseImmediate(oToken2);
            int imm3 = parseImmediate(oToken3);
            instruction = InstructionEncoder.encodeImm(opcode, imm);
        }

        ensureCapacity(ip + 1);
        code[ip++] = instruction;
        processingOperands = false;
        currentInstruction = null;
    }

    /**
     * 检查是否为寄存器指令（EP18R）
     */
    private boolean isRegisterInstruction(String instructionName) {
        return switch (instructionName.toLowerCase()) {
            case "mov", "li", "lw", "sw", "add", "sub", "mul", "div",
                 "slt", "sle", "sgt", "sge", "seq", "sne", "jf" -> true;
            default -> false;
        };
    }

    /**
     * 解析立即数
     */
    private int parseImmediate(Token token) {
        String text = token.getText();
        switch (token.getType()) {
            case INT:
                return Integer.parseInt(text);
            case CHAR:
                return text.charAt(1);
            case ID:
                // 检查是否为标签
                LabelSymbol sym = labels.get(text);
                if (sym != null && sym.isDefined) {
                    return sym.address;
                }
                // 返回标签地址（可能需要前向引用处理）
                return getLabelAddress(text);
            default:
                return 0;
        }
    }

    /**
     * 解析寄存器编号
     */
    private int parseRegister(Token token) {
        if (token.getType() == REG) {
            String rs = token.getText();
            rs = rs.substring(1);
            return Integer.parseInt(rs);
        }
        return 0;
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
                hasErrors = true;
            }
        }
    }

    protected void defineFunction(Token idToken, int args, int locals) {
        String name = idToken.getText();
        FunctionSymbol f = new FunctionSymbol(name, args, locals, ip);

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
        if (index >= code.length) {
            int newSize = Math.max(index, code.length) * 2;
            int[] newer = new int[newSize];
            System.arraycopy(code, 0, newer, 0, code.length);
            code = newer;
        }
    }
}
