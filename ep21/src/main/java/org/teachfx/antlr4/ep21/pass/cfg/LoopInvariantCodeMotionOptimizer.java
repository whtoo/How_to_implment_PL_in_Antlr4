package org.teachfx.antlr4.ep21.pass.cfg;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.teachfx.antlr4.ep21.analysis.dataflow.LoopAnalysis;
import org.teachfx.antlr4.ep21.analysis.dataflow.NaturalLoop;
import org.teachfx.antlr4.ep21.ir.IRNode;
import org.teachfx.antlr4.ep21.ir.expr.Operand;
import org.teachfx.antlr4.ep21.ir.expr.VarSlot;
import org.teachfx.antlr4.ep21.ir.expr.val.ConstVal;
import org.teachfx.antlr4.ep21.ir.expr.arith.BinExpr;
import org.teachfx.antlr4.ep21.ir.expr.arith.UnaryExpr;
import org.teachfx.antlr4.ep21.ir.stmt.Assign;
import org.teachfx.antlr4.ep21.ir.stmt.Label;
import org.teachfx.antlr4.ep21.pass.cfg.Loc;

import java.util.*;

/**
 * 循环不变代码外提优化器 (Loop Invariant Code Motion Optimizer)
 *
 * 将循环内不随迭代变化的表达式外提到循环外，减少重复计算。
 *
 * 实现策略：
 * 1. 使用LoopAnalysis识别自然循环
 * 2. 从内层到外层处理每个循环
 * 3. 识别循环不变表达式：
 *    - 所有操作数都是常量
 *    - 或所有操作数都是循环外定义的变量
 * 4. 将不变表达式外提到循环头之前的预头部块
 *
 * 示例：
 * 原始代码:
 *   while (i < n) {
 *     x = a + b  <- 循环不变，a和b在循环外定义
 *     use(x)
 *   }
 *
 * 优化后:
 *   x = a + b  <- 外提到循环外
 *   while (i < n) {
 *     use(x)
 *   }
 *
 * @author EP21 Team
 * @version 1.0
 */
public class LoopInvariantCodeMotionOptimizer implements IFlowOptimizer<IRNode> {

    private static final Logger logger = LogManager.getLogger(LoopInvariantCodeMotionOptimizer.class);

    private int movedInstructions = 0;
    private int processedLoops = 0;

    @Override
    public void onHandle(CFG<IRNode> cfg) {
        logger.info("开始循环不变代码外提优化...");

        movedInstructions = 0;
        processedLoops = 0;

        LoopAnalysis<IRNode> loopAnalysis = new LoopAnalysis<>();
        loopAnalysis.analyze(cfg);

        List<NaturalLoop<IRNode>> loops = loopAnalysis.getLoops();

        logger.debug("识别到 {} 个循环", loops.size());

        for (NaturalLoop<IRNode> loop : loops) {
            optimizeLoop(loop, cfg);
        }

        logger.info("循环不变代码外提完成: 处理了 {} 个循环, 外提了 {} 条指令",
                    processedLoops, movedInstructions);
    }

    private void optimizeLoop(NaturalLoop<IRNode> loop, CFG<IRNode> cfg) {
        Set<Integer> loopNodes = loop.getLoopNodes();
        Set<VarSlot> loopDefinedVars = collectLoopDefinedVars(loop, cfg);
        List<Loc<IRNode>> toHoist = new ArrayList<>();

        for (int nodeId : loopNodes) {
            BasicBlock<IRNode> block = cfg.getBlock(nodeId);
            if (block == null) continue;

            for (Loc<IRNode> loc : block.codes) {
                IRNode instr = loc.getInstruction();

                if (isLoopInvariant(instr, loopDefinedVars, loop, cfg)) {
                    toHoist.add(loc);
                    logger.debug("检测到循环不变表达式: {}", instr);
                }
            }
        }

        if (!toHoist.isEmpty()) {
            hoistInstructions(toHoist, loop, cfg);
            movedInstructions += toHoist.size();
        }

        processedLoops++;
    }

    private Set<VarSlot> collectLoopDefinedVars(NaturalLoop<IRNode> loop, CFG<IRNode> cfg) {
        Set<VarSlot> definedVars = new HashSet<>();

        for (int nodeId : loop.getLoopNodes()) {
            BasicBlock<IRNode> block = cfg.getBlock(nodeId);
            if (block == null) continue;

            for (Loc<IRNode> loc : block.codes) {
                IRNode instr = loc.getInstruction();
                if (instr instanceof Assign assign) {
                    definedVars.add(assign.getLhs());
                }
            }
        }

        return definedVars;
    }

    private boolean isLoopInvariant(IRNode instr, Set<VarSlot> loopDefinedVars,
                                   NaturalLoop<IRNode> loop, CFG<IRNode> cfg) {
        if (instr instanceof Assign assign) {
            return isExprLoopInvariant(assign.getRhs(), loopDefinedVars, loop, cfg);
        } else if (instr instanceof BinExpr binExpr) {
            return isExprLoopInvariant(binExpr, loopDefinedVars, loop, cfg);
        } else if (instr instanceof UnaryExpr unaryExpr) {
            return isExprLoopInvariant(unaryExpr, loopDefinedVars, loop, cfg);
        }

        return false;
    }

    private boolean isExprLoopInvariant(Object expr, Set<VarSlot> loopDefinedVars,
                                       NaturalLoop<IRNode> loop, CFG<IRNode> cfg) {
        if (expr instanceof ConstVal<?>) {
            return true;
        } else if (expr instanceof VarSlot varSlot) {
            return !loopDefinedVars.contains(varSlot);
        } else if (expr instanceof BinExpr binExpr) {
            return isExprLoopInvariant(binExpr.getLhs(), loopDefinedVars, loop, cfg) &&
                   isExprLoopInvariant(binExpr.getRhs(), loopDefinedVars, loop, cfg);
        } else if (expr instanceof UnaryExpr unaryExpr) {
            return isExprLoopInvariant(unaryExpr.expr, loopDefinedVars, loop, cfg);
        } else if (expr instanceof Operand operand) {
            return isExprLoopInvariant(operand, loopDefinedVars, loop, cfg);
        }

        return false;
    }

    private void hoistInstructions(List<Loc<IRNode>> toHoist,
                                 NaturalLoop<IRNode> loop,
                                 CFG<IRNode> cfg) {
        BasicBlock<IRNode> header = loop.getHeader();
        Set<Integer> predecessors = cfg.getFrontier(header.getId());

        if (predecessors.isEmpty()) {
            logger.debug("循环头没有前驱，无法创建预头部块");
            return;
        }

        int predId = predecessors.iterator().next();
        BasicBlock<IRNode> preheader = cfg.getBlock(predId);

        for (Loc<IRNode> loc : toHoist) {
            BasicBlock<IRNode> sourceBlock = findSourceBlock(loc, cfg);
            if (sourceBlock != null) {
                sourceBlock.codes.remove(loc);
                preheader.codes.add(loc);
                logger.debug("外提指令 {} 到预头部块 L{}", loc.getInstruction(), preheader.getId());
            }
        }
    }

    private BasicBlock<IRNode> findSourceBlock(Loc<IRNode> loc, CFG<IRNode> cfg) {
        for (BasicBlock<IRNode> block : cfg) {
            if (block.codes.contains(loc)) {
                return block;
            }
        }
        return null;
    }

    public int getMovedInstructionsCount() {
        return movedInstructions;
    }

    public int getProcessedLoopsCount() {
        return processedLoops;
    }
}
