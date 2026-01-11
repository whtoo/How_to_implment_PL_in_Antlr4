package org.teachfx.antlr4.ep18r.stackvm.config;

public interface IVMConfig {

    int getHeapSize();

    int getLocalsSize();

    int getMaxCallStackDepth();

    int getMaxExecutionSteps();

    boolean isDebugMode();

    boolean isTraceEnabled();

    boolean isVerboseErrors();

    int getInstructionCacheSize();

    boolean isEnableBoundsCheck();

    boolean isEnableTypeCheck();
}
