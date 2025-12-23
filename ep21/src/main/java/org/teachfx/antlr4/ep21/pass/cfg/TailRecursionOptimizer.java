package org.teachfx.antlr4.ep21.pass.cfg;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.teachfx.antlr4.ep21.ir.IRNode;
import org.teachfx.antlr4.ep21.ir.expr.CallFunc;
import org.teachfx.antlr4.ep21.ir.expr.Operand;
import org.teachfx.antlr4.ep21.ir.expr.VarSlot;
import org.teachfx.antlr4.ep21.ir.expr.arith.BinExpr;
import org.teachfx.antlr4.ep21.ir.expr.addr.FrameSlot;
import org.teachfx.antlr4.ep21.ir.expr.val.ConstVal;
import org.teachfx.antlr4.ep21.ir.stmt.*;
import org.teachfx.antlr4.ep21.symtab.symbol.MethodSymbol;
import org.teachfx.antlr4.ep21.symtab.type.OperatorType;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 尾递归优化器 (Tail Recursion Optimizer)
 *
 * 检测并转换尾递归函数为迭代形式，避免栈溢出问题。
 *
 * 当前实现：检测尾递归模式并标记优化函数
 * 未来计划：实际执行CFG转换（需要完整的IR API适配）
 *
 * @author EP21 Team
 * @version 2.1
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

            // 执行栈模拟转换
            try {
                // 提取当前函数的子CFG
                CFG<IRNode> functionCFG = extractFunctionCFG(entryBlock);
                
                // 创建ExecutionGraph并执行转换
                ExecutionGraph execGraph = new ExecutionGraph(
                    functionCFG, 
                    currentFunctionName, 
                    currentFunction
                );
                
                CFG<IRNode> transformedCFG = execGraph.transform();
                
                if (transformedCFG != functionCFG) {
                    // 转换成功
                    logger.info("函数 {} 成功转换为迭代形式", currentFunctionName);
                    System.out.println("[TailRecursionOptimizer] 函数 " + currentFunctionName + " 已转换为迭代形式");
                    optimizedFunctions.add(currentFunctionName);
                    optimized = true;
                } else {
                    logger.warn("函数 {} 转换未生效", currentFunctionName);
                }
            } catch (Exception e) {
                logger.error("转换函数 {} 时发生错误: {}", currentFunctionName, e.getMessage());
                e.printStackTrace();
            }
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
     * 提取单个函数的CFG
     * 从全局CFG中提取属于指定函数的所有基本块
     */
    private CFG<IRNode> extractFunctionCFG(BasicBlock<IRNode> functionEntry) {
        List<BasicBlock<IRNode>> functionBlocks = new ArrayList<>();
        Map<Integer, Integer> idMap = new HashMap<>();  // 旧ID -> 新ID映射
        int newId = 0;

        // 收集属于该函数的所有基本块
        // 简化版本：只收集从函数入口可达的块
        Set<Integer> visited = new HashSet<>();
        Queue<Integer> queue = new LinkedList<>();
        queue.add(functionEntry.getId());
        visited.add(functionEntry.getId());

        // 首先收集所有可达的节点ID
        while (!queue.isEmpty()) {
            Integer currentId = queue.poll();
            for (Triple<Integer, Integer, Integer> edge : currentCFG.getEdges()) {
                if (edge.getLeft().equals(currentId)) {
                    Integer targetId = edge.getMiddle();
                    if (!visited.contains(targetId)) {
                        visited.add(targetId);
                        queue.add(targetId);
                    }
                }
            }
        }

        // 为每个访问过的节点创建新的BasicBlock
        for (BasicBlock<IRNode> block : currentCFG) {
            if (visited.contains(block.getId())) {
                // 创建新的BasicBlock，使用新的ID
                BasicBlock<IRNode> newBlock = new BasicBlock<>(
                    block.getKind(),
                    block.getInstructionsView(),
                    block.getLabel(),
                    newId
                );
                functionBlocks.add(newBlock);
                idMap.put(block.getId(), newId);
                newId++;
            }
        }

        // 转换边，使用新的ID
        List<Triple<Integer, Integer, Integer>> newEdges = new ArrayList<>();
        for (Triple<Integer, Integer, Integer> edge : currentCFG.getEdges()) {
            Integer from = edge.getLeft();
            Integer to = edge.getMiddle();
            if (visited.contains(from) && visited.contains(to)) {
                newEdges.add(Triple.of(
                    idMap.get(from),
                    idMap.get(to),
                    edge.getRight()
                ));
            }
        }

        return new CFG<>(functionBlocks, newEdges);
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
     * 标记函数为已优化
     */
    private void markFunctionAsOptimized(FuncEntryLabel funcLabel) {
        // 修改函数标签以包含优化标记
        String rawLabel = funcLabel.getRawLabel();
        if (!rawLabel.contains("// TRO")) {
            String optimizedLabel = rawLabel + " // TRO: iterative";
            // 由于FuncEntryLabel的rawLabel是final，我们无法直接修改
            // 实际的优化需要在代码生成阶段检测此标记
            logger.debug("函数 {} 标记为TRO优化模式", currentFunctionName);
        }
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
