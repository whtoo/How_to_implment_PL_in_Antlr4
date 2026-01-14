# EP18R 寄存器虚拟机可视化模块 (vizvmr)

## 概述

vizvmr 是一个基于 Java Swing 的可视化模块，用于实时展示 EP18R 寄存器虚拟机的执行状态。

## 功能特性

### 核心功能
- **实时寄存器监控**: 4x4 网格显示 16 个寄存器（r0-r15）
- **内存可视化**: 可滚动表格显示堆内存和全局变量
- **代码反汇编**: 显示反汇编后的指令，高亮当前 PC 位置
- **调用栈追踪**: 显示函数调用栈和返回地址
- **执行控制**: 开始、暂停、停止、单步执行
- **断点支持**: 设置/清除断点，条件断点

### 高级功能
- **执行历史记录**: 支持撤销/重做操作
- **状态持久化**: 保存和加载配置
- **事件监听**: 完整的观察者模式实现

## 架构设计

```
vizvmr/
├── src/main/java/org/teachfx/antlr4/ep18r/vizvmr/
│   ├── VizVMRLauncher.java          # 启动器
│   ├── core/                         # 核心状态管理
│   │   ├── VMRStateModel.java       # 状态模型
│   │   └── VMRExecutionHistory.java # 执行历史
│   ├── event/                        # 事件系统
│   │   ├── VMRStateListener.java    # 状态监听器
│   │   ├── VMRExecutionListener.java # 执行监听器
│   │   └── *.java                   # 事件类
│   ├── integration/                  # 集成组件
│   │   ├── VMRInstrumentation.java  # 插桩适配器
│   │   └── VMRVisualBridge.java     # 可视化桥接器
│   ├── controller/                   # 控制器
│   │   ├── VMRStepController.java   # 单步控制器
│   │   └── VMRBreakpointManager.java # 断点管理器
│   ├── ui/                           # 用户界面
│   │   ├── MainFrame.java           # 主窗口
│   │   └── panel/                   # 面板组件
│   │       ├── RegisterPanel.java   # 寄存器面板
│   │       ├── MemoryPanel.java     # 内存面板
│   │       ├── CodePanel.java       # 代码面板
│   │       ├── StackPanel.java      # 调用栈面板
│   │       ├── ControlPanel.java    # 控制面板
│   │       └── StatusPanel.java     # 状态面板
│   └── util/                         # 工具类
│       └── ConfigPersistence.java   # 配置持久化
└── src/test/java/                    # 测试代码
```

## 使用方法

### 快速启动

```java
public class Main {
    public static void main(String[] args) {
        // 创建虚拟机
        VMConfig config = new VMConfig.Builder()
            .setHeapSize(1024 * 1024)
            .setStackSize(1024)
            .build();
        RegisterVMInterpreter vm = new RegisterVMInterpreter(config);

        // 创建可视化组件
        VMRStateModel stateModel = new VMRStateModel(1024 * 1024, 256, 100);
        VMRVisualBridge bridge = new VMRVisualBridge(vm, stateModel);

        // 加载并运行
        bridge.loadCode(new FileInputStream("program.vm"));
        bridge.start();
    }
}
```

### 监听事件

```java
// 注册状态监听器
bridge.getStateModel().addStateListener(new VMRStateListener() {
    @Override
    public void registerChanged(RegisterChangeEvent event) {
        System.out.printf("r%d: 0x%08X → 0x%08X%n",
            event.getRegisterNumber(),
            event.getOldValue(),
            event.getNewValue());
    }

    @Override
    public void pcChanged(PCChangeEvent event) {
        System.out.printf("PC: 0x%04X → 0x%04X%n",
            event.getOldPC(),
            event.getNewPC());
    }
});
```

### 使用断点

```java
VMRStepController controller = new VMRStepController(bridge);

// 设置断点
controller.setBreakpoint(0x100);

// 单步执行
controller.stepInto();

// 步过函数调用
controller.stepOver();

// 步出函数
controller.stepOut();

// 运行到断点
controller.continueExecution();
```

## 与 EP18R 集成

### 依赖配置

在 `pom.xml` 中添加：

```xml
<dependency>
    <groupId>org.teachfx</groupId>
    <artifactId>ep18r</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 运行可视化

```bash
cd vizvmr
mvn compile exec:java
```

## 技术规范

| 项目 | 规范 |
|------|------|
| Java 版本 | 21+ |
| GUI 框架 | Java Swing |
| 构建工具 | Maven 3.8+ |
| 测试框架 | JUnit 5 |
| 包名 | `org.teachfx.antlr4.ep18r.vizvmr` |

## 事件类型

### 状态事件
- `RegisterChangeEvent` - 寄存器值变化
- `MemoryChangeEvent` - 内存值变化
- `PCChangeEvent` - 程序计数器变化
- `VMStateChangeEvent` - 虚拟机状态变化

### 执行事件
- `InstructionExecutionEvent` - 指令执行
- `VMRExecutionListener` - 执行监听器接口

## 性能考虑

- Swing 更新在 EDT 中执行
- 状态更新使用批量处理
- 内存同步限制在前 1024 个地址
- 历史记录默认限制 1000 个快照
