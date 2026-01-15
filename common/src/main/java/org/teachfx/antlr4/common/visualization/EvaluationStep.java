package org.teachfx.antlr4.common.visualization;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * 表达式求值步骤类
 * 
 * <p>该类表示表达式求值过程中的单个步骤，包含步骤描述、
 * 中间结果和相关上下文信息。支持复杂表达式的逐步求值可视化。</p>
 * 
 * @author TeachFX Team
 * @version 1.0
 * @since EP18R改进计划阶段一
 */
public class EvaluationStep {
    
    /**
     * 步骤序号
     */
    private final int stepNumber;
    
    /**
     * 步骤描述
     */
    private final String description;
    
    /**
     * 当前表达式
     */
    private final String currentExpression;
    
    /**
     * 中间结果
     */
    private final Object intermediateResult;
    
    /**
     * 操作类型
     */
    private final OperationType operationType;
    
    /**
     * 操作数列表
     */
    private final List<Object> operands;
    
    /**
     * 附加信息
     */
    private final String additionalInfo;
    
    /**
     * 构造函数
     * 
     * @param stepNumber 步骤序号
     * @param description 步骤描述
     * @param currentExpression 当前表达式
     * @param intermediateResult 中间结果
     * @param operationType 操作类型
     */
    public EvaluationStep(int stepNumber, String description, String currentExpression,
                        Object intermediateResult, OperationType operationType) {
        this(stepNumber, description, currentExpression, intermediateResult, operationType,
             Collections.emptyList(), null);
    }
    
    /**
     * 完整构造函数
     * 
     * @param stepNumber 步骤序号
     * @param description 步骤描述
     * @param currentExpression 当前表达式
     * @param intermediateResult 中间结果
     * @param operationType 操作类型
     * @param operands 操作数列表
     * @param additionalInfo 附加信息
     */
    public EvaluationStep(int stepNumber, String description, String currentExpression,
                        Object intermediateResult, OperationType operationType,
                        List<Object> operands, String additionalInfo) {
        this.stepNumber = stepNumber;
        this.description = description != null ? description : "";
        this.currentExpression = currentExpression != null ? currentExpression : "";
        this.intermediateResult = intermediateResult;
        this.operationType = operationType != null ? operationType : OperationType.OTHER;
        this.operands = operands != null ? new ArrayList<>(operands) : new ArrayList<>();
        this.additionalInfo = additionalInfo;
    }
    
    // ==================== Getters ====================
    
    /**
     * 获取步骤序号
     * 
     * @return 步骤序号
     */
    public int getStepNumber() {
        return stepNumber;
    }
    
    /**
     * 获取步骤描述
     * 
     * @return 步骤描述
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * 获取当前表达式
     * 
     * @return 当前表达式
     */
    public String getCurrentExpression() {
        return currentExpression;
    }
    
    /**
     * 获取中间结果
     * 
     * @return 中间结果
     */
    public Object getIntermediateResult() {
        return intermediateResult;
    }
    
    /**
     * 获取操作类型
     * 
     * @return 操作类型
     */
    public OperationType getOperationType() {
        return operationType;
    }
    
    /**
     * 获取操作数列表
     * 
     * @return 操作数列表的拷贝
     */
    public List<Object> getOperands() {
        return new ArrayList<>(operands);
    }
    
    /**
     * 获取附加信息
     * 
     * @return 附加信息
     */
    public String getAdditionalInfo() {
        return additionalInfo;
    }
    
    /**
     * 检查是否有中间结果
     * 
     * @return 如果有中间结果返回true
     */
    public boolean hasIntermediateResult() {
        return intermediateResult != null;
    }
    
    /**
     * 检查是否有操作数
     * 
     * @return 如果有操作数返回true
     */
    public boolean hasOperands() {
        return !operands.isEmpty();
    }
    
    /**
     * 获取操作数数量
     * 
     * @return 操作数数量
     */
    public int getOperandCount() {
        return operands.size();
    }
    
    /**
     * 获取指定索引的操作数
     * 
     * @param index 操作数索引
     * @return 操作数
     * @throws IndexOutOfBoundsException 如果索引无效
     */
    public Object getOperand(int index) throws IndexOutOfBoundsException {
        return operands.get(index);
    }
    
    // ==================== 实用方法 ====================
    
    /**
     * 创建子表达式步骤
     * 
     * @param subExpression 子表达式
     * @param result 结果
     * @return 新的求值步骤
     */
    public EvaluationStep createSubStep(String subExpression, Object result) {
        return new EvaluationStep(
            this.stepNumber + 1,
            "Evaluate sub-expression: " + subExpression,
            subExpression,
            result,
            OperationType.SUB_EXPRESSION
        );
    }
    
    /**
     * 创建赋值步骤
     * 
     * @param variable 变量名
     * @param value 值
     * @return 新的求值步骤
     */
    public static EvaluationStep createAssignmentStep(int stepNumber, String variable, Object value) {
        return new EvaluationStep(
            stepNumber,
            "Assign value to variable",
            variable + " = " + value,
            value,
            OperationType.ASSIGNMENT
        );
    }
    
    /**
     * 创建函数调用步骤
     * 
     * @param stepNumber 步骤序号
     * @param functionName 函数名
     * @param arguments 参数列表
     * @param result 返回值
     * @return 新的求值步骤
     */
    public static EvaluationStep createFunctionCallStep(int stepNumber, String functionName,
                                                     List<Object> arguments, Object result) {
        return new EvaluationStep(
            stepNumber,
            "Call function: " + functionName,
            functionName + "(" + arguments + ")",
            result,
            OperationType.FUNCTION_CALL,
            arguments,
            "Return value: " + result
        );
    }
    
    /**
     * 创建运算步骤
     * 
     * @param stepNumber 步骤序号
     * @param operator 运算符
     * @param left 左操作数
     * @param right 右操作数
     * @param result 结果
     * @return 新的求值步骤
     */
    public static EvaluationStep createOperationStep(int stepNumber, String operator,
                                               Object left, Object right, Object result) {
        List<Object> operands = new ArrayList<>();
        operands.add(left);
        operands.add(right);
        
        return new EvaluationStep(
            stepNumber,
            "Perform operation: " + operator,
            left + " " + operator + " " + right,
            result,
            OperationType.ARITHMETIC,
            operands,
            "Result: " + result
        );
    }
    
    /**
     * 获取格式化的步骤描述
     * 
     * @return 格式化的字符串
     */
    public String getFormattedDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("Step ").append(stepNumber).append(": ").append(description);
        
        if (!currentExpression.isEmpty()) {
            sb.append("\n  Expression: ").append(currentExpression);
        }
        
        if (hasIntermediateResult()) {
            sb.append("\n  Result: ").append(intermediateResult);
        }
        
        if (hasOperands()) {
            sb.append("\n  Operands: ").append(operands);
        }
        
        if (additionalInfo != null && !additionalInfo.isEmpty()) {
            sb.append("\n  Info: ").append(additionalInfo);
        }
        
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return String.format(
            "EvaluationStep{step=%d, desc='%s', expr='%s', result=%s, type=%s}",
            stepNumber, description, currentExpression, intermediateResult, operationType
        );
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        EvaluationStep that = (EvaluationStep) obj;
        return stepNumber == that.stepNumber &&
               description.equals(that.description) &&
               currentExpression.equals(that.currentExpression) &&
               java.util.Objects.equals(intermediateResult, that.intermediateResult) &&
               operationType == that.operationType;
    }
    
    @Override
    public int hashCode() {
        return java.util.Objects.hash(stepNumber, description, currentExpression, 
                                   intermediateResult, operationType);
    }
    
    /**
     * 操作类型枚举
     */
    public enum OperationType {
        /**
         * 算术运算
         */
        ARITHMETIC,
        
        /**
         * 逻辑运算
         */
        LOGICAL,
        
        /**
         * 比较运算
         */
        COMPARISON,
        
        /**
         * 函数调用
         */
        FUNCTION_CALL,
        
        /**
         * 赋值
         */
        ASSIGNMENT,
        
        /**
         * 子表达式
         */
        SUB_EXPRESSION,
        
        /**
         * 变量访问
         */
        VARIABLE_ACCESS,
        
        /**
         * 常量
         */
        CONSTANT,
        
        /**
         * 其他
         */
        OTHER
    }
}