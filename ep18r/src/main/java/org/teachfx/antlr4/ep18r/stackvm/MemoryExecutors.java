package org.teachfx.antlr4.ep18r.stackvm;

/**
 * 内存访问指令执行器集合
 * 处理所有内存加载、存储和常量访问指令
 */
public class MemoryExecutors {

    // ==================== 常量加载指令 ====================

    /**
     * 加载整数立即数指令执行器
     */
    public static final InstructionExecutor LI = (operand, context) -> {
        int rd = context.extractRd(operand);
        int imm = context.extractImm16(operand);
        context.setRegister(rd, imm);
    };

    /**
     * 加载字符立即数指令执行器
     */
    public static final InstructionExecutor LC = (operand, context) -> {
        int rd = context.extractRd(operand);
        int imm = context.extractImm16(operand);
        context.setRegister(rd, imm);
    };

    /**
     * 加载浮点常量指令执行器
     */
    public static final InstructionExecutor LF = (operand, context) -> {
        int rd = context.extractRd(operand);
        int poolIndex = context.extractImm16(operand);

        // 从常量池加载浮点值
        // 这里简化处理，实际应该从constPool获取
        float value = (float) poolIndex;
        int intValue = Float.floatToIntBits(value);
        context.setRegister(rd, intValue);
    };

    /**
     * 加载字符串常量指令执行器
     */
    public static final InstructionExecutor LS = (operand, context) -> {
        int rd = context.extractRd(operand);
        int poolIndex = context.extractImm16(operand);

        // 从常量池加载字符串引用
        // 这里简化处理，实际应该从constPool获取
        // 返回字符串的地址或标识符
        context.setRegister(rd, poolIndex);
    };

    // ==================== 内存访问指令 ====================

    /**
     * 加载字指令执行器
     */
    public static final InstructionExecutor LW = (operand, context) -> {
        int rd = context.extractRd(operand);
        int base = context.extractRs1(operand);
        int offset = context.extractImm16(operand);

        int baseAddr = context.getRegister(base);
        int effectiveAddr = baseAddr + offset;

        // 检查地址有效性
        if (effectiveAddr < 0) {
            throw new IllegalArgumentException("Invalid memory address: " + effectiveAddr);
        }

        int value = context.readMemory(effectiveAddr);
        context.setRegister(rd, value);
    };

    /**
     * 存储字指令执行器
     */
    public static final InstructionExecutor SW = (operand, context) -> {
        int rs = context.extractRd(operand);  // rs 在 rd 字段位置
        int base = context.extractRs1(operand);
        int offset = context.extractImm16(operand);

        int baseAddr = context.getRegister(base);
        int effectiveAddr = baseAddr + offset;

        // 检查地址有效性
        if (effectiveAddr < 0) {
            throw new IllegalArgumentException("Invalid memory address: " + effectiveAddr);
        }

        int value = context.getRegister(rs);
        context.writeMemory(effectiveAddr, value);
    };

    /**
     * 全局加载指令执行器
     */
    public static final InstructionExecutor LW_G = (operand, context) -> {
        int rd = context.extractRd(operand);
        int offset = context.extractImm16(operand);

        // 简化实现：直接使用offset作为heap索引
        int value = context.readMemory(offset);
        context.setRegister(rd, value);
    };

    /**
     * 全局存储指令执行器
     */
    public static final InstructionExecutor SW_G = (operand, context) -> {
        int rs = context.extractRd(operand);  // rs 在 rd 字段位置
        int offset = context.extractImm16(operand);

        // 简化实现：直接使用offset作为heap索引
        int value = context.getRegister(rs);
        context.writeMemory(offset, value);
    };

    /**
     * 字段加载指令执行器
     */
    public static final InstructionExecutor LW_F = (operand, context) -> {
        int rd = context.extractRd(operand);
        int objPtrReg = context.extractRs1(operand);  // 对象指针寄存器
        int offset = context.extractImm16(operand);

        int objPtr = context.getRegister(objPtrReg);
        // 检查对象指针
        if (objPtr < 0) {
            throw new IndexOutOfBoundsException("Object pointer out of bounds: " + objPtr);
        }

        // 计算字段地址
        int fieldAddr = objPtr + offset;
        int value = context.readMemory(fieldAddr);
        context.setRegister(rd, value);
    };

    /**
     * 字段存储指令执行器
     */
    public static final InstructionExecutor SW_F = (operand, context) -> {
        int rs = context.extractRd(operand);  // rs 在 rd 字段位置
        int objPtrReg = context.extractRs1(operand);  // 对象指针寄存器
        int offset = context.extractImm16(operand);

        int objPtr = context.getRegister(objPtrReg);
        // 检查对象指针
        if (objPtr < 0) {
            throw new IndexOutOfBoundsException("Object pointer out of bounds: " + objPtr);
        }

        // 计算字段地址
        int fieldAddr = objPtr + offset;
        int value = context.getRegister(rs);
        context.writeMemory(fieldAddr, value);
    };

    // ==================== 结构体和特殊指令 ====================

    /**
     * 结构体分配指令执行器
     */
    public static final InstructionExecutor STRUCT = (operand, context) -> {
        int rd = context.extractRd(operand);
        int size = context.extractImm16(operand);

        // 简化实现：在堆上分配结构体空间
        // 实际实现需要集成垃圾回收器
        int structId = allocateStruct(size);
        context.setRegister(rd, structId);
    };

    /**
     * 空指针加载指令执行器
     */
    public static final InstructionExecutor NULL = (operand, context) -> {
        int rd = context.extractRd(operand);
        context.setRegister(rd, 0); // 0表示null
    };

    /**
     * 寄存器移动指令执行器
     */
    public static final InstructionExecutor MOV = (operand, context) -> {
        int rd = context.extractRd(operand);
        int rs1 = context.extractRs1(operand);
        int value = context.getRegister(rs1);
        context.setRegister(rd, value);
    };

    /**
     * 打印指令执行器
     */
    public static final InstructionExecutor PRINT = (operand, context) -> {
        int rs = context.extractRs1(operand);
        int value = context.getRegister(rs);
        System.out.println(value);
    };

    /**
     * 停机指令执行器
     */
    public static final InstructionExecutor HALT = (operand, context) -> {
        // 停机指令只需要设置一个标志
        // 实际停止由解释器循环处理
        throw new RuntimeException("HALT instruction executed");
    };

    /**
     * 分配结构体（简化实现）
     */
    private static int allocateStruct(int size) {
        // 简化实现：返回虚拟的对象ID
        // 实际实现需要与垃圾回收器集成
        return size; // 使用size作为标识符
    }
}
