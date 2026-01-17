package org.teachfx.antlr4.ep18r.vizvmr.unified.bridge;

import org.teachfx.antlr4.ep18r.vizvmr.unified.core.VMTypes;
import org.teachfx.antlr4.ep18r.vizvmr.unified.core.VMTypes.Command;
import org.teachfx.antlr4.ep18r.vizvmr.unified.event.IVMEventBus;
import org.teachfx.antlr4.ep18r.vizvmr.unified.event.VMEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 统一事件适配器
 *
 * <p>整合所有事件源到统一事件总线：</p>
 * <ul>
 *   <li>stackvm的VisualizationListener</li>
 *   <li>common的ExecutionListener</li>
 *   <li>common的StateChangeListener</li>
 * </ul>
 */
public class UnifiedEventAdapter implements org.teachfx.antlr4.ep18r.stackvm.interpreter.VisualizationListener {

    private static final Logger logger = LogManager.getLogger(UnifiedEventAdapter.class);

    private final IVMEventBus eventBus;

    public UnifiedEventAdapter(IVMEventBus eventBus) {
        this.eventBus = eventBus;
    }

    // ==================== VisualizationListener实现 ====================

    @Override
    public void beforeInstructionExecute(int pc, int opcode, String instruction) {
        logger.debug("指令执行前: PC={}, Opcode={}, Instruction={}", pc, opcode, instruction);
    }

    @Override
    public void afterInstructionExecute(int pc, int opcode, String instruction, int[] registers) {
        logger.debug("指令执行后: PC={}, Opcode={}, Instruction={}", pc, opcode, instruction);
        VMEvent event = new VMEvent.InstructionExecutedEvent(pc, opcode, instruction, "");
        eventBus.publish(event);
    }

    @Override
    public void onPause(int pc) {
        logger.debug("VM暂停: PC={}", pc);
        VMEvent event = new VMEvent.StateChangedEvent(
            VMTypes.VMState.RUNNING,
            VMTypes.VMState.PAUSED
        );
        eventBus.publish(event);
    }

    @Override
    public void onResume(int pc) {
        logger.debug("VM恢复: PC={}", pc);
        VMEvent event = new VMEvent.StateChangedEvent(
            VMTypes.VMState.PAUSED,
            VMTypes.VMState.RUNNING
        );
        eventBus.publish(event);
    }

    @Override
    public void onBreakpointHit(int pc) {
        logger.info("断点命中: PC={}", pc);
        VMEvent event = new VMEvent.BreakpointHitEvent(pc);
        eventBus.publish(event);
    }

    @Override
    public void onRegisterChange(int regNum, int oldValue, int newValue) {
        logger.debug("寄存器变化: r{}: {} -> {}", regNum, oldValue, newValue);
        VMEvent event = new VMEvent.RegisterChangedEvent(regNum, oldValue, newValue);
        eventBus.publish(event);
    }

    @Override
    public void onMemoryChange(int address, int oldValue, int newValue) {
        logger.debug("内存变化: [0x{}]: {} -> {}",
            Integer.toHexString(address), oldValue, newValue);
        VMEvent event = new VMEvent.MemoryChangedEvent(
            VMEvent.MemoryChangedEvent.MemoryType.HEAP,
            address, oldValue, newValue
        );
        eventBus.publish(event);
    }

    /**
     * 发布状态变化事件
     */
    public void publishStateChange(VMTypes.VMState oldState, VMTypes.VMState newState) {
        logger.info("状态变化: {} -> {}", oldState, newState);
        VMEvent event = new VMEvent.StateChangedEvent(oldState, newState);
        eventBus.publish(event);
    }

    /**
     * 发布PC变化事件
     */
    public void publishPCChange(int oldPC, int newPC) {
        logger.debug("PC变化: {} -> {}", oldPC, newPC);
        VMEvent event = new VMEvent.PCChangedEvent(oldPC, newPC);
        eventBus.publish(event);
    }

    /**
     * 发布执行开始事件
     */
    public void publishExecutionStarted() {
        logger.info("执行开始");
        VMEvent event = new VMEvent.StateChangedEvent(
            VMTypes.VMState.READY,
            VMTypes.VMState.RUNNING
        );
        eventBus.publish(event);
    }

    /**
     * 发布执行停止事件
     */
    public void publishExecutionStopped() {
        logger.info("执行停止");
        VMEvent event = new VMEvent.StateChangedEvent(
            VMTypes.VMState.RUNNING,
            VMTypes.VMState.HALTED
        );
        eventBus.publish(event);
    }
}
