package org.teachfx.antlr4.ep21.analysis.dataflow;

import org.teachfx.antlr4.ep21.ir.IRNode;
import org.teachfx.antlr4.ep21.ir.expr.Operand;
import org.teachfx.antlr4.ep21.pass.cfg.Loc;
import org.teachfx.antlr4.ep21.pass.cfg.CFG;

import java.util.*;

/**
 * 数据流分析框架基础类
 */
public abstract class DataFlowFramework<T extends IRNode> {
    protected final CFG<T> cfg;
    protected final Map<Integer, Loc<T>> locMap;
    
    public DataFlowFramework(CFG<T> cfg) {
        this.cfg = cfg;
        this.locMap = new HashMap<>();
        initializeLocations();
    }
    
    /**
     * 初始化位置信息
     */
    private void initializeLocations() {
        for (var node : cfg.nodes) {
            node.getIRNodes().forEach(instr -> {
                locMap.put(instr.hashCode(), new Loc<>(instr));
            });
        }
    }
    
    /**
     * 执行数据流分析
     */
    public abstract void analyze();
    
    /**
     * 获取某个指令的输入集合
     */
    public Set<Operand> getLiveIn(IRNode node) {
        Loc<T> loc = locMap.get(node.hashCode());
        return loc != null ? loc.liveIn : new HashSet<Operand>();
    }
    
    /**
     * 获取某个指令的输出集合
     */
    public Set<Operand> getLiveOut(IRNode node) {
        Loc<T> loc = locMap.get(node.hashCode());
        return loc != null ? loc.liveOut : new HashSet<Operand>();
    }
    
    /**
     * 修复点方程求解
     */
    protected boolean solveWithWorklist() {
        Queue<IRNode> worklist = new LinkedList<>();
        initializeWorklist(worklist);
        
        boolean changed = false;
        while (!worklist.isEmpty()) {
            IRNode node = worklist.poll();
            if (processNode(node, worklist)) {
                changed = true;
            }
        }
        return changed;
    }
    
    /**
     * 初始化工作列表
     */
    protected void initializeWorklist(Queue<IRNode> worklist) {
        for (var node : cfg.nodes) {
            node.getIRNodes().forEach(instr -> {
                worklist.add(instr);
            });
        }
    }
    
    /**
     * 处理单个节点
     */
    protected abstract boolean processNode(IRNode node, Queue<IRNode> worklist);
    
    /**
     * 获取前驱指令列表
     */
    protected List<IRNode> getPredecessors(IRNode node) {
        // 这里需要根据实际的CFG结构实现
        return new ArrayList<>();
    }
    
    /**
     * 获取后继指令列表
     */
    protected List<IRNode> getSuccessors(IRNode node) {
        // 这里需要根据实际的CFG结构实现
        return new ArrayList<>();
    }
}