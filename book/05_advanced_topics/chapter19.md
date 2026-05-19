# 第19章：优化器架构与跨模块集成

## 本章概述

本章聚焦于构建可扩展的编译器优化Pass架构和跨模块集成技术。通过学习本章，你将掌握优化器接口设计、适配器模式在跨模块集成中的应用，以及如何通过清晰的接口契约实现不同编译器组件之间的无缝协作。

【你现在站在哪】:
... → [IR生成] → [CFG构建] → [全局优化] → ✅ [优化器架构与跨模块集成] → [代码生成] → ...

## 动机与真实场景

真实场景：你的团队正在开发一个编译器项目，前端团队负责AST到IR的转换，后端团队负责虚拟机实现。你接到任务，需要将前端生成的优化后IR代码转换为虚拟机可执行的字节码，同时支持两种虚拟机目标（栈式VM和寄存器VM）。

具体挑战：
- 前端使用基于符号对象的类型系统（VariableSymbol），后端寄存器分配器使用字符串标识符
- 前端和后端是两个独立的模块（EP21和EP18R），由不同团队维护
- 两个模块的接口设计不同，但需要协同工作
- 需要支持多种优化Pass，且优化顺序需要可配置

如果缺少本章的能力，你将面临：
- 重复实现相同的功能
- 接口耦合严重，难以维护
- 优化Pass难以扩展
- 测试覆盖率低

本章将教你如何：
- 设计统一的优化Pass接口
- 使用适配器模式实现跨模块集成
- 构建可扩展的优化器架构
- 通过清晰的接口契约确保组件之间的松耦合协作

## 人类工程师线：技术与实现

### 核心概念

**优化Pass框架**是编译器中端的核心架构，它将复杂的优化过程分解为多个独立的、可组合的优化步骤。

通俗解释：优化Pass框架就像一条汽车装配线，每个Pass是专门负责某一工序的工作站。一个工作站负责"死代码消除"，另一个负责"常量折叠"。装配线将IR代码按顺序传递给每个工作站。

[图1：优化Pass框架流水线]
```
源IR代码
   ↓
[Pass 1: 死代码消除]
   ↓
[Pass 2: 常量折叠]
   ↓
[Pass 3: 尾递归优化]
   ↓
[Pass N: ...]
   ↓
优化后的IR代码
```

**适配器模式**在跨模块集成中扮演关键角色。当两个模块使用不同的接口设计但需要协同工作时，适配器模式提供一个中间层，将一个接口转换为另一个接口。

类比理解：适配器就像旅行电源适配器。你的笔记本电脑使用三孔插头（EP21的VariableSymbol），酒店只有两孔插座（EP18R的String）。你需要适配器将三孔插头转换为两孔插座。

[图2：适配器模式在跨模块集成中的应用]
```
EP21: VariableSymbol对象 → EP18RRegisterAllocatorAdapter → EP18R: String标识符
                         ↓
                    映射转换
                    VariableSymbol ↔ String
```

### 与仓库 EP 的对应关系

对应 EP：EP21

目录结构：
```
ep21/
├── src/main/java/org/teachfx/antlr4/ep21/
│   ├── pass/cfg/
│   │   ├── IFlowOptimizer.java      // 优化Pass统一接口
│   │   ├── DeadCodeEliminationOptimizer.java  // 死代码消除优化器
│   │   ├── TailRecursionOptimizer.java       // 尾递归优化器
│   │   └── CFGConstants.java      // CFG常量定义
│   └── pass/codegen/
│       ├── IRegisterAllocator.java         // EP21寄存器分配器接口
│       └── EP18RRegisterAllocatorAdapter.java  // EP18R适配器实现
```

关键类/方法说明：

**IFlowOptimizer** - 优化Pass统一接口
```java
public interface IFlowOptimizer<I extends IRNode> {
    /**
     * 对CFG执行优化处理
     * @param cfg 要优化的控制流图
     */
    void onHandle(CFG<I> cfg);
}
```

**EP18RRegisterAllocatorAdapter** - 跨模块接口适配器
```java
public class EP18RRegisterAllocatorAdapter implements IRegisterAllocator {

    private final IRegisterAllocator ep18rAllocator;
    private final Map<VariableSymbol, String> variableToName;
    private final Map<String, VariableSymbol> nameToVariable;
    private int nextVariableId;

    public EP18RRegisterAllocatorAdapter(IRegisterAllocator ep18rAllocator) {
        if (ep18rAllocator == null) {
            throw new IllegalArgumentException("EP18R register allocator cannot be null");
        }
        this.ep18rAllocator = ep18rAllocator;
        this.variableToName = new ConcurrentHashMap<>();
        this.nameToVariable = new ConcurrentHashMap<>();
        this.nextVariableId = 1;
    }

    @Override
    public int allocateRegister(VariableSymbol variable) {
        if (variable == null) {
            throw new IllegalArgumentException("Variable cannot be null");
        }

        String varName = getVariableName(variable);
        return ep18rAllocator.allocate(varName);
    }

    private String getVariableName(VariableSymbol variable) {
        if (variableToName.containsKey(variable)) {
            return variableToName.get(variable);
        }

        String name;
        if (variable.getName() != null) {
            name = variable.getName();
        } else {
            name = "var" + nextVariableId++;
        }

        variableToName.put(variable, name);
        nameToVariable.put(name, variable);
        return name;
    }

    @Override
    public void reset() {
        ep18rAllocator.reset();
        variableToName.clear();
        nameToVariable.clear();
        nextVariableId = 1;
    }

    @Override
    public int getAllocatedRegisterCount() {
        return ep18rAllocator.getAllocatedRegisterCount();
    }

    @Override
    public String generateAllocationReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== EP18R Register Allocator Adapter Report ===\n");
        report.append("Managed variables: ").append(variableToName.size()).append("\n");
        report.append("Allocated registers: ").append(getAllocatedRegisterCount()).append("\n");
        return report.toString();
    }
}
```

**IRegisterAllocator** - EP21寄存器分配器接口
```java
public interface IRegisterAllocator {

    @NotNull
    int allocateRegister(@NotNull VariableSymbol variable);

    @IntRange(from = -1, to = Integer.MAX_VALUE)
    int getStackOffset(@NotNull VariableSymbol variable);

    void reset();

    int getAllocatedRegisterCount();

    int getRegister(@NotNull VariableSymbol variable);

    boolean isSpilled(@NotNull VariableSymbol variable);

    void freeRegister(@NotNull VariableSymbol variable);
}
```

### 实战流程

```bash
# 运行所有优化Pass测试
mvn test -Dtest=*Optimizer*Test

# 运行寄存器分配器集成测试
mvn test -Dtest=RegisterAllocatorIntegrationTest

# 预期输出：
# [INFO] Tests run: X, Failures: 0, Errors: 0, Skipped: 0
# [INFO] BUILD SUCCESS
```

## AI 协作线

### 上下文设计

**源码文件**（按阅读顺序）：
1. `ep21/src/main/java/org/teachfx/antlr4/ep21/pass/cfg/IFlowOptimizer.java`
2. `ep21/src/main/java/org/teachfx/antlr4/ep21/pass/cfg/DeadCodeEliminationOptimizer.java`
3. `ep21/src/main/java/org/teachfx/antlr4/ep21/pass/codegen/IRegisterAllocator.java`
4. `ep21/src/main/java/org/teachfx/antlr4/ep21/pass/codegen/EP18RRegisterAllocatorAdapter.java`

### Prompt 模板

**类型 A：实现新的优化Pass**
```
请为EP21编译器实现公共子表达式消除优化器。

任务目标：
- 实现CommonSubexpressionEliminationOptimizer类
- 实现IFlowOptimizer<IRNode>接口
- 检测并消除重复的表达式计算

具体要求：
1. 实现接口
   - 类名：CommonSubexpressionEliminationOptimizer
   - 实现：IFlowOptimizer<IRNode>

2. 实现优化逻辑
   - 在CFG上检测公共子表达式
   - 使用哈希表记录已计算的表达式
   - 为重复表达式分配临时变量

3. 统计信息
   - 记录检测到的公共子表达式数量
   - 记录消除的指令数量
```

## 练习题

### 练习1：实现常量传播优化器

**难度**：⭐⭐⭐☆☆

**任务**：实现ConstantPropagationOptimizer类，在CFG上传播常量值。

### 练习2：实现循环不变量外提优化器

**难度**：⭐⭐⭐⭐☆

**任务**：实现LoopInvariantCodeMotionOptimizer，将循环中不变的计算外提。

## 本章小结

通过本章的学习，你已经掌握了：

1. **优化器架构设计**
   - 统一的优化Pass接口（IFlowOptimizer）
   - 可扩展的优化框架
   - 优化Pass的可组合性

2. **跨模块集成技术**
   - 适配器模式的应用
   - 跨模块接口适配
   - 模块间的松耦合协作

3. **实践技能**
   - 实现新的优化Pass
   - 设计接口适配器
   - 构建可扩展的优化系统

**下一章预告**：第20章将聚焦于AI Context Engineer实践，学习如何将AI作为编程助手系统性地参与编译器开发。

【你现在站在】:
```
... → [全局优化] → ✅ [优化器架构与跨模块集成] → [代码生成] → [AI协作] → ...
```

继续加油！好的架构设计让编译器更易于维护和扩展！
