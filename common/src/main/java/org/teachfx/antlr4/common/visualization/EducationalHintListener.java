package org.teachfx.antlr4.common.visualization;

import java.util.EventListener;

/**
 * 教育提示监听器接口
 * 
 * <p>该接口定义了教育相关提示的监听方法，
 * 包括学习提示、概念解释、代码分析等教育功能的回调。</p>
 * 
 * @author TeachFX Team
 * @version 1.0
 * @since EP18R改进计划阶段一
 */
public interface EducationalHintListener extends EventListener {
    
    /**
     * 提示可用时调用
     * 
     * @param hint 提示内容
     * @param category 提示类别
     * @param priority 提示优先级
     */
    default void hintAvailable(String hint, HintCategory category, int priority) {}
    
    /**
     * 概念解释可用时调用
     * 
     * @param concept 概念名称
     * @param explanation 概念解释
     * @param relatedInstruction 相关指令
     */
    default void conceptExplained(String concept, String explanation, String relatedInstruction) {}
    
    /**
     * 代码分析提示可用时调用
     * 
     * @param analysisType 分析类型
     * @param description 分析描述
     * @param suggestions 改进建议
     */
    default void codeAnalysisAvailable(String analysisType, String description, String[] suggestions) {}
    
    /**
     * 性能优化提示可用时调用
     * 
     * @param optimizationType 优化类型
     * @param currentCode 当前代码
     * @param optimizedCode 优化后的代码
     * @param improvement 改进程度描述
     */
    default void optimizationSuggestion(String optimizationType, String currentCode, 
                                 String optimizedCode, String improvement) {}
    
    /**
     * 错误学习提示可用时调用
     * 
     * @param errorType 错误类型
     * @param errorMessage 错误消息
     * @param explanation 错误解释
     * @param solution 解决方案
     */
    default void errorLearningHint(String errorType, String errorMessage, 
                             String explanation, String solution) {}
    
    /**
     * 调试提示可用时调用
     * 
     * @param debugHint 调试提示内容
     * @param context 上下文信息
     * @param action 建议的调试动作
     */
    default void debugHintAvailable(String debugHint, String context, String action) {}
    
    /**
     * 学习进度更新时调用
     * 
     * @param topic 学习主题
     * @param progress 进度百分比（0-100）
     * @param completedItems 已完成的项目
     * @param totalItems 总项目数
     */
    default void learningProgressUpdated(String topic, int progress, 
                                  int completedItems, int totalItems) {}
    
    /**
     * 交互式问题可用时调用
     * 
     * @param question 问题内容
     * @param options 选项列表
     * @param correctAnswer 正确答案索引
     * @param explanation 答案解释
     */
    default void interactiveQuestionAvailable(String question, String[] options, 
                                     int correctAnswer, String explanation) {}
    
    /**
     * 代码模式识别时调用
     * 
     * @param pattern 模式名称
     * @param description 模式描述
     * @param examples 示例代码
     */
    default void codePatternRecognized(String pattern, String description, String[] examples) {}
    
    /**
     * 最佳实践建议可用时调用
     * 
     * @param practice 实践名称
     * @param description 实践描述
     * @param benefits 好处说明
     */
    default void bestPracticeSuggested(String practice, String description, String benefits) {}
    
    /**
     * 学习资源推荐可用时调用
     * 
     * @param resourceType 资源类型
     * @param title 资源标题
     * @param description 资源描述
     * @param url 资源链接
     */
    default void learningResourceRecommended(String resourceType, String title, 
                                      String description, String url) {}
    
    /**
     * 提示类别枚举
     */
    enum HintCategory {
        /**
         * 基本操作
         */
        BASIC_OPERATION,
        
        /**
         * 高级概念
         */
        ADVANCED_CONCEPT,
        
        /**
         * 调试技巧
         */
        DEBUGGING,
        
        /**
         * 性能优化
         */
        PERFORMANCE,
        
        /**
         * 错误预防
         */
        ERROR_PREVENTION,
        
        /**
         * 代码风格
         */
        CODE_STYLE,
        
        /**
         * 最佳实践
         */
        BEST_PRACTICE,
        
        /**
         * 概念学习
         */
        CONCEPT_LEARNING
    }
}