package org.teachfx.antlr4.ep18r.stackvm.interpreter;

/**
 * 可视化监听器接口
 * 用于监听虚拟机执行过程中的事件
 */
public interface VisualizationListener {
    void beforeInstructionExecute(int pc, int opcode, String instruction);
    void afterInstructionExecute(int pc, int opcode, String instruction, int[] registers);
    void onPause(int pc);
    void onResume(int pc);
    void onBreakpointHit(int pc);
    void onRegisterChange(int regNum, int oldValue, int newValue);
    void onMemoryChange(int address, int oldValue, int newValue);
}
