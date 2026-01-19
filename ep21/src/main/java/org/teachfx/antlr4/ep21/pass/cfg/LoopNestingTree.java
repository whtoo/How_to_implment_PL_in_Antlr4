package org.teachfx.antlr4.ep21.pass.cfg;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.teachfx.antlr4.ep21.analysis.dataflow.NaturalLoop;
import org.teachfx.antlr4.ep21.ir.IRNode;

import java.util.*;

/**
 * 循环嵌套树（Loop Nesting Tree）- 表示循环之间的嵌套关系
 *
 * <p>循环嵌套树是一个树形结构，其中每个节点代表一个循环，
 * 父子关系表示循环的嵌套层次。</p>
 *
 * <h3>嵌套关系</h3>
 * <ul>
 *   <li>根节点：最外层的循环（没有父循环）</li>
 *   <li>内部节点：嵌套在其他循环内部的循环</li>
 *   <li>叶子节点：不包含任何子循环的循环</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 创建循环嵌套树
 * LoopNestingTree<IRNode> tree = LoopNestingTree.fromLoopInfo(loopInfo);
 *
 * // 获取最外层循环
 * List<NaturalLoop<IRNode>> roots = tree.getRootLoops();
 *
 * // 遍历所有循环（深度优先）
 * tree.traverseDepthFirst(loop -> {
 *     System.out.println("Loop: " + loop.getHeader() + ", depth: " + loop.getDepth());
 * });
 *
 * // 获取循环的所有子循环
 * for (NaturalLoop<IRNode> child : tree.getChildLoops(loop)) {
 *     System.out.println("Child of " + loop.getHeader() + ": " + child.getHeader());
 * }
 * }</pre>
 *
 * @param <I> IR节点类型参数
 * @see LoopInfo
 * @see NaturalLoop
 * @author EP21 Development Team
 * @version 1.0
 * @since 2026-01-19
 */
public class LoopNestingTree<I extends IRNode> {

    private static final Logger logger = LogManager.getLogger(LoopNestingTree.class);

    /**
     * 循环信息
     */
    private final LoopInfo<I> loopInfo;

    /**
     * 父循环映射 - childId -> parentLoop
     */
    private final Map<Integer, NaturalLoop<I>> parentMap;

    /**
     * 子循环映射 - parentId -> List<childLoop>
     */
    private final Map<Integer, List<NaturalLoop<I>>> childrenMap;

    /**
     * 根循环列表（最外层循环）
     */
    private final List<NaturalLoop<I>> roots;

    /**
     * 从LoopInfo构造循环嵌套树
     *
     * @param loopInfo 循环信息，不能为null
     * @throws NullPointerException 当loopInfo为null时抛出
     */
    public LoopNestingTree(@NotNull LoopInfo<I> loopInfo) {
        this.loopInfo = Objects.requireNonNull(loopInfo, "LoopInfo cannot be null");

        // 构建父-子关系
        this.parentMap = new HashMap<>();
        this.childrenMap = new HashMap<>();

        // 初始化所有循环
        for (NaturalLoop<I> loop : loopInfo.getLoops()) {
            parentMap.put(loop.getHeaderId(), null);
            childrenMap.put(loop.getHeaderId(), new ArrayList<>());
        }

        // 构建嵌套关系
        for (NaturalLoop<I> outerLoop : loopInfo.getLoops()) {
            for (NaturalLoop<I> innerLoop : loopInfo.getNestedLoops(outerLoop.getHeaderId())) {
                // 设置父-子关系
                parentMap.put(innerLoop.getHeaderId(), outerLoop);
                childrenMap.get(outerLoop.getHeaderId()).add(innerLoop);

                logger.debug("Nested relationship: {} is child of {}",
                            innerLoop.getHeaderId(), outerLoop.getHeaderId());
            }
        }

        // 收集根循环（没有父循环的循环）
        this.roots = new ArrayList<>();
        for (NaturalLoop<I> loop : loopInfo.getLoops()) {
            if (parentMap.get(loop.getHeaderId()) == null) {
                roots.add(loop);
            }
        }

        logger.info("LoopNestingTree created: {} roots, {} total loops",
                   roots.size(), loopInfo.getLoopCount());
    }

    /**
     * 从LoopInfo创建循环嵌套树（静态工厂方法）
     *
     * @param loopInfo 循环信息
     * @return 新创建的循环嵌套树
     */
    @NotNull
    public static <I extends IRNode> LoopNestingTree<I> fromLoopInfo(@NotNull LoopInfo<I> loopInfo) {
        return new LoopNestingTree<>(loopInfo);
    }

    /**
     * 获取所有根循环（最外层循环）
     *
     * @return 不可修改的根循环列表
     */
    @NotNull
    public List<NaturalLoop<I>> getRootLoops() {
        return Collections.unmodifiableList(roots);
    }

    /**
     * 获取循环的父循环
     *
     * @param loop 循环
     * @return 父循环，如果是根循环返回null
     */
    @Nullable
    public NaturalLoop<I> getParent(@NotNull NaturalLoop<I> loop) {
        return parentMap.get(loop.getHeaderId());
    }

    /**
     * 根据循环头ID获取父循环
     *
     * @param headerId 循环头ID
     * @return 父循环，如果是根循环返回null
     */
    @Nullable
    public NaturalLoop<I> getParent(int headerId) {
        return parentMap.get(headerId);
    }

    /**
     * 获取循环的所有子循环
     *
     * @param loop 循环
     * @return 不可修改的子循环列表，如果没有子循环返回空列表
     */
    @NotNull
    public List<NaturalLoop<I>> getChildLoops(@NotNull NaturalLoop<I> loop) {
        List<NaturalLoop<I>> children = childrenMap.get(loop.getHeaderId());
        return children != null ? Collections.unmodifiableList(children) : Collections.emptyList();
    }

    /**
     * 根据循环头ID获取所有子循环
     *
     * @param headerId 循环头ID
     * @return 不可修改的子循环列表，如果没有子循环返回空列表
     */
    @NotNull
    public List<NaturalLoop<I>> getChildLoops(int headerId) {
        List<NaturalLoop<I>> children = childrenMap.get(headerId);
        return children != null ? Collections.unmodifiableList(children) : Collections.emptyList();
    }

    /**
     * 判断循环是否为根循环
     *
     * @param loop 循环
     * @return true如果是根循环，false otherwise
     */
    public boolean isRootLoop(@NotNull NaturalLoop<I> loop) {
        return parentMap.get(loop.getHeaderId()) == null;
    }

    /**
     * 判断循环是否为根循环
     *
     * @param headerId 循环头ID
     * @return true如果是根循环，false otherwise
     */
    public boolean isRootLoop(int headerId) {
        return parentMap.get(headerId) == null;
    }

    /**
     * 判断循环是否为叶子循环（不包含任何子循环）
     *
     * @param loop 循环
     * @return true如果是叶子循环，false otherwise
     */
    public boolean isLeafLoop(@NotNull NaturalLoop<I> loop) {
        List<NaturalLoop<I>> children = childrenMap.get(loop.getHeaderId());
        return children == null || children.isEmpty();
    }

    /**
     * 判断循环是否为叶子循环
     *
     * @param headerId 循环头ID
     * @return true如果是叶子循环，false otherwise
     */
    public boolean isLeafLoop(int headerId) {
        List<NaturalLoop<I>> children = childrenMap.get(headerId);
        return children == null || children.isEmpty();
    }

    /**
     * 深度优先遍历所有循环
     *
     * @param visitor 访问者，对每个循环调用
     */
    public void traverseDepthFirst(@NotNull LoopVisitor<I> visitor) {
        Objects.requireNonNull(visitor, "Visitor cannot be null");

        for (NaturalLoop<I> root : roots) {
            traverseDepthFirstRecursive(root, visitor, new HashSet<>());
        }
    }

    /**
     * 广度优先遍历所有循环
     *
     * @param visitor 访问者，对每个循环调用
     */
    public void traverseBreadthFirst(@NotNull LoopVisitor<I> visitor) {
        Objects.requireNonNull(visitor, "Visitor cannot be null");

        Queue<NaturalLoop<I>> queue = new LinkedList<>(roots);

        while (!queue.isEmpty()) {
            NaturalLoop<I> loop = queue.poll();
            visitor.visit(loop);

            // 添加子循环到队列
            List<NaturalLoop<I>> children = childrenMap.get(loop.getHeaderId());
            if (children != null) {
                queue.addAll(children);
            }
        }
    }

    /**
     * 递归深度优先遍历
     *
     * @param loop 当前循环
     * @param visitor 访问者
     * @param visited 已访问的循环集合（防止循环）
     */
    private void traverseDepthFirstRecursive(@NotNull NaturalLoop<I> loop,
                                       @NotNull LoopVisitor<I> visitor,
                                       @NotNull Set<Integer> visited) {
        if (visited.contains(loop.getHeaderId())) {
            return;
        }

        visited.add(loop.getHeaderId());
        visitor.visit(loop);

        // 递归遍历子循环
        for (NaturalLoop<I> child : getChildLoops(loop)) {
            traverseDepthFirstRecursive(child, visitor, visited);
        }
    }

    /**
     * 获取循环的深度（从根循环开始计数）
     *
     * @param loop 循环
     * @return 深度值（根循环为0）
     */
    public int getDepth(@NotNull NaturalLoop<I> loop) {
        int depth = 0;
        NaturalLoop<I> current = loop;

        while (current != null) {
            current = getParent(current);
            depth++;
        }

        return depth;
    }

    /**
     * 根据循环头ID获取循环深度
     *
     * @param headerId 循环头ID
     * @return 深度值（根循环为0）
     */
    public int getDepth(int headerId) {
        NaturalLoop<I> loop = loopInfo.getLoops().stream()
                .filter(l -> l.getHeaderId() == headerId)
                .findFirst()
                .orElse(null);

        if (loop == null) {
            logger.warn("Loop not found for header ID: {}", headerId);
            return -1;
        }

        return getDepth(loop);
    }

    /**
     * 获取循环树的统计信息
     *
     * @return 树的统计信息
     */
    @NotNull
    public TreeStatistics getStatistics() {
        int totalLoops = loopInfo.getLoopCount();
        int rootLoops = roots.size();
        int maxDepth = 0;
        int leafLoops = 0;

        for (NaturalLoop<I> loop : loopInfo.getLoops()) {
            int depth = getDepth(loop);
            if (depth > maxDepth) {
                maxDepth = depth;
            }
            if (isLeafLoop(loop)) {
                leafLoops++;
            }
        }

        int[] depthHistogram = new int[maxDepth + 1];
        for (NaturalLoop<I> loop : loopInfo.getLoops()) {
            int depth = getDepth(loop);
            depthHistogram[depth]++;
        }

        return new TreeStatistics(
                totalLoops,
                rootLoops,
                leafLoops,
                maxDepth,
                depthHistogram
        );
    }

    /**
     * 循环访问者接口
     */
    @FunctionalInterface
    public interface LoopVisitor<I extends IRNode> {
        void visit(@NotNull NaturalLoop<I> loop);
    }

    /**
     * 循环树统计信息
     */
    public static class TreeStatistics {
        private final int totalLoops;
        private final int rootLoops;
        private final int leafLoops;
        private final int maxDepth;
        private final int[] depthHistogram;

        public TreeStatistics(int totalLoops, int rootLoops,
                          int leafLoops, int maxDepth,
                          int[] depthHistogram) {
            this.totalLoops = totalLoops;
            this.rootLoops = rootLoops;
            this.leafLoops = leafLoops;
            this.maxDepth = maxDepth;
            this.depthHistogram = depthHistogram;
        }

        public int getTotalLoops() {
            return totalLoops;
        }

        public int getRootLoops() {
            return rootLoops;
        }

        public int getLeafLoops() {
            return leafLoops;
        }

        public int getMaxDepth() {
            return maxDepth;
        }

        public int[] getDepthHistogram() {
            return depthHistogram;
        }

        @Override
        public String toString() {
            return "TreeStatistics{totalLoops=" + totalLoops +
                   ", rootLoops=" + rootLoops +
                   ", leafLoops=" + leafLoops +
                   ", maxDepth=" + maxDepth +
                   ", depthHistogram=" + Arrays.toString(depthHistogram) + "}";
        }
    }
}
