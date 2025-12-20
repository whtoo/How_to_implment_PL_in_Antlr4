package org.teachfx.antlr4.ep18.stackvm.instructions;

/**
 * 指令抽象基类
 * 提供指令的通用实现和默认行为
 */
public abstract class BaseInstruction implements Instruction {
    protected final String name;
    protected final int opcode;
    protected final boolean hasOperand;

    /**
     * 构造函数
     * @param name 指令名称
     * @param opcode 指令操作码
     * @param hasOperand 是否有操作数
     */
    public BaseInstruction(String name, int opcode, boolean hasOperand) {
        this.name = name;
        this.opcode = opcode;
        this.hasOperand = hasOperand;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getOpcode() {
        return opcode;
    }

    @Override
    public boolean hasOperand() {
        return hasOperand;
    }
}
