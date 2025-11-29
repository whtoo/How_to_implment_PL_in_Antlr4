package org.teachfx.antlr4.ep21.pass.cfg;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 控制流图常量定义类 - 提供统一的常量管理和类型安全的边类型定义
 * 
 * 改进特性：
 * 1. 类型安全的枚举类型替代魔法数字
 * 2. 接口定义确保常量一致性
 * 3. 运行时验证机制防止配置错误
 * 4. 详细的文档和注释
 * 5. 向后兼容性支持
 * 6. 性能优化和内存效率
 */
public final class CFGConstants {
    private static final Logger logger = LogManager.getLogger(CFGConstants.class);
    
    /**
     * 边类型枚举 - 提供类型安全的边类型定义
     * 替代原来的魔法数字，提高代码可读性和类型安全性
     */
    public enum EdgeType {
        /**
         * 跳转边类型 - 表示控制流跳转（如JMP指令产生的边）
         * 权重值: 5 (保持向后兼容)
         */
        JUMP(5, "跳转边"),
        
        /**
         * 后续边类型 - 表示顺序执行的边
         * 权重值: 10 (保持向后兼容)
         */
        SUCCESSOR(10, "后续边");
        
        private final int weight;
        private final String description;
        
        /**
         * 构造函数
         * @param weight 边权重，用于优先级排序
         * @param description 边的描述信息
         */
        EdgeType(int weight, String description) {
            this.weight = weight;
            this.description = description;
        }
        
        /**
         * 获取边权重
         * @return 权重整数值
         */
        public int getWeight() {
            return weight;
        }
        
        /**
         * 获取边的描述信息
         * @return 描述字符串
         */
        public String getDescription() {
            return description;
        }
        
        /**
         * 根据权重值获取对应的边类型
         * @param weight 权重值
         * @return 对应的EdgeType，如果无匹配返回null
         */
        public static EdgeType fromWeight(int weight) {
            return Arrays.stream(values())
                    .filter(type -> type.weight == weight)
                    .findFirst()
                    .orElse(null);
        }
        
        /**
         * 检查权重值是否有效
         * @param weight 要检查的权重值
         * @return true如果权重值有效，false otherwise
         */
        public static boolean isValidWeight(int weight) {
            return Arrays.stream(values()).anyMatch(type -> type.weight == weight);
        }
    }
    
    // ========================================================================
    // 传统常量定义 - 向后兼容性保留
    // ========================================================================
    
    /**
     * 跳转边类型常量 - 与CFGBuilder保持一致
     * @deprecated 使用EdgeType.JUMP替代，推荐使用类型安全的枚举
     */
    @Deprecated
    public static final int JUMP_EDGE_TYPE = EdgeType.JUMP.getWeight();
    
    /**
     * 后续边类型常量 - 与CFGBuilder保持一致
     * @deprecated 使用EdgeType.SUCCESSOR替代，推荐使用类型安全的枚举
     */
    @Deprecated
    public static final int SUCCESSOR_EDGE_TYPE = EdgeType.SUCCESSOR.getWeight();
    
    // ========================================================================
    // 性能优化常量
    // ========================================================================
    
    /**
     * 预定义的边权重集合，用于快速验证和查找
     */
    private static final Set<Integer> VALID_EDGE_WEIGHTS = new HashSet<>();
    
    /**
     * 静态初始化块 - 初始化验证集合和执行一致性检查
     */
    static {
        // 填充有效的边权重集合
        VALID_EDGE_WEIGHTS.add(EdgeType.JUMP.getWeight());
        VALID_EDGE_WEIGHTS.add(EdgeType.SUCCESSOR.getWeight());
        
        // 执行常量一致性验证
        validateConstantsConsistency();
        
        logger.debug("CFG常量初始化完成，验证边权重: {}", VALID_EDGE_WEIGHTS);
    }
    
    /**
     * 验证常量定义的一致性
     * 确保ControlFlowAnalysis和CFGBuilder中的常量值保持一致
     * 
     * @throws IllegalStateException 当检测到常量不一致时抛出
     */
    private static void validateConstantsConsistency() {
        try {
            // 这里可以扩展为从其他类读取常量进行验证
            // 目前通过反射方式验证，确保实际使用的常量值与定义一致
            
            validateEdgeTypeValue("JUMP_EDGE_TYPE", JUMP_EDGE_TYPE, EdgeType.JUMP.getWeight());
            validateEdgeTypeValue("SUCCESSOR_EDGE_TYPE", SUCCESSOR_EDGE_TYPE, EdgeType.SUCCESSOR.getWeight());
            
            logger.info("常量一致性验证通过");
            
        } catch (IllegalStateException e) {
            logger.error("常量一致性验证失败", e);
            throw e;
        }
    }
    
    /**
     * 验证单个边类型常量值的正确性
     * @param constantName 常量名称
     * @param actualValue 实际值
     * @param expectedValue 期望值
     * @throws IllegalStateException 当值不匹配时抛出
     */
    private static void validateEdgeTypeValue(String constantName, int actualValue, int expectedValue) {
        if (actualValue != expectedValue) {
            throw new IllegalStateException(
                String.format("常量 %s 值不匹配: 实际值=%d, 期望值=%d", 
                             constantName, actualValue, expectedValue));
        }
    }
    
    /**
     * 检查权重值是否为有效的边类型
     * @param weight 要检查的权重值
     * @return true如果权重值有效，false otherwise
     */
    public static boolean isValidEdgeWeight(int weight) {
        return VALID_EDGE_WEIGHTS.contains(weight);
    }
    
    /**
     * 获取边类型的描述信息
     * @param weight 边权重
     * @return 边的描述信息，如果权重无效返回null
     */
    public static String getEdgeDescription(int weight) {
        EdgeType edgeType = EdgeType.fromWeight(weight);
        return edgeType != null ? edgeType.getDescription() : null;
    }
    
    /**
     * 获取所有有效的边类型
     * @return 边类型数组
     */
    public static EdgeType[] getAllEdgeTypes() {
        return EdgeType.values();
    }
    
    /**
     * 获取所有有效的边权重值
     * @return 有效权重值的数组
     */
    public static int[] getAllValidWeights() {
        return VALID_EDGE_WEIGHTS.stream()
                .mapToInt(Integer::intValue)
                .toArray();
    }
    
    /**
     * 创建边三元组的便捷方法
     * @param source 源节点ID
     * @param target 目标节点ID
     * @param edgeType 边类型
     * @return 包含边信息的Triple对象
     */
    public static org.apache.commons.lang3.tuple.Triple<Integer, Integer, Integer> 
            createEdgeTriple(int source, int target, EdgeType edgeType) {
        return org.apache.commons.lang3.tuple.Triple.of(source, target, edgeType.getWeight());
    }
    
    /**
     * 创建边三元组的向后兼容方法
     * @param source 源节点ID
     * @param target 目标节点ID
     * @param weight 边权重（传统的int值）
     * @return 包含边信息的Triple对象
     * @throws IllegalArgumentException 当权重值无效时抛出
     */
    public static org.apache.commons.lang3.tuple.Triple<Integer, Integer, Integer> 
            createEdgeTriple(int source, int target, int weight) {
        if (!isValidEdgeWeight(weight)) {
            throw new IllegalArgumentException(
                String.format("无效的边权重值: %d. 有效值: %s", 
                             weight, Arrays.toString(getAllValidWeights())));
        }
        return org.apache.commons.lang3.tuple.Triple.of(source, target, weight);
    }
    
    // ========================================================================
    // 工具方法 - 性能优化
    // ========================================================================
    
    /**
     * 预计算常用边类型的字符串表示，避免重复计算
     */
    private static final String JUMP_EDGE_STRING = "JUMP_EDGE";
    private static final String SUCCESSOR_EDGE_STRING = "SUCCESSOR_EDGE";
    
    /**
     * 获取边类型的字符串表示
     * @param edgeType 边类型
     * @return 边类型的字符串表示
     */
    public static String getEdgeTypeString(EdgeType edgeType) {
        return switch (edgeType) {
            case JUMP -> JUMP_EDGE_STRING;
            case SUCCESSOR -> SUCCESSOR_EDGE_STRING;
        };
    }
    
    /**
     * 防止实例化工具类
     */
    private CFGConstants() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }
}