package org.teachfx.antlr4.ep21.pass.cfg;

import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * 增强的控制流边数据结构 - 支持高级CFG分析和优化
 *
 * <p>CFGEdge提供了比Triple<Integer,Integer,Integer>更丰富的边表示，
 * 支持类型安全的边类型、元数据标记和快速查询优化。</p>
 *
 * <p>设计特性：</p>
 * <ul>
 *   <li>类型安全的边类型：使用CFGConstants.EdgeType枚举</li>
 *   <li>不可变对象：所有字段为final，线程安全</li>
 *   <li>关键边标记：支持关键边（Critical Edge）识别</li>
 *   <li>权重支持：用于优先级排序和优化引导</li>
 *   <li>完整的equals/hashCode：支持集合操作</li>
 * </ul>
 *
 * <h3>关键边（Critical Edge）</h3>
 * <p>关键边是指入度>1且出度>1的边。拆分关键边对SSA形式转换和
 * 其他优化Pass很重要，因为它简化了PHI节点的插入和支配边界的计算。</p>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 创建跳转边
 * CFGEdge<IRNode> jumpEdge = CFGEdge.of(0, 1, CFGConstants.EdgeType.JUMP);
 *
 * // 创建关键边标记
 * CFGEdge<IRNode> criticalEdge = jumpEdge.withCritical(true);
 *
 * // 转换为传统Triple格式（向后兼容）
 * Triple<Integer, Integer, Integer> triple = edge.toTriple();
 * }</pre>
 *
 * @param <I> IR节点类型参数
 * @see CFGConstants.EdgeType
 * @see CFG
 * @author EP21 Development Team
 * @version 1.0
 * @since 2026-01-19
 */
public final class CFGEdge<I> {

    // ========================================================================
    // 字段定义 - 不可变设计确保线程安全
    // ========================================================================

    /**
     * 源基本块ID - 边的起始节点
     * 必须为非负整数，且在CFG中存在对应的基本块
     */
    private final int sourceId;

    /**
     * 目标基本块ID - 边的终止节点
     * 必须为非负整数，且在CFG中存在对应的基本块
     */
    private final int targetId;

    /**
     * 边类型 - 使用类型安全的枚举
     * 支持的类型：JUMP, SUCCESSOR, FALLTHROUGH, CRITICAL等
     */
    private final CFGConstants.EdgeType type;

    /**
     * 边权重 - 用于优先级排序和优化引导
     * 权重越高表示边越重要或更可能被执行
     */
    private final int weight;

    /**
     * 关键边标记 - 标记此边是否为关键边
     * 关键边：入度>1且出度>1的边
     *
     * <p>关键边的拆分对SSA形式转换和其他优化Pass很重要，
     * 因为它简化了PHI节点的插入和支配边界的计算。</p>
     */
    private final boolean isCritical;

    // ========================================================================
    // 私有构造函数 - 通过工厂方法创建实例
    // ========================================================================

    /**
     * 私有构造函数 - 强制通过工厂方法创建实例
     *
     * @param sourceId 源基本块ID，必须非负
     * @param targetId 目标基本块ID，必须非负
     * @param type 边类型，不能为null
     * @param weight 边权重，用于排序和优化
     * @param isCritical 是否为关键边
     * @throws IllegalArgumentException 当参数无效时抛出
     */
    private CFGEdge(int sourceId, int targetId,
                 CFGConstants.EdgeType type, int weight,
                 boolean isCritical) {
        // 参数验证
        validateSourceId(sourceId);
        validateTargetId(targetId);
        Objects.requireNonNull(type, "Edge type cannot be null");
        validateWeight(weight);

        // 初始化字段
        this.sourceId = sourceId;
        this.targetId = targetId;
        this.type = type;
        this.weight = weight;
        this.isCritical = isCritical;
    }

    // ========================================================================
    // 验证方法 - 私有辅助方法
    // ========================================================================

    /**
     * 验证源节点ID的有效性
     *
     * @param sourceId 源节点ID
     * @throws IllegalArgumentException 当ID为负数时抛出
     */
    private static void validateSourceId(int sourceId) {
        if (sourceId < 0) {
            throw new IllegalArgumentException(
                String.format("Source ID must be non-negative: %d", sourceId));
        }
    }

    /**
     * 验证目标节点ID的有效性
     *
     * @param targetId 目标节点ID
     * @throws IllegalArgumentException 当ID为负数时抛出
     */
    private static void validateTargetId(int targetId) {
        if (targetId < 0) {
            throw new IllegalArgumentException(
                String.format("Target ID must be non-negative: %d", targetId));
        }
    }

    /**
     * 验证权重值的有效性
     *
     * @param weight 权重值
     * @throws IllegalArgumentException 当权重为负数时抛出
     */
    private static void validateWeight(int weight) {
        if (weight < 0) {
            throw new IllegalArgumentException(
                String.format("Weight must be non-negative: %d", weight));
        }
    }

    // ========================================================================
    // 工厂方法 - 提供多种创建方式
    // ========================================================================

    /**
     * 创建CFGEdge实例的工厂方法 - 使用默认权重
     *
     * <p>权重默认使用EdgeType中定义的值，确保向后兼容性。</p>
     *
     * @param <I> IR节点类型
     * @param sourceId 源基本块ID，必须非负
     * @param targetId 目标基本块ID，必须非负
     * @param type 边类型，不能为null
     * @return 新创建的CFGEdge实例
     * @throws IllegalArgumentException 当参数无效时抛出
     */
    @NotNull
    public static <I> CFGEdge<I> of(int sourceId, int targetId,
                                      CFGConstants.EdgeType type) {
        return new CFGEdge<>(
            sourceId,
            targetId,
            type,
            type.getWeight(),  // 使用EdgeType中定义的默认权重
            false          // 默认不是关键边
        );
    }

    /**
     * 创建带自定义权重的CFGEdge实例
     *
     * @param <I> IR节点类型
     * @param sourceId 源基本块ID，必须非负
     * @param targetId 目标基本块ID，必须非负
     * @param type 边类型，不能为null
     * @param weight 自定义边权重，必须非负
     * @return 新创建的CFGEdge实例
     * @throws IllegalArgumentException 当参数无效时抛出
     */
    @NotNull
    public static <I> CFGEdge<I> of(int sourceId, int targetId,
                                      CFGConstants.EdgeType type, int weight) {
        return new CFGEdge<>(sourceId, targetId, type, weight, false);
    }

    /**
     * 创建关键边的工厂方法
     *
     * @param <I> IR节点类型
     * @param sourceId 源基本块ID，必须非负
     * @param targetId 目标基本块ID，必须非负
     * @param type 边类型，不能为null
     * @return 新创建的关键边CFGEdge实例（isCritical=true）
     * @throws IllegalArgumentException 当参数无效时抛出
     */
    @NotNull
    public static <I> CFGEdge<I> critical(int sourceId, int targetId,
                                         CFGConstants.EdgeType type) {
        return new CFGEdge<>(
            sourceId,
            targetId,
            type,
            type.getWeight(),
            true  // 标记为关键边
        );
    }

    /**
     * 从传统Triple格式创建CFGEdge
     *
     * <p>此方法提供向后兼容性，允许从现有的Triple<Integer,Integer,Integer>
     * 转换为新的CFGEdge类型。</p>
     *
     * @param <I> IR节点类型
     * @param triple 传统格式的边数据（source, target, weight）
     * @return 新创建的CFGEdge实例，isCritical默认为false
     * @throws IllegalArgumentException 当triple参数无效时抛出
     */
    @NotNull
    public static <I> CFGEdge<I> fromTriple(Triple<Integer, Integer, Integer> triple) {
        Objects.requireNonNull(triple, "Triple cannot be null");

        // 从权重推断边类型
        CFGConstants.EdgeType edgeType = CFGConstants.EdgeType.fromWeight(triple.getRight());
        if (edgeType == null) {
            throw new IllegalArgumentException(
                String.format("Invalid edge weight in triple: %d", triple.getRight()));
        }

        return new CFGEdge<>(
            triple.getLeft(),
            triple.getMiddle(),
            edgeType,
            triple.getRight(),
            false  // 从Triple转换的边默认不是关键边
        );
    }

    // ========================================================================
    // 转换方法 - 支持与其他数据结构互操作
    // ========================================================================

    /**
     * 将CFGEdge转换为传统Triple格式（向后兼容）
     *
     * <p>此方法确保与现有代码的兼容性，允许将新的CFGEdge
     * 转换为Triple<Integer,Integer,Integer>格式。</p>
     *
     * @return Triple表示的边数据（sourceId, targetId, weight）
     */
    @NotNull
    public Triple<Integer, Integer, Integer> toTriple() {
        return Triple.of(sourceId, targetId, weight);
    }

    /**
     * 转换为带关键边标记的Triple
     *
     * <p>注意：传统Triple格式不支持isCritical标记，此信息会丢失。
     * 如果需要保留关键边标记，建议使用CFGEdge而非Triple。</p>
     *
     * @return Triple表示的边数据，不包含isCritical信息
     */
    @NotNull
    @Deprecated
    public Triple<Integer, Integer, Integer> toTripleWithCriticalFlag() {
        // Triple不支持isCritical，此方法仅为向后兼容提供
        return toTriple();
    }

    // ========================================================================
    // 复制和修改方法 - 创建变体
    // ========================================================================

    /**
     * 创建此边的副本，但修改关键边标记
     *
     * @param critical 新的关键边标记值
     * @return 新CFGEdge实例，其他字段不变
     */
    @NotNull
    public CFGEdge<I> withCritical(boolean critical) {
        return new CFGEdge<>(
            this.sourceId,
            this.targetId,
            this.type,
            this.weight,
            critical
        );
    }

    /**
     * 创建此边的副本，但修改边类型
     *
     * @param newType 新的边类型
     * @return 新CFGEdge实例，其他字段不变
     */
    @NotNull
    public CFGEdge<I> withType(CFGConstants.EdgeType newType) {
        return new CFGEdge<>(
            this.sourceId,
            this.targetId,
            newType,
            newType.getWeight(),
            this.isCritical
        );
    }

    /**
     * 创建此边的副本，但修改权重
     *
     * @param newWeight 新的权重值
     * @return 新CFGEdge实例，其他字段不变
     */
    @NotNull
    public CFGEdge<I> withWeight(int newWeight) {
        return new CFGEdge<>(
            this.sourceId,
            this.targetId,
            this.type,
            newWeight,
            this.isCritical
        );
    }

    // ========================================================================
    // Getter方法 - 访问不可变字段
    // ========================================================================

    /**
     * 获取源基本块ID
     *
     * @return 源节点ID（非负整数）
     */
    public int getSourceId() {
        return sourceId;
    }

    /**
     * 获取目标基本块ID
     *
     * @return 目标节点ID（非负整数）
     */
    public int getTargetId() {
        return targetId;
    }

    /**
     * 获取边类型
     *
     * @return 边类型（JUMP, SUCCESSOR等）
     */
    @NotNull
    public CFGConstants.EdgeType getType() {
        return type;
    }

    /**
     * 获取边权重
     *
     * @return 权重值（非负整数）
     */
    public int getWeight() {
        return weight;
    }

    /**
     * 检查此边是否为关键边
     *
     * <p>关键边（Critical Edge）是指入度>1且出度>1的边。
     * 拆分关键边对SSA形式转换和其他优化Pass很重要。</p>
     *
     * @return true如果此边是关键边，false otherwise
     */
    public boolean isCritical() {
        return isCritical;
    }

    // ========================================================================
    // Object方法 - 支持集合操作
    // ========================================================================

    /**
     * 判断两个边是否相等
     *
     * <p>两条边相等的条件是：sourceId、targetId和type都相等。
     * 注意：weight和isCritical不影响相等性判断。</p>
     *
     * @param o 要比较的对象
     * @return true如果两条边相等，false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CFGEdge<?> that = (CFGEdge<?>) o;

        return sourceId == that.sourceId &&
               targetId == that.targetId &&
               type == that.type;
    }

    /**
     * 计算边的哈希码
     *
     * <p>哈希码基于sourceId、targetId和type计算，
     * 确保与equals方法一致。</p>
     *
     * @return 哈希码整数值
     */
    @Override
    public int hashCode() {
        return Objects.hash(sourceId, targetId, type);
    }

    /**
     * 获取边的字符串表示
     *
     * <p>格式：CFGEdge[sourceId=targetId, type=EDGE_TYPE, weight=X, critical=Y]</p>
     *
     * @return 边的可读字符串表示
     */
    @NotNull
    @Override
    public String toString() {
        return String.format(
            "CFGEdge[sourceId=%d, targetId=%d, type=%s, weight=%d, critical=%s]",
            sourceId,
            targetId,
            type,
            weight,
            isCritical
        );
    }

    /**
     * 获取简化的字符串表示（用于日志输出）
     *
     * <p>格式：[source->target (type)]</p>
     *
     * @return 边的简化字符串表示
     */
    @NotNull
    public String toShortString() {
        return String.format("[%d->%d (%s)]", sourceId, targetId, type);
    }

    /**
     * 创建反向边（交换source和target）
     *
     * <p>返回一条新的边，source和target互换，type保持不变。
     * 这对于分析反向控制流很有用。</p>
     *
     * @return 新CFGEdge实例，方向相反
     */
    @NotNull
    public CFGEdge<I> reversed() {
        return new CFGEdge<>(
            targetId,
            sourceId,
            type,
            weight,
            isCritical
        );
    }
}
