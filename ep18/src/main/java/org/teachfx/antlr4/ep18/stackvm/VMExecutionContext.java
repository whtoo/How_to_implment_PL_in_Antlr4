package org.teachfx.antlr4.ep18.stackvm;

import org.teachfx.antlr4.ep18.stackvm.instructions.Instruction;

/**
 * 虚拟机执行上下文
 * 封装指令执行所需的虚拟机状态信息
 */
public class VMExecutionContext {
    // VM状态
    private final CymbolStackVM vm;
    private final VMConfig config;
    private final VMStats stats;

    // 程序计数器
    private int programCounter;

    // 运行时数据结构
    private int[] stack;
    private int stackPointer;
    private int[] heap;
    private int[] locals;
    private StackFrame[] callStack;
    private int framePointer;

    // 调试支持
    private boolean traceEnabled;

    // 堆分配指针
    private int heapAllocPointer;

    // 结构体管理
    private java.util.List<StructValue> structTable;
    private int nextStructId;

    // 异常处理
    private VMExceptionHandler exceptionHandler;
    private VMExceptionMonitor exceptionMonitor;

    /**
     * 构造函数
     * @param vm 虚拟机实例
     * @param config 虚拟机配置
     * @param stats 性能统计
     * @param programCounter 程序计数器
     * @param stack 操作数栈
     * @param stackPointer 栈指针
     * @param heap 堆内存
     * @param locals 局部变量
     * @param callStack 调用栈
     * @param framePointer 帧指针
     * @param traceEnabled 是否启用跟踪
     * @param heapAllocPointer 堆分配指针
     * @param structTable 结构体表
     * @param nextStructId 下一个结构体ID
     */
    public VMExecutionContext(
            CymbolStackVM vm,
            VMConfig config,
            VMStats stats,
            int programCounter,
            int[] stack,
            int stackPointer,
            int[] heap,
            int[] locals,
            StackFrame[] callStack,
            int framePointer,
            boolean traceEnabled,
            int heapAllocPointer,
            java.util.List<StructValue> structTable,
            int nextStructId) {
        this.vm = vm;
        this.config = config;
        this.stats = stats;
        this.programCounter = programCounter;
        this.stack = stack;
        this.stackPointer = stackPointer;
        this.heap = heap;
        this.locals = locals;
        this.callStack = callStack;
        this.framePointer = framePointer;
        this.traceEnabled = traceEnabled;
        this.heapAllocPointer = heapAllocPointer;
        this.structTable = structTable;
        this.nextStructId = nextStructId;
    }

    /**
     * 无参构造函数（用于测试）
     * 创建默认的VM执行上下文
     */
    public VMExecutionContext() {
        this(
            new CymbolStackVM(VMConfig.builder().build()),
            VMConfig.builder().build(),
            new VMStats(),
            0,
            new int[1024],
            0,
            new int[1024],
            new int[256],
            new StackFrame[128],
            0,
            false,
            0,
            new java.util.ArrayList<>(),
            0
        );
    }

    /**
     * 获取虚拟机实例
     */
    public CymbolStackVM getVM() {
        return vm;
    }

    /**
     * 获取配置
     */
    public VMConfig getConfig() {
        return config;
    }

    /**
     * 获取性能统计
     */
    public VMStats getStats() {
        return stats;
    }

    /**
     * 获取程序计数器
     */
    public int getProgramCounter() {
        return programCounter;
    }

    /**
     * 设置程序计数器
     */
    public void setProgramCounter(int programCounter) {
        this.programCounter = programCounter;
    }

    /**
     * 获取操作数栈
     */
    public int[] getStack() {
        return stack;
    }

    /**
     * 获取栈指针
     */
    public int getStackPointer() {
        return stackPointer;
    }

    /**
     * 设置栈指针
     */
    public void setStackPointer(int stackPointer) {
        this.stackPointer = stackPointer;
    }

    /**
     * 获取堆内存
     */
    public int[] getHeap() {
        return heap;
    }

    /**
     * 获取局部变量数组
     */
    public int[] getLocals() {
        return locals;
    }

    /**
     * 获取调用栈
     */
    public StackFrame[] getCallStack() {
        return callStack;
    }

    /**
     * 获取当前帧指针
     */
    public int getFramePointer() {
        return framePointer;
    }

    /**
     * 设置帧指针
     */
    public void setFramePointer(int framePointer) {
        this.framePointer = framePointer;
    }

    /**
     * 检查是否启用跟踪
     */
    public boolean isTraceEnabled() {
        return traceEnabled;
    }

    /**
     * 设置跟踪标志
     */
    public void setTraceEnabled(boolean traceEnabled) {
        this.traceEnabled = traceEnabled;
    }

    /**
     * 获取堆分配指针
     */
    public int getHeapAllocPointer() {
        return heapAllocPointer;
    }

    /**
     * 设置堆分配指针
     */
    public void setHeapAllocPointer(int heapAllocPointer) {
        this.heapAllocPointer = heapAllocPointer;
    }

    /**
     * 获取结构体表
     */
    public java.util.List<StructValue> getStructTable() {
        return structTable;
    }

    /**
     * 获取下一个结构体ID
     */
    public int getNextStructId() {
        return nextStructId;
    }

    /**
     * 设置下一个结构体ID
     */
    public void setNextStructId(int nextStructId) {
        this.nextStructId = nextStructId;
    }

    // 栈操作方法

    /**
     * 将值压入栈
     */
    public void push(int value) {
        if (stackPointer >= stack.length) {
            throw new StackOverflowError("Stack overflow");
        }
        stack[stackPointer++] = value;
    }

    /**
     * 从栈弹出值
     */
    public int pop() {
        if (stackPointer <= 0) {
            throw new IllegalStateException("Stack underflow");
        }
        return stack[--stackPointer];
    }

    /**
     * 查看栈顶值（不弹出）
     */
    public int peek() {
        if (stackPointer <= 0) {
            throw new IllegalStateException("Stack is empty");
        }
        return stack[stackPointer - 1];
    }

    /**
     * 检查栈是否为空
     */
    public boolean isStackEmpty() {
        return stackPointer <= 0;
    }

    /**
     * 获取栈深度
     */
    public int getStackDepth() {
        return stackPointer;
    }

    // 堆操作方法

    /**
     * 堆内存读取
     */
    public int heapRead(int address) {
        if (address < 0 || address >= heap.length) {
            throw new IndexOutOfBoundsException("Heap address out of bounds: " + address);
        }
        return heap[address];
    }

    /**
     * 堆内存写入
     */
    public void heapWrite(int address, int value) {
        if (address < 0 || address >= heap.length) {
            throw new IndexOutOfBoundsException("Heap address out of bounds: " + address);
        }
        heap[address] = value;
    }

    /**
     * 分配堆内存
     */
    public int heapAlloc(int size) {
        if (heapAllocPointer + size > heap.length) {
            throw new OutOfMemoryError("Not enough heap space");
        }
        int address = heapAllocPointer;
        heapAllocPointer += size;
        return address;
    }

    // 局部变量操作

    /**
     * 读取局部变量
     */
    public int loadLocal(int index) {
        if (index < 0 || index >= locals.length) {
            throw new IndexOutOfBoundsException("Local variable index out of bounds: " + index);
        }
        return locals[index];
    }

    /**
     * 存储局部变量
     */
    public void storeLocal(int index, int value) {
        if (index < 0 || index >= locals.length) {
            throw new IndexOutOfBoundsException("Local variable index out of bounds: " + index);
        }
        locals[index] = value;
    }

    // 结构体操作

    /**
     * 将int值转换为Object
     */
    public Object intToValue(int value) {
        return value;
    }

    /**
     * 将Object值转换为int
     */
    public int valueToInt(Object obj) {
        if (obj == null) {
            return 0;
        }
        if (obj instanceof Integer) {
            return (Integer) obj;
        }
        throw new ClassCastException("Expected Integer but got: " + obj.getClass());
    }

    /**
     * 从结构体加载字段
     */
    public int loadStructField(int structRef, int fieldOffset) {
        if (structRef == 0) {
            throw new NullPointerException("Null struct reference");
        }

        int structIndex = structRef - 1;
        if (structIndex >= 0 && structIndex < structTable.size()) {
            StructValue struct = structTable.get(structIndex);
            Object fieldValue = struct.getField(fieldOffset);
            return valueToInt(fieldValue);
        } else if (structRef >= 0 && structRef < heap.length) {
            int actualAddress = structRef + fieldOffset;
            if (actualAddress < 0 || actualAddress >= heap.length) {
                throw new IndexOutOfBoundsException("Struct field address out of bounds: " + actualAddress);
            }
            return heap[actualAddress];
        } else {
            throw new IndexOutOfBoundsException("Invalid struct reference: " + structRef);
        }
    }

    /**
     * 向结构体存储字段
     */
    public void storeStructField(int structRef, int fieldOffset, int value) {
        if (structRef == 0) {
            throw new NullPointerException("Null struct reference");
        }

        int structIndex = structRef - 1;
        if (structIndex >= 0 && structIndex < structTable.size()) {
            StructValue struct = structTable.get(structIndex);
            Object fieldValue = intToValue(value);
            struct.setField(fieldOffset, fieldValue);
        } else if (structRef >= 0 && structRef < heap.length) {
            int actualAddress = structRef + fieldOffset;
            if (actualAddress < 0 || actualAddress >= heap.length) {
                throw new IndexOutOfBoundsException("Struct field address out of bounds: " + actualAddress);
            }
            heap[actualAddress] = value;
        } else {
            throw new IndexOutOfBoundsException("Invalid struct reference: " + structRef);
        }
    }

    /**
     * 创建结构体实例
     */
    public int createStruct(int nfields) {
        StructValue struct = new StructValue(nfields);
        for (int i = 0; i < nfields; i++) {
            struct.setField(i, 0);
        }
        structTable.add(struct);
        int structId = nextStructId++;
        return structId;
    }

    // 异常处理相关方法

    /**
     * 获取异常处理器
     */
    public VMExceptionHandler getExceptionHandler() {
        return exceptionHandler;
    }

    /**
     * 设置异常处理器
     */
    public void setExceptionHandler(VMExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    /**
     * 获取异常监控器
     */
    public VMExceptionMonitor getExceptionMonitor() {
        return exceptionMonitor;
    }

    /**
     * 设置异常监控器
     */
    public void setExceptionMonitor(VMExceptionMonitor exceptionMonitor) {
        this.exceptionMonitor = exceptionMonitor;
    }

    /**
     * 获取栈大小
     */
    public int getStackSize() {
        return config.getStackSize();
    }
}
