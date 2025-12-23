package org.teachfx.antlr4.ep18.stackvm;

import java.util.ArrayList;
import java.util.List;

/**
 * 32位定长指令反汇编器
 * <p>
 * 支持解析统一指令格式: opcode(8) + rd(5) + rs1(5) + rs2(5) + imm(9)
 * </p>
 */
public class DisAssembler {
    protected Object[] constPool;
    byte[] code;
    int codeSize;
    BytecodeDefinition def;
    private boolean use32BitFormat = true; // 默认使用32位格式

    public DisAssembler(byte[] code, int codeSize, Object[] constPool) {
        this.code = code;
        this.codeSize = codeSize;
        this.constPool = constPool;
    }

    public DisAssembler(byte[] code, int codeSize, Object[] constPool, boolean use32BitFormat) {
        this(code, codeSize, constPool);
        this.use32BitFormat = use32BitFormat;
    }

    public void disassemble() {
        System.out.println("Disassembly (32-bit format):");
        int i = 0;
        int instrCount = 0;
        while (i < codeSize) {
            int startAddr = i;
            i = disassembleInstruction(i);
            if (use32BitFormat) {
                // 32位格式：每条指令4字节
                int instruction = readInt32(code, startAddr);
                System.out.printf("  [0x%08X]", instruction);
            }
            System.out.println();
            instrCount++;
        }
        System.out.println();
        System.out.println("Total instructions: " + instrCount);
        System.out.println();
    }

    /**
     * 反汇编单条指令
     *
     * @param ip 指令指针
     * @return 下一条指令的地址
     */
    public int disassembleInstruction(int ip) {
        if (use32BitFormat) {
            return disassembleInstruction32(ip);
        } else {
            return disassembleInstructionLegacy(ip);
        }
    }

    /**
     * 32位定长格式反汇编
     */
    private int disassembleInstruction32(int ip) {
        // 边界检查：确保有足够的字节读取完整指令
        if (ip + 4 > codeSize) {
            System.out.printf("%04d: <INCOMPLETE>\n", ip);
            return codeSize;  // 返回codeSize以终止循环
        }
        int instruction = readInt32(code, ip);
        int opcode = InstructionEncoder.decodeOpcode(instruction);
        String instrName = InstructionEncoder.getMnemonic(opcode);

        // 检查是否为有效指令
        if (instrName.equals("unknown")) {
            System.out.printf("%04d:\t%-11s [UNKNOWN OPCODE 0x%02X]", ip, "?", opcode);
            return ip + 4;
        }

        System.out.printf("%04d:\t%-11s", ip, instrName);

        // 根据操作码判断指令类型并显示操作数
        List<String> operands = getOperands32(opcode, instruction);
        for (int i = 0; i < operands.size(); i++) {
            if (i > 0) System.out.print(", ");
            System.out.print(operands.get(i));
        }

        // 显示十六进制
        System.out.printf("  ; 0x%08X", instruction);

        return ip + 4;
    }

    /**
     * 从32位指令获取操作数
     */
    private List<String> getOperands32(int opcode, int instruction) {
        List<String> operands = new ArrayList<>();

        // EP18R寄存器VM指令
        if (opcode >= 0x60 && opcode <= 0x6E) {
            int rd = InstructionEncoder.decodeRd(instruction);
            int rs1 = InstructionEncoder.decodeRs1(instruction);
            int rs2 = InstructionEncoder.decodeRs2(instruction);
            int imm = InstructionEncoder.decodeImm(instruction);

            switch (opcode) {
                case InstructionEncoder.OP_MOV: // mov rd, rs
                    operands.add("r" + rd);
                    operands.add("r" + rs1);
                    break;
                case InstructionEncoder.OP_LI: // li rd, imm
                    operands.add("r" + rd);
                    operands.add(String.valueOf(imm));
                    break;
                case InstructionEncoder.OP_LW: // lw rd, offset(rs1)
                    operands.add("r" + rd);
                    operands.add(String.format("%d(r%d)", imm, rs1));
                    break;
                case InstructionEncoder.OP_SW: // sw rs2, offset(rd)
                    operands.add("r" + rs2);
                    operands.add(String.format("%d(r%d)", imm, rd));
                    break;
                case InstructionEncoder.OP_ADD: // add rd, rs1, rs2
                case InstructionEncoder.OP_SUB:
                case InstructionEncoder.OP_MUL:
                case InstructionEncoder.OP_DIV:
                case InstructionEncoder.OP_SLT:
                case InstructionEncoder.OP_SLE:
                case InstructionEncoder.OP_SGT:
                case InstructionEncoder.OP_SGE:
                case InstructionEncoder.OP_SEQ:
                case InstructionEncoder.OP_SNE:
                    operands.add("r" + rd);
                    operands.add("r" + rs1);
                    operands.add("r" + rs2);
                    break;
                case InstructionEncoder.OP_JF: // jf rs, offset
                    operands.add("r" + rs1);
                    operands.add(String.valueOf(imm));
                    break;
            }
        }
        // EP18栈式VM分支指令
        else if (opcode >= 0x40 && opcode <= 0x44) {
            int imm = InstructionEncoder.decodeImm(instruction);
            operands.add(String.valueOf(imm));
        }
        // EP18栈式VM push指令
        else if (opcode == InstructionEncoder.OP_PUSH) {
            int imm = InstructionEncoder.decodeImm(instruction);
            operands.add(String.valueOf(imm));
        }
        // EP18栈式VM load/store指令
        else if (opcode == InstructionEncoder.OP_ILOAD ||
                 opcode == InstructionEncoder.OP_ISTORE) {
            int imm = InstructionEncoder.decodeImm(instruction);
            operands.add(String.valueOf(imm));
        }

        return operands;
    }

    /**
     * 传统格式反汇编（兼容旧代码）
     */
    private int disassembleInstructionLegacy(int ip) {
        int opcode = code[ip];
        BytecodeDefinition.Instruction I = BytecodeDefinition.instructions[opcode];
        String instrName = I.name;
        System.out.printf("%04d:\t%-11s", ip, instrName);
        ip++;
        if (I.n == 0) {
            System.out.print("  ");
            return ip;
        }
        List<String> operands = new ArrayList<String>();
        for (int i = 0; i < I.n; i++) {
            int opnd = ByteCodeAssembler.getInt(code, ip);
            ip += 4;
            switch (I.type[i]) {
                case BytecodeDefinition.REG:
                    operands.add("r" + opnd);
                    break;
                case BytecodeDefinition.FUNC:
                case BytecodeDefinition.POOL:
                    operands.add(showConstPoolOperand(opnd));
                    break;
                case BytecodeDefinition.INT:
                    operands.add(String.valueOf(opnd));
            }
        }
        for (int i = 0; i < operands.size(); i++) {
            String s = operands.get(i);
            if (i > 0) System.out.print(", ");
            System.out.print(s);
        }
        return ip;
    }

    /**
     * 以十六进制格式反汇编（用于调试）
     */
    public void disassembleHex() {
        System.out.println("Hex Disassembly:");
        for (int i = 0; i < codeSize; i += 4) {
            if (i + 4 <= codeSize) {
                int instruction = readInt32(code, i);
                int opcode = InstructionEncoder.decodeOpcode(instruction);
                String instrName = InstructionEncoder.getMnemonic(opcode);
                System.out.printf("%04d: 0x%08X  %s", i, instruction, instrName);

                // 显示操作数
                List<String> operands = getOperands32(opcode, instruction);
                if (!operands.isEmpty()) {
                    System.out.print(" ");
                    for (int j = 0; j < operands.size(); j++) {
                        if (j > 0) System.out.print(", ");
                        System.out.print(operands.get(j));
                    }
                }
                System.out.println();
            } else {
                // 不完整的指令
                System.out.printf("%04d: ", i);
                for (; i < codeSize; i++) {
                    System.out.printf("%02X ", code[i]);
                }
                System.out.println(" [INCOMPLETE]");
            }
        }
        System.out.println();
    }

    /**
     * 解析字节数组为32位整数（大端序）
     */
    private int readInt32(byte[] bytes, int index) {
        return ((bytes[index] & 0xFF) << 24) |
               ((bytes[index + 1] & 0xFF) << 16) |
               ((bytes[index + 2] & 0xFF) << 8) |
               (bytes[index + 3] & 0xFF);
    }

    private String showConstPoolOperand(int poolIndex) {
        StringBuilder buf = new StringBuilder();
        buf.append("#");
        buf.append(poolIndex);
        String s = constPool[poolIndex].toString();
        if (constPool[poolIndex] instanceof String) s = '"' + s + '"';
        else if (constPool[poolIndex] instanceof FunctionSymbol fs) {
            s = fs.name + "()@" + fs.address;
        }
        buf.append(":");
        buf.append(s);

        return buf.toString();
    }

    /**
     * 验证指令编码
     *
     * @return 验证结果
     */
    public VerificationResult verify() {
        VerificationResult result = new VerificationResult();
        int i = 0;
        int instrCount = 0;

        while (i < codeSize) {
            if (i + 4 > codeSize) {
                result.addError("Incomplete instruction at offset " + i);
                break;
            }

            int instruction = readInt32(code, i);
            int opcode = InstructionEncoder.decodeOpcode(instruction);

            if (!InstructionEncoder.isValidOpcode(opcode)) {
                result.addError(String.format("Invalid opcode 0x%02X at offset %d", opcode, i));
            }

            // 验证寄存器字段
            int rd = InstructionEncoder.decodeRd(instruction);
            int rs1 = InstructionEncoder.decodeRs1(instruction);
            int rs2 = InstructionEncoder.decodeRs2(instruction);

            if (!InstructionEncoder.isValidRegister(rd)) {
                result.addError(String.format("Invalid rd=%d at offset %d", rd, i));
            }
            if (!InstructionEncoder.isValidRegister(rs1)) {
                result.addError(String.format("Invalid rs1=%d at offset %d", rs1, i));
            }
            if (!InstructionEncoder.isValidRegister(rs2)) {
                result.addError(String.format("Invalid rs2=%d at offset %d", rs2, i));
            }

            i += 4;
            instrCount++;
        }

        result.instructionCount = instrCount;
        result.valid = result.errors.isEmpty();
        return result;
    }

    /**
     * 验证结果
     */
    public static class VerificationResult {
        public boolean valid = true;
        public int instructionCount = 0;
        public List<String> errors = new ArrayList<>();

        public void addError(String error) {
            errors.add(error);
            valid = false;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Valid: %s, Instructions: %d%n", valid, instructionCount));
            if (!errors.isEmpty()) {
                sb.append("Errors:\n");
                for (String error : errors) {
                    sb.append("  - ").append(error).append("\n");
                }
            }
            return sb.toString();
        }
    }
}
