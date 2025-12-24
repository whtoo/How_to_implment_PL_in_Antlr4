package org.teachfx.antlr4.ep21.pass.cfg;

import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.teachfx.antlr4.ep21.ir.IRNode;
import org.teachfx.antlr4.ep21.ir.expr.CallFunc;
import org.teachfx.antlr4.ep21.ir.expr.Operand;
import org.teachfx.antlr4.ep21.ir.expr.VarSlot;
import org.teachfx.antlr4.ep21.ir.expr.addr.FrameSlot;
import org.teachfx.antlr4.ep21.ir.expr.arith.BinExpr;
import org.teachfx.antlr4.ep21.ir.expr.val.ConstVal;
import org.teachfx.antlr4.ep21.ir.stmt.*;
import org.teachfx.antlr4.ep21.symtab.scope.Scope;
import org.teachfx.antlr4.ep21.symtab.symbol.MethodSymbol;
import org.teachfx.antlr4.ep21.symtab.type.OperatorType;

import java.util.*;

/**
 * ExecutionGraph - 执行图构建器
 *
 * 用于显式栈模拟的递归转换。将递归CFG转换为使用显式栈的迭代式CFG。
 *
 * 基于Baeldung算法实现：https://www.baeldung.com/cs/convert-recursion-to-iteration
 *
 * 实现策略：
 * 1. 分析递归调用模式，识别尾递归/非尾递归
 * 2. 为每个递归调用创建栈帧管理代码
 * 3. 生成主循环：while (!stack.isEmpty()) { ... }
 * 4. 生成结果合并代码
 *
 * @author EP21 Team
 * @version 2.0
 * @since 2025-12-23
 */
public class ExecutionGraph {

    private static final Logger logger = LogManager.getLogger(ExecutionGraph.class);

    /** 原始CFG */
    private final CFG<IRNode> originalCFG;

    /** 转换后的CFG */
    private CFG<IRNode> transformedCFG;

    /** 函数名 */
    private final String functionName;

    /** 函数符号 */
    private final MethodSymbol functionSymbol;

    /** 递归调用列表 */
    private final List<RecursiveCall> recursiveCalls;

    /** 栈帧类型 */
    private final StackFrameType stackFrameType;

    /** 辅助类 */
    private final IRInstructionBuilder irBuilder;
    private final CFGMutableBuilder cfgBuilder;

    /**
     * 递归调用信息
     */
    public static class RecursiveCall {
        final BasicBlock<IRNode> block;
        final CallFunc callInstr;
        final int instructionIndex;
        final boolean isTailCall;

        public RecursiveCall(BasicBlock<IRNode> block, CallFunc callInstr,
                            int instructionIndex, boolean isTailCall) {
            this.block = block;
            this.callInstr = callInstr;
            this.instructionIndex = instructionIndex;
            this.isTailCall = isTailCall;
        }

        public String getFunctionName() {
            return callInstr.getFuncName();
        }

        public int getArgumentsCount() {
            return callInstr.getArgs();
        }
    }

    /**
     * 栈帧类型定义
     */
    public enum StackFrameType {
        /** 简单栈帧（单参数） */
        SIMPLE,
        /** 复杂栈帧（多参数） */
        COMPLEX,
        /** Fibonacci专用栈帧 */
        FIBONACCI
    }

    /**
     * 创建ExecutionGraph
     *
     * @param originalCFG 原始CFG
     * @param functionName 函数名
     * @param functionSymbol 函数符号
     */
    public ExecutionGraph(CFG<IRNode> originalCFG, String functionName, MethodSymbol functionSymbol) {
        this.originalCFG = originalCFG;
        this.functionName = functionName;
        this.functionSymbol = functionSymbol;
        this.recursiveCalls = new ArrayList<>();
        this.stackFrameType = determineStackFrameType();
        this.irBuilder = new IRInstructionBuilder();
        this.cfgBuilder = new CFGMutableBuilder();

        analyzeRecursiveCalls();
    }

    /**
     * 确定栈帧类型
     */
    private StackFrameType determineStackFrameType() {
        int argCount = functionSymbol.getArgs();

        // 检查是否是Fibonacci模式
        if (functionName.toLowerCase().contains("fib") && argCount == 1) {
            long recursiveCallCount = countRecursiveCalls();
            if (recursiveCallCount == 2) {
                return StackFrameType.FIBONACCI;
            }
        }

        return argCount <= 1 ? StackFrameType.SIMPLE : StackFrameType.COMPLEX;
    }

    /**
     * 统计递归调用数量
     */
    private long countRecursiveCalls() {
        long count = 0;
        for (BasicBlock<IRNode> block : originalCFG) {
            for (Loc<IRNode> loc : block) {
                if (loc.instr instanceof CallFunc call) {
                    if (call.getFuncName().equals(functionName)) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    /**
     * 分析递归调用
     */
    private void analyzeRecursiveCalls() {
        for (BasicBlock<IRNode> block : originalCFG) {
            List<IRNode> instructions = block.getIRNodes().toList();

            for (int i = 0; i < instructions.size(); i++) {
                IRNode node = instructions.get(i);

                if (node instanceof CallFunc call) {
                    if (call.getFuncName().equals(functionName)) {
                        // 检查是否是尾调用
                        boolean isTailCall = isTailCall(instructions, i);
                        recursiveCalls.add(new RecursiveCall(block, call, i, isTailCall));

                        logger.debug("Found recursive call at block {} index {} (tail: {})",
                            block.getId(), i, isTailCall);
                    }
                }
            }
        }

        logger.info("Found {} recursive calls in function {}",
            recursiveCalls.size(), functionName);
    }

    /**
     * 检查调用是否是尾调用
     */
    private boolean isTailCall(List<IRNode> instructions, int callIndex) {
        if (callIndex >= instructions.size() - 1) {
            return false;
        }

        IRNode nextInstr = instructions.get(callIndex + 1);
        return nextInstr instanceof ReturnVal;
    }

    /**
     * 从原始CFG中提取Scope
     */
    private Scope extractScopeFromOriginalCFG() {
        // 查找函数入口块并提取Scope
        for (BasicBlock<IRNode> block : originalCFG) {
            if (!block.isEmpty()) {
                IRNode firstInstr = block.getInstructionsView().get(0).instr;
                if (firstInstr instanceof FuncEntryLabel) {
                    FuncEntryLabel funcLabel = (FuncEntryLabel) firstInstr;
                    // 从Label中提取Scope
                    return funcLabel.getScope();
                }
            }
        }
        return null;  // 如果找不到，返回null
    }

    /**
     * 执行栈模拟转换
     *
     * @return 转换后的CFG
     */
    public CFG<IRNode> transform() {
        logger.info("Starting stack simulation transformation for {}", functionName);

        if (recursiveCalls.isEmpty()) {
            logger.warn("No recursive calls found, skipping transformation");
            return originalCFG;
        }

        switch (stackFrameType) {
            case FIBONACCI:
                transformedCFG = transformFibonacciIterative();
                break;
            case SIMPLE:
                transformedCFG = transformSimpleRecursive();
                break;
            case COMPLEX:
                transformedCFG = transformComplexRecursive();
                break;
        }

        logger.info("Stack simulation transformation completed for {}", functionName);
        return transformedCFG;
    }

    /**
     * 转换Fibonacci函数为迭代形式
     *
     * ⚠️ 技术债务: 此方法当前未实现CFG级转换
     * 
     * 实际的Fibonacci优化在代码生成阶段完成:
     * - RegisterVMGenerator.TROHelper.generateFibonacciIterative()
     * - 直接生成迭代式汇编代码，避免CFG复杂性
     *
     * 如需实现CFG级转换，需要:
     * 1. 创建累加器变量 (a, b)
     * 2. 生成while循环基本块
     * 3. 实现累加器更新逻辑 (temp = a + b; a = b; b = temp)
     * 4. 重建CFG边关系
     *
     * @return 原始CFG（未修改）
     * @see RegisterVMGenerator.TROHelper#generateFibonacciIterative
     * @deprecated 使用RegisterVMGenerator.TROHelper进行代码生成层优化
     */
    @Deprecated
    private CFG<IRNode> transformFibonacciIterative() {
        logger.warn("CFG-level Fibonacci transformation is not implemented.");
        logger.warn("Actual optimization is performed in RegisterVMGenerator.TROHelper during code generation.");
        return originalCFG;
    }

    /**
     * 转换简单递归（单参数尾递归）
     *
     * ⚠️ 技术债务: 此方法当前未实现CFG级转换
     * 
     * 示例转换: factorial(n) → while(n > 1) { result *= n; n--; }
     *
     * @return 原始CFG（未修改）
     * @deprecated 待实现 - 需要40-60小时工作量
     */
    @Deprecated
    private CFG<IRNode> transformSimpleRecursive() {
        logger.warn("CFG-level simple recursive transformation is not implemented.");
        logger.info("For factorial-like functions, consider implementing accumulator pattern:");
        logger.info("  factorial_tr(n, acc) = n <= 1 ? acc : factorial_tr(n-1, n*acc)");
        return originalCFG;
    }

    /**
     * 转换复杂递归（多参数）
     *
     * ⚠️ 技术债务: 此方法当前未实现CFG级转换
     * 
     * 示例转换: gcd(a, b) → while(b != 0) { temp = a % b; a = b; b = temp; }
     *
     * @return 原始CFG（未修改）
     * @deprecated 待实现 - 需要40-60小时工作量
     */
    @Deprecated
    private CFG<IRNode> transformComplexRecursive() {
        logger.warn("CFG-level complex recursive transformation is not implemented.");
        logger.info("For functions like gcd, consider implementing iterative version:");
        logger.info("  gcd(a, b) { while(b != 0) { int temp = a % b; a = b; b = temp; } return a; }");
        return originalCFG;
    }

    /**
     * 获取递归调用列表
     */
    public List<RecursiveCall> getRecursiveCalls() {
        return Collections.unmodifiableList(recursiveCalls);
    }

    /**
     * 获取栈帧类型
     */
    public StackFrameType getStackFrameType() {
        return stackFrameType;
    }

    /**
     * 获取函数名
     */
    public String getFunctionName() {
        return functionName;
    }

    /**
     * 获取函数符号
     */
    public MethodSymbol getFunctionSymbol() {
        return functionSymbol;
    }

    /**
     * 是否有递归调用
     */
    public boolean hasRecursiveCalls() {
        return !recursiveCalls.isEmpty();
    }

    /**
     * 获取转换后的CFG
     */
    public CFG<IRNode> getTransformedCFG() {
        return transformedCFG;
    }
}
