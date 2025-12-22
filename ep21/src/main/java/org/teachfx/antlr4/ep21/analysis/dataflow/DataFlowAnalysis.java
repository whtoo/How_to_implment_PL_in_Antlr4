package org.teachfx.antlr4.ep21.analysis.dataflow;

import org.teachfx.antlr4.ep21.ir.IRNode;
import org.teachfx.antlr4.ep21.pass.cfg.CFG;

/**
 * 统一的数据流分析接口
 *
 * @param <T> 数据流信息的类型（例如 Set<Operand> 对于活跃变量分析）
 * @param <I> IR节点类型
 */
public interface DataFlowAnalysis<T, I extends IRNode> {

    /**
     * 执行数据流分析
     */
    void analyze();

    /**
     * 获取分析方向
     * @return true 表示前向分析，false 表示后向分析
     */
    boolean isForward();

    /**
     * 获取CFG
     */
    CFG<I> getCFG();

    /**
     * 获取基本块的输入信息
     * @param blockId 基本块ID
     * @return 基本块入口处的数据流信息
     */
    T getIn(int blockId);

    /**
     * 获取基本块的输出信息
     * @param blockId 基本块ID
     * @return 基本块出口处的数据流信息
     */
    T getOut(int blockId);

    /**
     * 获取指令的输入信息
     * @param instr 指令
     * @return 指令入口处的数据流信息
     */
    T getIn(I instr);

    /**
     * 获取指令的输出信息
     * @param instr 指令
     * @return 指令出口处的数据流信息
     */
    T getOut(I instr);

    /**
     * 交汇操作（meet operation），用于合并来自不同路径的信息
     * @param a 第一个数据流信息
     * @param b 第二个数据流信息
     * @return 合并后的数据流信息
     */
    T meet(T a, T b);

    /**
     * 传递函数（transfer function），计算指令对数据流信息的影响
     * @param instr 指令
     * @param input 输入数据流信息
     * @return 输出数据流信息
     */
    T transfer(I instr, T input);

    /**
     * 初始化入口/出口信息
     * @return 初始数据流信息
     */
    T getInitialValue();
}