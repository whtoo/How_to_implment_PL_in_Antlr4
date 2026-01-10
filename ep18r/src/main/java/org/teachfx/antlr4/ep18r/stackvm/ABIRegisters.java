package org.teachfx.antlr4.ep18r.stackvm;

/**
 * ABI寄存器映射枚举
 * 统一管理ABI相关的寄存器名称和编号映射
 */
public enum ABIRegisters {

    RA(RegisterBytecodeDefinition.R1),
    A0(RegisterBytecodeDefinition.R2),
    A1(RegisterBytecodeDefinition.R3),
    A2(RegisterBytecodeDefinition.R4),
    A3(RegisterBytecodeDefinition.R5),
    A4(RegisterBytecodeDefinition.R6),
    A5(RegisterBytecodeDefinition.R7),
    S0(RegisterBytecodeDefinition.R8),
    S1(RegisterBytecodeDefinition.R9),
    S2(RegisterBytecodeDefinition.R10),
    S3(RegisterBytecodeDefinition.R11),
    S4(RegisterBytecodeDefinition.R12),
    SP(RegisterBytecodeDefinition.R13),
    FP(RegisterBytecodeDefinition.R14),
    LR(RegisterBytecodeDefinition.R15);

    private final int registerNumber;

    ABIRegisters(int registerNumber) {
        this.registerNumber = registerNumber;
    }

    public int getRegisterNumber() {
        return registerNumber;
    }

    public static ABIRegisters fromRegisterNumber(int regNum) {
        for (ABIRegisters abiReg : values()) {
            if (abiReg.registerNumber == regNum) {
                return abiReg;
            }
        }
        return null;
    }

    public boolean isCallerSaved() {
        return registerNumber >= RegisterBytecodeDefinition.R1 &&
               registerNumber <= RegisterBytecodeDefinition.R7;
    }

    public boolean isCalleeSaved() {
        return registerNumber >= RegisterBytecodeDefinition.R8 &&
               registerNumber <= RegisterBytecodeDefinition.R12;
    }

    public static ABIRegisters getArgumentRegister(int index) {
        ABIRegisters[] args = {A0, A1, A2, A3, A4, A5};
        if (index >= 0 && index < args.length) {
            return args[index];
        }
        return null;
    }

    public static ABIRegisters getCalleeSavedRegister(int index) {
        ABIRegisters[] calleeRegs = {S0, S1, S2, S3, S4};
        if (index >= 0 && index < calleeRegs.length) {
            return calleeRegs[index];
        }
        return null;
    }
}
