# 图书写作工作流（Workflow）

**项目名称**: AI Context Engineer 视角下的现代编译器实战：用 Java/ANTLR4 + AI 共同实现一门语言
**创建日期**: 2026-01-12
**版本**: 1.0
**状态**: ✅ 规划完成，可以启动写作

---

## 一、工作流概述

本工作流描述如何使用已创建的规划文档，系统化地生成《AI Context Engineer 视角下的现代编译器实战》一书的所有章节。

### 核心理念

**三层 Prompt 架构**：
```
第 1 层：系统级提示词 (SYSTEM_PROMPT.md)
    ↓ 角色设定：技术写作专家 + 编译器工程师 + AI 工程师
    ↓ 写作规范：双线叙事、工程实践、直观讲解
    ↓ 结构要求：6 个强制小节（概述→动机→技术→AI→练习→小结）
    
第 2 层：章节级模板 (CHAPTER_TEMPLATE.md)
    ↓ 完整章节结构模板
    ↓ AI 协作线详细设计
    ↓ 练习题设计（手工版 + AI 协作版）
    ↓ 内容检查清单
    
第 3 层：章节实例提示词 (CHAPTER_PROMPT_EXAMPLES.md)
    ↓ 已填好的示例章节提示词
    ↓ 可直接复制使用
    ↓ 覆盖基础和高级两个端点
```

---

## 二、准备工作

### 2.1 文件清单确认

所有必要的规划文档已创建在 `book/` 目录：

| 文件 | 行数 | 用途 | 状态 |
|------|------|------|------|
| BOOK_IMPLEMENTATION_PLAN.md | ~490 | 整体规划、目标读者、5 模块结构 | ✅ 已创建 |
| SYSTEM_PROMPT.md | ~308 | 系统级提示词：角色设定、写作原则、结构要求 | ✅ 已创建 |
| CHAPTER_TEMPLATE.md | ~999 | 章节级模板：完整的 6 节结构、AI 协作线详细设计 | ✅ 已创建 |
| CHAPTER_PROMPT_EXAMPLES.md | ~217 | 示例章节提示词：第 1 章（基础）、第 12 章（SSA） | ✅ 已创建 |
| IMPLEMENTATION_SUMMARY.md | ~600 | 完成报告：执行摘要、关键发现、后续建议 | ✅ 已创建 |

### 2.2 依赖关系确认

```
图书写作工作流

开始
  ↓
读取 BOOK_IMPLEMENTATION_PLAN.md（了解整体规划）
  ↓
读取 SYSTEM_PROMPT.md（理解角色和规范）
  ↓
读取 CHAPTER_TEMPLATE.md（理解章节结构）
  ↓
读取 CHAPTER_PROMPT_EXAMPLES.md（参考示例章节）
  ↓
填写 CHAPTER_TEMPLATE.md 的占位符，生成具体章节 Prompt
  ↓
将章节 Prompt 发送给技术写作代理（配合 SYSTEM_PROMPT.md）
  ↓
技术写作代理生成章节内容
  ↓
技术审查和代码验证
  ↓
保存章节到独立文件夹
  ↓
重复以上步骤，完成所有章节
  ↓
完成
```

---

## 三、具体操作步骤

### 步骤 1：创建工作分支

```bash
# 创建写作分支
git checkout -b book-writing-20260112

# 或从现有分支创建
git branch book-writing-20260112
git checkout book-writing-20260112
```

**目的**: 将图书写作与其他开发工作隔离开来。

### 步骤 2：创建图书目录结构

```bash
# 在当前目录下创建图书目录
mkdir -p book/00_front_matter
mkdir -p book/01_fundamentals
mkdir -p book/02_language_features
mkdir -p book/03_compilation_basics
mkdir -p book/04_modern_architecture
mkdir -p book/05_advanced_topics
mkdir -p book/appendices
mkdir -p book/assets
mkdir -p book/assets/diagrams
mkdir -p book/assets/code_samples
mkdir -p book/assets/prompts
```

**预期输出**:
```
book/
├── 00_front_matter/        # 前言、读者指南、AI 协作指南
├── 01_fundamentals/        # 第 1-5 章：基础语言与解释器
├── 02_language_features/     # 第 6-9 章：从解释到编译
├── 03_compilation_basics/   # 第 10-12 章：编译器基础
├── 04_modern_architecture/ # 第 13-16 章：现代编译器架构
├── 05_advanced_topics/      # 第 17-20 章：高级优化与 AI 协作
├── appendices/            # 附录：术语表、参考书目、索引
└── assets/                # 图表、代码示例、Prompt 库
    ├── diagrams/
    ├── code_samples/
    └── prompts/
```

### 步骤 3：编写第 0 章（前言）和读者指南

**目的**: 设置整体基调，为后续章节铺垫。

**使用文件**:
- `BOOK_IMPLEMENTATION_PLAN.md` - 了解前言内容要求
- `SYSTEM_PROMPT.md` - 角色和风格指导

**操作**:
1. 根据 `BOOK_IMPLEMENTATION_PLAN.md` 的模块 1 要求，创建前言提示词
2. 将前言提示词发送给技术写作代理
3. 生成第 0 章（前言）
4. 生成读者指南
5. 技术审查和调整

**保存位置**:
- `book/00_front_matter/00_preface.md`
- `book/00_front_matter/01_reader_guide.md`

### 步骤 4：编写第 1 章（样板章）

**目的**: 校准写作风格，建立后续章节的参考标准。

**使用文件**:
- `CHAPTER_PROMPT_EXAMPLES.md` - 第 1 章示例（基础工作台）
- `CHAPTER_TEMPLATE.md` - 完整章节结构模板
- `SYSTEM_PROMPT.md` - 系统级提示词

**操作**:
1. 直接使用 `CHAPTER_PROMPT_EXAMPLES.md` 中第 1 章的提示词
2. 发送给技术写作代理
3. 生成第 1 章内容
4. **技术审查**:
   - 检查代码示例是否可编译
   - 验证与 EP1-EP2 的对应关系是否正确
   - 确认 AI Prompt 模板是否完整
5. **校准风格**: 记录本章的字数、语气、图表数量等风格指标
6. 将第 1 章锁定为参考样板

**保存位置**:
- `book/01_fundamentals/chapter01.md`

**校准指标记录**:
| 指标 | 目标值 | 实际值 |
|------|---------|---------|
| 章节字数 | 8,000–12,000 | （待记录） |
| 代码示例数 | 3–5 个 | （待记录） |
| 图示占位符 | 3–5 个 | （待记录） |
| AI Prompt 模板数 | 1 个 | （待记录） |

### 步骤 5：批量生成后续章节提示词

**目的**: 为第 2–21 章生成具体的章节提示词。

**使用文件**:
- `BOOK_IMPLEMENTATION_PLAN.md` - 查看所有章节的标题、对应 EP、学习目标
- `CHAPTER_TEMPLATE.md` - 填充占位符，生成具体章节 Prompt
- `CHAPTER_PROMPT_EXAMPLES.md` - 参考示例章节的格式

**操作**:

**5.1 生成模块 1 的章节提示词（第 2–5 章）**

```bash
# 示例：为第 2 章生成提示词
# 复制 CHAPTER_TEMPLATE.md 的内容
cp book/CHAPTER_TEMPLATE.md book/01_fundamentals/chapter02_template.md

# 手动填占位符（或使用脚本自动化）
# 示例占位符需要填写的部分：
# - {chapter_title} → "第2章：表达式、运算与解释器基础"
# - {module_title} → "模块 1：基础语言与解释器（EP1–EP12）"
# - {target_reader} → "会 Java、有 Maven 使用经验..."
# - {prerequisites} → "Java 基础语法、命令行、基本 Git 操作"
# - {ep_range} → "EP3–EP4（表达式求值、访问者模式、变量内存）"
# - {previous_chapters} → "第1章：为人和 AI 搭建最小工作台"
# - {next_chapters} → "第3章：语句与控制流"
# - {learning_goal_1} → "理解表达式求值的核心机制"
# - {learning_goal_2} → "掌握访问者模式的设计和应用"
# - {learning_goal_3} → "能够在编译器中复用表达式求值逻辑"
```

**生成第 3 章提示词**:
- {chapter_title}: 第3章：语句与控制流
- {module_title}: 模块 1：基础语言与解释器（EP1–EP12）
- {target_reader}: 会 Java、有 Maven 使用经验...
- {prerequisites}: 表达式求值、变量管理
- {ep_range}: EP5–EP6（if/else、while、语句解析、递归下降）
- {previous_chapters}: 第2章：表达式、运算与解释器基础
- {next_chapters}: 第4章：符号表与作用域
- {learning_goal_1}: 理解控制流语句的语法和语义
- {learning_goal_2}: 掌握递归下降解析技术
- {learning_goal_3}: 能够在编译器中表示条件分支和循环

**生成第 4 章提示词**:
- {chapter_title}: 第4章：符号表与作用域
- {module_title}: 模块 1：基础语言与解释器（EP1–EP12）
- {target_reader}: 会 Java、有 Maven 使用经验...
- {prerequisites}: 作用域概念、变量声明
- {ep_range}: EP9–EP10（全局/局部作用域、变量声明、作用域链）
- {previous_chapters}: 第3章：语句与控制流
- {next_chapters}: 第5章：数组与更复杂的特性
- {learning_goal_1}: 理解作用域的概念和层次结构
- {learning_goal_2}: 掌握变量声明和解析的规则
- {learning_goal_3}: 能够构建基本的符号表系统

**生成第 5 章提示词**:
- {chapter_title}: 第5章：数组与更复杂的特性
- {module_title}: 模块 1：基础语言与解释器（EP1–EP12）
- {target_reader}: 会 Java、有 Maven 使用经验...
- {prerequisites}: 语句解析、表达式求值
- {ep_range}: EP11–EP12（函数调用、数组操作、向完整语言过渡）
- {previous_chapters}: 第4章：符号表与作用域
- {next_chapters}: 第6章：AST 构建与表达式求值（模块 2 起点）
- {learning_goal_1}: 理解数组的语法和语义
- {learning_goal_2}: 掌握函数调用的实现
- {learning_goal_3}: 能够支持更复杂的语言特性

**5.2 生成模块 2 的章节提示词（第 6–9 章）**

按照 BOOK_IMPLEMENTATION_PLAN.md 中的模块 2 章节规划生成：

**第 6 章**:
- {chapter_title}: 第6章：AST 构建与表达式求值
- {module_title}: 模块 2：从解释到编译（EP13–EP16）
- {target_reader}: 已经实现过解释器，准备学习编译器前端
- {prerequisites}: AST 节点层次、访问者模式、符号表基础
- {ep_range}: EP13（AST 构建、AST 求值器）
- {previous_chapters}: 第5章：数组与更复杂的特性
- {next_chapters}: 第7章：符号解析与类型系统
- {learning_goal_1}: 理解抽象语法树的设计原理
- {learning_goal_2}: 掌握访问者模式的实现
- {learning_goal_3}: 能够从 AST 求值器生成代码

**第 7 章**:
- {chapter_title}: 第7章：符号解析与类型系统
- {module_title}: 模块 2：从解释到编译（EP13–EP16）
- {target_reader}: 已经实现过解释器，准备学习编译器前端
- {prerequisites}: 多遍编译、符号表设计、类型系统
- {ep_range}: EP14（符号表、类型系统、Parser actions）
- {previous_chapters}: 第6章：AST 构建与表达式求值
- {next_chapters}: 第8章：完整类型检查与多遍编译架构
- {learning_goal_1}: 理解多遍编译的架构设计
- {learning_goal_2}: 掌握符号表的设计和实现
- {learning_goal_3}: 能够实现基础的类型检查

**第 8 章**:
- {chapter_title}: 第8章：完整类型检查与多遍编译架构
- {module_title}: 模块 2：从解释到编译（EP13–EP16）
- {target_reader}: 已经实现过解释器，准备学习编译器前端
- {prerequisites}: 类型系统、作用域管理、类型推导
- {ep_range}: EP16（完整编译器前端、类型检查、多遍编译）
- {previous_chapters}: 第7章：符号解析与类型系统
- {next_chapters}: 第10章：调用图分析与可视化（模块 3 起点）
- {learning_goal_1}: 理解类型系统的完整设计
- {learning_goal_2}: 掌握类型推导算法
- {learning_goal_3}: 能够实现完整的类型检查器前端

**第 9 章（过渡章）**:
- {chapter_title}: 第9章：从解释到编译的转换
- {module_title}: 过渡章（跨模块 1 和模块 2）
- {target_reader}: 理解解释器到编译器的演进
- {prerequisites}: EP1–EP16 的综合知识
- {ep_range}: EP13–EP16 综合复习
- {previous_chapters}: 第8章：完整类型检查与多遍编译架构
- {next_chapters}: 第10章：调用图分析与可视化（模块 3 起点）
- {learning_goal_1}: 理解从单遍到多遍的转换
- {learning_goal_2}: 掌握编译器前端的设计模式
- {learning_goal_3}: 理解解释器和编译器的本质区别

**5.3 生成模块 3 的章节提示词（第 10–12 章）**

按照 BOOK_IMPLEMENTATION_PLAN.md 中的模块 3 章节规划生成：

**第 10 章**:
- {chapter_title}: 第10章：调用图分析与可视化
- {module_title}: 模块 3：现代编译器架构（EP17–EP18R）
- {target_reader}: 已经实现过编译器，准备学习高级编译器技术
- {prerequisites}: 图数据结构、图算法基础
- {ep_range}: EP17（调用图分析、DOT 可视化）
- {previous_chapters}: 第9章：从解释到编译的转换
- {next_chapters}: 第11章：虚拟机设计与垃圾回收
- {learning_goal_1}: 理解调用图的概念和用途
- {learning_goal_2}: 掌握 Graphviz 可视化技术
- {learning_goal_3}: 能够为函数调用关系生成分析报告

**第 11 章**:
- {chapter_title}: 第11章：虚拟机设计与垃圾回收
- {module_title}: 模块 3：现代编译器架构（EP17–EP18R）
- {target_reader}: 已经实现过编译器，准备学习执行引擎
- {prerequisites}: 栈式虚拟机、字节码设计、内存管理
- {ep_range}: EP18（栈式 VM、引用计数 GC、ByteCodeAssembler）
- {previous_chapters}: 第10章：调用图分析与可视化
- {next_chapters}: 第12章：寄存器虚拟机与 ABI
- {learning_goal_1}: 理解栈式虚拟机的执行模型
- {learning_goal_2}: 掌握字节码的设计和编码
- {learning_goal_3}: 理解引用计数垃圾回收的原理

**第 12 章**:
- {chapter_title}: 第12章：寄存器虚拟机与 ABI
- {module_title}: 模块 3：现代编译器架构（EP17–EP18R）
- {target_reader}: 已经实现过编译器，准备学习寄存器分配
- {prerequisites}: 栈式虚拟机基础、寄存器分配算法
- {ep_range}: EP18R（寄存器 VM、ABI 规范、Linear Scan 分配）
- {previous_chapters}: 第11章：虚拟机设计与垃圾回收
- {next_chapters}: 第13章：中间表示（IR）设计（模块 4 起点）
- {learning_goal_1}: 理解寄存器虚拟机的执行模型
- {learning_goal_2}: 掌握 ABI 规范和调用约定
- {learning_goal_3}: 理解 Linear Scan 寄存器分配算法

**5.4 生成模块 4 的章节提示词（第 13–16 章）**

按照 BOOK_IMPLEMENTATION_PLAN.md 中的模块 4 章节规划生成：

**第 13 章**:
- {chapter_title}: 第13章：中间表示（IR）设计
- {module_title}: 模块 4：中间表示与优化（EP19–EP20）
- {target_reader}: 已经实现过编译器，准备学习中后端技术
- {prerequisites}: 编译器中后端、三地址码概念
- {ep_range}: EP19（IR 节点设计、MIR/LIR 分层）
- {previous_chapters}: 第12章：寄存器虚拟机与 ABI
- {next_chapters}: 第14章：控制流图与基础块
- {learning_goal_1}: 理解中间表示的作用和设计
- {learning_goal_2}: 掌握三地址码的设计
- {learning_goal_3}: 能够实现 IR 节点层次结构

**第 14 章**:
- {chapter_title}: 第14章：控制流图与基础块
- {module_title}: 模块 4：中间表示与优化（EP19–EP20）
- {target_reader}: 已经实现过编译器，准备学习优化技术
- {prerequisites}: 图算法、控制流分析
- {ep_range}: EP20（CFG 构建、基本块划分、支配关系）
- {previous_chapters}: 第13章：中间表示（IR）设计
- {next_chapters}: 第15章：本地优化与代码生成
- {learning_goal_1}: 理解控制流图的概念和构建
- {learning_goal_2}: 掌握基本块的划分算法
- {learning_goal_3}: 能够生成 CFG 并进行基本块分析

**第 15 章**:
- {chapter_title}: 第15章：本地优化与代码生成
- {module_title}: 模块 4：中间表示与优化（EP19–EP20）
- {target_reader}: 已经实现过编译器，准备学习完整的编译器
- {prerequisites}: CFG 基础、优化算法
- {ep_range}: EP20（常量折叠、死代码消除、公共子表达式消除、代码生成）
- {previous_chapters}: 第14章：控制流图与基础块
- {next_chapters}: 第16章：端到端编译器流水线
- {learning_goal_1}: 理解本地优化技术
- {learning_goal_2}: 掌握优化 Pass 的设计
- {learning_goal_3}: 能够实现完整的代码生成器

**第 16 章（过渡章）**:
- {chapter_title}: 第16章：端到端编译器流水线
- {module_title}: 过渡章（跨模块 4 和模块 5）
- {target_reader}: 理解完整编译器的集成
- {prerequisites}: 模块 3 和模块 4 的所有知识
- {ep_range}: EP19–EP20 综合复习
- {previous_chapters}: 第15章：本地优化与代码生成
- {next_chapters}: 第17章：SSA 与数据流基础（模块 5 起点）
- {learning_goal_1}: 理解端到端编译器流水线的完整流程
- {learning_goal_2}: 掌握模块集成和协调的技术
- {learning_goal_3}: 能够独立构建完整的编译器前端到后端

**5.5 生成模块 5 的章节提示词（第 17–20 章）**

按照 BOOK_IMPLEMENTATION_PLAN.md 中的模块 5 章节规划生成：

**第 17 章**:
- {chapter_title}: 第17章：SSA 与数据流基础
- {module_title}: 模块 5：高级优化与 AI 协作（EP21）
- {target_reader}: 已经实现过编译器，准备学习研究级优化
- {prerequisites}: CFG 基础、数据流分析理论
- {ep_range}: EP21（SSA 转换、φ 函数、支配树、活跃性分析）
- {previous_chapters}: 第16章：端到端编译器流水线
- {next_chapters}: 第18章：全局优化技术
- {learning_goal_1}: 理解 SSA 形式的定义和目的
- {learning_goal_2}: 掌握 φ 函数的放置算法
- {learning_goal_3}: 理解活跃性分析在 SSA 中的应用

**第 18 章**:
- {chapter_title}: 第18章：全局优化技术
- {module_title}: 模块 5：高级优化与 AI 协作（EP21）
- {target_reader}: 已经实现过编译器，准备学习高级优化算法
- {prerequisites}: SSA 基础、优化理论
- {ep_range}: EP21（尾递归优化、循环不变量外提、常量传播）
- {previous_chapters}: 第17章：SSA 与数据流基础
- {next_chapters}: 第19章：优化器架构与跨模块集成
- {learning_goal_1}: 理解全局优化技术
- {learning_goal_2}: 掌握尾递归优化算法
- {learning_goal_3}: 掌握循环不变量外提技术

**第 19 章**:
- {chapter_title}: 第19章：优化器架构与跨模块集成
- {module_title}: 模块 5：高级优化与 AI 协作（EP21）
- {target_reader}: 已经实现过编译器，准备学习编译器工程实践
- {prerequisites}: 优化 Pass 框架、EP21-EP18R 接口适配
- {ep_range}: EP21（优化 Pass 框架、跨模块集成、EP21-EP18R 接口适配）
- {previous_chapters}: 第18章：全局优化技术
- {next_chapters}: 第20章：AI Context Engineer 实践
- {learning_goal_1}: 理解优化 Pass 框架的设计
- {learning_goal_2}: 掌握跨模块集成的技术
- {learning_goal_3}: 能够设计可扩展的优化器架构

**第 20 章**:
- {chapter_title}: 第20章：AI Context Engineer 实践
- {module_title}: 模块 5：高级优化与 AI 协作（EP21）
- {target_reader}: 已经实现过编译器，准备掌握 AI 协作的方法论
- {prerequisites}: 所有优化技术、Prompt 工程
- {ep_range}: EP21 综合实践（优化算法 AI 辅助实现、大规模测试生成）
- {previous_chapters}: 第19章：优化器架构与跨模块集成
- {next_chapters}: 附录：关键术语表
- {learning_goal_1}: 掌握 AI Context Engineer 的方法论
- {learning_goal_2}: 能够设计 AI 协作的工作流
- {learning_goal_3}: 能够利用 AI 生成高质量代码和测试

---

## 四、章节内容生成流程

### 4.1 发送章节 Prompt 给技术写作代理

**操作流程**:

1. **准备章节 Prompt**:
   ```bash
   # 复制章节模板
   cp book/CHAPTER_TEMPLATE.md book/{module}/chapter{XX}_prompt.md
   ```

2. **填写章节信息**:
   - 根据表格填充占位符：
     - {chapter_title}: 章节标题
     - {module_title}: 模块标题
     - {target_reader}: 读者特征
     - {prerequisites}: 前置知识
     - {ep_range}: 对应 EP 范围
     - {previous_chapters}: 前面章节
     - {next_chapters}: 后续章节
     - {learning_goal_1/2/3}: 学习目标

3. **组合系统级提示词**:
   ```bash
   # 创建完整的章节 Prompt
   cat book/SYSTEM_PROMPT.md book/{module}/chapter{XX}_prompt.md > book/{module}/chapter{XX}_full.md
   ```

4. **发送给技术写作代理**:
   - 将生成的完整章节 Prompt 发送给技术写作代理
   - 技术写作代理将根据 SYSTEM_PROMPT.md 的角色和风格生成章节内容
   - 配合 CHAPTER_PROMPT_EXAMPLES.md 的示例格式

5. **接收章节内容**:
   - 技术写作代理生成 Markdown 格式的章节内容
   - 包含：概述、动机、技术实现、AI 协作线、练习题、小结

### 4.2 技术审查流程

**审查要点**:

1. **代码验证**:
   ```bash
   # 验证代码示例是否可编译
   cd {module_directory}
   mvn clean compile
   
   # 运行示例程序
   mvn exec:java -Dexec.args="{example_file}"
   ```

2. **EP 对应关系验证**:
   - 检查章节中提到的 EP、类名、方法名是否与仓库一致
   - 验证代码片段的准确性
   - 确认目录结构描述正确

3. **AI 协作线完整性检查**:
   - [ ] 是否有上下文设计小节
   - [ ] 是否有至少 1 个可复用的 AI Prompt 模板
   - [ ] 是否有 AI 应该/不该做的清单
   - [ ] 是否有验证与回滚策略

4. **内容完整性检查**:
   - [ ] 本章概述（1–3 句话）
   - [ ] 动机场景
   - [ ] 核心概念（1–3 个图示占位符）
   - [ ] EP 对应关系和关键代码
   - [ ] 实战流程（可运行的步骤）
   - [ ] 练习题（3–5 道，手工版 + AI 协作版）
   - [ ] 本章小结和下一章预告

### 4.3 章节保存

**命名规范**:
```
{module}/chapter{XX}.md
```

**示例**:
```
book/01_fundamentals/chapter01.md        # 第1章
book/01_fundamentals/chapter02.md        # 第2章
book/01_fundamentals/chapter03.md        # 第3章
book/01_fundamentals/chapter04.md        # 第4章
book/01_fundamentals/chapter05.md        # 第5章

book/02_language_features/chapter06.md    # 第6章
book/02_language_features/chapter07.md    # 第7章
book/02_language_features/chapter08.md    # 第8章
book/02_language_features/chapter09.md    # 第9章

# ... 其他模块类似
```

---

## 五、质量控制

### 5.1 章节质量标准

**每章交付前检查清单**:

| 类别 | 检查项 | 标准 |
|------|---------|------|
| **内容完整性** | 章节概述 | 1–3 句话 |
| | 动机场景 | 真实工程场景，说明痛点 |
| | 核心概念 | 通俗解释，1–3 个图示占位符 |
| | EP 对应关系 | 明确目录、关键类、方法 |
| | 实战流程 | 可运行的步骤，预期输出 |
| | AI 上下文设计 | 源码/文档/测试文件列表 |
| | AI Prompt 模板 | 至少 1 个可复用模板 |
| | AI 应该/不该做 | 各 3–5 条明确清单 |
| | 验证与回滚策略 | 测试命令、检查点、git 回滚 |
| | 练习题 | 3–5 道，手工版 + AI 协作版，带提示 |
| | 本章小结 | 总结收获，预告下一章 |
| **AI 协作线** | 硬性要求 | 至少 1 个 Prompt 模板 |
| | | 验证策略 | 独立小节说明 |
| **代码质量** | 可编译运行 | 基于真实仓库代码 |
| **可读性** | 使用第二人称"你" | 避免过于学术化 |
| **图表规范** | 占位符描述详细 | 读者可自行绘制 |

### 5.2 风格校准机制

**校准流程**:

1. **样板章校准**:
   - 第 1 章完成后，记录所有风格指标
   - 建立风格基准：
     - 字数范围：8,000–12,000
     - 代码示例数：3–5 个
     - 图示占位符：3–5 个
     - AI Prompt 模板：1 个
   - 后续章节参照第 1 章的风格

2. **定期风格审查**:
   - 每 3 章完成后进行一次风格检查
   - 确保术语一致性
   - 验证 AI Prompt 模板的一致性
   - 检查跨章引用和连贯性

---

## 六、AI Prompt 模板库管理

### 6.1 Prompt 模板收集

**收集位置**:
```
book/assets/prompts/
├── feature_implementation/      # 功能实现 Prompt
│   ├── add_syntax_feature.md
│   ├── implement_algorithm.md
│   └── refact_code.md
├── optimization/              # 优化实现 Prompt
│   ├── constant_folding.md
│   ├── dead_code_elimination.md
│   └── common_subexpression_elimination.md
└── testing/                   # 测试生成 Prompt
    ├── generate_unit_tests.md
    └── generate_integration_tests.md
```

**收集策略**:
- 每章生成的 AI Prompt 模板单独保存
- 按照类型（A/B/C）分类存储
- 记录每个 Prompt 的适用场景和约束条件
- 建立索引文件方便查找

---

## 七、进度跟踪

### 7.1 章节完成状态表

| 章节 | 状态 | 完成日期 | 字数 | 代码示例数 | AI Prompt 数 |
|------|------|---------|-------|------------|--------------|
| 第 0 章：前言 | ⏳ 待开始 | - | - | - |
| 第 1 章：基础工作台 | ⏳ 待开始 | - | - | - |
| 第 2 章：表达式、运算 | ⏳ 待开始 | - | - | - |
| 第 3 章：语句与控制流 | ⏳ 待开始 | - | - | - |
| 第 4 章：符号表与作用域 | ⏳ 待开始 | - | - | - |
| 第 5 章：数组与复杂特性 | ⏳ 待开始 | - | - | - |
| 第 6 章：AST 构建与表达式求值 | ⏳ 待开始 | - | - | - |
| 第 7 章：符号解析与类型系统 | ⏳ 待开始 | - | - | - |
| 第 8 章：完整类型检查与多遍编译 | ⏳ 待开始 | - | - | - |
| 第 9 章：从解释到编译的转换 | ⏳ 待开始 | - | - | - |
| 第 10 章：调用图分析与可视化 | ⏳ 待开始 | - | - | - |
| 第 11 章：虚拟机设计与垃圾回收 | ⏳ 待开始 | - | - | - |
| 第 12 章：寄存器虚拟机与 ABI | ⏳ 待开始 | - | - | - |
| 第 13 章：中间表示（IR）设计 | ⏳ 待开始 | - | - | - |
| 第 14 章：控制流图与基础块 | ⏳ 待开始 | - | - | - |
| 第 15 章：本地优化与代码生成 | ⏳ 待开始 | - | - | - |
| 第 16 章：端到端编译器流水线 | ⏳ 待开始 | - | - | - |
| 第 17 章：SSA 与数据流基础 | ⏳ 待开始 | - | - | - |
| 第 18 章：全局优化技术 | ⏳ 待开始 | - | - | - |
| 第 19 章：优化器架构与跨模块集成 | ⏳ 待开始 | - | - | - |
| 第 20 章：AI Context Engineer 实践 | ⏳ 待开始 | - | - | - |

### 7.2 进度里程碑

| 阶段 | 目标日期 | 状态 | 备注 |
|------|---------|------|------|
| 阶段 1：基础设施 | 第 2 周 | ⏳ 待开始 | 完成前言 + 第 1 章（样板） |
| 阶段 2：模块 1 | 第 7 周 | ⏳ 待开始 | 完成第 2–5 章 |
| 阶段 3：模块 2 | 第 11 周 | ⏳ 待开始 | 完成第 6–9 章 |
| 阶段 4：模块 3 | 第 15 周 | ⏳ 待开始 | 完成第 10–12 章 |
| 阶段 5：模块 4 | 第 19 周 | ⏳ 待开始 | 完成第 13–16 章 |
| 阶段 6：模块 5 | 第 23 周 | ⏳ 待开始 | 完成第 17–20 章 |
| 阶段 7：最终打磨 | 第 25 周 | ⏳ 待开始 | 完成所有章节 + 前言 + 附录 |

---

## 八、附录

### 8.1 章节索引

| 章节 | 对应模块 | 核心技术 | 关键代码文件 | AI Prompt 类型 |
|------|---------|---------|-------------|---------------|
| 第 1 章 | 模块 1 | ANTLR4 基础 | Hello.g4, ArrayInit.g4 | 类型 A：功能实现 |
| 第 2 章 | 模块 1 | 表达式求值 | EvalVisitor.java, LabeledExpr.g4 | 类型 A：功能实现 |
| 第 3 章 | 模块 1 | 控制流 | 语句解析 | 类型 A：功能实现 |
| 第 4 章 | 模块 1 | 符号表 | SymbolTable.java | 类型 A：功能实现 |
| 第 5 章 | 模块 1 | 数组 | 数组操作代码 | 类型 A：功能实现 |
| 第 6 章 | 模块 2 | AST 构建 | ASTNode.java, BuildAstVisitor.java | 类型 A：功能实现 |
| 第 7 章 | 模块 2 | 符号表 | SymbolTable.java | 类型 A：功能实现 |
| 第 8 章 | 模块 2 | 类型检查 | TypeChecker.java | 类型 A：功能实现 |
| 第 9 章 | 过渡章 | - | - | - |
| 第 10 章 | 模块 3 | 调用图 | CallGraphVisitor.java, DOT 输出 | 类型 A：功能实现 |
| 第 11 章 | 模块 3 | 虚拟机 | VMInterpreter.java, ByteCodeAssembler.java | 类型 A：功能实现 |
| 第 12 章 | 模块 3 | 寄存器 VM | RegisterVM.java, LinearScanAllocator.java | 类型 A：功能实现 |
| 第 13 章 | 模块 4 | IR 设计 | IRNode.java, LIRNode.java | 类型 B：优化实现 |
| 第 14 章 | 模块 4 | CFG | CFGBuilder.java, BasicBlock.java | 类型 B：优化实现 |
| 第 15 章 | 模块 4 | 本地优化 | ConstantFoldingOptimizer.java | 类型 B：优化实现 |
| 第 16 章 | 过渡章 | - | - | - |
| 第 17 章 | 模块 5 | SSA | SSAConverter.java, PhiFunction.java | 类型 B：优化实现 |
| 第 18 章 | 模块 5 | 全局优化 | TailRecursionOptimizer.java | 类型 B：优化实现 |
| 第 19 章 | 模块 5 | 优化器架构 | OptimizerPass.java | 类型 B：优化实现 |
| 第 20 章 | 模块 5 | AI 实践 | 综合优化 | 类型 C：测试生成 |
| 附录 A | - | 术语表 | - |
| 附录 B | - | 参考书目 | - |

### 8.2 关键术语表（按模块分组）

**模块 1：基础语言与解释器**
- 词法分析 (Lexical Analysis)
- 语法分析 (Parsing)
- 抽象语法树 (AST)
- 访问者模式 (Visitor Pattern)
- 解释器 (Interpreter)

**模块 2：从解释到编译**
- 多遍编译 (Multi-pass Compilation)
- 符号表 (Symbol Table)
- 类型检查 (Type Checking)
- 作用域 (Scope)

**模块 3：现代编译器架构**
- 调用图 (Call Graph)
- 虚拟机 (Virtual Machine)
- 字节码 (Bytecode)
- 垃圾回收 (Garbage Collection)
- 寄存器分配 (Register Allocation)

**模块 4：中间表示与优化**
- 中间表示 (IR - Intermediate Representation)
- 三地址码 (Three-Address Code)
- 控制流图 (CFG - Control Flow Graph)
- 基本块 (Basic Block)
- 优化 Pass (Optimization Pass)

**模块 5：高级优化与 AI 协作**
- 静态单赋值 (SSA - Static Single Assignment)
- φ 函数 (Phi Function)
- 数据流分析 (Dataflow Analysis)
- 活跃性分析 (Liveness Analysis)
- 尾递归优化 (Tail Recursion Optimization)
- AI Context Engineering (AI Context Engineering)

---

## 九、风险与应对

### 9.1 技术风险

| 风险 | 概率 | 影响 | 缓解措施 |
|------|-------|------|---------|
| 代码示例编译失败 | 中 | 高 | 使用真实仓库代码，定期验证 |
| AI Prompt 失效 | 中 | 中 | 定期更新 Prompt 模板 |
| 章节连贯性差 | 低 | 高 | 建立跨章引用机制 |
| 读者反馈不及时 | 中 | 中 | 建立读者反馈渠道 |
| 项目代码演进 | 高 | 中 | 定期同步代码变更 |

### 9.2 进度风险

| 风险 | 概率 | 影响 | 缓解措施 |
|------|-------|------|---------|
| 写作周期延长 | 中 | 高 | 分阶段交付，及时调整计划 |
| 质量不达标 | 中 | 高 | 建立严格的质量检查流程 |
| 突发情况 | 低 | 中 | 保留时间余量应对 |

---

## 十、工具推荐

### 10.1 写作工具

- **Markdown 编辑器**: Typora, VS Code, Obsidian
  - 推荐：Typora（实时预览、语法支持）
  - 必要：代码高亮、表格支持、数学公式支持

- **图示工具**: Draw.io, Graphviz, Mermaid
  - 推荐：Draw.io（在线协作、导出多格式）
  - 备选：Graphviz（适合调用图、CFG）
  - 备选：Mermaid（适合流程图、序列图）

- **版本控制**: Git
  - 要求：频繁提交、使用有意义的提交信息
  - 策略：每个章节一个 commit，使用语义化信息

- **代码验证**: Maven, IDE (IntelliJ IDEA, Eclipse)
  - 要求：所有代码示例必须能编译运行
  - 流程：本地编译 → 运行测试 → 验证输出

### 10.2 AI 工具

- **AI 写作工具**: ChatGPT, Claude, Cursor, Copilot
  - 用途：章节内容生成、代码示例生成、AI Prompt 优化
  - 推荐：Claude（长文本能力强，适合技术写作）
  - 备选：ChatGPT（代码生成能力强）
  - 备选：Cursor（IDE 集成，适合代码审查）

- **AI 辅助工具**:
  - Prompt 管理工具：如 PromptLayer
  - 代码审查工具：如 GitHub Copilot, Cursor
  - 测试生成工具：基于测试框架的 AI 辅助

---

## 十一、总结

### 11.1 工作流优势

**系统化**:
- 三层 Prompt 架构确保输出质量一致
- 模板化章节生成流程可重复、可扩展
- 严格的审查机制保证内容质量
- 完整的进度跟踪和风险管理

**可维护性**:
- 所有规划文档集中管理（book/ 目录）
- 章节独立存储，便于并行写作和审查
- Prompt 模板库支持快速生成和复用
- 风格校准机制确保全书一致性

**质量保证**:
- 每章都有完整的 AI 协作线设计
- 所有代码示例基于真实仓库且可验证
- 双线叙事确保同时教授编译器和 AI 协作技能
- 章节级检查清单确保不遗漏任何强制要求

### 11.2 下一步行动

**立即行动**:
1. ✅ 创建 Git 分支（已完成）
2. ✅ 创建图书目录结构（已完成）
3. ⏳ 编写第 0 章和第 1 章（样板章）
4. ⏳ 批量生成第 2–21 章的章节提示词
5. ⏳ 发送章节提示词给技术写作代理
6. ⏳ 技术审查和质量控制
7. ⏳ 完成所有章节
8. ⏳ 最终打磨和生成电子书

**里程碑**:
- **第 2 周末**: 完成第 0 章和第 1 章（样板）
- **第 7 周末**: 完成模块 1（第 2–5 章）
- **第 11 周末**: 完成模块 2（第 6–9 章）
- **第 15 周末**: 完成模块 3（第 10–12 章）
- **第 19 周末**: 完成模块 4（第 13–16 章）
- **第 23 周末**: 完成模块 5（第 17–20 章）
- **第 25 周末**: 完成所有章节 + 前言 + 附录

---

**文档版本**: 1.0
**最后更新**: 2026-01-12
**状态**: ✅ 就绪，可以启动写作
**下一步**: 执行步骤 2–5，开始第 0 章和第 1 章的写作
