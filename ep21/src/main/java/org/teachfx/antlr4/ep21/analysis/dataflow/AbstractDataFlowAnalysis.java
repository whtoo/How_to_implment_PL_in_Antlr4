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
            if (!cfg.nodes.isEmpty()) {
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
     * 使用Worklist算法执行数据流分析
     *
     * <p>Worklist算法改进了简单的迭代算法：
     * <ul>
     *   <li>只处理数据流发生变化的基本块</li>
     *   <li>减少不必要的重复计算</li>
     *   <li>对于大型CFG，性能提升显著</li>
     * </ul>
     *
     * <p>算法概述：
     * <ol>
     *   <li>初始化工作列表：所有基本块</li>
     *   <li>循环：从工作列表取出基本块</li>
     *   <li>重新计算基本块的数据流</li>
     *   <li>如果发生变化，将后继（前向分析）或前驱（后向分析）加入工作列表</li>
     *   <li>重复直到工作列表为空</li>
     * </ol>
     *
     * @throws IllegalStateException 如果分析未收敛
     */
    public void analyzeWithWorklist() {
        // 初始化工作列表：所有基本块
        Deque<Integer> worklist = new ArrayDeque<>();
        for (var block : getCFG()) {
            worklist.add(block.getId());
        }

        boolean changed;
        int iterations = 0;
        final int MAX_ITERATIONS = 10000;  // Worklist通常更快，但设置更高的保护

        while (!worklist.isEmpty() && iterations < MAX_ITERATIONS) {
            changed = false;
            iterations++;

            // 取出一个基本块进行处理
            int blockId = worklist.poll();
            var block = getCFG().getBlock(blockId);

            if (block == null) {
                continue;
            }

            // 重新计算该基本块的数据流
            T oldIn = getIn(blockId);
            T oldOut = getOut(blockId);

            // 计算新的in和out
            T newIn;
            T newOut;

            if (isForward()) {
                // 前向分析：in = meet(out[pred]) for all predecessors p
                newIn = computeInForward(block);
                newOut = computeOut(block, newIn);
            } else {
                // 后向分析：out = meet(in[succ]) for all successors s
                newOut = computeOutBackward(block);
                newIn = computeInBackward(block, newOut);
            }

            // 更新数据流信息
            in.put(blockId, newIn);
            out.put(blockId, newOut);

            // 计算基本块内指令的数据流
            computeInstructionDataFlow(block, newIn);

            // 检查数据流是否发生变化
            changed = !newIn.equals(oldIn) || !newOut.equals(oldOut);

            // 如果发生变化，将后继（前向）或前驱（后向）加入工作列表
            if (changed) {
                if (isForward()) {
                    // 前向分析：后继加入工作列表
                    for (Integer succId : getCFG().getSucceed(blockId)) {
                        if (!worklist.contains(succId)) {
                            worklist.add(succId);
                        }
                    }
                } else {
                    // 后向分析：前驱加入工作列表
                    for (Integer predId : getCFG().getFrontier(blockId)) {
                        if (!worklist.contains(predId)) {
                            worklist.add(predId);
                        }
                    }
                }
            }

            // 保护：防止无限循环
            if (iterations == MAX_ITERATIONS) {
                throw new IllegalStateException("数据流分析未在" + MAX_ITERATIONS + "次迭代内收敛");
            }
        }

        if (iterations >= MAX_ITERATIONS) {
            System.err.println("警告: Worklist数据流分析未在" + MAX_ITERATIONS + "次迭代内收敛");
        }
    }

    /**
     * 计算前向分析的in值（merge所有前驱的out值）
     *
     * @param block 基本块
     * @return 计算后的in值
     */
    private T computeInForward(BasicBlock<I> block) {
        T newIn = getInitialValue();

        // meet操作：所有前驱的out的并集
        for (Integer predId : getCFG().getFrontier(block.getId())) {
            T predOut = getOut(predId);
            newIn = meet(newIn, predOut);
        }

        return newIn;
    }

    /**
     * 计算前向分析的out值（执行transfer函数）
     *
     * @param block 基本块
     * @param inValue 基本块的in值
     * @return 计算后的out值
     */
    private T computeOut(BasicBlock<I> block, T inValue) {
        T current = inValue;
        List<I> instructions = getInstructions(block);

        for (I instr : instructions) {
            T newOut = transfer(instr, current);
            instrIn.put(instr, current);
            instrOut.put(instr, newOut);
            current = newOut;
        }

        return current;
    }

    /**
     * 计算后向分析的out值（merge所有后继的in值）
     *
     * @param block 基本块
     * @return 计算后的out值
     */
    private T computeOutBackward(BasicBlock<I> block) {
        T newOut = getInitialValue();

        // meet操作：所有后继的in的并集
        for (Integer succId : getCFG().getSucceed(block.getId())) {
            T succIn = getIn(succId);
            newOut = meet(newOut, succIn);
        }

        return newOut;
    }

    /**
     * 计算后向分析的in值（更新基本块的in）
     *
     * @param block 基本块
     * @param outValue 基本块的out值
     * @return 计算后的in值
     */
    private T computeInBackward(BasicBlock<I> block, T outValue) {
        T current = outValue;
        List<I> instructions = getInstructions(block);

        // 后向分析：逆序处理
        for (int i = instructions.size() - 1; i >= 0; i--) {
            I instr = instructions.get(i);
            T newIn = transfer(instr, current);
            instrIn.put(instr, newIn);
            instrOut.put(instr, current);
            current = newIn;
        }

        return current;
    }

    /**
     * 计算基本块内指令的数据流（辅助方法）
     *
     * @param block 基本块
     * @param inValue 基本块的in值
     */
    private void computeInstructionDataFlow(BasicBlock<I> block, T inValue) {
        List<I> instructions = getInstructions(block);
        T current = inValue;

        if (isForward()) {
            // 前向分析：正向处理
            for (I instr : instructions) {
                T newOut = transfer(instr, current);
                instrIn.put(instr, current);
                instrOut.put(instr, newOut);
                current = newOut;
            }
        } else {
            // 后向分析：逆向处理
            for (int i = instructions.size() - 1; i >= 0; i--) {
                I instr = instructions.get(i);
                T newIn = transfer(instr, current);
                instrIn.put(instr, newIn);
                instrOut.put(instr, current);
                current = newIn;
            }
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
     *
     * <p>简化实现：按基本块ID排序
     * <p>完整实现需要DFS计算真正的拓扑顺序
     */
    protected List<BasicBlock<I>> getForwardOrder() {
        // 简单实现：按基本块ID排序
        List<BasicBlock<I>> order = new ArrayList<>();
        for (BasicBlock<I> block : cfg) {
            order.add(block);
        }
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