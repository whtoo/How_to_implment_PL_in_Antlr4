package org.teachfx.antlr4.ep21.analysis.dataflow;

import org.teachfx.antlr4.ep21.ir.expr.Operand;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 定义集合工具类（Definition Sets Utility）
 *
 * 提供对Set<Definition>的常用操作，包括按变量分组、
 * 获取特定变量的定义、排除指定定义、格式化等。
 *
 * <p>设计原则：
 * <ul>
 *   <li>纯函数：所有方法不修改输入集合，返回新集合</li>
 *   <li>空安全：所有方法正确处理空输入</li>
 *   <li>流式友好：使用Java 8 Stream API实现</li>
 * </ul>
 *
 * @author EP21 Team
 * @version 1.0
 * @since 2026-01-18
 */
public class DefinitionSets {

    /**
     * 私有构造函数，防止实例化
     */
    private DefinitionSets() {
        throw new AssertionError("Utility class cannot be instantiated");
    }

    /**
     * 按变量分组定义
     *
     * <p>将定义集合按变量分组，返回映射：
     * {@code 变量 -> 该变量的所有定义}
     *
     * @param definitions 定义集合，可以为null或空
     * @return 按变量分组的映射，如果输入为null或空，返回空映射
     */
    public static Map<Operand, Set<Definition>> groupByVariable(Set<Definition> definitions) {
        if (definitions == null || definitions.isEmpty()) {
            return Collections.emptyMap();
        }

        return definitions.stream()
                .collect(Collectors.groupingBy(
                        Definition::getVariable,
                        Collectors.toSet()
                ));
    }

    /**
     * 获取特定变量的所有定义
     *
     * @param definitions 定义集合，可以为null或空
     * @param variable 目标变量，不能为null
     * @return 该变量的所有定义，如果未找到返回空集合
     * @throws NullPointerException 如果variable为null
     */
    public static Set<Definition> getDefinitionsOfVariable(
            Set<Definition> definitions, Operand variable) {
        if (definitions == null || definitions.isEmpty()) {
            return Collections.emptySet();
        }

        return definitions.stream()
                .filter(def -> def.getVariable().equals(variable))
                .collect(Collectors.toSet());
    }

    /**
     * 检查定义集合中是否包含对指定变量的定义
     *
     * @param definitions 定义集合，可以为null或空
     * @param variable 目标变量，不能为null
     * @return 如果包含返回true，否则返回false
     */
    public static boolean containsDefinitionFor(
            Set<Definition> definitions, Operand variable) {
        return !getDefinitionsOfVariable(definitions, variable).isEmpty();
    }

    /**
     * 排除指定定义
     *
     * <p>返回不包含toExclude的新集合，原集合不变。
     *
     * @param definitions 定义集合，可以为null或空
     * @param toExclude 要排除的定义，可以为null（此时返回原集合）
     * @return 排除后的定义集合
     */
    public static Set<Definition> exclude(
            Set<Definition> definitions, Definition toExclude) {
        if (definitions == null || definitions.isEmpty()) {
            return Collections.emptySet();
        }

        if (toExclude == null) {
            return new HashSet<>(definitions);
        }

        return definitions.stream()
                .filter(def -> !def.equals(toExclude))
                .collect(Collectors.toSet());
    }

    /**
     * 排除多个定义
     *
     * <p>返回不包含toExcludes中任何定义的新集合，原集合不变。
     *
     * @param definitions 定义集合，可以为null或空
     * @param toExcludes 要排除的定义集合，可以为null或空
     * @return 排除后的定义集合
     */
    public static Set<Definition> excludeAll(
            Set<Definition> definitions, Set<Definition> toExcludes) {
        if (definitions == null || definitions.isEmpty()) {
            return Collections.emptySet();
        }

        if (toExcludes == null || toExcludes.isEmpty()) {
            return new HashSet<>(definitions);
        }

        return definitions.stream()
                .filter(def -> !toExcludes.contains(def))
                .collect(Collectors.toSet());
    }

    /**
     * 合并两个定义集合
     *
     * <p>返回两个集合的并集，两个原集合不变。
     *
     * @param set1 第一个集合，可以为null或空
     * @param set2 第二个集合，可以为null或空
     * @return 合并后的集合
     */
    public static Set<Definition> union(
            Set<Definition> set1, Set<Definition> set2) {
        Set<Definition> result = new HashSet<>();

        if (set1 != null) {
            result.addAll(set1);
        }

        if (set2 != null) {
            result.addAll(set2);
        }

        return result;
    }

    /**
     * 计算两个定义集合的交集
     *
     * <p>返回两个集合的交集，两个原集合不变。
     *
     * @param set1 第一个集合，可以为null或空
     * @param set2 第二个集合，可以为null或空
     * @return 交集集合
     */
    public static Set<Definition> intersect(
            Set<Definition> set1, Set<Definition> set2) {
        if (set1 == null || set1.isEmpty()) {
            return Collections.emptySet();
        }

        if (set2 == null || set2.isEmpty()) {
            return Collections.emptySet();
        }

        return set1.stream()
                .filter(set2::contains)
                .collect(Collectors.toSet());
    }

    /**
     * 按基本块分组定义
     *
     * <p>返回映射：{@code 基本块ID -> 该基本块中的所有定义}
     *
     * @param definitions 定义集合，可以为null或空
     * @return 按基本块ID分组的映射，如果输入为null或空，返回空映射
     */
    public static Map<Integer, Set<Definition>> groupByBlockId(Set<Definition> definitions) {
        if (definitions == null || definitions.isEmpty()) {
            return Collections.emptyMap();
        }

        return definitions.stream()
                .collect(Collectors.groupingBy(
                        def -> def.getBlock().getId(),
                        Collectors.toSet()
                ));
    }

    /**
     * 获取特定基本块中的所有定义
     *
     * @param definitions 定义集合，可以为null或空
     * @param blockId 基本块ID
     * @return 该基本块中的所有定义，如果未找到返回空集合
     */
    public static Set<Definition> getDefinitionsInBlock(
            Set<Definition> definitions, int blockId) {
        if (definitions == null || definitions.isEmpty()) {
            return Collections.emptySet();
        }

        return definitions.stream()
                .filter(def -> def.getBlock().getId() == blockId)
                .collect(Collectors.toSet());
    }

    /**
     * 按指令索引分组定义
     *
     * <p>返回映射：{@code 指令索引 -> 该索引处的所有定义}
     *
     * @param definitions 定义集合，可以为null或空
     * @return 按指令索引分组的映射，如果输入为null或空，返回空映射
     */
    public static Map<Integer, Set<Definition>> groupByInstructionIndex(Set<Definition> definitions) {
        if (definitions == null || definitions.isEmpty()) {
            return Collections.emptyMap();
        }

        return definitions.stream()
                .collect(Collectors.groupingBy(
                        Definition::getInstructionIndex,
                        Collectors.toSet()
                ));
    }

    /**
     * 格式化定义集合为字符串
     *
     * <p>格式: {@code {d1, d2, d3}}
     *
     * <p>示例:
     * <pre>
     *   {}                           // 空集合
     *   {x@B0:0}                    // 单个定义
     *   {x@B0:0, y@B1:2}           // 多个定义
     * </pre>
     *
     * @param definitions 定义集合，可以为null或空
     * @return 格式化字符串
     */
    public static String format(Set<Definition> definitions) {
        if (definitions == null || definitions.isEmpty()) {
            return "{}";
        }

        StringBuilder sb = new StringBuilder("{");
        boolean first = true;

        // 按顺序排序（基于compareTo）：先按blockId，再按instructionIndex
        List<Definition> sorted = new ArrayList<>(definitions);
        sorted.sort(Comparator.comparingInt(Definition::getInstructionIndex));

        for (Definition def : sorted) {
            if (!first) {
                sb.append(", ");
            }
            sb.append(def.toString());
            first = false;
        }

        sb.append("}");
        return sb.toString();
    }

    /**
     * 格式化定义集合为详细字符串
     *
     * <p>格式: {@code {定义1: 描述, 定义2: 描述, ...}}
     *
     * <p>示例:
     * <pre>
     *   {
     *     x@B0:0: Assignment to x in block 0 at index 0,
     *     y@B1:2: Assignment to y in block 1 at index 2
     *   }
     * </pre>
     *
     * @param definitions 定义集合，可以为null或空
     * @return 详细格式化字符串
     */
    public static String formatDetailed(Set<Definition> definitions) {
        if (definitions == null || definitions.isEmpty()) {
            return "{}";
        }

        StringBuilder sb = new StringBuilder("{\n");

        // 按顺序排序
        List<Definition> sorted = new ArrayList<>(definitions);
        sorted.sort(Comparator.comparing(Definition::toString));

        for (int i = 0; i < sorted.size(); i++) {
            Definition def = sorted.get(i);
            sb.append("  ").append(def.toString())
              .append(": Assignment to ")
              .append(def.getVariable())
              .append(" in block ")
              .append(def.getBlock().getId())
              .append(" at index ")
              .append(def.getInstructionIndex());

            if (i < sorted.size() - 1) {
                sb.append(",");
            }
            sb.append("\n");
        }

        sb.append("}");
        return sb.toString();
    }

    /**
     * 获取定义集合的大小
     *
     * @param definitions 定义集合，可以为null或空
     * @return 集合大小，如果输入为null返回0
     */
    public static int size(Set<Definition> definitions) {
        return definitions == null ? 0 : definitions.size();
    }

    /**
     * 判断定义集合是否为空
     *
     * @param definitions 定义集合，可以为null
     * @return 如果为null或空返回true，否则返回false
     */
    public static boolean isEmpty(Set<Definition> definitions) {
        return definitions == null || definitions.isEmpty();
    }

    /**
     * 创建不可修改的定义集合
     *
     * <p>返回输入集合的不可修改视图，如果输入为null返回空不可修改集合。
     *
     * @param definitions 定义集合，可以为null
     * @return 不可修改的集合
     */
    public static Set<Definition> unmodifiableSet(Set<Definition> definitions) {
        if (definitions == null) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(new HashSet<>(definitions));
    }

    /**
     * 统计每个变量的定义数量
     *
     * @param definitions 定义集合，可以为null或空
     * @return 映射：{@code 变量 -> 定义数量}，如果输入为null或空，返回空映射
     */
    public static Map<Operand, Integer> countByVariable(Set<Definition> definitions) {
        if (definitions == null || definitions.isEmpty()) {
            return Collections.emptyMap();
        }

        return definitions.stream()
                .collect(Collectors.groupingBy(
                        Definition::getVariable,
                        Collectors.summingInt(def -> 1)
                ));
    }

    /**
     * 获取定义数量最多的变量
     *
     * @param definitions 定义集合，可以为null或空
     * @return 定义数量最多的变量及其数量，如果输入为null或空返回null
     */
    public static Map.Entry<Operand, Integer> getMostFrequentVariable(Set<Definition> definitions) {
        if (definitions == null || definitions.isEmpty()) {
            return null;
        }

        return countByVariable(definitions).entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElse(null);
    }

    /**
     * 过滤出属于指定基本块的定义
     *
     * @param definitions 定义集合，可以为null或空
     * @param blockIds 基本块ID集合，可以为null或空（此时返回空集合）
     * @return 属于指定基本块的定义集合
     */
    public static Set<Definition> filterByBlockIds(
            Set<Definition> definitions, Set<Integer> blockIds) {
        if (definitions == null || definitions.isEmpty()) {
            return Collections.emptySet();
        }

        if (blockIds == null || blockIds.isEmpty()) {
            return Collections.emptySet();
        }

        return definitions.stream()
                .filter(def -> blockIds.contains(def.getBlock().getId()))
                .collect(Collectors.toSet());
    }

    /**
     * 过滤出在指定指令索引范围的定义
     *
     * @param definitions 定义集合，可以为null或空
     * @param minIndex 最小指令索引（包含）
     * @param maxIndex 最大指令索引（不包含）
     * @return 在指定范围内的定义集合
     */
    public static Set<Definition> filterByInstructionRange(
            Set<Definition> definitions, int minIndex, int maxIndex) {
        if (definitions == null || definitions.isEmpty()) {
            return Collections.emptySet();
        }

        return definitions.stream()
                .filter(def -> def.getInstructionIndex() >= minIndex &&
                              def.getInstructionIndex() < maxIndex)
                .collect(Collectors.toSet());
    }
}
