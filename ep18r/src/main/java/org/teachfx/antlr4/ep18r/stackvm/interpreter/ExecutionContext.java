package org.teachfx.antlr4.ep18r.stackvm.interpreter;

import org.teachfx.antlr4.ep18r.stackvm.ErrorCode;
import org.teachfx.antlr4.ep18r.stackvm.FunctionSymbol;
import org.teachfx.antlr4.ep18r.stackvm.StackFrame;
import org.teachfx.antlr4.ep18r.stackvm.config.VMConfig;
import org.teachfx.antlr4.ep18r.stackvm.exception.VMStackOverflowException;
import org.teachfx.antlr4.ep18r.stackvm.instructions.model.RegisterBytecodeDefinition;

/**
 * 执行上下文
 * 封装寄存器、内存等执行环境
 * 提供统一的访问接口，消除重复代码
 */
public class ExecutionContext {
    private final RegisterVMInterpreter vm;
    private final int[] registers;
    private final int programCounter;

    public ExecutionContext(RegisterVMInterpreter vm, int programCounter) {
        this.vm = vm;
        this.registers = vm.getRegisters();
        this.programCounter = programCounter;
    }

    // ==================== 寄存器访问 ====================

    /**
     * 获取寄存器值
     */
    public int getRegister(int regNum) {
        validateRegister(regNum);
        return registers[regNum];
    }

    /**
     * 设置寄存器值
     */
    public void setRegister(int regNum, int value) {
        validateRegister(regNum);
        if (regNum != 0) { // r0是只读的零寄存器
            registers[regNum] = value;
        }
    }

    /**
     * 验证寄存器编号
     */
    private void validateRegister(int regNum) {
        if (regNum < 0 || regNum >= RegisterBytecodeDefinition.NUM_REGISTERS) {
            throw new IllegalArgumentException(
                "Invalid register number: " + regNum + ", must be 0-" +
                (RegisterBytecodeDefinition.NUM_REGISTERS - 1));
        }
    }

    // ==================== 操作数提取 ====================

    /**
     * 从操作数中提取rd字段（目标寄存器）
     */
    public int extractRd(int operand) {
        return (operand >> 21) & 0x1F;
    }

    /**
     * 从操作数中提取rs1字段（源寄存器1）
     */
    public int extractRs1(int operand) {
        return (operand >> 16) & 0x1F;
    }

    /**
     * 从操作数中提取rs2字段（源寄存器2）
     */
    public int extractRs2(int operand) {
        return (operand >> 11) & 0x1F;
    }

    /**
     * 从操作数中提取16位立即数（符号扩展）
     */
    public int extractImm16(int operand) {
        int imm = operand & 0xFFFF;
        if ((imm & 0x8000) != 0) {
            imm |= 0xFFFF0000;
        }
        return imm;
    }

    /**
     * 从操作数中提取26位立即数（符号扩展）
     */
    public int extractImm26(int operand) {
        int imm = operand & 0x3FFFFFF;
        if ((imm & 0x2000000) != 0) {
            imm |= 0xFC000000;
        }
        return imm;
    }

    // ==================== 内存访问 ====================

    /**
     * 读取内存
     */
    public int readMemory(int address) {
        return vm.readMemory(address);
    }

    /**
     * 写入内存
     */
    public void writeMemory(int address, int value) {
        vm.writeMemory(address, value);
    }

    /**
     * 读取堆内存
     */
    public int readHeap(int address) {
        return vm.readHeap(address);
    }

    /**
     * 写入堆内存
     */
    public void writeHeap(int address, int value) {
        vm.writeHeap(address, value);
    }

    // ==================== 跳转控制 ====================

    /**
     * 设置跳转目标
     */
    public void setJumpTarget(int target) {
        vm.setJumpTarget(target);
    }

    /**
     * 检查是否发生了跳转
     */
    public boolean didJump() {
        return vm.didJump();
    }

    // ==================== 异常处理 ====================

    /**
     * 抛出异常
     */
    public void throwException(Exception e) throws Exception {
        throw e;
    }

    // ==================== Getters ====================

    /**
     * 获取虚拟机配置
     */
    public VMConfig getConfig() {
        return vm.getConfig();
    }

    /**
     * 获取程序计数器
     */
    public int getProgramCounter() {
        return programCounter;
    }

    /**
     * 检查trace模式是否启用
     */
    public boolean isTraceEnabled() {
        return vm.isTraceEnabled();
    }

    /**
     * 获取寄存器数组（内部使用）
     */
    public int[] getRegisters() {
        return registers;
    }

    // ==================== VM内部状态访问 ====================

    /**
     * 获取堆分配指针
     */
    public int getHeapAllocPointer() {
        return vm.getHeapAllocPointer();
    }

    /**
     * 设置堆分配指针
     */
    public void setHeapAllocPointer(int pointer) {
        vm.setHeapAllocPointer(pointer);
    }

    /**
     * 获取调用栈数组
     */
    public StackFrame[] getCallStack() {
        return vm.getCallStack();
    }

    /**
     * 获取当前帧指针
     */
    public int getFramePointer() {
        return vm.getFramePointer();
    }

    /**
     * 设置帧指针
     */
    public void setFramePointer(int framePointer) {
        vm.setFramePointer(framePointer);
    }

    /**
     * 验证26位跳转目标地址（需要4字节对齐）
     */
    public void validateJumpTarget26(int target) {
        if (target < 0 || target >= vm.getCodeSize() || target % 4 != 0) {
            throw new IllegalArgumentException(
                "Invalid 26-bit jump target: " + target + " at PC=" + getProgramCounter());
        }
    }

    /**
     * 检查调用栈是否溢出
     */
    public void checkStackOverflow() {
        if (getFramePointer() + 1 >= getConfig().getMaxCallStackDepth()) {
            throw new VMStackOverflowException(ErrorCode.STACK_OVERFLOW, getProgramCounter(), null);
        }
    }

    /**
     * 验证堆内存边界
     */
    public void validateHeapBounds(int address, int size) {
        if (address < 0 || address + size > getConfig().getHeapSize()) {
            throw new IndexOutOfBoundsException(
                "Heap address out of bounds: " + address + " with size " + size);
        }
    }

    // ==================== 栈帧访问 ====================

    /**
     * 获取当前栈帧
     */
    public StackFrame getCurrentFrame() {
        return vm.getCurrentFrame();
    }

    /**
     * 根据代码地址查找函数符号
     * @param address 函数入口地址
     * @return 函数符号，如果找不到返回null
     */
    public FunctionSymbol getFunctionSymbol(int address) {
        return vm.findFunctionByAddress(address);
    }

    /**
     * 访问栈帧局部变量（通过偏移量）
     * 使用FP相对寻址：地址 = frameBasePointer + offset
     * @param offset 局部变量偏移量（字节偏移，如-16表示第一个局部变量）
     * @return 局部变量的值
     */
    public int getLocalVar(int offset) {
        StackFrame frame = getCurrentFrame();
        if (frame == null) {
            throw new IllegalStateException("No current stack frame");
        }

        // FP相对寻址：计算heap地址
        // offset是字节偏移，需要除以4转换为int数组索引
        int heapIndex = frame.frameBasePointer + offset / 4;

        // 检查地址有效性
        if (heapIndex < 0) {
            throw new IndexOutOfBoundsException("Local variable heap index out of bounds: " + heapIndex);
        }

        return vm.readHeap(heapIndex);
    }

    /**
     * 设置栈帧局部变量（通过偏移量）
     * 使用FP相对寻址：地址 = frameBasePointer + offset
     * @param offset 局部变量偏移量（字节偏移，如-16表示第一个局部变量）
     * @param value 要设置的值
     */
    public void setLocalVar(int offset, int value) {
        StackFrame frame = getCurrentFrame();
        if (frame == null) {
            throw new IllegalStateException("No current stack frame");
        }

        // FP相对寻址：计算heap地址
        // offset是字节偏移，需要除以4转换为int数组索引
        int heapIndex = frame.frameBasePointer + offset / 4;

        // 检查地址有效性
        if (heapIndex < 0) {
            throw new IndexOutOfBoundsException("Local variable heap index out of bounds: " + heapIndex);
        }

        vm.writeHeap(heapIndex, value);
    }

    /**
     * 检查地址是否在栈帧局部变量范围内
     * 简化实现：假设SP指向局部变量区域的开始
     */
    public boolean isStackAddress(int address) {
        // 简化实现：负地址表示栈地址
        // 实际情况更复杂，但这是当前实现的约定
        return address < 0;
    }
}
