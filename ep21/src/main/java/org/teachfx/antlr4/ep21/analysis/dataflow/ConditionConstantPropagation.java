package org.teachfx.antlr4.ep21.analysis.dataflow;

import org.teachfx.antlr4.ep21.ir.IRNode;
import org.teachfx.antlr4.ep21.ir.expr.Operand;
import org.teachfx.antlr4.ep21.ir.expr.VarSlot;
import org.teachfx.antlr4.ep21.ir.expr.val.ConstVal;
import org.teachfx.antlr4.ep21.ir.stmt.Assign;
import org.teachfx.antlr4.ep21.pass.cfg.CFG;

import java.util.*;

/**
 * 条件常量传播分析 (Condition Constant Propagation Analysis)
 *
 * 这是一个前向数据流分析，用于追踪变量的常量值，并识别条件分支的真假值。
 *
 * 分析目标：
 * 1. 识别始终为真或始终为假的条件分支
 * 2. 追踪变量在程序各点的常量值
 * 3. 为死代码消除、循环不变量代码移动等优化提供支持
 *
 * 使用的格结构：
 * - 数据流值：Map<VarSlot, LatticeValue>
 * - LatticeValue:
 *   - UNDEF: 变量未定义（初始状态）
 *   - KnownConstant: 变量是常量，值为ConstVal
 *   - UNKNOWN: 变量不是常量（从不同路径收敛得到不同值）
 *
 * 交汇操作（meet）：
 * - 如果两个路径对同一变量有不同定义，结果为UNKNOWN
 * - 如果一个路径是UNDEF，另一个有值，结果取有值的定义
 *
 * @author EP21 Team
 * @version 1.0
 */
public class ConditionConstantPropagation extends AbstractDataFlowAnalysis<Map<VarSlot, ConditionConstantPropagation.LatticeValue>, IRNode> {

    /**
     * 格值类型，表示变量的状态
     */
    public sealed interface LatticeValue permits ConditionConstantPropagation.UNDEF,
            ConditionConstantPropagation.KnownConstant, ConditionConstantPropagation.UNKNOWN {
        /**
         * 检查是否是常量
         */
        boolean isConstant();

        /**
         * 获取常量值
         */
        ConstVal<?> getConstant();
    }

    /**
     * 未定义状态
     */
    public record UNDEF() implements LatticeValue {
        @Override
        public boolean isConstant() {
            return false;
        }

        @Override
        public ConstVal<?> getConstant() {
            return null;
        }

        @Override
        public String toString() {
            return "UNDEF";
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof UNDEF;
        }

        @Override
        public int hashCode() {
            return 0;
        }
    }

    /**
     * 已知常量值
     */
    public record KnownConstant(ConstVal<?> value) implements LatticeValue {
        @Override
        public boolean isConstant() {
            return true;
        }

        @Override
        public ConstVal<?> getConstant() {
            return value;
        }

        @Override
        public String toString() {
            return "Const(" + value.getVal() + ")";
        }
    }

    /**
     * 未知状态（不是常量）
     */
    public record UNKNOWN() implements LatticeValue {
        @Override
        public boolean isConstant() {
            return false;
        }

        @Override
        public ConstVal<?> getConstant() {
            return null;
        }

        @Override
        public String toString() {
            return "UNKNOWN";
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof UNKNOWN;
        }

        @Override
        public int hashCode() {
            return 1;
        }
    }

    public ConditionConstantPropagation(CFG<IRNode> cfg) {
        super(cfg);
    }

    @Override
    public boolean isForward() {
        return true; // 条件常量传播是前向分析
    }

    @Override
    public Map<VarSlot, LatticeValue> meet(Map<VarSlot, LatticeValue> a, Map<VarSlot, LatticeValue> b) {
        // 合并两个数据流值
        Map<VarSlot, LatticeValue> result = new HashMap<>();

        // 获取所有变量
        Set<VarSlot> allVars = new HashSet<>();
        allVars.addAll(a.keySet());
        allVars.addAll(b.keySet());

        for (VarSlot var : allVars) {
            LatticeValue valA = a.getOrDefault(var, new UNDEF());
            LatticeValue valB = b.getOrDefault(var, new UNDEF());

            result.put(var, meetSingle(valA, valB));
        }

        return result;
    }

    /**
     * 对单个变量进行meet操作
     */
    private LatticeValue meetSingle(LatticeValue a, LatticeValue b) {
        // 如果两个值相同，直接返回
        if (a.equals(b)) {
            return a;
        }

        // 如果任一个是UNKNOWN，结果是UNKNOWN
        if (a instanceof UNKNOWN || b instanceof UNKNOWN) {
            return new UNKNOWN();
        }

        // 如果一个是UNDEF，返回另一个
        if (a instanceof UNDEF) {
            return b;
        }
        if (b instanceof UNDEF) {
            return a;
        }

        // 两个都是KnownConstant但值不同，结果是UNKNOWN
        if (a instanceof KnownConstant ca && b instanceof KnownConstant cb) {
            Object valA = ca.value().getVal();
            Object valB = cb.value().getVal();
            if (!Objects.equals(valA, valB)) {
                return new UNKNOWN();
            }
            return a; // 值相同，返回任意一个
        }

        return new UNKNOWN();
    }

    @Override
    public Map<VarSlot, LatticeValue> transfer(IRNode instr, Map<VarSlot, LatticeValue> input) {
        // 创建输入的副本
        Map<VarSlot, LatticeValue> result = new HashMap<>(input);

        if (instr instanceof Assign assign) {
            VarSlot target = assign.getLhs();
            Operand rhs = assign.getRhs();

            if (rhs instanceof ConstVal<?> constVal) {
                // 常量赋值：变量变成已知常量
                result.put(target, new KnownConstant(constVal));
            } else if (rhs instanceof VarSlot varSlot) {
                // 变量赋值：如果rhs是常量，则target也变成常量；否则变成UNKNOWN
                LatticeValue srcVal = input.getOrDefault(varSlot, new UNDEF());
                if (srcVal instanceof KnownConstant known) {
                    result.put(target, known);
                } else if (srcVal instanceof UNKNOWN) {
                    result.put(target, new UNKNOWN());
                }
                // 如果src是UNDEF，保持UNDEF
            }
            // 其他操作数类型，暂时保持UNDEF
        }
        // 其他指令类型可以在这里扩展

        return result;
    }

    @Override
    public Map<VarSlot, LatticeValue> getInitialValue() {
        // 初始值：所有变量都是UNDEF
        return new HashMap<>();
    }

    /**
     * 检查变量是否是常量
     */
    public boolean isConstant(VarSlot var) {
        return isConstant(var, 0); // 默认检查基本块入口
    }

    /**
     * 检查变量在指定基本块入口是否是常量
     */
    public boolean isConstant(VarSlot var, int blockId) {
        Map<VarSlot, LatticeValue> in = getIn(blockId);
        LatticeValue val = in.getOrDefault(var, new UNDEF());
        return val instanceof KnownConstant;
    }

    /**
     * 检查变量在指定基本块出口是否是常量
     */
    public boolean isConstantOut(VarSlot var, int blockId) {
        Map<VarSlot, LatticeValue> out = getOut(blockId);
        LatticeValue val = out.getOrDefault(var, new UNDEF());
        return val instanceof KnownConstant;
    }

    /**
     * 获取变量的常量值
     */
    public ConstVal<?> getConstantValue(VarSlot var) {
        return getConstantValue(var, 0);
    }

    /**
     * 获取变量在指定基本块入口的常量值
     */
    public ConstVal<?> getConstantValue(VarSlot var, int blockId) {
        Map<VarSlot, LatticeValue> in = getIn(blockId);
        LatticeValue val = in.getOrDefault(var, new UNDEF());
        if (val instanceof KnownConstant known) {
            return known.value();
        }
        return null;
    }

    /**
     * 获取变量在指定基本块出口的常量值
     */
    public ConstVal<?> getConstantValueOut(VarSlot var, int blockId) {
        Map<VarSlot, LatticeValue> out = getOut(blockId);
        LatticeValue val = out.getOrDefault(var, new UNDEF());
        if (val instanceof KnownConstant known) {
            return known.value();
        }
        return null;
    }

    /**
     * 检查变量是否是UNKNOWN（不是常量）
     */
    public boolean isUnknown(VarSlot var) {
        return isUnknown(var, 0);
    }

    /**
     * 检查变量在指定基本块入口是否是UNKNOWN
     */
    public boolean isUnknown(VarSlot var, int blockId) {
        Map<VarSlot, LatticeValue> in = getIn(blockId);
        LatticeValue val = in.getOrDefault(var, new UNDEF());
        return val instanceof UNKNOWN;
    }

    /**
     * 获取分析结果的字符串表示
     */
    public String getResultString() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== 条件常量传播分析结果 ===\n");

        for (var block : getCFG()) {
            int blockId = block.getId();
            sb.append("基本块 ").append(blockId).append(":\n");
            sb.append("  In: ").append(formatMap(getIn(blockId))).append("\n");
            sb.append("  Out: ").append(formatMap(getOut(blockId))).append("\n");
        }

        return sb.toString();
    }

    /**
     * 格式化Map为字符串
     */
    private String formatMap(Map<VarSlot, LatticeValue> map) {
        if (map.isEmpty()) {
            return "{}";
        }
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<VarSlot, LatticeValue> entry : map.entrySet()) {
            if (!first) {
                sb.append(", ");
            }
            sb.append(entry.getKey()).append("=").append(entry.getValue());
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

    /**
     * 检查条件是否为常量真
     */
    public boolean isConditionAlwaysTrue() {
        // 这个方法需要在具体场景中使用，如if条件
        // 返回常量值中的布尔常量
        return false; // 占位实现
    }
}
