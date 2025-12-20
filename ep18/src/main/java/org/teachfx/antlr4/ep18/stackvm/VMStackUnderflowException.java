package org.teachfx.antlr4.ep18.stackvm;

/**
 * VMStackUnderflowException - 栈下溢异常
 * 当尝试从空栈中弹出数据时抛出
 */
public class VMStackUnderflowException extends VMRuntimeException {

    public VMStackUnderflowException(String message, int pc, String instruction) {
        super(message, pc, instruction);
    }

    public VMStackUnderflowException(int pc, String instruction) {
        this("Stack underflow: attempted to pop from empty stack", pc, instruction);
    }

    public VMStackUnderflowException(int pc, String instruction, int requiredSize, int actualSize) {
        this(String.format("Stack underflow: required %d elements but stack has only %d", requiredSize, actualSize), 
             pc, instruction);
    }
}