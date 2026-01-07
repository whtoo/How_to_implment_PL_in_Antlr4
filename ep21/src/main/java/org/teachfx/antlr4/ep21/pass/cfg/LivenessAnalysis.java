package org.teachfx.antlr4.ep21.pass.cfg;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.teachfx.antlr4.ep21.ir.IRNode;
import org.teachfx.antlr4.ep21.ir.IRVisitor;
import org.teachfx.antlr4.ep21.ir.Prog;
import org.teachfx.antlr4.ep21.ir.expr.CallFunc;
import org.teachfx.antlr4.ep21.ir.expr.Expr;
import org.teachfx.antlr4.ep21.ir.expr.Operand;
import org.teachfx.antlr4.ep21.ir.expr.VarSlot;
import org.teachfx.antlr4.ep21.ir.expr.addr.FrameSlot;
import org.teachfx.antlr4.ep21.ir.expr.addr.OperandSlot;
import org.teachfx.antlr4.ep21.ir.expr.arith.BinExpr;
import org.teachfx.antlr4.ep21.ir.expr.arith.UnaryExpr;
import org.teachfx.antlr4.ep21.ir.expr.val.ConstVal;
import org.teachfx.antlr4.ep21.ir.stmt.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 活性分析类 - 实现数据流分析中的活性分析算法
 * 用于计算每个基本块的活性变量集合，为寄存器分配等优化提供基础
 *
 * 实现IFlowOptimizer接口以统一CFG模块抽象。
 */
public class LivenessAnalysis implements IRVisitor<Void, Void>, IFlowOptimizer<IRNode> {
    private static final Logger logger = LogManager.getLogger(LivenessAnalysis.class);
    
    // 当前正在分析的基本块
    private BasicBlock<IRNode> currentBlock;
    
    // 用于存储所有基本块的活性分析结果
    private Map<BasicBlock<IRNode>, Set<Operand>> blockLiveInMap;
    private Map<BasicBlock<IRNode>, Set<Operand>> blockLiveOutMap;
    
    public LivenessAnalysis() {
        this.blockLiveInMap = new HashMap<>();
        this.blockLiveOutMap = new HashMap<>();
    }

    // IFlowOptimizer接口实现

    /**
     * IFlowOptimizer接口方法：对CFG执行活性分析。
     * 此方法将CFG作为优化Pass运行，并存储分析结果供后续优化使用。
     *
     * @param cfg 待分析的控制流图
     */
    @Override
    public void onHandle(CFG<IRNode> cfg) {
        analyze(cfg);
    }
    
    /**
     * 对控制流图进行活性分析的主方法
     * @param cfg 待分析的控制流图
     */
    public void analyze(@NotNull CFG<IRNode> cfg) {
        Objects.requireNonNull(cfg, "CFG cannot be null");
        
        logger.info("开始活性分析，CFG节点数: {}", cfg.nodes.size());
        
        // 第一阶段：收集每个基本块的def和use信息
        collectDefUseInfo(cfg);
        
        // 第二阶段：迭代计算in和out集合
        computeLiveInOut(cfg);
        
        logger.info("活性分析完成");
    }
    
    /**
     * 第一阶段：收集每个基本块的def和use信息
     */
    private void collectDefUseInfo(@NotNull CFG<IRNode> cfg) {
        for (BasicBlock<IRNode> block : cfg.nodes) {
            currentBlock = block;
            
            // 清空并重新计算def和use集合
            block.def.clear();
            block.liveUse.clear();
            
            // 遍历基本块中的每条指令
            for (Loc<IRNode> loc : block.codes) {
                IRNode instr = loc.instr;
                
                // 使用访问者模式处理不同类型的指令
                if (instr instanceof Assign assign) {
                    handleAssign(assign);
                } else if (instr instanceof CJMP cjmp) {
                    handleCJMP(cjmp);
                } else if (instr instanceof ReturnVal returnVal) {
                    handleReturnVal(returnVal);
                } else if (instr instanceof CallFunc callFunc) {
                    handleCallFunc(callFunc);
                }
                // Label, JMP等指令不需要特殊处理
            }
        }
        
        if (logger.isDebugEnabled()) {
            for (BasicBlock<IRNode> block : cfg.nodes) {
                logger.debug("基本块 {}: def={}, use={}",
                    block.getId(), block.def, block.liveUse);
            }
        }
    }
    
    /**
     * 处理赋值语句的def和use
     */
    private void handleAssign(@NotNull Assign assign) {
        Expr rhs = assign.getRhs();
        if (rhs instanceof VarSlot varSlot && !isConst(varSlot)) {
            currentBlock.liveUse.add(varSlot);
        }

        VarSlot lhs = assign.getLhs();
        if (!isConst(lhs)) {
            currentBlock.def.add(lhs);
        }
    }
    
    /**
     * 处理条件跳转的use
     */
    private void handleCJMP(@NotNull CJMP cjmp) {
        // 条件变量是use
        if (cjmp.cond instanceof VarSlot varSlot && !isConst(varSlot)) {
            currentBlock.liveUse.add(varSlot);
        }
    }
    
    /**
     * 处理返回语句的use
     */
    private void handleReturnVal(@NotNull ReturnVal returnVal) {
        if (returnVal.getRetVal() instanceof VarSlot varSlot && !isConst(varSlot)) {
            currentBlock.liveUse.add(varSlot);
        }
    }
    
    /**
     * 处理函数调用的use
     */
    private void handleCallFunc(@NotNull CallFunc callFunc) {
        // 这里简化处理，假设函数调用使用其参数
        // 实际实现可能需要更复杂的参数分析
    }
    
    /**
     * 第二阶段：迭代计算in和out集合
     */
    private void computeLiveInOut(@NotNull CFG<IRNode> cfg) {
        boolean changed;
        int iteration = 0;
        
        // 初始化in和out集合
        for (BasicBlock<IRNode> block : cfg.nodes) {
            block.liveIn.clear();
            block.liveOut.clear();
        }
        
        do {
            changed = false;
            iteration++;
            
            // 按逆序遍历基本块（从出口到入口）
            for (int i = cfg.nodes.size() - 1; i >= 0; i--) {
                BasicBlock<IRNode> block = cfg.nodes.get(i);
                
                // 保存旧的in集合用于比较
                Set<Operand> oldIn = new HashSet<>(block.liveIn);
                
                // 计算out集合：所有后继块的in集合的并集
                computeLiveOut(block, cfg);
                
                // 计算in集合：use ∪ (out - def)
                computeLiveIn(block);
                
                // 检查是否有变化
                if (!block.liveIn.equals(oldIn)) {
                    changed = true;
                }
            }
            
            logger.debug("活性分析迭代 {} 完成", iteration);
            
            // 防止无限循环
            if (iteration > 1000) {
                logger.error("活性分析迭代次数过多，可能存在循环依赖");
                break;
            }
            
        } while (changed);
        
        logger.info("活性分析共迭代 {} 次", iteration);
        
        // 保存结果到map中
        for (BasicBlock<IRNode> block : cfg.nodes) {
            blockLiveInMap.put(block, new HashSet<>(block.liveIn));
            blockLiveOutMap.put(block, new HashSet<>(block.liveOut));
        }
        
        if (logger.isDebugEnabled()) {
            for (BasicBlock<IRNode> block : cfg.nodes) {
                logger.debug("基本块 {}: liveIn={}, liveOut={}",
                    block.getId(), block.liveIn, block.liveOut);
            }
        }
    }
    
    /**
     * 计算基本块的out集合
     * out[b] = ∪_{s∈succ(b)} in[s]
     */
    private void computeLiveOut(@NotNull BasicBlock<IRNode> block, @NotNull CFG<IRNode> cfg) {
        block.liveOut.clear();
        
        // 获取所有后继块的in集合的并集
        Set<Integer> successors = cfg.getSucceed(block.getId());
        for (Integer succId : successors) {
            BasicBlock<IRNode> succBlock = cfg.getBlock(succId);
            if (succBlock != null) {
                block.liveOut.addAll(succBlock.liveIn);
            }
        }
    }
    
    /**
     * 计算基本块的in集合
     * in[b] = use[b] ∪ (out[b] - def[b])
     */
    private void computeLiveIn(@NotNull BasicBlock<IRNode> block) {
        block.liveIn.clear();
        
        // 先添加use集合
        block.liveIn.addAll(block.liveUse);
        
        // 添加(out - def)
        for (Operand var : block.liveOut) {
            if (!block.def.contains(var)) {
                block.liveIn.add(var);
            }
        }
    }
    
    /**
     * 判断变量是否为常量
     */
    private boolean isConst(@NotNull Operand operand) {
        return operand instanceof ConstVal<?>;
    }
    
    /**
     * 获取基本块的liveIn集合
     */
    @NotNull
    public Set<Operand> getLiveIn(@NotNull BasicBlock<IRNode> block) {
        return blockLiveInMap.getOrDefault(block, Collections.emptySet());
    }
    
    /**
     * 获取基本块的liveOut集合
     */
    @NotNull
    public Set<Operand> getLiveOut(@NotNull BasicBlock<IRNode> block) {
        return blockLiveOutMap.getOrDefault(block, Collections.emptySet());
    }
    
    /**
     * 获取所有基本块的活性分析结果
     */
    @NotNull
    public Map<BasicBlock<IRNode>, Set<Operand>> getAllLiveIn() {
        return Collections.unmodifiableMap(blockLiveInMap);
    }
    
    @NotNull
    public Map<BasicBlock<IRNode>, Set<Operand>> getAllLiveOut() {
        return Collections.unmodifiableMap(blockLiveOutMap);
    }
    
    // IRVisitor接口实现 - 用于指令遍历
    
    @Override
    public Void visit(BinExpr node) {
        // 二元表达式会使用操作数
        return null;
    }

    @Override
    public Void visit(UnaryExpr node) {
        // 一元表达式会使用操作数
        return null;
    }

    @Override
    public Void visit(CallFunc callFunc) {
        handleCallFunc(callFunc);
        return null;
    }

    @Override
    public Void visit(Label label) {
        // Label不产生use或def
        return null;
    }

    @Override
    public Void visit(JMP jmp) {
        // 无条件跳转不产生use或def
        return null;
    }

    @Override
    public Void visit(CJMP cjmp) {
        handleCJMP(cjmp);
        return null;
    }

    @Override
    public Void visit(Assign assign) {
        handleAssign(assign);
        return null;
    }

    @Override
    public Void visit(ReturnVal returnVal) {
        handleReturnVal(returnVal);
        return null;
    }

    @Override
    public Void visit(Prog prog) {
        // Prog节点在基本块级别处理
        return null;
    }

    @Override
    public Void visit(OperandSlot operandSlot) {
        // 操作数槽位可能包含变量使用
        if (operandSlot instanceof VarSlot varSlot && !isConst(varSlot)) {
            currentBlock.liveUse.add(varSlot);
        }
        return null;
    }

    @Override
    public Void visit(FrameSlot frameSlot) {
        // 帧槽位可能包含变量使用
        if (!isConst(frameSlot)) {
            currentBlock.liveUse.add(frameSlot);
        }
        return null;
    }

    @Override
    public <T> Void visit(ConstVal<T> tConstVal) {
        // 常量不产生use或def
        return null;
    }
}
