package org.teachfx.antlr4.ep21.analysis.dataflow;

import org.teachfx.antlr4.ep21.ir.IRNode;
import org.teachfx.antlr4.ep21.ir.expr.Operand;
import org.teachfx.antlr4.ep21.ir.stmt.Assign;
import org.teachfx.antlr4.ep21.pass.cfg.CFG;

import java.util.HashSet;
import java.util.Set;

/**
 * 到达定义分析，使用统一数据流分析接口实现
 * 这是一个前向分析，数据流信息类型为 Set<Operand>
 * 注：简化实现，实际应为 Set<Definition>，其中Definition包含变量和定义点
 */
public class ReachingDefinitionAnalysis extends AbstractDataFlowAnalysis<Set<Operand>, IRNode> {

    public ReachingDefinitionAnalysis(CFG<IRNode> cfg) {
        super(cfg);
    }

    @Override
    public boolean isForward() {
        return true; // 到达定义分析是前向分析
    }

    @Override
    public Set<Operand> meet(Set<Operand> a, Set<Operand> b) {
        // 到达定义分析使用并集操作
        Set<Operand> result = new HashSet<>(a);
        result.addAll(b);
        return result;
    }

    @Override
    public Set<Operand> transfer(IRNode instr, Set<Operand> input) {
        // 到达定义分析的传递函数:
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
     * 计算gen集合（新产生的定义）
     */
    private Set<Operand> computeGen(IRNode instr) {
        Set<Operand> gen = new HashSet<>();

        if (instr instanceof Assign assign) {
            // 左边的目标变量是一个新定义
            gen.add(assign.getLhs());
        }
        // 其他指令类型可以在这里扩展

        return gen;
    }

    /**
     * 计算kill集合（被杀死的老定义）
     */
    private Set<Operand> computeKill(IRNode instr) {
        Set<Operand> kill = new HashSet<>();

        if (instr instanceof Assign assign) {
            // 对变量x的新定义会杀死所有对x的老定义
            // 这里简化处理：只杀死当前变量
            kill.add(assign.getLhs());
        }

        return kill;
    }

    /**
     * 获取分析结果的字符串表示
     */
    public String getResultString() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== 到达定义分析结果 ===\n");

        for (var block : getCFG()) {
            int blockId = block.getId();
            sb.append("基本块 ").append(blockId).append(":\n");
            sb.append("  In: ").append(formatOperandSet(getIn(blockId))).append("\n");
            sb.append("  Out: ").append(formatOperandSet(getOut(blockId))).append("\n");

            sb.append("  指令序列:\n");
            for (var loc : block.codes) {
                IRNode instr = loc.getInstruction();
                sb.append("    ").append(instr.toString())
                  .append(" [In: ").append(formatOperandSet(getIn(instr)))
                  .append(", Out: ").append(formatOperandSet(getOut(instr))).append("]\n");
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