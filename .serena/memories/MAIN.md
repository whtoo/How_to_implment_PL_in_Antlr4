# 主控制Agent记忆

## 项目概述
- **项目名称**: How to implement PL in ANTLR4
- **项目性质**: 渐进式编译器教学项目，共21个EP
- **核心语言**: Cymbol (类C教学语言)
- **架构分层**: 前端 → 中端 → 后端 → VM

## 分层记忆体系导航

### 第一层: 主控 Agent 记忆 (本文档)
**用途**: 任务识别、Sub-Agent 协调、公共记忆维护

### 第二层: EP 专属记忆
**位置**: `.serena/memories/`

**已建立**:
- ✅ `EP21_TECH_MEM.md` - 高级优化编译器 (2025-12-23更新)

### 第三层: Skill 库
**位置**: `.claude/skills/`

## EP依赖关系图

```
基础层: EP1 → EP2 → ... → EP10
           ↓
编译器核心: EP11 → EP12 → ... → EP20
           ↓
高级优化: EP21 ✅

虚拟机分支: EP16 → EP17 → EP18 (栈式VM)
                    ↓
                    EP18R (寄存器VM)
```

## 2025-12-23 进度报告

### EP21 - 高级优化编译器
**状态**: ✅ SSA重构完成 (Phase 3.1)

**完成内容**:
1. **数据流分析框架** (2025-12-22)
   - ✅ AbstractDataFlowAnalysis.java
   - ✅ LiveVariableAnalysis.java
   - ✅ ReachingDefinitionAnalysis.java

2. **SSA转换器** (2025-12-23)
   - ✅ DominatorAnalysis.java
   - ✅ SSAGraph.java - 完整重构
     - 基于支配边界的Φ函数插入
     - 完整变量重命名（左值+右值）
     - 支配树递归重命名算法
     - 正确变量栈管理

3. **关键改进**
   - FrameSlot增强 - 保存VariableSymbol引用
   - Operand类优化 - 提供默认accept实现
   - 测试修复 - 223测试全部通过

**测试状态**: ✅ 223/223 通过
```
Tests run: 223, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### Phase3 剩余任务
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

## 快速开始流程

### 场景 1: 在EP21工作

```bash
【步骤 1】加载主控记忆
↓

【步骤 2】识别用户意图
分析: - 任务类型: 开发/测试
      - 目标EP: EP21
      - 复杂度: 高

【步骤 3】加载EP21记忆
读取: EP21_TECH_MEM.md
提取: - 核心类: SSAGraph, DominatorAnalysis
      - 目录结构: src/main/java/org/teachfx/antlr4/ep21/analysis/
      - 依赖: EP20(CFG), EP17(IR)

【步骤 4】加载编译器专家Skill
读取: .claude/skills/compiler-expert/SKILL.md

【步骤 5】创建Sub-Agent (如需要)
创建: "EP21_SSA优化Agent"

【步骤 6】协调执行
```

### 场景 2: 跨EP对比

```bash
【步骤 1】加载主控记忆
↓

【步骤 2】识别EP列表
加载: EP21_TECH_MEM.md

【步骤 3】对比分析
生成: 跨EP依赖关系图谱
```

## 配置文件索引

### 构建与依赖
| 文件 | 位置 | 用途 |
|------|------|------|
| 根POM | `/pom.xml` | Maven父配置，Java 21, ANTLR4 4.13.2 |
| 模块POM | `ep*/pom.xml` | EP专属依赖和插件 |
| 语法文件 | `src/main/antlr4/org/teachfx/antlr4/ep*/parser/Cymbol.g4` | ANTLR4语法定义 |

### 开发工具
| 工具 | 配置位置 | 功能 |
|------|----------|------|
| Context7 | `.mcp.json` | 上下文管理和历史记录 |
| Log4j2 | `src/main/resources/log4j2.xml` | 日志配置 |

### 测试框架
| 框架 | 版本 | 用途 |
|------|------|------|
| JUnit 5 | 5.11.3 | 测试运行器 |
| AssertJ | 3.27.0 | 流畅断言 |
| Mockito | 5.8.0 | Mock和Stub |
| JaCoCo | 0.8.12 | 覆盖率报告 |

**覆盖率要求**:
- 整体: ≥85%
- 核心模块: ≥90%
- 新功能: 100%

## 常用命令速查

### 构建和测试
```bash
# 编译整个项目
mvn clean compile

# 编译特定EP
mvn clean compile -pl ep21

# 运行所有测试
mvn test

# 运行EP特定测试
mvn test -pl ep21

# 生成覆盖率报告
mvn jacoco:report
open ep21/target/site/jacoco/index.html
```

### 开发脚本
```bash
# Linux/macOS
./scripts/run.sh compile ep21
./scripts/run.sh test ep21

# Windows PowerShell
.\scripts\run.ps1 compile ep21
```

## 质量门禁

### 编译要求
- ✅ 所有EP必须编译通过
- ✅ 无警告 (除已知的deprecation警告)
- ✅ 依赖关系正确

### 测试要求
- ✅ 所有测试必须通过
- ✅ 覆盖率 ≥85%
- ✅ 新功能覆盖率 100%

### 代码质量
- ✅ 遵循项目编码规范
- ✅ 添加必要注释
- ✅ 文档同步更新

## 记忆加载策略

### 自动加载
```
用户输入: "在 ep21 中工作"
    ↓
加载: MAIN.md (本文档)
    ↓
识别: 提到 EP21
    ↓
加载: EP21_TECH_MEM.md
    ↓
识别: 开发任务
    ↓
加载: .claude/skills/compiler-expert/SKILL.md
```

### 手动加载
- 使用 `mcp__serena__read_memory` 读取特定记忆
- 使用 `mcp__serena__list_memories` 查看可用记忆

## 维护计划

### 短期 (1周)
- [ ] 继续EP21 Phase3剩余任务
- [ ] 控制流优化实现
- [ ] 寄存器分配算法

### 中期 (1月)
- [ ] 创建剩余EP专属记忆 (EP1-20)
- [ ] 补充Skill库 (TDD流程)
- [ ] 自动化Sub-Agent创建工具

### 长期 (季度)
- [ ] 智能任务识别和EP推断
- [ ] 跨项目记忆共享机制

## 版本历史

- **v2.1** (2025-12-23): 
  - EP21 SSA重构完成
  - 223测试通过
  - 分层记忆体系完善
  - 自动化记忆管理

---

**文档版本**: v2.1  
**维护者**: Claude Code  
**最后更新**: 2025-12-23  
**推荐使用**: 所有新开发会话