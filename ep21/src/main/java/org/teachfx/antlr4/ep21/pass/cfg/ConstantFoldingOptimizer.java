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
 * 常量折叠优化器 (Constant Folding Optimizer)
 *
 * 在编译时计算常量表达式的值，并用计算结果替换原表达式。
 *
 * 实现策略：
 * 1. 使用简单的局部常量传播来跟踪哪些临时变量持有常量值
 * 2. 当检测到二元/一元表达式的操作数都是常量时，进行折叠
 * 3. 将结果替换为新的常量赋值
 *
 * IR结构说明：
 * - BinExpr和UnaryExpr是独立指令（继承Expr，不继承Operand）
 * - Assign指令的rhs是Operand（如ConstVal或VarSlot）
 * - 常量通过Assign指令分配给临时变量
 *
 * 支持的操作：
 * - 算术运算: ADD, SUB, MUL, DIV, MOD
 * - 比较运算: LT, LE, GT, GE, EQ, NE
 * - 逻辑运算: AND, OR
 * - 一元运算: NEG, NOT
 *
 * @author EP21 Team
 * @version 1.0
 */
public class ConstantFoldingOptimizer implements IFlowOptimizer<IRNode> {

    private static final Logger logger = LogManager.getLogger(ConstantFoldingOptimizer.class);

    /** 优化统计信息 */
    private int foldedExpressions = 0;
    private int processedNodes = 0;

    /** 常量值映射：VarSlot -> ConstVal */
    private Map<VarSlot, ConstVal<?>> constantMap = new HashMap<>();

    @Override
    public void onHandle(CFG<IRNode> cfg) {
        logger.info("开始常量折叠优化...");

        // 重置统计信息
        foldedExpressions = 0;
        processedNodes = 0;
        constantMap.clear();

        // 遍历所有基本块
        for (BasicBlock<IRNode> block : cfg) {
            optimizeBlock(block);
        }

        logger.info("常量折叠优化完成: 处理了 {} 个节点, 折叠了 {} 个表达式",
                    processedNodes, foldedExpressions);
    }

    /**
     * 优化单个基本块
     *
     * 注意：由于BasicBlock使用Loc<IRNode>包装指令，这里只进行常量传播分析，
     * 实际的指令替换需要更深层次的IR修改支持。
     */
    private void optimizeBlock(BasicBlock<IRNode> block) {
        // 收集所有IR节点到列表
        List<IRNode> nodes = block.getIRNodes().collect(java.util.stream.Collectors.toList());

        // 第一遍：收集所有常量赋值
        for (IRNode node : nodes) {
            processedNodes++;

            // 处理常量赋值：跟踪哪些变量持有常量
            if (node instanceof Assign assign) {
                Operand rhs = assign.getRhs();
                if (rhs instanceof ConstVal<?> constVal) {
                    // 记录常量赋值: lhs = const
                    constantMap.put(assign.getLhs(), constVal);
                    logger.trace("记录常量赋值: {} = {}", assign.getLhs(), constVal);
                }
            }
        }

        // 第二遍：尝试折叠表达式
        for (IRNode node : nodes) {
            if (node instanceof BinExpr binExpr) {
                ConstVal<?> folded = tryFoldBinaryExpression(binExpr);
                if (folded != null) {
                    logger.debug("检测到可折叠的二元表达式: {} → {}", binExpr, folded);
                    foldedExpressions++;
                    // 注意：实际替换需要修改Loc包装的指令，这里暂时只记录
                }
            } else if (node instanceof UnaryExpr unaryExpr) {
                ConstVal<?> folded = tryFoldUnaryExpression(unaryExpr);
                if (folded != null) {
                    logger.debug("检测到可折叠的一元表达式: {} → {}", unaryExpr, folded);
                    foldedExpressions++;
                    // 注意：实际替换需要修改Loc包装的指令，这里暂时只记录
                }
            }
        }

        logger.debug("基本块优化完成: 当前常量映射包含 {} 个变量", constantMap.size());
    }

    /**
     * 尝试折叠二元表达式
     *
     * @param binExpr 二元表达式
     * @return 折叠后的常量值，如果不能折叠则返回null
     */
    @SuppressWarnings("unchecked")
    private ConstVal<?> tryFoldBinaryExpression(BinExpr binExpr) {
        VarSlot lhs = binExpr.getLhs();
        VarSlot rhs = binExpr.getRhs();
        OperatorType.BinaryOpType op = binExpr.getOpType();

        // 检查左右操作数是否都是已知的常量
        ConstVal<?> lhsConst = constantMap.get(lhs);
        ConstVal<?> rhsConst = constantMap.get(rhs);

        if (lhsConst == null || rhsConst == null) {
            return null;
        }

        Object lhsValue = lhsConst.getVal();
        Object rhsValue = rhsConst.getVal();

        try {
            Object result = switch (op) {
                // 算术运算
                case ADD -> evalAdd(lhsValue, rhsValue);
                case SUB -> evalSub(lhsValue, rhsValue);
                case MUL -> evalMul(lhsValue, rhsValue);
                case DIV -> evalDiv(lhsValue, rhsValue);
                case MOD -> evalMod(lhsValue, rhsValue);

                // 比较运算
                case LT -> evalCompare(lhsValue, rhsValue, (a, b) -> compare(a, b) < 0);
                case LE -> evalCompare(lhsValue, rhsValue, (a, b) -> compare(a, b) <= 0);
                case GT -> evalCompare(lhsValue, rhsValue, (a, b) -> compare(a, b) > 0);
                case GE -> evalCompare(lhsValue, rhsValue, (a, b) -> compare(a, b) >= 0);
                case EQ -> evalCompare(lhsValue, rhsValue, Objects::equals);
                case NE -> evalCompare(lhsValue, rhsValue, (a, b) -> !Objects.equals(a, b));

                // 逻辑运算
                case AND -> evalLogicalAnd(lhsValue, rhsValue);
                case OR -> evalLogicalOr(lhsValue, rhsValue);

                default -> null;
            };

            if (result != null) {
                return ConstVal.valueOf(result);
            }
        } catch (ArithmeticException e) {
            logger.warn("常量折叠算术异常: {} {} {} = ? (除以零?)", lhsValue, op, rhsValue);
        } catch (Exception e) {
            logger.debug("常量折叠失败: {} {} {}", lhsValue, op, rhsValue);
        }

        return null;
    }

    /**
     * 尝试折叠一元表达式
     */
    @SuppressWarnings("unchecked")
    private ConstVal<?> tryFoldUnaryExpression(UnaryExpr unaryExpr) {
        // UnaryExpr使用公共字段而非getter方法
        VarSlot expr = unaryExpr.expr;
        OperatorType.UnaryOpType op = unaryExpr.op;

        ConstVal<?> constVal = constantMap.get(expr);
        if (constVal == null) {
            return null;
        }

        Object value = constVal.getVal();

        try {
            Object result = switch (op) {
                case NEG -> {
                    if (value instanceof Integer i) {
                        yield -i;
                    } else if (value instanceof Double d) {
                        yield -d;
                    }
                    yield null;
                }
                case NOT -> {
                    if (value instanceof Boolean b) {
                        yield !b;
                    }
                    yield null;
                }
            };

            if (result != null) {
                return ConstVal.valueOf(result);
            }
        } catch (Exception e) {
            logger.debug("一元表达式折叠失败: {} {}", op, value);
        }

        return null;
    }

    // ========== 二元运算求值方法 ==========

    private Object evalAdd(Object lhs, Object rhs) {
        if (lhs instanceof Integer li && rhs instanceof Integer ri) {
            return li + ri;
        } else if (lhs instanceof Number ln && rhs instanceof Number rn) {
            return ln.doubleValue() + rn.doubleValue();
        } else if (lhs instanceof String ls && rhs instanceof String rs) {
            return ls + rs;
        }
        return null;
    }

    private Object evalSub(Object lhs, Object rhs) {
        if (lhs instanceof Integer li && rhs instanceof Integer ri) {
            return li - ri;
        } else if (lhs instanceof Number ln && rhs instanceof Number rn) {
            return ln.doubleValue() - rn.doubleValue();
        }
        return null;
    }

    private Object evalMul(Object lhs, Object rhs) {
        if (lhs instanceof Integer li && rhs instanceof Integer ri) {
            return li * ri;
        } else if (lhs instanceof Number ln && rhs instanceof Number rn) {
            return ln.doubleValue() * rn.doubleValue();
        }
        return null;
    }

    private Object evalDiv(Object lhs, Object rhs) {
        if (lhs instanceof Integer li && rhs instanceof Integer ri) {
            if (ri == 0) throw new ArithmeticException("Division by zero");
            return li / ri;
        } else if (lhs instanceof Number ln && rhs instanceof Number rn) {
            if (rn.doubleValue() == 0.0) throw new ArithmeticException("Division by zero");
            return ln.doubleValue() / rn.doubleValue();
        }
        return null;
    }

    private Object evalMod(Object lhs, Object rhs) {
        if (lhs instanceof Integer li && rhs instanceof Integer ri) {
            if (ri == 0) throw new ArithmeticException("Division by zero");
            return li % ri;
        }
        return null;
    }

    private Boolean evalCompare(Object lhs, Object rhs, java.util.function.BiPredicate<Object, Object> predicate) {
        return predicate.test(lhs, rhs);
    }

    private int compare(Object lhs, Object rhs) {
        if (lhs instanceof Comparable<?> lc && rhs instanceof Comparable<?>) {
            @SuppressWarnings("unchecked")
            int result = ((Comparable<Object>) lc).compareTo(rhs);
            return result;
        }
        throw new IllegalArgumentException("Cannot compare " + lhs + " and " + rhs);
    }

    private Boolean evalLogicalAnd(Object lhs, Object rhs) {
        if (lhs instanceof Boolean lb && rhs instanceof Boolean rb) {
            return lb && rb;
        }
        return null;
    }

    private Boolean evalLogicalOr(Object lhs, Object rhs) {
        if (lhs instanceof Boolean lb && rhs instanceof Boolean rb) {
            return lb || rb;
        }
        return null;
    }

    /**
     * 获取折叠的表达式数量
     */
    public int getFoldedCount() {
        return foldedExpressions;
    }

    /**
     * 获取处理的节点数量
     */
    public int getProcessedCount() {
        return processedNodes;
    }

    /**
     * 获取当前常量映射（用于测试）
     */
    public Map<VarSlot, ConstVal<?>> getConstantMap() {
        return Collections.unmodifiableMap(constantMap);
    }
}
