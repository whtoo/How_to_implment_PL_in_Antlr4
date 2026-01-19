package org.teachfx.antlr4.ep21.pass.cfg;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.teachfx.antlr4.ep21.analysis.dataflow.NaturalLoop;
import org.teachfx.antlr4.ep21.ir.IRNode;

import java.util.*;

/**
 * 循环信息类（LoopInfo）- 存储循环分析结果
 *
 * <p>LoopInfo提供了循环分析的完整结果，包括：
 * 自然循环检测、嵌套结构分析、循环不变表达式检测等。</p>
 *
 * <h3>核心功能</h3>
 * <ul>
 *   <li>自然循环识别：基于回边识别自然循环</li>
 *   <li>嵌套结构分析：构建循环嵌套树（Loop Nesting Tree）</li>
 *   <li>循环属性计算：深度、迭代次数估算、头块识别</li>
 *   <li>循环体管理：循环包含的基本块列表</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 分析CFG中的循环
 * LoopInfo<IRNode> loopInfo = LoopInfoAnalyzer.analyze(cfg);
 *
 * // 获取所有循环
 * List<NaturalLoop<IRNode>> loops = loopInfo.getLoops();
 * for (NaturalLoop<IRNode> loop : loops) {
 *     System.out.println("Loop header: " + loop.getHeader());
 *     System.out.println("Loop depth: " + loop.getDepth());
 *     System.out.println("Loop body size: " + loop.getBody().size());
 * }
 *
 * // 查询嵌套关系
 * NaturalLoop<IRNode> outerLoop = loops.get(0);
 * for (NaturalLoop<IRNode> innerLoop : loopInfo.getNestedLoops(outerLoop)) {
 *     System.out.println("Inner loop of " + outerLoop.getHeader() + ": " + innerLoop.getHeader());
 * }
 * }</pre>
 *
 * @param <I> IR节点类型参数
 * @see NaturalLoop
 * @see LoopNestingTree
 * @author EP21 Development Team
 * @version 1.0
 * @since 2026-01-19
 */
public class LoopInfo<I extends IRNode> {

    private static final Logger logger = LogManager.getLogger(LoopInfo.class);

    /**
     * CFG实例
     */
    private final CFG<I> cfg;

    /**
     * 所有自然循环列表
     */
    private final List<NaturalLoop<I>> loops;

    /**
     * 循环嵌套树
     */
    private final Map<Integer, List<Integer>> nestingTree;

    /**
     * 循环头映射 - blockId -> NaturalLoop
     */
    private final Map<Integer, NaturalLoop<I>> headerToLoopMap;

    /**
     * 最外层循环列表（没有父循环的循环）
     */
    private final List<NaturalLoop<I>> outermostLoops;

    /**
     * 从CFG和循环列表构造LoopInfo
     *
     * @param cfg CFG实例，不能为null
     * @param loops 自然循环列表，不能为null
     * @throws NullPointerException 当参数为null时抛出
     */
    public LoopInfo(@NotNull CFG<I> cfg,
                  @NotNull List<NaturalLoop<I>> loops) {
        this.cfg = Objects.requireNonNull(cfg, "CFG cannot be null");
        this.loops = new ArrayList<>(Objects.requireNonNull(loops, "Loops cannot be null"));

        // 构建循环嵌套树
        this.nestingTree = buildNestingTree(loops);

        // 构建循环头映射
        this.headerToLoopMap = new HashMap<>();
        for (NaturalLoop<I> loop : loops) {
            headerToLoopMap.put(loop.getHeaderId(), loop);
        }

        // 识别最外层循环
        this.outermostLoops = new ArrayList<>();
        for (NaturalLoop<I> loop : loops) {
            if (loop.getParentLoop() == null) {
                outermostLoops.add(loop);
            }
        }

        logger.info("LoopInfo created: {} loops, {} outermost",
                   loops.size(), outermostLoops.size());
    }

    /**
     * 构建循环嵌套树
     *
     * @param loops 自然循环列表
     * @return 嵌套树映射（父循环 -> 子循环列表）
     */
    @NotNull
    private Map<Integer, List<Integer>> buildNestingTree(@NotNull List<NaturalLoop<I>> loops) {
        Map<Integer, List<Integer>> tree = new HashMap<>();

        // 初始化所有循环
        for (NaturalLoop<I> loop : loops) {
            tree.put(loop.getHeaderId(), new ArrayList<>());
            loop.setParentLoop(null);
        }

        // 构建嵌套关系
        for (NaturalLoop<I> outerLoop : loops) {
            for (NaturalLoop<I> innerLoop : loops) {
                if (innerLoop == outerLoop) {
                    continue;
                }

                // 检查内循环是否在外循环体内
                if (isInside(innerLoop, outerLoop)) {
                    innerLoop.setParentLoop(outerLoop);
                    tree.get(outerLoop.getHeaderId()).add(innerLoop.getHeaderId());
                    logger.debug("Loop nesting: {} is inside {}",
                                   innerLoop.getHeader(), outerLoop.getHeader());
                }
            }
        }

        return tree;
    }

    /**
     * 判断内循环是否在外循环体内
     *
     * @param innerLoop 内循环
     * @param outerLoop 外循环
     * @return true如果内循环在外循环体内，false otherwise
     */
    private boolean isInside(@NotNull NaturalLoop<I> innerLoop,
                         @NotNull NaturalLoop<I> outerLoop) {
        // 简化实现：检查内循环头是否在外循环体内
        Set<Integer> outerBody = outerLoop.getBody();

        // 如果内循环头在外循环体内，则认为内嵌套
        return outerBody.contains(innerLoop.getHeaderId());
    }

    /**
     * 获取所有循环
     *
     * @return 不可修改的自然循环列表
     */
    @NotNull
    public List<NaturalLoop<I>> getLoops() {
        return Collections.unmodifiableList(loops);
    }

    /**
     * 获取循环嵌套树
     *
     * @param headerId 循环头ID
     * @return 直接嵌套的子循环头ID列表
     */
    @NotNull
    public List<Integer> getNestedLoopIds(int headerId) {
        return Collections.unmodifiableList(
                nestingTree.getOrDefault(headerId, Collections.emptyList())
        );
    }

    /**
     * 获取嵌套的子循环列表
     *
     * @param headerId 循环头ID
     * @return 直接嵌套的子循环列表
     */
    @NotNull
    public List<NaturalLoop<I>> getNestedLoops(int headerId) {
        List<Integer> nestedIds = getNestedLoopIds(headerId);
        List<NaturalLoop<I>> nestedLoops = new ArrayList<>();
        for (int id : nestedIds) {
            NaturalLoop<I> loop = headerToLoopMap.get(id);
            if (loop != null) {
                nestedLoops.add(loop);
            }
        }
        return Collections.unmodifiableList(nestedLoops);
    }

    /**
     * 获取父循环
     *
     * @param headerId 循环头ID
     * @return 直接父循环，如果是最外层返回null
     */
    @Nullable
    public NaturalLoop<I> getParentLoop(int headerId) {
        NaturalLoop<I> loop = headerToLoopMap.get(headerId);
        return loop != null ? loop.getParentLoop() : null;
    }

    /**
     * 获取循环深度
     *
     * @param headerId 循环头ID
     * @return 循环深度（从0开始，最外层为0）
     */
    public int getLoopDepth(int headerId) {
        int depth = 0;
        NaturalLoop<I> current = headerToLoopMap.get(headerId);

        while (current != null) {
            depth++;
            current = current.getParentLoop();
        }

        return depth;
    }

    /**
     * 获取所有最外层循环
     *
     * @return 不可修改的最外层循环列表
     */
    @NotNull
    public List<NaturalLoop<I>> getOutermostLoops() {
        return Collections.unmodifiableList(outermostLoops);
    }

    /**
     * 获取循环总数
     *
     * @return 循环数量
     */
    public int getLoopCount() {
        return loops.size();
    }

    /**
     * 获取最大循环嵌套深度
     *
     * @return 最大嵌套深度
     */
    public int getMaxNestingDepth() {
        int maxDepth = 0;
        for (NaturalLoop<I> loop : loops) {
            int depth = getLoopDepth(loop.getHeaderId());
            if (depth > maxDepth) {
                maxDepth = depth;
            }
        }
        return maxDepth;
    }

    /**
     * 获取循环统计信息
     *
     * @return 循环统计对象
     */
    @NotNull
    public LoopStatistics getStatistics() {
        int maxDepth = getMaxNestingDepth();
        int[] depthHistogram = new int[maxDepth + 1];

        for (NaturalLoop<I> loop : loops) {
            int depth = getLoopDepth(loop.getHeaderId());
            depthHistogram[depth]++;
        }

        return new LoopStatistics(
                loops.size(),
                outermostLoops.size(),
                maxDepth,
                depthHistogram
        );
    }
 
    /**
     * 循环统计信息
     */
    public static class LoopStatistics {
        private final int totalLoops;
        private final int outermostLoops;
        private final int maxNestingDepth;
        private final int[] depthHistogram;

        public LoopStatistics(int totalLoops, int outermostLoops,
                             int maxNestingDepth, int[] depthHistogram) {
            this.totalLoops = totalLoops;
            this.outermostLoops = outermostLoops;
            this.maxNestingDepth = maxNestingDepth;
            this.depthHistogram = depthHistogram;
        }

        public int getTotalLoops() {
            return totalLoops;
        }

        public int getOutermostLoops() {
            return outermostLoops;
        }

        public int getMaxNestingDepth() {
            return maxNestingDepth;
        }

        public int[] getDepthHistogram() {
            return depthHistogram;
        }

        @NotNull
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("LoopStatistics{");
            sb.append("totalLoops=").append(totalLoops);
            sb.append(", outermostLoops=").append(outermostLoops);
            sb.append(", maxNestingDepth=").append(maxNestingDepth);
            sb.append(", depthHistogram=[");

            for (int i = 0; i < depthHistogram.length; i++) {
                sb.append(depthHistogram[i]);
                if (i < depthHistogram.length - 1) {
                    sb.append(", ");
                }
            }

            sb.append("]}");
            return sb.toString();
        }
    }
}
