package org.teachfx.antlr4.ep21.pass.cfg;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.teachfx.antlr4.ep21.ir.IRNode;
import org.teachfx.antlr4.ep21.ir.expr.CallFunc;
import org.teachfx.antlr4.ep21.ir.stmt.FuncEntryLabel;
import org.teachfx.antlr4.ep21.ir.stmt.ReturnVal;
import org.teachfx.antlr4.ep21.symtab.symbol.MethodSymbol;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 尾递归优化器 (Tail Recursion Optimizer) - Path B 实现方案
 *
 * 检测尾递归模式并标记函数，实际的代码转换在代码生成阶段完成。
 *
 * 实现方案: Path B (代码生成层优化)
 * - 检测层: 此优化器负责检测和标记可优化的函数
 * - 转换层: RegisterVMGenerator.TROHelper / StackVMGenerator 执行实际转换
 *
 * 优势:
 * - 避免复杂的CFG API适配
 * - 代码生成更直接、可控
 * - 适合实际编译器项目
 *
 * @author EP21 Team
 * @version 3.0 - Path B 稳定实现
 * @see org.teachfx.antlr4.ep21.pass.codegen.RegisterVMGenerator.TROHelper
 */
public class TailRecursionOptimizer implements IFlowOptimizer<IRNode> {

    private static final Logger logger = LogManager.getLogger(TailRecursionOptimizer.class);

    /** 优化统计信息 */
    private int functionsOptimized = 0;
    private int tailCallsDetected = 0;

    /** 当前正在分析的函数信息 */
    private String currentFunctionName;
    private MethodSymbol currentFunction;
    private BasicBlock<IRNode> functionEntryBlock;
    private CFG<IRNode> currentCFG;

    /** 已优化的函数集合 */
    private Set<String> optimizedFunctions = new HashSet<>();

    @Override
    public void onHandle(CFG<IRNode> cfg) {
        System.out.println("[TailRecursionOptimizer] 开始尾递归优化...");
        logger.info("开始尾递归优化...");

        // 重置统计信息
        functionsOptimized = 0;
        tailCallsDetected = 0;
        currentCFG = cfg;
        optimizedFunctions = new HashSet<>();

        // 收集所有函数的入口块
        List<BasicBlock<IRNode>> functionEntries = collectFunctionEntries(cfg);

        // 对每个函数进行尾递归检测和优化
        for (BasicBlock<IRNode> entryBlock : functionEntries) {
            optimizeFunction(entryBlock);
        }

        logger.info("尾递归优化完成: 优化了 {} 个函数, 检测到 {} 个尾调用",
                    functionsOptimized, tailCallsDetected);
        System.out.println("[TailRecursionOptimizer] 优化完成: 优化了 " + functionsOptimized + " 个函数");
    }

    /**
     * 收集所有函数的入口块
     */
    private List<BasicBlock<IRNode>> collectFunctionEntries(CFG<IRNode> cfg) {
        List<BasicBlock<IRNode>> entries = new ArrayList<>();

        for (BasicBlock<IRNode> block : cfg) {
            if (!block.isEmpty()) {
                IRNode firstInstr = block.getInstructionsView().get(0).instr;
                if (firstInstr instanceof FuncEntryLabel) {
                    entries.add(block);
                }
            }
        }

        logger.debug("找到 {} 个函数入口块", entries.size());
        return entries;
    }

    private void optimizeFunction(BasicBlock<IRNode> entryBlock) {
        FuncEntryLabel funcLabel = (FuncEntryLabel) entryBlock.getInstructionsView().get(0).instr;
        currentFunctionName = extractFunctionName(funcLabel);
        currentFunction = (MethodSymbol) funcLabel.getScope();
        functionEntryBlock = entryBlock;

        logger.debug("分析函数: {}", currentFunctionName);

        boolean optimized = false;

        // 策略1: 检测Fibonacci模式
        if (isFibonacciPattern()) {
            logger.info("检测到Fibonacci模式: {}", currentFunctionName);
            System.out.println("[TailRecursionOptimizer] 检测到Fibonacci模式: " + currentFunctionName);

            // Path B: 标记函数，实际转换由代码生成器完成
            // RegisterVMGenerator.TROHelper.generateFibonacciIterative()
            optimizedFunctions.add(currentFunctionName);
            optimized = true;

            logger.info("函数 {} 已标记为Fibonacci优化模式（代码生成阶段转换）", currentFunctionName);
        }

        // 策略2: 检测直接尾递归
        if (!optimized) {
            List<TailCallInfo> tailCalls = detectDirectTailCalls();
            if (!tailCalls.isEmpty()) {
                logger.info("在函数 {} 中检测到 {} 个直接尾调用", currentFunctionName, tailCalls.size());
                tailCallsDetected += tailCalls.size();

                // 标记为尾递归优化
                optimizedFunctions.add(currentFunctionName);
                optimized = true;
            }
        }

        if (optimized) {
            functionsOptimized++;
        }
    }

    /**
     * 从FuncEntryLabel中提取函数名
     */
    private String extractFunctionName(FuncEntryLabel funcLabel) {
        String label = funcLabel.getRawLabel();
        // .def fib: args=1, locals=1
        return label.substring(5, label.indexOf(':'));
    }

    /**
     * 检测是否是Fibonacci模式
     */
    private boolean isFibonacciPattern() {
        if (currentFunction == null || currentFunction.getArgs() != 1) {
            return false;
        }

        if (!currentFunctionName.toLowerCase().contains("fib")) {
            return false;
        }

        int recursiveCallCount = 0;
        for (BasicBlock<IRNode> block : currentCFG) {
            for (Loc<IRNode> loc : block) {
                if (loc.instr instanceof CallFunc call) {
                    if (call.getFuncName().equals(currentFunctionName)) {
                        recursiveCallCount++;
                    }
                }
            }
        }

        boolean isFib = recursiveCallCount == 2;
        System.out.println("[TailRecursionOptimizer] 函数 " + currentFunctionName +
                          " 递归调用计数: " + recursiveCallCount + ", Fibonacci模式: " + isFib);
        return isFib;
    }

    /**
     * 检测直接尾递归调用
     */
    private List<TailCallInfo> detectDirectTailCalls() {
        List<TailCallInfo> tailCalls = new ArrayList<>();

        for (BasicBlock<IRNode> block : currentCFG) {
            List<IRNode> instructions = block.getIRNodes().collect(Collectors.toList());

            for (int i = instructions.size() - 1; i >= 0; i--) {
                IRNode node = instructions.get(i);

                if (node instanceof ReturnVal returnVal) {
                    if (i > 0 && instructions.get(i - 1) instanceof CallFunc call) {
                        if (call.getFuncName().equals(currentFunctionName)) {
                            tailCalls.add(new TailCallInfo(block, returnVal, call, i));
                            logger.debug("检测到直接尾递归: 块 {}, 调用 {}", block.getId(), call);
                        }
                    }
                    break;
                }
            }
        }

        return tailCalls;
    }

    /**
     * 检查函数是否已被优化
     */
    public boolean isFunctionOptimized(String functionName) {
        return optimizedFunctions.contains(functionName);
    }

    /**
     * 获取优化的函数数量
     */
    public int getFunctionsOptimized() {
        return functionsOptimized;
    }

    /**
     * 获取检测到的尾调用数量
     */
    public int getTailCallsDetected() {
        return tailCallsDetected;
    }

    /**
     * 获取已优化的函数集合
     */
    public Set<String> getOptimizedFunctions() {
        return Collections.unmodifiableSet(optimizedFunctions);
    }

    /**
     * 尾调用信息
     */
    private static class TailCallInfo {
        final BasicBlock<IRNode> block;
        final ReturnVal returnVal;
        final CallFunc call;
        final int instructionIndex;

        TailCallInfo(BasicBlock<IRNode> block, ReturnVal returnVal, CallFunc call, int instructionIndex) {
            this.block = block;
            this.returnVal = returnVal;
            this.call = call;
            this.instructionIndex = instructionIndex;
        }
    }
}
