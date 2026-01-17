# EP18R 寄存器虚拟机可视化模块 (vizvmr) - JavaFX版本

## 概述

vizvmr 是一个基于 JavaFX 和 RxJava3 的统一响应式可视化模块，用于实时展示 EP18R 寄存器虚拟机的执行状态。

## 功能特性

### 核心功能
- **实时寄存器监控**: 4x4 网格显示 16 个寄存器（r0-r15）
- **内存可视化**: 可滚动表格显示堆内存和全局变量
- **代码反汇编**: 反汇编指令列表，高亮当前 PC 位置，支持滚动
- **调用栈追踪**: 树形结构显示函数调用栈帧，支持滚动
- **执行控制**: 开始、暂停、停止、单步执行
- **断点支持**: 设置/清除断点，自动步进模式
- **自动步进**: 支持可配置的自动步进延迟（默认1秒）

### 高级功能
- **响应式状态管理**: 基于 RxJava3 的响应式编程模型
- **事件驱动架构**: 完整的事件发布-订阅机制
- **统一事件总线**: VMState、PC、寄存器、内存等状态流
- **线程安全**: 使用 Platform.runLater() 确保 UI 更新在 JavaFX 应用线程执行
- **非阻塞执行**: VM 执行在后台线程，避免 UI 冻结

## 架构设计

```
vizvmr/
├── src/main/java/org/teachfx/antlr4/ep18r/vizvmr/
│   ├── UnifiedVizVMRLauncher.java   # JavaFX 启动器
│   ├── unified/                      # 统一响应式架构
│   │   ├── core/                 # 核心状态管理
│   │   │   ├── IRxVMStateManager.java    # 状态管理器接口
│   │   │   ├── RxVMStateManagerImpl.java # 响应式状态管理器实现
│   │   │   ├── IVM.java              # VM 抽象接口
│   │   │   ├── IVMAdapter.java         # VM 接口适配器
│   │   │   └── VMTypes.java          # VM 类型定义
│   │   ├── bridge/               # 事件适配层
│   │   │   └── UnifiedEventAdapter.java # 统一事件适配器
│   │   └── event/                # 事件系统
│   │       ├── IVMEventBus.java       # 事件总线接口
│   │       ├── VMEventBusImpl.java    # 事件总线实现
│   │       └── VMEvent.java          # 事件定义
│   └── ui/javafx/              # JavaFX UI 组件
│       ├── CodeView.java           # 代码视图（支持滚动）
│       ├── RegisterView.java        # 寄存器视图
│       ├── MemoryView.java          # 内存视图（堆+全局变量）
│       ├── StackView.java           # 调用栈视图（支持滚动）
│       └── StatusView.java          # 状态视图
└── pom.xml                         # Maven 构建配置
```

## 使用方法

### 快速启动

```bash
cd vizvmr
mvn clean compile exec:java
```

### 加载代码

1. 点击"文件" → "加载代码..."
2. 选择 `.vmr` 或 `.vm` 文件
3. 代码自动反汇编并显示在代码视图中

### 执行控制

```java
// 开始执行（自动步进模式，1秒/指令）
stateManager.start();

// 暂停执行
stateManager.pause();

// 停止执行
stateManager.stop();

// 单步执行
stateManager.step();
```

## 事件系统

### 事件类型
- `INSTRUCTION_FETCHED` - 指令获取事件（执行前）
- `INSTRUCTION_EXECUTED` - 指令执行事件（执行后）
- `REGISTER_CHANGED` - 寄存器值变化
- `MEMORY_CHANGED` - 内存值变化
- `PC_CHANGED` - 程序计数器变化
- `STACK_CHANGED` - 调用栈变化
- `STATE_CHANGED` - VM 状态变化
- `BREAKPOINT_HIT` - 断点命中

### 响应式状态流

```java
// 订阅寄存器状态流
stateManager.registers()
    .subscribe(registers -> {
        Platform.runLater(() -> registerView.updateRegisters(registers));
    });

// 订阅 PC 状态流
stateManager.pc()
    .subscribe(pc -> {
        Platform.runLater(() -> codeView.highlightPC(pc));
    });

// 订阅调用栈状态流
stateManager.callStack()
    .subscribe(stack -> {
        Platform.runLater(() -> stackView.updateStack(stack));
    });
```

## 技术规范

| 项目 | 规范 |
|------|------|
| Java 版本 | 21+ |
| GUI 框架 | JavaFX |
| 响应式框架 | RxJava3 |
| 构建工具 | Maven 3.8+ |
| 测试框架 | JUnit 5 |
| 包名 | `org.teachfx.antlr4.ep18r.vizvmr` |

## 架构特点

### 1. 响应式状态管理
- 使用 `BehaviorSubject` 提供状态流
- 通过 `Observable` 暴露可观测状态
- 自动状态同步和传播

### 2. 线程模型
- **VM 执行**: 在 `ForkJoinPool.commonPool()` 后台线程执行
- **UI 更新**: 使用 `Platform.runLater()` 在 JavaFX 应用线程执行
- **事件订阅**: 使用 `Schedulers.single()` 确保顺序性
- **非阻塞**: VM 执行不会阻塞 UI 线程

### 3. 自动步进模式
- 可配置步进延迟（默认 1000ms）
- 在每条指令执行后延迟，让 UI 有时间更新
- 适合可视化演示和调试

### 4. 滚动支持
- 代码视图使用 `ScrollPane` 包装 `VBox`
- 调用栈使用 `ScrollPane` 包装 `TreeView`
- 支持垂直滚动条自动显示
- 固定容器高度，超出内容可滚动

## 性能优化

- **状态批量更新**: RxJava 支持多状态订阅合并
- **UI 线程隔离**: VM 执行与 UI 渲染分离
- **事件异步传递**: 避免阻塞事件发布
- **线程调度优化**: 使用专用调度器管理并发

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

### VM 接口适配

`IVMAdapter` 提供统一的 VM 抽象层：
- 反汇编代码获取
- 调用栈数据获取
- 断点管理
- 自动步进模式控制

## 版本历史

### v2.1 (当前) - JavaFX 统一响应式版本
- 完全迁移到 JavaFX
- 基于 RxJava3 的响应式架构
- 支持 UI 滚动（代码视图、调用栈）
- 修复 UI 线程冻结问题（VM 在后台线程执行）
- 正确的事件订阅时机（INSTRUCTION_EXECUTED）
- 自动步进延迟默认 1 秒

### v1.5 - Swing + JavaFX 双框架支持
- 添加 JavaFX 启动器
- 统一事件总线
- 核心状态管理

### v1.0 - Swing 版本
- 基于 Java Swing 的实现
- 观察者模式状态管理

## 已知限制

- 全局变量同步在首次执行时可能显示为空（VM 初始化状态）
- 自动步进模式下，VM 执行速度受延迟配置影响
- 调用栈只显示返回地址，不显示完整栈帧信息

## 未来计划

- [ ] 支持条件断点
- [ ] 添加执行性能统计面板
- [ ] 支持多个 VM 实例同时运行
- [ ] 添加导出执行日志功能
- [ ] 支持热重载代码
