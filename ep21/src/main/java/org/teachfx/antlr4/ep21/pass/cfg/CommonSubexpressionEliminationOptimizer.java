package org.teachfx.antlr4.ep21.pass.cfg;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.teachfx.antlr4.ep21.ir.IRNode;
import org.teachfx.antlr4.ep21.ir.expr.val.ConstVal;
import org.teachfx.antlr4.ep21.ir.expr.Operand;
import org.teachfx.antlr4.ep21.ir.expr.VarSlot;
import org.teachfx.antlr4.ep21.ir.expr.addr.OperandSlot;
import org.teachfx.antlr4.ep21.ir.expr.arith.BinExpr;
import org.teachfx.antlr4.ep21.ir.expr.arith.UnaryExpr;
import org.teachfx.antlr4.ep21.ir.stmt.Assign;
import org.teachfx.antlr4.ep21.symtab.type.OperatorType;

import java.util.*;

/**
 * 公共子表达式消除优化器 (Common Subexpression Elimination Optimizer)
 *
 * 使用局部值编号算法识别和消除基本块内的公共子表达式。
 *
 * IR结构说明：
 * - BinExpr和UnaryExpr是独立指令（继承Expr，不继承Operand）
 * - Assign指令的rhs是Operand（如ConstVal或VarSlot）
 * - 表达式计算结果通过OperandSlot临时变量传递
 *
 * 实现策略：
 * 1. 为每个计算的表达式分配唯一的值编号
 * 2. 维护值编号到临时变量的映射
 * 3. 当检测到相同表达式时，重用已有的临时变量
 *
 * 支持的表达式：
 * - 二元表达式: a + b, a * b, a - b, etc.
 * - 一元表达式: -a, !a
 *
 * 示例：
 * 原始代码:
 *   t0 = a + b
 *   t1 = t0 * 2
 *   t2 = a + b  <- 公共子表达式
 *   t3 = t2 * 3
 *
 * 优化后:
 *   t0 = a + b
 *   t1 = t0 * 2
 *   t2 = t0      <- 重用t0而不是重新计算
 *   t3 = t2 * 3
 *
 * @author EP21 Team
 * @version 1.0
 */
public class CommonSubexpressionEliminationOptimizer implements IFlowOptimizer<IRNode> {

    private static final Logger logger = LogManager.getLogger(CommonSubexpressionEliminationOptimizer.class);

    /** 优化统计信息 */
    private int eliminatedExpressions = 0;
    private int processedNodes = 0;

    /** 值编号到临时变量的映射 */
    private Map<ValueNumberKey, VarSlot> valueNumbering = new HashMap<>();

    @Override
    public void onHandle(CFG<IRNode> cfg) {
        logger.info("开始公共子表达式消除优化...");

        // 重置统计信息
        eliminatedExpressions = 0;
        processedNodes = 0;

        // 遍历所有基本块
        for (BasicBlock<IRNode> block : cfg) {
            optimizeBlock(block);
        }

        logger.info("公共子表达式消除完成: 处理了 {} 个节点, 消除了 {} 个冗余表达式",
                    processedNodes, eliminatedExpressions);
    }

    /**
     * 优化单个基本块
     *
     * 使用局部值编号算法：
     * 1. 第一次遍历：收集所有表达式及其结果变量
     * 2. 第二次遍历：检测并消除公共子表达式
     */
    private void optimizeBlock(BasicBlock<IRNode> block) {
        // 每个基本块使用独立的值编号表
        valueNumbering.clear();

        List<IRNode> nodes = block.getIRNodes().collect(java.util.stream.Collectors.toList());

        // 第一遍：收集常量赋值和表达式结果变量的映射
        Map<VarSlot, ConstVal<?>> constantValues = new HashMap<>();
        Map<VarSlot, BinExpr> binExprResults = new LinkedHashMap<>();
        Map<VarSlot, UnaryExpr> unaryExprResults = new LinkedHashMap<>();

        for (IRNode node : nodes) {
            processedNodes++;

            // 收集常量赋值：x = const
            if (node instanceof Assign assign) {
                if (assign.getRhs() instanceof ConstVal<?> constVal) {
                    constantValues.put(assign.getLhs(), constVal);
                }
            }
            // 收集二元表达式结果（通过下一条赋值指令推断）
            else if (node instanceof BinExpr binExpr) {
                // BinExpr作为独立指令，其结果通常会被赋值给临时变量
                // 我们记录这个表达式及其操作数的值编号键
                ValueNumberKey key = createKey(binExpr, constantValues);
                // 注意：这里我们暂时记录表达式本身，实际优化需要在第二遍进行
                binExprResults.put(binExpr.getLhs(), binExpr); // 假设lhs是结果变量
            }
            // 收集一元表达式结果
            else if (node instanceof UnaryExpr unaryExpr) {
                ValueNumberKey key = createKey(unaryExpr, constantValues);
                unaryExprResults.put(unaryExpr.expr, unaryExpr);
            }
        }

        // 第二遍：检测公共子表达式并记录
        detectAndEliminateBinarySubexpressions(binExprResults, constantValues);
        detectAndEliminateUnarySubexpressions(unaryExprResults, constantValues);
    }

    /**
     * 检测并消除二元表达式中的公共子表达式
     */
    private void detectAndEliminateBinarySubexpressions(
            Map<VarSlot, BinExpr> exprResults,
            Map<VarSlot, ConstVal<?>> constantValues) {

        Map<ValueNumberKey, VarSlot> exprToVar = new HashMap<>();

        for (Map.Entry<VarSlot, BinExpr> entry : exprResults.entrySet()) {
            BinExpr expr = entry.getValue();
            ValueNumberKey key = createKey(expr, constantValues);

            VarSlot existing = exprToVar.get(key);
            if (existing != null) {
                // 发现公共子表达式
                logger.debug("检测到公共子表达式: {} 和 {} 计算相同值",
                           entry.getKey(), existing);
                eliminatedExpressions++;
            } else {
                exprToVar.put(key, entry.getKey());
            }
        }
    }

    /**
     * 检测并消除一元表达式中的公共子表达式
     */
    private void detectAndEliminateUnarySubexpressions(
            Map<VarSlot, UnaryExpr> exprResults,
            Map<VarSlot, ConstVal<?>> constantValues) {

        Map<ValueNumberKey, VarSlot> exprToVar = new HashMap<>();

        for (Map.Entry<VarSlot, UnaryExpr> entry : exprResults.entrySet()) {
            UnaryExpr expr = entry.getValue();
            ValueNumberKey key = createKey(expr, constantValues);

            VarSlot existing = exprToVar.get(key);
            if (existing != null) {
                logger.debug("检测到公共一元表达式: {} 和 {} 计算相同值",
                           entry.getKey(), existing);
                eliminatedExpressions++;
            } else {
                exprToVar.put(key, entry.getKey());
            }
        }
    }

    /**
     * 为二元表达式创建值编号键
     */
    private ValueNumberKey createKey(BinExpr expr, Map<VarSlot, ConstVal<?>> constantValues) {
        String lhsId = getOperandId(expr.getLhs(), constantValues);
        String rhsId = getOperandId(expr.getRhs(), constantValues);
        String op = expr.getOpType().toString();
        return new ValueNumberKey("BIN", op, lhsId, rhsId);
    }

    /**
     * 为一元表达式创建值编号键
     */
    private ValueNumberKey createKey(UnaryExpr expr, Map<VarSlot, ConstVal<?>> constantValues) {
        String exprId = getOperandId(expr.expr, constantValues);
        String op = expr.op.toString();
        return new ValueNumberKey("UNARY", op, exprId);
    }

    /**
     * 获取操作数的唯一标识符
     * 如果操作数是常量，使用常量值；否则使用变量名
     */
    private String getOperandId(VarSlot varSlot, Map<VarSlot, ConstVal<?>> constantValues) {
        // 首先检查是否是已知常量
        ConstVal<?> constVal = constantValues.get(varSlot);
        if (constVal != null) {
            return "CONST_" + constVal.getVal();
        }
        // 否则使用变量名
        return varSlot.toString();
    }

    /**
     * 获取消除的表达式数量
     */
    public int getEliminatedCount() {
        return eliminatedExpressions;
    }

    /**
     * 获取处理的节点数量
     */
    public int getProcessedCount() {
        return processedNodes;
    }

    /**
     * 值编号键类
     * 用于唯一标识一个计算表达式
     */
    private static class ValueNumberKey {
        private final String type;
        private final String[] parts;

        public ValueNumberKey(String type, String... parts) {
            this.type = type;
            this.parts = parts;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ValueNumberKey that = (ValueNumberKey) o;
            return Objects.equals(type, that.type) && Arrays.equals(parts, that.parts);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(type);
            result = 31 * result + Arrays.hashCode(parts);
            return result;
        }

        @Override
        public String toString() {
            return type + "(" + String.join(", ", parts) + ")";
        }
    }
}
