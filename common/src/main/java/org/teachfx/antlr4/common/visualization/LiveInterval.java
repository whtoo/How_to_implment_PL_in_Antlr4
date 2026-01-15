package org.teachfx.antlr4.common.visualization;

/**
 * 活跃区间类
 * 
 * <p>该类表示寄存器分配中的活跃区间，包含变量的生命周期信息。
 * 用于线性扫描寄存器分配算法和寄存器分配可视化。</p>
 * 
 * @author TeachFX Team
 * @version 1.0
 * @since EP18R改进计划阶段一
 */
public class LiveInterval implements Comparable<LiveInterval> {
    
    /**
     * 变量名或标识符
     */
    private final String variableName;
    
    /**
     * 区间开始位置（指令序号）
     */
    private final int start;
    
    /**
     * 区间结束位置（指令序号）
     */
    private final int end;
    
    /**
     * 分配的寄存器编号
     */
    private Integer registerNumber;
    
    /**
     * 是否溢出到栈
     */
    private boolean spilled;
    
    /**
     * 优先级（用于寄存器分配决策）
     */
    private final int priority;
    
    /**
     * 使用点列表
     */
    private final java.util.List<Integer> usePoints;
    
    /**
     * 构造函数
     * 
     * @param variableName 变量名
     * @param start 开始位置
     * @param end 结束位置
     */
    public LiveInterval(String variableName, int start, int end) {
        this(variableName, start, end, 0, new java.util.ArrayList<>());
    }
    
    /**
     * 带优先级的构造函数
     * 
     * @param variableName 变量名
     * @param start 开始位置
     * @param end 结束位置
     * @param priority 优先级
     */
    public LiveInterval(String variableName, int start, int end, int priority) {
        this(variableName, start, end, priority, new java.util.ArrayList<>());
    }
    
    /**
     * 完整构造函数
     * 
     * @param variableName 变量名
     * @param start 开始位置
     * @param end 结束位置
     * @param priority 优先级
     * @param usePoints 使用点列表
     */
    public LiveInterval(String variableName, int start, int end, int priority,
                     java.util.List<Integer> usePoints) {
        if (variableName == null || variableName.trim().isEmpty()) {
            throw new IllegalArgumentException("Variable name cannot be null or empty");
        }
        if (start < 0 || end < start) {
            throw new IllegalArgumentException("Invalid range: start=" + start + ", end=" + end);
        }
        
        this.variableName = variableName;
        this.start = start;
        this.end = end;
        this.priority = priority;
        this.usePoints = usePoints != null ? new java.util.ArrayList<>(usePoints) : new java.util.ArrayList<>();
        this.registerNumber = null;
        this.spilled = false;
    }
    
    // ==================== Getters and Setters ====================
    
    /**
     * 获取变量名
     * 
     * @return 变量名
     */
    public String getVariableName() {
        return variableName;
    }
    
    /**
     * 获取开始位置
     * 
     * @return 开始位置
     */
    public int getStart() {
        return start;
    }
    
    /**
     * 获取结束位置
     * 
     * @return 结束位置
     */
    public int getEnd() {
        return end;
    }
    
    /**
     * 获取区间长度
     * 
     * @return 区间长度
     */
    public int getLength() {
        return end - start + 1;
    }
    
    /**
     * 获取分配的寄存器编号
     * 
     * @return 寄存器编号，如果未分配则返回null
     */
    public Integer getRegisterNumber() {
        return registerNumber;
    }
    
    /**
     * 设置寄存器编号
     * 
     * @param registerNumber 寄存器编号
     */
    public void setRegisterNumber(Integer registerNumber) {
        this.registerNumber = registerNumber;
        this.spilled = false;
    }
    
    /**
     * 检查是否已分配寄存器
     * 
     * @return 如果已分配寄存器返回true
     */
    public boolean hasRegister() {
        return registerNumber != null;
    }
    
    /**
     * 检查是否溢出到栈
     * 
     * @return 如果溢出返回true
     */
    public boolean isSpilled() {
        return spilled;
    }
    
    /**
     * 设置溢出状态
     * 
     * @param spilled 是否溢出
     */
    public void setSpilled(boolean spilled) {
        this.spilled = spilled;
        if (spilled) {
            this.registerNumber = null;
        }
    }
    
    /**
     * 获取优先级
     * 
     * @return 优先级
     */
    public int getPriority() {
        return priority;
    }
    
    /**
     * 获取使用点列表
     * 
     * @return 使用点列表的拷贝
     */
    public java.util.List<Integer> getUsePoints() {
        return new java.util.ArrayList<>(usePoints);
    }
    
    /**
     * 添加使用点
     * 
     * @param usePoint 使用点位置
     */
    public void addUsePoint(int usePoint) {
        if (usePoint >= start && usePoint <= end && !usePoints.contains(usePoint)) {
            usePoints.add(usePoint);
            java.util.Collections.sort(usePoints);
        }
    }
    
    /**
     * 移除使用点
     * 
     * @param usePoint 使用点位置
     * @return 如果成功移除返回true
     */
    public boolean removeUsePoint(int usePoint) {
        return usePoints.remove(Integer.valueOf(usePoint));
    }
    
    /**
     * 获取使用点数量
     * 
     * @return 使用点数量
     */
    public int getUsePointCount() {
        return usePoints.size();
    }
    
    // ==================== 区间操作方法 ====================
    
    /**
     * 检查是否与另一个区间重叠
     * 
     * @param other 另一个区间
     * @return 如果重叠返回true
     */
    public boolean overlaps(LiveInterval other) {
        return this.start <= other.end && this.end >= other.start;
    }
    
    /**
     * 检查指定位置是否在区间内
     * 
     * @param position 位置
     * @return 如果在区间内返回true
     */
    public boolean contains(int position) {
        return position >= start && position <= end;
    }
    
    /**
     * 获取与另一个区间的重叠部分
     * 
     * @param other 另一个区间
     * @return 重叠区间，如果不重叠返回null
     */
    public LiveInterval getOverlap(LiveInterval other) {
        if (!overlaps(other)) {
            return null;
        }
        
        int overlapStart = Math.max(this.start, other.start);
        int overlapEnd = Math.min(this.end, other.end);
        
        return new LiveInterval(
            "overlap_" + this.variableName + "_" + other.variableName,
            overlapStart,
            overlapEnd
        );
    }
    
    /**
     * 检查是否包含指定使用点
     * 
     * @param usePoint 使用点
     * @return 如果包含返回true
     */
    public boolean hasUsePoint(int usePoint) {
        return usePoints.contains(usePoint);
    }
    
    /**
     * 获取下一个使用点
     * 
     * @param fromPosition 起始位置
     * @return 下一个使用点，如果没有返回-1
     */
    public int getNextUsePoint(int fromPosition) {
        for (int usePoint : usePoints) {
            if (usePoint >= fromPosition) {
                return usePoint;
            }
        }
        return -1;
    }
    
    /**
     * 计算活跃密度（使用点数/区间长度）
     * 
     * @return 活跃密度
     */
    public double getDensity() {
        return getLength() > 0 ? (double) getUsePointCount() / getLength() : 0.0;
    }
    
    // ==================== 比较和排序 ====================
    
    @Override
    public int compareTo(LiveInterval other) {
        // 按开始位置排序
        int startCompare = Integer.compare(this.start, other.start);
        if (startCompare != 0) {
            return startCompare;
        }
        
        // 开始位置相同则按结束位置排序
        int endCompare = Integer.compare(this.end, other.end);
        if (endCompare != 0) {
            return endCompare;
        }
        
        // 结束位置相同则按变量名排序
        return this.variableName.compareTo(other.variableName);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        LiveInterval that = (LiveInterval) obj;
        return start == that.start &&
               end == that.end &&
               variableName.equals(that.variableName);
    }
    
    @Override
    public int hashCode() {
        return java.util.Objects.hash(variableName, start, end);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("LiveInterval{");
        sb.append("var='").append(variableName).append("'");
        sb.append(", range=[").append(start).append("-").append(end).append("]");
        
        if (hasRegister()) {
            sb.append(", reg=").append(registerNumber);
        } else if (spilled) {
            sb.append(", spilled=true");
        }
        
        if (!usePoints.isEmpty()) {
            sb.append(", uses=").append(usePoints);
        }
        
        sb.append("}");
        return sb.toString();
    }
    
    /**
     * 获取详细描述
     * 
     * @return 详细描述字符串
     */
    public String getDetailedDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("Variable: ").append(variableName).append("\n");
        sb.append("Live Range: ").append(start).append(" - ").append(end).append("\n");
        sb.append("Length: ").append(getLength()).append(" instructions\n");
        sb.append("Priority: ").append(priority).append("\n");
        sb.append("Use Points: ").append(usePoints).append("\n");
        sb.append("Density: ").append(String.format("%.3f", getDensity())).append("\n");
        
        if (hasRegister()) {
            sb.append("Register: r").append(registerNumber).append("\n");
        } else if (spilled) {
            sb.append("Status: Spilled to stack\n");
        } else {
            sb.append("Status: Unallocated\n");
        }
        
        return sb.toString();
    }
    
    // ==================== 静态工厂方法 ====================
    
    /**
     * 创建临时变量的活跃区间
     * 
     * @param start 开始位置
     * @param end 结束位置
     * @return 活跃区间
     */
    public static LiveInterval createTemporary(int start, int end) {
        return new LiveInterval("temp_" + start + "_" + end, start, end);
    }
    
    /**
     * 创建参数的活跃区间
     * 
     * @param paramIndex 参数索引
     * @param start 开始位置
     * @param end 结束位置
     * @return 活跃区间
     */
    public static LiveInterval createParameter(int paramIndex, int start, int end) {
        return new LiveInterval("param_" + paramIndex, start, end, 10);
    }
    
    /**
     * 合并两个重叠的活跃区间
     * 
     * @param first 第一个区间
     * @param second 第二个区间
     * @return 合并后的区间
     * @throws IllegalArgumentException 如果区间不重叠
     */
    public static LiveInterval merge(LiveInterval first, LiveInterval second) {
        if (!first.overlaps(second)) {
            throw new IllegalArgumentException("Intervals do not overlap");
        }
        
        int mergedStart = Math.min(first.start, second.start);
        int mergedEnd = Math.max(first.end, second.end);
        
        LiveInterval merged = new LiveInterval(
            "merged_" + first.variableName + "_" + second.variableName,
            mergedStart,
            mergedEnd,
            Math.max(first.priority, second.priority)
        );
        
        // 合并使用点
        for (int usePoint : first.usePoints) {
            merged.addUsePoint(usePoint);
        }
        for (int usePoint : second.usePoints) {
            merged.addUsePoint(usePoint);
        }
        
        return merged;
    }
}