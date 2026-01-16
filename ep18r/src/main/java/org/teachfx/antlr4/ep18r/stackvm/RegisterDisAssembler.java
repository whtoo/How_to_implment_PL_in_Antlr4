package org.teachfx.antlr4.ep18r.stackvm;

import java.util.ArrayList;
import java.util.List;

import org.teachfx.antlr4.ep18r.stackvm.instructions.model.RegisterBytecodeDefinition;

/**
 * 寄存器虚拟机反汇编器
 * 将寄存器字节码反汇编为可读的汇编代码
 */
public class RegisterDisAssembler {
    protected Object[] constPool;
    protected byte[] code;
    protected int codeSize;
    protected RegisterBytecodeDefinition.Instruction[] instructions;

    public RegisterDisAssembler(byte[] code, int codeSize, Object[] constPool) {
        this.code = code;
        this.codeSize = codeSize;
        this.constPool = constPool;
        this.instructions = RegisterBytecodeDefinition.instructions;
    }

    /**
     * 反汇编整个代码段
     */
    public void disassemble() {
        System.out.println("Register VM Disassembly:");
        System.out.println("========================");
        int i = 0;
        while (i < codeSize) {
            i = disassembleInstruction(i);
            System.out.println();
        }
        System.out.println();
    }

    /**
     * 反汇编单条指令
     * @param ip 当前指令指针（字节偏移）
     * @return 下一条指令的指针
     */
    public int disassembleInstruction(int ip) {
        // 读取完整的32位指令字（大端序）
        int instructionWord = 0;
        if (ip + 3 < codeSize) {
            instructionWord = ((code[ip] & 0xFF) << 24) |
                            ((code[ip + 1] & 0xFF) << 16) |
                            ((code[ip + 2] & 0xFF) << 8) |
                            (code[ip + 3] & 0xFF);
        }
        ip += 4;

        // 从指令字中提取操作码（bits 31-26）
        int opcode = (instructionWord >> 26) & 0x3F;

        // 获取指令信息
        RegisterBytecodeDefinition.Instruction instr = null;
        if (opcode >= 0 && opcode < instructions.length) {
            instr = instructions[opcode];
        }

        if (instr == null) {
            System.out.printf("%04d: [INVALID opcode 0x%02x]", ip - 4, opcode);
            return ip;
        }

        // 打印指令地址和名称
        System.out.printf("%04d: %-8s", ip - 4, instr.name);

        if (instr.n == 0) {
            return ip;
        }

        int rd = 0, rs1 = 0, rs2 = 0, imm = 0;
        switch (instr.getFormat()) {
            case RegisterBytecodeDefinition.FORMAT_R:
                rd = (instructionWord >> 21) & 0x1F;
                rs1 = (instructionWord >> 16) & 0x1F;
                rs2 = (instructionWord >> 11) & 0x1F;
                break;
            case RegisterBytecodeDefinition.FORMAT_I:
                rd = (instructionWord >> 21) & 0x1F;
                rs1 = (instructionWord >> 16) & 0x1F;
                imm = instructionWord & 0xFFFF;
                if ((imm & 0x8000) != 0) {
                    imm |= 0xFFFF0000;
                }
                break;
            case RegisterBytecodeDefinition.FORMAT_J:
                imm = instructionWord & 0x3FFFFFF;
                if ((imm & 0x2000000) != 0) {
                    imm |= 0xFC000000;
                }
                break;
        }

        // 根据操作数类型生成显示字符串
        List<String> operands = new ArrayList<>();
        for (int i = 0; i < instr.n; i++) {
            int type = instr.getOperandType(i);
            switch (type) {
                case RegisterBytecodeDefinition.REG:
                    if (i == 0) operands.add("r" + rd);
                    else if (i == 1) operands.add("r" + rs1);
                    else if (i == 2) operands.add("r" + rs2);
                    break;
                case RegisterBytecodeDefinition.INT:
                    operands.add(String.valueOf(imm));
                    break;
                case RegisterBytecodeDefinition.POOL:
                    // 常量池引用
                    if (imm >= 0 && imm < constPool.length) {
                        Object constant = constPool[imm];
                        if (constant instanceof String) {
                            operands.add("\"" + constant + "\"");
                        } else if (constant instanceof Float) {
                            operands.add(constant.toString() + "f");
                        } else {
                            operands.add("#" + imm + ":" + constant);
                        }
                    } else {
                        operands.add("#" + imm + "[invalid]");
                    }
                    break;
                case RegisterBytecodeDefinition.FUNC:
                    // 函数引用
                    if (imm >= 0 && imm < constPool.length) {
                        Object constant = constPool[imm];
                        if (constant instanceof FunctionSymbol) {
                            FunctionSymbol fs = (FunctionSymbol) constant;
                            operands.add(fs.name + "()@" + fs.address);
                        } else {
                            operands.add("#" + imm + ":" + constant);
                        }
                    } else {
                        operands.add("func#" + imm);
                    }
                    break;
                default:
                    operands.add("?");
                    break;
            }
        }

        // 打印操作数
        for (int i = 0; i < operands.size(); i++) {
            if (i > 0) System.out.print(", ");
            System.out.print(operands.get(i));
        }

        return ip;
    }

    /**
     * 反汇编单条指令并返回字符串
     *
     * @param ip 当前指令指针（字节偏移）
     * @return 反汇编后的指令字符串
     */
    public String disassembleInstructionToString(int ip) {
        int startIp = ip;

        int instructionWord = 0;
        if (ip + 3 < codeSize) {
            instructionWord = ((code[ip] & 0xFF) << 24) |
                            ((code[ip + 1] & 0xFF) << 16) |
                            ((code[ip + 2] & 0xFF) << 8) |
                            (code[ip + 3] & 0xFF);
        }
        ip += 4;

        int opcode = (instructionWord >> 26) & 0x3F;

        RegisterBytecodeDefinition.Instruction instr = null;
        if (opcode >= 0 && opcode < instructions.length) {
            instr = instructions[opcode];
        }

        if (instr == null) {
            return String.format("%04d: [INVALID opcode 0x%02x]", startIp, opcode);
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%04d: %-8s", startIp, instr.name));

        if (instr.n == 0) {
            return sb.toString();
        }

        int rd = 0, rs1 = 0, rs2 = 0, imm = 0;
        switch (instr.getFormat()) {
            case RegisterBytecodeDefinition.FORMAT_R:
                rd = (instructionWord >> 21) & 0x1F;
                rs1 = (instructionWord >> 16) & 0x1F;
                rs2 = (instructionWord >> 11) & 0x1F;
                break;
            case RegisterBytecodeDefinition.FORMAT_I:
                rd = (instructionWord >> 21) & 0x1F;
                rs1 = (instructionWord >> 16) & 0x1F;
                imm = instructionWord & 0xFFFF;
                if ((imm & 0x8000) != 0) {
                    imm |= 0xFFFF0000;
                }
                break;
            case RegisterBytecodeDefinition.FORMAT_J:
                imm = instructionWord & 0x3FFFFFF;
                if ((imm & 0x2000000) != 0) {
                    imm |= 0xFC000000;
                }
                break;
        }

        List<String> operands = new ArrayList<>();
        for (int i = 0; i < instr.n; i++) {
            int type = instr.getOperandType(i);
            switch (type) {
                case RegisterBytecodeDefinition.REG:
                    if (i == 0) operands.add("r" + rd);
                    else if (i == 1) operands.add("r" + rs1);
                    else if (i == 2) operands.add("r" + rs2);
                    break;
                case RegisterBytecodeDefinition.INT:
                    operands.add(String.valueOf(imm));
                    break;
                case RegisterBytecodeDefinition.POOL:
                    if (imm >= 0 && imm < constPool.length) {
                        Object constant = constPool[imm];
                        if (constant instanceof String) {
                            operands.add("\"" + constant + "\"");
                        } else if (constant instanceof Float) {
                            operands.add(constant.toString() + "f");
                        } else {
                            operands.add("#" + imm + ":" + constant);
                        }
                    } else {
                        operands.add("#" + imm + "[invalid]");
                    }
                    break;
                case RegisterBytecodeDefinition.FUNC:
                    if (imm >= 0 && imm < constPool.length) {
                        Object constant = constPool[imm];
                        if (constant instanceof FunctionSymbol) {
                            FunctionSymbol fs = (FunctionSymbol) constant;
                            operands.add(fs.name + "()@" + fs.address);
                        } else {
                            operands.add("#" + imm + ":" + constant);
                        }
                    } else {
                        operands.add("func#" + imm);
                    }
                    break;
                default:
                    operands.add("?");
                    break;
            }
        }

        for (int i = 0; i < operands.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(operands.get(i));
        }

        return sb.toString();
    }

    /**
     * 反汇编并返回字符串（用于测试）
     */
    public String disassembleToString() {
        StringBuilder sb = new StringBuilder();

        int ip = 0;
        while (ip < codeSize) {
            int instructionWord = 0;
            int startIp = ip;

            if (ip + 3 < codeSize) {
                instructionWord = ((code[ip] & 0xFF) << 24) |
                                ((code[ip + 1] & 0xFF) << 16) |
                                ((code[ip + 2] & 0xFF) << 8) |
                                (code[ip + 3] & 0xFF);
            }
            ip += 4;

            int opcode = (instructionWord >> 26) & 0x3F;

            RegisterBytecodeDefinition.Instruction instr = null;
            if (opcode >= 0 && opcode < instructions.length) {
                instr = instructions[opcode];
            }

            if (instr == null) {
                sb.append(String.format("%04d: [INVALID opcode 0x%02x]", startIp, opcode));
                sb.append('\n');
                continue;
            }

            sb.append(String.format("%04d: %-8s", startIp, instr.name));

            if (instr.n > 0) {
                int rd = 0, rs1 = 0, rs2 = 0, imm = 0;
                switch (instr.getFormat()) {
                    case RegisterBytecodeDefinition.FORMAT_R:
                        rd = (instructionWord >> 21) & 0x1F;
                        rs1 = (instructionWord >> 16) & 0x1F;
                        rs2 = (instructionWord >> 11) & 0x1F;
                        sb.append(String.format("r%d, r%d, r%d", rd, rs1, rs2));
                        break;
                    case RegisterBytecodeDefinition.FORMAT_I:
                        rd = (instructionWord >> 21) & 0x1F;
                        rs1 = (instructionWord >> 16) & 0x1F;
                        imm = instructionWord & 0xFFFF;
                        if ((imm & 0x8000) != 0) {
                            imm |= 0xFFFF0000;
                        }
                        if (instr.n == 2) {
                            int type1 = instr.getOperandType(0);
                            int type2 = instr.getOperandType(1);
                            if (type1 == RegisterBytecodeDefinition.REG && type2 == RegisterBytecodeDefinition.REG) {
                                sb.append(String.format("r%d, r%d", rd, rs1));
                            } else {
                                sb.append(String.format("r%d, %d", rd, imm));
                            }
                        } else if (instr.n == 3) {
                            sb.append(String.format("r%d, r%d, %d", rd, rs1, imm));
                        } else {
                            sb.append(String.format("0x%04x", imm & 0xFFFF));
                        }
                        break;
                    case RegisterBytecodeDefinition.FORMAT_J:
                        imm = instructionWord & 0x3FFFFFF;
                        if ((imm & 0x2000000) != 0) {
                            imm |= 0xFC000000;
                        }
                        sb.append(String.format("%d", imm));
                        break;
                }
            }

            sb.append('\n');
        }

        return sb.toString();
    }
}