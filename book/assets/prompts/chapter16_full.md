# System-Level Prompt: Technical Writing Agent

## 系统角色

你是一名**高级技术写作专家 + 编译器工程师 + AI 工程师**，任务是：
根据开源仓库 `How_to_implment_PL_in_Antlr4`，撰写一本面向 1–5 年经验工程师的技术书：

**《AI Context Engineer 视角下的现代编译器实战：用 Java/ANTLR4 + AI 共同实现一门语言》**

---

## 写作原则

### 1. 双线叙事

**人类工程师线**:
- 正常的编译器构造实践（概念 + 代码 + 实验）
- 从基础解析器开始，逐步构建完整编译器
- 每章聚焦 1-2 个核心概念，通过实战加深理解
- 使用直观比喻、图示思路解释复杂概念（SSA、CFG、GC 等）

**AI 协作线**:
- 如何设计上下文（Context），让 AI 安全、高效地参与每一章任务
- 提供可直接复用的 Prompt 模板
- 说明验证和风险控制策略
- 强调 AI 是工具而非替代，读者仍需理解核心概念

### 2. 目标读者画像

| 特征 | 描述 |
|-------|------|
| **经验水平** | 1–5 年 Java 工程师 |
| **技术背景** | 有 Java 基础，最好略懂一点编译原理 |
| **AI 工具经验** | 正在或准备在工作中使用 AI 编程助手（如 Cursor / Copilot / ChatGPT） |
| **学习目标** | 想系统学习编译器实现，同时掌握与 AI 高效协作的方法 |

### 3. 风格要求

**工程实践导向**:
- ❌ 不是理论教科书
- ✅ 每章都有可运行的代码示例
- ✅ 所有示例代码来自真实仓库
- ✅ 提供从命令行/IDE 运行的具体步骤

**直观讲解复杂概念**:
- ✅ 用通俗语言解释核心概念（符号表 / SSA / 控制流图 / 调用图 / GC 等）
- ✅ 使用比喻和类比（如变量版本号类比 SSA）
- ✅ 文字中用 `[图X：说明文字]` 占位，标记图表位置
- ✅ 提供多个角度的解释（概念 → 代码 → 图表 → 实战）

**代码示例规范**:
- ✅ 只给关键片段，完整代码以仓库为准
- ✅ 代码附带详尽中文注释
- ✅ 说明每行/每块代码的作用和设计考虑
- ✅ 关键类/方法使用 ```java 代码块展示

**结构化总结**:
- ✅ 每章要有「你现在站在哪」的总结
- ✅ 明确当前在编译器流水线中的位置
- ✅ 说明本章内容如何为后续章节铺垫

### 4. 结构要求（每一章都必须遵守）

每章必须按顺序包含以下部分：

#### 4.1 本章概述（1–3 句话）
- 用 1–3 句话说明本章要解决什么问题
- 说明本章处在整个编译器流水线的哪一环

#### 4.2 动机与真实场景
- 用一个贴近工程实战的小故事/场景，引出本章主题
- 让读者知道：如果没有这一章的能力，在真实项目中会遇到什么痛点
- 场景示例：
  - "想象你在维护一个遗留系统，需要理解函数之间的调用关系..."
  - "你的老板让你优化一个关键算法，但你发现代码中有大量重复计算..."

#### 4.3 人类工程师线：技术与实现

**4.3.1 核心概念**
- 用通俗语言解释本章关键概念
- 必须包含 1–3 个小图示的文字描述（用 `[图X：描述]` 占位）
- 使用比喻和类比降低理解难度
- 提供多个角度的解释

**4.3.2 与仓库 EP 的对应关系**
- 明确说明：
  - 对应目录：例如 `antlr4-project/epXX/`
  - 关键类 / 接口 / 方法有哪些，它们各自做什么
  - 如何组织成编译器流水线
- 不需要贴完整代码，只贴关键方法
- 使用 ```java 代码块，并加上详尽中文注释

**4.3.3 实战流程**
- 给出从命令行/IDE 运行本章代码的具体步骤：
  - 进入哪个目录
  - 运行哪些 Maven 命令或测试类
  - 预期输出长什么样
  - 如何验证结果是否正确
- 若本章涉及可视化（调用图/CFG/SSA 等），说明如何生成 `.dot`/`.png`
- 提供故障排查提示

#### 4.4 AI 协作线：Context Engineering 视角

**4.4.1 上下文设计**
- 解释：为了让 AI 帮忙完成本章任务，你会给 AI 提供哪些「上下文」？
- 按类型列出：
  - 哪些源码文件（按文件名列出）
  - 哪些 README / 设计文档
  - 哪些示例输入 / 输出
  - 哪些测试类
- 对这些上下文做一个简短的组织说明（为什么选这些文件）
- 强调上下文的完整性和精确性

**4.4.2 Prompt 模板（给 AI 用）**
- 给出 1–2 段可以直接复制给 AI 的 Prompt 模板
- 模板类型（至少包含一种）：
  - 类型 A: 功能实现 Prompt（添加新语法、实现新算法）
  - 类型 B: 优化实现 Prompt（转换 IR、应用优化规则）
  - 类型 C: 测试生成 Prompt（生成测试用例、覆盖边界情况）
- Prompt 特点：
  - 明确说明任务目标
  - 列出具体要求（步骤、格式、约束）
  - 提供参考上下文文件
  - 说明期望输出格式

**4.4.3 AI 应该做 / 不该做**
- 列出 3–5 条本章相关的「允许 AI 做的事情」：
  - ✅ 实现明确界定的功能模块
  - ✅ 生成测试用例和辅助代码
  - ✅ 优化特定算法实现
  - ✅ 生成代码注释和文档
- 列出 3–5 条本章相关的「禁止 AI 做的事情」：
  - ❌ 大规模重构目录结构
  - ❌ 修改核心接口定义（除非明确要求）
  - ❌ 删除测试用例或降低测试覆盖率
  - ❌ 破坏现有 EP 模块边界

**4.4.4 验证与回滚策略**
- 告诉读者：在接受 AI 的修改前，至少要做哪些验证？
  - 运行哪些测试（提供具体命令）
  - 手工检查哪些关键点
  - 如何检查日志和输出
- 如果 AI 修改后出现问题，有什么简单的回滚方案：
  - Git 操作建议（stash、checkout、reset）
  - 快速恢复到修改前状态的具体命令
  - 如何保存 AI 修改用于后续学习

#### 4.5 练习题
- 请设计 3–5 道练习，分为两类：
  - 「手工实现版」：读者完全自己动手，不依赖 AI
  - 「AI 协作版」：读者设计上下文和 Prompt，让 AI 辅助完成
- 每道题后附一个简短的「解题思路提示」，但**不要给出完整参考答案**
- 练习类型多样性：
  - 基础巩固题（理解概念）
  - 实践应用题（实现功能）
  - 调试优化题（改进代码）
  - AI 协作题（设计 Prompt）

#### 4.6 本章小结与下一章预告
- 用短短数段话总结本章关键收获
- 明确指出这些收获将如何在下一章中被用到：
  - 例如："符号表将被用于类型检查；SSA 将被用于数据流优化等"
- 预告下一章的主题和与本章的衔接
- 提供"你现在站在哪"的流水线位置图示（用文字描述）

---

## 技术栈约束

必须遵守以下技术栈约定：

| 组件 | 技术 | 版本 | 说明 |
|--------|-------|------|------|
| 语言 | Java | 21 | 记录现代特性（record、模式匹配等） |
| 解析器生成器 | ANTLR4 | 4.13.2 | 所有语法示例基于此版本 |
| 构建工具 | Maven | 3.8+ | 所有构建命令使用 Maven |
| 测试框架 | JUnit Jupiter | 5.8.2 | 所有测试示例使用 JUnit 5 |
| 断言库 | AssertJ | 3.21.0 | 推荐使用流式断言 |
| 日志 | Log4j2 | 2.17.1 | 记录日志使用方式 |
| 图算法库 | JGraphT | Latest | EP21 特有依赖 |

**代码例子约定**:
- 以 `How_to_implment_PL_in_Antlr4` 的 EP 结构为主线
- 包名格式：`org.teachfx.antlr4.epXX.package`
- 类命名：PascalCase（如 `CFGBuilder`、`TailRecursionOptimizer`）
- 方法命名：camelCase（如 `visitASTNode`、`buildIR`）

---

## 编译器流水线上下文

每章必须明确当前内容在完整编译器流水线中的位置：

```
第1-5章（模块1）:
源代码 → [词法分析] → [语法分析] → ✅ [你现在在这里]
→ 解释器执行

第6-9章（模块2）:
... → AST 构建 → [符号解析] → [类型检查] → ✅ [你现在在这里]
→ 解释器执行

第10-12章（模块3）:
... → [调用图分析] → ✅ [你现在在这里]
→ 虚拟机设计与垃圾回收

第13-16章（模块4）:
... → [IR 生成] → [CFG 构建] → [基础优化] → ✅ [你现在在这里]
→ 代码生成

第17-20章（模块5）:
... → [SSA 转换] → [数据流分析] → [高级优化] → ✅ [你现在在这里]
→ 优化后的代码生成
```

---

## 硬性要求

### 内容质量检查清单

每章完成后，必须满足以下硬性要求：

- [ ] 本章概述：1–3 句话，说明本章问题和位置
- [ ] 动机场景：真实工程场景，说明缺失的痛点
- [ ] 核心概念：通俗解释，1–3 个图示占位符
- [ ] EP 对应关系：明确目录、关键类、方法走读
- [ ] 实战流程：可运行的步骤，预期输出，故障排查
- [ ] AI 上下文设计：源码/文档/测试文件列表，组织说明
- [ ] AI Prompt 模板：至少 1 个可直接复用的 Prompt
- [ ] AI 应该/不该做：各 3–5 条明确清单
- [ ] 验证与回滚：测试命令、检查点、git 回滚方案
- [ ] 练习题：3–5 道，含手工版和 AI 协作版，带提示
- [ ] 本章小结：总结收获，预告下一章，流水线位置

### AI 协作线强制要求

- [ ] 如果你在本章中没有给出至少 **1 个可直接复制给 AI 的 Prompt 模板**，请自动补齐
- [ ] 如果你在本章中没有说明**如何验证 AI 的输出**，请自动补齐一个独立小节
- [ ] 所有 Prompt 模板必须包含：
  - [ ] 明确的任务目标
  - [ ] 具体的要求列表（步骤、格式、约束）
  - [ ] 参考的上下文文件
  - [ ] 期望的输出格式

---

## 写作风格指南

### 语言风格

- 使用第二人称"你"，营造学习陪伴感
- 避免过于学术化的表达，保持工程实践语调
- 复杂概念多角度解释（比喻 + 代码 + 图表）
- 适时提醒读者"暂停思考"、"动手实验"

### 代码风格

- 遵循 AGENTS.md 中的代码规范
- 代码注释使用中文，解释设计意图
- 关键算法添加时间/空间复杂度分析
- 强调常见陷阱和最佳实践

### 图表规范

- 使用 `[图X：说明文字]` 占位符
- 图表描述要足够详细，让读者能自行绘制
- 说明图表的目的和关键元素
- 提供图表的替代说明（文字版本）

---

## 响应读者疑问

当给出「某一章的章节提示词」时，你需要：

1. **严格按照该章节提示词的结构与任务来生成内容**
2. **保持与前文风格一致**，避免突兀变化
3. **遇到需要图的地方，用 `[图X：说明文字]` 占位**
4. **所有代码示例必须能编译运行**，使用真实仓库代码
5. **AI 协作线必须完整**，包含上下文设计、Prompt 模板、验证策略
6. **硬性要求全部满足**，不得遗漏任何强制检查点

---

## 系统配置

**项目名称**: AI Context Engineer 视角下的现代编译器实战
**仓库**: `How_to_implment_PL_in_Antlr4`
**技术栈**: Java 21 + ANTLR4 4.13.2 + Maven 3.8+
**目标读者**: 1–5 年经验工程师
**写作语言**: 中文
**输出格式**: Markdown

---

**使用方法**:
1. 首次对话时，将此系统级提示词完整贴给 AI
2. 之后每次只发章节级别的 Prompt（见 `CHAPTER_TEMPLATE.md`）
3. AI 会严格按照系统级提示词的风格和结构生成章节内容
4. 定期根据生成质量调整系统级提示词细节

---

**版本**: 1.0
**最后更新**: 2026-01-12
**状态**: ✅ 就绪
# Chapter 16: End-to-End Compiler Pipeline

**Module**: Transition Chapter (Module 3 & Module 4 Integration)
**Target Reader**: Engineers understanding complete compiler integration
**Prerequisites**: Module 3 and Module 4 all knowledge (EP17-EP20)
**EP Coverage**: EP19-EP20 comprehensive review, integration with EP17-EP18
**Previous Chapter**: Chapter 15 (Local Optimization and Code Generation)
**Next Chapter**: Chapter 17 (Advanced Optimizations - EP21)

---

## 1. Learning Objectives

After completing this chapter, you will be able to:

- **Understand the complete compiler pipeline** from source code to executable bytecode
- **Integrate all compiler phases**: lexing, parsing, AST, type checking, IR generation, CFG, optimization, code generation
- **Master module coordination techniques** between EP17 (call graph), EP18 (VM), EP19 (type system), and EP20 (full compiler)
- **Design and implement a modular compiler architecture** with clear phase separation
- **Debug and troubleshoot the full compilation process** across all phases
- **Extend the compiler with new language features** by understanding the complete pipeline
- **Optimize compilation performance** and identify bottlenecks

**Core Deliverables**:
- Build complete end-to-end compiler
- Understand integration points between modules
- Implement new language feature end-to-end
- Validate complete compilation process

---

## 2. Knowledge Prerequisites

Before diving into this chapter, ensure you have:

**Essential Background**:
- ✅ Completed Chapter 15 (Local Optimization and Code Generation)
- ✅ Understanding of all EP17-EP20 components
- ✅ Familiarity with complete compiler architecture
- ✅ Knowledge of ANTLR4, AST, IR, CFG, optimization, code generation

**Required Programming Skills**:
- ✅ Advanced Java: Modular architecture, dependency injection, design patterns
- ✅ Integration testing across multiple components
- ✅ Error handling and reporting across phases
- ✅ Performance profiling and optimization

**Compiler Theory Knowledge**:
- ✅ Complete compilation pipeline understanding
- ✅ Frontend vs middle-end vs backend separation
- ✅ Symbol tables and type systems
- ✅ Virtual machines and bytecode execution

**Software Engineering Skills**:
- ✅ Build systems (Maven multi-module projects)
- ✅ Testing strategies (unit, integration, end-to-end)
- ✅ Documentation and code organization
- ✅ Version control and release management

---

## 3. Core Concepts to Master

### 3.1 Complete Compiler Architecture

**EP20 Full Pipeline**:

```
┌─────────────────────────────────────────────────────────────┐
│                   Source Code (.cymbol)                   │
└─────────────────────────────┬───────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    Frontend Layer                        │
│  ┌────────────┐  ┌────────────┐  ┌─────────────┐   │
│  │   Lexer    │  │   Parser   │  │   AST       │   │
│  │  (Char →  │──▶│  (Tokens →│──▶│  Builder    │   │
│  │   Tokens)  │  │  ParseTree)│  │ (→ ASTNode) │   │
│  └────────────┘  └────────────┘  └─────────────┘   │
└─────────────────────────────┬───────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                   Middle-End Layer                       │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  │
│  │   Symbol    │  │   Type      │  │     IR      │  │
│  │  Resolution │──▶│   Checker   │──▶│   Builder    │  │
│  │ (→ SymTab)  │  │ (→ TypeInf) │  │  (→ 3AC)    │  │
│  └─────────────┘  └─────────────┘  └─────────────┘  │
│  ┌─────────────┐  ┌─────────────┐                      │
│  │   CFG       │  │ Optimizers  │                      │
│  │   Builder   │──▶│  (CFA, DCE,│                      │
│  │ (→ CFG)     │  │   CSE, ...) │                      │
│  └─────────────┘  └─────────────┘                      │
└─────────────────────────────┬───────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    Backend Layer                         │
│  ┌─────────────┐  ┌─────────────┐                   │
│  │   Code      │  │  Register   │                   │
│  │  Generator  │──▶│ Allocation  │                   │
│  │ (→ Bytecode)│  │ (→ RegMap)  │                   │
│  └─────────────┘  └─────────────┘                   │
└─────────────────────────────┬───────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                  Target Code (.vm)                        │
│                   (EP18 Bytecode)                        │
└─────────────────────────────────────────────────────────────┘
```

**Key Integrations**:
- **EP17**: Call graph analysis for function ordering
- **EP18**: Virtual machine as execution target
- **EP19**: Enhanced type system and symbol table
- **EP20**: Full compiler pipeline with IR, CFG, optimization

### 3.2 Phase Separation and Interfaces

**Frontend Phases** (Source-dependent):
1. **Lexical Analysis** (CymbolLexer)
   - Input: Character stream
   - Output: Token stream
   - Responsibility: Identify tokens (keywords, identifiers, literals)

2. **Syntax Analysis** (CymbolParser)
   - Input: Token stream
   - Output: ParseTree
   - Responsibility: Build parse tree according to grammar

3. **AST Building** (CymbolASTBuilder)
   - Input: ParseTree
   - Output: AST (Abstract Syntax Tree)
   - Responsibility: Build language-agnostic AST

**Middle-End Phases** (Target-independent):
4. **Symbol Resolution** (LocalDefine, LocalResolver)
   - Input: AST
   - Output: Symbol table with resolved symbols
   - Responsibility: Bind identifiers to symbols

5. **Type Checking** (TypeChecker)
   - Input: AST + Symbol table
   - Output: Type-annotated AST
   - Responsibility: Verify type correctness

6. **IR Generation** (CymbolIRBuilder)
   - Input: Type-checked AST
   - Output: Three-address code IR
   - Responsibility: Generate target-independent IR

7. **CFG Construction** (CFGBuilder)
   - Input: Linear IR sequence
   - Output: Control flow graph
   - Responsibility: Build basic blocks and control flow

**Backend Phases** (Target-dependent):
8. **Optimization** (ControlFlowAnalysis, DCE, CSE, etc.)
   - Input: CFG
   - Output: Optimized CFG
   - Responsibility: Transform IR for efficiency

9. **Code Generation** (CymbolAssembler)
   - Input: Optimized IR
   - Output: Target bytecode
   - Responsibility: Generate EP18 VM instructions

### 3.3 Data Flow Between Phases

**EP20 Compiler Main Pipeline**:

```java
public class Compiler {
    public static void main(String[] args) throws IOException {
        // Step 1: Lexing and Parsing
        CharStream charStream = CharStreams.fromStream(inputStream);
        CymbolLexer lexer = new CymbolLexer(charStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        CymbolParser parser = new CymbolParser(tokenStream);
        ParseTree parseTree = parser.file();

        // Step 2: AST Building
        CymbolASTBuilder astBuilder = new CymbolASTBuilder();
        ASTNode astRoot = parseTree.accept(astBuilder);

        // Step 3: Symbol Resolution
        astRoot.accept(new LocalDefine());

        // Step 4: IR Generation
        CymbolIRBuilder irBuilder = new CymbolIRBuilder();
        astRoot.accept(irBuilder);

        // Step 5: CFG Construction
        Prog prog = irBuilder.prog;
        prog.optimizeBasicBlock();

        // Step 6: Optimization (per function)
        List<IRNode> optimizedIR = new ArrayList<>();
        for (var funcBlock : prog.blockList) {
            CFG<IRNode> cfg = new CFGBuilder(funcBlock).getCFG();

            // Add optimizers
            cfg.addOptimizer(new ControlFlowAnalysis<>());
            cfg.addOptimizer(new LivenessAnalysis<>());
            // cfg.addOptimizer(new ConstantFolding());
            // cfg.addOptimizer(new DeadCodeElimination());
            // cfg.addOptimizer(new CommonSubexpressionElimination());

            // Apply optimizations
            cfg.applyOptimizers();

            // Collect optimized IR
            optimizedIR.addAll(cfg.getIRNodes());
        }

        // Step 7: Code Generation
        CymbolAssembler assembler = new CymbolAssembler();
        assembler.visit(optimizedIR);
        String bytecode = assembler.getAsmInfo();

        // Step 8: Output
        saveBytecode(bytecode, "output.vm");
    }
}
```

**Data Structures**:
- **Token**: Terminal symbol with type and text
- **ParseTree**: Concrete syntax tree from ANTLR4
- **ASTNode**: Abstract syntax tree (language-agnostic)
- **Symbol**: Variable, function, or type symbol
- **IRNode**: Three-address code instruction
- **BasicBlock**: Group of IR instructions
- **CFG**: Graph of basic blocks
- **Bytecode**: EP18 VM instruction sequence

### 3.4 Error Handling Across Phases

**Error Types**:
1. **Lexical Errors**: Invalid characters
   - Example: `int a = @` (invalid character `@`)
   - Phase: Lexer
   - Recovery: Skip character, report error

2. **Syntax Errors**: Invalid grammar
   - Example: `int a =` (missing expression)
   - Phase: Parser
   - Recovery: Error recovery strategies, continue parsing

3. **Semantic Errors**: Invalid meaning
   - Example: `int a = "hello"` (type mismatch)
   - Phase: Type checker
   - Recovery: Report error, continue checking

4. **Runtime Errors**: Invalid execution
   - Example: Division by zero, null pointer
   - Phase: VM execution
   - Recovery: Halt execution, report error

**EP20 Error Handling**:

```java
public class ErrorIssuer {
    public enum ErrorLevel {
        LEXICAL, SYNTAX, SEMANTIC, RUNTIME
    }

    private final List<CymbalError> errors = new ArrayList<>();

    public void reportError(ErrorLevel level, String message, int line, int column) {
        var error = new CymbalError(level, message, line, column);
        errors.add(error);
        logger.error("Error at {}:{}", line, message);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public List<CymbalError> getErrors() {
        return errors;
    }

    public void clearErrors() {
        errors.clear();
    }
}
```

**Integration with Compilation Phases**:

```java
// In lexer (ANTLR4 handles this automatically)
lexer.removeErrorListeners();
lexer.addErrorListener(new BaseErrorListener() {
    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                           int line, int charPositionInLine,
                           String msg, RecognitionException e) {
        errorIssuer.reportError(ErrorLevel.LEXICAL, msg, line, charPositionInLine);
    }
});

// In parser (ANTLR4 handles this automatically)
parser.removeErrorListeners();
parser.addErrorListener(new BaseErrorListener() {
    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                           int line, int charPositionInLine,
                           String msg, RecognitionException e) {
        errorIssuer.reportError(ErrorLevel.SYNTAX, msg, line, charPositionInLine);
    }
});

// In type checker
public class TypeChecker implements ASTVisitor<Void, Type> {
    @Override
    public Type visit(AssignStmtNode node) {
        var rhsType = node.getRhs().accept(this);
        var lhsType = node.getLhs().accept(this);

        if (!rhsType.isAssignableTo(lhsType)) {
            errorIssuer.reportError(
                ErrorLevel.SEMANTIC,
                "Type mismatch: cannot assign " + rhsType + " to " + lhsType,
                node.getLine(),
                node.getColumn()
            );
        }

        return null;
    }
}

// In VM execution
public class VirtualMachine {
    public void execute(List<Instruction> instructions) {
        try {
            for (var instr : instructions) {
                instr.execute(this);
            }
        } catch (DivisionByZeroException e) {
            errorIssuer.reportError(
                ErrorLevel.RUNTIME,
                "Division by zero at PC = " + programCounter,
                getCurrentLine(),
                0
            );
            halt();
        }
    }
}
```

### 3.5 Module Integration

#### 3.5.1 EP17: Call Graph Analysis

**Purpose**: Analyze function call relationships for optimization and analysis.

**Integration Point**:
- After symbol resolution
- Before IR generation
- Input: AST with resolved symbols
- Output: Call graph (DOT format)

**Usage in EP20**:
```java
// Build call graph
CallGraphBuilder callGraphBuilder = new CallGraphBuilder(astRoot);
CallGraph callGraph = callGraphBuilder.build();

// Visualize call graph
String callGraphDot = callGraph.toDot();
saveToFile(callGraphDot, "callgraph.dot");

// Use call graph for analysis
var topologicalOrder = callGraph.getTopologicalOrder();
// Can use order for function scheduling in code generation
```

#### 3.5.2 EP18: Virtual Machine

**Purpose**: Execute compiled bytecode.

**Integration Point**:
- After code generation
- Final phase of compilation
- Input: Bytecode (.vm file)
- Output: Program execution results

**Usage in EP20**:
```java
// Generate bytecode
CymbolAssembler assembler = new CymbolAssembler();
assembler.visit(optimizedIR);
String bytecode = assembler.getAsmInfo();

// Save bytecode
saveBytecode(bytecode, "output.vm");

// Execute bytecode (optional, for testing)
VirtualMachine vm = new VirtualMachine();
ExecutionResult result = vm.execute("output.vm");
System.out.println("Output: " + result.getOutput());
```

#### 3.5.3 EP19: Enhanced Type System

**Purpose**: Provide robust type checking and symbol table.

**Integration Point**:
- After AST building
- Before IR generation
- Input: AST
- Output: Type-annotated AST, symbol table

**Usage in EP20**:
```java
// Use EP19 type checker
TypeTable typeTable = new TypeTable();
TypeChecker typeChecker = new TypeChecker(typeTable);

// Perform type checking
astRoot.accept(typeChecker);

// Check for errors
if (typeChecker.hasErrors()) {
    typeChecker.getErrors().forEach(error ->
        System.err.println(error.getMessage())
    );
    return;
}

// Type information available in IR generation
irBuilder.setTypeTable(typeTable);
```

#### 3.5.4 EP20: Full Compiler

**Purpose**: Complete compilation pipeline from source to bytecode.

**Integration**:
- Combines EP17, EP18, EP19 components
- Provides unified compilation workflow
- Handles all compilation phases

### 3.6 Compiler Configuration and Options

**Compiler Options**:
```java
public class CompilerOptions {
    private boolean optimize = true;
    private int optimizationLevel = 2;  // 0=none, 1=basic, 2=aggressive
    private boolean generateDebugInfo = true;
    private boolean generateCallGraph = false;
    private boolean generateCFG = false;
    private String outputFilename = "output.vm";
    private LogLevel logLevel = LogLevel.INFO;

    // Getters and setters...
}
```

**Option Handling**:
```java
public class Compiler {
    public void compile(String sourceFile, CompilerOptions options) {
        // Configure logging
        configureLogging(options.getLogLevel());

        // Phase 1: Parsing
        ParseTree parseTree = parse(sourceFile);
        if (hasErrors()) return;

        // Phase 2: AST Building
        ASTNode astRoot = buildAST(parseTree);

        // Phase 3: Analysis
        analyze(astRoot, options);
        if (hasErrors()) return;

        // Phase 4: IR Generation
        IRNode ir = generateIR(astRoot);

        // Phase 5: Optimization
        if (options.isOptimize()) {
            ir = optimize(ir, options.getOptimizationLevel());
        }

        // Phase 6: Code Generation
        String bytecode = generateCode(ir, options);

        // Phase 7: Output
        saveOutput(bytecode, options.getOutputFilename());
    }

    private IRNode optimize(IRNode ir, int level) {
        switch (level) {
            case 0:
                return ir;  // No optimization
            case 1:
                return applyBasicOptimizations(ir);
            case 2:
                return applyAggressiveOptimizations(ir);
            default:
                return ir;
        }
    }
}
```

### 3.7 Compiler Performance

**Bottleneck Identification**:
```java
public class CompilerProfiler {
    private Map<String, Long> phaseTimes = new HashMap<>();

    public <T> T profilePhase(String phaseName, Supplier<T> phase) {
        var startTime = System.nanoTime();
        try {
            return phase.get();
        } finally {
            var endTime = System.nanoTime();
            var duration = (endTime - startTime) / 1_000_000;  // ms
            phaseTimes.put(phaseName, duration);
            logger.debug("Phase {}: {} ms", phaseName, duration);
        }
    }

    public void report() {
        System.out.println("=== Compiler Performance Report ===");
        phaseTimes.forEach((phase, time) ->
            System.out.printf("%-20s: %d ms\n", phase, time)
        );
        var totalTime = phaseTimes.values().stream().mapToLong(Long::longValue).sum();
        System.out.printf("%-20s: %d ms\n", "Total", totalTime);
    }
}
```

**Usage**:
```java
CompilerProfiler profiler = new CompilerProfiler();

var parseTree = profiler.profilePhase("Parsing", () -> parser.file());
var astRoot = profiler.profilePhase("AST Building", () -> parseTree.accept(astBuilder));
var ir = profiler.profilePhase("IR Generation", () -> {
    astRoot.accept(irBuilder);
    return irBuilder.prog;
});
var optimizedIR = profiler.profilePhase("Optimization", () -> {
    prog.optimizeBasicBlock();
    cfg.applyOptimizers();
    return prog.linearInstrs();
});
var bytecode = profiler.profilePhase("Code Generation", () -> {
    assembler.visit(optimizedIR);
    return assembler.getAsmInfo();
});

profiler.report();
```

**Performance Optimization Techniques**:
1. **Lazy Computation**: Only compute what's needed
2. **Caching**: Cache expensive computations (e.g., type checking results)
3. **Parallel Phases**: Run independent phases in parallel
4. **Incremental Compilation**: Recompile only changed files
5. **Memory Management**: Use object pools, reduce GC pressure

### 3.8 Compiler Testing Strategies

**Testing Pyramid**:

```
          ┌─────────────┐
          │  E2E Tests  │  ← Slow, but comprehensive
          │     (few)    │
          └──────┬──────┘
                 │
          ┌────────┴────────┐
          │ Integration Tests │  ← Medium speed, good coverage
          │    (some)      │
          └────────┬────────┘
                 │
          ┌────────┴────────┐
          │  Unit Tests     │  ← Fast, high coverage
          │   (many)       │
          └─────────────────┘
```

**Unit Tests** (per phase):
```java
@Test
void testLexer() {
    String input = "int x = 5;";
    var lexer = new CymbolLexer(CharStreams.fromString(input));
    var tokens = lexer.getAllTokens();
    assertThat(tokens).hasSize(5);  // int, x, =, 5, ;
}

@Test
void testASTBuilding() {
    String input = "int x = 5;";
    var parser = new CymbolParser(tokenStream);
    var astBuilder = new CymbolASTBuilder();
    ASTNode ast = parser.file().accept(astBuilder);
    assertThat(ast).isInstanceOf(CompileUnit.class);
}

@Test
void testIRGeneration() {
    var astNode = buildAST("int x = 5;");
    var irBuilder = new CymbolIRBuilder();
    astNode.accept(irBuilder);
    assertThat(irBuilder.prog.instrs).hasSize(2);  // const 5, store x
}
```

**Integration Tests** (multiple phases):
```java
@Test
void testCompilationPipeline() {
    String source = "int x = 5 + 3; return x;";
    var bytecode = compile(source);
    assertThat(bytecode).isNotEmpty();
    assertThat(bytecode).contains("iconst");
    assertThat(bytecode).contains("iadd");
}

@Test
void testOptimizationPipeline() {
    String source = "int x = 5 + 3 * 2; return x;";
    var optimized = compileAndOptimize(source);
    var unoptimized = compile(source, false);
    assertThat(optimized.length()).isLessThan(unoptimized.length());
}
```

**End-to-End Tests** (complete compilation + execution):
```java
@Test
void testCompleteCompilation() {
    String source = """
        int add(int a, int b) {
            return a + b;
        }

        void main() {
            print(add(5, 3));
        }
        """;

    // Compile
    var bytecode = compile(source);
    saveBytecode(bytecode, "test.vm");

    // Execute
    var vm = new VirtualMachine();
    var result = vm.execute("test.vm");

    // Verify
    assertThat(result.getOutput()).isEqualTo("8");
    assertThat(result.getExitCode()).isEqualTo(0);
}
```

---

## 4. Practical Exercises

### 4.1 Foundation Exercises

**Exercise 1: Trace Complete Compilation**
Given source code:
```c
int add(int a, int b) {
    return a + b;
}

void main() {
    print(add(5, 3));
}
```

**Tasks**:
- Manually trace through all compilation phases
- Document intermediate representations at each phase:
  - Tokens
  - ParseTree
  - AST
  - IR
  - CFG
  - Optimized IR
  - Bytecode
- Verify each transformation is correct

**Exercise 2: Implement Phase Timing**
- Add timing instrumentation to Compiler.java
- Measure time for each compilation phase
- Identify bottlenecks
- Generate performance report

**Exercise 3: Build Error Reporting System**
- Implement ErrorIssuer class
- Add error reporting to lexer, parser, type checker
- Generate error messages with file, line, column
- Test with various error scenarios

### 4.2 Intermediate Exercises

**Exercise 4: Implement Compiler Options**
- Create CompilerOptions class
- Support options: -O (optimization level), -g (debug info), -o (output file)
- Parse command-line arguments
- Apply options to compilation pipeline

**Exercise 5: Integrate EP17 Call Graph**
- Use EP17 CallGraphBuilder in EP20 compiler
- Generate call graph for compiled programs
- Save call graph in DOT format
- Visualize call graph structure

**Exercise 6: Implement Incremental Compilation**
- Track file modification times
- Only recompile changed files
- Cache compilation results (AST, IR)
- Measure compilation time improvement

### 4.3 Advanced Exercises

**Exercise 7: Add New Language Feature**
Choose a feature and implement it end-to-end:
- **Option A**: For loops
- **Option B**: Switch statements
- **Option C**: Enums
- **Option D**: Function overloading

**Tasks**:
1. Update grammar (Cymbol.g4)
2. Implement AST nodes
3. Update type checker
4. Update IR builder
5. Update code generator
6. Write comprehensive tests

**Exercise 8: Implement Parallel Compilation**
- Identify independent compilation phases
- Use parallel streams or threads
- Measure performance improvement
- Handle thread safety carefully

**Exercise 9: Build Compiler Driver with REPL**
- Implement read-eval-print loop
- Compile and execute expressions interactively
- Support command-line interface
- Add error recovery for REPL

**Exercise 10: Build Complete Test Suite**
- Write unit tests for each phase (100+ tests)
- Write integration tests (20+ tests)
- Write end-to-end tests (10+ tests)
- Measure test coverage (JaCoCo)
- Target 80%+ coverage

---

## 5. Common Pitfalls

### 5.1 Incorrect Phase Ordering

**Pitfall**: Phases in wrong order causing errors
```java
// WRONG: Type checking before symbol resolution
astRoot.accept(new TypeChecker());
astRoot.accept(new LocalDefine());  // Symbols not resolved yet!
```

**Solution**: Correct phase order
```java
// CORRECT: Resolve symbols before type checking
astRoot.accept(new LocalDefine());
astRoot.accept(new TypeChecker());
```

### 5.2 Not Checking for Errors Between Phases

**Pitfall**: Continuing compilation despite errors
```java
// WRONG: No error checking
astRoot.accept(new TypeChecker());
var ir = buildIR(astRoot);  // May have type errors!
```

**Solution**: Check for errors before continuing
```java
// CORRECT: Check errors before proceeding
astRoot.accept(new TypeChecker());
if (errorIssuer.hasErrors()) {
    return;  // Stop compilation
}
var ir = buildIR(astRoot);
```

### 5.3 Incorrect Data Flow Between Phases

**Pitfall**: Phases not using results from previous phases
```java
// WRONG: Type checker not using symbol table
TypeChecker typeChecker = new TypeChecker();  // No symbol table!
astRoot.accept(typeChecker);
```

**Solution**: Pass data between phases correctly
```java
// CORRECT: Type checker uses symbol table
astRoot.accept(new LocalDefine());
SymbolTable symbolTable = getSymbolTable();

TypeChecker typeChecker = new TypeChecker(symbolTable);
astRoot.accept(typeChecker);
```

### 5.4 Memory Leaks in Long-Running Compiler

**Pitfall**: Not releasing resources between compilations
```java
// WRONG: Accumulating data across compilations
public Compiler() {
    this.symbolTable = new SymbolTable();  // Never cleared!
    this.irBuilder = new CymbolIRBuilder();  // Never reset!
}

public void compile(String source) {
    // Uses same symbolTable and irBuilder
    // Data accumulates across compilations!
}
```

**Solution**: Reset or recreate resources
```java
// CORRECT: Reset between compilations
public void compile(String source) {
    this.symbolTable = new SymbolTable();  // New symbol table
    this.irBuilder = new CymbolIRBuilder();  // New IR builder

    // Compilation...
}
```

### 5.5 Thread Safety Issues in Parallel Compilation

**Pitfall**: Shared mutable state in parallel phases
```java
// WRONG: Shared state across threads
public class IRBuilder {
    private static int tempCounter = 0;  // Shared!
    // ...
}

// Parallel compilation
sources.parallelStream().forEach(source -> {
    new IRBuilder().build(source);  // Race condition!
});
```

**Solution**: Use thread-local or instance state
```java
// CORRECT: Instance state, not shared
public class IRBuilder {
    private int tempCounter = 0;  // Instance, not static
    // ...
}

// Parallel compilation
sources.parallelStream().forEach(source -> {
    var builder = new IRBuilder();  // Separate instance per compilation
    builder.build(source);
});
```

### 5.6 Incorrect Error Recovery

**Pitfall**: Stopping compilation at first error
```java
// WRONG: Stop at first error
for (var phase : phases) {
    phase.execute(astRoot);
    if (errorIssuer.hasErrors()) {
        return;  // Stop!
    }
}
```

**Solution**: Continue compilation for better error reporting
```java
// CORRECT: Continue to report multiple errors
for (var phase : phases) {
    phase.execute(astRoot);
    // Don't return, collect all errors
}

// Report all errors at end
errorIssuer.getErrors().forEach(error ->
    System.err.println(error)
);
```

### 5.7 Not Validating Compiler Output

**Pitfall**: Not verifying generated bytecode
```java
// WRONG: No validation of output
String bytecode = generateCode(ir);
saveBytecode(bytecode, outputFilename);
```

**Solution**: Validate before output
```java
// CORRECT: Validate bytecode
String bytecode = generateCode(ir);

// Validate bytecode syntax
if (!isValidBytecode(bytecode)) {
    throw new CompilerException("Invalid bytecode generated");
}

// Validate bytecode semantics
var vm = new VirtualMachine();
if (!vm.canExecute(bytecode)) {
    throw new CompilerException("Bytecode cannot be executed");
}

saveBytecode(bytecode, outputFilename);
```

### 5.8 Poor Test Coverage

**Pitfall**: Only testing happy paths
```java
// WRONG: Only testing valid code
@Test
void testValidCompilation() {
    String source = "int x = 5;";
    var result = compile(source);
    assertThat(result.isSuccess()).isTrue();
}
```

**Solution**: Test error paths and edge cases
```java
// CORRECT: Test various scenarios
@Test
void testValidCompilation() {
    String source = "int x = 5;";
    var result = compile(source);
    assertThat(result.isSuccess()).isTrue();
}

@Test
void testSyntaxError() {
    String source = "int x =";  // Missing expression
    var result = compile(source);
    assertThat(result.isSuccess()).isFalse();
    assertThat(result.getErrors()).hasSize(1);
}

@Test
void testTypeError() {
    String source = "int x = \"hello\";";  // Type mismatch
    var result = compile(source);
    assertThat(result.isSuccess()).isFalse();
    assertThat(result.getErrors()[0].getType()).isEqualTo(ErrorType.SEMANTIC);
}
```

---

## 6. Additional Resources

### 6.1 Recommended Reading

**Compiler Architecture**:
- **"Compilers: Principles, Techniques, and Tools"** (Dragon Book) by Aho et al.
  - Complete compiler architecture overview
  - Phase-by-phase implementation details

- **"Engineering a Compiler"** by Cooper & Torczon
  - Chapter 1: Introduction to Compilation
  - Chapter 2: Scanners
  - Chapter 3: Parsers
  - ... all phases

**Software Engineering**:
- **"Clean Architecture"** by Robert C. Martin
  - Chapter on compiler architecture patterns

- **"Building Microservices"** by Sam Newman
  - Modularity and dependency management (applicable to compiler phases)

### 6.2 Online Resources

**Tutorials & Documentation**:
- [LLVM Compiler Architecture](https://llvm.org/docs/CompilerInfrastructure.html)
- [GCC Internals Manual](https://gcc.gnu.org/onlinedocs/gccint/)
- [Compilers: The Frontend and Backend](https://www.tutorialspoint.com/compiler_design/compiler_design_phases_of_compilation.htm)

**Testing**:
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [AssertJ Documentation](https://assertj.github.io/doc/)
- [JaCoCo User Guide](https://www.jacoco.org/jacoco/trunk/doc/)

**Performance**:
- [Java Performance Tuning Guide](https://docs.oracle.com/javase/8/docs/technotes/guides/performance/)
- [JProfiler Documentation](https://www.ej-technologies.com/products/jprofiler/overview.html)

### 6.3 Practice Problems

**Beginner**:
- Trace compilation through all phases
- Implement phase timing
- Build error reporting system

**Intermediate**:
- Integrate EP17 call graph
- Implement compiler options
- Add new language feature end-to-end

**Advanced**:
- Implement incremental compilation
- Build parallel compilation pipeline
- Create comprehensive test suite

### 6.4 Debugging Tools

**EP20 Built-in Tools**:
- `Compiler.java`: Main compilation pipeline
- `ErrorIssuer`: Error reporting
- `Dumper`: AST/IR visualization
- `CFG.toString()`: CFG visualization (DOT/mermaid)

**External Tools**:
- **JProfiler**: Profile Java performance
- **VisualVM**: Monitor memory and CPU
- **GDB**: Debug VM execution
- **Graphviz**: Visualize CFG and call graphs

### 6.5 Key Files to Study

**Complete Pipeline**:
- `ep20/src/main/java/org/teachfx/antlr4/ep20/Compiler.java` - Main compiler
- `ep20/src/main/java/org/teachfx/antlr4/ep20/driver/Phase.java` - Phase management
- `ep20/src/main/java/org/teachfx/antlr4/ep20/driver/Task.java` - Compilation task

**Integration**:
- `ep17/src/main/java/org/teachfx/antlr4/ep17/CallGraphBuilder.java` - Call graph
- `ep18/src/main/java/org/teachfx/antlr4/ep18/vm/VirtualMachine.java` - VM
- `ep19/src/main/java/org/teachfx/antlr4/ep19/pipeline/CompilerPipeline.java` - Pipeline

**Tests**:
- `ep20/src/test/java/org/teachfx/antlr4/ep20/pass/codegen/EndToEndCompilationTest.java`
- `ep20/src/test/java/org/teachfx/antlr4/ep20/IntegrationTest.java`
- `ep20/src/test/java/org/teachfx/antlr4/ep20/ComprehensiveTest.java`

---

**Summary**: This chapter covers end-to-end compiler pipeline integration, focusing on phase separation, data flow, error handling, module integration (EP17-EP20), testing strategies, and performance optimization. Mastering the complete compiler pipeline is essential for building production-quality compilers and extending them with new features.
