package org.teachfx.antlr4.ep18r.stackvm.callingconvention;

import org.teachfx.antlr4.ep18r.stackvm.StackOffsets;

public class ParameterPassing {

    private ParameterPassing() {}

    public static int getStackArgOffset(int argIndex) {
        return StackOffsets.argOffset(argIndex);
    }

    public static int getArgRegister(int argIndex) {
        return StackOffsets.getArgRegister(argIndex);
    }

    public static String getArgRegisterName(int argIndex) {
        int reg = getArgRegister(argIndex);
        return StackOffsets.getAbiName(reg);
    }

    public static boolean isStackArg(int argIndex) {
        return argIndex >= 6;
    }

    public static String generateArgPassing(int argIndex, String value, boolean isImmediate) {
        if (argIndex < 6) {
            String regName = getArgRegisterName(argIndex);
            if (isImmediate) {
                return String.format("    li %s, %s          # 第%d个参数\n", regName, value, argIndex + 1);
            } else {
                return String.format("    mov %s, %s         # 第%d个参数\n", regName, value, argIndex + 1);
            }
        } else {
            int offset = getStackArgOffset(argIndex);
            if (isImmediate) {
                return String.format("    li t0, %s          # 第%d个参数（临时）\n", value, argIndex + 1) +
                       String.format("    sw t0, %d(fp)      # 存储到栈偏移量%d\n", offset, offset);
            } else {
                return String.format("    sw %s, %d(fp)      # 第%d个参数存储到栈偏移量%d\n", value, offset, argIndex + 1, offset);
            }
        }
    }

    public static int getReturnValueRegister() {
        return 2;
    }

    public static String getReturnValueRegisterName() {
        return StackOffsets.getAbiName(getReturnValueRegister());
    }

    public static String generateReturnValue(String value, boolean isImmediate) {
        String retReg = getReturnValueRegisterName();
        if (isImmediate) {
            return String.format("    li %s, %s          # 设置返回值\n", retReg, value);
        } else {
            return String.format("    mov %s, %s         # 设置返回值\n", retReg, value);
        }
    }

    public static int getTargetAbiReturnValueRegister() {
        return 2;
    }

    public static int getCurrentReturnValueRegister() {
        return 2;
    }

    public static int getReturnValueRegister(boolean useTargetAbi) {
        return useTargetAbi ? getTargetAbiReturnValueRegister() : getCurrentReturnValueRegister();
    }

    public static int getArgumentRegister(int argIndex) {
        if (argIndex < 0 || argIndex > 5) {
            throw new IllegalArgumentException("Argument index must be between 0 and 5, got: " + argIndex);
        }
        return 2 + argIndex;
    }

    public static String getArgumentRegisterName(int argIndex) {
        return StackOffsets.getAbiName(getArgumentRegister(argIndex));
    }

    public static boolean isArgumentRegister(int regNum) {
        return regNum >= 2 && regNum <= 7;
    }

    public static int getArgumentIndex(int regNum) {
        if (isArgumentRegister(regNum)) {
            return regNum - 2;
        }
        return -1;
    }

    public static int getSavedRegister(int saveIndex) {
        if (saveIndex < 0 || saveIndex > 4) {
            throw new IllegalArgumentException("Saved register index must be between 0 and 4, got: " + saveIndex);
        }
        return 8 + saveIndex;
    }

    public static String getSavedRegisterName(int saveIndex) {
        return StackOffsets.getAbiName(getSavedRegister(saveIndex));
    }

    public static String generateFunctionCall(String funcName, String[] argValues, boolean useTargetAbi) {
        StringBuilder sb = new StringBuilder();

        int numArgs = Math.min(argValues.length, 6);
        for (int i = 0; i < numArgs; i++) {
            String argReg = useTargetAbi ? getArgumentRegisterName(i) : "r" + getArgumentRegister(i);
            sb.append(String.format("    li %s, %s          # 设置第%d个参数\n", argReg, argValues[i], i + 1));
        }

        sb.append(String.format("    call %s             # 调用函数\n", funcName));

        return sb.toString();
    }
}
