package org.teachfx.antlr4.ep21.analysis.dataflow;

import org.teachfx.antlr4.ep21.ir.IRNode;
import org.teachfx.antlr4.ep21.ir.expr.Operand;
import org.teachfx.antlr4.ep21.ir.stmt.Assign;
import org.teachfx.antlr4.ep21.pass.cfg.CFG;

import java.util.HashSet;
import java.util.Set;

/**
 * 活跃变量分析，使用统一数据流分析接口实现
 * 这是一个后向分析，数据流信息类型为 Set<Operand>
 */
public class LiveVariableAnalysis extends AbstractDataFlowAnalysis<Set<Operand>, IRNode> {

    public LiveVariableAnalysis(CFG<IRNode> cfg) {
        super(cfg);
    }

    @Override
    public boolean isForward() {
        return false; // 活跃变量分析是后向分析
    }

    @Override
    public Set<Operand> meet(Set<Operand> a, Set<Operand> b) {
        // 活跃变量分析使用并集操作
        Set<Operand> result = new HashSet<>(a);
        result.addAll(b);
        return result;
    }

    @Override
    public Set<Operand> transfer(IRNode instr, Set<Operand> input) {
        // 活跃变量分析的传递函数:
        // out = gen ∪ (in - kill)
        Set<Operand> gen = computeGen(instr);
        Set<Operand> kill = computeKill(instr);

        Set<Operand> result = new HashSet<>(gen);
        result.addAll(input);
        result.removeAll(kill);
        return result;
    }

    @Override
    public Set<Operand> getInitialValue() {
        // 初始值为空集合
        return new HashSet<>();
    }

    /**
     * 计算gen集合（使用但不定义的变量）
     */
    private Set<Operand> computeGen(IRNode instr) {
        Set<Operand> gen = new HashSet<>();

        if (instr instanceof Assign assign) {
            // 右边表达式的使用变量
            gen.addAll(findUsedOperands(assign.getRhs()));
        }
        // 其他指令类型可以在这里扩展

        return gen;
    }

    /**
     * 计算kill集合（定义但不使用的变量）
     */
    private Set<Operand> computeKill(IRNode instr) {
        Set<Operand> kill = new HashSet<>();

        if (instr instanceof Assign assign) {
            // 左边的目标变量
            kill.add(assign.getLhs());
        }

        return kill;
    }

    /**
     * 查找表达式中使用的操作数
     */
    private Set<Operand> findUsedOperands(Operand operand) {
        Set<Operand> used = new HashSet<>();
        used.add(operand);
        return used;
    }

    /**
     * 获取活跃变量分析结果的字符串表示
     */
    public String getResultString() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== 活跃变量分析结果 ===\n");

        for (var block : getCFG()) {
            int blockId = block.getId();
            sb.append("基本块 ").append(blockId).append(":\n");
            sb.append("  LiveIn: ").append(formatOperandSet(getIn(blockId))).append("\n");
            sb.append("  LiveOut: ").append(formatOperandSet(getOut(blockId))).append("\n");

            sb.append("  指令序列:\n");
            for (var loc : block.codes) {
                IRNode instr = loc.getInstruction();
                sb.append("    ").append(instr.toString())
                  .append(" [LiveIn: ").append(formatOperandSet(getIn(instr)))
                  .append(", LiveOut: ").append(formatOperandSet(getOut(instr))).append("]\n");
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * 格式化操作数集合
     */
    private String formatOperandSet(Set<Operand> operands) {
        if (operands.isEmpty()) {
            return "{}";
        }
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Operand op : operands) {
            if (!first) {
                sb.append(", ");
            }
            sb.append(op.toString());
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }

    /**
     * 打印分析结果
     */
    public void printResult() {
        System.out.println(getResultString());
    }
}