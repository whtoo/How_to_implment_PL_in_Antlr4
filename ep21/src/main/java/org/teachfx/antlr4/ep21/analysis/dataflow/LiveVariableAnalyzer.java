package org.teachfx.antlr4.ep21.analysis.dataflow;

import org.teachfx.antlr4.ep21.ir.IRNode;
import org.teachfx.antlr4.ep21.ir.expr.Operand;
import org.teachfx.antlr4.ep21.ir.stmt.Assign;
import org.teachfx.antlr4.ep21.ir.expr.CallFunc;
import org.teachfx.antlr4.ep21.pass.cfg.BasicBlock;
import org.teachfx.antlr4.ep21.pass.cfg.CFG;
import org.teachfx.antlr4.ep21.pass.cfg.Loc;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 活跃变量分析器
 * 基于Loc类实现的数据流分析，用于确定变量在程序点的活跃性
 */
public class LiveVariableAnalyzer {
    private final CFG<IRNode> cfg;
    
    public LiveVariableAnalyzer(CFG<IRNode> cfg) {
        this.cfg = cfg;
    }
    
    /**
     * 执行活跃变量分析
     */
    public void analyze() {
        boolean changed = true;
        
        // 迭代直到收敛
        while (changed) {
            changed = false;
            
            // 逆序遍历基本块（从后往前）
            List<BasicBlock<IRNode>> reversedBlocks = new ArrayList<>(cfg.nodes);
            Collections.reverse(reversedBlocks);
            
            for (BasicBlock<IRNode> block : reversedBlocks) {
                if (analyzeBasicBlock(block)) {
                    changed = true;
                }
            }
        }
    }
    
    /**
     * 分析单个基本块
     */
    private boolean analyzeBasicBlock(BasicBlock<IRNode> block) {
        boolean changed = false;
        
        // 逆序遍历基本块中的指令
        List<Loc<IRNode>> instructions = new ArrayList<>(block.codes);
        Collections.reverse(instructions);
        
        Set<Operand> liveOut = new HashSet<>(block.liveOut);
        
        for (Loc<IRNode> loc : instructions) {
            IRNode instr = loc.getInstruction();
            
            // 计算gen和kill集合
            Set<Operand> gen = computeGen(instr);
            Set<Operand> kill = computeKill(instr, block.def);
            
            // 活跃变量方程: liveIn = gen ∪ (liveOut - kill)
            Set<Operand> newLiveIn = new HashSet<>(gen);
            newLiveIn.addAll(liveOut);
            newLiveIn.removeAll(kill);
            
            // 更新liveOut为下一个指令的liveIn
            liveOut = newLiveIn;
            
            // 检查是否有变化
            if (!loc.liveIn.equals(newLiveIn)) {
                loc.liveIn = newLiveIn;
                changed = true;
            }
        }
        
        // 更新基本块的liveIn为最后一个指令的liveIn
        if (!instructions.isEmpty()) {
            Set<Operand> finalLiveIn = instructions.get(0).liveIn;
            if (!block.liveIn.equals(finalLiveIn)) {
                block.liveIn = new HashSet<>(finalLiveIn);
                changed = true;
            }
        }
        
        // 计算基本块的liveOut = 后继基本块的liveIn的并集
        Set<Operand> newLiveOut = new HashSet<>();
        for (Integer successorId : cfg.getSucceed(block.getId())) {
            BasicBlock<IRNode> successor = cfg.getBlock(successorId);
            newLiveOut.addAll(successor.liveIn);
        }
        
        if (!block.liveOut.equals(newLiveOut)) {
            block.liveOut = new HashSet<>(newLiveOut);
            changed = true;
        }
        
        return changed;
    }
    
    /**
     * 计算gen集合（使用但不定义的变量）
     */
    private Set<Operand> computeGen(IRNode instr) {
        Set<Operand> gen = new HashSet<>();
        
        if (instr instanceof Assign assign) {
            // 右边表达式的使用变量
            gen.addAll(findUsedOperands(assign.getRhs()));
        } else if (instr instanceof CallFunc call) {
            // 函数调用的参数 - 这里暂时不处理参数具体内容
            // CallFunc只有参数数量，没有具体的参数列表
        }
        
        return gen;
    }
    
    /**
     * 计算kill集合（定义但不使用的变量）
     */
    private Set<Operand> computeKill(IRNode instr, Set<Operand> blockDef) {
        Set<Operand> kill = new HashSet<>();
        
        if (instr instanceof Assign assign) {
            // 左边的目标变量
            kill.add(assign.getLhs());
        }
        
        return kill;
    }
    
    /**
     * 查找表达式中使用的操作数
     */
    private Set<Operand> findUsedOperands(Operand operand) {
        Set<Operand> used = new HashSet<>();
        used.add(operand);
        return used;
    }
    
    /**
     * 获取变量在某个指令处的活跃信息
     */
    public String getLiveVariableInfo(IRNode instr) {
        StringBuilder info = new StringBuilder();
        
        for (BasicBlock<IRNode> block : cfg.nodes) {
            for (Loc<IRNode> loc : block.codes) {
                if (loc.getInstruction() == instr) {
                    info.append("指令: ").append(instr.toString()).append("\n");
                    info.append("LiveIn: ").append(formatOperandSet(loc.liveIn)).append("\n");
                    info.append("LiveOut: ").append(formatOperandSet(loc.liveOut)).append("\n");
                    break;
                }
            }
        }
        
        return info.toString();
    }
    
    /**
     * 格式化操作数集合为字符串
     */
    private String formatOperandSet(Set<Operand> operands) {
        return operands.stream()
                .map(Operand::toString)
                .collect(Collectors.joining(", ", "{", "}"));
    }
    
    /**
     * 打印活跃变量分析结果
     */
    public void printAnalysisResult() {
        System.out.println("=== 活跃变量分析结果 ===");
        
        for (BasicBlock<IRNode> block : cfg.nodes) {
            System.out.println("基本块 " + block.getId() + ":");
            System.out.println("  LiveIn: " + formatOperandSet(block.liveIn));
            System.out.println("  LiveOut: " + formatOperandSet(block.liveOut));
            System.out.println("  Def: " + formatOperandSet(block.def));
            System.out.println("  LiveUse: " + formatOperandSet(block.liveUse));
            
            System.out.println("  指令序列:");
            for (Loc<IRNode> loc : block.codes) {
                System.out.println("    " + loc.getInstruction().toString() +
                                 " [LiveIn: " + formatOperandSet(loc.liveIn) +
                                 ", LiveOut: " + formatOperandSet(loc.liveOut) + "]");
            }
            System.out.println();
        }
    }
}