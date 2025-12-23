package org.teachfx.antlr4.ep21.pass.cfg;

import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.teachfx.antlr4.ep21.ir.IRNode;
import org.teachfx.antlr4.ep21.ir.expr.*;
import org.teachfx.antlr4.ep21.ir.expr.arith.BinExpr;
import org.teachfx.antlr4.ep21.ir.expr.arith.UnaryExpr;
import org.teachfx.antlr4.ep21.ir.expr.addr.FrameSlot;
import org.teachfx.antlr4.ep21.ir.expr.val.ConstVal;
import org.teachfx.antlr4.ep21.ir.stmt.*;
import org.teachfx.antlr4.ep21.symtab.scope.Scope;
import org.teachfx.antlr4.ep21.symtab.symbol.MethodSymbol;
import org.teachfx.antlr4.ep21.symtab.symbol.VariableSymbol;
import org.teachfx.antlr4.ep21.symtab.type.OperatorType;
import org.teachfx.antlr4.ep21.utils.Kind;

import java.util.ArrayList;
import java.util.List;

/**
 * IR Instruction Builder - 用于方便地构建IR指令
 *
 * 这个类提供了工厂方法来创建各种IR指令，简化代码生成过程。
 * 所有方法都遵循EP21的实际IR API设计。
 *
 * @author EP21 Team
 * @version 2.0
 * @since 2025-12-23
 */
public class IRInstructionBuilder {

    private static final Logger logger = LogManager.getLogger(IRInstructionBuilder.class);

    private int labelCounter = 0;
    private int tempCounter = 0;

    // ==================== Label Instructions ====================

    /**
     * 创建函数入口标签
     * @param funcName 函数名
     * @param args 参数数量
     * @param locals 局部变量数量
     * @param scope 作用域
     */
    public FuncEntryLabel createFuncEntryLabel(String funcName, int args, int locals, Scope scope) {
        FuncEntryLabel entryLabel = new FuncEntryLabel(funcName, args, locals, scope);
        return entryLabel;
    }

    /**
     * 创建普通标签
     * @param name 标签名称
     * @param scope 作用域
     */
    public Label createLabel(String name, Scope scope) {
        Label label = new Label(name, scope);
        return label;
    }

    /**
     * 创建自动命名标签
     * @param scope 作用域
     */
    public Label createAutoLabel(Scope scope) {
        String name = "L" + labelCounter++;
        return createLabel(name, scope);
    }

    /**
     * 创建自动命名标签（带序号）
     * @param scope 作用域
     * @param ord 序号
     */
    public Label createLabelWithOrd(Scope scope, int ord) {
        return new Label(scope, ord);
    }

    // ==================== Assignment Instructions ====================

    /**
     * 创建赋值指令: lhs = rhs
     * @param lhs 左值（必须是VarSlot类型）
     * @param rhs 右值
     */
    public Assign createAssign(VarSlot lhs, Operand rhs) {
        Assign assign = new Assign(lhs, rhs);
        return assign;
    }

    /**
     * 创建FrameSlot赋值
     * @param slotIdx 槽位索引
     * @param rhs 右值
     */
    public Assign createFrameAssign(int slotIdx, Operand rhs) {
        // Directly create Assign to avoid type inference issues
        FrameSlot frameSlot = new FrameSlot(slotIdx);
        Assign assign = new Assign(frameSlot, rhs);
        return assign;
    }

    // ==================== Binary Expression ====================

    /**
     * 创建二元表达式: lhs op rhs
     * 注意: BinExpr需要VarSlot类型的操作数
     */
    public BinExpr createBinExpr(VarSlot lhs, OperatorType.BinaryOpType op, VarSlot rhs) {
        BinExpr binExpr = new BinExpr(op, lhs, rhs);
        return binExpr;
    }

    /**
     * 创建加法表达式
     */
    public BinExpr createAdd(VarSlot lhs, VarSlot rhs) {
        return createBinExpr(lhs, OperatorType.BinaryOpType.ADD, rhs);
    }

    /**
     * 创建减法表达式
     */
    public BinExpr createSub(VarSlot lhs, VarSlot rhs) {
        return createBinExpr(lhs, OperatorType.BinaryOpType.SUB, rhs);
    }

    /**
     * 创建乘法表达式
     */
    public BinExpr createMul(VarSlot lhs, VarSlot rhs) {
        return createBinExpr(lhs, OperatorType.BinaryOpType.MUL, rhs);
    }

    // ==================== Unary Expression ====================

    /**
     * 创建一元表达式: op expr
     * 注意: UnaryExpr需要VarSlot类型的操作数
     */
    public UnaryExpr createUnaryExpr(OperatorType.UnaryOpType op, VarSlot expr) {
        UnaryExpr unaryExpr = new UnaryExpr(op, expr);
        return unaryExpr;
    }

    /**
     * 创建负数表达式
     */
    public UnaryExpr createNeg(VarSlot expr) {
        return createUnaryExpr(OperatorType.UnaryOpType.NEG, expr);
    }

    // ==================== Control Flow Instructions ====================

    /**
     * 创建条件跳转: if (cond) goto thenBlock else goto elseBlock
     * 注意：这里只创建CJMP对象，不设置跳转目标块
     * 跳转目标需要在LinearIRBlock阶段设置
     *
     * @param cond 条件变量
     * @param thenBlock then分支块
     * @param elseBlock else分支块
     */
    public CJMP createCJMP(VarSlot cond, LinearIRBlock thenBlock, LinearIRBlock elseBlock) {
        CJMP cjmp = new CJMP(cond, thenBlock, elseBlock);
        return cjmp;
    }

    /**
     * 创建无条件跳转: goto target
     * 注意：这里只创建JMP对象，不设置跳转目标块
     * 跳转目标需要在LinearIRBlock阶段设置
     *
     * @param target 目标块
     */
    public JMP createJMP(LinearIRBlock target) {
        JMP jmp = new JMP(target);
        return jmp;
    }

    // ==================== Return Instructions ====================

    /**
     * 创建返回指令: return retVal
     * @param retVal 返回值变量（可以是null表示void返回）
     * @param scope 作用域
     */
    public ReturnVal createReturn(VarSlot retVal, Scope scope) {
        ReturnVal returnVal = new ReturnVal(retVal, scope);
        return returnVal;
    }

    /**
     * 创建主函数返回指令: halt
     * @param scope 作用域
     */
    public ReturnVal createHalt(Scope scope) {
        ReturnVal ret = createReturn(null, scope);
        ret.setMainEntry(true);
        return ret;
    }

    // ==================== Function Call Instructions ====================

    /**
     * 创建函数调用指令
     * 注意：CallFunc只存储函数名和参数数量，不存储实际参数
     *
     * @param funcName 函数名
     * @param args 参数数量
     * @param funcType 函数符号
     */
    public CallFunc createCallFunc(String funcName, int args, MethodSymbol funcType) {
        CallFunc callFunc = new CallFunc(funcName, args, funcType);
        return callFunc;
    }

    // ==================== Constant Values ====================

    /**
     * 创建整数常量
     */
    public ConstVal<Integer> createIntConst(int value) {
        return ConstVal.valueOf(value);
    }

    /**
     * 创建布尔常量
     */
    public ConstVal<Boolean> createBoolConst(boolean value) {
        return ConstVal.valueOf(value);
    }

    // ==================== Temporary Variables ====================

    /**
     * 创建临时变量槽
     * @param slotIdx 槽位索引
     */
    public FrameSlot createTempSlot(int slotIdx) {
        FrameSlot tempSlot = new FrameSlot(slotIdx);
        return tempSlot;
    }

    /**
     * 创建下一个临时变量槽（自动递增）
     */
    public FrameSlot createNextTempSlot() {
        return createTempSlot(tempCounter++);
    }

    /**
     * 重置临时计数器
     */
    public void resetTempCounter() {
        tempCounter = 0;
    }

    /**
     * 重置标签计数器
     */
    public void resetLabelCounter() {
        labelCounter = 0;
    }

    /**
     * 重置所有计数器
     */
    public void resetCounters() {
        resetTempCounter();
        resetLabelCounter();
    }

    // ==================== BasicBlock Helpers ====================

    /**
     * 创建基本块
     *
     * @param kind 基本块类型
     * @param label 标签
     * @param instructions 指令列表
     * @param ord 序号
     */
    public BasicBlock<IRNode> createBasicBlock(Kind kind, Label label, List<IRNode> instructions, int ord) {
        List<Loc<IRNode>> locs = new ArrayList<>();
        for (IRNode instr : instructions) {
            locs.add(new Loc<>(instr));
        }

        BasicBlock<IRNode> block = new BasicBlock<>(kind, locs, label, ord);
        return block;
    }

    /**
     * 创建空的基本块
     * @param ord 序号
     * @param scope 作用域
     */
    public BasicBlock<IRNode> createEmptyBasicBlock(int ord, Scope scope) {
        Label label = createLabelWithOrd(scope, ord);
        return createBasicBlock(Kind.CONTINUOUS, label, List.of(), ord);
    }

    /**
     * 创建带有标签的基本块
     * @param ord 序号
     * @param labelName 标签名称
     * @param scope 作用域
     */
    public BasicBlock<IRNode> createLabeledBasicBlock(int ord, String labelName, Scope scope) {
        Label label = createLabel(labelName, scope);
        List<IRNode> instructions = new ArrayList<>();
        instructions.add(label);
        return createBasicBlock(Kind.CONTINUOUS, label, instructions, ord);
    }

    // ==================== Compound Instructions ====================

    /**
     * 创建while循环结构
     * 生成以下结构:
     *   loopHeader:
     *     if (!condition) goto loopExit
     *   loopBody:
     *     ...body instructions...
     *     goto loopHeader
     *   loopExit:
     *
     * 注意：这是辅助结构，实际使用时需要创建LinearIRBlock并设置跳转关系
     */
    public WhileLoopStructure createWhileLoop(
            VarSlot condition,
            String loopName,
            Scope scope) {

        Label loopHeader = createLabel(loopName + "_header", scope);
        Label loopBody = createLabel(loopName + "_body", scope);
        Label loopExit = createLabel(loopName + "_exit", scope);

        return new WhileLoopStructure(loopHeader, loopBody, loopExit, loopName);
    }

    /**
     * While循环结构（辅助类，用于存储循环相关信息）
     */
    public static class WhileLoopStructure {
        public final Label loopHeader;
        public final Label loopBody;
        public final Label loopExit;
        public final String loopName;

        public WhileLoopStructure(Label loopHeader, Label loopBody, Label loopExit, String loopName) {
            this.loopHeader = loopHeader;
            this.loopBody = loopBody;
            this.loopExit = loopExit;
            this.loopName = loopName;
        }

        /**
         * 获取循环的所有标签
         */
        public List<Label> getAllLabels() {
            return List.of(loopHeader, loopBody, loopExit);
        }
    }

    /**
     * 创建if-else结构
     * 生成以下结构:
     *   ifHeader:
     *     if (condition) goto thenBlock else goto elseBlock
     *   thenBlock:
     *     ...then instructions...
     *     goto ifExit
     *   elseBlock:
     *     ...else instructions...
     *     goto ifExit
     *   ifExit:
     *
     * 注意：这是辅助结构，实际使用时需要创建LinearIRBlock并设置跳转关系
     */
    public IfElseStructure createIfElse(
            VarSlot condition,
            String ifName,
            Scope scope) {

        Label ifHeader = createLabel(ifName + "_header", scope);
        Label thenBlock = createLabel(ifName + "_then", scope);
        Label elseBlock = createLabel(ifName + "_else", scope);
        Label ifExit = createLabel(ifName + "_exit", scope);

        return new IfElseStructure(ifHeader, thenBlock, elseBlock, ifExit, ifName);
    }

    /**
     * If-Else结构（辅助类）
     */
    public static class IfElseStructure {
        public final Label ifHeader;
        public final Label thenBlock;
        public final Label elseBlock;
        public final Label ifExit;
        public final String ifName;

        public IfElseStructure(Label ifHeader, Label thenBlock, Label elseBlock, Label ifExit, String ifName) {
            this.ifHeader = ifHeader;
            this.thenBlock = thenBlock;
            this.elseBlock = elseBlock;
            this.ifExit = ifExit;
            this.ifName = ifName;
        }

        /**
         * 获取if-else的所有标签
         */
        public List<Label> getAllLabels() {
            return List.of(ifHeader, thenBlock, elseBlock, ifExit);
        }
    }

    // ==================== Edge Creation Helpers ====================

    /**
     * 创建CFG边
     * @param from 起始节点ID
     * @param to 目标节点ID
     * @param weight 边权重
     */
    public Triple<Integer, Integer, Integer> createEdge(int from, int to, int weight) {
        return Triple.of(from, to, weight);
    }

    /**
     * 创建CFG边（权重为1）
     */
    public Triple<Integer, Integer, Integer> createEdge(int from, int to) {
        return createEdge(from, to, 1);
    }
}
