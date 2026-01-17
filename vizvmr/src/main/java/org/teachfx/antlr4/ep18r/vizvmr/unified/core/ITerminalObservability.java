package org.teachfx.antlr4.ep18r.vizvmr.unified.core;

import io.reactivex.rxjava3.core.Observable;
import org.teachfx.antlr4.ep18r.vizvmr.unified.event.VMEvent;

import java.util.List;

/**
 * 终端可观测性接口
 *
 * <p>提供虚拟机执行的完整可观测性，用于：</p>
 * <ul>
 *   <li>单元测试中的状态验证</li>
 *   <li>UI集成测试中的问题追踪</li>
 *   <li>调试时的详细日志</li>
 * </ul>
 */
public interface ITerminalObservability {

    // ==================== 事件日志 ====================

    /**
     * 获取完整事件日志
     * @return 按时间顺序排列的所有事件
     */
    List<VMEvent> getEventLog();

    /**
     * 获取事件流
     * @return 事件的可观察流
     */
    Observable<VMEvent> eventStream();

    /**
     * 清除事件日志
     */
    void clearEventLog();

    /**
     * 获取事件计数
     * @return 已记录的事件数量
     */
    int getEventCount();

    // ==================== 状态快照 ====================

    /**
     * 捕获当前状态快照
     * @return 状态快照
     */
    StateSnapshot captureSnapshot();

    /**
     * 获取所有历史快照
     * @return 快照列表
     */
    List<StateSnapshot> getAllSnapshots();

    /**
     * 清除所有快照
     */
    void clearSnapshots();

    /**
     * 恢复到指定快照
     * @param snapshot 要恢复的快照
     */
    void restoreSnapshot(StateSnapshot snapshot);

    // ==================== 性能指标 ====================

    /**
     * 获取已执行的指令数
     * @return 指令计数
     */
    long getInstructionCount();

    /**
     * 获取执行时长（毫秒）
     * @return 执行时长
     */
    long getExecutionDuration();

    /**
     * 获取平均指令执行时间（微秒）
     * @return 平均指令时间
     */
    double getAverageInstructionTime();

    /**
     * 获取断点命中次数
     * @return 命中次数
     */
    int getBreakpointHitCount();

    // ==================== 断言工具 ====================

    /**
     * 获取测试断言工具
     * @return 断言工具
     */
    AssertionHelper assertions();

    /**
     * 检查是否达到指定状态
     * @param expectedState 期望的VM状态
     * @return 是否达到
     */
    boolean isStateReached(VMTypes.VMState expectedState);

    /**
     * 检查是否执行到指定PC
     * @param expectedPC 期望的PC值
     * @return 是否到达
     */
    boolean isPCReached(int expectedPC);

    /**
     * 检查是否包含指定事件
     * @param eventType 事件类型
     * @return 是否包含
     */
    boolean hasEvent(Class<? extends VMEvent> eventType);

    // ==================== 诊断信息 ====================

    /**
     * 获取诊断报告
     * @return 诊断报告文本
     */
    String getDiagnosticReport();

    /**
     * 打印完整执行历史
     */
    void printExecutionHistory();

    /**
     * 打印当前状态摘要
     */
    void printStateSummary();

    /**
     * 获取详细状态信息
     * @return 状态信息JSON格式
     */
    String getStateDetails();

    // ==================== 内部类 ====================

    /**
     * 状态快照
     */
    class StateSnapshot {
        private final int[] registers;
        private final int[] heap;
        private final int[] globals;
        private final int pc;
        private final int callStackDepth;
        private final VMTypes.VMState state;
        private final long timestamp;
        private final long executionSteps;

        public StateSnapshot(int[] registers, int[] heap, int[] globals,
                           int pc, int callStackDepth,
                           VMTypes.VMState state,
                           long timestamp, long executionSteps) {
            this.registers = registers.clone();
            this.heap = heap.clone();
            this.globals = globals.clone();
            this.pc = pc;
            this.callStackDepth = callStackDepth;
            this.state = state;
            this.timestamp = timestamp;
            this.executionSteps = executionSteps;
        }

        public int[] getRegisters() { return registers; }
        public int[] getHeap() { return heap; }
        public int[] getGlobals() { return globals; }
        public int getPC() { return pc; }
        public int getCallStackDepth() { return callStackDepth; }
        public VMTypes.VMState getState() { return state; }
        public long getTimestamp() { return timestamp; }
        public long getExecutionSteps() { return executionSteps; }
    }

    /**
     * 断言辅助工具
     */
    class AssertionHelper {
        private final List<VMEvent> events;
        private final ITerminalObservability observability;

        public AssertionHelper(List<VMEvent> events, ITerminalObservability observability) {
            this.events = events;
            this.observability = observability;
        }

        public AssertionHelper assertEventCount(int expectedCount) {
            int actualCount = events.size();
            if (actualCount != expectedCount) {
                throw new AssertionError(
                    String.format("Expected %d events, but got %d", expectedCount, actualCount));
            }
            return this;
        }

        public AssertionHelper assertStateReached(VMTypes.VMState expectedState) {
            if (!observability.isStateReached(expectedState)) {
                throw new AssertionError(
                    String.format("Expected state %s, but not reached", expectedState));
            }
            return this;
        }

        public AssertionHelper assertPCReached(int expectedPC) {
            if (!observability.isPCReached(expectedPC)) {
                throw new AssertionError(
                    String.format("Expected PC %d, but not reached", expectedPC));
            }
            return this;
        }

        public AssertionHelper assertEventExists(Class<? extends VMEvent> eventType) {
            if (!observability.hasEvent(eventType)) {
                throw new AssertionError(
                    String.format("Expected event of type %s, but not found", eventType.getSimpleName()));
            }
            return this;
        }

        public AssertionHelper assertRegisterValue(int regNum, int expectedValue) {
            int[] regs = observability.captureSnapshot().getRegisters();
            if (regNum < 0 || regNum >= regs.length) {
                throw new IllegalArgumentException("Invalid register number: " + regNum);
            }
            int actualValue = regs[regNum];
            if (actualValue != expectedValue) {
                throw new AssertionError(
                    String.format("Expected register r%d = %d, but got %d", regNum, expectedValue, actualValue));
            }
            return this;
        }

        public AssertionHelper assertExecutionSteps(long expectedSteps) {
            long actualSteps = observability.captureSnapshot().getExecutionSteps();
            if (actualSteps != expectedSteps) {
                throw new AssertionError(
                    String.format("Expected %d execution steps, but got %d", expectedSteps, actualSteps));
            }
            return this;
        }
    }
}
