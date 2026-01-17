package org.teachfx.antlr4.ep18r.vizvmr.unified.core;

import org.teachfx.antlr4.ep18r.stackvm.interpreter.VisualizationListener;

/**
 * 虚拟机抽象接口
 *
 * <p>定义虚拟机的基本操作接口，避免直接依赖RegisterVMInterpreter</p>
 */
public interface IVM {

    void exec() throws Exception;

    void step();

    void loadCode(byte[] bytecode);

    void setPaused(boolean paused);

    boolean isPaused();

    void setStepMode(boolean stepMode);

    boolean isStepMode();

    void setAutoStepMode(boolean autoStepMode);

    void setAutoStepDelay(int delayMs);

    int getAutoStepDelay();

    void addBreakpoint(int pc);

    void removeBreakpoint(int pc);

    java.util.Set<Integer> getBreakpoints();

    boolean hasBreakpoints();

    /**
     * 获取寄存器值
     */
    int getRegister(int regNum);

    /**
     * 读取堆内存
     */
    int readHeap(int address);

    /**
     * 读取全局变量
     */
    Object readGlobal(int address);

    /**
     * 获取程序计数器
     */
    int getProgramCounter();

    /**
     * 添加可视化监听器
     */
    void addVisualizationListener(VisualizationListener listener);
}
