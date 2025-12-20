package org.teachfx.antlr4.ep18.stackvm;

import java.util.Arrays;

/**
 * CymbolStackVM - 基于栈的虚拟机实现
 * 执行字节码指令，管理堆栈和内存
 */
public class CymbolStackVM {
    // 虚拟机配置
    private final VMConfig config;
    private final VMStats stats;   // 性能统计

    // 运行时数据结构
    private int[] stack;           // 操作数栈
    private int stackPointer;      // 栈指针
    private int[] heap;            // 堆内存
    private int[] instructionCache; // 指令缓存
    private int[] locals;          // 局部变量数组
    private StackFrame[] callStack; // 调用栈
    private int framePointer;      // 当前帧指针

    // 调试支持
    private java.util.Set<Integer> breakpoints; // 断点集合
    private boolean stepMode;                   // 单步执行模式
    private boolean waitingForStep;             // 等待单步执行

    // 堆分配指针
    private int heapAllocPointer;  // 下一个可用堆地址

    // 结构体管理（统一表示适配层）
    private java.util.List<StructValue> structTable;  // 结构体实例表
    private int nextStructId;  // 下一个结构体ID（0保留给null）

    // 执行状态
    private boolean running;
    private int programCounter;    // 程序计数器

    // 异常处理
    private VMExceptionHandler exceptionHandler;
    private VMExceptionMonitor exceptionMonitor;
    
    /**
     * 构造函数 - 使用配置创建虚拟机实例
     * @param config 虚拟机配置
     */
    public CymbolStackVM(VMConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("VMConfig cannot be null");
        }
        this.config = config;
        this.stats = new VMStats();
        this.exceptionHandler = new VMExceptionHandler();
        this.exceptionMonitor = new VMExceptionMonitor();
        initializeVM();
    }
    
    /**
     * 初始化虚拟机运行时环境
     */
    private void initializeVM() {
        // 初始化堆内存
        this.heap = new int[config.getHeapSize()];
        this.heapAllocPointer = 0;

        // 初始化结构体管理
        this.structTable = new java.util.ArrayList<>();
        this.nextStructId = 1; // 0保留给null引用

        // 初始化操作数栈
        this.stack = new int[config.getStackSize()];
        this.stackPointer = 0;

        // 初始化指令缓存
        this.instructionCache = new int[config.getInstructionCacheSize()];

        // 初始化局部变量数组（使用栈大小）
        this.locals = new int[config.getStackSize()];

        // 初始化调用栈
        this.callStack = new StackFrame[config.getMaxFrameCount()];
        this.framePointer = -1;

        // 初始化调试支持
        this.breakpoints = new java.util.HashSet<>();
        this.stepMode = false;
        this.waitingForStep = false;

        // 初始化执行状态
        this.running = false;
        this.programCounter = 0;

        // 如果是调试模式，输出初始化信息
        if (config.isDebugMode()) {
            System.out.println("VM initialized with config: " + config);
        }
    }
    
    /**
     * 执行字节码
     * @param bytecode 字节码数组
     * @return 执行结果
     * @throws Exception 执行异常
     */
    public int execute(byte[] bytecode) throws Exception {
        if (bytecode == null || bytecode.length == 0) {
            throw new IllegalArgumentException("Bytecode cannot be null or empty");
        }

        long startTime = System.nanoTime();
        long startMemory = getUsedMemory();

        // 加载字节码到指令缓存
        loadBytecode(bytecode);

        // 开始执行
        this.running = true;
        this.programCounter = 0;

        try {
            while (running && programCounter < instructionCache.length) {
                // 调试支持：检查断点
                if (breakpoints.contains(programCounter)) {
                    System.out.println("[BREAKPOINT] Hit breakpoint at PC=" + programCounter);
                    // 在实际调试器中，这里会暂停并等待用户输入
                    // 简化实现：仅打印信息并继续
                }

                // 获取当前指令
                int instruction = instructionCache[programCounter++];

                // 执行指令
                executeInstruction(instruction);

                // 调试支持：单步执行模式
                if (stepMode) {
                    System.out.println("[STEP] Executed instruction at PC=" + (programCounter - 1));
                    stepMode = false; // 执行一步后退出单步模式
                }
            }

            // 返回栈顶值作为结果
            int result = stackPointer > 0 ? stack[stackPointer - 1] : 0;

            // 记录性能统计
            long endTime = System.nanoTime();
            long endMemory = getUsedMemory();
            stats.recordExecution(startTime);
            stats.recordMemoryUsage(endMemory);

            if (config.isDebugMode()) {
                System.out.println("Execution completed successfully");
            }

            return result;
        } catch (Exception e) {
            // 记录错误统计
            stats.recordError(e);

            if (config.isVerboseErrors()) {
                System.err.println("VM execution error at PC=" + (programCounter - 1) + ": " + e.getMessage());
            }
            throw e;
        } finally {
            this.running = false;
        }
    }
    
    /**
     * 加载字节码到指令缓存
     * @param bytecode 字节码数组
     */
    private void loadBytecode(byte[] bytecode) {
        // 将字节数组转换为整数数组（4字节一个指令）
        int instructionCount = Math.min(bytecode.length / 4, instructionCache.length);
        for (int i = 0; i < instructionCount; i++) {
            int offset = i * 4;
            instructionCache[i] = ((bytecode[offset] & 0xFF) << 24) |
                                   ((bytecode[offset + 1] & 0xFF) << 16) |
                                   ((bytecode[offset + 2] & 0xFF) << 8) |
                                   (bytecode[offset + 3] & 0xFF);
        }
        
        if (config.isDebugMode()) {
            System.out.println("Loaded " + instructionCount + " instructions");
        }
    }
    
    /**
     * 从指令中提取操作数（低24位，有符号扩展）
     * @param instruction 指令
     * @return 操作数值
     */
    private int extractOperand(int instruction) {
        int value = instruction & 0xFFFFFF;
        // 处理符号扩展（如果最高位是1，则为负数）
        if ((value & 0x800000) != 0) {
            value |= 0xFF000000; // 符号扩展
        }
        return value;
    }

    /**
     * 将int值转换为Object（用于StructValue字段存储）
     * @param value int值
     * @return Object值（Integer或null）
     */
    private Object intToValue(int value) {
        return value; // int自动装箱为Integer
    }

    /**
     * 将Object值转换为int（用于从StructValue字段加载）
     * @param obj Object值
     * @return int值
     * @throws ClassCastException 如果obj不是Integer
     */
    private int valueToInt(Object obj) {
        if (obj == null) {
            return 0; // null视为0
        }
        if (obj instanceof Integer) {
            return (Integer) obj;
        }
        throw new ClassCastException("Expected Integer but got: " + obj.getClass());
    }

    /**
     * 执行单条指令
     * @param instruction 指令
     * @throws Exception 执行异常
     */
    private void executeInstruction(int instruction) throws Exception {
        // 提取操作码（高8位）
        int opcode = (instruction >> 24) & 0xFF;

        if (config.isTraceEnabled()) {
            System.out.println("Executing instruction at PC=" + (programCounter - 1) +
                             ": opcode=0x" + Integer.toHexString(opcode) +
                             ", instruction=0x" + Integer.toHexString(instruction));
        }

        // 使用指令工厂获取指令实现
        org.teachfx.antlr4.ep18.stackvm.instructions.InstructionFactory factory =
            org.teachfx.antlr4.ep18.stackvm.instructions.InstructionFactory.getInstance();

        // 检查指令是否已重构（使用新的策略模式）
        if (factory.isSupported(opcode)) {
            // 使用新的策略模式执行指令
            org.teachfx.antlr4.ep18.stackvm.instructions.Instruction instr = factory.getRequiredInstruction(opcode);
            int operand = extractOperand(instruction);

            // 创建执行上下文
            VMExecutionContext context = new VMExecutionContext(
                this, config, stats, programCounter, stack, stackPointer,
                heap, locals, callStack, framePointer, config.isTraceEnabled(),
                heapAllocPointer, structTable, nextStructId
            );

            // 设置异常处理器和监控器
            context.setExceptionHandler(exceptionHandler);
            context.setExceptionMonitor(exceptionMonitor);

            // 执行指令（带异常处理）
            try {
                instr.execute(context, operand);

                // 更新VM状态
                programCounter = context.getProgramCounter();
                stackPointer = context.getStackPointer();
                framePointer = context.getFramePointer();
                heapAllocPointer = context.getHeapAllocPointer();
                nextStructId = context.getNextStructId();
            } catch (VMException e) {
                // 记录异常到监控器
                if (exceptionMonitor != null) {
                    exceptionMonitor.recordException(e, false, 0);
                }

                // 尝试使用异常处理器处理
                boolean handled = false;
                if (exceptionHandler != null) {
                    handled = exceptionHandler.handleException(e, context);
                }

                // 如果异常未被处理，重新抛出
                if (!handled) {
                    throw e;
                }

                // 异常已处理，更新VM状态
                programCounter = context.getProgramCounter();
                stackPointer = context.getStackPointer();
                framePointer = context.getFramePointer();
                heapAllocPointer = context.getHeapAllocPointer();
                nextStructId = context.getNextStructId();
            }
        } else {
            // 暂时回退到原有的switch实现（向后兼容）
            try {
                executeInstructionLegacy(instruction);
            } catch (VMException e) {
                // 记录异常到监控器
                if (exceptionMonitor != null) {
                    exceptionMonitor.recordException(e, false, 0);
                }

                // 尝试使用异常处理器处理
                boolean handled = false;
                if (exceptionHandler != null) {
                    VMExecutionContext context = new VMExecutionContext(
                        this, config, stats, programCounter, stack, stackPointer,
                        heap, locals, callStack, framePointer, config.isTraceEnabled(),
                        heapAllocPointer, structTable, nextStructId
                    );
                    context.setExceptionHandler(exceptionHandler);
                    context.setExceptionMonitor(exceptionMonitor);
                    handled = exceptionHandler.handleException(e, context);
                }

                // 如果异常未被处理，重新抛出
                if (!handled) {
                    throw e;
                }
            }
        }
    }

    /**
     * 原有指令执行方法（保持向后兼容）
     * 将在所有指令迁移到新模式后删除
     */
    private void executeInstructionLegacy(int instruction) throws Exception {
        // 提取操作码（高8位）
        int opcode = (instruction >> 24) & 0xFF;

        if (config.isTraceEnabled()) {
            System.out.println("Executing instruction (legacy) at PC=" + (programCounter - 1) +
                             ": opcode=0x" + Integer.toHexString(opcode) +
                             ", instruction=0x" + Integer.toHexString(instruction));
        }

        // 根据操作码执行指令
        switch (opcode) {
            case BytecodeDefinition.INSTR_IADD:
                executeIAdd();
                break;
            case BytecodeDefinition.INSTR_ISUB:
                executeISub();
                break;
            case BytecodeDefinition.INSTR_IMUL:
                executeIMul();
                break;
            case BytecodeDefinition.INSTR_IDIV:
                executeIDiv();
                break;
            case BytecodeDefinition.INSTR_ILT:
                executeILt();
                break;
            case BytecodeDefinition.INSTR_ILE:
                executeILe();
                break;
            case BytecodeDefinition.INSTR_IGT:
                executeIGt();
                break;
            case BytecodeDefinition.INSTR_IGE:
                executeIGe();
                break;
            case BytecodeDefinition.INSTR_IEQ:
                executeIEq();
                break;
            case BytecodeDefinition.INSTR_INE:
                executeINe();
                break;
            case BytecodeDefinition.INSTR_INEG:
                executeINeg();
                break;
            case BytecodeDefinition.INSTR_INOT:
                executeINot();
                break;
            case BytecodeDefinition.INSTR_IAND:
                executeIAnd();
                break;
            case BytecodeDefinition.INSTR_IOR:
                executeIOr();
                break;
            case BytecodeDefinition.INSTR_IXOR:
                executeIXor();
                break;
            case BytecodeDefinition.INSTR_ICONST:
                executeIConst(instruction);
                break;
            case BytecodeDefinition.INSTR_HALT:
                executeHalt();
                break;
            case BytecodeDefinition.INSTR_BR:
                executeBr(instruction);
                break;
            case BytecodeDefinition.INSTR_BRT:
                executeBrt(instruction);
                break;
            case BytecodeDefinition.INSTR_BRF:
                executeBrf(instruction);
                break;
            case BytecodeDefinition.INSTR_LOAD:
                executeLoad(instruction);
                break;
            case BytecodeDefinition.INSTR_STORE:
                executeStore(instruction);
                break;
            case BytecodeDefinition.INSTR_GLOAD:
                executeGload(instruction);
                break;
            case BytecodeDefinition.INSTR_GSTORE:
                executeGstore(instruction);
                break;
            case BytecodeDefinition.INSTR_FLOAD:
                executeFload(instruction);
                break;
            case BytecodeDefinition.INSTR_FSTORE:
                executeFstore(instruction);
                break;
            case BytecodeDefinition.INSTR_FADD:
                executeFadd();
                break;
            case BytecodeDefinition.INSTR_FSUB:
                executeFsub();
                break;
            case BytecodeDefinition.INSTR_FMUL:
                executeFmul();
                break;
            case BytecodeDefinition.INSTR_FDIV:
                executeFdiv();
                break;
            case BytecodeDefinition.INSTR_FLT:
                executeFlt();
                break;
            case BytecodeDefinition.INSTR_FEQ:
                executeFeq();
                break;
            case BytecodeDefinition.INSTR_ITOF:
                executeItof();
                break;
            case BytecodeDefinition.INSTR_CALL:
                executeCall(instruction);
                break;
            case BytecodeDefinition.INSTR_RET:
                executeRet();
                break;
            case BytecodeDefinition.INSTR_STRUCT:
                executeStruct(instruction);
                break;
            case BytecodeDefinition.INSTR_NULL:
                executeNull();
                break;
            case BytecodeDefinition.INSTR_POP:
                executePop();
                break;
            case BytecodeDefinition.INSTR_PRINT:
                executePrint();
                break;
            default:
                throw new VMInvalidOpcodeException(
                    "Unsupported opcode: 0x" + Integer.toHexString(opcode),
                    programCounter - 1, opcode);
        }
    }

    // 指令执行方法实现

    private void executeStruct(int instruction) {
        int nfields = extractOperand(instruction);
        // 保持向后兼容性：检查堆空间是否足够（模拟原有行为）
        if (heapAllocPointer + nfields > heap.length) {
            throw new OutOfMemoryError("Not enough heap space for struct with " + nfields + " fields");
        }
        // 创建StructValue实例
        StructValue struct = new StructValue(nfields);
        // 初始化字段为0（Integer）
        for (int i = 0; i < nfields; i++) {
            struct.setField(i, 0); // int自动装箱为Integer
        }
        // 添加到结构体表并分配ID
        structTable.add(struct);
        int structId = nextStructId++;
        // 压入结构体ID（兼容现有代码）
        push(structId);
        // 更新堆分配指针以模拟堆分配（保持兼容性）
        heapAllocPointer += nfields;
    }

    private void executeNull() {
        // 将0作为null引用压入栈
        push(0);
    }

    private void executePop() {
        pop(); // 直接弹出，忽略值
    }

    private void executePrint() {
        int value = pop();
        System.out.println(value);
    }

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

    private void executeILt() {
        int b = pop();
        int a = pop();
        push(a < b ? 1 : 0);
    }

    private void executeILe() {
        int b = pop();
        int a = pop();
        push(a <= b ? 1 : 0);
    }

    private void executeIGt() {
        int b = pop();
        int a = pop();
        push(a > b ? 1 : 0);
    }

    private void executeIGe() {
        int b = pop();
        int a = pop();
        push(a >= b ? 1 : 0);
    }

    private void executeIEq() {
        int b = pop();
        int a = pop();
        push(a == b ? 1 : 0);
    }

    private void executeINe() {
        int b = pop();
        int a = pop();
        push(a != b ? 1 : 0);
    }

    private void executeINeg() {
        int a = pop();
        push(-a);
    }

    private void executeINot() {
        int a = pop();
        push(a == 0 ? 1 : 0);
    }

    private void executeIAnd() {
        int b = pop();
        int a = pop();
        push(a & b);
    }

    private void executeIOr() {
        int b = pop();
        int a = pop();
        push(a | b);
    }

    private void executeIXor() {
        int b = pop();
        int a = pop();
        push(a ^ b);
    }

    private void executeIConst(int instruction) {
        int value = extractOperand(instruction);
        push(value);
    }

    private void executeHalt() {
        this.running = false;
    }

    private void executeBr(int instruction) {
        int address = extractOperand(instruction);
        // 跳转到指定地址（地址以指令为单位，需要乘以4转换为字节索引？）
        // 在CymbolStackVM中，instructionCache是int数组，每个元素是一条指令
        // programCounter指向instructionCache的索引，所以address直接作为索引
        programCounter = address;
    }

    private void executeBrt(int instruction) {
        int address = extractOperand(instruction);
        int condition = pop();
        if (condition != 0) {
            programCounter = address;
        }
    }

    private void executeBrf(int instruction) {
        int address = extractOperand(instruction);
        int condition = pop();
        if (condition == 0) {
            programCounter = address;
        }
    }

    private void executeLoad(int instruction) {
        int index = extractOperand(instruction);
        if (index < 0 || index >= locals.length) {
            throw VMMemoryAccessException.outOfBounds(programCounter, "LOAD", index, 0, locals.length - 1, VMMemoryException.MemoryAccessType.READ);
        }
        push(locals[index]);
    }

    private void executeStore(int instruction) {
        int index = extractOperand(instruction);
        if (index < 0 || index >= locals.length) {
            throw VMMemoryAccessException.outOfBounds(programCounter, "STORE", index, 0, locals.length - 1, VMMemoryException.MemoryAccessType.WRITE);
        }
        int value = pop();
        locals[index] = value;
    }

    private void executeGload(int instruction) {
        int address = extractOperand(instruction);
        if (address < 0 || address >= heap.length) {
            throw VMMemoryAccessException.outOfBounds(programCounter, "GLOAD", address, 0, heap.length - 1, VMMemoryException.MemoryAccessType.READ);
        }
        push(heap[address]);
    }

    private void executeGstore(int instruction) {
        int address = extractOperand(instruction);
        if (address < 0 || address >= heap.length) {
            throw VMMemoryAccessException.outOfBounds(programCounter, "GSTORE", address, 0, heap.length - 1, VMMemoryException.MemoryAccessType.WRITE);
        }
        int value = pop();
        heap[address] = value;
    }

    private void executeFload(int instruction) {
        // FLOAD: 从结构体加载字段
        // 操作数：字段偏移量
        // 栈顶：结构体引用（可能是ID或堆地址）
        int fieldOffset = extractOperand(instruction);
        int structRef = pop(); // 结构体引用

        // 检查null引用（0表示null）
        if (structRef == 0) {
            throw VMMemoryAccessException.nullPointer(programCounter, "FLOAD", VMMemoryException.MemoryAccessType.READ);
        }

        // 判断structRef是结构体ID还是堆地址
        // 结构体ID从1开始，且ID-1必须在structTable范围内
        int structIndex = structRef - 1;
        if (structIndex >= 0 && structIndex < structTable.size()) {
            // 有效的结构体ID：使用StructValue
            StructValue struct = structTable.get(structIndex);
            Object fieldValue = struct.getField(fieldOffset);
            int intValue = valueToInt(fieldValue);
            push(intValue);
        } else if (structRef >= 0 && structRef < heap.length) {
            // 堆地址：回退到原有堆访问逻辑（保持兼容性）
            int actualAddress = structRef + fieldOffset;
            if (actualAddress < 0 || actualAddress >= heap.length) {
                throw VMMemoryAccessException.outOfBounds(programCounter, "FLOAD", actualAddress, 0, heap.length - 1, VMMemoryException.MemoryAccessType.READ);
            }
            push(heap[actualAddress]);
        } else {
            throw new VMMemoryAccessException("Invalid struct reference: " + structRef, programCounter, "FLOAD", (long)structRef, 0, VMMemoryException.MemoryAccessType.READ);
        }
    }

    private void executeFstore(int instruction) {
        // FSTORE: 存储值到结构体字段
        // 操作数：字段偏移量
        // 栈顶：值，下一个：结构体引用
        int fieldOffset = extractOperand(instruction);
        int value = pop();
        int structRef = pop();

        // 检查null引用（0表示null）
        if (structRef == 0) {
            throw VMMemoryAccessException.nullPointer(programCounter, "FSTORE", VMMemoryException.MemoryAccessType.WRITE);
        }

        // 判断structRef是结构体ID还是堆地址
        // 结构体ID从1开始，且ID-1必须在structTable范围内
        int structIndex = structRef - 1;
        if (structIndex >= 0 && structIndex < structTable.size()) {
            // 有效的结构体ID：使用StructValue
            StructValue struct = structTable.get(structIndex);
            Object fieldValue = intToValue(value);
            struct.setField(fieldOffset, fieldValue);
        } else if (structRef >= 0 && structRef < heap.length) {
            // 堆地址：回退到原有堆访问逻辑（保持兼容性）
            int actualAddress = structRef + fieldOffset;
            if (actualAddress < 0 || actualAddress >= heap.length) {
                throw VMMemoryAccessException.outOfBounds(programCounter, "FSTORE", actualAddress, 0, heap.length - 1, VMMemoryException.MemoryAccessType.WRITE);
            }
            heap[actualAddress] = value;
        } else {
            throw new VMMemoryAccessException("Invalid struct reference: " + structRef, programCounter, "FSTORE", (long)structRef, 0, VMMemoryException.MemoryAccessType.WRITE);
        }
    }

    private void executeCall(int instruction) {
        // CALL指令：操作数为目标地址（简化）
        int targetAddress = extractOperand(instruction);
        // 保存返回地址（当前programCounter，指向下一条指令）
        int returnAddress = programCounter;
        // 创建虚拟FunctionSymbol用于栈帧
        FunctionSymbol dummySymbol = new FunctionSymbol("dummy", 0, 0, targetAddress);
        // 创建栈帧
        StackFrame frame = new StackFrame(dummySymbol, returnAddress);
        // 保存当前局部变量状态？简化处理
        callStack[++framePointer] = frame;
        // 跳转到目标地址
        programCounter = targetAddress;
    }

    private void executeRet() {
        // RET指令：从栈帧恢复返回地址
        if (framePointer < 0) {
            throw new VMStackUnderflowException("RET called without active frame", programCounter, "RET");
        }
        StackFrame frame = callStack[framePointer--];
        programCounter = frame.getReturnAddress();
    }

    // 浮点指令实现

    private void executeFadd() {
        int bBits = pop();
        int aBits = pop();
        float a = Float.intBitsToFloat(aBits);
        float b = Float.intBitsToFloat(bBits);
        float result = a + b;
        push(Float.floatToIntBits(result));
    }

    private void executeFsub() {
        int bBits = pop();
        int aBits = pop();
        float a = Float.intBitsToFloat(aBits);
        float b = Float.intBitsToFloat(bBits);
        float result = a - b;
        push(Float.floatToIntBits(result));
    }

    private void executeFmul() {
        int bBits = pop();
        int aBits = pop();
        float a = Float.intBitsToFloat(aBits);
        float b = Float.intBitsToFloat(bBits);
        float result = a * b;
        push(Float.floatToIntBits(result));
    }

    private void executeFdiv() {
        int bBits = pop();
        int aBits = pop();
        float a = Float.intBitsToFloat(aBits);
        float b = Float.intBitsToFloat(bBits);
        if (b == 0.0f) {
            throw new VMDivisionByZeroException(programCounter - 1, "FDIV");
        }
        float result = a / b;
        push(Float.floatToIntBits(result));
    }

    private void executeFlt() {
        int bBits = pop();
        int aBits = pop();
        float a = Float.intBitsToFloat(aBits);
        float b = Float.intBitsToFloat(bBits);
        push(a < b ? 1 : 0);
    }

    private void executeFeq() {
        int bBits = pop();
        int aBits = pop();
        float a = Float.intBitsToFloat(aBits);
        float b = Float.intBitsToFloat(bBits);
        push(a == b ? 1 : 0);
    }

    private void executeItof() {
        int a = pop();
        float result = (float) a;
        push(Float.floatToIntBits(result));
    }
    
    /**
     * 将值压入栈
     * @param value 值
     */
    protected void push(int value) {
        if (stackPointer >= stack.length) {
            throw new VMStackOverflowException("Stack overflow at PC=" + programCounter, programCounter, "PUSH");
        }
        stack[stackPointer++] = value;
    }

    /**
     * 从栈弹出值
     * @return 栈顶值
     */
    protected int pop() {
        if (stackPointer <= 0) {
            throw new VMStackUnderflowException("Stack underflow at PC=" + programCounter, programCounter, "POP");
        }
        return stack[--stackPointer];
    }

    /**
     * 查看栈顶值（不弹出）
     * @return 栈顶值
     */
    protected int peek() {
        if (stackPointer <= 0) {
            throw new VMStackUnderflowException("Stack is empty at PC=" + programCounter, programCounter, "PEEK");
        }
        return stack[stackPointer - 1];
    }
    
    
    /**
     * 检查虚拟机是否正在运行
     * @return 是否运行中
     */
    public boolean isRunning() {
        return running;
    }
    
    /**
     * 停止虚拟机执行
     */
    public void stop() {
        this.running = false;
    }

    /**
     * 获取虚拟机统计信息
     * @return 统计信息
     */
    public VMStats getStats() {
        return stats;
    }

    /**
     * 获取已使用的内存量
     * @return 已使用的内存（字节）
     */
    private long getUsedMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    /**
     * 获取栈深度
     * @return 当前栈深度
     */
    public int getStackDepth() {
        return stackPointer;
    }

    /**
     * 检查栈是否为空
     * @return 是否为空
     */
    public boolean isStackEmpty() {
        return stackPointer <= 0;
    }

    /**
     * 获取配置
     * @return 配置对象
     */
    public VMConfig getConfig() {
        return config;
    }

    // 调试功能

    /**
     * 设置断点
     * @param address 指令地址（programCounter值）
     */
    public void setBreakpoint(int address) {
        if (address < 0 || address >= instructionCache.length) {
            throw new IllegalArgumentException("Breakpoint address out of range: " + address);
        }
        breakpoints.add(address);
        if (config.isDebugMode()) {
            System.out.println("[DEBUG] Breakpoint set at PC=" + address);
        }
    }

    /**
     * 清除断点
     * @param address 指令地址
     */
    public void clearBreakpoint(int address) {
        breakpoints.remove(address);
        if (config.isDebugMode()) {
            System.out.println("[DEBUG] Breakpoint cleared at PC=" + address);
        }
    }

    /**
     * 清除所有断点
     */
    public void clearAllBreakpoints() {
        breakpoints.clear();
        if (config.isDebugMode()) {
            System.out.println("[DEBUG] All breakpoints cleared");
        }
    }

    /**
     * 启用单步执行模式（下一步指令执行后暂停）
     */
    public void step() {
        this.stepMode = true;
        if (config.isDebugMode()) {
            System.out.println("[DEBUG] Step mode enabled");
        }
    }

    /**
     * 获取当前断点集合
     * @return 断点地址集合
     */
    public java.util.Set<Integer> getBreakpoints() {
        return new java.util.HashSet<>(breakpoints);
    }

    /**
     * 函数调用 - 供新的CallInstruction使用
     * @param functionAddress 函数入口地址
     * @param returnAddress 返回地址
     */
    public void callFunction(int functionAddress, int returnAddress) {
        // 创建虚拟FunctionSymbol用于栈帧
        FunctionSymbol dummySymbol = new FunctionSymbol("func_" + functionAddress, 0, 0, functionAddress);
        // 创建栈帧
        StackFrame frame = new StackFrame(dummySymbol, returnAddress);
        // 压入调用栈
        if (framePointer + 1 >= callStack.length) {
            throw new VMStackOverflowException("Call stack overflow at PC=" + programCounter, programCounter, "CALL");
        }
        callStack[++framePointer] = frame;
        // 跳转到目标地址
        programCounter = functionAddress;
    }

    /**
     * 函数返回 - 供新的RetInstruction使用
     */
    public void returnFromFunction() {
        // 从栈帧恢复返回地址
        if (framePointer < 0) {
            throw new VMStackUnderflowException("RET called without active frame at PC=" + programCounter, programCounter, "RET");
        }
        StackFrame frame = callStack[framePointer--];
        programCounter = frame.getReturnAddress();
    }

    /**
     * 获取程序计数器
     * @return 当前程序计数器值
     */
    public int getProgramCounter() {
        return programCounter;
    }

    /**
     * 获取异常处理器
     * @return 异常处理器
     */
    public VMExceptionHandler getExceptionHandler() {
        return exceptionHandler;
    }

    /**
     * 设置异常处理器
     * @param exceptionHandler 异常处理器
     */
    public void setExceptionHandler(VMExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    /**
     * 获取异常监控器
     * @return 异常监控器
     */
    public VMExceptionMonitor getExceptionMonitor() {
        return exceptionMonitor;
    }

    /**
     * 设置异常监控器
     * @param exceptionMonitor 异常监控器
     */
    public void setExceptionMonitor(VMExceptionMonitor exceptionMonitor) {
        this.exceptionMonitor = exceptionMonitor;
    }
}
