package org.teachfx.antlr4.ep21.analysis.dataflow;

import org.teachfx.antlr4.ep21.ir.IRNode;
import org.teachfx.antlr4.ep21.ir.expr.Operand;
import org.teachfx.antlr4.ep21.pass.cfg.BasicBlock;

import java.util.Objects;

/**
 * 定义（Definition）类
 *
 * 表示程序中的一个变量定义点，包含变量和定义位置信息。
 * 用于精确的到达定义分析，区分不同位置的对同一变量的定义。
 *
 * <p>示例：
 * <pre>
 *   x = 1;   // 定义d1: Definition(x, Block0, 0)
 *   if (cond) {
 *     x = 2; // 定义d2: Definition(x, Block1, 0)
 *   }
 *   y = x + 1; // 到达定义: {d1, d2}，而不是简单的{x}
 * </pre>
 *
 * @author EP21 Team
 * @version 1.0
 * @since 2026-01-18
 */
public class Definition {

    /** 被定义的变量 */
    private final Operand variable;

    /** 定义所在的基本块 */
    private final BasicBlock block;

    /** 定义指令在基本块中的索引 */
    private final int instructionIndex;

    /** 完整的指令对象（可选，用于调试和报告） */
    private final IRNode instruction;

    /**
     * 完整构造函数
     *
     * @param variable 被定义的变量，不能为null
     * @param block 定义所在的基本块，不能为null
     * @param instructionIndex 定义指令在基本块中的索引（≥0）
     * @param instruction 完整指令对象，可以为null
     * @throws NullPointerException 如果variable或block为null
     * @throws IllegalArgumentException 如果instructionIndex < 0
     */
    public Definition(Operand variable, BasicBlock block, int instructionIndex, IRNode instruction) {
        this.variable = Objects.requireNonNull(variable, "Variable cannot be null");
        this.block = Objects.requireNonNull(block, "Block cannot be null");

        if (instructionIndex < 0) {
            throw new IllegalArgumentException("Instruction index must be >= 0, got: " + instructionIndex);
        }

        this.instructionIndex = instructionIndex;
        this.instruction = instruction;
    }

    /**
     * 简化构造函数（不含完整指令）
     *
     * @param variable 被定义的变量，不能为null
     * @param block 定义所在的基本块，不能为null
     * @param instructionIndex 定义指令在基本块中的索引（≥0）
     * @throws NullPointerException 如果variable或block为null
     * @throws IllegalArgumentException 如果instructionIndex < 0
     */
    public Definition(Operand variable, BasicBlock block, int instructionIndex) {
        this(variable, block, instructionIndex, null);
    }

    /**
     * 获取被定义的变量
     *
     * @return 被定义的变量
     */
    public Operand getVariable() {
        return variable;
    }

    /**
     * 获取定义所在的基本块
     *
     * @return 定义所在的基本块
     */
    public BasicBlock getBlock() {
        return block;
    }

    /**
     * 获取定义指令的索引
     *
     * @return 指令索引（≥0）
     */
    public int getInstructionIndex() {
        return instructionIndex;
    }

    /**
     * 获取完整的指令对象
     *
     * @return 完整指令对象，如果未提供则返回null
     */
    public IRNode getInstruction() {
        return instruction;
    }

    /**
     * 判断两个Definition是否相等
     *
     * <p>两个Definition相等当且仅当：
     * <ol>
     *   <li>是同一变量（variable相同）
     *   <li>在同一基本块（block相同）
     *   <li>在同一指令索引（instructionIndex相同）
     * </ol>
     *
     * <p>注意：instruction对象不参与相等性判断，因为可能为null。
     *
     * @param obj 要比较的对象
     * @return 如果相等返回true，否则返回false
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Definition)) {
            return false;
        }
        Definition other = (Definition) obj;
        return Objects.equals(variable, other.variable) &&
               Objects.equals(block, other.block) &&
               instructionIndex == other.instructionIndex;
    }

    /**
     * 计算hashCode
     *
     * <p>hashCode计算基于variable、block和instructionIndex，
     * 与equals()方法保持一致。
     *
     * @return hashCode值
     */
    @Override
    public int hashCode() {
        return Objects.hash(variable, block, instructionIndex);
    }

    /**
     * 字符串表示
     *
     * <p>格式: {@code variable@blockId:instructionIndex}
     *
     * <p>示例:
     * <pre>
     *   x@B0:0  // 变量x在基本块0的第0条指令定义
     *   y@B1:3  // 变量y在基本块1的第3条指令定义
     * </pre>
     *
     * @return 字符串表示
     */
    @Override
    public String toString() {
        return variable + "@" + block.getId() + ":" + instructionIndex;
    }

    /**
     * 比较两个Definition的顺序
     *
     * <p>顺序基于：基本块ID → 指令索引
     *
     * @param other 另一个Definition
     * @return 如果this在other之前返回负数，相等返回0，之后返回正数
     */
    public int compareTo(Definition other) {
        if (other == null) {
            return 1;
        }

        // 首先按基本块ID比较
        int blockCompare = Integer.compare(this.block.getId(), other.block.getId());
        if (blockCompare != 0) {
            return blockCompare;
        }

        // 然后按指令索引比较
        return Integer.compare(this.instructionIndex, other.instructionIndex);
    }
}
