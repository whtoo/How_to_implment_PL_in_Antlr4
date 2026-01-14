package org.teachfx.antlr4.ep18r.vizvmr.integration;

import org.teachfx.antlr4.ep18r.stackvm.StackFrame;
import org.teachfx.antlr4.ep18r.stackvm.config.VMConfig;
import org.teachfx.antlr4.ep18r.stackvm.instructions.model.RegisterBytecodeDefinition;
import org.teachfx.antlr4.ep18r.stackvm.interpreter.RegisterVMInterpreter;
import org.teachfx.antlr4.ep18r.vizvmr.core.VMRStateModel;
import org.teachfx.antlr4.ep18r.vizvmr.event.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 虚拟机插桩适配器
 * 通过适配器模式在虚拟机关键位置插入监听点，捕获执行状态变化
 */
public class VMRInstrumentation {
    private final RegisterVMInterpreter vm;
    private final VMRStateModel stateModel;
    private boolean instrumented = false;

    // 反射字段缓存
    private Field registersField;
    private Field programCounterField;
    private Field framePointerField;
    private Field heapField;
    private Field heapAllocPointerField;
    private Field callStackField;
    private Field runningField;

    public VMRInstrumentation(RegisterVMInterpreter vm, VMRStateModel stateModel) {
        this.vm = vm;
        this.stateModel = stateModel;
    }

    /**
     * 执行插桩
     * 通过反射获取虚拟机内部状态并同步到状态模型
     */
    public void instrument() {
        if (instrumented) {
            return;
        }

        try {
            // 获取私有字段
            registersField = getAccessibleField(RegisterVMInterpreter.class, "registers");
            programCounterField = getAccessibleField(RegisterVMInterpreter.class, "programCounter");
            framePointerField = getAccessibleField(RegisterVMInterpreter.class, "framePointer");
            heapField = getAccessibleField(RegisterVMInterpreter.class, "heap");
            heapAllocPointerField = getAccessibleField(RegisterVMInterpreter.class, "heapAllocPointer");
            callStackField = getAccessibleField(RegisterVMInterpreter.class, "callStack");
            runningField = getAccessibleField(RegisterVMInterpreter.class, "running");

            // 同步初始状态
            syncState();

            instrumented = true;
            System.out.println("[VMRInstrumentation] 虚拟机插桩完成");
        } catch (Exception e) {
            throw new RuntimeException("Failed to instrument VM", e);
        }
    }

    /**
     * 同步虚拟机状态到状态模型
     */
    public void syncState() {
        try {
            // 同步寄存器
            int[] registers = (int[]) registersField.get(vm);
            for (int i = 0; i < registers.length; i++) {
                if (stateModel.getRegister(i) != registers[i]) {
                    stateModel.setRegister(i, registers[i]);
                }
            }

            // 同步程序计数器
            int pc = programCounterField.getInt(vm);
            if (stateModel.getProgramCounter() != pc) {
                stateModel.setProgramCounter(pc);
            }

            // 同步帧指针
            int fp = framePointerField.getInt(vm);
            // 注意：这里不能直接设置 framePointer，因为它是私有字段且没有 setter
            // 我们需要在适当的时候通过其他方式同步

            // 同步堆
            int[] heap = (int[]) heapField.get(vm);
            for (int i = 0; i < Math.min(heap.length, 1024); i++) {
                // 只同步前1024个堆地址以提高性能
                if (stateModel.readHeap(i) != heap[i]) {
                    stateModel.writeHeap(i, heap[i]);
                }
            }

            // 同步堆分配指针
            int hap = heapAllocPointerField.getInt(vm);
            if (stateModel.getHeapAllocPointer() != hap) {
                // 这里需要通过反射设置，但我们不能直接修改
                // 堆分配指针是内部状态，可以通过allocateHeap间接修改
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to sync VM state", e);
        }
    }

    /**
     * 在指令执行前调用
     */
    public void beforeInstructionExecute(int pc, int opcode) {
        stateModel.setProgramCounter(pc);
        stateModel.incrementExecutionStep();

        // 通知指令执行
        RegisterBytecodeDefinition.Instruction instr = RegisterBytecodeDefinition.getInstruction(opcode);
        String mnemonic = (instr != null) ? instr.name : "UNKNOWN";
        stateModel.notifyInstructionExecuted(pc, opcode, mnemonic, "");
    }

    /**
     * 在寄存器写入后调用
     */
    public void afterRegisterWrite(int regNum, int oldValue, int newValue) {
        stateModel.setRegister(regNum, newValue);
    }

    /**
     * 在内存写入后调用
     */
    public void afterMemoryWrite(int address, int oldValue, int newValue, String memoryType) {
        MemoryChangeEvent.MemoryType type;
        switch (memoryType.toUpperCase()) {
            case "HEAP":
                type = MemoryChangeEvent.MemoryType.HEAP;
                stateModel.writeHeap(address, newValue);
                break;
            case "GLOBAL":
                type = MemoryChangeEvent.MemoryType.GLOBAL;
                stateModel.writeGlobal(address, newValue);
                break;
            default:
                type = MemoryChangeEvent.MemoryType.HEAP;
                stateModel.writeHeap(address, newValue);
        }
    }

    /**
     * 在栈帧变化后调用
     */
    public void afterStackFramePush(StackFrame frame) {
        stateModel.pushStackFrame(frame);
    }

    public void afterStackFramePop() {
        stateModel.popStackFrame();
    }

    /**
     * 检查虚拟机是否正在运行
     */
    public boolean isVMRunning() {
        try {
            return runningField.getBoolean(vm);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取程序计数器
     */
    public int getProgramCounter() {
        try {
            return programCounterField.getInt(vm);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 获取寄存器值
     */
    public int getRegister(int regNum) {
        try {
            int[] registers = (int[]) registersField.get(vm);
            return registers[regNum];
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 获取堆分配指针
     */
    public int getHeapAllocPointer() {
        try {
            return heapAllocPointerField.getInt(vm);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 获取帧指针
     */
    public int getFramePointer() {
        try {
            return framePointerField.getInt(vm);
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * 获取当前栈帧
     */
    public StackFrame getCurrentFrame() {
        try {
            int fp = framePointerField.getInt(vm);
            StackFrame[] callStack = (StackFrame[]) callStackField.get(vm);
            if (fp >= 0 && fp < callStack.length) {
                return callStack[fp];
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取字节码
     */
    public byte[] getCode() {
        return vm.getCode();
    }

    /**
     * 获取常量池
     */
    public Object[] getConstantPool() {
        try {
            // 使用反射获取常量池
            Field constPoolField = getAccessibleField(RegisterVMInterpreter.class, "constPool");
            return (Object[]) constPoolField.get(vm);
        } catch (Exception e) {
            return new Object[0];
        }
    }

    /**
     * 获取代码大小
     */
    public int getCodeSize() {
        return vm.getCodeSize();
    }

    /**
     * 辅助方法：获取可访问的字段
     */
    private Field getAccessibleField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field;
    }

    /**
     * 检查是否已插桩
     */
    public boolean isInstrumented() {
        return instrumented;
    }
}
