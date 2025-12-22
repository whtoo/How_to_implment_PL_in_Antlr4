package org.teachfx.antlr4.ep21.analysis.dataflow;

import org.teachfx.antlr4.ep21.ir.IRNode;
import org.teachfx.antlr4.ep21.pass.cfg.BasicBlock;
import org.teachfx.antlr4.ep21.pass.cfg.CFG;

import java.util.*;

/**
 * 抽象数据流分析基类，实现迭代求解器
 *
 * @param <T> 数据流信息的类型
 * @param <I> IR节点类型
 */
public abstract class AbstractDataFlowAnalysis<T, I extends IRNode> implements DataFlowAnalysis<T, I> {

    protected final CFG<I> cfg;
    protected final Map<Integer, T> in;  // 基本块入口信息
    protected final Map<Integer, T> out; // 基本块出口信息
    protected final Map<I, T> instrIn;   // 指令入口信息
    protected final Map<I, T> instrOut;  // 指令出口信息

    public AbstractDataFlowAnalysis(CFG<I> cfg) {
        this.cfg = cfg;
        this.in = new HashMap<>();
        this.out = new HashMap<>();
        this.instrIn = new HashMap<>();
        this.instrOut = new HashMap<>();
        initialize();
    }

    /**
     * 初始化数据流信息
     */
    protected void initialize() {
        // 初始化所有基本块的入口/出口信息
        for (BasicBlock<I> block : cfg) {
            in.put(block.getId(), getInitialValue());
            out.put(block.getId(), getInitialValue());
        }

        // 初始化所有指令的入口/出口信息
        for (BasicBlock<I> block : cfg) {
            for (I instr : getInstructions(block)) {
                instrIn.put(instr, getInitialValue());
                instrOut.put(instr, getInitialValue());
            }
        }

        // 根据分析方向设置边界条件
        if (isForward()) {
            // 前向分析：入口基本块的in为初始值
            if (!cfg.isEmpty()) {
                BasicBlock<I> entry = cfg.getBlock(0); // 假设第一个基本块是入口
                if (entry != null) {
                    in.put(entry.getId(), getInitialValue());
                }
            }
        } else {
            // 后向分析：出口基本块的out为初始值
            // 这里需要识别出口基本块（没有后继的基本块）
            for (BasicBlock<I> block : cfg) {
                if (cfg.getSucceed(block.getId()).isEmpty()) {
                    out.put(block.getId(), getInitialValue());
                }
            }
        }
    }

    @Override
    public void analyze() {
        boolean changed = true;
        int iteration = 0;

        while (changed && iteration < 1000) { // 防止无限循环
            changed = false;
            iteration++;

            if (isForward()) {
                changed = forwardIteration();
            } else {
                changed = backwardIteration();
            }

            if (iteration % 100 == 0) {
                System.out.println("数据流分析迭代次数: " + iteration + ", 变化: " + changed);
            }
        }

        if (iteration >= 1000) {
            System.err.println("警告: 数据流分析未在1000次迭代内收敛");
        }
    }

    /**
     * 前向分析迭代
     */
    private boolean forwardIteration() {
        boolean changed = false;

        // 前向分析：按照拓扑顺序处理基本块
        List<BasicBlock<I>> order = getForwardOrder();
        for (BasicBlock<I> block : order) {
            int blockId = block.getId();

            // 计算in[block] = meet(out[p]) for all predecessors p
            T newIn = getInitialValue();
            for (Integer predId : cfg.getFrontier(blockId)) {
                T predOut = out.get(predId);
                newIn = meet(newIn, predOut);
            }

            if (!newIn.equals(in.get(blockId))) {
                in.put(blockId, newIn);
                changed = true;
            }

            // 计算基本块内指令的数据流
            T current = newIn;
            for (I instr : getInstructions(block)) {
                T newOut = transfer(instr, current);
                if (!newOut.equals(instrOut.get(instr))) {
                    instrOut.put(instr, newOut);
                    instrIn.put(instr, current);
                    changed = true;
                }
                current = newOut;
            }

            // 更新基本块的out
            if (!current.equals(out.get(blockId))) {
                out.put(blockId, current);
                changed = true;
            }
        }

        return changed;
    }

    /**
     * 后向分析迭代
     */
    private boolean backwardIteration() {
        boolean changed = false;

        // 后向分析：按照逆拓扑顺序处理基本块
        List<BasicBlock<I>> order = getBackwardOrder();
        for (BasicBlock<I> block : order) {
            int blockId = block.getId();

            // 计算out[block] = meet(in[s]) for all successors s
            T newOut = getInitialValue();
            for (Integer succId : cfg.getSucceed(blockId)) {
                T succIn = in.get(succId);
                newOut = meet(newOut, succIn);
            }

            if (!newOut.equals(out.get(blockId))) {
                out.put(blockId, newOut);
                changed = true;
            }

            // 计算基本块内指令的数据流（逆序）
            List<I> instructions = getInstructions(block);
            T current = newOut;
            for (int i = instructions.size() - 1; i >= 0; i--) {
                I instr = instructions.get(i);
                T newIn = transfer(instr, current); // 注意：后向分析的transfer函数可能不同
                if (!newIn.equals(instrIn.get(instr))) {
                    instrIn.put(instr, newIn);
                    instrOut.put(instr, current);
                    changed = true;
                }
                current = newIn;
            }

            // 更新基本块的in
            if (!current.equals(in.get(blockId))) {
                in.put(blockId, current);
                changed = true;
            }
        }

        return changed;
    }

    /**
     * 获取前向分析顺序（拓扑顺序）
     */
    protected List<BasicBlock<I>> getForwardOrder() {
        // 简单实现：按基本块ID排序
        List<BasicBlock<I>> order = new ArrayList<>();
        for (BasicBlock<I> block : cfg) {
            order.add(block);
        }
        // 可以改进为真正的拓扑排序
        return order;
    }

    /**
     * 获取后向分析顺序（逆拓扑顺序）
     */
    protected List<BasicBlock<I>> getBackwardOrder() {
        List<BasicBlock<I>> order = getForwardOrder();
        Collections.reverse(order);
        return order;
    }

    /**
     * 获取基本块中的所有指令
     */
    protected List<I> getInstructions(BasicBlock<I> block) {
        List<I> instructions = new ArrayList<>();
        // 注意：BasicBlock中指令存储在codes中，每个code是Loc<I>
        block.codes.forEach(loc -> instructions.add(loc.getInstruction()));
        return instructions;
    }

    @Override
    public CFG<I> getCFG() {
        return cfg;
    }

    @Override
    public T getIn(int blockId) {
        return in.getOrDefault(blockId, getInitialValue());
    }

    @Override
    public T getOut(int blockId) {
        return out.getOrDefault(blockId, getInitialValue());
    }

    @Override
    public T getIn(I instr) {
        return instrIn.getOrDefault(instr, getInitialValue());
    }

    @Override
    public T getOut(I instr) {
        return instrOut.getOrDefault(instr, getInitialValue());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof AbstractDataFlowAnalysis)) return false;
        AbstractDataFlowAnalysis<?, ?> that = (AbstractDataFlowAnalysis<?, ?>) obj;
        return cfg.equals(that.cfg);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cfg);
    }
}