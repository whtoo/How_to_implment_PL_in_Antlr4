package org.teachfx.antlr4.ep18.stackvm.stackframe;

import org.teachfx.antlr4.ep18.stackvm.FunctionSymbol;
import org.teachfx.antlr4.ep18.stackvm.LabelSymbol;
import org.teachfx.antlr4.ep18.stackvm.StackFrame;
import org.teachfx.antlr4.ep18.symtab.symbol.StructSymbol;

import java.util.*;

/**
 * 动态链接器
 * 支持运行时函数调用、符号解析和动态链接
 */
public class DynamicLinker {
    // 全局符号表
    private final Map<String, FunctionSymbol> functionTable;
    private final Map<String, LabelSymbol> labelTable;
    private final Map<String, StructSymbol> structTable;

    // 调用栈跟踪
    private final Deque<LinkageRecord> callStack;
    private int callDepth;

    // 链接统计
    private int totalCalls;
    private int resolvedCalls;
    private int failedCalls;

    /**
     * 构造函数
     */
    public DynamicLinker() {
        this.functionTable = new HashMap<>();
        this.labelTable = new HashMap<>();
        this.structTable = new HashMap<>();
        this.callStack = new ArrayDeque<>();
        this.callDepth = 0;
        this.totalCalls = 0;
        this.resolvedCalls = 0;
        this.failedCalls = 0;
    }

    /**
     * 注册函数符号
     */
    public void registerFunction(FunctionSymbol function) {
        if (function == null || function.name == null) {
            throw new IllegalArgumentException("Invalid function symbol");
        }
        functionTable.put(function.name, function);
    }

    /**
     * 注册标签符号
     */
    public void registerLabel(LabelSymbol label) {
        if (label == null || label.getName() == null) {
            throw new IllegalArgumentException("Invalid label symbol");
        }
        labelTable.put(label.getName(), label);
    }

    /**
     * 注册结构体符号
     */
    public void registerStruct(StructSymbol struct) {
        if (struct == null || struct.getName() == null) {
            throw new IllegalArgumentException("Invalid struct symbol");
        }
        structTable.put(struct.getName(), struct);
    }

    /**
     * 解析函数调用
     * @param functionName 函数名
     * @param caller 调用者栈帧
     * @return 链接结果
     */
    public LinkageResult resolveFunctionCall(String functionName, StackFrame caller) {
        totalCalls++;

        if (functionName == null || functionName.isEmpty()) {
            failedCalls++;
            return LinkageResult.failure("Function name is null or empty");
        }

        // 查找函数符号
        FunctionSymbol targetFunction = functionTable.get(functionName);
        if (targetFunction == null) {
            failedCalls++;
            return LinkageResult.failure("Function not found: " + functionName);
        }

        // 验证调用兼容性
        CompatibilityCheckResult check = checkCompatibility(caller, targetFunction);
        if (!check.isCompatible()) {
            failedCalls++;
            return LinkageResult.failure("Incompatible call: " + check.getErrorMessage());
        }

        resolvedCalls++;

        // 创建链接记录
        LinkageRecord record = new LinkageRecord(
            caller.getSymbol().name,
            functionName,
            System.nanoTime(),
            callDepth++
        );

        callStack.push(record);

        return LinkageResult.success(targetFunction, record);
    }

    /**
     * 解析跳转标签
     * @param labelName 标签名
     * @return 跳转目标地址，如果未找到则返回-1
     */
    public int resolveJumpTarget(String labelName) {
        if (labelName == null || labelName.isEmpty()) {
            return -1;
        }

        LabelSymbol label = labelTable.get(labelName);
        if (label == null) {
            return -1;
        }

        return label.getAddress();
    }

    /**
     * 完成函数调用（返回时调用）
     */
    public void completeFunctionCall() {
        if (!callStack.isEmpty()) {
            LinkageRecord record = callStack.pop();
            record.setCompletionTime(System.nanoTime());
            callDepth = Math.max(0, callDepth - 1);
        }
    }

    /**
     * 检查调用兼容性
     */
    private CompatibilityCheckResult checkCompatibility(StackFrame caller, FunctionSymbol target) {
        // 检查参数数量
        if (caller.getSymbol().nargs != target.nargs) {
            return CompatibilityCheckResult.incompatible(
                "Argument count mismatch: expected " + target.nargs +
                ", got " + caller.getSymbol().nargs
            );
        }

        // 检查局部变量数量（可选）
        if (caller.getSymbol().nlocals != target.nlocals) {
            // 这不是错误，只是信息性检查
        }

        return CompatibilityCheckResult.compatible();
    }

    /**
     * 获取当前调用栈深度
     */
    public int getCallDepth() {
        return callDepth;
    }

    /**
     * 获取调用栈信息
     */
    public List<LinkageRecord> getCallStack() {
        return new ArrayList<>(callStack);
    }

    /**
     * 获取链接统计信息
     */
    public LinkageStatistics getStatistics() {
        return new LinkageStatistics(
            totalCalls,
            resolvedCalls,
            failedCalls,
            callDepth,
            functionTable.size(),
            labelTable.size(),
            structTable.size()
        );
    }

    /**
     * 清除所有符号
     */
    public void clear() {
        functionTable.clear();
        labelTable.clear();
        structTable.clear();
        callStack.clear();
        callDepth = 0;
        totalCalls = 0;
        resolvedCalls = 0;
        failedCalls = 0;
    }

    // 内部类

    /**
     * 链接结果
     */
    public static class LinkageResult {
        private final boolean success;
        private final String errorMessage;
        private final FunctionSymbol targetFunction;
        private final LinkageRecord linkageRecord;

        private LinkageResult(boolean success, String errorMessage,
                             FunctionSymbol targetFunction, LinkageRecord linkageRecord) {
            this.success = success;
            this.errorMessage = errorMessage;
            this.targetFunction = targetFunction;
            this.linkageRecord = linkageRecord;
        }

        public static LinkageResult success(FunctionSymbol function, LinkageRecord record) {
            return new LinkageResult(true, null, function, record);
        }

        public static LinkageResult failure(String errorMessage) {
            return new LinkageResult(false, errorMessage, null, null);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public FunctionSymbol getTargetFunction() {
            return targetFunction;
        }

        public LinkageRecord getLinkageRecord() {
            return linkageRecord;
        }
    }

    /**
     * 兼容性检查结果
     */
    public static class CompatibilityCheckResult {
        private final boolean compatible;
        private final String errorMessage;

        private CompatibilityCheckResult(boolean compatible, String errorMessage) {
            this.compatible = compatible;
            this.errorMessage = errorMessage;
        }

        public static CompatibilityCheckResult compatible() {
            return new CompatibilityCheckResult(true, null);
        }

        public static CompatibilityCheckResult incompatible(String errorMessage) {
            return new CompatibilityCheckResult(false, errorMessage);
        }

        public boolean isCompatible() {
            return compatible;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

    /**
     * 链接记录
     */
    public static class LinkageRecord {
        private final String callerName;
        private final String calleeName;
        private final long startTime;
        private long completionTime;
        private final int callDepth;

        public LinkageRecord(String callerName, String calleeName, long startTime, int callDepth) {
            this.callerName = callerName;
            this.calleeName = calleeName;
            this.startTime = startTime;
            this.callDepth = callDepth;
        }

        public void setCompletionTime(long completionTime) {
            this.completionTime = completionTime;
        }

        public String getCallerName() {
            return callerName;
        }

        public String getCalleeName() {
            return calleeName;
        }

        public long getStartTime() {
            return startTime;
        }

        public long getCompletionTime() {
            return completionTime;
        }

        public int getCallDepth() {
            return callDepth;
        }

        public long getDuration() {
            return completionTime > 0 ? completionTime - startTime : 0;
        }

        @Override
        public String toString() {
            return String.format("Call[%s -> %s, depth=%d, duration=%d ns]",
                callerName, calleeName, callDepth, getDuration());
        }
    }

    /**
     * 链接统计信息
     */
    public static class LinkageStatistics {
        private final int totalCalls;
        private final int resolvedCalls;
        private final int failedCalls;
        private final int maxCallDepth;
        private final int functionCount;
        private final int labelCount;
        private final int structCount;

        public LinkageStatistics(int totalCalls, int resolvedCalls, int failedCalls,
                               int maxCallDepth, int functionCount, int labelCount, int structCount) {
            this.totalCalls = totalCalls;
            this.resolvedCalls = resolvedCalls;
            this.failedCalls = failedCalls;
            this.maxCallDepth = maxCallDepth;
            this.functionCount = functionCount;
            this.labelCount = labelCount;
            this.structCount = structCount;
        }

        public int getTotalCalls() {
            return totalCalls;
        }

        public int getResolvedCalls() {
            return resolvedCalls;
        }

        public int getFailedCalls() {
            return failedCalls;
        }

        public int getMaxCallDepth() {
            return maxCallDepth;
        }

        public int getFunctionCount() {
            return functionCount;
        }

        public int getLabelCount() {
            return labelCount;
        }

        public int getStructCount() {
            return structCount;
        }

        public double getSuccessRate() {
            return totalCalls > 0 ? (double) resolvedCalls / totalCalls : 0.0;
        }

        @Override
        public String toString() {
            return String.format(
                "Linkage Statistics:\n" +
                "  Total Calls: %d\n" +
                "  Resolved: %d (%.2f%%)\n" +
                "  Failed: %d\n" +
                "  Max Call Depth: %d\n" +
                "  Functions: %d\n" +
                "  Labels: %d\n" +
                "  Structs: %d",
                totalCalls,
                resolvedCalls,
                getSuccessRate() * 100,
                failedCalls,
                maxCallDepth,
                functionCount,
                labelCount,
                structCount
            );
        }
    }
}
