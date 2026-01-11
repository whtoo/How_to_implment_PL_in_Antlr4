package org.teachfx.antlr4.ep18r.pass.codegen;

import org.teachfx.antlr4.ep18r.stackvm.instructions.model.RegisterBytecodeDefinition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 寄存器VM代码生成器，将IR转换为32位固定格式的寄存器VM字节码。
 *
 * <p>EP18R 指令格式（32位固定长度，大端序）:
 * <ul>
 *   <li>R-type: [opcode:6][rd:5][rs1:5][rs2:5][unused:11] - 用于算术/逻辑指令</li>
 *   <li>I-type: [opcode:6][rd:5][rs1:5][imm:16] - 用于加载/存储/分支指令</li>
 *   <li>J-type: [opcode:6][imm:26] - 用于跳转指令</li>
 * </ul>
 *
 * <p>指令映射示例:
 * <pre>
 *   加法: a = b + c → add r2, r3, r4
 *   加载: lw r3, fp, 4 → 从fp+4加载到r3
 *   存储: sw r2, fp, 0 → 存储r2到fp+0
 *   常量: li r5, 42 → 加载立即数42到r5
 *   跳转: j loop → 无条件跳转到loop标签
 *   条件: jt r1, then → r1为真时跳转到then
 * </pre>
 *
 * @see ByteCodeEncoder
 * @see RegisterBytecodeDefinition
 */
public class RegisterAssembler {

    private final ByteCodeEncoder encoder;
    private final List<Integer> bytecode;
    private final Map<String, Integer> labelMap;
    private final List<LabelFixup> pendingFixups;
    private final IRegisterAllocator allocator;

    // 指令格式常量
    public static final int FORMAT_R = ByteCodeEncoder.FORMAT_R;
    public static final int FORMAT_I = ByteCodeEncoder.FORMAT_I;
    public static final int FORMAT_J = ByteCodeEncoder.FORMAT_J;

    // 特殊寄存器定义
    public static final int R0 = RegisterBytecodeDefinition.R0;   // 零寄存器
    public static final int R1 = RegisterBytecodeDefinition.R1;   // 返回地址 (ra)
    public static final int R2 = RegisterBytecodeDefinition.R2;   // 参数0/返回值 (a0)
    public static final int R3 = RegisterBytecodeDefinition.R3;   // 参数1 (a1)
    public static final int R4 = RegisterBytecodeDefinition.R4;   // 参数2 (a2)
    public static final int R5 = RegisterBytecodeDefinition.R5;   // 参数3 (a3)
    public static final int R6 = RegisterBytecodeDefinition.R6;   // 参数4 (a4)
    public static final int R7 = RegisterBytecodeDefinition.R7;   // 参数5 (a5)
    public static final int R8 = RegisterBytecodeDefinition.R8;   // 保存寄存器 (s0)
    public static final int R9 = RegisterBytecodeDefinition.R9;   // 保存寄存器 (s1)
    public static final int R13 = RegisterBytecodeDefinition.R13; // 栈指针 (sp)
    public static final int R14 = RegisterBytecodeDefinition.R14; // 帧指针 (fp)
    public static final int R15 = RegisterBytecodeDefinition.R15; // 链接寄存器 (lr)

    /**
     * 标签重定位信息
     */
    private static class LabelFixup {
        final String labelName;
        final int instructionOffset;
        final int format;

        LabelFixup(String labelName, int instructionOffset, int format) {
            this.labelName = labelName;
            this.instructionOffset = instructionOffset;
            this.format = format;
        }
    }

    /**
     * 构造一个新的RegisterAssembler。
     */
    public RegisterAssembler() {
        this.encoder = new ByteCodeEncoder();
        this.bytecode = new ArrayList<>();
        this.labelMap = new HashMap<>();
        this.pendingFixups = new ArrayList<>();
        this.allocator = new BasicRegisterAllocator();
    }

    /**
     * 构造一个新的RegisterAssembler，使用指定的寄存器分配器。
     *
     * @param allocator 寄存器分配器
     */
    public RegisterAssembler(IRegisterAllocator allocator) {
        this.encoder = new ByteCodeEncoder();
        this.bytecode = new ArrayList<>();
        this.labelMap = new HashMap<>();
        this.pendingFixups = new ArrayList<>();
        this.allocator = allocator;
    }

    // ========== R-type 指令生成 ==========

    /**
     * 生成R-type算术/逻辑指令: [opcode:6][rd:5][rs1:5][rs2:5][unused:11]
     *
     * @param opcode 操作码 (如INSTR_ADD, INSTR_SUB等)
     * @param rd     目标寄存器
     * @param rs1    源寄存器1
     * @param rs2    源寄存器2
     * @return 当前代码位置（用于后续修复）
     */
    public int emitRType(int opcode, int rd, int rs1, int rs2) {
        int instruction = encoder.encodeRType(opcode, rd, rs1, rs2);
        bytecode.add(instruction);
        return bytecode.size() - 1;
    }

    /**
     * 生成加法指令: add rd, rs1, rs2
     *
     * @param rd  目标寄存器，存储加法结果
     * @param rs1 源寄存器1，第一个操作数
     * @param rs2 源寄存器2，第二个操作数
     * @return 生成的指令在字节码中的位置索引
     */
    public int emitAdd(int rd, int rs1, int rs2) {
        return emitRType(RegisterBytecodeDefinition.INSTR_ADD, rd, rs1, rs2);
    }

    /**
     * 生成减法指令: sub rd, rs1, rs2
     *
     * @param rd  目标寄存器，存储减法结果
     * @param rs1 源寄存器1，被减数
     * @param rs2 源寄存器2，减数
     * @return 生成的指令在字节码中的位置索引
     */
    public int emitSub(int rd, int rs1, int rs2) {
        return emitRType(RegisterBytecodeDefinition.INSTR_SUB, rd, rs1, rs2);
    }

    /**
     * 生成乘法指令: mul rd, rs1, rs2
     *
     * @param rd  目标寄存器，存储乘法结果
     * @param rs1 源寄存器1，第一个乘数
     * @param rs2 源寄存器2，第二个乘数
     * @return 生成的指令在字节码中的位置索引
     */
    public int emitMul(int rd, int rs1, int rs2) {
        return emitRType(RegisterBytecodeDefinition.INSTR_MUL, rd, rs1, rs2);
    }

    /**
     * 生成除法指令: div rd, rs1, rs2
     *
     * @param rd  目标寄存器，存储除法结果（商）
     * @param rs1 源寄存器1，被除数
     * @param rs2 源寄存器2，除数
     * @return 生成的指令在字节码中的位置索引
     * @throws ArithmeticException 如果除数为0，在执行时抛出异常
     */
    public int emitDiv(int rd, int rs1, int rs2) {
        return emitRType(RegisterBytecodeDefinition.INSTR_DIV, rd, rs1, rs2);
    }

    /**
     * 生成比较小于指令: slt rd, rs1, rs2 (set if less than)
     *
     * @param rd  目标寄存器，如果rs1 < rs2则置为1，否则置为0
     * @param rs1 源寄存器1，第一个比较操作数
     * @param rs2 源寄存器2，第二个比较操作数
     * @return 生成的指令在字节码中的位置索引
     */
    public int emitSlt(int rd, int rs1, int rs2) {
        return emitRType(RegisterBytecodeDefinition.INSTR_SLT, rd, rs1, rs2);
    }

    /**
     * 生成比较小于等于指令: sle rd, rs1, rs2 (set if less or equal)
     *
     * @param rd  目标寄存器，如果rs1 ≤ rs2则置为1，否则置为0
     * @param rs1 源寄存器1，第一个比较操作数
     * @param rs2 源寄存器2，第二个比较操作数
     * @return 生成的指令在字节码中的位置索引
     */
    public int emitSle(int rd, int rs1, int rs2) {
        return emitRType(RegisterBytecodeDefinition.INSTR_SLE, rd, rs1, rs2);
    }

    /**
     * 生成比较大于指令: sgt rd, rs1, rs2 (set if greater than)
     *
     * @param rd  目标寄存器，如果rs1 > rs2则置为1，否则置为0
     * @param rs1 源寄存器1，第一个比较操作数
     * @param rs2 源寄存器2，第二个比较操作数
     * @return 生成的指令在字节码中的位置索引
     */
    public int emitSgt(int rd, int rs1, int rs2) {
        return emitRType(RegisterBytecodeDefinition.INSTR_SGT, rd, rs1, rs2);
    }

    /**
     * 生成比较大于等于指令: sge rd, rs1, rs2 (set if greater or equal)
     *
     * @param rd  目标寄存器，如果rs1 ≥ rs2则置为1，否则置为0
     * @param rs1 源寄存器1，第一个比较操作数
     * @param rs2 源寄存器2，第二个比较操作数
     * @return 生成的指令在字节码中的位置索引
     */
    public int emitSge(int rd, int rs1, int rs2) {
        return emitRType(RegisterBytecodeDefinition.INSTR_SGE, rd, rs1, rs2);
    }

    /**
     * 生成比较相等指令: seq rd, rs1, rs2 (set if equal)
     *
     * @param rd  目标寄存器，如果rs1 = rs2则置为1，否则置为0
     * @param rs1 源寄存器1，第一个比较操作数
     * @param rs2 源寄存器2，第二个比较操作数
     * @return 生成的指令在字节码中的位置索引
     */
    public int emitSeq(int rd, int rs1, int rs2) {
        return emitRType(RegisterBytecodeDefinition.INSTR_SEQ, rd, rs1, rs2);
    }

    /**
     * 生成比较不等指令: sne rd, rs1, rs2 (set if not equal)
     *
     * @param rd  目标寄存器，如果rs1 ≠ rs2则置为1，否则置为0
     * @param rs1 源寄存器1，第一个比较操作数
     * @param rs2 源寄存器2，第二个比较操作数
     * @return 生成的指令在字节码中的位置索引
     */
    public int emitSne(int rd, int rs1, int rs2) {
        return emitRType(RegisterBytecodeDefinition.INSTR_SNE, rd, rs1, rs2);
    }

    /**
     * 生成逻辑与指令: and rd, rs1, rs2
     *
     * @param rd  目标寄存器，存储按位与结果
     * @param rs1 源寄存器1，第一个操作数
     * @param rs2 源寄存器2，第二个操作数
     * @return 生成的指令在字节码中的位置索引
     */
    public int emitAnd(int rd, int rs1, int rs2) {
        return emitRType(RegisterBytecodeDefinition.INSTR_AND, rd, rs1, rs2);
    }

    /**
     * 生成逻辑或指令: or rd, rs1, rs2
     *
     * @param rd  目标寄存器，存储按位或结果
     * @param rs1 源寄存器1，第一个操作数
     * @param rs2 源寄存器2，第二个操作数
     * @return 生成的指令在字节码中的位置索引
     */
    public int emitOr(int rd, int rs1, int rs2) {
        return emitRType(RegisterBytecodeDefinition.INSTR_OR, rd, rs1, rs2);
    }

    /**
     * 生成逻辑异或指令: xor rd, rs1, rs2
     *
     * @param rd  目标寄存器，存储按位异或结果
     * @param rs1 源寄存器1，第一个操作数
     * @param rs2 源寄存器2，第二个操作数
     * @return 生成的指令在字节码中的位置索引
     */
    public int emitXor(int rd, int rs1, int rs2) {
        return emitRType(RegisterBytecodeDefinition.INSTR_XOR, rd, rs1, rs2);
    }

    // ========== I-type 指令生成 ==========

    /**
     * 生成I-type指令: [opcode:6][rd:5][rs1:5][imm:16]
     *
     * @param opcode 操作码
     * @param rd     目标寄存器
     * @param rs1    源寄存器1（或基址寄存器）
     * @param imm    16位有符号立即数
     * @return 当前代码位置
     */
    public int emitIType(int opcode, int rd, int rs1, int imm) {
        int instruction = encoder.encodeIType(opcode, rd, rs1, imm);
        bytecode.add(instruction);
        return bytecode.size() - 1;
    }

    /**
     * 生成加载立即数指令: li rd, imm
     *
     * @param rd  目标寄存器，存储立即数值
     * @param imm 16位有符号立即数
     * @return 生成的指令在字节码中的位置索引
     */
    public int emitLi(int rd, int imm) {
        return emitIType(RegisterBytecodeDefinition.INSTR_LI, rd, R0, imm);
    }

    /**
     * 生成加载字符指令: lc rd, imm
     *
     * @param rd 目标寄存器，存储字符的ASCII值
     * @param c  要加载的字符
     * @return 生成的指令在字节码中的位置索引
     */
    public int emitLc(int rd, char c) {
        return emitIType(RegisterBytecodeDefinition.INSTR_LC, rd, R0, c);
    }

    /**
     * 生成加载字指令: lw rd, base, offset
     *
     * @param rd     目标寄存器，存储从内存加载的值
     * @param base   基址寄存器，内存地址的基础
     * @param offset 16位有符号偏移量，相对于基址的偏移
     * @return 生成的指令在字节码中的位置索引
     */
    public int emitLw(int rd, int base, int offset) {
        return emitIType(RegisterBytecodeDefinition.INSTR_LW, rd, base, offset);
    }

    /**
     * 生成存储字指令: sw rs, base, offset
     * 注意：对于存储指令，rd字段实际存放的是源寄存器
     *
     * @param rs     源寄存器，要存储的值
     * @param base   基址寄存器，内存地址的基础
     * @param offset 16位有符号偏移量，相对于基址的偏移
     * @return 生成的指令在字节码中的位置索引
     */
    public int emitSw(int rs, int base, int offset) {
        return emitIType(RegisterBytecodeDefinition.INSTR_SW, rs, base, offset);
    }

    /**
     * 生成条件为真跳转指令: jt rs, target
     * 如果rs不为0，跳转到target
     */
    public int emitJt(int rs, String target) {
        int pos = emitIType(RegisterBytecodeDefinition.INSTR_JT, R0, rs, 0);
        pendingFixups.add(new LabelFixup(target, pos, FORMAT_I));
        return pos;
    }

    /**
     * 生成条件为假跳转指令: jf rs, target
     * 如果rs为0，跳转到target
     */
    public int emitJf(int rs, String target) {
        int pos = emitIType(RegisterBytecodeDefinition.INSTR_JF, R0, rs, 0);
        pendingFixups.add(new LabelFixup(target, pos, FORMAT_I));
        return pos;
    }

    /**
     * 生成mov指令: mov rd, rs
     * 使用add rd, rs, r0实现
     */
    public int emitMov(int rd, int rs) {
        return emitRType(RegisterBytecodeDefinition.INSTR_ADD, rd, rs, R0);
    }

    /**
     * 生成取负指令: neg rd, rs
     */
    public int emitNeg(int rd, int rs) {
        return emitRType(RegisterBytecodeDefinition.INSTR_NEG, rd, rs, R0);
    }

    /**
     * 生成逻辑非指令: not rd, rs
     */
    public int emitNot(int rd, int rs) {
        return emitRType(RegisterBytecodeDefinition.INSTR_NOT, rd, rs, R0);
    }

    // ========== J-type 指令生成 ==========

    /**
     * 生成J-type指令: [opcode:6][imm:26]
     *
     * @param opcode 操作码
     * @param imm    26位有符号立即数（跳转目标）
     * @return 当前代码位置
     */
    public int emitJType(int opcode, int imm) {
        int instruction = encoder.encodeJType(opcode, imm);
        bytecode.add(instruction);
        return bytecode.size() - 1;
    }

    /**
     * 生成无条件跳转指令: j target
     */
    public int emitJ(String target) {
        int pos = emitJType(RegisterBytecodeDefinition.INSTR_J, 0);
        pendingFixups.add(new LabelFixup(target, pos, FORMAT_J));
        return pos;
    }

    /**
     * 生成函数调用指令: call target
     */
    public int emitCall(String target) {
        int pos = emitJType(RegisterBytecodeDefinition.INSTR_CALL, 0);
        pendingFixups.add(new LabelFixup(target, pos, FORMAT_J));
        return pos;
    }

    /**
     * 生成返回指令: ret
     */
    public int emitRet() {
        return emitJType(RegisterBytecodeDefinition.INSTR_RET, 0);
    }

    /**
     * 生成停机指令: halt
     */
    public int emitHalt() {
        return emitJType(RegisterBytecodeDefinition.INSTR_HALT, 0);
    }

    // ========== 标签管理 ==========

    /**
     * 定义一个标签位置。
     *
     * @param label 标签名
     */
    public void defineLabel(String label) {
        int address = bytecode.size() * 4;  // 每条指令4字节
        labelMap.put(label, address);

        // 修复之前引用此标签的待处理跳转指令
        fixupLabel(label, address);
    }

    /**
     * 修复引用指定标签的所有待处理跳转指令。
     */
    private void fixupLabel(String label, int targetAddress) {
        List<LabelFixup> remaining = new ArrayList<>();

        for (LabelFixup fixup : pendingFixups) {
            if (fixup.labelName.equals(label)) {
                int instructionIndex = fixup.instructionOffset;
                int currentInstruction = bytecode.get(instructionIndex);

                if (fixup.format == FORMAT_J) {
                    // J-type: 计算相对偏移量
                    int currentAddress = instructionIndex * 4;
                    int offset = (targetAddress - currentAddress) / 4;  // 转换为指令数
                    bytecode.set(instructionIndex, encoder.encodeJType(
                        encoder.extractOpcode(currentInstruction), offset));
                } else if (fixup.format == FORMAT_I) {
                    // I-type (jt/jf): 计算相对偏移量
                    int currentAddress = instructionIndex * 4;
                    int offset = (targetAddress - currentAddress) / 4;
                    int rs1 = encoder.extractRs1(currentInstruction);
                    bytecode.set(instructionIndex, encoder.encodeIType(
                        encoder.extractOpcode(currentInstruction), R0, rs1, offset));
                }
            } else {
                remaining.add(fixup);
            }
        }

        pendingFixups.clear();
        pendingFixups.addAll(remaining);
    }

    // ========== 字节码输出 ==========

    /**
     * 获取生成的字节码列表。
     *
     * @return 32位指令字列表
     */
    public List<Integer> getBytecode() {
        return new ArrayList<>(bytecode);
    }

    /**
     * 获取编码后的字节数组（大端序）。
     *
     * @return 字节数组，每条指令4字节
     */
    public byte[] toByteArray() {
        byte[] result = new byte[bytecode.size() * 4];
        for (int i = 0; i < bytecode.size(); i++) {
            byte[] encoded = encoder.encode(bytecode.get(i));
            System.arraycopy(encoded, 0, result, i * 4, 4);
        }
        return result;
    }

    /**
     * 获取当前代码大小（字节数）。
     *
     * @return 代码大小
     */
    public int getCodeSize() {
        return bytecode.size() * 4;
    }

    /**
     * 获取当前指令数量。
     *
     * @return 指令数量
     */
    public int getInstructionCount() {
        return bytecode.size();
    }

    /**
     * 检查是否有未修复的标签引用。
     *
     * @return 如果有未修复的标签返回true
     */
    public boolean hasUnresolvedLabels() {
        return !pendingFixups.isEmpty();
    }

    /**
     * 获取未修复的标签列表。
     *
     * @return 未修复标签名称列表
     */
    public List<String> getUnresolvedLabels() {
        List<String> result = new ArrayList<>();
        for (LabelFixup fixup : pendingFixups) {
            if (!result.contains(fixup.labelName)) {
                result.add(fixup.labelName);
            }
        }
        return result;
    }

    /**
     * 重置汇编器状态，清空所有代码和标签。
     */
    public void reset() {
        bytecode.clear();
        labelMap.clear();
        pendingFixups.clear();
        allocator.reset();
    }

    /**
     * 获取汇编格式的代码（用于调试）。
     *
     * @return 汇编格式字符串列表
     */
    public List<String> toAssemblyList() {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < bytecode.size(); i++) {
            String label = "";
            int address = i * 4;
            for (Map.Entry<String, Integer> entry : labelMap.entrySet()) {
                if (entry.getValue() == address) {
                    label = entry.getKey() + ":";
                    break;
                }
            }
            if (!label.isEmpty()) {
                result.add(label);
            }
            result.add(String.format("  %s", encoder.toAssemblyString(bytecode.get(i))));
        }
        return result;
    }

    /**
     * 打印汇编格式到标准输出（用于调试）。
     */
    public void printAssembly() {
        for (String line : toAssemblyList()) {
            System.out.println(line);
        }
    }

    // ========== 寄存器分配器接口 ==========

    /**
     * 寄存器分配器接口。
     */
    public interface IRegisterAllocator {
        /**
         * 为变量分配一个寄存器。
         *
         * @param varName 变量名
         * @return 寄存器编号
         */
        int allocate(String varName);

        /**
         * 释放寄存器。
         *
         * @param reg 寄存器编号
         */
        void free(int reg);

        /**
         * 获取变量对应的寄存器编号。
         *
         * @param varName 变量名
         * @return 寄存器编号，如果未分配返回-1
         */
        int getRegister(String varName);

        /**
         * 重置分配器状态。
         */
        void reset();
    }

    /**
     * 基础寄存器分配器实现。
     * 使用简单的线性分配策略，分配完所有可用寄存器后溢出到栈。
     */
    public static class BasicRegisterAllocator implements IRegisterAllocator {
        private final Map<String, Integer> varToReg;
        private final List<Integer> freeRegs;
        private static final int NUM_REGS = 16;
        private static final int FIRST_ALLOCATABLE = 2;  // R0和R1保留

        public BasicRegisterAllocator() {
            varToReg = new HashMap<>();
            freeRegs = new ArrayList<>();
            reset();
        }

        @Override
        public int allocate(String varName) {
            // 检查是否已分配
            Integer existing = varToReg.get(varName);
            if (existing != null) {
                return existing;
            }

            // 分配新寄存器
            if (!freeRegs.isEmpty()) {
                int reg = freeRegs.remove(0);
                varToReg.put(varName, reg);
                return reg;
            }

            // 寄存器用尽，返回-1表示需要溢出处理
            return -1;
        }

        @Override
        public void free(int reg) {
            // 移除使用此寄存器的变量映射
            varToReg.entrySet().removeIf(entry -> entry.getValue() == reg);
            // 将寄存器放回空闲列表
            if (reg >= FIRST_ALLOCATABLE && reg < NUM_REGS && !freeRegs.contains(reg)) {
                freeRegs.add(reg);
            }
        }

        @Override
        public int getRegister(String varName) {
            return varToReg.getOrDefault(varName, -1);
        }

        @Override
        public void reset() {
            varToReg.clear();
            freeRegs.clear();
            // 初始化可分配寄存器列表（R2-R15）
            for (int i = FIRST_ALLOCATABLE; i < NUM_REGS; i++) {
                freeRegs.add(i);
            }
        }
    }
}
