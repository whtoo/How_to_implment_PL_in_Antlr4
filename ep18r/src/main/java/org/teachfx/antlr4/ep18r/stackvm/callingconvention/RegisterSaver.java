package org.teachfx.antlr4.ep18r.stackvm.callingconvention;

import org.teachfx.antlr4.ep18r.stackvm.ABIRegisters;
import org.teachfx.antlr4.ep18r.stackvm.StackFrame;
import org.teachfx.antlr4.ep18r.stackvm.StackOffsets;
import org.teachfx.antlr4.ep18r.stackvm.instructions.model.RegisterBytecodeDefinition;
import org.teachfx.antlr4.ep18r.stackvm.interpreter.ExecutionContext;

/**
 * 寄存器保存器
 * 负责调用者保存和被调用者保存寄存器的保存/恢复操作
 */
public class RegisterSaver {

    private RegisterSaver() {}

    public static int saveCallerRegisters(StackFrame frame, int regMask) {
        int[] callerSavedRegs = {1, 2, 3, 4, 5, 6, 7, 15};
        int offset = 0;
        for (int idx = 0; idx < callerSavedRegs.length; idx++) {
            int reg = callerSavedRegs[idx];
            if ((regMask & (1 << reg)) != 0) {
                frame.savedCallerRegisters[idx] = 0;
                offset += 4;
            }
        }
        return offset;
    }

    public static void restoreCallerRegisters(StackFrame frame, int regMask, ExecutionContext context) {
        int[] callerSavedRegs = {1, 2, 3, 4, 5, 6, 7, 15};
        for (int idx = 0; idx < callerSavedRegs.length; idx++) {
            int reg = callerSavedRegs[idx];
            if ((regMask & (1 << reg)) != 0) {
                context.setRegister(reg, frame.savedCallerRegisters[idx]);
            }
        }
    }

    public static int saveCalleeRegisters(StackFrame frame, int regMask) {
        int offset = 0;
        for (int i = 8; i <= 12; i++) {
            if ((regMask & (1 << i)) != 0) {
                int stackOffset = StackOffsets.S0_SAVE_OFFSET + (i - 8) * 4;
                offset += 4;
            }
        }
        return offset;
    }

    public static void restoreCalleeRegisters(StackFrame frame, int regMask, ExecutionContext context) {
        for (int i = 8; i <= 12; i++) {
            if ((regMask & (1 << i)) != 0) {
                int stackOffset = StackOffsets.S0_SAVE_OFFSET + (i - 8) * 4;
                int fp = context.getRegister(RegisterBytecodeDefinition.R14);
                int address = fp + stackOffset / 4;
                int value = context.readMemory(address);
                context.setRegister(i, value);
            }
        }
    }

    public static int calculateCallerSavedMask() {
        return (1 << ABIRegisters.RA.getRegisterNumber()) |
               (1 << ABIRegisters.A0.getRegisterNumber()) |
               (1 << ABIRegisters.A1.getRegisterNumber()) |
               (1 << ABIRegisters.A2.getRegisterNumber()) |
               (1 << ABIRegisters.A3.getRegisterNumber()) |
               (1 << ABIRegisters.A4.getRegisterNumber()) |
               (1 << ABIRegisters.A5.getRegisterNumber()) |
               (1 << ABIRegisters.LR.getRegisterNumber());
    }

    public static int calculateCalleeSavedMask() {
        return (1 << ABIRegisters.S0.getRegisterNumber()) |
               (1 << ABIRegisters.S1.getRegisterNumber()) |
               (1 << ABIRegisters.S2.getRegisterNumber()) |
               (1 << ABIRegisters.S3.getRegisterNumber()) |
               (1 << ABIRegisters.S4.getRegisterNumber());
    }
}
