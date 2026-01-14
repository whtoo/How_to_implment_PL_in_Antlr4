# EP18R 可视化模块 (vizvmr) 测试标准

## 📋 概述

本文档定义了 **vizvmr** 模块的测试规范、测试策略和测试标准。vizvmr 是一个可视化模块，包含事件系统、状态模型、GUI组件和虚拟机集成，需要针对不同层次制定相应的测试策略。

## 🎯 测试策略

### 1.1 测试金字塔
```
            UI测试 (5%)
               ^
               |
        集成测试 (25%)
               ^
               |
        单元测试 (70%) - 基础测试
```

- **单元测试 (70%)**: 测试单个类或方法，关注隔离性和确定性
- **集成测试 (25%)**: 测试组件间交互和事件系统
- **UI测试 (5%)**: 测试图形用户界面和用户交互

### 1.2 测试覆盖率要求

| 测试类型 | 覆盖率目标 | 验证方法 | 关键模块 |
|---------|-----------|----------|----------|
| **整体覆盖率** | ≥ 80% | JaCoCo 报告 | 全部模块 |
| **核心模块** | ≥ 90% | JaCoCo 报告 | 事件系统、状态模型 |
| **GUI组件** | ≥ 70% | 功能测试 | 面板组件、控制组件 |
| **集成层** | ≥ 85% | 集成测试 | 桥接器、插桩适配器 |
| **新功能模块** | 100% | TDD 要求 | 新开发的组件 |

### 1.3 测试命名规范

#### 功能测试
```java
@Test
@DisplayName("应该正确更新寄存器值并触发事件")
void testRegisterUpdate_ShouldFireEvent() {
    // 测试代码
}

@Test  
@DisplayName("应该正确同步虚拟机状态到模型")
void testVMStateSync_ShouldUpdateModel() {
    // 测试代码
}
```

#### 边界条件测试
```java
@Test
@DisplayName("应该处理空程序计数器变化")
void testNullPCChange_ShouldHandleGracefully() {
    // 测试代码
}

@Test
@DisplayName("应该正确处理无效寄存器索引")
void testInvalidRegisterIndex_ShouldThrowException() {
    // 测试代码
}
```

#### 异常场景测试
```java
@Test
@DisplayName("应该处理虚拟机执行异常")
void testVMExecutionError_ShouldNotifyListeners() {
    // 测试代码
}

@Test
@DisplayName("应该处理反射访问失败")
void testReflectionFailure_ShouldHandleGracefully() {
    // 测试代码
}
```

#### GUI测试
```java
@Test
@DisplayName("应该正确渲染寄存器面板")
void testRegisterPanel_Rendering() {
    // 测试代码
}

@Test  
@DisplayName("应该响应按钮点击事件")
void testControlPanel_ButtonClick() {
    // 测试代码
}
```

## 📝 单元测试模板

### 2.1 事件系统测试模板

#### VMRStateListenerTest.java
```java
package org.teachfx.antlr4.ep18r.vizvmr.event;

import org.junit.jupiter.api.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("状态监听器测试")
class VMRStateListenerTest {
    
    private VMRStateListener listener;
    private RegisterChangeEvent registerEvent;
    private MemoryChangeEvent memoryEvent;
    private PCChangeEvent pcEvent;
    
    @BeforeEach
    void setUp() {
        // 创建模拟监听器和事件
        listener = mock(VMRStateListener.class);
        registerEvent = new RegisterChangeEvent(this, 1, 0, 100, 200, "r1");
        memoryEvent = new MemoryChangeEvent(this, 2, 0x1000, 0, 0x12345678, MemoryType.HEAP);
        pcEvent = new PCChangeEvent(this, 3, 0x100, 0x104);
    }
    
    @Test
    @DisplayName("应该正确接收寄存器变化事件")
    void testRegisterChanged_ShouldBeCalled() {
        // 触发事件
        listener.registerChanged(registerEvent);
        
        // 验证监听器被调用
        verify(listener, times(1)).registerChanged(registerEvent);
    }
    
    @Test
    @DisplayName("应该正确处理批量寄存器更新")
    void testRegistersUpdated_ShouldHandleMultipleEvents() {
        RegisterChangeEvent[] events = {registerEvent, registerEvent};
        
        // 触发批量更新
        listener.registersUpdated(events);
        
        // 验证每个事件都被处理
        verify(listener, times(2)).registerChanged(any(RegisterChangeEvent.class));
    }
    
    @Test
    @DisplayName("应该正确接收内存变化事件")
    void testMemoryChanged_ShouldBeCalled() {
        listener.memoryChanged(memoryEvent);
        verify(listener, times(1)).memoryChanged(memoryEvent);
    }
    
    @Test
    @DisplayName("应该正确接收PC变化事件")
    void testPCChanged_ShouldBeCalled() {
        listener.pcChanged(pcEvent);
        verify(listener, times(1)).pcChanged(pcEvent);
    }
}
```

#### VMRExecutionListenerTest.java
```java
package org.teachfx.antlr4.ep18r.vizvmr.event;

import org.junit.jupiter.api.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("执行监听器测试")
class VMRExecutionListenerTest {
    
    private VMRExecutionListener listener;
    private InstructionExecutionEvent instructionEvent;
    private VMStateChangeEvent stateEvent;
    
    @BeforeEach
    void setUp() {
        listener = mock(VMRExecutionListener.class);
        instructionEvent = new InstructionExecutionEvent(this, 1, 0x100, 
            "li r1, 100", InstructionPhase.EXECUTE);
        stateEvent = new VMStateChangeEvent(this, 2, 
            VMStateChangeEvent.State.RUNNING, VMStateChangeEvent.State.PAUSED);
    }
    
    @Test
    @DisplayName("应该接收指令执行事件")
    void testInstructionEvents_ShouldBeCalled() {
        // 测试不同阶段的指令事件
        listener.beforeInstructionDecode(instructionEvent);
        listener.beforeInstructionExecute(instructionEvent);
        listener.afterInstructionExecute(instructionEvent);
        
        verify(listener, times(1)).beforeInstructionDecode(instructionEvent);
        verify(listener, times(1)).beforeInstructionExecute(instructionEvent);
        verify(listener, times(1)).afterInstructionExecute(instructionEvent);
    }
    
    @Test
    @DisplayName("应该接收虚拟机状态变化事件")
    void testVMStateChanged_ShouldBeCalled() {
        listener.vmStateChanged(stateEvent);
        verify(listener, times(1)).vmStateChanged(stateEvent);
    }
    
    @Test
    @DisplayName("应该处理执行异常")
    void testExecutionError_ShouldHandleException() {
        Throwable error = new RuntimeException("Test error");
        listener.executionError(error, 0x100);
        // 验证默认实现被调用（打印错误信息）
    }
}
```

### 2.2 状态模型测试模板

#### VMRStateModelTest.java
```java
package org.teachfx.antlr4.ep18r.vizvmr.core;

import org.junit.jupiter.api.*;
import org.teachfx.antlr4.ep18r.stackvm.StackFrame;
import org.teachfx.antlr4.ep18r.vizvmr.event.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("状态模型测试")
class VMRStateModelTest {
    
    private VMRStateModel stateModel;
    private VMRStateListener stateListener;
    private VMRExecutionListener executionListener;
    
    @BeforeEach
    void setUp() {
        // 创建状态模型（小尺寸用于测试）
        stateModel = new VMRStateModel(1024, 256, 32);
        stateListener = mock(VMRStateListener.class);
        executionListener = mock(VMRExecutionListener.class);
        
        stateModel.addStateListener(stateListener);
        stateModel.addExecutionListener(executionListener);
    }
    
    @Test
    @DisplayName("应该正确更新寄存器值")
    void testUpdateRegister_ShouldUpdateValueAndFireEvent() {
        // 初始值
        int registerIndex = 1;
        int newValue = 0x12345678;
        
        // 更新寄存器
        stateModel.updateRegister(registerIndex, newValue);
        
        // 验证值已更新
        assertThat(stateModel.getRegister(registerIndex)).isEqualTo(newValue);
        
        // 验证事件被触发
        verify(stateListener, times(1)).registerChanged(any(RegisterChangeEvent.class));
    }
    
    @Test
    @DisplayName("应该正确更新内存值")
    void testUpdateMemory_ShouldUpdateValueAndFireEvent() {
        int address = 0x1000;
        int newValue = 0xdeadbeef;
        
        stateModel.updateMemory(address, newValue);
        
        assertThat(stateModel.getMemory(address)).isEqualTo(newValue);
        verify(stateListener, times(1)).memoryChanged(any(MemoryChangeEvent.class));
    }
    
    @Test
    @DisplayName("应该正确更新程序计数器")
    void testUpdateProgramCounter_ShouldUpdatePCAndFireEvent() {
        int oldPC = 0x100;
        int newPC = 0x104;
        
        stateModel.setProgramCounter(oldPC);
        stateModel.updateProgramCounter(newPC);
        
        assertThat(stateModel.getProgramCounter()).isEqualTo(newPC);
        verify(stateListener, times(1)).pcChanged(any(PCChangeEvent.class));
    }
    
    @Test
    @DisplayName("应该正确处理调用栈操作")
    void testCallStackOperations_ShouldManageStackFrames() {
        StackFrame frame1 = new StackFrame("func1", 0x200, 10);
        StackFrame frame2 = new StackFrame("func2", 0x300, 20);
        
        // 推入栈帧
        stateModel.pushStackFrame(frame1);
        stateModel.pushStackFrame(frame2);
        
        assertThat(stateModel.getCallStackDepth()).isEqualTo(2);
        assertThat(stateModel.getCurrentStackFrame()).isEqualTo(frame2);
        
        // 弹出栈帧
        stateModel.popStackFrame();
        
        assertThat(stateModel.getCallStackDepth()).isEqualTo(1);
        assertThat(stateModel.getCurrentStackFrame()).isEqualTo(frame1);
    }
    
    @Test
    @DisplayName("应该批量更新寄存器以提高性能")
    void testBatchRegisterUpdate_ShouldFireSingleEvent() {
        RegisterChangeEvent[] events = new RegisterChangeEvent[3];
        for (int i = 0; i < 3; i++) {
            events[i] = new RegisterChangeEvent(this, i, 0, i * 100, i * 200, "r" + i);
        }
        
        stateListener.registersUpdated(events);
        
        // 验证批量更新被处理
        verify(stateListener, times(3)).registerChanged(any(RegisterChangeEvent.class));
    }
    
    @Test
    @DisplayName("应该处理无效寄存器索引")
    void testInvalidRegisterIndex_ShouldThrowException() {
        assertThatThrownBy(() -> stateModel.updateRegister(16, 100))
            .isInstanceOf(IndexOutOfBoundsException.class);
        
        assertThatThrownBy(() -> stateModel.updateRegister(-1, 100))
            .isInstanceOf(IndexOutOfBoundsException.class);
    }
    
    @Test
    @DisplayName("应该处理无效内存地址")
    void testInvalidMemoryAddress_ShouldThrowException() {
        assertThatThrownBy(() -> stateModel.updateMemory(-1, 100))
            .isInstanceOf(IndexOutOfBoundsException.class);
        
        assertThatThrownBy(() -> stateModel.updateMemory(1024, 100))
            .isInstanceOf(IndexOutOfBoundsException.class);
    }
    
    @Test
    @DisplayName("应该正确管理修改标记")
    void testModificationTracking_ShouldTrackChanges() {
        // 更新寄存器
        stateModel.updateRegister(1, 100);
        stateModel.updateRegister(2, 200);
        
        // 验证修改标记
        assertThat(stateModel.isRegisterModified(1)).isTrue();
        assertThat(stateModel.isRegisterModified(2)).isTrue();
        assertThat(stateModel.isRegisterModified(3)).isFalse();
        
        // 清除修改标记
        stateModel.clearModificationFlags();
        
        assertThat(stateModel.isRegisterModified(1)).isFalse();
        assertThat(stateModel.isRegisterModified(2)).isFalse();
    }
    
    @Test
    @DisplayName("应该正确统计执行信息")
    void testExecutionStatistics_ShouldTrackCounts() {
        // 模拟指令执行
        stateModel.incrementExecutionSteps();
        stateModel.incrementExecutionSteps();
        
        assertThat(stateModel.getExecutionSteps()).isEqualTo(2);
        assertThat(stateModel.getStartTime()).isGreaterThan(0);
        
        // 测试重置
        stateModel.resetStatistics();
        
        assertThat(stateModel.getExecutionSteps()).isEqualTo(0);
    }
}
```

### 2.3 集成层测试模板

#### VMRInstrumentationTest.java
```java
package org.teachfx.antlr4.ep18r.vizvmr.integration;

import org.junit.jupiter.api.*;
import org.teachfx.antlr4.ep18r.stackvm.config.VMConfig;
import org.teachfx.antlr4.ep18r.stackvm.interpreter.RegisterVMInterpreter;
import org.teachfx.antlr4.ep18r.vizvmr.core.VMRStateModel;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("虚拟机插桩适配器测试")
class VMRInstrumentationTest {
    
    private RegisterVMInterpreter vm;
    private VMRStateModel stateModel;
    private VMRInstrumentation instrumentation;
    
    @BeforeEach
    void setUp() {
        // 创建虚拟机实例
        VMConfig config = new VMConfig.Builder()
            .heapSize(1024)
            .localsSize(256)
            .maxCallStackDepth(32)
            .build();
        vm = new RegisterVMInterpreter(config);
        
        // 创建状态模型
        stateModel = new VMRStateModel(1024, 256, 32);
        
        // 创建插桩适配器
        instrumentation = new VMRInstrumentation(vm, stateModel);
    }
    
    @Test
    @DisplayName("应该成功执行插桩")
    void testInstrument_ShouldSucceed() {
        assertThatCode(() -> instrumentation.instrument())
            .doesNotThrowAnyException();
        
        assertThat(instrumentation.isInstrumented()).isTrue();
    }
    
    @Test
    @DisplayName("应该同步寄存器状态")
    void testSyncRegisters_ShouldUpdateModel() {
        // 执行插桩
        instrumentation.instrument();
        
        // 同步状态
        instrumentation.syncRegisters();
        
        // 验证状态模型已更新
        // 注意：由于是反射访问，具体值取决于虚拟机初始状态
        assertThat(stateModel.getRegister(0)).isEqualTo(0); // r0 应该是0
    }
    
    @Test
    @DisplayName("应该同步内存状态")
    void testSyncMemory_ShouldUpdateModel() {
        instrumentation.instrument();
        instrumentation.syncMemory();
        
        // 验证内存状态已同步
        // 具体断言取决于实现
    }
    
    @Test
    @DisplayName("应该同步调用栈状态")
    void testSyncCallStack_ShouldUpdateModel() {
        instrumentation.instrument();
        instrumentation.syncCallStack();
        
        // 验证调用栈状态已同步
        assertThat(stateModel.getCallStackDepth()).isGreaterThanOrEqualTo(0);
    }
    
    @Test
    @DisplayName("应该同步程序计数器")
    void testSyncProgramCounter_ShouldUpdateModel() {
        instrumentation.instrument();
        instrumentation.syncProgramCounter();
        
        // 验证PC已同步
        assertThat(stateModel.getProgramCounter()).isGreaterThanOrEqualTo(0);
    }
    
    @Test
    @DisplayName("应该处理重复插桩")
    void testDuplicateInstrumentation_ShouldNotFail() {
        // 第一次插桩
        instrumentation.instrument();
        
        // 第二次插桩（应该被忽略）
        assertThatCode(() -> instrumentation.instrument())
            .doesNotThrowAnyException();
    }
    
    @Test
    @DisplayName("应该处理无效虚拟机实例")
    void testInvalidVMInstance_ShouldHandleGracefully() {
        VMRInstrumentation invalidInstrumentation = 
            new VMRInstrumentation(null, stateModel);
        
        assertThatThrownBy(() -> invalidInstrumentation.instrument())
            .isInstanceOf(NullPointerException.class);
    }
}
```

#### VMRVisualBridgeTest.java
```java
package org.teachfx.antlr4.ep18r.vizvmr.integration;

import org.junit.jupiter.api.*;
import org.teachfx.antlr4.ep18r.stackvm.config.VMConfig;
import org.teachfx.antlr4.ep18r.stackvm.interpreter.RegisterVMInterpreter;
import org.teachfx.antlr4.ep18r.vizvmr.core.VMRStateModel;
import org.teachfx.antlr4.ep18r.vizvmr.event.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("可视化桥接器测试")
class VMRVisualBridgeTest {
    
    private RegisterVMInterpreter vm;
    private VMRStateModel stateModel;
    private VMRVisualBridge bridge;
    private VMRStateListener stateListener;
    private VMRExecutionListener executionListener;
    
    @BeforeEach
    void setUp() {
        // 创建虚拟机实例
        VMConfig config = new VMConfig.Builder()
            .heapSize(1024)
            .localsSize(256)
            .maxCallStackDepth(32)
            .build();
        vm = new RegisterVMInterpreter(config);
        
        // 创建状态模型
        stateModel = new VMRStateModel(1024, 256, 32);
        
        // 创建桥接器
        bridge = new VMRVisualBridge(vm, stateModel);
        
        // 创建模拟监听器
        stateListener = mock(VMRStateListener.class);
        executionListener = mock(VMRExecutionListener.class);
        
        // 注册监听器
        stateModel.addStateListener(stateListener);
        stateModel.addExecutionListener(executionListener);
    }
    
    @Test
    @DisplayName("应该正确初始化反汇编器")
    void testInitializeDisAssembler_ShouldSucceed() {
        assertThatCode(() -> bridge.getDisAssembler())
            .doesNotThrowAnyException();
    }
    
    @Test
    @DisplayName("应该启动执行线程")
    void testStartExecution_ShouldCreateThread() {
        bridge.startExecution();
        
        assertThat(bridge.isRunning()).isTrue();
        assertThat(bridge.getExecutionThread()).isNotNull();
        
        // 清理
        bridge.stopExecution();
    }
    
    @Test
    @DisplayName("应该暂停执行")
    void testPauseExecution_ShouldPauseThread() {
        bridge.startExecution();
        bridge.pauseExecution();
        
        assertThat(bridge.isPaused()).isTrue();
        
        bridge.stopExecution();
    }
    
    @Test
    @DisplayName("应该恢复执行")
    void testResumeExecution_ShouldResumeThread() {
        bridge.startExecution();
        bridge.pauseExecution();
        bridge.resumeExecution();
        
        assertThat(bridge.isPaused()).isFalse();
        
        bridge.stopExecution();
    }
    
    @Test
    @DisplayName("应该停止执行")
    void testStopExecution_ShouldStopThread() {
        bridge.startExecution();
        bridge.stopExecution();
        
        assertThat(bridge.isRunning()).isFalse();
    }
    
    @Test
    @DisplayName("应该执行单条指令")
    void testExecuteSingleInstruction_ShouldAdvancePC() {
        int initialPC = stateModel.getProgramCounter();
        
        bridge.executeSingleInstruction();
        
        // 验证PC已前进（具体值取决于指令）
        assertThat(stateModel.getProgramCounter()).isNotEqualTo(initialPC);
    }
    
    @Test
    @DisplayName("应该处理执行错误")
    void testExecutionError_ShouldNotifyListeners() {
        // 模拟执行错误
        Throwable error = new RuntimeException("Test error");
        int pc = 0x100;
        
        bridge.handleExecutionError(error, pc);
        
        verify(executionListener, times(1)).executionError(error, pc);
    }
    
    @Test
    @DisplayName("应该转发状态变化事件")
    void testStateChangeForwarding_ShouldForwardEvents() {
        // 模拟状态变化
        VMStateChangeEvent event = new VMStateChangeEvent(
            this, 1, 
            VMStateChangeEvent.State.PAUSED, 
            VMStateChangeEvent.State.RUNNING
        );
        
        bridge.vmStateChanged(event);
        
        // 验证事件被转发
        verify(executionListener, times(1)).vmStateChanged(event);
    }
    
    @Test
    @DisplayName("应该处理重复启动")
    void testDuplicateStart_ShouldHandleGracefully() {
        bridge.startExecution();
        
        // 第二次启动应该被忽略或处理
        assertThatCode(() -> bridge.startExecution())
            .doesNotThrowAnyException();
        
        bridge.stopExecution();
    }
}
```

### 2.4 GUI组件测试模板

#### RegisterPanelTest.java
```java
package org.teachfx.antlr4.ep18r.vizvmr.ui.component;

import org.junit.jupiter.api.*;
import org.teachfx.antlr4.ep18r.vizvmr.core.VMRStateModel;
import org.teachfx.antlr4.ep18r.vizvmr.event.RegisterChangeEvent;
import javax.swing.*;
import java.awt.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("寄存器面板测试")
class RegisterPanelTest {
    
    private VMRStateModel stateModel;
    private RegisterPanel registerPanel;
    
    @BeforeEach
    void setUp() {
        // 创建状态模型
        stateModel = new VMRStateModel(1024, 256, 32);
        
        // 创建寄存器面板
        registerPanel = new RegisterPanel(stateModel);
    }
    
    @Test
    @DisplayName("应该正确初始化组件")
    void testInitialization_ShouldCreateComponents() {
        // 验证面板已创建
        assertThat(registerPanel).isNotNull();
        
        // 验证包含16个寄存器显示组件
        Component[] components = registerPanel.getComponents();
        assertThat(components.length).isGreaterThan(0);
    }
    
    @Test
    @DisplayName("应该正确显示寄存器值")
    void testRegisterDisplay_ShouldShowValues() {
        // 更新寄存器值
        stateModel.updateRegister(1, 0x12345678);
        stateModel.updateRegister(2, 100);
        
        // 触发UI更新（需要在EDT中执行）
        SwingUtilities.invokeLater(() -> {
            registerPanel.updateDisplay();
        });
        
        // 验证显示已更新
        // 注意：UI测试可能需要更复杂的断言
    }
    
    @Test
    @DisplayName("应该高亮修改的寄存器")
    void testModifiedRegisterHighlighting_ShouldChangeColor() {
        // 更新寄存器
        stateModel.updateRegister(3, 200);
        
        // 触发更新
        SwingUtilities.invokeLater(() -> {
            registerPanel.updateDisplay();
            
            // 验证修改的寄存器有特殊样式
            // 具体断言取决于实现
        });
    }
    
    @Test
    @DisplayName("应该特殊显示零寄存器")
    void testZeroRegisterDisplay_ShouldHaveSpecialStyle() {
        // r0 应该是零寄存器
        SwingUtilities.invokeLater(() -> {
            // 验证r0有特殊样式
            // 具体断言取决于实现
        });
    }
    
    @Test
    @DisplayName("应该特殊显示特殊寄存器")
    void testSpecialRegisterDisplay_ShouldHaveSpecialStyle() {
        // r13 (SP), r14 (FP), r15 (LR) 是特殊寄存器
        SwingUtilities.invokeLater(() -> {
            // 验证特殊寄存器有特殊样式
            // 具体断言取决于实现
        });
    }
    
    @Test
    @DisplayName("应该处理寄存器变化事件")
    void testRegisterChangeEvent_ShouldUpdateDisplay() {
        // 创建寄存器变化事件
        RegisterChangeEvent event = new RegisterChangeEvent(
            this, 1, 1, 0, 100, "r1"
        );
        
        // 触发事件处理
        SwingUtilities.invokeLater(() -> {
            registerPanel.registerChanged(event);
            
            // 验证显示已更新
            // 具体断言取决于实现
        });
    }
    
    @Test
    @DisplayName("应该支持双击编辑")
    void testDoubleClickEdit_ShouldAllowEditing() {
        // 模拟双击事件
        SwingUtilities.invokeLater(() -> {
            // 触发双击事件
            // 验证编辑对话框被打开
            // 具体断言取决于实现
        });
    }
    
    @Test
    @DisplayName("应该支持右键菜单")
    void testRightClickMenu_ShouldShowContextMenu() {
        SwingUtilities.invokeLater(() -> {
            // 触发右键点击
            // 验证上下文菜单被显示
            // 具体断言取决于实现
        });
    }
}
```

## 🧪 集成测试模板

### 3.1 端到端测试模板

#### VMRIntegrationTest.java
```java
package org.teachfx.antlr4.ep18r.vizvmr.integration;

import org.junit.jupiter.api.*;
import org.teachfx.antlr4.ep18r.stackvm.config.VMConfig;
import org.teachfx.antlr4.ep18r.stackvm.interpreter.RegisterVMInterpreter;
import org.teachfx.antlr4.ep18r.vizvmr.core.VMRStateModel;
import org.teachfx.antlr4.ep18r.vizvmr.ui.MainFrame;
import javax.swing.*;
import java.io.ByteArrayInputStream;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("可视化模块集成测试")
class VMRIntegrationTest {
    
    private RegisterVMInterpreter vm;
    private VMRStateModel stateModel;
    private VMRVisualBridge bridge;
    private MainFrame mainFrame;
    
    @BeforeEach
    void setUp() {
        // 创建虚拟机实例
        VMConfig config = new VMConfig.Builder()
            .heapSize(1024)
            .localsSize(256)
            .maxCallStackDepth(32)
            .build();
        vm = new RegisterVMInterpreter(config);
        
        // 创建状态模型
        stateModel = new VMRStateModel(1024, 256, 32);
        
        // 创建桥接器
        bridge = new VMRVisualBridge(vm, stateModel);
        
        // 在EDT中创建主窗口
        SwingUtilities.invokeLater(() -> {
            mainFrame = new MainFrame(bridge, stateModel);
            mainFrame.setVisible(false); // 测试时不显示窗口
        });
    }
    
    @AfterEach
    void tearDown() {
        // 清理资源
        if (mainFrame != null) {
            SwingUtilities.invokeLater(() -> {
                mainFrame.dispose();
            });
        }
    }
    
    @Test
    @DisplayName("应该完整加载和显示汇编程序")
    void testFullAssemblyLoadAndDisplay() {
        // 测试汇编程序
        String assemblyCode = 
            ".text\n" +
            "main:\n" +
            "  li r1, 100\n" +
            "  li r2, 200\n" +
            "  add r3, r1, r2\n" +
            "  halt\n";
        
        assertThatCode(() -> {
            // 加载汇编程序
            ByteArrayInputStream input = new ByteArrayInputStream(assemblyCode.getBytes());
            RegisterVMInterpreter.load(vm, input);
            
            // 同步状态
            bridge.getInstrumentation().instrument();
            bridge.getInstrumentation().syncState();
            
            // 验证状态
            assertThat(stateModel.getProgramCounter()).isEqualTo(0);
            // 更多断言...
        }).doesNotThrowAnyException();
    }
    
    @Test
    @DisplayName("应该完整执行单步调试流程")
    void testSingleStepDebuggingFlow() {
        // 加载简单程序
        String assemblyCode = 
            ".text\n" +
            "main:\n" +
            "  li r1, 10\n" +
            "  li r2, 20\n" +
            "  add r3, r1, r2\n" +
            "  halt\n";
        
        ByteArrayInputStream input = new ByteArrayInputStream(assemblyCode.getBytes());
        RegisterVMInterpreter.load(vm, input);
        
        // 执行单步
        bridge.getInstrumentation().instrument();
        
        // 单步执行指令
        for (int i = 0; i < 3; i++) {
            bridge.executeSingleInstruction();
            
            // 验证状态更新
            assertThat(stateModel.getProgramCounter()).isEqualTo((i + 1) * 4);
            // 更多断言...
        }
    }
    
    @Test
    @DisplayName("应该正确处理断点")
    void testBreakpointHandling() {
        // 加载程序
        String assemblyCode = 
            ".text\n" +
            "main:\n" +
            "  li r1, 100\n" +      // PC=0
            "  li r2, 200\n" +      // PC=4
            "  add r3, r1, r2\n" +  // PC=8
            "  halt\n";             // PC=12
        
        ByteArrayInputStream input = new ByteArrayInputStream(assemblyCode.getBytes());
        RegisterVMInterpreter.load(vm, input);
        
        bridge.getInstrumentation().instrument();
        
        // 设置断点在 PC=8
        // 注意：实际实现中需要断点管理器
        // bridge.getBreakpointManager().addBreakpoint(8);
        
        // 执行到断点
        bridge.startExecution();
        // 等待到达断点
        // 验证在PC=8处暂停
        
        bridge.stopExecution();
    }
    
    @Test
    @DisplayName("应该完整记录执行历史")
    void testExecutionHistoryRecording() {
        // 加载程序
        String assemblyCode = 
            ".text\n" +
            "main:\n" +
            "  li r1, 1\n" +
            "  li r2, 2\n" +
            "  li r3, 3\n" +
            "  halt\n";
        
        ByteArrayInputStream input = new ByteArrayInputStream(assemblyCode.getBytes());
        RegisterVMInterpreter.load(vm, input);
        
        bridge.getInstrumentation().instrument();
        
        // 执行指令
        for (int i = 0; i < 3; i++) {
            bridge.executeSingleInstruction();
        }
        
        // 验证历史记录
        // 注意：实际实现中需要历史记录器
        // assertThat(bridge.getExecutionHistory().size()).isEqualTo(3);
        // 验证每个快照都包含正确的状态
    }
    
    @Test
    @DisplayName("应该处理执行异常")
    void testExecutionExceptionHandling() {
        // 加载包含错误的程序
        String assemblyCode = 
            ".text\n" +
            "main:\n" +
            "  li r1, 100\n" +
            "  div r2, r1, r0\n" +  // 除零错误
            "  halt\n";
        
        ByteArrayInputStream input = new ByteArrayInputStream(assemblyCode.getBytes());
        RegisterVMInterpreter.load(vm, input);
        
        bridge.getInstrumentation().instrument();
        
        // 执行并捕获异常
        assertThatThrownBy(() -> {
            bridge.startExecution();
            // 等待异常
            Thread.sleep(100);
            bridge.stopExecution();
        }).isInstanceOf(Exception.class);
        
        // 验证错误处理被调用
        // 具体断言取决于实现
    }
}
```

## 🛠️ 测试工具和配置

### 4.1 测试依赖配置 (pom.xml)
```xml
<dependencies>
    <!-- 测试框架 -->
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <version>5.8.2</version>
        <scope>test</scope>
    </dependency>
    
    <!-- 断言库 -->
    <dependency>
        <groupId>org.assertj</groupId>
        <artifactId>assertj-core</artifactId>
        <version>3.21.0</version>
        <scope>test</scope>
    </dependency>
    
    <!-- Mock框架 -->
    <dependency>
        <groupId>org.mockito</groupId>
        <artifactId>mockito-core</artifactId>
        <version>4.5.1</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.mockito</groupId>
        <artifactId>mockito-junit-jupiter</artifactId>
        <version>4.5.1</version>
        <scope>test</scope>
    </dependency>
    
    <!-- UI测试 -->
    <dependency>
        <groupId>org.assertj</groupId>
        <artifactId>assertj-swing-junit</artifactId>
        <version>3.9.2</version>
        <scope>test</scope>
    </dependency>
</dependencies>

<build>
    <plugins>
        <!-- 测试覆盖率 -->
        <plugin>
            <groupId>org.jacoco</groupId>
            <artifactId>jacoco-maven-plugin</artifactId>
            <version>0.8.8</version>
            <executions>
                <execution>
                    <goals>
                        <goal>prepare-agent</goal>
                    </goals>
                </execution>
                <execution>
                    <id>report</id>
                    <phase>test</phase>
                    <goals>
                        <goal>report</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

### 4.2 测试运行配置

#### JUnit 5 配置 (src/test/resources/junit-platform.properties)
```properties
# JUnit Platform配置
junit.jupiter.conditions.deactivate = *
junit.jupiter.extensions.autodetection.enabled = true
junit.jupiter.testinstance.lifecycle.default = per_method
junit.jupiter.execution.parallel.enabled = true
junit.jupiter.execution.parallel.mode.default = same_thread
junit.jupiter.execution.parallel.mode.classes.default = concurrent
junit.jupiter.execution.parallel.config.strategy = fixed
junit.jupiter.execution.parallel.config.fixed.parallelism = 4

# 测试报告
junit.platform.output.capture.stdout = true
junit.platform.output.capture.stderr = true
```

#### 测试数据目录结构
```
src/test/resources/
├── test-programs/           # 测试用汇编程序
│   ├── simple-add.vasm      # 简单加法程序
│   ├── factorial.vasm       # 阶乘程序
│   ├── fibonacci.vasm       # 斐波那契程序
│   └── loops.vasm           # 循环程序
├── expected-outputs/        # 预期输出
│   ├── simple-add.txt
│   ├── factorial.txt
│   └── fibonacci.txt
└── test-configs/           # 测试配置
    ├── small-config.json   # 小内存配置
    ├── medium-config.json  # 中等内存配置
    └── large-config.json   # 大内存配置
```

## 📊 测试报告和质量指标

### 5.1 质量指标
| 指标 | 目标值 | 测量方法 | 报告频率 |
|------|--------|----------|----------|
| **代码覆盖率** | ≥ 80% | JaCoCo | 每次构建 |
| **单元测试通过率** | 100% | JUnit | 每次构建 |
| **集成测试通过率** | ≥ 95% | JUnit | 每次构建 |
| **UI测试通过率** | ≥ 90% | AssertJ Swing | 每日构建 |
| **构建时间** | < 5分钟 | Maven | 每次构建 |
| **测试执行时间** | < 3分钟 | JUnit | 每次构建 |

### 5.2 测试报告生成
```bash
# 运行所有测试并生成报告
mvn clean test jacoco:report

# 只运行单元测试
mvn test -Dtest="*Test"

# 只运行集成测试
mvn test -Dtest="*IntegrationTest"

# 生成HTML报告
mvn jacoco:report
```

## 🔄 持续集成

### 6.1 GitHub Actions 配置 (.github/workflows/test.yml)
```yaml
name: vizvmr Tests

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  test:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 21
      uses: actions/setup-java@v3
      with:
        java-version: '21'
        distribution: 'temurin'
    
    - name: Run tests with coverage
      run: mvn clean test jacoco:report
    
    - name: Upload coverage report
      uses: codecov/codecov-action@v3
      with:
        file: ./target/site/jacoco/jacoco.xml
    
    - name: Check coverage threshold
      run: |
        # 检查覆盖率是否达到阈值
        # 具体实现取决于覆盖率检查工具
```

## 📝 测试文档要求

### 7.1 测试用例文档
每个测试类必须有对应的文档，包括：
- **测试目的**: 测试什么功能
- **测试场景**: 覆盖哪些场景
- **预期行为**: 预期结果是什么
- **依赖条件**: 测试前提条件
- **测试数据**: 使用的测试数据

### 7.2 测试评审
- **代码审查**: 所有测试代码必须经过代码审查
- **测试用例评审**: 定期评审测试用例的完整性和有效性
- **覆盖率评审**: 定期评审测试覆盖率报告

---

**文档版本**: 1.0  
**创建日期**: 2026-01-14  
**最后更新**: 2026-01-14  
**维护者**: EP18R开发团队
