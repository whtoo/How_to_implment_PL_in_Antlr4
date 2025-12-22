# EP21技术记忆文档

## 基本信息
- **EP编号**: EP21 - 高级优化编译器
- **项目阶段**: Phase3 优化层重构
- **最后更新**: 2025-12-23
- **维护状态**: 活跃开发中

## 核心架构

### 编译器优化流程
```
前端(EP11-16) → 中端(EP17-20) → 优化层(EP21) → 后端(EP16/17) → VM(EP18)
```

### 主要组件
1. **数据流分析框架** (`analysis/dataflow/`)
   - `AbstractDataFlowAnalysis.java` - 抽象数据流分析基类
   - `LiveVariableAnalysis` - 活跃变量分析
   - `ReachingDefinitionAnalysis` - 到达定义分析
   - 状态: ✅ 已实现并通过测试

2. **SSA转换器** (`analysis/ssa/`)
   - `DominatorAnalysis.java` - 支配关系分析
   - `SSAGraph.java` - SSA图构建和管理
   - 状态: ✅ 2025-12-23 重构完成，全面改进

3. **中间表示层**
   - MIR (Mid-level IR) - 高级中间表示
   - LIR (Low-level IR) - 低级中间表示
   - 转换器: `IRConversionTest.java`

4. **控制流图** (`pass/cfg/`)
   - `CFG.java` - 控制流图核心
   - `BasicBlock.java` - 基本块管理
   - `CFGBuilder.java` - CFG构建器

## 2025-12-23 SSA重构成果

### 关键改进

#### 1. FrameSlot增强 (`src/main/java/org/teachfx/antlr4/ep21/ir/expr/addr/FrameSlot.java`)
```java
// 新增字段
private final VariableSymbol symbol;

// 新增方法
public VariableSymbol getSymbol()
public String getVariableName()

// 修改构造函数
public FrameSlot(int idx, VariableSymbol symbol)
```

**意义**: 保存变量符号引用，使SSA转换能够获取真实变量名

#### 2. SSAGraph完善 (`src/main/java/org/teachfx/antlr4/ep21/analysis/ssa/SSAGraph.java`)

**新增功能**:
- `getVariableName(VarSlot)` - 从VarSlot提取变量名
- `renameOperand(Operand)` - 重命名操作数中的变量使用
- 完善的变量栈管理（正确弹出Phi和普通指令定义的变量）

**核心算法**:
```java
public SSAGraph buildSSA() {
    // 1. 插入Φ函数
    insertPhiFunctions();
    
    // 2. 变量重命名
    renameVariables();
    
    return this;
}
```

**特性**:
- ✅ 基于支配边界的Φ函数插入
- ✅ 完整变量重命名（左值+右值）
- ✅ 支配树递归重命名算法
- ✅ 正确变量栈管理

#### 3. Operand类优化 (`src/main/java/org/teachfx/antlr4/ep21/ir/expr/Operand.java`)
- 从abstract改为具体类
- 提供默认accept实现：`return null`
- 解决匿名Operand类编译问题

#### 4. 测试修复 (`src/test/java/org/teachfx/antlr4/ep21/test/LIRNodeTest.java`)
- 修正匿名Operand类accept方法返回类型

### 测试结果
```
Tests run: 223, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## 文件结构

```
ep21/
├── src/main/java/org/teachfx/antlr4/ep21/
│   ├── analysis/
│   │   ├── dataflow/
│   │   │   ├── AbstractDataFlowAnalysis.java
│   │   │   ├── LiveVariableAnalysis.java
│   │   │   └── ReachingDefinitionAnalysis.java
│   │   └── ssa/
│   │       ├── DominatorAnalysis.java
│   │       └── SSAGraph.java ✅ 2025-12-23重构
│   ├── ir/
│   │   ├── expr/
│   │   │   ├── addr/
│   │   │   │   └── FrameSlot.java ✅ 2025-12-23增强
│   │   │   └── Operand.java ✅ 2025-12-23优化
│   │   └── lir/
│   │       └── LIRNode.java
│   └── pass/
│       ├── cfg/           # 控制流图
│       ├── codegen/       # 代码生成
│       └── symtab/        # 符号表
└── src/test/
    └── test/
        └── LIRNodeTest.java ✅ 2025-12-23修复
```

## 关键技术特性

### SSA转换标准算法
基于Cytron等人的经典SSA构造算法：

1. **Φ函数插入** (第40-65行)
   - 收集变量定义位置
   - 使用工作列表算法
   - 在支配边界插入Φ函数

2. **变量重命名** (第156-293行)
   - 构建支配树孩子关系
   - 深度优先遍历
   - 维护变量版本栈

3. **参数填充** (第284-303行)
   - 为后继块Φ函数填充参数
   - 使用当前版本映射

### 变量名提取机制
```java
private String getVariableName(VarSlot varSlot) {
    if (varSlot instanceof FrameSlot frameSlot) {
        String name = frameSlot.getVariableName();
        if (name != null) {
            return name;
        }
    }
    return varSlot.toString();  // 回退到toString()
}
```

### 变量栈管理
- 压栈: `varStacks.computeIfAbsent(varName, k -> new Stack<>()).push(newVersion)`
- 弹栈: `stack.pop()`
- 作用域: 基于基本块边界

## 测试覆盖

### 数据流分析测试
- `AbstractDataFlowAnalysisTest` - 基础框架测试
- `LiveVariableAnalysisTest` - 活跃变量测试
- `ReachingDefinitionAnalysisTest` - 到达定义测试

### SSA测试
- `SSAGraphTest` - SSA转换测试
- 验证Φ函数插入
- 验证变量重命名

### IR测试
- `LIRNodeTest` - LIR节点测试 ✅ 223测试通过
- `MIRNodeTest` - MIR节点测试
- `IRConversionTest` - IR转换测试

## 性能指标

### 编译性能
- 数据流分析: O(n) - n
- SSA构建: O(n*m) - n为基本块，m为变量数
- 支配为基本块数量分析: O((V+E)*logV) - V为节点，E为边

### 内存使用
- SSAGraph: ~909行代码，59个分支
- CFG构建: 支持2047+节点高性能构建

## 已知问题和限制

### 当前限制
1. 变量重命名支持Assign指令
   - ✅ 已完成基本实现
2. 需要扩展支持更多指令类型
   - 📋 已添加到TDD计划: TASK-3.2.5

### 改进建议
1. ✅ 扩展SSA转换支持更多指令类型 (TASK-3.2.5)
2. ✅ 添加SSA验证器 (TASK-3.2.5.6)
3. 集成活跃变量分析和SSA

## 与其他EP的关系

### 依赖关系
- **前置依赖**: EP20 (IR系统, CFG)
- **并行依赖**: EP18 (VM执行)
- **后续输出**: EP16/17 (代码生成)

### 共享组件
- **符号表系统**: 与EP19/20共享
- **IR结构**: 与EP20兼容
- **类型系统**: 与EP14-19共享

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

## 未来计划

### Phase3剩余任务 (2025-12-23待完成)
- [ ] SSA扩展 (TASK-3.2.5)
  - [ ] CallFunc指令支持
  - [ ] ReturnVal指令支持
  - [ ] CJMP指令支持
  - [ ] JMP指令支持
  - [ ] 表达式重命名完善
  - [ ] SSA验证器实现
- [ ] 控制流优化
  - [ ] 常量传播
  - [ ] 公共子表达式消除
  - [ ] 死代码消除
- [ ] 寄存器分配
  - [ ] 图着色算法
  - [ ] 线性扫描
- [ ] 指令调度
  - [ ] 列表调度算法
  - [ ] 寄存器压力感知

### Phase4计划
- [ ] 机器相关优化
- [ ] 向量化支持
- [ ] 自动并行化

## 版本历史

- **v2.0** (2025-12-22): 数据流分析框架实现
- **v2.1** (2025-12-23): SSA重构完成，变量重命名优化
  - FrameSlot增强
  - SSAGraph完善
  - Operand类优化
  - 223测试通过
- **v2.2** (2025-12-23): TDD文档更新
  - 添加TASK-3.2.5: 扩展SSA转换器支持更多指令
  - 详细TDD测试用例 (4.5.6.1 - 4.5.6.6)
  - CallFunc、ReturnVal、CJMP、JMP指令支持测试
  - 表达式重命名和SSA验证器测试
  - TDD重构计划版本升级至v1.1

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
- **TDD重构计划**: `ep21/docs/TDD重构计划.md`
  - 版本: v1.1 (2025-12-23更新)
  - 添加TASK-3.2.5: 扩展SSA转换器支持更多指令
  - 详细TDD测试用例模板 (4.5.6.1-4.5.6.6)
  - 项目看板和任务追踪表已更新

---

**维护者**: Claude Code  
**联系方式**: 通过GitHub Issues  
**最后验证**: 2025-12-23 (所有223测试通过)