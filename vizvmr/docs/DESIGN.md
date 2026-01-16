# EP18R 可视化模块 (vizvmr) 设计文档

## 📋 概述

**vizvmr** 是一个独立的可视化模块，为 EP18R 寄存器虚拟机提供实时的图形化执行过程展示。该模块采用观察者模式设计，通过事件监听器捕获虚拟机执行状态，使用 Java Swing 构建交互式 GUI 界面。

## 🎯 设计目标

1. **实时可视化**: 显示寄存器文件、内存、调用栈等虚拟机组件的实时状态
2. **交互式控制**: 提供单步执行、断点设置、暂停/继续等调试功能
3. **状态跟踪**: 记录和回放指令执行历史
4. **教学友好**: 界面直观，适合编译器原理教学演示
5. **模块化设计**: 独立模块，与 EP18R 虚拟机松耦合

## 🏗️ 系统架构

### 整体架构图
```
┌─────────────────────────────────────────────────────────┐
│                    vizvmr 可视化模块                     │
├─────────────────────────────────────────────────────────┤
│  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐   │
│  │ GUI层   │  │控制层   │  │模型层   │  │集成层   │   │
│  │         │  │         │  │         │  │         │   │
│  └─────────┘  └─────────┘  └─────────┘  └─────────┘   │
└─────────────────────────────────────────────────────────┘
         │              │              │              │
         ▼              ▼              ▼              ▼
┌─────────────────────────────────────────────────────────┐
│                EP18R 寄存器虚拟机模块                    │
│                 (RegisterVMInterpreter)                  │
└─────────────────────────────────────────────────────────┘
```

### 架构层次
1. **GUI层 (Presentation Layer)**: Java Swing 组件，负责界面渲染和用户交互
2. **控制层 (Control Layer)**: 执行控制器、断点管理器、历史记录管理器
3. **模型层 (Model Layer)**: 状态模型、事件系统、数据管理层
4. **集成层 (Integration Layer)**: 虚拟机插桩适配器、桥接器、接口适配器

## 📁 模块结构

### 包结构
```
org.teachfx.antlr4.ep18r.vizvmr/
├── VizVMRLauncher.java            # 独立启动器
│
├── core/                          # 核心模型
│   ├── VMRStateModel.java         # 状态数据模型
│   └── VMRExecutionHistory.java   # 执行历史记录
│
├── event/                         # 事件系统
│   ├── VMRStateListener.java      # 状态监听器接口 (含vmStateChanged)
│   ├── VMRExecutionListener.java  # 执行监听器接口
│   ├── VMRStateEvent.java         # 状态事件基类
│   ├── RegisterChangeEvent.java   # 寄存器变化事件
│   ├── MemoryChangeEvent.java     # 内存变化事件
│   ├── PCChangeEvent.java         # 程序计数器变化事件
│   ├── InstructionExecutionEvent.java # 指令执行事件
│   └── VMStateChangeEvent.java    # 虚拟机状态变化事件
│
├── integration/                   # 集成层
│   ├── VMRInstrumentation.java    # 虚拟机插桩适配器
│   └── VMRVisualBridge.java       # 可视化桥接器
│
├── controller/                    # 控制器
│   ├── VMRStepController.java     # 单步执行控制器
│   └── VMRBreakpointManager.java  # 断点管理器
│
├── ui/                            # 用户界面
│   ├── MainFrame.java             # 主窗口框架
│   ├── panel/                     # UI组件
│   │   ├── RegisterPanel.java     # 寄存器显示面板
│   │   ├── MemoryPanel.java       # 内存显示面板
│   │   ├── CodePanel.java         # 代码显示面板
│   │   ├── StackPanel.java        # 调用栈面板
│   │   ├── ControlPanel.java      # 控制面板
│   │   ├── StatusPanel.java       # 状态面板
│   │   └── LogPanel.java          # 日志面板 (新增)
│   └── dialog/                    # 对话框 (预留)
│
└── util/                          # 工具类
    └── ConfigPersistence.java     # 配置持久化 (新增)
```

## 🔧 核心组件设计

### 1. 事件监听器系统 (Event Listener System)

#### 状态监听器 (VMRStateListener)
```java
public interface VMRStateListener extends EventListener {
    void registerChanged(RegisterChangeEvent event);
    void memoryChanged(MemoryChangeEvent event);
    void pcChanged(PCChangeEvent event);
    void vmStateChanged(VMStateChangeEvent event);  // 添加：虚拟机状态变化监听
    default void registersUpdated(RegisterChangeEvent[] events);
    default void memoryUpdated(MemoryChangeEvent[] events);
}
```

#### 执行监听器 (VMRExecutionListener)
```java
public interface VMRExecutionListener extends EventListener {
    default void instructionFetched(InstructionExecutionEvent event);
    default void beforeInstructionDecode(InstructionExecutionEvent event);
    default void beforeInstructionExecute(InstructionExecutionEvent event);
    default void afterInstructionExecute(InstructionExecutionEvent event);
    void vmStateChanged(VMStateChangeEvent event);
    default void executionError(Throwable error, int pc);
    default void executionStarted();
    default void executionFinished();
    default void executionPaused();
}
```

### 2. 状态模型 (VMRStateModel)

#### 职责
- 管理虚拟机所有状态（寄存器、内存、调用栈、PC等）
- 提供状态变更通知机制
- 维护修改追踪和性能统计
- 支持批处理更新以提高性能

#### 关键数据结构
```java
public class VMRStateModel {
    // 状态存储
    private final int[] registers;                 // 16个寄存器值
    private int[] heap;                            // 堆内存 (非final，支持restoreSnapshot)
    private int[] globals;                         // 全局变量 (非final，支持restoreSnapshot)
    private final StackFrame[] callStack;          // 调用栈
    private int framePointer;                      // 当前帧指针
    private int programCounter;                    // 程序计数器
    private int heapAllocPointer;                  // 堆分配指针

    // 修改追踪
    private final boolean[] registerModified;      // 寄存器修改标记
    private final Set<Integer> modifiedMemoryAddresses; // 内存修改地址
    private final Set<Integer> modifiedHeapAddresses;   // 堆修改地址

    // 监听器管理
    private final List<VMRStateListener> stateListeners;
    private final List<VMRExecutionListener> executionListeners;

    // 执行状态
    private volatile VMStateChangeEvent.State vmState;
    private long executionSteps;
    private long startTime;
    private int eventStepNumber;                   // 事件步数计数器
}
```

### 3. 虚拟机插桩适配器 (VMRInstrumentation)

#### 设计模式：适配器模式 + 反射
- **目的**: 在不修改 EP18R 源代码的情况下监听虚拟机内部状态
- **实现**: 使用 Java 反射访问虚拟机私有字段和方法
- **关键插桩点**:
  - 寄存器读写操作
  - 内存访问操作
  - 程序计数器更新
  - 调用栈操作
  - 指令执行前后

#### 反射字段访问
```java
public class VMRInstrumentation {
    // 反射字段缓存
    private Field registersField;
    private Field programCounterField;
    private Field framePointerField;
    private Field heapField;
    private Field heapAllocPointerField;
    private Field callStackField;
    private Field runningField;
    
    // 同步状态到模型
    public void syncState() {
        syncRegisters();
        syncMemory();
        syncCallStack();
        syncProgramCounter();
    }
}
```

### 4. 可视化桥接器 (VMRVisualBridge)

#### 职责
- 连接虚拟机和可视化界面
- 管理执行线程和控制流
- 提供反汇编支持
- 处理执行控制（启动、暂停、停止、单步）

#### 执行控制状态机
```
         ┌─────────┐
         │  空闲    │
         └────┬────┘
              │ loadProgram()
              ▼
         ┌─────────┐
         │  已加载  │
         └────┬────┘
              │ startExecution()
              ▼
         ┌─────────┐
         │ 执行中   │◄──┐
         └────┬────┘   │
    pause()   │   resume()│
              ▼          │
         ┌─────────┐    │
         │ 已暂停   │────┘
         └────┬────┘
              │ stopExecution()
              ▼
         ┌─────────┐
         │  已停止  │
         └─────────┘
```

### 5. GUI界面设计

#### 主窗口布局 (MainFrame)
```
+-------------------------------------------------+
| [菜单栏] File View Run Debug Help               |
+-------------------------------------------------+
| [工具栏] ▶ ⏸ ⏹ ⏭  🔍 💾 📊                    |
+-------------------------------------------------+
| [代码面板]        | [寄存器面板]                |
| PC: 0x004 li r1, 100| r0: 0x00000000 (0)       |
| PC: 0x008 add r2, r1, r1 | r1: 0x00000064 (100)|
| PC: 0x00C add r3, r2, r1 | r2: 0x000000c8 (200)|
| ...               | ...                         |
|                   | r15: 0x00000000 (0)        |
+-------------------+-----------------------------+
| [内存面板]        | [调用栈面板]                |
| 0x0000: 0x12345678| Frame 0: main (PC=0x004)   |
| 0x0004: 0x9abcdef0| Frame 1: func1 (PC=0x024)  |
| 0x0008: 0xdeadbeef| Frame 2: func2 (PC=0x044)  |
| ...               | ...                         |
+-------------------+-----------------------------+
| [状态栏] 已执行: 153 指令 | 耗时: 0.45s | ✅运行中|
+-------------------------------------------------+
```

#### 组件详细设计

##### 寄存器面板 (RegisterPanel)
- **布局**: 4×4网格，显示16个寄存器
- **颜色编码**:
  - 🔴 红色: 最近修改的寄存器
  - 🔵 蓝色: 特殊寄存器 (r13-SP, r14-FP, r15-LR)
  - ⚪ 灰色: 未修改的寄存器
  - 🟢 绿色: 零寄存器 (r0)
- **显示格式**: 十六进制 (0x...) + 十进制 + 字符表示
- **交互**: 双击修改值，右键查看历史

##### 内存面板 (MemoryPanel)
- **布局**: 可滚动的表格，每行显示4个内存字
- **显示模式**:
  - 十六进制: `0x12345678`
  - 十进制: `305419896`
  - ASCII: 显示可打印字符 (32-126)
- **功能**: 地址搜索、跳转、内存范围查看

##### 代码面板 (CodePanel)
- **功能**: 显示反汇编指令，高亮当前PC
- **反汇编器**: 使用 EP18R 的 `RegisterDisAssembler`
- **标记**:
  - ● 红色圆点: 断点
  - 🟡 黄色背景: 当前PC位置
  - 🟢 浅绿背景: 已执行指令
- **交互**: 点击设置断点，右键跳转

##### 调用栈面板 (StackPanel)
- **显示**: 垂直列表，显示调用栈层次
- **信息**: 函数名、返回地址、局部变量数
- **展开/折叠**: 查看局部变量详情
- **导航**: 双击跳转到返回地址

##### 控制面板 (ControlPanel)
```java
public class ControlPanel extends JPanel {
    // 控制按钮
    private JButton btnStart;      // ▶ 开始执行
    private JButton btnPause;      // ⏸ 暂停执行
    private JButton btnStop;       // ⏹ 停止执行
    private JButton btnStep;       // ⏭ 单步执行
    private JButton btnStepOver;   // ⏭⏭ 单步步过
    private JButton btnStepOut;    // ⏭↗ 单步步出
    private JButton btnRunToBreak; // ⏭● 运行到断点
    
    // 断点管理
    private JButton btnAddBreak;   // +● 添加断点
    private JButton btnRemoveBreak;// -● 删除断点
    private JButton btnClearBreaks;// ×● 清除所有断点
}
```

##### 状态面板 (StatusPanel)
- **执行统计**: 已执行指令数、运行时间、指令频率
- **虚拟机状态**: 运行中、已暂停、已停止、错误
- **性能指标**: 平均指令时间、内存使用率
- **通知区域**: 显示警告、错误、信息消息

## 🔌 与EP18R的集成接口

### 1. 扩展 IVirtualMachine 接口
```java
// 建议在 EP18R 中扩展接口
public interface IVirtualMachineVisualizable extends IVirtualMachine {
    // 监听器管理
    void addStateListener(VMRStateListener listener);
    void removeStateListener(VMRStateListener listener);
    void addExecutionListener(VMRExecutionListener listener);
    void removeExecutionListener(VMRExecutionListener listener);
    
    // 状态获取
    int[] getRegisterValues();
    int getProgramCounter();
    StackFrame[] getCallStack();
    byte[] getCodeMemory();
    Object[] getConstantPool();
    
    // 执行控制
    void setExecutionMode(ExecutionMode mode);
    void setBreakpoints(Set<Integer> breakpoints);
    void pauseExecution();
    void resumeExecution();
}
```

### 2. 实际集成方案
由于不能修改 EP18R 源代码，采用 **反射适配器模式**：

1. **VMRInstrumentation**: 通过反射访问虚拟机内部状态
2. **状态同步**: 定期同步寄存器、内存、调用栈状态
3. **事件触发**: 在虚拟机关键位置插入事件触发点
4. **控制代理**: 通过桥接器控制虚拟机执行流程

### 3. 数据流
```
虚拟机执行 → 状态变化 → 事件触发 → 监听器通知 → 状态模型更新 → GUI刷新
    ↑           ↑           ↑           ↑           ↑           ↑
    │           │           │           │           │           │
控制命令 ←── 用户操作 ←── GUI事件 ←── 状态同步 ←── 模型查询 ←── 界面渲染
```

## 📊 数据结构设计

### 1. 事件数据结构
```java
public abstract class VMRStateEvent extends EventObject {
    private final long timestamp;      // 事件时间戳
    private final int stepNumber;      // 执行步数
    
    public abstract String getDescription(); // 事件描述
}

public class RegisterChangeEvent extends VMRStateEvent {
    private final int registerIndex;   // 寄存器索引 (0-15)
    private final int oldValue;        // 旧值
    private final int newValue;        // 新值
    private final String registerName; // 寄存器名称 (r0-r15)
}

public class MemoryChangeEvent extends VMRStateEvent {
    private final int address;         // 内存地址
    private final int oldValue;        // 旧值
    private final int newValue;        // 新值
    private final MemoryType type;     // 内存类型 (HEAP, GLOBAL, STACK)
}
```

### 2. 状态快照
```java
public class VMRStateSnapshot {
    private final int[] registers;     // 寄存器值快照
    private final int[] heap;          // 堆内存快照
    private final int[] globals;       // 全局变量快照
    private final StackFrame[] callStack; // 调用栈快照
    private final int programCounter;  // PC快照
    private final long timestamp;      // 快照时间
    private final int stepNumber;      // 执行步数
    
    // 序列化和反序列化支持
    public byte[] serialize() { ... }
    public static VMRStateSnapshot deserialize(byte[] data) { ... }
}
```

## 🚀 执行控制设计

### 1. 执行模式枚举
```java
public enum ExecutionMode {
    RUN,                    // 连续执行
    STEP,                   // 单步执行
    STEP_OVER,              // 单步步过（遇到call执行整个函数）
    STEP_OUT,               // 单步步出（执行到当前函数返回）
    STEP_TO_BREAKPOINT,     // 运行到下一个断点
    PAUSE,                  // 暂停执行
    STOP                    // 停止执行
}
```

### 2. 断点系统
```java
public class VMRBreakpointManager {
    private final Set<Integer> breakpoints; // 断点地址集合
    private final Map<Integer, BreakpointCondition> conditions; // 条件断点
    
    public static class BreakpointCondition {
        private final String expression; // 条件表达式
        private final Predicate<ExecutionContext> predicate; // 条件谓词
        
        public boolean evaluate(ExecutionContext context) {
            return predicate.test(context);
        }
    }
    
    // 断点命中检查
    public boolean shouldBreak(int pc, ExecutionContext context) {
        return breakpoints.contains(pc) && 
               (conditions.get(pc) == null || conditions.get(pc).evaluate(context));
    }
}
```

### 3. 单步执行控制器
```java
public class VMRStepController {
    private ExecutionMode currentMode = ExecutionMode.PAUSE;
    private final VMRVisualBridge bridge;
    private final VMRBreakpointManager breakpointManager;
    
    public void step() {
        if (currentMode == ExecutionMode.STEP) {
            bridge.executeSingleInstruction();
        } else if (currentMode == ExecutionMode.STEP_OVER) {
            executeStepOver();
        } else if (currentMode == ExecutionMode.STEP_OUT) {
            executeStepOut();
        }
    }
    
    private void executeStepOver() {
        int currentDepth = bridge.getCallStackDepth();
        do {
            bridge.executeSingleInstruction();
        } while (bridge.getCallStackDepth() > currentDepth);
    }
}
```

## 🎨 用户界面设计原则

### 1. 响应式设计
- **实时更新**: 状态变化立即反映到界面
- **性能优化**: 批处理更新，避免频繁重绘
- **线程安全**: Swing 更新在 EDT 中执行

### 2. 可用性原则
- **一致性**: 遵循 Java Swing 设计规范
- **反馈**: 用户操作立即得到视觉反馈
- **容错**: 错误处理和优雅降级

### 3. 可访问性
- **键盘导航**: 支持常用快捷键
- **高对比度**: 支持色盲友好配色方案
- **缩放支持**: 界面元素可缩放

### 4. 主题支持
```java
public enum VMRTheme {
    LIGHT("Light"),      // 亮色主题
    DARK("Dark"),        // 暗色主题
    CLASSIC("Classic"),  // 经典主题（类似IDE）
    HIGH_CONTRAST("High Contrast"); // 高对比度主题
}
```

## 🔧 配置管理

### 1. 配置文件格式 (JSON)
```json
{
  "window": {
    "width": 1200,
    "height": 800,
    "maximized": false,
    "position": {"x": 100, "y": 100}
  },
  "theme": "CLASSIC",
  "updateInterval": 100,
  "maxHistorySize": 1000,
  "breakpoints": [0x100, 0x200, 0x300],
  "showHexValues": true,
  "showDecimalValues": true,
  "showAsciiValues": false
}
```

### 2. 配置管理类
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
    
    // 配置加载和保存
    public void loadFromFile(String path) { ... }
    public void saveToFile(String path) { ... }
    public void applyToUI(MainFrame frame) { ... }
}
```

## 🧪 测试策略

### 1. 单元测试
- **事件系统**: 测试事件创建、分发、处理
- **状态模型**: 测试状态更新、通知、序列化
- **工具类**: 测试格式化、颜色管理、资源加载

### 2. 集成测试
- **虚拟机集成**: 测试与 EP18R 的集成
- **GUI集成**: 测试组件协同工作
- **端到端测试**: 完整工作流测试

### 3. UI测试
- **功能测试**: 测试按钮点击、菜单操作
- **可视化测试**: 测试渲染正确性
- **交互测试**: 测试用户交互流程

### 4. 性能测试
- **内存使用**: 监控内存泄漏
- **响应时间**: 测试界面响应速度
- **并发测试**: 测试多线程环境下的稳定性

## 🔄 部署和维护

### 1. 构建配置 (Maven)
```xml
<dependencies>
    <dependency>
        <groupId>org.teachfx</groupId>
        <artifactId>ep18r</artifactId>
        <version>1.0.0</version>
    </dependency>
</dependencies>
```

### 2. 运行要求
- **Java版本**: JDK 21+
- **内存要求**: 最小 512MB，推荐 1GB
- **屏幕分辨率**: 最小 1024×768，推荐 1280×1024

### 3. 打包发布
- **JAR打包**: 包含所有依赖
- **启动脚本**: 提供跨平台启动脚本
- **安装程序**: 可选安装程序（未来版本）

## 📈 未来扩展

### 1. 功能扩展
- **性能分析器**: 指令频率分析、热点识别
- **内存分析器**: 内存泄漏检测、使用模式分析
- **代码覆盖**: 执行路径覆盖分析
- **反编译器**: 高级反汇编和代码分析

### 2. 技术升级
- **JavaFX迁移**: 从 Swing 迁移到 JavaFX
- **Web版本**: 基于 Web 的可视化界面
- **插件系统**: 支持第三方插件扩展

### 3. 集成扩展
- **IDE集成**: 作为 IDE 插件
- **教学平台**: 集成到在线教学平台
- **CI/CD集成**: 自动化测试和验证

## 📝 设计决策记录

### 1. 架构决策
- **选择Java Swing**: 标准库，无额外依赖，成熟稳定
- **观察者模式**: 松耦合，易于扩展
- **反射适配器**: 不修改 EP18R 源代码，保持模块独立

### 2. 性能决策
- **批处理更新**: 减少事件通知频率
- **懒加载**: 按需加载反汇编和格式化数据
- **缓存机制**: 缓存频繁访问的数据

### 3. 用户体验决策
- **多主题支持**: 适应不同用户偏好
- **快捷键**: 提高专家用户效率
- **渐进式披露**: 高级功能默认隐藏

---

**文档版本**: 1.1
**创建日期**: 2026-01-14
**最后更新**: 2026-01-16
**维护者**: EP18R开发团队

### 更新记录

| 版本 | 日期 | 更新内容 |
|------|------|----------|
| 1.1 | 2026-01-16 | 添加 `vmStateChanged()` 到 VMRStateListener；更新 VMRStateModel 的 heap/globals 为非 final；更新包结构以匹配实际实现 |
| 1.0 | 2026-01-14 | 初始版本 |
