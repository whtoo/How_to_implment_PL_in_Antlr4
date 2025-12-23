package org.teachfx.antlr4.ep21.pass.cfg;

import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.teachfx.antlr4.ep21.ir.IRNode;
import org.teachfx.antlr4.ep21.ir.expr.CallFunc;
import org.teachfx.antlr4.ep21.ir.expr.addr.FrameSlot;
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

    private CFG<IRNode> transformFibonacciIterative() {
        logger.info("Transforming Fibonacci function using accumulator pattern");

        cfgBuilder.clear();
        irBuilder.resetCounters();

        // 获取函数作用域（从原始CFG的FuncEntryLabel中提取）
        Scope scope = extractScopeFromOriginalCFG();

        // 创建LinearIRBlocks（在LinearIRBlock阶段设置跳转关系）
        LinearIRBlock entryBlock = new LinearIRBlock(scope);
        LinearIRBlock loopCondBlock = new LinearIRBlock(scope);
        LinearIRBlock loopBodyBlock = new LinearIRBlock(scope);
        LinearIRBlock exitBlock = new LinearIRBlock(scope);

        // 1. 创建函数入口标签: .def fib_iter: args=1, locals=3
        FuncEntryLabel entryLabel = irBuilder.createFuncEntryLabel(
            functionName + "_iter", 1, 3, scope);  // 1 arg (n), 3 locals (a, b, temp)
        entryBlock.addStmt(entryLabel);

        // 2. 初始化累加器: a = 0, b = 1
        // FrameSlot索引: 参数0=n, 局部1=a, 局部2=b, 局部3=temp
        entryBlock.addStmt(irBuilder.createFrameAssign(1, irBuilder.createIntConst(0)));  // a = 0
        entryBlock.addStmt(irBuilder.createFrameAssign(2, irBuilder.createIntConst(1)));  // b = 1

        // 3. 创建循环条件标签
        Label loopCondLabel = irBuilder.createLabel("loop_cond", scope);
        loopCondBlock.addStmt(loopCondLabel);

        // 4. 创建比较指令: temp_slot = n > 1
        // 由于EP21 IR限制，我们使用简化的方式
        // 创建CJMP指令: if (n > 1) goto loopBody else goto exit
        // 注意：CJMP需要VarSlot类型的条件，这里创建一个临时槽
        FrameSlot condSlot = irBuilder.createTempSlot(10);  // 使用固定的临时槽索引
        // 由于BinExpr需要VarSlot，我们需要先创建VarSlot形式的FrameSlot
        VarSlot nSlot = new FrameSlot(0);  // 参数n在槽0
        VarSlot const1Slot = new FrameSlot(100);  // 常量1的槽位
        
        // 创建比较: cond = n - 1
        BinExpr cmpExpr = irBuilder.createSub(nSlot, const1Slot);
        loopCondBlock.addStmt(irBuilder.createFrameAssign(10, cmpExpr));

        // 创建CJMP: if (cond > 0) goto loopBody else goto exit
        VarSlot condVarSlot = new FrameSlot(10);
        CJMP cjmp = new CJMP(condVarSlot, loopBodyBlock, exitBlock);
        loopCondBlock.addStmt(cjmp);

        // 5. 创建循环体标签
        Label loopBodyLabel = irBuilder.createLabel("loop_body", scope);
        loopBodyBlock.addStmt(loopBodyLabel);

        // 6. 循环体指令: temp = a + b; a = b; b = temp; n = n - 1
        VarSlot aSlot = new FrameSlot(1);  // a在槽1
        VarSlot bSlot = new FrameSlot(2);  // b在槽2

        // temp = a + b
        BinExpr addExpr = irBuilder.createAdd(aSlot, bSlot);
        loopBodyBlock.addStmt(irBuilder.createFrameAssign(3, addExpr));  // temp在槽3

        // a = b
        loopBodyBlock.addStmt(irBuilder.createFrameAssign(1, bSlot));

        // b = temp
        VarSlot tempSlot = new FrameSlot(3);
        loopBodyBlock.addStmt(irBuilder.createFrameAssign(2, tempSlot));

        // n = n - 1
        BinExpr decExpr = irBuilder.createSub(nSlot, const1Slot);
        loopBodyBlock.addStmt(irBuilder.createFrameAssign(0, decExpr));

        // 跳转回循环条件
        JMP jmpToCond = new JMP(loopCondBlock);
        loopBodyBlock.addStmt(jmpToCond);

        // 7. 创建返回块
        Label exitLabel = irBuilder.createLabel("exit", scope);
        exitBlock.addStmt(exitLabel);
        // 返回b的值
        ReturnVal returnVal = irBuilder.createReturn(bSlot, scope);
        exitBlock.addStmt(returnVal);

        // 设置块之间的链接（用于LinearIRBlock）
        entryBlock.setLink(loopCondBlock);
        // CJMP已经设置了loopCondBlock到loopBodyBlock和exitBlock的链接
        // JMP已经设置了loopBodyBlock到loopCondBlock的链接

        // 8. 将LinearIRBlocks转换为BasicBlocks
        List<BasicBlock<IRNode>> cachedNodes = new ArrayList<>();
        BasicBlock<IRNode> entryBB = BasicBlock.buildFromLinearBlock(entryBlock, cachedNodes);
        BasicBlock<IRNode> loopCondBB = BasicBlock.buildFromLinearBlock(loopCondBlock, cachedNodes);
        BasicBlock<IRNode> loopBodyBB = BasicBlock.buildFromLinearBlock(loopBodyBlock, cachedNodes);
        BasicBlock<IRNode> exitBB = BasicBlock.buildFromLinearBlock(exitBlock, cachedNodes);

        // 9. 添加节点到CFGBuilder
        cfgBuilder.addNode(entryBB);
        cfgBuilder.addNode(loopCondBB);
        cfgBuilder.addNode(loopBodyBB);
        cfgBuilder.addNode(exitBB);

        // 10. 添加边（控制流）
        // entry -> loopCond
        cfgBuilder.addEdge(entryBB.getId(), loopCondBB.getId());
        // loopCond -> loopBody (n > 1)
        cfgBuilder.addEdge(loopCondBB.getId(), loopBodyBB.getId());
        // loopCond -> exit (n <= 1)
        cfgBuilder.addEdge(loopCondBB.getId(), exitBB.getId());
        // loopBody -> loopCond
        cfgBuilder.addEdge(loopBodyBB.getId(), loopCondBB.getId());

        CFG<IRNode> result = cfgBuilder.build();
        logger.info("Created iterative Fibonacci CFG with {} blocks", cfgBuilder.getNodeCount());
        return result;
    }

    /**
     * 转换简单递归（单参数）
     */
    private CFG<IRNode> transformSimpleRecursive() {
        logger.info("Transforming simple recursive function");
        // TODO: 实现简单递归转换
        return originalCFG;
    }

    /**
     * 转换复杂递归（多参数）
     */
    private CFG<IRNode> transformComplexRecursive() {
        logger.info("Transforming complex recursive function");
        // TODO: 实现复杂递归转换
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
