package org.teachfx.antlr4.ep18r.stackvm;

/**
 * 栈帧管理器
 * 统一管理CALL/RET指令的栈帧操作逻辑
 */
public class StackFrameManager {

    /**
     * 创建新的栈帧并初始化
     *
     * @param target 目标函数地址
     * @param context 执行上下文
     * @return 新创建的栈帧
     */
    public static StackFrame createStackFrame(int target, ExecutionContext context) {
        int returnAddr = context.getProgramCounter() + 4;

        FunctionSymbol func = context.getFunctionSymbol(target);
        int nargs = (func != null) ? func.nargs : 0;
        int nlocals = (func != null) ? func.nlocals : 0;

        final int NUM_CALLEE_SAVED_REGS = 5;
        int numStackArgs = Math.max(0, nargs - 6);

        int frameSize = StackOffsets.calculateFrameSize(NUM_CALLEE_SAVED_REGS, nlocals, numStackArgs);
        int frameSizeWords = frameSize / 4;
        int spAdjustment = StackOffsets.calculateSpAdjustment(NUM_CALLEE_SAVED_REGS, nlocals);

        int currentSP = context.getRegister(RegisterBytecodeDefinition.R13);
        int currentHeapPointer = context.getHeapAllocPointer();

        int newSP = currentHeapPointer;
        int newFP = newSP + frameSizeWords - 1;

        if (newSP + frameSizeWords > context.getConfig().getHeapSize()) {
            throw new OutOfMemoryError("Not enough heap space for frame: need " + frameSize + " bytes");
        }

        int oldFP = context.getRegister(RegisterBytecodeDefinition.R14);
        int fpSaveAddress = newFP + StackOffsets.FP_SAVE_OFFSET / 4;
        context.writeMemory(fpSaveAddress, oldFP);

        int localsBase = newFP + StackOffsets.FIRST_LOCAL_OFFSET / 4;
        StackFrame newFrame = new StackFrame(func, returnAddr, localsBase);

        saveCallerRegisters(newFrame, context);

        int newFramePointer = context.getFramePointer() + 1;
        context.getCallStack()[newFramePointer] = newFrame;
        context.setFramePointer(newFramePointer);

        context.setRegister(RegisterBytecodeDefinition.R13, newSP);
        context.setRegister(RegisterBytecodeDefinition.R14, newFP);
        context.setRegister(RegisterBytecodeDefinition.R15, returnAddr);

        context.setHeapAllocPointer(newSP + frameSizeWords);

        copyStackArguments(context, currentSP, newFP, numStackArgs);

        return newFrame;
    }

    /**
     * 销毁当前栈帧并恢复调用者状态
     *
     * @param context 执行上下文
     * @return 返回地址
     */
    public static int destroyStackFrame(ExecutionContext context) {
        int returnAddr;
        int currentFramePointer = context.getFramePointer();

        if (currentFramePointer < 0) {
            returnAddr = context.getRegister(RegisterBytecodeDefinition.R15);
        } else {
            StackFrame frame = context.getCallStack()[currentFramePointer];
            returnAddr = frame.returnAddress;

            restoreCallerRegisters(frame, context);

            int currentFP = context.getRegister(RegisterBytecodeDefinition.R14);
            int fpSaveAddress = currentFP + StackOffsets.FP_SAVE_OFFSET / 4;
            int prevFP = context.readMemory(fpSaveAddress);
            context.setRegister(RegisterBytecodeDefinition.R14, prevFP);

            int frameSizeWords = calculateFrameSizeWords(frame);
            int newSP = currentFP + 1 - frameSizeWords;
            context.setRegister(RegisterBytecodeDefinition.R13, newSP);

            context.setFramePointer(currentFramePointer - 1);
        }

        return returnAddr;
    }

    /**
     * 保存调用者保存寄存器到栈帧
     */
    private static void saveCallerRegisters(StackFrame frame, ExecutionContext context) {
        frame.savedCallerRegisters[0] = context.getRegister(3);
        frame.savedCallerRegisters[1] = context.getRegister(4);
        frame.savedCallerRegisters[2] = context.getRegister(5);
        frame.savedCallerRegisters[3] = context.getRegister(6);
        frame.savedCallerRegisters[4] = context.getRegister(7);
        frame.savedCallerRegisters[5] = context.getRegister(15);
        frame.savedCallerRegisters[6] = context.getRegister(1);
    }

    /**
     * 从栈帧恢复调用者保存寄存器
     */
    private static void restoreCallerRegisters(StackFrame frame, ExecutionContext context) {
        context.setRegister(3, frame.savedCallerRegisters[0]);
        context.setRegister(4, frame.savedCallerRegisters[1]);
        context.setRegister(5, frame.savedCallerRegisters[2]);
        context.setRegister(6, frame.savedCallerRegisters[3]);
        context.setRegister(7, frame.savedCallerRegisters[4]);
        context.setRegister(15, frame.savedCallerRegisters[5]);
        context.setRegister(1, frame.savedCallerRegisters[6]);
    }

    /**
     * 计算栈帧大小（字数）
     */
    private static int calculateFrameSizeWords(StackFrame frame) {
        if (frame.symbol != null) {
            int nargs = frame.symbol.nargs;
            int nlocals = frame.symbol.nlocals;
            int numStackArgs = Math.max(0, nargs - 6);
            final int NUM_CALLEE_SAVED_REGS = 5;
            int frameSize = StackOffsets.calculateFrameSize(NUM_CALLEE_SAVED_REGS, nlocals, numStackArgs);
            return frameSize / 4;
        } else {
            return 5;
        }
    }

    /**
     * 复制栈参数从调用者栈帧到被调用者栈帧
     */
    private static void copyStackArguments(ExecutionContext context, int srcSP, int dstFP, int numStackArgs) {
        if (numStackArgs <= 0) {
            return;
        }

        int srcBase = srcSP + StackOffsets.ARG_AREA_START_OFFSET / 4;
        int dstBase = dstFP + StackOffsets.ARG_AREA_START_OFFSET / 4;

        for (int i = 0; i < numStackArgs; i++) {
            int argValue = context.readMemory(srcBase + i);
            context.writeMemory(dstBase + i, argValue);
        }
    }
}
