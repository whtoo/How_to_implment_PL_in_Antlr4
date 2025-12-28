package org.teachfx.antlr4.ep21.pass.cfg;

import org.teachfx.antlr4.ep21.ir.IRNode;

import java.util.Map;

/**
 * 控制流图构建器接口。
 *
 * 抽象不一致问题修复：
 * - 原来 CFGBuilder 是具体类，无接口约束
 * - 现在通过 ICFGBuilder 接口统一抽象
 *
 * @since 2025-12-28
 */
public interface ICFGBuilder {

    /**
     * 从起始线性IR块构建控制流图。
     * 实现类应通过此方法创建CFG实例。
     *
     * @param startBlock 起始基本块 (必须非空)
     * @return 构建完成的CFG实例
     * @throws NullPointerException if startBlock is null
     * @throws IllegalArgumentException if startBlock has invalid state
     */
    CFG<IRNode> buildFrom(LinearIRBlock startBlock);

    /**
     * 获取构建统计信息，用于调试和监控。
     *
     * @return 包含CFG统计信息的不可变Map
     */
    Map<String, Object> getStatistics();

    /**
     * 验证构建的CFG的一致性和正确性。
     *
     * @return 如果CFG有效返回true，否则返回false
     */
    boolean validateCFG();

    /**
     * 获取构建的控制流图。
     *
     * @return CFG实例 (永远不为空)
     */
    CFG<IRNode> getCFG();
}
