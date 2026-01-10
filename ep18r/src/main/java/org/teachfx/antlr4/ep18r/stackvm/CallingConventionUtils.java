package org.teachfx.antlr4.ep18r.stackvm;

public class CallingConventionUtils {

    private CallingConventionUtils() {}

    public static void saveCallerRegisters(StackFrame frame, int regMask) {
        RegisterSaver.saveCallerRegisters(frame, regMask);
    }

    public static void restoreCallerRegisters(StackFrame frame, int regMask) {
        RegisterSaver.restoreCallerRegisters(frame, regMask, null);
    }

    public static int saveCalleeRegisters(StackFrame frame, int regMask) {
        return RegisterSaver.saveCalleeRegisters(frame, regMask);
    }

    public static void restoreCalleeRegisters(StackFrame frame, int regMask) {
        RegisterSaver.restoreCalleeRegisters(frame, regMask, null);
    }

    public static String generatePrologue(int numLocals, int usedCalleeRegMask) {
        StringBuilder sb = new StringBuilder();

        int numCalleeRegs = Integer.bitCount(usedCalleeRegMask & 0x1F00);
        int frameSize = StackOffsets.calculateFrameSize(numCalleeRegs, numLocals, 0);
        int spAdjustment = StackOffsets.calculateSpAdjustment(numCalleeRegs, numLocals);

        sb.append(String.format("    addi sp, sp, %d      # 分配栈帧，大小=%d字节\n", spAdjustment, frameSize));
        sb.append(String.format("    sw fp, %d(sp)        # 保存旧帧指针\n", StackOffsets.FP_SAVE_OFFSET));

        int fpOffset = frameSize - 4;
        sb.append(String.format("    addi fp, sp, %d      # 设置新帧指针\n", fpOffset));

        for (int i = 8; i <= 12; i++) {
            if ((usedCalleeRegMask & (1 << i)) != 0) {
                int offset = StackOffsets.S0_SAVE_OFFSET + (i - 8) * 4;
                String regName = StackOffsets.getAbiName(i);
                sb.append(String.format("    sw %s, %d(fp)      # 保存%s\n", regName, offset, regName));
            }
        }

        return sb.toString();
    }

    public static String generateEpilogue(int numLocals, int usedCalleeRegMask) {
        StringBuilder sb = new StringBuilder();

        for (int i = 12; i >= 8; i--) {
            if ((usedCalleeRegMask & (1 << i)) != 0) {
                int offset = StackOffsets.S0_SAVE_OFFSET + (i - 8) * 4;
                String regName = StackOffsets.getAbiName(i);
                sb.append(String.format("    lw %s, %d(fp)      # 恢复%s\n", regName, offset, regName));
            }
        }

        sb.append(String.format("    lw fp, %d(sp)        # 恢复旧帧指针\n", StackOffsets.FP_SAVE_OFFSET));

        int numCalleeRegs = Integer.bitCount(usedCalleeRegMask & 0x1F00);
        int frameSize = StackOffsets.calculateFrameSize(numCalleeRegs, numLocals, 0);

        sb.append(String.format("    addi sp, sp, %d      # 释放栈帧\n", frameSize));
        sb.append("    # ret                     # 函数返回（由调用者添加）\n");

        return sb.toString();
    }

    public static int getStackArgOffset(int argIndex) {
        return ParameterPassing.getStackArgOffset(argIndex);
    }

    public static int getArgRegister(int argIndex) {
        return ParameterPassing.getArgRegister(argIndex);
    }

    public static String getArgRegisterName(int argIndex) {
        return ParameterPassing.getArgRegisterName(argIndex);
    }

    public static boolean isStackArg(int argIndex) {
        return ParameterPassing.isStackArg(argIndex);
    }

    public static String generateArgPassing(int argIndex, String value, boolean isImmediate) {
        return ParameterPassing.generateArgPassing(argIndex, value, isImmediate);
    }

    public static int getReturnValueRegister() {
        return ParameterPassing.getReturnValueRegister();
    }

    public static String getReturnValueRegisterName() {
        return ParameterPassing.getReturnValueRegisterName();
    }

    public static String generateReturnValue(String value, boolean isImmediate) {
        return ParameterPassing.generateReturnValue(value, isImmediate);
    }

    public static int createRegMask(int... regNumbers) {
        return ABIValidator.createRegMask(regNumbers);
    }

    public static int createRegMaskFromNames(String... regNames) {
        return ABIValidator.createRegMaskFromNames(regNames);
    }

    public static boolean isRegisterInMask(int mask, int regNum) {
        return ABIValidator.isRegisterInMask(mask, regNum);
    }

    public static int countRegistersInMask(int mask) {
        return ABIValidator.countRegistersInMask(mask);
    }

    public static int getTargetAbiReturnValueRegister() {
        return ParameterPassing.getTargetAbiReturnValueRegister();
    }

    public static int getCurrentReturnValueRegister() {
        return ParameterPassing.getCurrentReturnValueRegister();
    }

    public static int getReturnValueRegister(boolean useTargetAbi) {
        return ParameterPassing.getReturnValueRegister(useTargetAbi);
    }

    public static int getArgumentRegister(int argIndex) {
        return ParameterPassing.getArgumentRegister(argIndex);
    }

    public static String getArgumentRegisterName(int argIndex) {
        return ParameterPassing.getArgumentRegisterName(argIndex);
    }

    public static boolean isArgumentRegister(int regNum) {
        return ParameterPassing.isArgumentRegister(regNum);
    }

    public static int getArgumentIndex(int regNum) {
        return ParameterPassing.getArgumentIndex(regNum);
    }

    public static int getSavedRegister(int saveIndex) {
        return ParameterPassing.getSavedRegister(saveIndex);
    }

    public static String getSavedRegisterName(int saveIndex) {
        return ParameterPassing.getSavedRegisterName(saveIndex);
    }

    public static String generateFunctionCall(String funcName, String[] argValues, boolean useTargetAbi) {
        return ParameterPassing.generateFunctionCall(funcName, argValues, useTargetAbi);
    }

    public static boolean validateAbiRegisterUsage(int regNum, String regType) {
        return ABIValidator.validateAbiRegisterUsage(regNum, regType);
    }

    public static String validateCallingConvention(int callerSavedMask, int calleeSavedMask,
                                                    int numArgs, int numLocals) {
        return ABIValidator.validateCallingConvention(callerSavedMask, calleeSavedMask, numArgs, numLocals);
    }

    public static String generateABIReport(String functionName, int numArgs, int numLocals,
                                            int callerSavedMask, int calleeSavedMask) {
        return ABIValidator.generateABIReport(functionName, numArgs, numLocals, callerSavedMask, calleeSavedMask);
    }
}