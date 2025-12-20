package org.teachfx.antlr4.ep18.stackvm.instructions.controlflow;

import org.teachfx.antlr4.ep18.stackvm.VMExecutionContext;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 控制流执行器
 * 提供高效的控制流执行，包括分支预测统计和跳转目标验证
 */
public class ControlFlowExecutor {

    /**
     * 分支预测统计
     */
    public static class BranchStatistics {
        private final AtomicLong totalBranches = new AtomicLong(0);
        private final AtomicLong takenBranches = new AtomicLong(0);
        private final AtomicLong notTakenBranches = new AtomicLong(0);
        private final Map<Integer, long[]> branchHistory = new HashMap<>();

        /**
         * 记录分支执行
         * @param pc 分支指令地址
         * @param taken 是否跳转
         */
        public synchronized void recordBranch(int pc, boolean taken) {
            totalBranches.incrementAndGet();
            if (taken) {
                takenBranches.incrementAndGet();
            } else {
                notTakenBranches.incrementAndGet();
            }

            // 记录每个分支点的历史
            branchHistory.computeIfAbsent(pc, k -> new long[2]);
            if (taken) {
                branchHistory.get(pc)[0]++;
            } else {
                branchHistory.get(pc)[1]++;
            }
        }

        /**
         * 获取总分支次数
         */
        public long getTotalBranches() {
            return totalBranches.get();
        }

        /**
         * 获取跳转次数
         */
        public long getTakenBranches() {
            return takenBranches.get();
        }

        /**
         * 获取未跳转次数
         */
        public long getNotTakenBranches() {
            return notTakenBranches.get();
        }

        /**
         * 获取跳转率
         */
        public double getTakenRate() {
            long total = totalBranches.get();
            return total > 0 ? (double) takenBranches.get() / total : 0.0;
        }

        /**
         * 预测特定分支是否会跳转（基于历史）
         * @param pc 分支指令地址
         * @return 预测结果：true=预测跳转, false=预测不跳转
         */
        public boolean predictBranch(int pc) {
            long[] history = branchHistory.get(pc);
            if (history == null) {
                // 默认预测不跳转
                return false;
            }
            // 基于历史频率预测
            return history[0] > history[1];
        }

        /**
         * 获取分支预测准确率
         * @param pc 分支指令地址
         */
        public double getBranchAccuracy(int pc) {
            long[] history = branchHistory.get(pc);
            if (history == null) {
                return 0.0;
            }
            long total = history[0] + history[1];
            if (total == 0) return 0.0;
            return (double) Math.max(history[0], history[1]) / total;
        }

        @Override
        public String toString() {
            return String.format(
                "Branch Statistics:\n" +
                "  Total branches: %d\n" +
                "  Taken: %d (%.2f%%)\n" +
                "  Not taken: %d (%.2f%%)\n" +
                "  Unique branch points: %d",
                totalBranches.get(),
                takenBranches.get(), getTakenRate() * 100,
                notTakenBranches.get(), (1 - getTakenRate()) * 100,
                branchHistory.size()
            );
        }
    }

    private static final BranchStatistics globalStats = new BranchStatistics();

    /**
     * 获取全局分支统计
     */
    public static BranchStatistics getGlobalStatistics() {
        return globalStats;
    }

    /**
     * 验证跳转目标地址
     * @param context 执行上下文
     * @param targetAddress 目标地址
     * @param codeLength 代码长度
     * @throws IllegalArgumentException 如果目标地址无效
     */
    public static void validateJumpTarget(VMExecutionContext context, int targetAddress, int codeLength) {
        if (targetAddress < 0 || targetAddress >= codeLength) {
            throw new IllegalArgumentException(
                String.format("Invalid jump target: %d (code length: %d) at PC: %d",
                    targetAddress, codeLength, context.getProgramCounter())
            );
        }
    }

    /**
     * 执行无条件跳转
     * @param context 执行上下文
     * @param targetAddress 目标地址
     */
    public static void jump(VMExecutionContext context, int targetAddress) {
        context.setProgramCounter(targetAddress);
    }

    /**
     * 执行条件跳转（如果条件为真）
     * @param context 执行上下文
     * @param condition 条件值
     * @param targetAddress 目标地址
     * @return true如果跳转，false否则
     */
    public static boolean jumpIfTrue(VMExecutionContext context, int condition, int targetAddress) {
        boolean taken = condition != 0;
        globalStats.recordBranch(context.getProgramCounter(), taken);

        if (taken) {
            context.setProgramCounter(targetAddress);
        }
        return taken;
    }

    /**
     * 执行条件跳转（如果条件为假）
     * @param context 执行上下文
     * @param condition 条件值
     * @param targetAddress 目标地址
     * @return true如果跳转，false否则
     */
    public static boolean jumpIfFalse(VMExecutionContext context, int condition, int targetAddress) {
        boolean taken = condition == 0;
        globalStats.recordBranch(context.getProgramCounter(), taken);

        if (taken) {
            context.setProgramCounter(targetAddress);
        }
        return taken;
    }

    /**
     * 重置统计信息
     */
    public static void resetStatistics() {
        // 创建新的统计实例
    }
}
