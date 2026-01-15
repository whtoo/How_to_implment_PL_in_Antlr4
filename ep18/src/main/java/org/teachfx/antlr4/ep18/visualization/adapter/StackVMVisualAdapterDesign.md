# StackVMVisualAdapter 设计文档

## 1. 概述

StackVMVisualAdapter 是 EP18 栈式虚拟机的可视化适配器，实现 `IVirtualMachineVisualization` 接口，为栈式虚拟机提供统一的可视化支持。

## 2. 架构设计

### 2.1 类结构
```
StackVMVisualAdapter
├── 核心依赖
│   ├── CymbolStackVM vm (底层虚拟机)
│   ├── EventBus eventBus (事件总线)
│   ├── EventPublisher eventPublisher (事件发布者)
│   └── BytecodeDefinition instructionSet (指令集)
├── 执行状态
│   ├── VMState<StackVMSpecificState> vmState
│   ├── AtomicBoolean running
│   ├── AtomicBoolean paused
│   ├── AtomicBoolean stepMode
│   ├── Set<Integer> breakpoints
│   └── AtomicInteger stepCounter
├── 监听器列表
│   ├── List<ExecutionListener> executionListeners
│   ├── List<StateChangeListener> stateChangeListeners
│   └── List<EducationalHintListener> educationalHintListeners
└── 执行线程
    └── Thread executionThread
```

### 2.2 包路径
- 包名: `org.teachfx.antlr4.ep18.visualization.adapter`
- 依赖: `org.teachfx.antlr4.common.visualization.*`

## 3. 核心功能设计

### 3.1 状态管理
#### StackVMSpecificState 内部类
```java
public static class StackVMSpecificState {
    private int[] stack;           // 操作数栈快照
    private int stackPointer;      // 栈指针位置
    private int[] heap;           // 堆内存快照
    private int heapAllocPointer; // 堆分配指针
    private int[] locals;         // 局部变量快照
    private StackFrame[] callStack; // 调用栈
    private int framePointer;     // 帧指针
    private Map<String, Object> additionalInfo; // 附加信息
}
```

### 3.2 状态更新机制
```java
private void updateVMState() {
    // 获取栈式虚拟机状态
    int pc = vm.getProgramCounter();
    int[] stack = vm.getStackCopy();
    int stackPointer = vm.getStackDepth();
    int[] heap = vm.getHeapCopy();
    int[] locals = vm.getLocalsCopy();
    StackFrame[] callStack = vm.getCallStackCopy();
    int framePointer = vm.getFramePointer();
    
    // 更新VMState
    vmState.setProgramCounter(pc);
    vmState.setCurrentInstruction(disassembleInstruction(pc));
    vmState.setExecutionState(getExecutionState());
    
    // 更新特定状态
    StackVMSpecificState specificState = vmState.getVmSpecificState();
    specificState.updateStack(stack, stackPointer);
    specificState.updateHeap(heap);
    specificState.updateLocals(locals);
    specificState.updateCallStack(callStack, framePointer);
}
```

### 3.3 执行控制
#### 扩展 CymbolStackVM 以支持可视化
需要扩展 CymbolStackVM 添加以下功能：
1. **暂停机制**: 在指令循环中添加暂停检查点
2. **断点支持**: 管理断点集合
3. **事件通知**: 在关键执行点触发事件
4. **状态访问**: 提供受保护的状态访问方法

#### 执行控制方法
```java
@Override
public void step() {
    if (!running.get() || paused.get()) {
        stepMode.set(true);
        if (paused.get()) {
            resumeExecutionForStep();
        } else {
            startStepExecution();
        }
    }
}

@Override
public void run() {
    if (running.get() && !paused.get()) return;
    stepMode.set(false);
    startExecutionThread();
}

@Override
public void pause() {
    if (running.get() && !paused.get()) {
        paused.set(true);
        vm.setPaused(true);
        notifyPause(vmState.getProgramCounter());
    }
}
```

## 4. 事件系统集成

### 4.1 事件监听器
需要扩展 CymbolStackVM 以支持事件监听：
```java
// 在 CymbolStackVM 中添加
public interface StackVMExecutionListener {
    void beforeInstructionExecute(int pc, String instruction);
    void afterInstructionExecute(int pc, String instruction, int[] stackSnapshot);
    void onPause(int pc);
    void onResume(int pc);
    void onBreakpointHit(int pc);
    void onStackChange(int oldPointer, int newPointer);
    void onHeapChange(int address, int oldValue, int newValue);
}
```

### 4.2 事件发布
适配器负责将栈式虚拟机事件转换为统一的事件格式：
```java
private void publishInstructionExecuted(int pc, String instruction) {
    eventPublisher.publish(new InstructionExecutedEvent(
        this,
        stepCounter.get(),
        pc,
        instruction,
        vm.getCurrentRegisters() // 对于栈式VM，可能是栈顶值
    ));
}
```

## 5. 教育功能设计

### 5.1 栈操作可视化
```java
@Override
public void highlightCurrentOperation(String description) {
    // 高亮当前栈操作（push/pop/计算）
    eventPublisher.publish(new OperationHighlightedEvent(
        this,
        stepCounter.get(),
        description,
        vmState.getProgramCounter(),
        vmState.getCurrentInstruction()
    ));
}
```

### 5.2 表达式求值可视化
```java
@Override
public void showExpressionEvaluation(String expression, List<EvaluationStep> steps) {
    // 展示栈式表达式的求值过程
    eventPublisher.publish(new ExpressionEvaluationEvent(
        this,
        stepCounter.get(),
        expression,
        steps
    ));
}
```

### 5.3 调用栈可视化
```java
@Override
public void visualizeCallStack(String description) {
    // 可视化函数调用栈
    StackFrame[] frames = vm.getCallStackCopy();
    List<CallStackFrame> visualFrames = convertToVisualFrames(frames);
    eventPublisher.publish(new CallStackVisualizationEvent(
        this,
        stepCounter.get(),
        description,
        visualFrames
    ));
}
```

## 6. 与 RegisterVMVisualAdapter 的差异

### 6.1 状态模型的差异
| 特性 | StackVMVisualAdapter | RegisterVMVisualAdapter |
|------|---------------------|------------------------|
| 主要存储 | 操作数栈 | 寄存器文件 |
| 状态访问 | 栈指针+栈数组 | 寄存器数组 |
| 调用约定 | 基于栈的调用 | 基于寄存器的调用 |
| 内存模型 | 栈+堆 | 寄存器+堆 |

### 6.2 可视化重点
1. **栈操作可视化**: 显示push/pop操作和栈变化
2. **表达式求值**: 展示栈式求值过程
3. **函数调用**: 可视化栈帧创建和销毁
4. **内存布局**: 展示栈、堆、数据段的关系

## 7. 实现步骤

### 7.1 第一阶段：基础适配器
1. 创建 StackVMVisualAdapter 类框架
2. 实现基本状态获取方法
3. 集成事件系统骨架

### 7.2 第二阶段：执行控制
1. 扩展 CymbolStackVM 支持暂停/断点
2. 实现 step/run/pause/stop 方法
3. 添加执行线程管理

### 7.3 第三阶段：事件系统
1. 实现完整的事件监听和发布
2. 添加状态变化通知
3. 集成教育功能

### 7.4 第四阶段：教育功能
1. 实现栈操作可视化
2. 添加表达式求值演示
3. 集成调用栈可视化

## 8. 依赖关系

### 8.1 对 common 模块的依赖
```xml
<dependency>
    <groupId>org.teachfx</groupId>
    <artifactId>antlr4-common</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 8.2 对 EP18 模块的内部依赖
- `CymbolStackVM` - 核心虚拟机
- `VMExecutionContext` - 执行上下文
- `BytecodeDefinition` - 指令定义
- `StackFrame` - 栈帧管理

## 9. 测试策略

### 9.1 单元测试
1. 状态获取测试
2. 执行控制测试
3. 事件发布测试

### 9.2 集成测试
1. 与 CymbolStackVM 集成测试
2. 可视化界面集成测试
3. 教育功能集成测试

### 9.3 端到端测试
1. 完整可视化流程测试
2. 性能测试
3. 兼容性测试

## 10. 风险与缓解

### 10.1 技术风险
1. **CymbolStackVM 扩展风险**: 需要修改现有虚拟机代码
   - 缓解：保持向后兼容性，通过包装模式实现

2. **性能风险**: 频繁的状态更新可能影响性能
   - 缓解：使用缓存和增量更新

3. **线程安全风险**: 多线程环境下的状态同步
   - 缓解：使用原子变量和同步块

### 10.2 兼容性风险
1. **API 变化风险**: common 模块接口可能变化
   - 缓解：使用版本控制和接口稳定性保证

2. **依赖冲突风险**: 多个模块依赖 common
   - 缓解：使用 Maven 依赖管理

## 11. 后续优化方向

### 11.1 性能优化
1. 状态快照压缩
2. 事件批量发布
3. 内存使用优化

### 11.2 功能增强
1. 高级断点功能（条件断点、数据断点）
2. 性能分析工具
3. 代码覆盖率分析

### 11.3 用户体验
1. 自定义可视化主题
2. 交互式调试
3. 教学示例库