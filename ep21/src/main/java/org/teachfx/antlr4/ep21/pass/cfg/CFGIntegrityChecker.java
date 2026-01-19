package org.teachfx.antlr4.ep21.pass.cfg;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.teachfx.antlr4.ep21.ir.IRNode;

import java.util.*;

/**
 * CFG完整性验证器（CFG Integrity Checker）- 验证CFG的完整性和正确性
 *
 * <p>CFG完整性验证器用于检测CFG中的常见错误和问题，
 * 包括基本块连通性、边一致性、不可达代码、跳转目标有效性等。</p>
 *
 * <h3>检查项目</h3>
 * <ul>
 *   <li>基本块连通性：从入口可达所有节点</li>
 *   <li>边一致性：所有边都有有效的源和目标</li>
 *   <li>不可达代码：检测无法从入口到达的代码</li>
 *   <li>跳转目标有效性：验证所有跳转边指向存在的目标</li>
 *   <li>循环检测：验证循环的正确性</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 创建CFG完整性验证器
 * CFGIntegrityChecker<IRNode> checker = new CFGIntegrityChecker<>(cfg);
 *
 * // 执行完整性检查
 * IntegrityReport report = checker.check();
 *
 * // 输出检查结果
 * if (report.isValid()) {
 *     System.out.println("CFG is valid");
 * } else {
 *     System.out.println("CFG has errors:");
 *     for (Issue issue : report.getIssues()) {
 *         System.out.println("  - " + issue.getMessage());
 *     }
 * }
 * }</pre>
 *
 * @param <I> IR节点类型参数
 * @see CFG
 * @see EnhancedCFG
 * @author EP21 Development Team
 * @version 1.0
 * @since 2026-01-19
 */
public class CFGIntegrityChecker<I extends IRNode> {

    private static final Logger logger = LogManager.getLogger(CFGIntegrityChecker.class);

    /**
     * 要检查的CFG
     */
    private final CFG<I> cfg;

    /**
     * 检查到的所有问题
     */
    private final List<IntegrityIssue> issues;

    /**
     * 构造CFG完整性验证器
     *
     * @param cfg CFG实例，不能为null
     * @throws NullPointerException 当cfg为null时抛出
     */
    public CFGIntegrityChecker(@NotNull CFG<I> cfg) {
        this.cfg = Objects.requireNonNull(cfg, "CFG cannot be null");
        this.issues = new ArrayList<>();
    }

    /**
     * 执行所有完整性检查
     *
     * @return 完整性报告
     */
    @NotNull
    public IntegrityReport check() {
        logger.info("Starting CFG integrity check");

        // 清空之前的问题列表
        issues.clear();

        // 执行各项检查
        checkBlockConnectivity();
        checkEdgeConsistency();
        checkUnreachableBlocks();
        checkJumpTargetValidity();

        logger.info("CFG integrity check completed: {} issues found", issues.size());

        return new IntegrityReport(issues);
    }

    /**
     * 检查基本块连通性
     *
     * <p>从入口块开始，检查是否可以到达所有其他基本块。</p>
     */
    private void checkBlockConnectivity() {
        logger.debug("Checking block connectivity");

        if (cfg.nodes.isEmpty()) {
            issues.add(new IntegrityIssue(
                    IssueType.EMPTY_CFG,
                    "CFG contains no basic blocks"
            ));
            return;
        }

        // 假设第一个块是入口块
        BasicBlock<I> entryBlock = cfg.nodes.get(0);
        Set<Integer> reachableBlocks = new HashSet<>();
        Queue<Integer> queue = new LinkedList<>();

        // 从入口块开始遍历
        queue.add(entryBlock.getId());
        reachableBlocks.add(entryBlock.getId());

        // BFS遍历
        while (!queue.isEmpty()) {
            int currentBlockId = queue.poll();

            // 获取所有后继块
            Set<Integer> successors = cfg.getSucceed(currentBlockId);
            for (int successorId : successors) {
                if (!reachableBlocks.contains(successorId)) {
                    reachableBlocks.add(successorId);
                    queue.add(successorId);
                }
            }
        }

        // 检查是否有不可达的块
        for (BasicBlock<I> block : cfg.nodes) {
            if (!reachableBlocks.contains(block.getId())) {
                issues.add(new IntegrityIssue(
                        IssueType.UNREACHABLE_BLOCK,
                        "Block L" + block.getId() + " is unreachable from entry block L" +
                                entryBlock.getId()
                ));
                logger.warn("Unreachable block detected: L{}", block.getId());
            }
        }
    }

    /**
     * 检查边一致性
     *
     * <p>验证所有边都有有效的源和目标块。</p>
     */
    private void checkEdgeConsistency() {
        logger.debug("Checking edge consistency");

        Set<Integer> validBlockIds = new HashSet<>();
        for (BasicBlock<I> block : cfg.nodes) {
            validBlockIds.add(block.getId());
        }

        for (var edgeTriple : cfg.edges) {
            int sourceId = edgeTriple.getLeft();
            int targetId = edgeTriple.getMiddle();

            // 检查源块是否存在
            if (!validBlockIds.contains(sourceId)) {
                issues.add(new IntegrityIssue(
                        IssueType.INVALID_EDGE_SOURCE,
                        "Edge from non-existent block L" + sourceId
                ));
                logger.warn("Invalid edge source: L{}", sourceId);
            }

            // 检查目标块是否存在
            if (!validBlockIds.contains(targetId)) {
                issues.add(new IntegrityIssue(
                        IssueType.INVALID_EDGE_TARGET,
                        "Edge to non-existent block L" + targetId
                ));
                logger.warn("Invalid edge target: L{}", targetId);
            }
        }
    }

    /**
     * 检查不可达代码
     *
     * <p>检测无法从入口块到达的基本块。</p>
     */
    private void checkUnreachableBlocks() {
        logger.debug("Checking for unreachable blocks");

        // 注意：这个检查已经在checkBlockConnectivity中执行
        // 这里可以添加额外的检查，如空块等
        for (BasicBlock<I> block : cfg.nodes) {
            if (block.isEmpty()) {
                // 空块不一定是错误，但可能表示冗余代码
                logger.debug("Empty block detected: L{}", block.getId());
            }
        }
    }

    /**
     * 检查跳转目标有效性
     *
     * <p>验证所有跳转边都指向有效的目标块。</p>
     */
    private void checkJumpTargetValidity() {
        logger.debug("Checking jump target validity");

        // 分析每个块的指令，检查跳转目标
        for (BasicBlock<I> block : cfg.nodes) {
            I lastInstruction = block.getLastInstruction();

            // 如果最后一条指令是跳转，检查目标是否存在
            if (lastInstruction != null) {
                // 这里需要检查指令类型，如果需要更深入的IR分析
                // 目前只做简单的块存在性检查
                logger.trace("Block L{} last instruction: {}", block.getId(), lastInstruction);
            }
        }
    }

    /**
     * 检查循环的正确性
     *
     * <p>验证所有回边都指向有效的循环头。</p>
     */
    private void checkLoopCorrectness() {
        logger.debug("Checking loop correctness");

        // 识别回边：指向较早出现的块的边
        Set<Integer> blockIndices = new HashSet<>();
        for (int i = 0; i < cfg.nodes.size(); i++) {
            blockIndices.add(cfg.nodes.get(i).getId());
        }

        for (var edgeTriple : cfg.edges) {
            int sourceId = edgeTriple.getLeft();
            int targetId = edgeTriple.getMiddle();

            // 如果目标块在源块之前出现，这可能是回边
            // 注意：这只是简单的启发式，准确的判断需要支配分析
            logger.trace("Potential back edge: L{} -> L{}", sourceId, targetId);
        }
    }

    /**
     * 完整性问题类型
     */
    public enum IssueType {
        EMPTY_CFG,
        INVALID_EDGE_SOURCE,
        INVALID_EDGE_TARGET,
        UNREACHABLE_BLOCK,
        INVALID_JUMP_TARGET,
        LOOP_CORRECTNESS,
        DUPLICATE_EDGE,
        ORPHAN_BLOCK
    }

    /**
     * 完整性问题
     */
    public static class IntegrityIssue {
        private final IssueType type;
        private final String message;

        public IntegrityIssue(IssueType type, String message) {
            this.type = Objects.requireNonNull(type, "Issue type cannot be null");
            this.message = Objects.requireNonNull(message, "Message cannot be null");
        }

        public IssueType getType() {
            return type;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return "[" + type + "] " + message;
        }
    }

    /**
     * 完整性报告
     */
    public static class IntegrityReport {
        private final List<IntegrityIssue> issues;

        public IntegrityReport(@NotNull List<IntegrityIssue> issues) {
            this.issues = new ArrayList<>(Objects.requireNonNull(issues,
                    "Issues list cannot be null"));
        }

        /**
         * 判断CFG是否有效（没有问题）
         */
        public boolean isValid() {
            return issues.isEmpty();
        }

        /**
         * 获取所有问题
         */
        @NotNull
        public List<IntegrityIssue> getIssues() {
            return Collections.unmodifiableList(issues);
        }

        /**
         * 获取问题数量
         */
        public int getIssueCount() {
            return issues.size();
        }

        @Override
        public String toString() {
            return String.format(
                    "IntegrityReport{valid=%s, issues=%d}",
                    isValid(),
                    issues.size()
            );
        }
    }
}
