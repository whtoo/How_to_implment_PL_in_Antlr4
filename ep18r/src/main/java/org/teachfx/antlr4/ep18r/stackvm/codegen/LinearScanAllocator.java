package org.teachfx.antlr4.ep18r.stackvm.codegen;

import org.teachfx.antlr4.ep18r.stackvm.StackOffsets;
import org.teachfx.antlr4.ep21.ir.expr.VarSlot;
import org.teachfx.antlr4.ep18r.symtab.symbol.VariableSymbol;
import org.teachfx.antlr4.ep21.analysis.dataflow.ReachingDefinitionAnalysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 活跃区间：表示变量的活跃范围
 */
class LiveInterval {
    private final VarSlot variable;
    private final int start;
    private final int end;

    public LiveInterval(VarSlot variable, int start, int end) {
        if (start > end) {
            throw new IllegalArgumentException("Start must be less than or equal to end");
        }
        this.variable = variable;
        this.start = start;
        this.end = end;
    }

    public VarSlot getVariable() {
        return variable;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public int getLength() {
        return end - start;
    }

    public boolean overlaps(LiveInterval other) {
        return !(this.end < other.start || this.start > other.end);
    }

    public boolean contains(int position) {
        return position >= start && position < end;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof LiveInterval)) return false;
        LiveInterval other = (LiveInterval) obj;
        return variable.equals(other.variable);
    }

    @Override
    public int hashCode() {
        return variable != null ? variable.hashCode() : 0;
    }

    @Override
    public String toString() {
        return String.format("[%d, %d]: %s", start, end, variable);
    }
}

/**
 * 线性扫描寄存器分配器
 *
 * <p>实现线性扫描寄存器分配算法，将虚拟寄存器（变量）映射到物理寄存器。
 * 当物理寄存器不足时，支持溢出到栈。</p>
 *
 * <p>EP18R 寄存器规范（16个物理寄存器）：</p>
 * <pre>
 * R0  (zero) - 零寄存器，恒为0，只读，不可分配
 * R1  (ra)   - 返回地址/临时值，调用者保存
 * R2  (a0)   - 参数1/返回值，调用者保存
 * R3  (a1)   - 参数2，调用者保存
 * R4  (a2)   - 参数3，调用者保存
 * R5  (a3)   - 参数4，调用者保存
 * R6  (a4)   - 参数5，调用者保存
 * R7  (a5)   - 参数6，调用者保存
 * R8  (s0)   - 被调用者保存寄存器
 * R9  (s1)   - 被调用者保存寄存器
 * R10 (s2)   - 被调用者保存寄存器
 * R11 (s3)   - 被调用者保存寄存器
 * R12 (s4)   - 被调用者保存寄存器
 * R13 (sp)   - 栈指针，不可分配
 * R14 (fp)   - 帧指针，不可分配
 * R15 (lr)   - 链接寄存器，调用者保存
 * </pre>
 *
 * <p>可分配寄存器：R1-R12, R15（共13个，不包括R0, R13, R14）</p>
 * <p>推荐优先使用被调用者保存寄存器：R8-R12（s0-s4）</p>
 *
 * @author EP18R Register VM Team
 * @version 1.0
 */
public class LinearScanAllocator implements IRegisterAllocator {

    // ==================== 寄存器常量 ====================

    /** 寄存器总数 */
    private static final int NUM_REGISTERS = 16;

    /** 零寄存器（不可分配） */
    private static final int R0 = 0;

    /** 栈指针（不可分配） */
    private static final int R13 = 13;

    /** 帧指针（不可分配） */
    private static final int R14 = 14;

    /** 被调用者保存寄存器起始编号（s0） */
    private static final int S0_START = 8;

    /** 被调用者保存寄存器结束编号（s4） */
    private static final int S4_END = 12;

    /** 链接寄存器 */
    private static final int R15 = 15;

    /** 调用者保存寄存器（ra, a0-a5, lr）: R1-R7, R15 */
    private static final int[] CALLER_SAVED_REGS = {1, 2, 3, 4, 5, 6, 7, 15};

    /** 被调用者保存寄存器（s0-s4）: R8-R12 */
    private static final int[] CALLEE_SAVED_REGS = {8, 9, 10, 11, 12};

    /** 不可分配的寄存器: R0, R13, R14 */
    private static final int[] RESERVED_REGS = {0, 13, 14};

    // ==================== 实例字段 ====================

    /** 物理寄存器状态：0=空闲，1=占用 */
    private final int[] physicalRegs;

    /** 变量到物理寄存器的映射 */
    private final Map<String, Integer> varToReg;

    /** 变量到栈溢出槽位的映射 */
    private final Map<String, Integer> spillSlots;

    /** 寄存器到变量的反向映射 */
    private final Map<Integer, String> regToVar;

    /** 下一个可用的溢出槽位（从0开始，负数表示相对于fp的偏移） */
    private int nextSpillSlot;

    /** 分配策略：优先使用被调用者保存寄存器 */
    private final boolean preferCalleeSaved;

    // ==================== 构造函数 ====================

    /**
     * 创建默认配置的线性扫描寄存器分配器
     * 优先使用被调用者保存寄存器（s0-s4）
     */
    public LinearScanAllocator() {
        this(true);
    }

    /**
     * 创建线性扫描寄存器分配器
     *
     * @param preferCalleeSaved 是否优先使用被调用者保存寄存器
     */
    public LinearScanAllocator(boolean preferCalleeSaved) {
        this.physicalRegs = new int[NUM_REGISTERS];
        this.varToReg = new HashMap<>();
        this.spillSlots = new HashMap<>();
        this.regToVar = new HashMap<>();
        this.nextSpillSlot = 0;
        this.preferCalleeSaved = preferCalleeSaved;
        reset();
    }

    // ==================== 核心分配方法 ====================

    @Override
    public int allocate(String varName) {
        // 检查变量是否已分配
        if (varToReg.containsKey(varName)) {
            return varToReg.get(varName);
        }

        // 查找可用的物理寄存器
        int reg = findAvailableRegister();
        if (reg == -1) {
            // 没有可用寄存器，从已分配的变量中选择一个进行溢出
            // 选择第一个已分配的变量（不在spillSlots中的）进行溢出
            String varToSpill = null;
            for (Map.Entry<String, Integer> entry : varToReg.entrySet()) {
                if (!spillSlots.containsKey(entry.getKey())) {
                    varToSpill = entry.getKey();
                    break;
                }
            }

            if (varToSpill != null) {
                // 溢出该变量，释放其寄存器
                spillToStack(varToSpill);
                // 现在可以找到可用寄存器
                reg = findAvailableRegister();
            }
        }

        // 如果仍然没有可用寄存器，将新变量溢出到栈
        if (reg == -1) {
            spillToStack(varName);
            return -1;  // 返回-1表示在栈上
        }

        // 标记寄存器为占用
        physicalRegs[reg] = 1;
        varToReg.put(varName, reg);
        regToVar.put(reg, varName);

        return reg;
    }

    @Override
    public void free(String varName) {
        Integer reg = varToReg.get(varName);
        if (reg != null) {
            // 释放物理寄存器
            physicalRegs[reg] = 0;
            varToReg.remove(varName);
            regToVar.remove(reg);
        }

        // 检查是否需要释放溢出槽位
        if (spillSlots.containsKey(varName)) {
            spillSlots.remove(varName);
        }
    }

    @Override
    public int getRegister(String varName) {
        Integer reg = varToReg.get(varName);
        return reg != null ? reg : -1;
    }

    @Override
    public Map<String, Integer> getAllocation() {
        return Collections.unmodifiableMap(varToReg);
    }

    @Override
    public void reset() {
        // 重置所有寄存器为空闲
        for (int i = 0; i < NUM_REGISTERS; i++) {
            physicalRegs[i] = 0;
        }

        // 清空映射表
        varToReg.clear();
        spillSlots.clear();
        regToVar.clear();
        nextSpillSlot = 0;
    }

    @Override
    public int spillToStack(String varName) {
        // 如果变量已溢出，返回现有槽位
        if (spillSlots.containsKey(varName)) {
            return spillSlots.get(varName);
        }

        // 检查变量是否在寄存器中
        Integer reg = varToReg.get(varName);
        if (reg != null) {
            // 释放寄存器
            physicalRegs[reg] = 0;
            varToReg.remove(varName);
            regToVar.remove(reg);
        }

        // 分配新的栈槽位（相对于fp，向下增长）
        // 第一个溢出槽位在 fp-16，第二个在 fp-20，依此类推
        int slotOffset = StackOffsets.FIRST_LOCAL_OFFSET - (nextSpillSlot * 4);
        spillSlots.put(varName, slotOffset);
        nextSpillSlot++;

        return slotOffset;
    }

    @Override
    public boolean isSpilled(String varName) {
        return spillSlots.containsKey(varName);
    }

    @Override
    public int getSpillSlot(String varName) {
        Integer slot = spillSlots.get(varName);
        return slot != null ? slot : -1;
    }

    /**
     * 获取溢出槽位的数量
     *
     * @return 溢出槽位数量
     */
    public int getSpillSlotCount() {
        return spillSlots.size();
    }

    @Override
    public int getAvailableRegisterCount() {
        int count = 0;
        for (int i = 0; i < NUM_REGISTERS; i++) {
            if (!isReserved(i) && physicalRegs[i] == 0) {
                count++;
            }
        }
        return count;
    }

    @Override
    public int getAllocatedRegisterCount() {
        return varToReg.size();
    }

    // ==================== 内部辅助方法 ====================

    /**
     * 查找可用的物理寄存器
     *
     * @return 可用寄存器编号，如果没有可用寄存器则返回-1
     */
    private int findAvailableRegister() {
        if (preferCalleeSaved) {
            // 优先使用被调用者保存寄存器（s0-s4: R8-R12）
            for (int reg : CALLEE_SAVED_REGS) {
                if (physicalRegs[reg] == 0) {
                    return reg;
                }
            }
            // 其次使用调用者保存寄存器（ra, a0-a5, lr: R1-R7, R15）
            for (int reg : CALLER_SAVED_REGS) {
                if (physicalRegs[reg] == 0) {
                    return reg;
                }
            }
        } else {
            // 简单线性扫描，从R1到R15（跳过保留寄存器）
            for (int reg = 1; reg < NUM_REGISTERS; reg++) {
                if (!isReserved(reg) && physicalRegs[reg] == 0) {
                    return reg;
                }
            }
        }
        return -1; // 没有可用寄存器
    }

    /**
     * 检查寄存器是否为保留寄存器（不可分配）
     *
     * @param regNum 寄存器编号
     * @return true如果是保留寄存器
     */
    private boolean isReserved(int regNum) {
        for (int reserved : RESERVED_REGS) {
            if (regNum == reserved) {
                return true;
            }
        }
        return false;
    }

    // ==================== 扩展功能方法 ====================

    /**
     * 强制将变量分配到指定的物理寄存器
     *
     * <p>用于特殊需求，如参数必须放在a0-a5寄存器中。</p>
     *
     * @param varName 变量名称
     * @param regNum 物理寄存器编号
     * @throws IllegalArgumentException 如果寄存器无效或已被占用
     */
    public void forceAllocate(String varName, int regNum) {
        if (regNum < 0 || regNum >= NUM_REGISTERS) {
            throw new IllegalArgumentException("Invalid register number: " + regNum);
        }
        if (isReserved(regNum)) {
            throw new IllegalArgumentException("Cannot allocate reserved register: " + regNum);
        }
        if (physicalRegs[regNum] != 0) {
            throw new IllegalArgumentException("Register " + regNum + " is already allocated to: " + regToVar.get(regNum));
        }

        // 如果变量之前已分配，先释放
        free(varName);

        // 分配新寄存器
        physicalRegs[regNum] = 1;
        varToReg.put(varName, regNum);
        regToVar.put(regNum, varName);
    }

    /**
     * 获取寄存器的ABI名称
     *
     * @param regNum 寄存器编号
     * @return ABI名称（如 "a0", "s1"）
     */
    public String getRegisterName(int regNum) {
        return StackOffsets.getAbiName(regNum);
    }

    /**
     * 获取变量所在寄存器的ABI名称
     *
     * @param varName 变量名称
     * @return ABI名称，如果变量未分配寄存器则返回null
     */
    public String getRegisterAbiName(String varName) {
        Integer reg = varToReg.get(varName);
        return reg != null ? getRegisterName(reg) : null;
    }

    /**
     * 获取所有已分配的变量名称
     *
     * @return 变量名称集合
     */
    public Set<String> getAllocatedVariables() {
        return new LinkedHashSet<>(varToReg.keySet());
    }

    /**
     * 获取所有已溢出的变量名称
     *
     * @return 变量名称集合
     */
    public Set<String> getSpilledVariables() {
        return new LinkedHashSet<>(spillSlots.keySet());
    }

    /**
     * 检查寄存器是否为调用者保存寄存器
     *
     * @param regNum 寄存器编号
     * @return true如果是调用者保存寄存器
     */
    public boolean isCallerSaved(int regNum) {
        for (int reg : CALLER_SAVED_REGS) {
            if (regNum == reg) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查寄存器是否为被调用者保存寄存器
     *
     * @param regNum 寄存器编号
     * @return true如果是被调用者保存寄存器
     */
    public boolean isCalleeSaved(int regNum) {
        for (int reg : CALLEE_SAVED_REGS) {
            if (regNum == reg) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取指定类型（调用者保存/被调用者保存）的可用寄存器列表
     *
     * @param callerSaved true获取调用者保存，false获取被调用者保存
     * @return 可用寄存器编号数组
     */
    public int[] getAvailableRegistersByType(boolean callerSaved) {
        int[] regs = callerSaved ? CALLER_SAVED_REGS : CALLEE_SAVED_REGS;
        int count = 0;
        for (int reg : regs) {
            if (physicalRegs[reg] == 0) {
                count++;
            }
        }

        int[] available = new int[count];
        int idx = 0;
        for (int reg : regs) {
            if (physicalRegs[reg] == 0) {
                available[idx++] = reg;
            }
        }
        return available;
    }

    /**
     * 生成寄存器分配报告（用于调试）
     *
     * @return 报告字符串
     */
    public String generateAllocationReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Register Allocation Report ===\n");
        sb.append("Allocated variables: ").append(varToReg.size()).append("\n");
        sb.append("Spilled variables: ").append(spillSlots.size()).append("\n");
        sb.append("Available registers: ").append(getAvailableRegisterCount()).append("\n");
        sb.append("\n");

        if (!varToReg.isEmpty()) {
            sb.append("Variable to Register Mapping:\n");
            for (Map.Entry<String, Integer> entry : varToReg.entrySet()) {
                String regName = getRegisterName(entry.getValue());
                String regType = isCallerSaved(entry.getValue()) ? "caller-saved" :
                                 isCalleeSaved(entry.getValue()) ? "callee-saved" : "reserved";
                sb.append(String.format("  %-20s -> %2d (%-5s, %s)\n",
                        entry.getKey(), entry.getValue(), regName, regType));
            }
            sb.append("\n");
        }

        if (!spillSlots.isEmpty()) {
            sb.append("Spilled Variables:\n");
            for (Map.Entry<String, Integer> entry : spillSlots.entrySet()) {
                sb.append(String.format("  %-20s -> fp%+d\n",
                        entry.getKey(), entry.getValue()));
            }
            sb.append("\n");
        }

        sb.append("Register Status:\n");
        for (int i = 0; i < NUM_REGISTERS; i++) {
            String status = isReserved(i) ? "RESERVED" :
                           physicalRegs[i] == 0 ? "FREE" : "USED";
            String var = physicalRegs[i] != 0 ? regToVar.get(i) : "";
            String regName = getRegisterName(i);
            sb.append(String.format("  r%-2d (%-5s) [%-8s] %s\n",
                    i, regName, status, var));
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        return generateAllocationReport();
    }

    // ==================== 静态工具方法 ====================

    /**
     * 获取参数寄存器编号
     *
     * @param argIndex 参数索引（0-5）
     * @return 寄存器编号（2-7）
     * @throws IllegalArgumentException 如果argIndex超出范围
     */
    public static int getArgRegister(int argIndex) {
        if (argIndex < 0 || argIndex > 5) {
            throw new IllegalArgumentException("Argument index must be 0-5, got: " + argIndex);
        }
        return 2 + argIndex; // a0=2, a1=3, ..., a5=7
    }

    /**
     * 获取返回值寄存器编号
     *
     * @return 寄存器编号（2，即a0）
     */
    public static int getReturnValueRegister() {
        return 2; // a0
    }

    /**
     * 检查寄存器是否为参数寄存器（a0-a5）
     *
     * @param regNum 寄存器编号
     * @return true如果是参数寄存器
     */
    public static boolean isArgumentRegister(int regNum) {
        return regNum >= 2 && regNum <= 7;
    }
}
