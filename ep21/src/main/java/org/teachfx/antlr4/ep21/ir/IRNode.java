package org.teachfx.antlr4.ep21.ir;

import java.util.Set;

/**
 * IR节点统一基类
 * 所有IR节点（Expr、Stmt、MIRNode、LIRNode）的公共接口
 */
public abstract class IRNode {

    /**
     * 获取IR节点的复杂度级别
     * 0: 最高级抽象（程序、函数等）
     * 1: 控制结构（if、while等）
     * 2: 基本语句（赋值、调用等）
     * 3: 表达式
     *
     * @return 复杂度级别 (0-3)
     */
    public int getComplexityLevel() {
        // 默认实现：基于类名推断
        String className = this.getClass().getSimpleName();
        if (className.contains("Prog") || className.contains("Function")) {
            return 0;
        } else if (className.contains("If") || className.contains("While") || className.contains("For")) {
            return 1;
        } else if (className.contains("Stmt") || className.contains("Assign") || className.contains("Call")) {
            return 2;
        } else {
            return 3; // 表达式
        }
    }

    /**
     * 检查是否为基本块入口
     * 默认返回false，具体子类可以覆盖
     */
    public boolean isBasicBlockEntry() {
        String className = this.getClass().getSimpleName();
        return className.contains("Label") || className.contains("FuncEntry");
    }

    /**
     * 获取此节点使用的变量集合
     * 默认返回空集合，具体子类可以覆盖
     */
    public Set<String> getUsedVariables() {
        return Set.of();
    }

    /**
     * 获取此节点定义的变量集合
     * 默认返回空集合，具体子类可以覆盖
     */
    public Set<String> getDefinedVariables() {
        return Set.of();
    }

    /**
     * 获取IR节点的类型标识
     * 用于类型检查和优化
     */
    public IRNodeType getIRNodeType() {
        return IRNodeType.fromClass(this.getClass());
    }

    /**
     * IR节点类型枚举
     */
    public enum IRNodeType {
        PROGRAM(0, "Prog"),
        FUNCTION(0, "Function"),
        BASIC_BLOCK(1, "BasicBlock"),
        CONTROL_FLOW(1, "If", "While", "For"),
        STATEMENT(2, "Stmt", "Assign", "Return"),
        EXPRESSION(3, "Expr", "BinExpr", "UnaryExpr"),
        MIR_NODE(2, "MIR"),
        LIR_NODE(2, "LIR");

        private final String[] prefixes;
        private final int defaultLevel;

        IRNodeType(int level, String... prefixes) {
            this.prefixes = prefixes;
            this.defaultLevel = level;
        }

        public static IRNodeType fromClass(Class<?> clazz) {
            String className = clazz.getSimpleName();
            for (IRNodeType type : values()) {
                for (String prefix : type.prefixes) {
                    if (className.contains(prefix)) {
                        return type;
                    }
                }
            }
            return EXPRESSION; // 默认为表达式
        }

        public int getDefaultLevel() {
            return defaultLevel;
        }
    }

    /**
     * 注意：accept方法由各个IR层次子类（Expr、Stmt、MIRNode、LIRNode）自行定义
     * 这样可以支持不同的返回类型（Expr返回E，Stmt返回S等）
     */
}
