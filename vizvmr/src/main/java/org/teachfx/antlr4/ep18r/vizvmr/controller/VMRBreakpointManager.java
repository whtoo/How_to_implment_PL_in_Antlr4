package org.teachfx.antlr4.ep18r.vizvmr.controller;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * 断点管理器
 * 支持设置/清除断点、条件断点、断点命中时暂停执行
 */
public class VMRBreakpointManager {
    private final Set<Integer> breakpoints;
    private final Set<Integer> disabledBreakpoints;
    private final Set<ConditionalBreakpoint> conditionalBreakpoints;
    private int hitCount;
    private int totalHits;

    public VMRBreakpointManager() {
        this.breakpoints = new HashSet<>();
        this.disabledBreakpoints = new HashSet<>();
        this.conditionalBreakpoints = new HashSet<>();
        this.hitCount = 0;
        this.totalHits = 0;
    }

    /**
     * 设置断点
     */
    public void setBreakpoint(int pc) {
        breakpoints.add(pc);
    }

    /**
     * 清除断点
     */
    public void clearBreakpoint(int pc) {
        breakpoints.remove(pc);
        disabledBreakpoints.remove(pc);
        conditionalBreakpoints.removeIf(bp -> bp.pc == pc);
    }

    /**
     * 切换断点
     */
    public void toggleBreakpoint(int pc) {
        if (breakpoints.contains(pc)) {
            clearBreakpoint(pc);
        } else {
            setBreakpoint(pc);
        }
    }

    /**
     * 禁用断点
     */
    public void disableBreakpoint(int pc) {
        if (breakpoints.contains(pc)) {
            disabledBreakpoints.add(pc);
        }
    }

    /**
     * 启用断点
     */
    public void enableBreakpoint(int pc) {
        disabledBreakpoints.remove(pc);
    }

    /**
     * 设置条件断点
     */
    public void setConditionalBreakpoint(int pc, Predicate<Integer> condition) {
        if (breakpoints.contains(pc)) {
            conditionalBreakpoints.add(new ConditionalBreakpoint(pc, condition));
        }
    }

    /**
     * 检查是否应该暂停
     */
    public boolean shouldPause(int pc) {
        if (!breakpoints.contains(pc) || disabledBreakpoints.contains(pc)) {
            return false;
        }

        totalHits++;

        // 检查条件断点
        for (ConditionalBreakpoint conditional : conditionalBreakpoints) {
            if (conditional.pc == pc) {
                if (conditional.condition.test(pc)) {
                    hitCount++;
                    return true;
                }
                return false;
            }
        }

        // 无条件断点
        hitCount++;
        return true;
    }

    /**
     * 获取所有断点
     */
    public Set<Integer> getBreakpoints() {
        return new HashSet<>(breakpoints);
    }

    /**
     * 获取禁用的断点
     */
    public Set<Integer> getDisabledBreakpoints() {
        return new HashSet<>(disabledBreakpoints);
    }

    /**
     * 获取条件断点
     */
    public Set<ConditionalBreakpoint> getConditionalBreakpoints() {
        return new HashSet<>(conditionalBreakpoints);
    }

    /**
     * 清除所有断点
     */
    public void clearAllBreakpoints() {
        breakpoints.clear();
        disabledBreakpoints.clear();
        conditionalBreakpoints.clear();
    }

    /**
     * 获取命中次数
     */
    public int getHitCount() {
        return hitCount;
    }

    /**
     * 获取总命中次数
     */
    public int getTotalHits() {
        return totalHits;
    }

    /**
     * 重置命中计数
     */
    public void resetHitCount() {
        hitCount = 0;
        totalHits = 0;
    }

    /**
     * 检查是否有断点
     */
    public boolean hasBreakpoints() {
        return !breakpoints.isEmpty();
    }

    /**
     * 获取断点数量
     */
    public int getBreakpointCount() {
        return breakpoints.size();
    }

    /**
     * 内部类：条件断点
     */
    public static class ConditionalBreakpoint {
        public final int pc;
        public final Predicate<Integer> condition;

        public ConditionalBreakpoint(int pc, Predicate<Integer> condition) {
            this.pc = pc;
            this.condition = condition;
        }
    }
}
