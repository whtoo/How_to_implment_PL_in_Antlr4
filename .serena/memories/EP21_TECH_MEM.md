# EP21技术记忆文档 (精简版)

**版本**: v3.0 (2026-01-03)
**状态**: 核心功能完成 (Core Completed)

---

## 基本信息

- **EP编号**: EP21 - 高级优化编译器
- **项目阶段**: 核心功能完成
- **最后更新**: 2026-01-03
- **实现路径**: Path B (代码生成层优化) ✅
- **测试通过**: 563个测试 (100%)

---

## 当前实现状态 (Path B)

### 核心决策
选择 Path B (代码生成层优化) 而非 Path A (IR层CFG转换)

**理由**:
- 实用性强，直接生成优化代码
- 避免复杂的CFG API适配问题
- 测试全部通过，功能稳定
- 适合实际编译器项目

### 核心组件

| 组件 | 状态 | 描述 |
|------|------|------|
| **TailRecursionOptimizer** | ✅ 完成 | 检测Fibonacci和尾递归模式 |
| **RegisterVMGenerator.TROHelper** | ✅ 完成 | 生成迭代式VMR代码 |
| **StackVMGenerator** | ✅ 完成 | 生成EP18字节码 (13测试) |
| **RegisterVMGenerator** | ✅ 完成 | 生成EP18R汇编 (14测试) |
| **SSA转换器** | ✅ 完成 | ReturnVal/CJMP/JMP指令支持 + SSA验证器 |
| **基础优化Pass** | ✅ 完成 | 常量折叠、CSE、DCE (53测试) |

---

## 编译器优化流程

```
前端(EP11-16) → 中端(EP17-20) → 优化层(EP21) → 后端(EP16/17) → VM(EP18)
```

### 主要组件

1. **数据流分析框架** (`analysis/dataflow/`)
   - `AbstractDataFlowAnalysis` - 抽象基类
   - `LiveVariableAnalysis` - 活跃变量分析
   - `ReachingDefinitionAnalysis` - 到达定义分析
   - `ConditionConstantPropagation` - 条件常量传播 (16测试)
   - `LoopAnalysis` - 循环分析 (13测试)

2. **SSA转换器** (`analysis/ssa/`)
   - `DominatorAnalysis` - 支配关系分析
   - `SSAGraph` - SSA图构建和管理
   - `SSAValidator` - SSA验证器 (10测试)

3. **中间表示层**
   - MIR (Mid-level IR) - 高级中间表示
   - LIR (Low-level IR) - 低级中间表示
   - 转换器: `IRConversionTest` (23测试)

4. **控制流图** (`pass/cfg/`)
   - `CFG` - 控制流图核心
   - `BasicBlock` - 基本块管理
   - `CFGBuilder` - CFG构建器
   - 测试: 124个CFG相关测试全部通过

5. **优化Pass** (`pass/cfg/`)
   - `ConstantFoldingOptimizer` - 常量折叠 (30测试)
   - `CommonSubexpressionEliminationOptimizer` - CSE (16测试)
   - `DeadCodeEliminationOptimizer` - DCE (15测试)
   - `TailRecursionOptimizer` - 尾递归优化 (14测试)

6. **代码生成** (`pass/codegen/`)
   - `StackVMGenerator` - EP18栈式VM (13测试)
   - `RegisterVMGenerator` - EP18R寄存器VM (14测试)

---

## 测试统计

```
总测试数: 563个 ✅
通过: 563个 (100%)
失败: 0个
错误: 0个
覆盖率: ≥85%
```

**测试分类**:
- 单元测试: ~495个 (88%)
- 集成测试: ~46个 (8%)
- 端到端测试: ~22个 (4%)

**里程碑**: M1/M2/M3/M4 全部达成 ✅

---

## Path B 实现架构

```
检测层 (IR)
    ↓ TailRecursionOptimizer
    检测Fibonacci模式 → 标记函数
    
转换层 (Code Generation)
    ↓ RegisterVMGenerator.TROHelper
    生成迭代式汇编代码
    
执行层 (VM)
    ↓ EP18 / EP18R
    运行优化后的代码
```

**关键优势**:
1. 职责分离: 检测和转换分离
2. 实用性强: 直接生成优化代码
3. 稳定可靠: 563测试覆盖
4. 易于维护: 避免复杂CFG API

---

## 尾递归优化 (TRO)

### 支持的递归模式

**1. Fibonacci模式**
```c
int fib(int n) {
    if (n <= 1) return n;
    return fib(n-1) + fib(n-2);  // 2个递归调用
}
```
→ 转换为累加器迭代形式

**2. 直接尾递归模式**
```c
int countdown(int n) {
    if (n <= 0) return 0;
    return countdown(n-1);  // 1个尾位置递归调用
}
```
→ 转换为while循环

### TRO测试覆盖
- `TailRecursionOptimizerTest` - 检测测试 (14个)
- `RegisterVMGeneratorTROTest` - 代码生成测试 (9个)
- `FibonacciTailRecursionEndToEndTest` - 端到端测试 (5个)
- `SSAValidatorTest` - SSA验证测试 (10个)

---

## 技术债务清理

**已清理** (2025-12-26):
- ❌ `ExecutionGraph.java` (~500行) - 已删除
- ❌ `IRInstructionBuilder.java` (~425行) - 已删除
- ❌ `StackFrame.java` (~390行) - 已删除
- ❌ `CFGMutableBuilder.java` (~245行) - 已删除
- ✅ 总代码减少: ~1560行
- ✅ 重构 `TailRecursionOptimizer` 从340行减少到220行

**无技术债务** - 所有文档与实际代码一致 ✅

---

## 依赖关系

### 依赖
- **前置依赖**: EP20 (IR系统, CFG)
- **并行依赖**: EP18 (VM执行)
- **后续输出**: EP16/17 (代码生成)

### 共享组件
- **符号表系统**: 与EP19/20共享
- **IR结构**: 与EP20兼容
- **类型系统**: 与EP14-19共享

---

## 调试技巧

### 常用命令
```bash
# 编译EP21
mvn clean compile -pl ep21

# 运行所有测试
mvn test -pl ep21

# 运行特定测试
mvn test -pl ep21 -Dtest=LIRNodeTest

# 生成覆盖率报告
mvn jacoco:report -pl ep21
```

### 可视化工具
- SSA图DOT输出: `ssaGraph.toDOT()`
- SSA图Mermaid输出: `ssaGraph.toMermaid()`
- CFG可视化: `cfg.toDOT()`

---

## 文件结构

```
ep21/
├── src/main/java/org/teachfx/antlr4/ep21/
│   ├── analysis/
│   │   ├── dataflow/       # 数据流分析
│   │   └── ssa/            # SSA转换
│   ├── ir/
│   │   ├── expr/           # IR表达式
│   │   └── lir/            # LIR指令
│   └── pass/
│       ├── cfg/            # 控制流图和优化
│       ├── codegen/        # 代码生成
│       ├── symtab/         # 符号表
│       ├── ast/            # AST构建
│       └── ir/             # IR构建
└── src/test/
    ├── java/org/teachfx/antlr4/ep21/
    │   ├── integration/    # 集成测试
    │   ├── pass/           # Pass测试
    │   └── test/           # 单元测试
    └── resources/          # 测试资源
```

---

## 未来计划 (可选 - 研究生进阶)

- [ ] TASK-3.1: 控制流优化重构
- [ ] TASK-3.2: 高级数据流分析扩展
- [ ] TASK-3.3: 高级寄存器分配实现
- [ ] TASK-3.4: 指令选择优化实现
- [ ] TASK-4.2: 指令调度重构
- [ ] TASK-4.3: 类型系统扩展
- [ ] TASK-4.4: 运行时支持系统

---

## 相关资源

### 技术文档
- [SSA Construction](https://en.wikipedia.org/wiki/Static_single_assignment_form)
- [Dominator Analysis](https://en.wikipedia.org/wiki/Dominator_(graph_theory))
- [Data Flow Analysis](https://en.wikipedia.org/wiki/Data-flow_analysis)

### 代码位置
- **主要代码**: `ep21/src/main/java/org/teachfx/antlr4/ep21/`
- **测试代码**: `ep21/src/test/java/org/teachfx/antlr4/ep21/`
- **构建配置**: `ep21/pom.xml`

### TDD文档
- **TDD重构计划**: `ep21/docs/TDD重构计划.md` (v2.3)

---

**维护者**: Claude Code
**联系方式**: 通过GitHub Issues
**最后验证**: 2026-01-03 (563个测试全部通过)

---

**版本历史** (精简版):
- **v3.0** (2026-01-03): 压缩精简技术记忆，已完成部分只保留overview ✅
- **v2.x** (2025-12-22~26): 核心功能实现完成 ✅
