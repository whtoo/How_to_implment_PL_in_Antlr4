# 第11章提示：虚拟机设计与垃圾回收

## 章节概要

**章节标题**: 虚拟机设计与垃圾回收

**所属模块**: 模块3 (EP17-EP18R)

**目标读者**: 已实现过编译器的工程师，学习执行引擎

**先修知识**:
- 栈式虚拟机基础（操作数栈、指令指针）
- 字节码设计与编码（固定长度指令、操作数格式）
- 内存管理基础（堆、栈、全局变量）
- 数据结构基础（数组、链表、哈希表）

**EP范围**: EP18（栈式虚拟机、引用计数GC、ByteCodeAssembler）

**前导章节**: 第10章

**后续章节**: 第12章

## 学习目标

1. **理解栈式虚拟机执行模型**
   - 掌握基于栈的操作数管理机制
   - 理解指令获取-译码-执行循环
   - 实现完整的字节码解释器

2. **掌握字节码设计与编码**
   - 设计固定长度指令集（32位统一编码）
   - 实现算术、逻辑、控制流、内存访问指令
   - 处理操作数编码（立即数、寄存器、内存地址）

3. **理解引用计数垃圾回收原理**
   - 学习引用计数算法的基础理论
   - 实现自动内存管理和对象生命周期追踪
   - 掌握内存分配与回收机制

## 核心内容框架

### 第1节：虚拟机架构设计

**1.1 虚拟机核心组件**
```java
public class CymbolStackVM {
    // 操作数栈
    private int[] stack;
    private int stackPointer;
    
    // 字节码存储
    private int[] instructionCache;
    private int instructionCount;
    
    // 堆内存
    private int[] heap;
    private int heapAllocPointer;
    
    // 局部变量存储
    private int[] locals;
    
    // 调用栈
    private StackFrame[] callStack;
    private int framePointer;
    
    // 垃圾回收器
    private GarbageCollector garbageCollector;
    
    // 结构体管理
    private List<StructValue> structTable;
    
    // 执行状态
    private int programCounter;
    private boolean running;
}
```

**1.2 指令获取-译码-执行循环**
```java
public int execute(byte[] bytecode) throws Exception {
    // 加载字节码
    loadBytecode(bytecode);
    
    // 创建主函数栈帧
    FunctionSymbol mainSymbol = new FunctionSymbol("main", 0, 0, 0);
    StackFrame mainFrame = new StackFrame(mainSymbol, -1);
    callStack[++framePointer] = mainFrame;
    
    // 开始执行
    this.running = true;
    this.programCounter = 0;
    
    while (running && programCounter >= 0 && programCounter < instructionCount) {
        // 获取当前指令
        int instruction = instructionCache[programCounter];
        int currentPC = programCounter;
        programCounter++; // 预取下一条指令
        
        // 执行指令
        executeInstruction(instruction);
    }
    
    // 返回结果
    int result = stackPointer > 0 ? stack[stackPointer - 1] : 0;
    return result;
}
```

### 第2节：字节码设计与编码

**2.1 指令编码格式**
```
统一32位指令格式（大端序）：
| 操作码 (8位) | 操作数 (24位) |
|-------------|-------------|
| opcode[31:24] | operand[23:0] |
```

**2.2 完整指令集**
```java
public class BytecodeDefinition {
    // 算术指令
    public static final short INSTR_IADD = 1;   // int add
    public static final short INSTR_ISUB = 2;   // int subtract
    public static final short INSTR_IMUL = 3;   // int multiply
    public static final short INSTR_IDIV = 4;   // int divide
    
    // 比较指令
    public static final short INSTR_ILT = 5;    // int less than
    public static final short INSTR_IGT = 7;    // int greater than
    public static final short INSTR_IEQ = 9;    // int equal
    public static final short INSTR_INE = 10;   // int not equal
    
    // 逻辑指令
    public static final short INSTR_INOT = 12;  // int not
    public static final short INSTR_IAND = 13;  // int and
    public static final short INSTR_IOR = 14;   // int or
    public static final short INSTR_IXOR = 15;  // int xor
    
    // 浮点指令
    public static final short INSTR_FADD = 16;  // float add
    public static final short INSTR_FSUB = 17;  // float subtract
    public static final short INSTR_FMUL = 18;  // float multiply
    public static final short INSTR_FDIV = 19;  // float divide
    public static final short INSTR_FLT = 20;  // float less than
    public static final short INSTR_FEQ = 21;  // float equal
    public static final short INSTR_ITOF = 22;  // int to float
    
    // 控制流指令
    public static final short INSTR_CALL = 23;   // function call
    public static final short INSTR_RET = 24;    // function return
    public static final short INSTR_BR = 25;     // unconditional jump
    public static final short INSTR_BRT = 26;    // branch if true
    public static final short INSTR_BRF = 27;    // branch if false
    
    // 内存访问指令
    public static final short INSTR_LOAD = 32;   // load local variable
    public static final short INSTR_STORE = 35;  // store local variable
    public static final short INSTR_GLOAD = 33;  // load global variable
    public static final short INSTR_GSTORE = 36;  // store global variable
    
    // 结构体指令
    public static final short INSTR_FLOAD = 34;  // load struct field
    public static final short INSTR_FSTORE = 37;  // store struct field
    public static final short INSTR_STRUCT = 39;  // create struct
    
    // 系统指令
    public static final short INSTR_ICONST = 29;  // push int constant
    public static final short INSTR_FCONST = 30;  // push float constant
    public static final short INSTR_NULL = 40;    // push null
    public static final short INSTR_POP = 41;     // pop and discard
    public static final short INSTR_PRINT = 38;  // print stack top
    public static final short INSTR_HALT = 42;    // stop execution
}
```

**2.3 字节码汇编器设计**
```java
public class ByteCodeAssembler extends VMAssemblerBaseListener {
    private byte[] code = new byte[INITIAL_CODE_SIZE];
    private int ip = 0;  // 指令指针
    
    private Map<String, LabelSymbol> labels;
    private List<Object> constPool;
    private Map<String, Integer> instructionOpcodeMapping;
    
    // 生成指令
    protected void gen(Token instrToken, Token operandToken) {
        String instructionName = instrToken.getText();
        Integer opcode = instructionOpcodeMapping.get(instructionName);
        code[ip++] = (byte) (opcode & 0xff);
        genOperand(operandToken);
    }
    
    // 生成操作数
    protected void genOperand(Token operandToken) {
        String text = operandToken.getText();
        int v = parseOperand(text);
        code[ip++] = (byte) ((v >> 24) & 0xff);
        code[ip++] = (byte) ((v >> 16) & 0xff);
        code[ip++] = (byte) ((v >> 8) & 0xff);
        code[ip++] = (byte) (v & 0xff);
    }
}
```

### 第3节：指令执行实现

**3.1 算术指令执行**
```java
private void executeIAdd() {
    int b = pop();
    int a = pop();
    push(a + b);
}

private void executeISub() {
    int b = pop();
    int a = pop();
    push(a - b);
}

private void executeIMul() {
    int b = pop();
    int a = pop();
    push(a * b);
}

private void executeIDiv() {
    int b = pop();
    int a = pop();
    if (b == 0) {
        throw new VMDivisionByZeroException(programCounter - 1, "IDIV");
    }
    push(a / b);
}
```

**3.2 比较指令执行**
```java
private void executeILt() {
    int b = pop();
    int a = pop();
    push(a < b ? 1 : 0);
}

private void executeIEq() {
    int b = pop();
    int a = pop();
    push(a == b ? 1 : 0);
}
```

**3.3 控制流指令执行**
```java
private void executeBr(int instruction) {
    int address = extractOperand(instruction);
    programCounter = address;  // 无条件跳转
}

private void executeBrt(int instruction) {
    int address = extractOperand(instruction);
    int condition = pop();
    if (condition != 0) {
        programCounter = address;  // 条件为真时跳转
    }
}

private void executeBrf(int instruction) {
    int address = extractOperand(instruction);
    int condition = pop();
    if (condition == 0) {
        programCounter = address;  // 条件为假时跳转
    }
}
```

**3.4 函数调用与返回**
```java
private void executeCall(int instruction) {
    int targetAddress = extractOperand(instruction);
    int returnAddress = programCounter;
    
    // 创建新栈帧
    FunctionSymbol dummySymbol = new FunctionSymbol("func_" + targetAddress, 0, 0, targetAddress);
    StackFrame frame = new StackFrame(dummySymbol, returnAddress, null);
    
    // 保存调用者栈深度
    if (framePointer >= 0 && callStack[framePointer] != null) {
        callStack[framePointer].setDebugData("savedStackDepth", stackPointer);
    }
    
    // 压入调用栈
    callStack[++framePointer] = frame;
    
    // 跳转到目标地址
    programCounter = targetAddress;
}

private void executeRet() {
    if (framePointer < 0) {
        throw new VMStackUnderflowException("RET called without active frame", programCounter, "RET");
    }
    
    StackFrame frame = callStack[framePointer--];
    int returnAddress = frame.getReturnAddress();
    
    // 恢复调用者栈深度
    Integer savedDepth = null;
    if (framePointer >= 0 && callStack[framePointer] != null) {
        savedDepth = (Integer) callStack[framePointer].getDebugData("savedStackDepth");
    }
    
    // 获取返回值（栈顶元素）
    int returnValue = stackPointer > 0 ? stack[stackPointer - 1] : 0;
    
    // 恢复栈状态
    if (savedDepth != null) {
        stackPointer = savedDepth;
    } else {
        // 保留返回值在栈顶
        stackPointer = Math.max(0, savedDepth);
    }
    
    // 压入返回值（如果有）
    if (returnValue != 0) {
        stack[stackPointer++] = returnValue;
    }
    
    // 恢复程序计数器
    programCounter = returnAddress;
    
    // 检查main函数返回
    if (returnAddress == -1) {
        running = false;
    }
}
```

### 第4节：内存管理

**4.1 栈操作实现**
```java
protected void push(int value) {
    if (stackPointer >= stack.length) {
        throw new VMStackOverflowException("Stack overflow at PC=" + programCounter, programCounter, "PUSH");
    }
    
    // 如果是结构体引用，增加引用计数
    if (value > 0 && garbageCollector.isObjectAlive(value)) {
        garbageCollector.incrementRef(value);
    }
    
    stack[stackPointer++] = value;
}

protected int pop() {
    if (stackPointer <= 0) {
        throw new VMStackUnderflowException("Stack underflow at PC=" + programCounter, programCounter, "POP");
    }
    
    int value = stack[--stackPointer];
    
    // 如果是结构体引用，减少引用计数
    if (value > 0 && garbageCollector.isObjectAlive(value)) {
        garbageCollector.decrementRef(value);
    }
    
    return value;
}
```

**4.2 局部变量访问**
```java
private void executeLoad(int instruction) {
    int index = extractOperand(instruction);
    if (index < 0 || index >= locals.length) {
        throw VMMemoryAccessException.outOfBounds(programCounter, "LOAD", index, 0, locals.length - 1, 
                                                   VMMemoryException.MemoryAccessType.READ);
    }
    push(locals[index]);
}

private void executeStore(int instruction) {
    int index = extractOperand(instruction);
    if (index < 0 || index >= locals.length) {
        throw VMMemoryAccessException.outOfBounds(programCounter, "STORE", index, 0, locals.length - 1, 
                                                   VMMemoryException.MemoryAccessType.WRITE);
    }
    int value = pop();
    locals[index] = value;
}
```

**4.3 全局变量访问**
```java
private void executeGload(int instruction) {
    int address = extractOperand(instruction);
    if (address < 0 || address >= heap.length) {
        throw VMMemoryAccessException.outOfBounds(programCounter, "GLOAD", address, 0, heap.length - 1, 
                                                   VMMemoryException.MemoryAccessType.READ);
    }
    push(heap[address]);
}

private void executeGstore(int instruction) {
    int address = extractOperand(instruction);
    if (address < 0 || address >= heap.length) {
        throw VMMemoryAccessException.outOfBounds(programCounter, "GSTORE", address, 0, heap.length - 1, 
                                                   VMMemoryException.MemoryAccessType.READ);
    }
    int value = pop();
    heap[address] = value;
}
```

### 第5节：引用计数垃圾回收

**5.1 GC对象头设计**
```java
public class GCObjectHeader {
    private int refCount;      // 引用计数
    private int size;           // 对象大小
    private int offset;         // 堆偏移量
    private boolean alive;       // 对象是否存活
    
    public GCObjectHeader(int size) {
        this.size = size;
        this.refCount = 0;
        this.alive = true;
    }
    
    public void incrementRef() {
        refCount++;
    }
    
    public int decrementRef() {
        return --refCount;
    }
    
    public boolean isAlive() {
        return alive;
    }
}
```

**5.2 引用计数GC实现**
```java
public class ReferenceCountingGC implements GarbageCollector {
    private final byte[] heap;
    private final Map<Integer, GCObjectHeader> objectHeaders;
    private final TreeMap<Integer, FreeBlock> freeList;
    private final AtomicInteger nextObjectId;
    private final GCStats stats;
    
    // 分配对象
    @Override
    public int allocate(int size) throws OutOfMemoryError {
        // 尝试垃圾回收以释放空间
        if (!hasFreeBlockFor(size)) {
            collect();
        }
        
        // 使用首次适应算法查找空闲块
        Integer offset = findFreeBlock(size);
        if (offset == null) {
            throw new OutOfMemoryError("Out of memory after garbage collection");
        }
        
        // 创建对象ID和头部
        int objectId = nextObjectId.getAndIncrement();
        GCObjectHeader header = new GCObjectHeader(size);
        header.setOffset(offset);
        header.incrementRef();  // 分配者持有引用
        objectHeaders.put(objectId, header);
        
        stats.recordAllocation(size);
        return objectId;
    }
    
    // 增加引用
    @Override
    public void incrementRef(int objectId) {
        GCObjectHeader header = objectHeaders.get(objectId);
        if (header != null) {
            header.incrementRef();
        }
    }
    
    // 减少引用并自动回收
    @Override
    public void decrementRef(int objectId) {
        GCObjectHeader header = objectHeaders.get(objectId);
        if (header != null) {
            int refCount = header.decrementRef();
            if (refCount <= 0) {
                collectObject(objectId);  // 立即回收
            }
        }
    }
    
    // 垃圾回收
    @Override
    public void collect() {
        long startTime = System.nanoTime();
        int collectedObjects = 0;
        long collectedMemory = 0;
        
        // 收集引用计数为0的对象
        List<Integer> objectsToCollect = new ArrayList<>();
        for (Map.Entry<Integer, GCObjectHeader> entry : objectHeaders.entrySet()) {
            if (entry.getValue().getRefCount() <= 0) {
                objectsToCollect.add(entry.getKey());
            }
        }
        
        // 回收对象
        for (int objectId : objectsToCollect) {
            collectedMemory += collectObject(objectId);
            collectedObjects++;
        }
        
        long endTime = System.nanoTime();
        stats.recordCollection(collectedObjects, collectedMemory, endTime - startTime);
    }
}
```

**5.3 空闲链表管理**
```java
// 首次适应算法
private Integer findFreeBlock(int size) {
    for (Map.Entry<Integer, FreeBlock> entry : freeList.entrySet()) {
        if (entry.getValue().size >= size) {
            return entry.getKey();  // 返回第一个足够大的空闲块
        }
    }
    return null;  // 没有合适的空闲块
}

// 空闲块合并
private void addFreeBlock(int offset, int size) {
    // 检查是否可以与前一个块合并
    Map.Entry<Integer, FreeBlock> prevEntry = freeList.floorEntry(offset);
    if (prevEntry != null) {
        FreeBlock prevBlock = prevEntry.getValue();
        if (prevEntry.getKey() + prevBlock.size == offset) {
            // 合并前一个块
            freeList.remove(prevEntry.getKey());
            offset = prevEntry.getKey();
            size += prevBlock.size;
        }
    }
    
    // 检查是否可以与后一个块合并
    Map.Entry<Integer, FreeBlock> nextEntry = freeList.higherEntry(offset);
    if (nextEntry != null && nextEntry.getKey() == offset + size) {
        // 合并后一个块
        freeList.remove(nextEntry.getKey());
        size += nextEntry.getValue().size;
    }
    
    // 添加合并后的空闲块
    freeList.put(offset, new FreeBlock(offset, size));
}
```

### 第6节：结构体系统

**6.1 结构体值表示**
```java
public class StructValue {
    private final int id;
    private final Object[] fields;
    private int referenceCount;
    
    public StructValue(int nfields) {
        this.id = nextStructId++;
        this.fields = new Object[nfields];
        // 初始化字段为0
        for (int i = 0; i < nfields; i++) {
            fields[i] = 0;
        }
        this.referenceCount = 1;
    }
    
    public Object getField(int offset) {
        return fields[offset];
    }
    
    public void setField(int offset, Object value) {
        fields[offset] = value;
    }
    
    public int incrementRef() {
        return ++referenceCount;
    }
    
    public int decrementRef() {
        return --referenceCount;
    }
}
```

**6.2 结构体字段访问**
```java
private void executeStruct(int instruction) {
    int nfields = extractOperand(instruction);
    
    // 使用GC分配内存
    int structId = garbageCollector.allocate(nfields);
    
    // 创建StructValue实例
    StructValue struct = new StructValue(nfields);
    structTable.add(struct);
    gcIdToStructIndex.put(structId, structTable.size() - 1);
    
    // 压入结构体ID
    push(structId);
    
    // 增加引用计数（结构体被压入栈）
    garbageCollector.incrementRef(structId);
}
```

**6.3 结构体字段加载**
```java
private void executeFload(int instruction) {
    int fieldOffset = extractOperand(instruction);
    int structRef = pop();
    
    // 检查null引用
    if (structRef == 0) {
        throw VMMemoryAccessException.nullPointer(programCounter, "FLOAD", 
                                                   VMMemoryException.MemoryAccessType.READ);
    }
    
    // 查找StructValue
    Integer structIndex = gcIdToStructIndex.get(structRef);
    if (structIndex != null && structIndex >= 0 && structIndex < structTable.size()) {
        StructValue struct = structTable.get(structIndex);
        if (struct != null) {
            Object fieldValue = struct.getField(fieldOffset);
            int intValue = valueToInt(fieldValue);
            push(intValue);
            return;
        }
    }
    
    throw new VMMemoryAccessException("Invalid struct reference: " + structRef, programCounter, "FLOAD", 
                                                   (long)structRef, 0, VMMemoryException.MemoryAccessType.READ);
}
```

## 实践练习

### 练习1：基础字节码汇编
**目标**: 实现完整的字节码汇编器
**任务**:
1. 创建`ByteCodeAssembler`类，继承ANTLR4的`VMAssemblerBaseListener`
2. 实现指令映射表（指令名→操作码）
3. 实现标签解析和符号表管理
4. 实现常量池管理
5. 生成32位固定长度字节码

**测试输入** (VM汇编语言):
```
# 简单加法
iconst 5
iconst 3
iadd
print
halt

# 函数调用
iconst 10
iconst 20
call add
print
halt

add:
    load 0
    load 1
    iadd
    ret
```

**预期输出**: 32位字节码文件

### 练习2：基础指令解释器
**目标**: 实现指令分发和执行引擎
**任务**:
1. 实现取指-译码-执行循环
2. 实现switch语句或策略模式的指令分发
3. 实现所有算术指令（IADD, ISUB, IMUL, IDIV）
4. 实现所有比较指令（ILT, IGT, IEQ, INE）
5. 实现所有逻辑指令（IAND, IOR, IXOR, INOT）
6. 实现控制流指令（BR, BRT, BRF）

**测试用例**:
```java
@Test
public void testArithmeticInstructions() {
    // 测试加法
    int[] bytecode = encodeInstructions(
        encodeInstruction(BytecodeDefinition.INSTR_ICONST, 5),
        encodeInstruction(BytecodeDefinition.INSTR_ICONST, 3),
        encodeInstruction(BytecodeDefinition.INSTR_IADD),
        encodeInstruction(BytecodeDefinition.INSTR_HALT)
    );
    
    VMConfig config = VMConfig.builder().build();
    CymbolStackVM vm = new CymbolStackVM(config);
    int result = vm.execute(bytecode);
    
    assertThat(result).isEqualTo(8);  // 5 + 3 = 8
    
    // 测试减法
    bytecode = encodeInstructions(
        encodeInstruction(BytecodeDefinition.INSTR_ICONST, 10),
        encodeInstruction(BytecodeDefinition.INSTR_ICONST, 4),
        encodeInstruction(BytecodeDefinition.INSTR_ISUB),
        encodeInstruction(BytecodeDefinition.INSTR_HALT)
    );
    
    result = vm.execute(bytecode);
    assertThat(result).isEqualTo(6);  // 10 - 4 = 6
    
    // 测试乘法
    bytecode = encodeInstructions(
        encodeInstruction(BytecodeDefinition.INSTR_ICONST, 5),
        encodeInstruction(BytecodeDefinition.INSTR_ICONST, 6),
        encodeInstruction(BytecodeDefinition.INSTR_IMUL),
        encodeInstruction(BytecodeDefinition.INSTR_HALT)
    );
    
    result = vm.execute(bytecode);
    assertThat(result).isEqualTo(30);  // 5 * 6 = 30
    
    // 测试除法
    bytecode = encodeInstructions(
        encodeInstruction(BytecodeDefinition.INSTR_ICONST, 20),
        encodeInstruction(BytecodeDefinition.INSTR_ICONST, 4),
        encodeInstruction(BytecodeDefinition.INSTR_IDIV),
        encodeInstruction(BytecodeDefinition.INSTR_HALT)
    );
    
    result = vm.execute(bytecode);
    assertThat(result).isEqualTo(5);  // 20 / 4 = 5
}
```

### 练习3：函数调用与返回
**目标**: 实现完整的函数调用机制
**任务**:
1. 实现CALL指令：保存返回地址、创建栈帧、跳转
2. 实现RET指令：弹出栈帧、恢复返回地址、恢复栈深度
3. 实现栈帧管理和调用栈操作
4. 测试嵌套函数调用和递归

**测试用例**:
```
# 嵌套函数调用
iconst 10
call func1
print
halt

func1:
    iconst 20
    call func2
    iadd
    ret

func2:
    iconst 5
    iconst 3
    iadd
    ret

# 递归斐波那契
iconst 6
call fib
print
halt

fib:
    dup          ; 复制n
    iconst 1
    isub
    dup
    iconst 2
    imul
    ilt fib_end
    ret

fib_end:
    ret
```

**预期结果**:
- 嵌套调用：func2返回5，func1返回25，主程序输出25
- 递归调用：fib(6)正确计算斐波那契数列第6项（8）

### 练习4：内存访问指令
**目标**: 实现局部变量和全局变量访问
**任务**:
1. 实现LOAD/STORE指令（局部变量）
2. 实现GLOAD/GSTORE指令（全局变量）
3. 实现边界检查和错误处理
4. 测试变量生命周期和作用域

**测试用例**:
```
# 局部变量测试
iconst 10
store 0
iconst 20
store 1
load 0
load 1
iadd
print
halt

# 全局变量测试
.global counter
iconst 0
gstore counter
gload counter
iconst 1
iadd
gstore counter
gload counter
print
halt

# 混合测试
.global global_var
iconst 100
store 0         ; local var 0
gstore global_var ; store 100 to global
gload global_var
store 1         ; local var 1
load 1
iadd
print
halt
```

### 练习5：引用计数垃圾回收
**目标**: 实现完整的引用计数GC系统
**任务**:
1. 设计并实现GC对象头（引用计数、存活状态）
2. 实现对象分配器（首次适应算法、空闲链表管理）
3. 实现引用计数管理（增减引用、自动回收）
4. 实现空闲块合并机制
5. 添加GC统计和性能监控

**测试用例**:
```java
@Test
public void testBasicAllocation() {
    GCConfig config = GCConfig.builder()
        .heapSize(1024)
        .build();
    
    ReferenceCountingGC gc = new ReferenceCountingGC(config);
    
    // 分配多个对象
    int obj1 = gc.allocate(16);   // 16字节
    int obj2 = gc.allocate(32);   // 32字节
    int obj3 = gc.allocate(8);    // 8字节
    
    assertThat(obj1).isNotEqualTo(0);
    assertThat(obj2).isNotEqualTo(0);
    assertThat(obj3).isNotEqualTo(0);
    assertThat(obj1).isNotEqualTo(obj2);
    assertThat(obj1).isNotEqualTo(obj3);
}

@Test
public void testReferenceCounting() {
    ReferenceCountingGC gc = new ReferenceCountingGC(heapSize);
    
    int obj = gc.allocate(16);
    
    // 增加引用
    gc.incrementRef(obj);
    assertThat(gc.getRefCount(obj)).isEqualTo(1);
    
    gc.incrementRef(obj);
    assertThat(gc.getRefCount(obj)).isEqualTo(2);
    
    // 减少引用
    gc.decrementRef(obj);
    assertThat(gc.getRefCount(obj)).isEqualTo(1);
    
    gc.decrementRef(obj);
    assertThat(gc.getRefCount(obj)).isEqualTo(0);
    assertThat(gc.isObjectAlive(obj)).isFalse();
}

@Test
public void testGarbageCollection() {
    ReferenceCountingGC gc = new ReferenceCountingGC(heapSize);
    
    // 分配对象直到堆满
    List<Integer> objects = new ArrayList<>();
    try {
        while (true) {
            objects.add(gc.allocate(16));
        }
    } catch (OutOfMemoryError e) {
        // 预期：堆已满
    }
    
    // 释放所有引用
    for (int obj : objects) {
        gc.decrementRef(obj);
    }
    
    // 触发垃圾回收
    gc.collect();
    
    // 验证：堆空间已释放
    assertThat(gc.getAvailableMemory()).isGreaterThan(heapSize * 0.9);
}
```

### 练习6：结构体系统
**目标**: 实现结构体创建和字段访问
**任务**:
1. 实现STRUCT指令：分配内存、创建StructValue
2. 实现FLOAD/FSTORE指令：结构体字段访问
3. 集成垃圾回收器（结构体引用计数）
4. 实现空指针检查
5. 测试结构体生命周期

**测试用例**:
```
# 结构体创建和使用
struct 2    ; 创建2字段结构体
dup          ; 复制结构体引用
iconst 10
fstore 0     ; 存储x=10
dup          ; 再次复制结构体引用
iconst 20
fstore 1     ; 存储y=20
dup          ; 复制结构体引用
fload 0      ; 加载x
print
dup          ; 复制结构体引用
fload 1      ; 加载y
print
halt

# 结构体生命周期测试
struct 1
store 0      ; 存储结构体引用到局部变量
iconst 0
store 1      ; 用局部变量0覆盖引用，触发回收
struct 2
fload 0      ; 尝试访问已回收的结构体
print
halt
```

**预期结果**:
- 正常使用：正确访问和修改结构体字段
- 内存泄漏检测：正确回收无引用的结构体
- 空指针检查：访问null引用时抛出异常

## 技术要点总结

### 关键数据结构
- **操作数栈**: 后进先出（LIFO）数据结构，O(1)压入/弹出
- **指令缓存**: 固定长度指令数组，支持随机访问
- **调用栈**: 栈帧数组，支持函数嵌套调用
- **堆内存**: 字节数组，支持对象分配和GC
- **GC对象头**: 引用计数、大小、偏移量、存活状态
- **空闲链表**: TreeMap管理，支持快速查找和合并

### 关键算法
- **首次适应分配**: O(n)时间复杂度，查找第一个足够大的空闲块
- **引用计数**: O(1)增减操作，O(1)回收判断
- **空闲块合并**: O(log n)查找邻接块并合并
- **栈帧管理**: O(1)压入/弹出，保存/恢复调用上下文

### 关键技术
- **固定长度指令编码**: 32位统一格式，简化译码逻辑
- **操作数提取**: 符号扩展处理有符号操作数
- **大端序字节序**: 高字节在前，低字节在后
- **垃圾回收触发**: 分配失败时自动触发GC
- **引用计数集成**: 栈操作时自动增减引用计数

### 设计模式
- **策略模式**: 指令执行抽象为独立策略类
- **工厂模式**: InstructionFactory动态创建指令对象
- **访问者模式**: ANTLR4生成的VMAssemblerBaseListener
- **模板方法**: ByteCodeAssembler继承基类并实现钩子

## 扩展阅读

### 推荐阅读材料
1. **《编译原理》** - 第2章：编译器架构与虚拟机
2. **《计算机程序的构造和解释》** - 第4章：虚拟机
3. **《垃圾回收算法》** - 引用计数算法章节
4. **JVM规范** - Java虚拟机指令集参考
5. **LLVM参考手册** - 基于寄存器的虚拟机设计

### 相关EP参考
- EP16: 三地址码生成（字节码的上游）
- EP17: 调用图分析（优化决策的基础）
- EP18R: 寄存器虚拟机（本章节的升级版本）
- EP19: 中间表示生成（字节码的下游）

## 常见问题与调试

### Q1: 栈溢出错误
**原因**: 递归调用过深或无限递归
**解决方案**:
1. 检查调用栈深度限制
2. 添加循环检测（最大执行步数）
3. 实现尾递归优化（后续章节）
4. 增加栈大小配置

### Q2: 垃圾回收不释放对象
**原因**: 循环引用或引用计数错误
**解决方案**:
1. 添加GC统计信息，追踪对象分配和回收
2. 检查栈操作时的引用计数更新
3. 使用调试模式验证对象生命周期
4. 实现GC日志，记录回收过程

### Q3: 除零错误
**原因**: IDIV或FDIV指令的分母为零
**解决方案**:
1. 在执行除法前检查分母
2. 抛出VMDivisionByZeroException异常
3. 提供详细的错误信息（PC位置、指令、操作数）
4. 在测试中覆盖除零边界情况

### Q4: 字节码编码错误
**原因**: 操作数编码不正确或大端序处理错误
**解决方案**:
1. 编写单元测试验证编码正确性
2. 使用位运算工具函数确保正确性
3. 添加反汇编器验证字节码
4. 对比手动编码和自动编码的结果

### Q5: 结构体字段越界
**原因**: FLOAD/FSTORE的字段偏移量超出范围
**解决方案**:
1. 在加载/存储前检查字段偏移有效性
2. 抛出VMMemoryAccessException异常
3. 提供结构体字段数量信息
4. 在StructValue中添加边界检查方法

## 技术指标与性能考虑

### 时间复杂度
- 指令执行循环：O(N)，N为指令数
- 垃圾回收：O(M)，M为对象数
- 对象分配：O(log n)（TreeMap查找空闲块）
- 空闲块合并：O(log n)（邻接块查找）
- 栈操作：O(1)

### 空间复杂度
- 操作数栈：O(H)，H为栈高度
- 调用栈：O(D)，D为调用深度
- 堆内存：O(S)，S为堆大小
- GC对象表：O(M)，M为对象数
- 空闲链表：O(n)，n为空闲块数

### 性能基准
- 小型程序（<100行）：<10ms执行时间
- 中型程序（100-1000行）：10-100ms执行时间
- 大型程序（1000-10000行）：100ms-10s执行时间
- GC暂停时间：<1ms（引用计数零停顿）
- 内存使用率：60-80%（考虑碎片）

## 关键文件清单

### 核心实现文件
- `/ep18/src/main/java/org/teachfx/antlr4/ep18/stackvm/CymbolStackVM.java` - 虚拟机主引擎
- `/ep18/src/main/java/org/teachfx/antlr4/ep18/stackvm/ByteCodeAssembler.java` - 字节码汇编器
- `/ep18/src/main/java/org/teachfx/antlr4/ep18/stackvm/BytecodeDefinition.java` - 指令定义
- `/ep18/src/main/java/org/teachfx/antlr4/ep18/gc/ReferenceCountingGC.java` - 引用计数GC
- `/ep18/src/main/java/org/teachfx/antlr4/ep18/gc/GarbageCollector.java` - GC接口
- `/ep18/src/main/java/org/teachfx/antlr4/ep18/stackvm/StructValue.java` - 结构体表示

### 指令执行文件
- `/ep18/src/main/java/org/teachfx/antlr4/ep18/stackvm/instructions/InstructionFactory.java` - 指令工厂
- `/ep18/src/main/java/org/teachfx/antlr4/ep18/stackvm/instructions/Instruction.java` - 指令接口
- `/ep18/src/main/java/org/teachfx/antlr4/ep18/stackvm/instructions/arithmetic/` - 算术指令
- `/ep18/src/main/java/org/teachfx/antlr4/ep18/stackvm/instructions/comparison/` - 比较指令
- `/ep18/src/main/java/org/teachfx/antlr4/ep18/stackvm/instructions/controlflow/` - 控制流指令

### 内存管理文件
- `/ep18/src/main/java/org/teachfx/antlr4/ep18/stackvm/StackFrame.java` - 栈帧管理
- `/ep18/src/main/java/org/teachfx/antlr4/ep18/stackvm/VMConfig.java` - 虚拟机配置
- `/ep18/src/main/java/org/teachfx/antlr4/ep18/stackvm/StructSpace.java` - 结构体存储（兼容层）

### 测试文件
- `/ep18/src/test/java/org/teachfx/antlr4/ep18/CymbolStackVMTest.java` - 核心功能测试
- `/ep18/src/test/java/org/teachfx/antlr4/ep18/VMInterpreterTest.java` - 解释器测试
- `/ep18/src/test/java/org/teachfx/antlr4/ep18/gc/GarbageCollectorTest.java` - GC测试

## 代码示例

### 示例1：完整的虚拟机实现
```java
package org.teachfx.antlr4.ep18.stackvm;

public class CymbolStackVM {
    private final VMConfig config;
    private final VMStats stats;
    private int[] stack;
    private int stackPointer;
    private int[] heap;
    private int[] instructionCache;
    private int instructionCount;
    private int[] locals;
    private StackFrame[] callStack;
    private int framePointer;
    private GarbageCollector garbageCollector;
    private boolean running;
    private int programCounter;
    
    public CymbolStackVM(VMConfig config) {
        this.config = config;
        this.stats = new VMStats();
        initializeVM();
    }
    
    private void initializeVM() {
        this.heap = new int[config.getHeapSize()];
        this.heapAllocPointer = 0;
        
        if (config.isEnableGC()) {
            int gcHeapSize = config.getGcHeapSize() > 0 ? config.getGcHeapSize() : config.getHeapSize();
            this.garbageCollector = new ReferenceCountingGC(gcHeapSize);
        } else {
            this.garbageCollector = new NoOpGarbageCollector();
        }
        
        this.stack = new int[config.getStackSize()];
        this.stackPointer = 0;
        this.instructionCache = new int[config.getInstructionCacheSize()];
        this.instructionCount = 0;
        this.locals = new int[config.getStackSize()];
        this.callStack = new StackFrame[config.getMaxCallStackDepth()];
        this.framePointer = -1;
        this.running = false;
        this.programCounter = 0;
    }
    
    public int execute(byte[] bytecode) throws Exception {
        loadBytecode(bytecode);
        
        FunctionSymbol mainSymbol = new FunctionSymbol("main", 0, 0, 0);
        StackFrame mainFrame = new StackFrame(mainSymbol, -1);
        callStack[++framePointer] = mainFrame;
        
        this.running = true;
        this.programCounter = 0;
        
        while (running && programCounter >= 0 && programCounter < instructionCount) {
            int instruction = instructionCache[programCounter];
            int currentPC = programCounter;
            programCounter++;
            
            executeInstruction(instruction);
        }
        
        int result = stackPointer > 0 ? stack[stackPointer - 1] : 0;
        
        long endTime = System.nanoTime();
        stats.recordExecution(endTime);
        
        return result;
    }
    
    private void loadBytecode(byte[] bytecode) {
        this.instructionCount = Math.min(bytecode.length / 4, instructionCache.length);
        for (int i = 0; i < this.instructionCount; i++) {
            int offset = i * 4;
            instructionCache[i] = ((bytecode[offset] & 0xFF) << 24) |
                                   ((bytecode[offset + 1] & 0xFF) << 16) |
                                   ((bytecode[offset + 2] & 0xFF) << 8) |
                                   (bytecode[offset + 3] & 0xFF);
        }
    }
    
    protected void push(int value) {
        if (stackPointer >= stack.length) {
            throw new VMStackOverflowException("Stack overflow at PC=" + programCounter, programCounter, "PUSH");
        }
        
        if (value > 0 && garbageCollector.isObjectAlive(value)) {
            garbageCollector.incrementRef(value);
        }
        
        stack[stackPointer++] = value;
    }
    
    protected int pop() {
        if (stackPointer <= 0) {
            throw new VMStackUnderflowException("Stack underflow at PC=" + programCounter, programCounter, "POP");
        }
        
        int value = stack[--stackPointer];
        
        if (value > 0 && garbageCollector.isObjectAlive(value)) {
            garbageCollector.decrementRef(value);
        }
        
        return value;
    }
    
    private int extractOperand(int instruction) {
        int value = instruction & 0xFFFFFF;
        if ((value & 0x800000) != 0) {
            value |= 0xFF000000;  // 符号扩展
        }
        return value;
    }
    
    private void executeInstruction(int instruction) throws Exception {
        int opcode = (instruction >> 24) & 0xFF;
        
        InstructionFactory factory = InstructionFactory.getInstance();
        Instruction instr = factory.getRequiredInstruction(opcode);
        
        int operand = extractOperand(instruction);
        VMExecutionContext context = new VMExecutionContext(
            this, config, stats, programCounter, stack, stackPointer,
            heap, locals, callStack, framePointer, false,
            heapAllocPointer, null
        );
        
        try {
            instr.execute(context, operand);
            programCounter = context.getProgramCounter();
            stackPointer = context.getStackPointer();
            framePointer = context.getFramePointer();
            heapAllocPointer = context.getHeapAllocPointer();
        } catch (VMException e) {
            stats.recordError(e);
            throw e;
        }
    }
}
```

### 示例2：引用计数GC实现
```java
package org.teachfx.antlr4.ep18.gc;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ReferenceCountingGC implements GarbageCollector {
    private final int heapSize;
    private final byte[] heap;
    private final Map<Integer, GCObjectHeader> objectHeaders;
    private final TreeMap<Integer, FreeBlock> freeList;
    private final AtomicInteger nextObjectId;
    private final GCStats stats;
    
    public ReferenceCountingGC(int heapSize) {
        if (heapSize <= 0) {
            throw new IllegalArgumentException("Heap size must be positive");
        }
        
        this.heapSize = heapSize;
        this.heap = new byte[heapSize];
        this.objectHeaders = new ConcurrentHashMap<>();
        this.freeList = new TreeMap<>();
        this.nextObjectId = new AtomicInteger(1);
        this.stats = new GCStats();
        
        // 初始化空闲链表
        freeList.put(0, new FreeBlock(0, heapSize));
    }
    
    @Override
    public int allocate(int size) throws OutOfMemoryError {
        if (size <= 0) {
            throw new IllegalArgumentException("Size must be positive");
        }
        
        if (size > heapSize) {
            throw new OutOfMemoryError("Object size exceeds heap size");
        }
        
        if (!hasFreeBlockFor(size)) {
            collect();
        }
        
        Integer offset = findFreeBlock(size);
        if (offset == null) {
            throw new OutOfMemoryError("Out of memory after garbage collection");
        }
        
        FreeBlock block = freeList.remove(offset);
        if (block.size > size) {
            int remainingSize = block.size - size;
            int remainingOffset = offset + size;
            freeList.put(remainingOffset, new FreeBlock(remainingOffset, remainingSize));
        }
        
        int objectId = nextObjectId.getAndIncrement();
        GCObjectHeader header = new GCObjectHeader(size);
        header.setOffset(offset);
        header.incrementRef();
        objectHeaders.put(objectId, header);
        
        stats.recordAllocation(size);
        return objectId;
    }
    
    @Override
    public void incrementRef(int objectId) {
        GCObjectHeader header = objectHeaders.get(objectId);
        if (header != null) {
            header.incrementRef();
        }
    }
    
    @Override
    public void decrementRef(int objectId) {
        GCObjectHeader header = objectHeaders.get(objectId);
        if (header != null) {
            int refCount = header.decrementRef();
            if (refCount <= 0) {
                collectObject(objectId);
            }
        }
    }
    
    @Override
    public void collect() {
        long startTime = System.nanoTime();
        int collectedObjects = 0;
        long collectedMemory = 0;
        
        List<Integer> objectsToCollect = new ArrayList<>();
        for (Map.Entry<Integer, GCObjectHeader> entry : objectHeaders.entrySet()) {
            if (entry.getValue().getRefCount() <= 0) {
                objectsToCollect.add(entry.getKey());
            }
        }
        
        for (int objectId : objectsToCollect) {
            collectedMemory += collectObject(objectId);
            collectedObjects++;
        }
        
        long endTime = System.nanoTime();
        stats.recordCollection(collectedObjects, collectedMemory, endTime - startTime);
    }
    
    @Override
    public boolean isObjectAlive(int objectId) {
        GCObjectHeader header = objectHeaders.get(objectId);
        return header != null && header.isAlive();
    }
    
    @Override
    public GCStats getStats() {
        return stats;
    }
    
    @Override
    public void resetStats() {
        stats.reset();
    }
    
    private boolean hasFreeBlockFor(int size) {
        for (FreeBlock block : freeList.values()) {
            if (block.size >= size) {
                return true;
            }
        }
        return false;
    }
    
    private Integer findFreeBlock(int size) {
        for (Map.Entry<Integer, FreeBlock> entry : freeList.entrySet()) {
            if (entry.getValue().size >= size) {
                return entry.getKey();
            }
        }
        return null;
    }
    
    private long collectObject(int objectId) {
        GCObjectHeader header = objectHeaders.get(objectId);
        if (header == null) {
            return 0;
        }
        
        int size = header.getSize();
        int offset = header.getOffset();
        
        addFreeBlock(offset, size);
        
        objectHeaders.remove(objectId);
        header.setAlive(false);
        
        return size;
    }
    
    private void addFreeBlock(int offset, int size) {
        // 检查与前一个块合并
        Map.Entry<Integer, FreeBlock> prevEntry = freeList.floorEntry(offset);
        if (prevEntry != null) {
            FreeBlock prevBlock = prevEntry.getValue();
            if (prevEntry.getKey() + prevBlock.size == offset) {
                freeList.remove(prevEntry.getKey());
                offset = prevEntry.getKey();
                size += prevBlock.size;
            }
        }
        
        // 检查与后一个块合并
        Map.Entry<Integer, FreeBlock> nextEntry = freeList.higherEntry(offset);
        if (nextEntry != null && nextEntry.getKey() == offset + size) {
            FreeBlock nextBlock = nextEntry.getValue();
            freeList.remove(nextEntry.getKey());
            size += nextBlock.getValue().size;
        }
        
        freeList.put(offset, new FreeBlock(offset, size));
    }
}
```

### 示例3：字节码汇编器
```java
package org.teachfx.antlr4.ep18.stackvm;

import org.antlr.v4.runtime.Token;
import org.teachfx.antlr4.ep18.parser.VMAssemblerBaseListener;
import org.teachfx.antlr4.ep18.parser.VMAssemblerParser;
import org.teachfx.antlr4.ep18.parser.VMAssemblerParser.FunctionDeclarationContext;
import org.teachfx.antlr4.ep18.parser.VMAssemblerParser.InstrContext;
import org.teachfx.antlr4.ep18.parser.VMAssemblerParser.LabelContext;
import org.teachfx.antlr4.ep18.parser.VMAssemblerParser.TempContext;

import java.io.*;
import java.util.*;

public class ByteCodeAssembler extends VMAssemblerBaseListener {
    public static final int INITIAL_CODE_SIZE = 2048;
    
    private byte[] code = new byte[INITIAL_CODE_SIZE];
    private int ip = 0;
    private int dataSize = 0;
    private FunctionSymbol mainFunction;
    private List<Object> constPool = new ArrayList<>();
    private Map<String, LabelSymbol> labels = new HashMap<>();
    private Map<String, Integer> instructionOpcodeMapping = new HashMap<>();
    private Map<String, Integer> globalVariables = new HashMap<>();
    private boolean hasErrors = false;
    
    public ByteCodeAssembler(BytecodeDefinition.Instruction[] instructions) {
        for (int i = 1; i < instructions.length; ++i) {
            instructionOpcodeMapping.put(instructions[i].name.toLowerCase(), i);
        }
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
    
    @Override
    public void exitFunctionDeclaration(FunctionDeclarationContext ctx) {
        defineFunction(ctx.name, Integer.valueOf(ctx.a.getText()), Integer.valueOf(ctx.lo.getText()));
    }
    
    protected void gen(Token instrToken) {
        String instructionName = instrToken.getText();
        Integer opCodeI = instructionOpcodeMapping.get(instructionName);
        if (opCodeI == null) {
            System.err.println("Unknown instruction: " + instructionName);
            hasErrors = true;
            return;
        }
        
        int opcode = opCodeI.intValue();
        ensureCapacity(ip + 1);
        code[ip++] = (byte) (opcode & 0xff);
    }
    
    protected void gen(Token instrToken, Token operandToken) {
        gen(instrToken);
        genOperand(operandToken);
    }
    
    protected void gen(Token instrToken, Token oToken1, Token oToken2) {
        gen(instrToken);
        genOperand(oToken1);
        genOperand(oToken2);
    }
    
    protected void gen(Token instrToken, Token oToken1, Token oToken2, Token oToken3) {
        gen(instrToken);
        genOperand(oToken1);
        genOperand(oToken2);
        genOperand(oToken3);
    }
    
    public void genOperand(Token operandToken) {
        String text = operandToken.getText();
        int v = 0;
        
        switch (operandToken.getType()) {
            case INT:
                v = Integer.valueOf(text);
                break;
            case CHAR:
                v = Character.valueOf(text.charAt(1));
                break;
            case FLOAT:
                v = getConstantPoolIndex(Float.valueOf(text));
                break;
            case STRING:
                v = getConstantPoolIndex(String.valueOf(text));
                break;
            case ID:
                if (currentInstruction != null && currentInstruction.equals("call")) {
                    v = getFunctionIndex(text);
                } else {
                    Integer globalAddr = globalVariables.get(text);
                    if (globalAddr != null) {
                        v = globalAddr;
                    } else {
                        v = getLabelAddress(text);
                    }
                }
                break;
            case FUNC:
                v = getFunctionIndex(text);
                break;
            case REG:
                v = getRegisterNumber(operandToken);
                break;
        }
        
        ensureCapacity(ip + 4);
        code[ip++] = (byte) ((v >> 24) & 0xff);
        code[ip++] = (byte) ((v >> 16) & 0xff);
        code[ip++] = (byte) ((v >> 8) & 0xff);
        code[ip++] = (byte) (v & 0xff);
    }
    
    public byte[] getMachineCode() {
        byte[] result = new byte[ip];
        System.arraycopy(code, 0, result, 0, ip);
        return result;
    }
    
    public Object[] getConstantPool() {
        return constPool.toArray();
    }
    
    public boolean hasErrors() {
        return hasErrors;
    }
}
```

## 测试验证

### 单元测试要点
1. **字节码汇编器测试**
   - 测试指令编码正确性
   - 测试标签解析和前向引用
   - 测试常量池管理
   - 测试全局变量声明

2. **虚拟机核心功能测试**
   - 测试所有算术指令
   - 测试所有比较指令
   - 测试所有逻辑指令
   - 测试控制流指令
   - 测试函数调用和返回

3. **内存管理测试**
   - 测试栈操作（push/pop/peek）
   - 测试局部变量访问
   - 测试全局变量访问
   - 测试边界检查和异常处理

4. **垃圾回收测试**
   - 测试对象分配
   - 测试引用计数增减
   - 测试垃圾回收触发
   - 测试内存泄漏检测
   - 测试性能和统计

### 集成测试
1. **端到端测试**
   - 从源代码到字节码到执行的全流程
   - 复杂程序的编译和执行
   - 性能基准测试

2. **边界条件测试**
   - 栈溢出和下溢测试
   - 堆内存耗尽测试
   - 无限循环检测测试
   - 除零和溢出测试

3. **压力测试**
   - 大规模程序执行测试
   - 深度递归测试
   - 高频内存分配和回收

## 章节总结

本章将引导读者从理论到实践，完整掌握虚拟机设计与垃圾回收技术。通过6个递进式练习，读者将能够：

1. **理论掌握**: 理解栈式虚拟机架构和引用计数GC原理
2. **技术实现**: 实现完整的字节码解释器和自动内存管理系统
3. **实践应用**: 解决实际编译器后端执行问题
4. **工程能力**: 构建高性能、可调试的虚拟机执行平台

学习完本章后，读者将具备设计和实现现代虚拟机的能力，为后续优化章节（SSA、数据流分析）打下坚实基础。
