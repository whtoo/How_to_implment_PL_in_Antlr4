package org.teachfx.antlr4.ep21.pass.cfg;

import org.teachfx.antlr4.ep21.ir.IRNode;
import org.teachfx.antlr4.ep21.ir.expr.Operand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * StackFrame - 栈帧数据结构
 *
 * 用于显式栈模拟的递归转换。每个StackFrame代表一次递归调用的栈帧。
 *
 * 基于Baeldung算法实现：https://www.baeldung.com/cs/convert-recursion-to-iteration
 *
 * 栈帧结构：
 * - parameters: 函数参数
 * - locals: 局部变量
 * - returnValue: 返回值占位符
 * - programCounter: 指向下一个要执行的子节点索引
 * - parent: 父栈帧（调用者）
 * - children: 子栈帧列表（被调用者）
 * - state: 执行状态（EXECUTING, COMPLETED）
 *
 * @author EP21 Team
 * @version 1.0
 * @since 2025-12-23
 */
public class StackFrame {

    /** 栈帧状态 */
    public enum State {
        EXECUTING,   // 正在执行
        COMPLETED,   // 执行完成
        SUSPENDED    // 挂起（等待子调用返回）
    }

    /** 函数名称 */
    private final String functionName;

    /** 参数映射: 参数名 -> 值 */
    private final Map<String, Operand> parameters;

    /** 局部变量: 变量名 -> 值 */
    private final Map<String, Operand> locals;

    /** 返回值占位符 */
    private Operand returnValue;

    /** 程序计数器：指向下一个要执行的子节点索引 */
    private int programCounter;

    /** 父栈帧（调用者） */
    private final StackFrame parent;

    /** 子栈帧列表（被调用者） */
    private final List<StackFrame> children;

    /** 当前执行状态 */
    private State state;

    /** 对应的CFG节点ID（用于调试） */
    private final int cfgNodeId;

    /** 深度（用于调试和限制递归深度） */
    private final int depth;

    /**
     * 创建根栈帧
     *
     * @param functionName 函数名称
     * @param parameters 函数参数
     */
    public StackFrame(String functionName, Map<String, Operand> parameters) {
        this(functionName, parameters, null, 0);
    }

    /**
     * 创建栈帧
     *
     * @param functionName 函数名称
     * @param parameters 函数参数
     * @param parent 父栈帧
     * @param cfgNodeId CFG节点ID
     */
    public StackFrame(String functionName, Map<String, Operand> parameters,
                     StackFrame parent, int cfgNodeId) {
        this.functionName = functionName;
        this.parameters = new HashMap<>(parameters);
        this.locals = new HashMap<>();
        this.returnValue = null;
        this.programCounter = 0;
        this.parent = parent;
        this.children = new ArrayList<>();
        this.state = State.EXECUTING;
        this.cfgNodeId = cfgNodeId;
        this.depth = parent == null ? 0 : parent.depth + 1;
    }

    /**
     * 获取参数值
     *
     * @param paramName 参数名
     * @return 参数值，如果不存在返回null
     */
    public Operand getParameter(String paramName) {
        return parameters.get(paramName);
    }

    /**
     * 设置参数值
     *
     * @param paramName 参数名
     * @param value 参数值
     */
    public void setParameter(String paramName, Operand value) {
        parameters.put(paramName, value);
    }

    /**
     * 获取所有参数
     */
    public Map<String, Operand> getParameters() {
        return new HashMap<>(parameters);
    }

    /**
     * 获取局部变量值
     *
     * @param varName 变量名
     * @return 变量值，如果不存在返回null
     */
    public Operand getLocal(String varName) {
        return locals.get(varName);
    }

    /**
     * 设置局部变量值
     *
     * @param varName 变量名
     * @param value 变量值
     */
    public void setLocal(String varName, Operand value) {
        locals.put(varName, value);
    }

    /**
     * 获取所有局部变量
     */
    public Map<String, Operand> getLocals() {
        return new HashMap<>(locals);
    }

    /**
     * 获取返回值
     */
    public Operand getReturnValue() {
        return returnValue;
    }

    /**
     * 设置返回值
     *
     * @param returnValue 返回值
     */
    public void setReturnValue(Operand returnValue) {
        this.returnValue = returnValue;
    }

    /**
     * 是否有返回值
     */
    public boolean hasReturnValue() {
        return returnValue != null;
    }

    /**
     * 获取程序计数器
     */
    public int getProgramCounter() {
        return programCounter;
    }

    /**
     * 增加程序计数器
     */
    public void incrementProgramCounter() {
        this.programCounter++;
    }

    /**
     * 设置程序计数器
     */
    public void setProgramCounter(int pc) {
        this.programCounter = pc;
    }

    /**
     * 是否还有下一个子节点要执行
     *
     * @param totalChildren 总子节点数
     * @return 如果还有未执行的子节点返回true
     */
    public boolean hasNextChild(int totalChildren) {
        return programCounter < totalChildren;
    }

    /**
     * 获取下一个要执行的子节点索引
     */
    public int getNextChildIndex() {
        return programCounter;
    }

    /**
     * 添加子栈帧
     *
     * @param child 子栈帧
     */
    public void addChild(StackFrame child) {
        children.add(child);
    }

    /**
     * 获取子栈帧列表
     */
    public List<StackFrame> getChildren() {
        return new ArrayList<>(children);
    }

    /**
     * 获取子栈帧数量
     */
    public int getChildCount() {
        return children.size();
    }

    /**
     * 获取父栈帧
     */
    public StackFrame getParent() {
        return parent;
    }

    /**
     * 是否是根栈帧
     */
    public boolean isRoot() {
        return parent == null;
    }

    /**
     * 获取当前状态
     */
    public State getState() {
        return state;
    }

    /**
     * 设置状态
     */
    public void setState(State state) {
        this.state = state;
    }

    /**
     * 标记为完成
     */
    public void markCompleted() {
        this.state = State.COMPLETED;
    }

    /**
     * 标记为挂起
     */
    public void markSuspended() {
        this.state = State.SUSPENDED;
    }

    /**
     * 标记为执行中
     */
    public void markExecuting() {
        this.state = State.EXECUTING;
    }

    /**
     * 是否已完成
     */
    public boolean isCompleted() {
        return state == State.COMPLETED;
    }

    /**
     * 是否正在执行
     */
    public boolean isExecuting() {
        return state == State.EXECUTING;
    }

    /**
     * 是否挂起
     */
    public boolean isSuspended() {
        return state == State.SUSPENDED;
    }

    /**
     * 获取函数名称
     */
    public String getFunctionName() {
        return functionName;
    }

    /**
     * 获取CFG节点ID
     */
    public int getCfgNodeId() {
        return cfgNodeId;
    }

    /**
     * 获取深度
     */
    public int getDepth() {
        return depth;
    }

    /**
     * 创建此栈帧的副本
     * 用于状态保存和恢复
     */
    public StackFrame copy() {
        StackFrame copy = new StackFrame(
            this.functionName,
            new HashMap<>(this.parameters),
            this.parent,  // 注意：parent引用保持不变
            this.cfgNodeId
        );
        copy.locals.putAll(this.locals);
        copy.returnValue = this.returnValue;
        copy.programCounter = this.programCounter;
        copy.state = this.state;
        // 注意：children不复制，因为每次执行会动态生成
        return copy;
    }

    /**
     * 创建子栈帧
     *
     * @param functionName 子函数名称
     * @param parameters 子函数参数
     * @param cfgNodeId CFG节点ID
     * @return 新的子栈帧
     */
    public StackFrame createChild(String functionName, Map<String, Operand> parameters, int cfgNodeId) {
        StackFrame child = new StackFrame(functionName, parameters, this, cfgNodeId);
        this.addChild(child);
        return child;
    }

    /**
     * 合并子栈帧的返回值
     * 用于后序遍历合并递归结果
     *
     * @param childIndex 子栈帧索引
     * @param childReturnValue 子栈帧的返回值
     */
    public void mergeChildResult(int childIndex, Operand childReturnValue) {
        // 默认实现：什么都不做
        // 具体合并逻辑由子类或转换器实现
        // 例如：fib(n) = fib(n-1) + fib(n-2) 需要累加返回值
        locals.put("child_result_" + childIndex, childReturnValue);
    }

    /**
     * 获取指定子栈帧的返回值
     */
    public Operand getChildResult(int childIndex) {
        return locals.get("child_result_" + childIndex);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("StackFrame{");
        sb.append("func='").append(functionName).append('\'');
        sb.append(", depth=").append(depth);
        sb.append(", pc=").append(programCounter);
        sb.append(", state=").append(state);
        sb.append(", params=").append(parameters);
        if (returnValue != null) {
            sb.append(", return=").append(returnValue);
        }
        if (!children.isEmpty()) {
            sb.append(", children=").append(children.size());
        }
        sb.append('}');
        return sb.toString();
    }

    /**
     * 获取栈帧路径（用于调试）
     * 从根到当前栈帧的路径
     */
    public String getPath() {
        if (isRoot()) {
            return functionName;
        }
        return parent.getPath() + " -> " + functionName;
    }

    /**
     * 打印栈帧树（用于调试）
     */
    public void printTree() {
        printTree("", true);
    }

    private void printTree(String prefix, boolean isLast) {
        System.out.println(prefix + (isLast ? "└── " : "├── ") + toString());
        if (!children.isEmpty()) {
            for (int i = 0; i < children.size() - 1; i++) {
                children.get(i).printTree(prefix + (isLast ? "    " : "│   "), false);
            }
            if (children.size() > 0) {
                children.get(children.size() - 1).printTree(
                    prefix + (isLast ? "    " : "│   "), true);
            }
        }
    }

    /**
     * Builder模式创建StackFrame
     */
    public static class Builder {
        private final String functionName;
        private final Map<String, Operand> parameters = new HashMap<>();
        private StackFrame parent;
        private int cfgNodeId;

        public Builder(String functionName) {
            this.functionName = functionName;
        }

        public Builder addParameter(String name, Operand value) {
            this.parameters.put(name, value);
            return this;
        }

        public Builder setParameters(Map<String, Operand> params) {
            this.parameters.putAll(params);
            return this;
        }

        public Builder setParent(StackFrame parent) {
            this.parent = parent;
            return this;
        }

        public Builder setCfgNodeId(int cfgNodeId) {
            this.cfgNodeId = cfgNodeId;
            return this;
        }

        public StackFrame build() {
            return new StackFrame(functionName, parameters, parent, cfgNodeId);
        }
    }

    /**
     * 创建Builder
     */
    public static Builder builder(String functionName) {
        return new Builder(functionName);
    }
}
