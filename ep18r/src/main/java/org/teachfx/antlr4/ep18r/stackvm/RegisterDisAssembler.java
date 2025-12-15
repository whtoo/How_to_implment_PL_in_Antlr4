package org.teachfx.antlr4.ep18r.stackvm;

import java.util.ArrayList;
import java.util.List;

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
        // 读取操作码（1字节）
        int opcode = code[ip] & 0xFF;
        ip++;

        // 读取操作数（4字节）
        int operand = 0;
        if (ip + 3 < codeSize) {
            operand = ((code[ip] & 0xFF) << 24) |
                      ((code[ip + 1] & 0xFF) << 16) |
                      ((code[ip + 2] & 0xFF) << 8) |
                      (code[ip + 3] & 0xFF);
        }
        ip += 4;

        // 获取指令信息
        RegisterBytecodeDefinition.Instruction instr = null;
        if (opcode >= 0 && opcode < instructions.length) {
            instr = instructions[opcode];
        }

        if (instr == null) {
            System.out.printf("%04d: [INVALID opcode 0x%02x]", ip - 5, opcode);
            return ip;
        }

        // 打印指令地址和名称
        System.out.printf("%04d: %-8s", ip - 5, instr.name);

        // 根据指令格式反汇编操作数
        if (instr.n == 0) {
            // 无操作数指令
            return ip;
        }

        // 提取操作数字段
        int rd = 0, rs1 = 0, rs2 = 0, imm = 0;
        switch (instr.getFormat()) {
            case RegisterBytecodeDefinition.FORMAT_R:
                // R类型: op rd, rs1, rs2
                rd = (operand >> 21) & 0x1F;
                rs1 = (operand >> 16) & 0x1F;
                rs2 = (operand >> 11) & 0x1F;
                break;
            case RegisterBytecodeDefinition.FORMAT_I:
                // I类型: op rd, rs1, imm 或 op rd, imm
                rd = (operand >> 21) & 0x1F;
                rs1 = (operand >> 16) & 0x1F;
                imm = operand & 0xFFFF;
                // 符号扩展
                if ((imm & 0x8000) != 0) {
                    imm |= 0xFFFF0000;
                }
                break;
            case RegisterBytecodeDefinition.FORMAT_J:
                // J类型: op imm
                imm = operand & 0x3FFFFFF;
                // 符号扩展
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
     * 反汇编并返回字符串（用于测试）
     */
    public String disassembleToString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Register VM Disassembly:\n");
        sb.append("========================\n");

        int ip = 0;
        while (ip < codeSize) {
            // 保存当前ip用于计算下一条指令
            int startIp = ip;

            // 读取操作码
            int opcode = code[ip] & 0xFF;
            ip++;

            // 读取操作数
            int operand = 0;
            if (ip + 3 < codeSize) {
                operand = ((code[ip] & 0xFF) << 24) |
                          ((code[ip + 1] & 0xFF) << 16) |
                          ((code[ip + 2] & 0xFF) << 8) |
                          (code[ip + 3] & 0xFF);
            }
            ip += 4;

            // 获取指令信息
            RegisterBytecodeDefinition.Instruction instr = null;
            if (opcode >= 0 && opcode < instructions.length) {
                instr = instructions[opcode];
            }

            if (instr == null) {
                sb.append(String.format("%04d: [INVALID opcode 0x%02x]\n", startIp, opcode));
                continue;
            }

            sb.append(String.format("%04d: %-8s", startIp, instr.name));

            if (instr.n > 0) {
                // 简化：只显示操作数值（实际实现应解码）
                sb.append(String.format("0x%08x", operand));
            }

            sb.append("\n");
        }

        return sb.toString();
    }
}