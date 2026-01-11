package org.teachfx.antlr4.ep18r.stackvm.callingconvention;

import org.teachfx.antlr4.ep18r.stackvm.StackOffsets;

public class ABIValidator {

    private ABIValidator() {}

    public static boolean validateAbiRegisterUsage(int regNum, String regType) {
        switch (regType) {
            case "arg":
                return regNum >= 2 && regNum <= 7;
            case "saved":
                return regNum >= 8 && regNum <= 12;
            case "temp":
                return (regNum >= 1 && regNum <= 7) || regNum == 15;
            case "special":
                return regNum == 0 || regNum == 13 || regNum == 14;
            default:
                return false;
        }
    }

    public static String validateCallingConvention(int callerSavedMask, int calleeSavedMask,
                                                   int numArgs, int numLocals) {
        StringBuilder errors = new StringBuilder();

        int usedCalleeRegs = calleeSavedMask & 0x1F00;
        if (usedCalleeRegs != 0) {
            int illegalRegs = calleeSavedMask & ~0x1F00;
            if (illegalRegs != 0) {
                errors.append("错误：被调用者保存寄存器掩码包含非法寄存器。\n");
            }
        }

        if (numArgs < 0) {
            errors.append("错误：参数数量不能为负数。\n");
        }

        if (numLocals < 0) {
            errors.append("错误：局部变量数量不能为负数。\n");
        }

        try {
            int numCalleeRegs = Integer.bitCount(usedCalleeRegs);
            int frameSize = StackOffsets.calculateFrameSize(numCalleeRegs, numLocals,
                                                          Math.max(0, numArgs - 6));
            if (frameSize % 8 != 0) {
                errors.append("警告：栈帧大小(").append(frameSize).append(")不是8字节对齐。\n");
            }
        } catch (Exception e) {
            errors.append("错误：栈帧大小计算失败：").append(e.getMessage()).append("\n");
        }

        return errors.length() == 0 ? null : errors.toString();
    }

    public static String generateABIReport(String functionName, int numArgs, int numLocals,
                                           int callerSavedMask, int calleeSavedMask) {
        StringBuilder report = new StringBuilder();
        report.append("函数ABI使用报告：").append(functionName).append("\n");
        report.append("========================================\n");

        report.append("参数数量: ").append(numArgs).append("\n");
        if (numArgs > 0) {
            report.append("参数传递:\n");
            for (int i = 0; i < numArgs; i++) {
                if (i < 6) {
                    report.append("  参数").append(i + 1).append(": 寄存器 ")
                          .append(getArgRegisterName(i)).append("\n");
                } else {
                    int offset = ParameterPassing.getStackArgOffset(i);
                    report.append("  参数").append(i + 1).append(": 栈偏移 fp")
                          .append(offset >= 0 ? "+" : "").append(offset).append("\n");
                }
            }
        }

        report.append("局部变量数量: ").append(numLocals).append("\n");
        if (numLocals > 0) {
            report.append("局部变量偏移（相对于fp）:\n");
            for (int i = 0; i < numLocals; i++) {
                int offset = StackOffsets.localVarOffset(i);
                report.append("  局部变量").append(i).append(": fp")
                      .append(offset >= 0 ? "+" : "").append(offset).append("\n");
            }
        }

        report.append("调用者保存寄存器使用: ");
        report.append(formatRegMask(callerSavedMask)).append("\n");

        report.append("被调用者保存寄存器使用: ");
        report.append(formatRegMask(calleeSavedMask)).append("\n");

        int numCalleeRegs = Integer.bitCount(calleeSavedMask & 0x1F00);
        int numStackArgs = Math.max(0, numArgs - 6);
        int frameSize = StackOffsets.calculateFrameSize(numCalleeRegs, numLocals, numStackArgs);
        report.append("栈帧大小: ").append(frameSize).append(" 字节\n");
        report.append("栈指针对齐: ").append(frameSize % 8 == 0 ? "是" : "否").append("\n");

        String validation = validateCallingConvention(callerSavedMask, calleeSavedMask,
                                                     numArgs, numLocals);
        if (validation != null) {
            report.append("\nABI验证警告/错误:\n");
            report.append(validation);
        } else {
            report.append("\nABI验证: 通过\n");
        }

        return report.toString();
    }

    public static int createRegMask(int... regNumbers) {
        int mask = 0;
        for (int reg : regNumbers) {
            if (reg >= 0 && reg < 16) {
                mask |= (1 << reg);
            }
        }
        return mask;
    }

    public static int createRegMaskFromNames(String... regNames) {
        int mask = 0;
        for (String name : regNames) {
            if (name.startsWith("r")) {
                try {
                    int reg = Integer.parseInt(name.substring(1));
                    if (reg >= 0 && reg < 16) {
                        mask |= (1 << reg);
                    }
                } catch (NumberFormatException e) {
                }
            }
        }
        return mask;
    }

    public static boolean isRegisterInMask(int mask, int regNum) {
        return (mask & (1 << regNum)) != 0;
    }

    public static int countRegistersInMask(int mask) {
        return Integer.bitCount(mask);
    }

    private static String getArgRegisterName(int argIndex) {
        int reg = StackOffsets.getArgRegister(argIndex);
        return StackOffsets.getAbiName(reg);
    }

    private static String formatRegMask(int mask) {
        if (mask == 0) return "无";

        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (int i = 0; i < 16; i++) {
            if ((mask & (1 << i)) != 0) {
                if (!first) sb.append(", ");
                sb.append(StackOffsets.getAbiName(i)).append("(r").append(i).append(")");
                first = false;
            }
        }
        return sb.toString();
    }
}
