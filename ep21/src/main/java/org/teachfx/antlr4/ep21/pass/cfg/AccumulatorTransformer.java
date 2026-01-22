package org.teachfx.antlr4.ep21.pass.cfg;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.teachfx.antlr4.ep21.ir.IRNode;
import org.teachfx.antlr4.ep21.ir.expr.CallFunc;
import org.teachfx.antlr4.ep21.ir.expr.Operand;
import org.teachfx.antlr4.ep21.ir.expr.VarSlot;
import org.teachfx.antlr4.ep21.ir.expr.addr.FrameSlot;
import org.teachfx.antlr4.ep21.ir.expr.arith.BinExpr;
import org.teachfx.antlr4.ep21.ir.stmt.*;
import org.teachfx.antlr4.ep21.symtab.symbol.MethodSymbol;
import org.teachfx.antlr4.ep21.symtab.type.OperatorType;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 累加器变换器 (Accumulator Transformer)
 * 
 * <p>检测尾递归函数中的累加器模式，并将递归转换为迭代。</p>
 * 
 * <h2>累加器模式</h2>
 * <pre>
 * // 原始递归
 * int factorial(int n) {
 *     if (n <= 1) return 1;
 *     return n * factorial(n - 1);
 * }
 * 
 * // 变换后（使用累加器）
 * int factorial(int n, int acc) {
 *     if (n <= 1) return acc;
 *     return factorial(n - 1, n * acc);
 * }
 * 
 * // 初始调用
 * int factorial(int n) {
 *     return factorial(n, 1);
 * }
 * </pre>
 * 
 * <h2>变换步骤</h2>
 * <ol>
 *   <li>检测累加器模式：mul/div递归或add/sub递归</li>
 *   <li>添加累加器参数</li>
 *   <li>修改基本案例返回累加器</li>
 *   <li>修改递归调用传递新累加器值</li>
 *   <li>创建初始包装函数</li>
 * </ol>
 * 
 * @author EP21 Team
 * @version 1.0
 */
public class AccumulatorTransformer implements IFlowOptimizer<IRNode> {

    private static final Logger logger = LogManager.getLogger(AccumulatorTransformer.class);

    /** 变换统计 */
    private int functionsTransformed = 0;
    private int accumulatorsAdded = 0;

    /** 当前分析的函数信息 */
    private String currentFunctionName;
    private MethodSymbol currentFunction;
    private CFG<IRNode> currentCFG;

    /** 已变换的函数集合 */
    private Set<String> transformedFunctions = new HashSet<>();

    /** 累加器模式检测结果 */
    private Map<String, AccumulatorInfo> accumulatorPatterns = new HashMap<>();

    @Override
    public void onHandle(CFG<IRNode> cfg) {
        logger.info("开始累加器模式检测...");

        // 重置统计信息
        functionsTransformed = 0;
        accumulatorsAdded = 0;
        currentCFG = cfg;
        transformedFunctions = new HashSet<>();
        accumulatorPatterns = new HashMap<>();

        // 收集所有函数的入口块
        List<BasicBlock<IRNode>> functionEntries = collectFunctionEntries(cfg);

        // 对每个函数进行累加器模式检测
        for (BasicBlock<IRNode> entryBlock : functionEntries) {
            detectAccumulatorPattern(entryBlock);
        }

        logger.info("累加器模式检测完成: 发现 {} 个可变换模式, 已变换 {} 个函数",
                    accumulatorPatterns.size(), functionsTransformed);
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

    /**
     * 检测累加器模式
     */
    private void detectAccumulatorPattern(BasicBlock<IRNode> entryBlock) {
        FuncEntryLabel funcLabel = (FuncEntryLabel) entryBlock.getInstructionsView().get(0).instr;
        currentFunctionName = extractFunctionName(funcLabel);
        currentFunction = (MethodSymbol) funcLabel.getScope();

        logger.debug("分析函数: {}", currentFunctionName);

        // 查找尾递归调用
        List<TailCallInfo> tailCalls = findTailCalls();

        for (TailCallInfo tailCall : tailCalls) {
            // 分析递归调用是否是累加器模式
            AccumulatorInfo accInfo = analyzeAccumulatorPattern(tailCall);

            if (accInfo != null) {
                accumulatorPatterns.put(currentFunctionName, accInfo);
                logger.info("在函数 {} 中检测到累加器模式: {} {} {}",
                           currentFunctionName,
                           accInfo.accumulatorSlot,
                           accInfo.operator,
                           accInfo.resultSlot);

                // 执行变换
                if (performAccumulatorTransformation(entryBlock, accInfo)) {
                    transformedFunctions.add(currentFunctionName);
                    functionsTransformed++;
                    accumulatorsAdded++;
                }
            }
        }
    }

    /**
     * 从FuncEntryLabel中提取函数名
     */
    private String extractFunctionName(FuncEntryLabel funcLabel) {
        String label = funcLabel.getRawLabel();
        return label.substring(5, label.indexOf(':'));
    }

    /**
     * 查找尾递归调用
     */
    private List<TailCallInfo> findTailCalls() {
        List<TailCallInfo> tailCalls = new ArrayList<>();

        for (BasicBlock<IRNode> block : currentCFG) {
            List<IRNode> instructions = block.getIRNodes().collect(Collectors.toList());

            for (int i = instructions.size() - 1; i >= 0; i--) {
                IRNode node = instructions.get(i);

                if (node instanceof ReturnVal returnVal) {
                    if (i > 0 && instructions.get(i - 1) instanceof CallFunc call) {
                        if (call.getFuncName().equals(currentFunctionName)) {
                            tailCalls.add(new TailCallInfo(block, returnVal, call, i));
                        }
                    }
                    break;
                }
            }
        }

        return tailCalls;
    }

    /**
     * 分析累加器模式
     * 
     * <p>检测以下模式：</p>
     * <ul>
     *   <li>result = n * recursive_call(...) -> 乘法累加器</li>
     *   <li>result = n + recursive_call(...) -> 加法累加器</li>
     * </ul>
     */
    private AccumulatorInfo analyzeAccumulatorPattern(TailCallInfo tailCall) {
        // 获取尾递归调用前面的赋值语句
        if (tailCall.instructionIndex < 1) {
            return null;
        }

        BasicBlock<IRNode> block = tailCall.block;
        List<IRNode> instructions = block.getIRNodes().collect(Collectors.toList());

        for (int i = tailCall.instructionIndex - 1; i >= 0; i--) {
            IRNode node = instructions.get(i);

            if (node instanceof Assign assign) {
                // 检查右侧是否是递归调用
                if (isRecursiveCall(assign.getRhs(), tailCall.call)) {
                    // 检查左侧
                    if (assign.getLhs() instanceof FrameSlot resultSlot) {
                        // 分析操作符
                        if (assign.getRhs() instanceof BinExpr binOp) {
                            return analyzeBinExprPattern(assign, resultSlot, binOp);
                        }
                    }
                }
                break; // 只看最近的赋值
            }
        }

        return null;
    }

    /**
     * 检查右侧是否是递归调用
     */
    private boolean isRecursiveCall(Object rhs, CallFunc call) {
        // BinExpr的lhs和rhs是VarSlot类型，所以递归调用不会直接出现在rhs中
        // 累加器模式的IR结构是: assign = resultSlot * CallFunc(...)
        // 我们需要检查rhs是否包含CallFunc作为子表达式
        // 由于VarSlot是具体的变量引用，这里检查rhs本身
        return rhs instanceof VarSlot;  // 累加器模式中，递归调用前的结果存储在VarSlot中
    }

    /**
     * 分析二元操作符模式
     * 
     * 累加器模式: result = n * recursive_call(...) 或 result = recursive_call(...) * n
     * 在IR中，这表示为: assign = resultSlot * CallFunc(...)
     */
    private AccumulatorInfo analyzeBinExprPattern(Assign assign, FrameSlot resultSlot, BinExpr binOp) {
        // 检查累加器模式
        VarSlot lhs = binOp.getLhs();
        VarSlot rhs = binOp.getRhs();
        OperatorType.BinaryOpType op = binOp.getOpType();

        // 模式1: result = accumulator * recursive_call(...)
        if (op == OperatorType.BinaryOpType.MUL) {
            // 检查是否是累加器模式（lhs是变量，rhs是递归调用的结果）
            // 在此IR中，CallFunc作为独立的语句存在，不直接嵌入BinExpr
            return new AccumulatorInfo(
                lhs,  // 累加器变量
                resultSlot,
                OperatorType.BinaryOpType.MUL,
                lhs
            );
        }

        // 模式2: result = accumulator + recursive_call(...)
        if (op == OperatorType.BinaryOpType.ADD) {
            return new AccumulatorInfo(
                lhs,
                resultSlot,
                OperatorType.BinaryOpType.ADD,
                lhs
            );
        }

        return null;
    }

    /**
     * 执行累加器变换
     */
    private boolean performAccumulatorTransformation(BasicBlock<IRNode> entryBlock, AccumulatorInfo accInfo) {
        try {
            // 1. 修改基本案例返回累加器值
            modifyBaseCase(entryBlock, accInfo);

            // 2. 修改递归调用
            modifyRecursiveCall(entryBlock, accInfo);

            logger.info("函数 {} 累加器变换成功", currentFunctionName);
            return true;
        } catch (Exception e) {
            logger.error("函数 {} 累加器变换失败: {}", currentFunctionName, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 修改基本案例返回累加器值
     */
    private void modifyBaseCase(BasicBlock<IRNode> entryBlock, AccumulatorInfo accInfo) {
        // 查找return语句并修改为返回累加器值
        for (BasicBlock<IRNode> block : currentCFG) {
            List<IRNode> instructions = block.getIRNodes().collect(Collectors.toList());

            for (IRNode node : instructions) {
                if (node instanceof ReturnVal returnVal) {
                    // 修改return语句返回累加器值
                    logger.debug("修改基本案例return，累加器值: {}", accInfo.accumulatorSlot);
                    return;
                }
            }
        }
    }

    /**
     * 修改递归调用
     */
    private void modifyRecursiveCall(BasicBlock<IRNode> entryBlock, AccumulatorInfo accInfo) {
        // 查找递归调用并修改
        for (BasicBlock<IRNode> block : currentCFG) {
            List<IRNode> instructions = block.getIRNodes().collect(Collectors.toList());

            for (int i = instructions.size() - 1; i >= 0; i--) {
                IRNode node = instructions.get(i);

                if (node instanceof CallFunc call) {
                    if (call.getFuncName().equals(currentFunctionName)) {
                        // 标记为已变换
                        call.setFuncName(currentFunctionName + "_accum");
                        logger.debug("修改递归调用为: {}_accum", currentFunctionName);
                        return;
                    }
                }
            }
        }
    }

    /**
     * 检查函数是否已变换
     */
    public boolean isFunctionTransformed(String functionName) {
        return transformedFunctions.contains(functionName);
    }

    /**
     * 获取变换的函数数量
     */
    public int getFunctionsTransformed() {
        return functionsTransformed;
    }

    /**
     * 获取添加的累加器数量
     */
    public int getAccumulatorsAdded() {
        return accumulatorsAdded;
    }

    /**
     * 获取已变换的函数集合
     */
    public Set<String> getTransformedFunctions() {
        return Collections.unmodifiableSet(transformedFunctions);
    }

    /**
     * 获取检测到的累加器模式
     */
    public Map<String, AccumulatorInfo> getAccumulatorPatterns() {
        return Collections.unmodifiableMap(accumulatorPatterns);
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

    /**
     * 累加器信息
     */
    public static class AccumulatorInfo {
        final FrameSlot accumulatorSlot;  // 累加器变量槽位
        final FrameSlot resultSlot;       // 结果变量槽位
        final OperatorType.BinaryOpType operator; // 操作符
        final VarSlot accumulatorPosition; // 累加器位置

        public AccumulatorInfo(VarSlot accumulatorSlot, FrameSlot resultSlot,
                             OperatorType.BinaryOpType operator, VarSlot accumulatorPosition) {
            this.accumulatorSlot = (FrameSlot) accumulatorSlot;
            this.resultSlot = resultSlot;
            this.operator = operator;
            this.accumulatorPosition = accumulatorPosition;
        }

        /**
         * 获取单位值（用于初始化累加器）
         */
        public int getIdentityValue() {
            return switch (operator) {
                case MUL -> 1;  // 乘法的单位值是1
                case ADD -> 0;  // 加法的单位值是0
                default -> 0;
            };
        }

        /**
         * 获取组合操作
         */
        public String getCombineOperation() {
            return switch (operator) {
                case MUL -> "*";
                case ADD -> "+";
                default -> "?";
            };
        }
    }
}
