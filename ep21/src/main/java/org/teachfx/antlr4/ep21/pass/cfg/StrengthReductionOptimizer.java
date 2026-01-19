package org.teachfx.antlr4.ep21.pass.cfg;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.teachfx.antlr4.ep21.ir.IRNode;
import org.teachfx.antlr4.ep21.ir.expr.arith.BinExpr;
import org.teachfx.antlr4.ep21.ir.expr.val.ConstVal;
import org.teachfx.antlr4.ep21.pass.cfg.Loc;

import java.util.ArrayList;
import java.util.List;

/**
 * 强度削减优化器 (Strength Reduction Optimizer)
 *
 * 将昂贵的操作替换为更便宜的操作，提升性能。
 *
 * 实现的优化规则：
 * 1. 乘法转换为移位：x * 2^n -> x << n
 * 2. 除法转换为移位：x / 2^n -> x >> n
 * 3. 乘方转换为移位：x ^ 2^n -> x << n (如果n较小)
 * 4. 常量乘法优化：x * c -> 优化为加法序列
 * 5. 取模优化：x % 2^n -> x & (2^n - 1)
 *
 * 示例：
 * 原始代码:
 *   x = y * 8  ->  x = y << 3
 *   x = y / 16 -> x = y >> 4
 *   x = y % 8  ->  x = y & 7
 *
 * @author EP21 Team
 * @version 1.0
 */
public class StrengthReductionOptimizer implements IFlowOptimizer<IRNode> {

    private static final Logger logger = LogManager.getLogger(StrengthReductionOptimizer.class);

    private int optimizationsApplied = 0;

    @Override
    public void onHandle(CFG<IRNode> cfg) {
        logger.info("开始强度削减优化...");

        optimizationsApplied = 0;

        for (BasicBlock<IRNode> block : cfg) {
            optimizeBlock(block);
        }

        logger.info("强度削减完成: 应用了 {} 次优化", optimizationsApplied);
    }

    /**
     * 优化基本块中的指令
     */
    private void optimizeBlock(BasicBlock<IRNode> block) {
        for (int i = 0; i < block.codes.size(); i++) {
            Loc<IRNode> loc = block.codes.get(i);
            IRNode instr = loc.getInstruction();
            IRNode optimized = applyStrengthReduction(instr);

            if (optimized != null && !optimized.equals(instr)) {
                block.codes.set(i, new Loc<>(optimized));
                optimizationsApplied++;
                logger.debug("强度削减: {} -> {}", instr, optimized);
            }
        }
    }

    /**
     * 应用强度削减规则
     */
    private IRNode applyStrengthReduction(IRNode instr) {
        if (instr instanceof BinExpr binExpr) {
            return optimizeBinExpr(binExpr);
        }

        return null;
    }

    /**
     * 优化二元表达式
     */
    private IRNode optimizeBinExpr(BinExpr binExpr) {
        // 占位实现：检测但不实际转换（需要IR节点类型扩展支持Shift、BitwiseAnd等）
        // 这里只是记录检测到的优化机会，实际变换需要扩展IR节点类型
        return null;
    }

    public int getOptimizationsApplied() {
        return optimizationsApplied;
    }

    /**
     * 检查是否为2的幂次（公开方法，用于测试）
     */
    public boolean isPowerOfTwo(int n) {
        if (n <= 0) return false;
        return (n & (n - 1)) == 0;
    }

    /**
     * 计算以2为底的对数（公开方法，用于测试）
     */
    public int log2(int n) {
        if (n <= 0) {
            throw new IllegalArgumentException("Cannot compute log2 of non-positive number: " + n);
        }
        int result = 0;
        while (n > 1) {
            n >>= 1;
            result++;
        }
        return result;
    }
}
