# EP18R 可视化模块 (vizvmr) API 文档

## 📋 概述

本文档提供了 **vizvmr** 模块的完整 API 参考，包括所有公开类、接口、方法和使用示例。vizvmr 是一个基于观察者模式的可视化系统，通过事件监听器捕获虚拟机状态变化，并使用 Java Swing 提供图形化界面。

## 🏗️ 包结构

### 核心包 (org.teachfx.antlr4.ep18r.vizvmr)
```
org.teachfx.antlr4.ep18r.vizvmr
├── VizVMRLauncher.java          # 模块启动器
├── core/                        # 核心模型包
│   ├── VMRStateModel.java       # 状态数据模型
│   └── VMRExecutionHistory.java # 执行历史记录
├── event/                       # 事件系统包
│   ├── VMRStateListener.java    # 状态监听器接口
│   ├── VMRExecutionListener.java # 执行监听器接口
│   ├── RegisterChangeEvent.java # 寄存器变化事件
│   ├── MemoryChangeEvent.java   # 内存变化事件
│   ├── PCChangeEvent.java       # PC变化事件
│   ├── InstructionExecutionEvent.java # 指令执行事件
│   └── VMStateChangeEvent.java  # 虚拟机状态变化事件
├── integration/                 # 集成层包
│   ├── VMRInstrumentation.java  # 虚拟机插桩适配器
│   └── VMRVisualBridge.java     # 可视化桥接器
├── controller/                  # 控制层包
│   ├── VMRStepController.java   # 单步执行控制器
│   └── VMRBreakpointManager.java # 断点管理器
├── ui/                         # 用户界面包
│   ├── MainFrame.java          # 主窗口框架
│   └── panel/                  # 面板组件包
│       ├── RegisterPanel.java  # 寄存器面板
│       ├── MemoryPanel.java    # 内存面板
│       ├── CodePanel.java      # 代码面板
│       ├── StackPanel.java     # 调用栈面板
│       ├── ControlPanel.java   # 控制面板
│       └── StatusPanel.java    # 状态面板
└── util/                       # 工具包
    └── ConfigPersistence.java  # 配置持久化工具
```

## 🎯 核心类 API

### 1. VizVMRLauncher - 模块启动器

#### 类概述
`VizVMRLauncher` 是 vizvmr 模块的入口点，负责初始化可视化系统并启动主窗口。

#### 构造函数
```java
public VizVMRLauncher()
```

#### 主要方法
```java
// 启动可视化界面
public void launch()
```

#### 使用示例
```java
public static void main(String[] args) {
    VizVMRLauncher launcher = new VizVMRLauncher();
    launcher.launch();
}
```

### 2. VMRStateModel - 状态数据模型

#### 类概述
`VMRStateModel` 是可视化系统的核心数据模型，负责管理虚拟机状态（寄存器、内存、调用栈等）并提供事件通知机制。

#### 构造函数
```java
// 创建状态模型实例
public VMRStateModel(int heapSize, int globalsSize, int maxCallStackDepth)
```

#### 状态管理方法
```java
// 寄存器管理
public void setRegister(int regNum, int value)
public int getRegister(int regNum)
public int[] getRegisters()
public boolean isRegisterModified(int regNum)
public void clearModifiedFlags()

// 堆内存管理
public void writeHeap(int address, int value)
public int readHeap(int address)
public int[] getHeap()
public int allocateHeap(int size)
public int getHeapAllocPointer()
public Set<Integer> getModifiedHeapAddresses()

// 全局变量管理
public void writeGlobal(int address, int value)
public int readGlobal(int address)
public int[] getGlobals()
public Set<Integer> getModifiedMemoryAddresses()

// 程序计数器管理
public void setProgramCounter(int pc)
public int getProgramCounter()

// 调用栈管理
public void pushStackFrame(StackFrame frame)
public StackFrame popStackFrame()
public StackFrame getCurrentFrame()
public int getFramePointer()
public int getCallStackDepth()
public StackFrame[] getCallStack()

// 执行统计
public void incrementExecutionStep()
public long getExecutionSteps()
public long getExecutionTime()

// 虚拟机状态管理
public void setVMState(VMStateChangeEvent.State state)
public VMStateChangeEvent.State getVMState()

// 快照功能
public VMRStateSnapshot createSnapshot()
public void restoreSnapshot(VMRStateSnapshot snapshot)
```

#### 监听器管理方法
```java
// 添加监听器
public void addStateListener(VMRStateListener listener)
public void addExecutionListener(VMRExecutionListener listener)

// 移除监听器
public void removeStateListener(VMRStateListener listener)
public void removeExecutionListener(VMRExecutionListener listener)
```

#### 使用示例
```java
// 创建状态模型
VMRStateModel stateModel = new VMRStateModel(1024, 256, 32);

// 更新寄存器并触发事件
stateModel.setRegister(1, 0x12345678);

// 更新内存
stateModel.writeHeap(100, 0x5678);

// 设置程序计数器
stateModel.setProgramCounter(0x100);

// 获取状态
int value = stateModel.getRegister(1);
int memValue = stateModel.readHeap(100);
int pc = stateModel.getProgramCounter();
```

### 3. VMRExecutionHistory - 执行历史记录

#### 类概述
`VMRExecutionHistory` 管理虚拟机执行的历史快照，支持撤销和重做操作。

#### 构造函数
```java
public VMRExecutionHistory(int maxHistorySize)
```

#### 主要方法
```java
// 历史记录操作
public void saveState(VMRStateModel stateModel)
public VMRStateSnapshot undo(VMRStateModel targetModel)
public VMRStateSnapshot redo(VMRStateModel targetModel)
public void clear()

// 历史查询
public boolean canUndo()
public boolean canRedo()
public int getUndoSize()
public int getRedoSize()
public int getSnapshotCount()
```

#### 使用示例
```java
// 创建历史记录器
VMRExecutionHistory history = new VMRExecutionHistory(1000);

// 捕获快照
history.saveState(stateModel);

// 撤销操作
if (history.canUndo()) {
    VMRStateSnapshot restored = history.undo(stateModel);
}

// 重做操作
if (history.canRedo()) {
    VMRStateSnapshot restored = history.redo(stateModel);
}
```

## 🔌 事件系统 API

### 4. VMRStateListener - 状态监听器接口

#### 接口定义
```java
public interface VMRStateListener extends EventListener {
    // 寄存器变化事件
    void registerChanged(RegisterChangeEvent event);
    
    // 内存变化事件
    void memoryChanged(MemoryChangeEvent event);
    
    // 程序计数器变化事件
    void pcChanged(PCChangeEvent event);
    
    // 批量更新方法（性能优化）
    default void registersUpdated(RegisterChangeEvent[] events);
    default void memoryUpdated(MemoryChangeEvent[] events);
}
```

#### 事件类
```java
// RegisterChangeEvent
public RegisterChangeEvent(Object source, int stepNumber, 
                          int registerIndex, int oldValue, int newValue, String registerName)

// MemoryChangeEvent  
public MemoryChangeEvent(Object source, int stepNumber,
                        int address, int oldValue, int newValue, MemoryType type)

// PCChangeEvent
public PCChangeEvent(Object source, int stepNumber, int oldPC, int newPC)
```

#### 使用示例
```java
public class MyStateListener implements VMRStateListener {
    @Override
    public void registerChanged(RegisterChangeEvent event) {
        System.out.printf("寄存器 %s 从 %d 变为 %d%n",
            event.getRegisterName(), event.getOldValue(), event.getNewValue());
    }
    
    @Override
    public void memoryChanged(MemoryChangeEvent event) {
        System.out.printf("内存[0x%04X] 从 0x%08X 变为 0x%08X%n",
            event.getAddress(), event.getOldValue(), event.getNewValue());
    }
    
    @Override
    public void pcChanged(PCChangeEvent event) {
        System.out.printf("PC 从 0x%04X 跳转到 0x%04X%n",
            event.getOldPC(), event.getNewPC());
    }
}

// 注册监听器
stateModel.addStateListener(new MyStateListener());
```

### 5. VMRExecutionListener - 执行监听器接口

#### 接口定义
```java
public interface VMRExecutionListener extends EventListener {
    // 指令执行事件
    default void instructionFetched(InstructionExecutionEvent event);
    default void beforeInstructionDecode(InstructionExecutionEvent event);
    default void beforeInstructionExecute(InstructionExecutionEvent event);
    default void afterInstructionExecute(InstructionExecutionEvent event);
    
    // 虚拟机状态变化
    void vmStateChanged(VMStateChangeEvent event);
    
    // 执行错误处理
    default void executionError(Throwable error, int pc);
    
    // 执行控制事件
    default void executionStarted();
    default void executionFinished();
    default void executionPaused();
}
```

#### 事件类
```java
// InstructionExecutionEvent
public InstructionExecutionEvent(Object source, int stepNumber,
                               int pc, String instruction, InstructionPhase phase)

// VMStateChangeEvent
public VMStateChangeEvent(Object source, int stepNumber,
                         State oldState, State newState)
```

#### 使用示例
```java
public class MyExecutionListener implements VMRExecutionListener {
    @Override
    public void afterInstructionExecute(InstructionExecutionEvent event) {
        System.out.printf("在 PC=0x%04X 执行指令: %s%n",
            event.getPC(), event.getInstruction());
    }
    
    @Override
    public void vmStateChanged(VMStateChangeEvent event) {
        System.out.printf("虚拟机状态: %s -> %s%n",
            event.getOldState(), event.getNewState());
    }
}

// 注册监听器
stateModel.addExecutionListener(new MyExecutionListener());
```

## 🔗 集成层 API

### 6. VMRInstrumentation - 虚拟机插桩适配器

#### 类概述
`VMRInstrumentation` 通过反射机制在虚拟机关键位置插入监听点，捕获执行状态变化。

#### 构造函数
```java
public VMRInstrumentation(RegisterVMInterpreter vm, VMRStateModel stateModel)
```

#### 主要方法
```java
// 插桩操作
public void instrument()
public boolean isInstrumented()
public void uninstall()

// 状态同步
public void syncRegisters()
public void syncMemory()
public void syncCallStack()
public void syncProgramCounter()
public void syncState()  // 同步所有状态

// 反射访问（高级使用）
public int[] getRegistersReflectively()
public int getProgramCounterReflectively()
public StackFrame[] getCallStackReflectively()
```

#### 使用示例
```java
// 创建虚拟机实例
RegisterVMInterpreter vm = new RegisterVMInterpreter(config);
VMRStateModel stateModel = new VMRStateModel(1024, 256, 32);

// 创建并执行插桩
VMRInstrumentation instrumentation = new VMRInstrumentation(vm, stateModel);
instrumentation.instrument();

// 同步状态
instrumentation.syncState();
```

### 7. VMRVisualBridge - 可视化桥接器

#### 类概述
`VMRVisualBridge` 连接虚拟机和可视化界面，提供执行控制和事件转发功能。

#### 构造函数
```java
public VMRVisualBridge(RegisterVMInterpreter vm, VMRStateModel stateModel)
```

#### 主要方法
```java
// 执行控制
public void start()
public void pause()
public void resume()
public void stop()
public void step()

// 状态查询
public boolean isRunning()
public boolean isPaused()
public VMRStateModel getStateModel()
public VMRInstrumentation getInstrumentation()
public int getCurrentPC()
public int getRegister(int regNum)

// 代码加载
public boolean loadCode(InputStream input)

// 反汇编支持
public RegisterDisAssembler getDisAssembler()
public String getDisassembly()
```

#### 使用示例
```java
// 创建虚拟机和状态模型
RegisterVMInterpreter vm = new RegisterVMInterpreter(config);
VMRStateModel stateModel = new VMRStateModel(1024, 256, 32);

// 创建桥接器
VMRVisualBridge bridge = new VMRVisualBridge(vm, stateModel);

// 加载代码
try (FileInputStream fis = new FileInputStream("program.vm")) {
    boolean hasErrors = bridge.loadCode(fis);
    if (hasErrors) {
        System.err.println("加载失败");
    }
}

// 启动执行
bridge.start();

// 单步执行
bridge.step();

// 暂停执行
bridge.pause();

// 恢复执行
bridge.resume();

// 停止执行
bridge.stop();
```

## 🎮 控制层 API

### 8. VMRStepController - 单步执行控制器

#### 类概述
`VMRStepController` 管理单步执行模式，支持单步、步过、步出等高级调试功能。

#### 构造函数
```java
public VMRStepController(VMRVisualBridge bridge)
```

#### 主要方法
```java
// 执行模式控制
public StepMode getCurrentMode()

// 单步执行操作
public void stepInto()
public void stepOver()
public void stepOut()
public void runToPC(int pc)
public void continueExecution()

// 状态查询
public boolean isStepping()
public boolean shouldPause(int pc)

// 断点管理
public void setBreakpoint(int pc)
public void clearBreakpoint(int pc)
public void toggleBreakpoint(int pc)
public void clearAllBreakpoints()
public VMRBreakpointManager getBreakpointManager()
```

#### 枚举类型
```java
public enum ExecutionMode {
    RUN,                    // 连续执行
    STEP,                   // 单步执行
    STEP_OVER,              // 单步步过
    STEP_OUT,               // 单步步出
    STEP_TO_BREAKPOINT,     // 运行到断点
    PAUSE,                  // 暂停
    STOP                    // 停止
}
```

#### 使用示例
```java
// 创建控制器
VMRStepController controller = new VMRStepController(bridge);

// 单步执行
controller.stepInto();

// 执行步过（不进入函数调用）
controller.stepOver();

// 执行步出（运行到当前函数返回）
controller.stepOut();

// 运行到指定PC
controller.runToPC(0x100);

// 继续执行直到断点
controller.continueExecution();

// 设置断点
controller.setBreakpoint(0x200);

// 切换断点
controller.toggleBreakpoint(0x200);
```

### 9. VMRBreakpointManager - 断点管理器

#### 类概述
`VMRBreakpointManager` 管理断点的设置、清除和条件断点功能。

#### 构造函数
```java
public VMRBreakpointManager()
```

#### 主要方法
```java
// 断点管理
public void setBreakpoint(int pc)
public void clearBreakpoint(int pc)
public void toggleBreakpoint(int pc)
public void disableBreakpoint(int pc)
public void enableBreakpoint(int pc)
public void setConditionalBreakpoint(int pc, Predicate<Integer> condition)
public void clearAllBreakpoints()

// 断点查询
public boolean hasBreakpoints()
public int getBreakpointCount()
public Set<Integer> getBreakpoints()
public Set<Integer> getDisabledBreakpoints()
public Set<ConditionalBreakpoint> getConditionalBreakpoints()

// 断点检查
public boolean shouldPause(int pc)

// 统计
public int getHitCount()
public int getTotalHits()
public void resetHitCount()
```

#### 条件断点类
```java
public class BreakpointCondition {
    private final String expression;
    private final Predicate<ExecutionContext> predicate;
    
    public BreakpointCondition(String expression, Predicate<ExecutionContext> predicate)
    public boolean evaluate(ExecutionContext context)
    public String getExpression()
}
```

#### 使用示例
```java
// 创建断点管理器
VMRBreakpointManager breakpointManager = new VMRBreakpointManager();

// 添加普通断点
breakpointManager.setBreakpoint(0x100);
breakpointManager.setBreakpoint(0x200);

// 添加条件断点
breakpointManager.setConditionalBreakpoint(0x300, pc -> {
    int r1 = visualBridge.getRegister(1);
    return r1 == 100;
});

// 切换断点
breakpointManager.toggleBreakpoint(0x100);

// 禁用断点
breakpointManager.disableBreakpoint(0x100);

// 启用断点
breakpointManager.enableBreakpoint(0x100);

// 清除断点
breakpointManager.clearBreakpoint(0x100);

// 检查断点
if (breakpointManager.shouldPause(pc)) {
    System.out.println("断点命中！");
}

// 清除所有断点
breakpointManager.clearAllBreakpoints();

// 获取命中统计
System.out.println("命中次数: " + breakpointManager.getHitCount());
System.out.println("总命中次数: " + breakpointManager.getTotalHits());
```

## 🖥️ 用户界面 API

### 10. MainFrame - 主窗口框架

#### 类概述
`MainFrame` 是可视化模块的主窗口，负责组织和管理所有面板组件。

#### 构造函数
```java
public MainFrame(VMRVisualBridge bridge)
```

#### 主要方法
```java
// 窗口控制
public void setVisible(boolean visible)
public void dispose()
public void setTitle(String title)

// 面板访问
public RegisterPanel getRegisterPanel()
public MemoryPanel getMemoryPanel()
public CodePanel getCodePanel()
public StackPanel getStackPanel()
public ControlPanel getControlPanel()
public StatusPanel getStatusPanel()

// 布局管理
public void updateLayout()
public void setTheme(VMRTheme theme)
```

#### 使用示例
```java
// 创建主窗口
MainFrame mainFrame = new MainFrame(bridge);
mainFrame.setVisible(true);

// 访问面板组件
RegisterPanel registerPanel = mainFrame.getRegisterPanel();
registerPanel.refresh();

MemoryPanel memoryPanel = mainFrame.getMemoryPanel();
memoryPanel.refresh();
```

### 11. RegisterPanel - 寄存器面板

#### 类概述
`RegisterPanel` 显示16个寄存器的实时状态，支持颜色编码和交互操作。

#### 构造函数
```java
public RegisterPanel(VMRVisualBridge bridge)
```

#### 主要方法
```java
// 显示更新
public void updateRegister(int regNum, int value)
public void refresh()

// 注意：当前实现缺少颜色编码功能（待实现）
// 设计要求：
// - 红色：最近修改的寄存器
// - 蓝色：特殊寄存器 (r13-SP, r14-FP, r15-LR)
// - 灰色：未修改的寄存器
// - 绿色：零寄存器 (r0)
```

#### 使用示例
```java
// 创建寄存器面板
RegisterPanel registerPanel = new RegisterPanel(visualBridge);

// 更新单个寄存器
registerPanel.updateRegister(1, 0x12345678);

// 刷新所有寄存器
registerPanel.refresh();
```

### 12. MemoryPanel - 内存面板

#### 类概述
`MemoryPanel` 显示内存内容的表格视图，支持搜索、跳转和多显示模式。

#### 构造函数
```java
public MemoryPanel(VMRVisualBridge bridge)
```

#### 主要方法
```java
// 显示更新
public void updateMemory(int address, int value)
public void refresh()

// 导航功能
private void scrollToAddress(int address)

// 注意：当前实现使用硬编码数据，需要连接到VMRStateModel
// 待改进：连接到实际虚拟机内存
```

#### 使用示例
```java
// 创建内存面板
MemoryPanel memoryPanel = new MemoryPanel(visualBridge);

// 更新特定地址
memoryPanel.updateMemory(0x100, 0x12345678);

// 刷新显示
memoryPanel.refresh();
```

### 13. CodePanel - 代码面板

#### 类概述
`CodePanel` 显示反汇编的指令代码，支持PC高亮、断点标记和交互操作。

#### 构造函数
```java
public CodePanel(VMRVisualBridge bridge)
```

#### 主要方法
```java
// 代码显示
public void setInstructions(RegisterDisAssembler disAssembler)
public void highlightPC(int pc)
public void refresh()

// 断点管理
public void toggleBreakpoint(int pc)
public void toggleBreakpointAtSelection()
public void clearAllBreakpoints()
public boolean isBreakpointAt(int pc)
public Set<Integer> getBreakpoints()

// 注意：当前断点管理在面板内部，应与VMRBreakpointManager集成
```

#### 使用示例
```java
// 创建代码面板
CodePanel codePanel = new CodePanel(visualBridge);

// 设置反汇编指令
codePanel.setInstructions(bridge.getDisAssembler());

// 高亮当前PC
codePanel.highlightPC(0x100);

// 切换断点
codePanel.toggleBreakpoint(0x100);

// 在选择位置切换断点
codePanel.toggleBreakpointAtSelection();

// 清除所有断点
codePanel.clearAllBreakpoints();
```

### 14. StackPanel - 调用栈面板

#### 类概述
`StackPanel` 显示函数调用栈的层次结构，支持展开/折叠和局部变量查看。

#### 构造函数
```java
public StackPanel(VMRVisualBridge bridge)
```

#### 主要方法
```java
// 栈显示
public void updateDisplay()
public void expandFrame(int frameIndex)
public void collapseFrame(int frameIndex)
public void expandAll()
public void collapseAll()

// 栈帧访问
public JComponent getFrameComponent(int frameIndex)
public List<JComponent> getFrameComponents()

// 事件处理
public void callStackChanged()
```

#### 使用示例
```java
// 创建调用栈面板
StackPanel stackPanel = new StackPanel(stateModel);

// 更新显示
stackPanel.updateDisplay();

// 展开第一个栈帧
stackPanel.expandFrame(0);

// 展开所有栈帧
stackPanel.expandAll();
```

### 15. ControlPanel - 控制面板

#### 类概述
`ControlPanel` 提供执行控制按钮和断点管理界面。

#### 构造函数
```java
public ControlPanel(VMRVisualBridge bridge)
```

#### 主要方法
```java
// 按钮访问
public JButton getStartButton()
public JButton getPauseButton()
public JButton getStopButton()
public JButton getStepButton()
public JButton getStepOverButton()
public JButton getStepOutButton()

// 状态更新
public void updateButtonStates()
public void setExecutionMode(ExecutionMode mode)

// 断点界面
public JButton getAddBreakpointButton()
public JButton getRemoveBreakpointButton()
public JButton getClearBreakpointsButton()
```

#### 使用示例
```java
// 创建控制面板
ControlPanel controlPanel = new ControlPanel(bridge, controller);

// 更新按钮状态
controlPanel.updateButtonStates();

// 获取按钮引用
JButton startButton = controlPanel.getStartButton();
JButton stepButton = controlPanel.getStepButton();
```

### 16. StatusPanel - 状态面板

#### 类概述
`StatusPanel` 显示虚拟机执行状态和统计信息。

#### 构造函数
```java
public StatusPanel(VMRVisualBridge bridge)
```

#### 主要方法
```java
// 状态更新
public void updateDisplay()
public void setStatusMessage(String message)
public void setStatusColor(Color color)

// 统计显示
public void updateStatistics()
public void setShowInstructions(boolean show)
public void setShowTime(boolean show)
public void setShowMemory(boolean show)

// 事件处理
public void executionStarted()
public void executionPaused()
public void executionFinished()
public void executionError(Throwable error)
```

#### 使用示例
```java
// 创建状态面板
StatusPanel statusPanel = new StatusPanel(stateModel);

// 更新显示
statusPanel.updateDisplay();

// 设置状态消息
statusPanel.setStatusMessage("执行中...");
statusPanel.setStatusColor(Color.GREEN);

// 更新统计信息
statusPanel.updateStatistics();
```

## ⚙️ 工具类 API

### 17. ConfigPersistence - 配置持久化工具

#### 类概述
`ConfigPersistence` 负责可视化配置的保存和加载。

#### 主要方法
```java
// 配置保存
public static void saveConfig(String path, VMRConfig config)
public static void saveConfig(File file, VMRConfig config)

// 配置加载
public static VMRConfig loadConfig(String path)
public static VMRConfig loadConfig(File file)

// 默认配置
public static VMRConfig getDefaultConfig()
```

#### 配置类
```java
public class VMRConfig {
    private int windowWidth;
    private int windowHeight;
    private boolean windowMaximized;
    private Point windowPosition;
    private VMRTheme theme;
    private int updateInterval;
    private int maxHistorySize;
    private Set<Integer> breakpoints;
    private boolean showHexValues;
    private boolean showDecimalValues;
    private boolean showAsciiValues;
    
    // Getter 和 Setter 方法
    // 序列化和反序列化方法
}
```

#### 使用示例
```java
// 保存配置
VMRConfig config = new VMRConfig();
config.setWindowWidth(1200);
config.setWindowHeight(800);
config.setTheme(VMRTheme.CLASSIC);
ConfigPersistence.saveConfig("vizvmr-config.json", config);

// 加载配置
VMRConfig loadedConfig = ConfigPersistence.loadConfig("vizvmr-config.json");
```

## 🧪 扩展和自定义

### 自定义监听器示例
```java
// 自定义状态监听器
public class CustomStateListener implements VMRStateListener {
    private final List<RegisterChangeEvent> registerHistory = new ArrayList<>();
    
    @Override
    public void registerChanged(RegisterChangeEvent event) {
        // 记录寄存器变化历史
        registerHistory.add(event);
        
        // 特殊处理特定寄存器
        if (event.getRegisterIndex() == 1) {
            System.out.println("寄存器 r1 被修改");
        }
    }
    
    @Override
    public void memoryChanged(MemoryChangeEvent event) {
        // 监控特定内存区域
        if (event.getAddress() >= 0x1000 && event.getAddress() < 0x2000) {
            System.out.println("堆内存被修改");
        }
    }
    
    public List<RegisterChangeEvent> getRegisterHistory() {
        return Collections.unmodifiableList(registerHistory);
    }
}

// 使用自定义监听器
CustomStateListener customListener = new CustomStateListener();
stateModel.addStateListener(customListener);
```

### 自定义面板组件示例
```java
// 自定义信息面板
public class CustomInfoPanel extends JPanel implements VMRStateListener {
    private final JLabel infoLabel;
    private final VMRStateModel stateModel;
    
    public CustomInfoPanel(VMRStateModel stateModel) {
        this.stateModel = stateModel;
        this.infoLabel = new JLabel();
        
        setLayout(new BorderLayout());
        add(infoLabel, BorderLayout.CENTER);
        
        // 注册监听器
        stateModel.addStateListener(this);
    }
    
    @Override
    public void registerChanged(RegisterChangeEvent event) {
        updateInfo();
    }
    
    @Override
    public void pcChanged(PCChangeEvent event) {
        updateInfo();
    }
    
    private void updateInfo() {
        int pc = stateModel.getProgramCounter();
        int steps = (int) stateModel.getExecutionSteps();
        infoLabel.setText(String.format("PC: 0x%04X | 已执行: %d 指令", pc, steps));
    }
}

// 使用自定义面板
CustomInfoPanel infoPanel = new CustomInfoPanel(stateModel);
mainFrame.add(infoPanel, BorderLayout.SOUTH);
```

### 插件系统扩展点
```java
// 插件接口
public interface VMRPlugin {
    void initialize(VMRStateModel stateModel, VMRVisualBridge bridge);
    void shutdown();
    String getName();
    String getDescription();
}

// 示例插件：性能分析器
public class PerformanceAnalyzerPlugin implements VMRPlugin {
    private final Map<Integer, Integer> instructionCount = new HashMap<>();
    private long startTime;
    
    @Override
    public void initialize(VMRStateModel stateModel, VMRVisualBridge bridge) {
        startTime = System.currentTimeMillis();
        
        // 监听指令执行
        stateModel.addExecutionListener(new VMRExecutionListener() {
            @Override
            public void afterInstructionExecute(InstructionExecutionEvent event) {
                int opcode = extractOpcode(event.getInstruction());
                instructionCount.merge(opcode, 1, Integer::sum);
            }
        });
    }
    
    @Override
    public void shutdown() {
        // 输出性能报告
        long duration = System.currentTimeMillis() - startTime;
        System.out.println("执行时间: " + duration + "ms");
        System.out.println("指令统计:");
        instructionCount.forEach((opcode, count) -> 
            System.out.printf("  指令 %d: %d 次%n", opcode, count));
    }
    
    @Override
    public String getName() {
        return "性能分析器";
    }
    
    @Override
    public String getDescription() {
        return "分析指令执行频率和性能指标";
    }
}
```

## 🔧 高级配置

### 线程配置
```java
// 配置执行线程优先级
Thread executionThread = bridge.getExecutionThread();
if (executionThread != null) {
    executionThread.setPriority(Thread.NORM_PRIORITY);
}

// 配置EDT更新频率
SwingUtilities.invokeLater(() -> {
    // GUI更新代码
});
```

### 性能调优
```java
// 限制状态更新频率
stateModel.setUpdateThrottle(100); // 100ms更新间隔

// 批处理事件
stateModel.enableBatchUpdates(true);

// 内存优化
stateModel.setMaxHistorySize(500); // 限制历史记录大小
```

## 📚 最佳实践

### 1. 事件处理最佳实践
```java
// 在EDT中处理GUI更新
public class SafeStateListener implements VMRStateListener {
    @Override
    public void registerChanged(RegisterChangeEvent event) {
        SwingUtilities.invokeLater(() -> {
            // 安全地更新GUI
            updateRegisterDisplay(event);
        });
    }
}
```

### 2. 内存管理最佳实践
```java
// 及时清理资源
public class ResourceManager {
    public void cleanup(VMRStateModel stateModel, VMRVisualBridge bridge) {
        // 移除监听器
        stateModel.removeStateListener(listener);
        stateModel.removeExecutionListener(executorListener);
        
        // 停止执行线程
        if (bridge.isRunning()) {
            bridge.stopExecution();
        }
        
        // 清理历史记录
        history.clear();
    }
}
```

### 3. 错误处理最佳实践
```java
// 统一的错误处理
public class ErrorHandler implements VMRExecutionListener {
    @Override
    public void executionError(Throwable error, int pc) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(null,
                String.format("执行错误 at PC=0x%04X: %s", pc, error.getMessage()),
                "执行错误", JOptionPane.ERROR_MESSAGE);
        });
        
        // 记录日志
        logger.error("VM execution error at PC={}", pc, error);
    }
}
```

---

**API版本**: 1.0  
**创建日期**: 2026-01-14  
**最后更新**: 2026-01-14  
**维护者**: EP18R开发团队
