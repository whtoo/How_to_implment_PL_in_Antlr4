package org.teachfx.antlr4.ep18r.stackvm.instructions.executors;

import org.teachfx.antlr4.ep18r.stackvm.instructions.InstructionExecutor;
import org.teachfx.antlr4.ep18r.stackvm.instructions.model.RegisterBytecodeDefinition;
import org.teachfx.antlr4.ep18r.stackvm.Logger;

/**
 * 内存访问指令执行器集合
 * 处理所有内存加载、存储和常量访问指令
 */
public class MemoryExecutors {

    private static final Logger logger = Logger.getLogger(MemoryExecutors.class);

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
     * 特殊处理：当基址寄存器是r13（SP）或r14（FP）时，访问栈帧局部变量
     */
    public static final InstructionExecutor LW = (operand, context) -> {
        int rd = context.extractRd(operand);
        int base = context.extractRs1(operand);
        int offset = context.extractImm16(operand);

        int baseAddr = context.getRegister(base);

        // 检查是否是FP相对寻址（通过FP寄存器）
        if (base == RegisterBytecodeDefinition.R14) { // FP寄存器
            // FP相对寻址：地址 = FP + offset
            // offset是字节偏移，需要转换为int数组索引（除以4）
            int effectiveAddr = baseAddr + offset / 4;

            // 检查地址有效性
            if (effectiveAddr < 0) {
                throw new IllegalArgumentException("Invalid FP-relative address: " + effectiveAddr);
            }

            int value = context.readMemory(effectiveAddr);
            context.setRegister(rd, value);
        } else {
            // 常规内存访问（offset是字节偏移，堆是int数组，需要除以4）
            int effectiveAddr = baseAddr + offset / 4;

            // 检查地址有效性
            if (effectiveAddr < 0) {
                throw new IllegalArgumentException("Invalid memory address: " + effectiveAddr);
            }

            int value = context.readMemory(effectiveAddr);
            context.setRegister(rd, value);
        }
    };

    /**
     * 存储字指令执行器
     * 特殊处理：当基址寄存器是r13（SP）或r14（FP）时，访问栈帧局部变量
     */
    public static final InstructionExecutor SW = (operand, context) -> {
        int rs = context.extractRd(operand);
        int base = context.extractRs1(operand);
        int offset = context.extractImm16(operand);
        int value = context.getRegister(rs);
        int baseAddr = context.getRegister(base);

        if (base == RegisterBytecodeDefinition.R14) {
            int effectiveAddr = baseAddr + offset / 4;
            if (effectiveAddr < 0) {
                throw new IllegalArgumentException("Invalid FP-relative address: " + effectiveAddr);
            }
            context.writeMemory(effectiveAddr, value);
        } else {
            int effectiveAddr = baseAddr + offset / 4;
            if (effectiveAddr < 0) {
                throw new IllegalArgumentException("Invalid memory address: " + effectiveAddr);
            }
            context.writeMemory(effectiveAddr, value);
        }
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

        // 计算字段地址（offset是字节偏移，堆是int数组，需要除以4）
        int fieldAddr = objPtr + offset / 4;
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

        // 计算字段地址（offset是字节偏移，堆是int数组，需要除以4）
        int fieldAddr = objPtr + offset / 4;
        int value = context.getRegister(rs);
        if (context.isTraceEnabled()) {
            logger.memoryTrace("[SW_F] objPtr=%d, offset=%d, fieldAddr=%d, value=%d", objPtr, offset, fieldAddr, value);
        }
        context.writeMemory(fieldAddr, value);
    };

    // ==================== 结构体和特殊指令 ====================

    /**
     * 结构体分配指令执行器
     * struct rd, size: 在堆上分配结构体空间
     * size参数表示字段数，每个字段占4字节
     */
    public static final InstructionExecutor STRUCT = (operand, context) -> {
        int rd = context.extractRd(operand);
        int numFields = context.extractImm16(operand);

        // 计算结构体大小（每个字段4字节，堆是int数组，每个元素4字节）
        int structSize = numFields; // 字段数，每个字段对应一个int数组元素

        // 在堆上分配结构体空间
        int address = context.getHeapAllocPointer();

        // 验证堆边界
        context.validateHeapBounds(address, structSize);

        // 初始化结构体内存（全部设为0）
        for (int i = 0; i < structSize; i++) {
            context.writeHeap(address + i, 0);
        }

        // 更新堆分配指针
        context.setHeapAllocPointer(address + structSize);

        // 将结构体地址存储到寄存器
        if (context.isTraceEnabled()) {
            logger.memoryTrace("[STRUCT] 分配结构体: numFields=%d, address=%d, heapAllocPointer=%d", numFields, address, address + structSize);
        }
        context.setRegister(rd, address);
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
        int rs = context.extractRd(operand);
        int value = context.getRegister(rs);
        Logger.programOutput(value);
    };

    /**
     * 停机指令执行器
     */
    public static final InstructionExecutor HALT = (operand, context) -> {
        // 停机指令只需要设置一个标志
        // 实际停止由解释器循环处理
        throw new RuntimeException("HALT instruction executed");
    };
}
