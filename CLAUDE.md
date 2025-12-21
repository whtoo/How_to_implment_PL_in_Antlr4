# CLAUDE.md


This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**How to implement PL in ANTLR4** is a systematic compiler construction educational project that demonstrates how to implement a programming language using ANTLR4. It consists of 21 progressive episodes (EP1-EP21) that guide learners from basic lexer/parser implementation to advanced compiler optimization techniques.

The project implements the **Cymbol language** (a C-like language) with a complete compiler pipeline: frontend (lexer, parser, AST), middle-end (type system, semantic analysis, IR), and backend (code generation, VM execution).

### 文档地图 (Documentation Map)

| 章节 | 内容概述 | 相关 EP 范围 | 关键用途 |
|------|----------|--------------|----------|
| **Architecture** | 项目结构、模块划分、核心组件 | EP1-EP21 (全部) | 理解整体架构和模块职责 |
| **Development Commands** | 构建、测试、执行命令 | EP1-EP21 (全部) | 日常开发工作流 |
| **Testing Strategy** | 测试级别、覆盖率要求、运行测试 | EP1-EP21 (全部) | 确保代码质量和测试合规 |
| **ANTLR4 Integration** | 语法文件、解析器生成 | EP1-EP10 (基础), EP20 (主语法) | 语法设计和解析器开发 |
| **Key File Locations** | 核心编译器组件文件位置 | EP20 (主参考), 其他 EP 类似 | 快速定位关键源代码 |
| **Development Guidelines** | 代码风格、错误处理、日志规范 | EP1-EP21 (全部) | 保持代码一致性和可维护性 |
| **Common Tasks** | 常见开发任务步骤 | EP1-EP21 (全部) | 指导常见操作（添加 AST 节点等） |
| **Claude Code Skills** | Compiler Development Skill 使用指南，覆盖开发、重构、调试、架构设计和规范设计任务 | EP1-EP21 (全部) | 自动获取编译器开发专业知识，包括规范设计指导 |
| **Agent 工作流程** | Fork 子代理、上下文监控、进度保存 | EP1-EP21 (全部) | 管理复杂任务和上下文限制 |
| **CCLSP Code Analysis** | 智能代码分析工具使用指南 | EP1-EP21 (全部) | 高效代码导航和问题诊断 |
| **Module Code Structure Exploration Guide** | 模块代码探索方法论 | EP1-EP21 (全部) | 系统化理解模块内部结构 |
| **Context7 MCP Usage** | 上下文管理工具使用指南 | EP1-EP21 (全部) | 保持会话连续性和决策追踪 |
| **Episode-Specific Guidance** | EP 特定工作焦点和记忆管理 | EP1-EP21 (分组) | 在特定 EP 工作时优化上下文 |

## Architecture

### Module Structure
The project is organized as 21 Maven modules (`ep1` through `ep21`), each representing a learning stage:
- **EP1-EP10**: Foundation (lexer, parser, AST, visitors, interpreters)
- **EP11-EP20**: Compiler core (type system, symbol tables, IR, CFG, code generation)
- **EP21**: Advanced optimization (dataflow analysis, SSA form)

Each module has:
- `src/main/java/` - Java source code
- `src/main/antlr4/` - ANTLR grammar files (`.g4`)
- `src/test/java/` - Unit and integration tests
- `pom.xml` - Module-specific Maven configuration

### Key Components
- **Frontend**: `Cymbol.g4` grammar, AST nodes, visitors
- **Type System**: Symbol tables, type checking, semantic analysis
- **Intermediate Representation**: Three-address code format
- **Control Flow Graph**: Basic blocks, dominance analysis
- **Virtual Machine**: Stack-based VM (ep18)
- **Optimization Framework**: Dataflow analysis, SSA (ep21)

### Core Design Patterns
- **Visitor Pattern**: Used throughout for AST traversal
- **Builder Pattern**: For constructing complex objects
- **Factory Pattern**: For creating AST nodes
- **Singleton Pattern**: For global compiler components

## Development Commands

### Build and Test
```bash
# Build entire project
mvn clean compile

# Run all tests
mvn test

# Build specific module (e.g., ep20)
mvn clean compile -pl ep20

# Run tests for specific module
mvn test -pl ep20

# Generate test coverage report
mvn jacoco:report
```

### Using Development Scripts
The `scripts/` directory contains multi-platform runner scripts:
```bash
# Linux/macOS
./scripts/run.sh compile ep20
./scripts/run.sh run ep20 "program.cymbol"
./scripts/run.sh test ep20

# Windows PowerShell
.\scripts\run.ps1 compile ep20
.\scripts\run.ps1 run ep20 "program.cymbol"

# Windows CMD
scripts\run.bat compile ep20
scripts\run.bat run ep20 "program.cymbol"
```

### Direct Execution
```bash
# Run compiler with a Cymbol program
mvn compile exec:java -pl ep20 -Dexec.args="program.cymbol"

# Run specific main class
mvn compile exec:java -pl ep20 -Dexec.mainClass="org.teachfx.antlr4.ep20.Compiler"
```

## Testing Strategy

### Test Levels
1. **Unit Tests**: Individual classes/methods (JUnit 5 + AssertJ)
2. **Integration Tests**: Module interactions and compiler phases
3. **End-to-End Tests**: Complete compilation pipeline

### Test Coverage Requirements
- Overall coverage: ≥85%
- Core modules (IR, CFG, optimizer): ≥90%
- New functionality: 100%

### Running Tests
```bash
# Run all tests in a module
mvn test -pl ep20

# Run specific test class
mvn test -pl ep20 -Dtest=ArraysTest

# Run tests matching pattern
mvn test -pl ep20 -Dtest="*ExprNodeTest"
```

## ANTLR4 Integration

### Grammar Files
- Located in `src/main/antlr4/org/teachfx/antlr4/ep*/parser/`
- Main grammar: `Cymbol.g4` (ep20)
- ANTLR4 generates lexer, parser, and visitor classes

### Regenerating Parser Code
```bash
# Maven automatically regenerates ANTLR4 code during build
mvn generate-sources -pl ep20

# Or manually using ANTLR4 tool
java -jar antlr-4.13.2-complete.jar -visitor -no-listener Cymbol.g4
```

## Key File Locations

### Core Compiler Components
- **Main Compiler**: `ep20/src/main/java/org/teachfx/antlr4/ep20/Compiler.java`
- **Cymbol Grammar**: `ep20/src/main/antlr4/org/teachfx/antlr4/ep20/parser/Cymbol.g4`
- **AST Base Class**: `ep20/src/main/java/org/teachfx/antlr4/ep20/ast/ASTNode.java`
- **Symbol Table**: `ep20/src/main/java/org/teachfx/antlr4/ep20/symtab/SymbolTable.java`
- **IR Generation**: `ep20/src/main/java/org/teachfx/antlr4/ep20/ir/IRGenerator.java`

### Configuration Files
- **Root POM**: `/pom.xml` (parent POM for all modules)
- **Module POMs**: `ep*/pom.xml` (module-specific configuration)
- **CI/CD**: `.github/workflows/maven.yml` (GitHub Actions workflow)

### Documentation
- **Main README**: `/README.md` (Chinese) and `/README_EN.md` (English)
- **Technical Docs**: `/.qoder/repowiki/en/` (232 files of comprehensive documentation)
- **Project Docs**: `/docs/` (course materials, testing strategy, design principles)

## Development Guidelines

### Code Style
- Follow Java 21+ syntax and features
- Use meaningful variable and method names
- Add Javadoc comments for public APIs
- Maintain consistent indentation (4 spaces)

### Error Handling
- Use checked exceptions for recoverable errors
- Use runtime exceptions for programming errors
- Provide meaningful error messages with context
- Log errors using Log4j2

### Logging
- Use Log4j2 for all logging
- Different log levels: TRACE, DEBUG, INFO, WARN, ERROR
- Configure logging in `log4j2.xml` files

### Adding New Features
1. Start with tests (TDD approach)
2. Implement in appropriate module based on complexity
3. Update documentation in `.qoder/repowiki/`
4. Ensure backward compatibility
5. Run full test suite before committing

## Common Tasks

### Adding a New AST Node
1. Create class in `ep*/src/main/java/org/teachfx/antlr4/ep*/ast/`
2. Extend appropriate base class (ExprNode, StmtNode, etc.)
3. Implement visitor pattern methods
4. Add grammar rule in `Cymbol.g4`
5. Create corresponding test class

### Modifying the Grammar
1. Edit `Cymbol.g4` or create new `.g4` file
2. Run `mvn generate-sources` to regenerate parser
3. Update visitor implementations
4. Update tests to reflect changes

### Debugging Compilation Issues
1. Enable debug logging in `log4j2.xml`
2. Use `-Dlog4j.configurationFile=path/to/log4j2-debug.xml`
3. Check AST visualization tools in ep19/ep20
4. Use CFG visualization for control flow issues

## Claude Code Skills

### Compiler Development Skill

This project includes a specialized **Compiler Development** skill that provides comprehensive guidance for working with the Cymbol language compiler implementation. The skill is located at `.claude/skills/compiler-development/SKILL.md`.

#### Skill Overview

The Compiler Development skill is a Claude Code skill that activates automatically when users ask about compiler-related topics. It provides expertise across the complete compiler pipeline implemented in this 21-episode educational project.

**默认启动策略**：对于本项目的开发、重构、调试、架构设计和规范设计任务，Compiler Development Skill 会自动激活并提供专业指导：
- **开发任务**：新增功能、模块扩展、API 修改（EP1-EP21 各模块）
- **重构任务**：代码优化、设计模式调整、架构重构
- **调试任务**：问题诊断、错误修复、性能调优、编译问题调试
- **架构设计任务**：系统架构设计、模块划分、接口设计、技术选型
- **规范设计任务**：语言规范设计、API规范设计、编码规范制定、接口契约设计

技能自动检测编译器相关话题并激活，无需手动调用。

#### Skill Capabilities

The Compiler Development skill can assist with:

1. **ANTLR4 Grammar Design and Analysis**
   - Explain Cymbol language syntax and grammar rules
   - Debug parsing errors, ambiguities, and grammar conflicts
   - Guide grammar modifications, extensions, and best practices

2. **Abstract Syntax Tree (AST) Development**
   - Explain AST node types, hierarchy, and design patterns
   - Help traverse, analyze, and manipulate AST structures
   - Guide AST transformation and visitor pattern implementation

3. **Type System and Symbol Table Implementation**
   - Explain type checking, type inference, and type safety
   - Debug type-related errors and type conversion issues
   - Guide symbol table construction, scope management, and name resolution

4. **Intermediate Representation and Optimization**
   - Explain three-address code generation and IR design
   - Analyze control flow graphs (CFG) and basic blocks
   - Guide IR optimization techniques and dataflow analysis
   - Explain SSA form construction and optimization passes

5. **Code Generation and Virtual Machine**
   - Explain bytecode generation for stack-based virtual machine
   - Debug code generation issues and register allocation
   - Guide VM implementation, execution, and memory management
   - Explain garbage collection and runtime system design

6. **Project Navigation and Development Workflow**
   - Navigate the 21-episode progressive structure (EP1-EP21)
   - Explain module dependencies, interactions, and build system
   - Guide testing strategies, coverage requirements, and TDD practices
   - Provide development scripts and multi-platform support

7. **Compiler Specification and Standards Design**
   - Guide language specification design and formal definitions
   - Assist with API contract design and interface specifications
   - Help establish coding standards and best practices
   - Support documentation standards and technical writing guidelines

#### When to Use the Skill

The Compiler Development skill activates when users ask about:
- ANTLR4 grammar design, parsing, and debugging
- Cymbol language syntax, semantics, and language design
- Compiler specification design and standards definition
- Compiler construction concepts, phases, and implementation
- Type systems, symbol tables, semantic analysis, and name resolution
- Intermediate representation, control flow, optimization, and code generation
- Virtual machine design, bytecode execution, and runtime systems
- Project-specific module navigation, testing, and development workflows

#### Usage Examples

```bash
# Example: Debug a complex parsing issue
User: My Cymbol program with nested structs fails to parse with "mismatched input"

Compiler Development: This is likely a grammar ambiguity issue. Let me analyze...
The problem may be in the structMember rule conflicting with varDecl.
Try adding syntactic predicates or restructuring the grammar rules.

# Example: Understand the compilation pipeline
User: How does type information flow through the compiler phases?

Compiler Development: Type information flows through these phases:
1. Lexing/Parsing: Token streams → Parse trees
2. AST Construction: Parse trees → AST with type placeholders
3. Symbol Collection: AST → Symbol table with type declarations
4. Type Checking: AST + Symbol table → Fully typed AST
5. IR Generation: Typed AST → Typed intermediate representation
Check TypeChecker.java and SymbolTable.java in ep20 for details.
```

#### Skill Location and Activation

- **Skill Directory**: `.claude/skills/compiler-development/`
- **Skill File**: `SKILL.md` (with YAML frontmatter)
- **Activation**: Automatic based on skill description matching compiler-related topics
- **Scope**: Project-specific knowledge of the complete Cymbol compiler implementation

#### Skill Structure

The skill follows Claude Code's skill format:
- **YAML Frontmatter**: Defines skill name, description, and allowed tools
- **Project Overview**: Context about the 21-episode compiler project
- **Core Development Flow**: Detailed guidance for each compiler phase
- **Project Navigation**: Key file locations and testing strategies
- **Development Workflows**: Common tasks and best practices
- **Troubleshooting**: Solutions for common compiler development issues

#### Extending the Skill

To extend or modify the Compiler Development skill:
1. Edit `.claude/skills/compiler-development/SKILL.md`
2. Update the YAML frontmatter description if adding new capabilities
3. Add new sections for additional compiler topics or features
4. Update this section in CLAUDE.md to reflect changes

#### Integration with Development Environment

The skill integrates with the project's development tools:
- **Maven Build System**: Module-specific build and test commands
- **Multi-platform Scripts**: `scripts/` directory for Linux, macOS, and Windows
- **Testing Framework**: JUnit 5 with coverage requirements (≥85% overall)
- **Documentation**: `.qoder/repowiki/` with 232+ technical documentation files

## Agent 工作流程

### Overview

在复杂的编译器开发任务中，合理使用 Claude Code 的 Agent 功能可以显著提高效率和上下文管理能力。本工作流程指导如何通过 fork 子代理、监控上下文限制、保存进度到文档等方式，实现长期、复杂的开发任务。

### Fork 新子代理

当遇到以下情况时，应考虑 fork 新的子代理：

1. **复杂多阶段任务**：需要独立执行编译、测试、调试等不同阶段
2. **上下文限制临近**：当前对话上下文长度接近模型限制（约 200K tokens）
3. **专业化分工**：需要不同专长的代理（如语法分析、类型检查、代码生成）
4. **并行执行**：多个独立任务可以同时进行以加快进度

**操作方法**：
```bash
# 使用 Task 工具启动新的子代理
# 示例：启动编译器开发代理处理特定 EP 模块
Task(description="处理 ep18 GC 集成", prompt="继续实现 ep18 的垃圾回收功能...", subagent_type="general-purpose")
```

### 监控上下文限制

定期监控上下文使用情况，避免超出限制：

1. **识别上下文积累点**：
   - 大量代码文件读取和分析
   - 长时间对话历史
   - 多次工具调用记录

2. **预警信号**：
   - 响应速度明显下降
   - 模型开始遗忘早期对话内容
   - 工具调用结果被截断

3. **主动管理策略**：
   - 定期总结进度并保存到文档
   - 关闭不再需要的背景代理
   - 使用 Context7 记录关键决策点

### 保存进度到文档

在 fork 新代理前，必须将当前进度保存到相关文档：

1. **进度文档位置**：
   - 模块特定文档：`ep*/docs/` 目录下（如 `ep18/docs/GC_集成进度_20251221.md`）
   - 技术设计文档：`/.qoder/repowiki/en/` 目录
   - 测试进展文档：`ep*/src/test/resources/` 目录

2. **保存内容**：
   - 当前实现状态和已完成的功能
   - 遇到的错误和解决方案
   - 下一步计划的具体任务
   - 关键设计决策和理由

3. **文档格式**：
   ```markdown
   ## 进度更新 [YYYY-MM-DD]

   ### 已完成
   - 功能 A：实现细节...
   - 功能 B：测试结果...

   ### 当前问题
   - 问题 1：描述...
   - 解决方案：尝试...

   ### 下一步计划
   - 任务 1：具体描述...
   - 任务 2：依赖关系...

   ### 设计决策
   - 决策 1：选择方案 X，因为...
   ```

### Fork 继续工作流程

完整的 fork-and-continue 工作流程：

```bash
# 1. 检查当前上下文状态
# 2. 保存进度到相关文档
# 3. 使用 Task 工具 fork 新代理
# 4. 新代理读取进度文档继续工作
# 5. 重复此流程直到任务完成

# 示例：继续 ep18 的垃圾回收实现
Task(description="继续 ep18 GC 实现", prompt="读取 ep18/docs/GC_集成进度_20251221.md 中的进度，继续实现垃圾回收器的内存压缩功能...", subagent_type="general-purpose")
```

### 默认 Agent 配置

对于本项目的开发、重构、调试、架构设计和规范设计任务，**默认自动启动 Compiler Development Skill**：

- **开发任务**：新增功能、模块扩展、API 修改
- **重构任务**：代码优化、设计模式调整、架构重构
- **调试任务**：问题诊断、错误修复、性能调优、编译问题调试
- **架构设计任务**：系统架构设计、模块划分、接口设计、技术选型
- **规范设计任务**：语言规范设计、API规范设计、编码规范制定、接口契约设计

**自动激活机制**：
- 当用户请求涉及编译器开发、ANTLR4 语法、类型系统等话题时
- Compiler Development Skill 自动提供专业知识指导
- 与 CCLSP、Context7 等工具无缝集成

## CCLSP Code Analysis

### Overview

**CCLSP** (Claude Code Language Server Protocol) provides intelligent code analysis through language servers configured for this project. Always prefer using CCLSP tools over manual code search when analyzing Java, TypeScript, or Python code.

#### Available Language Servers
- **Java**: JDT Language Server (`jdtls`) configured for `.java` files
- **TypeScript/JavaScript**: TypeScript Language Server (`typescript-language-server`) for `.js`, `.ts`, `.jsx`, `.tsx` files
- **Python**: Python Language Server (`pylsp`) for `.py`, `.pyi` files

### How to Use CCLSP

When analyzing code in this project, follow these guidelines:

1. **Always Use CCLSP Tools First**: Before using `Grep`, `Glob`, or manual search, use CCLSP tools:
   ```bash
   # Instead of searching with grep:
   # grep -r "CymbolStackVM" .

   # Use CCLSP tools:
   mcp__cclsp__find_definition
   mcp__cclsp__find_references
   mcp__cclsp__get_diagnostics
   ```

2. **Key CCLSP Functions**:
   - `mcp__cclsp__find_definition`: Find exact symbol definition location
   - `mcp__cclsp__find_references`: Find all references to a symbol
   - `mcp__cclsp__get_diagnostics`: Get real-time code analysis warnings/errors
   - `mcp__cclsp__restart_server`: Restart language servers if needed

3. **Server Configuration**:
   - Configuration file: `.claude/cclsp.json`
   - Java server: `jdtls` (installed at `/usr/local/bin/jdtls`)
   - Workspace root: Current Maven multi-module project directory
   - Servers auto-start on first use

### Code Analysis Workflow

#### When Analyzing Java Code
1. **Start with diagnostics**: Use `mcp__cclsp__get_diagnostics` to check code quality
   - Detects unused imports, variables, methods
   - Identifies type errors and code smells
   - Provides precise line locations

2. **Navigate symbols**: Use `mcp__cclsp__find_definition` and `mcp__cclsp__find_references`
   - Accurate symbol resolution across entire project
   - Handles method overloading and inheritance
   - Works across module boundaries (ep1-ep21)

3. **Verify server status**: Use `mcp__cclsp__restart_server` if servers are unresponsive

#### Example Workflow
```bash
# Analyze a Java class for issues
User: Check the CymbolStackVM class for problems

# Step 1: Get diagnostics
mcp__cclsp__get_diagnostics file_path="ep18/src/main/java/org/teachfx/antlr4/ep18/stackvm/CymbolStackVM.java"
# Returns: 6 warnings (unused imports, unused variables)

# Step 2: Find all usages
mcp__cclsp__find_references file_path="..." symbol_name="CymbolStackVM" symbol_kind="class"
# Returns: 12 references across source and test files

# Step 3: Jump to definition
mcp__cclsp__find_definition file_path="..." symbol_name="executeInstruction" symbol_kind="method"
# Returns: Exact method definition location
```

### Benefits Over Manual Search

1. **Accuracy**: Language servers understand Java semantics (inheritance, overloading, generics)
2. **Completeness**: Finds all references, including those in generated code and dependencies
3. **Real-time analysis**: Provides current diagnostics based on actual compilation
4. **Cross-module awareness**: Works across all 21 Maven modules

### Server Management

- **Auto-start**: Servers start automatically on first CCLSP tool use
- **Configuration**: Modify `.claude/cclsp.json` to add/remove language servers
- **Restarting**: Use `mcp__cclsp__restart_server` if analysis seems stale or servers hang
- **Java-specific**: JDTLS integrates with Maven project structure and dependencies

### Integration with Other Tools

CCLSP complements other Claude Code features:
- **Context7**: Use CCLSP for real-time code analysis, Context7 for historical context
- **Compiler Development Skill**: CCLSP provides low-level code navigation, skill provides high-level compiler concepts
- **Maven Build**: CCLSP uses actual project build configuration for accurate analysis

## Module Code Structure Exploration Guide

### Overview
When exploring the code structure of a specific module (e.g., ep20), follow a systematic approach that prioritizes CCLSP tools for intelligent code analysis, supplemented by minimal use of grep and read for specific tasks. This guide provides a step-by-step workflow for efficiently understanding module architecture.

### Why Prioritize CCLSP?
CCLSP language servers provide semantic understanding of code that surpasses text-based search:
- **Semantic accuracy**: Understands Java inheritance, method overloading, and generics
- **Cross-module awareness**: Works across all 21 Maven modules
- **Real-time analysis**: Based on actual compilation context
- **Precise navigation**: Exact symbol definition and reference locations

### Exploration Workflow

#### Step 1: Module Context Establishment
Before diving into code, establish module context:
- **Check pom.xml**: Understand dependencies and build configuration
  ```bash
  # Minimal read usage for configuration
  read file_path="ep20/pom.xml" limit=30
  ```
- **Identify module role**: Based on episode number (EP1-EP21 progression)

#### Step 2: CCLSP-Driven Code Analysis
Start with CCLSP tools to get comprehensive code understanding:

1. **Get module diagnostics** - Identify code quality issues:
   ```bash
   # Get diagnostics for main source directory
   mcp__cclsp__get_diagnostics file_path="ep20/src/main/java/org/teachfx/antlr4/ep20/Compiler.java"
   ```

2. **Find key class definitions** - Locate main entry points:
   ```bash
   # Find definition of Compiler class
   mcp__cclsp__find_definition file_path="ep20/src/main/java/org/teachfx/antlr4/ep20/Compiler.java" symbol_name="Compiler" symbol_kind="class"
   ```

3. **Explore symbol references** - Understand usage patterns:
   ```bash
   # Find all references to SymbolTable
   mcp__cclsp__find_references file_path="ep20/src/main/java/org/teachfx/antlr4/ep20/symtab/SymbolTable.java" symbol_name="SymbolTable" symbol_kind="class" include_declaration=true
   ```

4. **Navigate package structure** - Use CCLSP to understand organization:
   ```bash
   # Find definitions in specific packages
   mcp__cclsp__find_definition file_path="ep20/src/main/java/org/teachfx/antlr4/ep20/ast/ASTNode.java" symbol_name="ASTNode" symbol_kind="class"
   ```

#### Step 3: Targeted grep Usage (Minimal)
Use grep only for patterns that CCLSP doesn't handle well:

1. **Find specific patterns** in non-Java files:
   ```bash
   # Search for specific configuration patterns
   grep pattern="<artifactId>" glob="ep20/pom.xml" output_mode="content"
   ```

2. **Locate TODO/FIXME comments**:
   ```bash
   # Find development notes
   grep pattern="TODO|FIXME" path="ep20/src" output_mode="files_with_matches" head_limit=5
   ```

3. **Search for specific string literals**:
   ```bash
   # Find error message patterns
   grep pattern="error.*message" path="ep20/src/main/java" output_mode="content" -i head_limit=3
   ```

#### Step 4: Strategic Read Operations
Use read for specific file examination:

1. **Read key configuration files**:
   ```bash
   # Examine grammar file structure
   read file_path="ep20/src/main/antlr4/org/teachfx/antlr4/ep20/parser/Cymbol.g4" limit=50
   ```

2. **View test structure**:
   ```bash
   # Understand test organization
   read file_path="ep20/src/test/java/org/teachfx/antlr4/ep20/CompilerTest.java" limit=30
   ```

3. **Check log configuration**:
   ```bash
   # Examine logging setup
   read file_path="ep20/src/main/resources/log4j2.xml" limit=20
   ```

### Example: Exploring ep20 Module

#### Quick Exploration Script
```bash
# 1. Check module configuration
read file_path="ep20/pom.xml" limit=20

# 2. Get overall code quality
mcp__cclsp__get_diagnostics file_path="ep20/src/main/java/org/teachfx/antlr4/ep20/Compiler.java"

# 3. Find main compiler components
mcp__cclsp__find_definition file_path="ep20/src/main/java/org/teachfx/antlr4/ep20/Compiler.java" symbol_name="compile" symbol_kind="method"

# 4. Explore type system
mcp__cclsp__find_references file_path="ep20/src/main/java/org/teachfx/antlr4/ep20/symtab/TypeChecker.java" symbol_name="TypeChecker" symbol_kind="class"

# 5. Targeted grep for specific patterns
grep pattern="implements.*Visitor" path="ep20/src/main/java" output_mode="files_with_matches"
```

### Best Practices

1. **CCLSP First Principle**: Always start with CCLSP tools before using grep/read
2. **Progressive Exploration**: Start high-level (Compiler class), then drill down to specific components
3. **Context Preservation**: Use findings to build mental model of module architecture
4. **Tool Selection Guide**:
   - Use CCLSP for: class/method navigation, type analysis, reference finding
   - Use grep for: text patterns, comments, configuration values
   - Use read for: file structure examination, configuration details
5. **Efficiency Tips**:
   - Combine multiple CCLSP calls in parallel when independent
   - Use head_limit to avoid overwhelming output
   - Restart servers if analysis seems stale: `mcp__cclsp__restart_server`

### Common Exploration Scenarios

| Scenario | Primary Tool | Secondary Tool |
|----------|--------------|----------------|
| Understanding class hierarchy | CCLSP find_definition | grep for "extends"/"implements" |
| Finding usages of a method | CCLSP find_references | grep for method name |
| Examining configuration | read file | grep for specific values |
| Locating error handling | CCLSP diagnostics | grep for "throw" or "catch" |
| Understanding test coverage | read test files | grep for "@Test" |

### Integration with Other Tools
- **Compiler Development Skill**: Use for high-level compiler concepts while exploring
- **Context7**: Check for previous exploration history of the module
- **Maven**: Build module to ensure CCLSP has accurate compilation context

## Context7 MCP Usage

### Overview

**Context7** is a context management MCP server that automatically records and retrieves project-specific context.
This enables seamless continuity across conversations by maintaining awareness of previous discussions and decisions.

#### Key Capabilities
- **Automatic Context Recording**: Tracks project activities, decisions, and technical details
- **Context Retrieval**: Access to historical conversation data and project state
- **Session Management**: Maintains continuity across multiple development sessions

### How to Use Context7

When working on this project, Context7 automatically tracks your interactions and provides relevant context when needed.

#### Context-Aware Development Workflow

```bash
# Example: Resuming work on a specific episode
User: Let's continue implementing the type checker in ep20

Claude: I'll check the context to see our previous progress on the type checker...
# Context7 will provide previous discussion, implementation status, and next steps
# You can then continue work seamlessly

# Example: Getting project state
User: What's the current status of this project?

Claude: I'll retrieve the project context to provide an accurate status update...
# Context7 provides current episode progress, recent changes, open issues, etc.

# Example: Understanding previous decisions
User: Why did we implement the symbol table this way?

Claude: I'll check the context for our previous discussions about symbol table design...
# Context7 provides historical rationale, alternatives considered, final decisions
```

#### Best Practices for Using Context7

1. **Always Check Context First**: Before starting new work, ask Context7 for relevant context
   ```
   Let me check the context to see if there's any relevant history for this task...
   ```

2. **Maintain Context-Awareness**: Acknowledge when Context7 provides relevant information
   ```
   Based on the context, I can see we previously worked on [X] and the next step is [Y]...
   ```

3. **Update Context with Decisions**: When making important decisions, ensure they're captured
   ```
   I'll record this decision in the context for future reference...
   ```

4. **Use Context for Consistency**: Reference previous patterns and conventions from context
   ```
   According to the context, we typically implement [pattern] for [scenario]...
   ```

#### Session Continuity Guidelines

- **New Sessions**: Always begin by checking Context7 for project state and recent activities
- **Task Resumption**: Use Context7 to retrieve the exact state and next steps for paused tasks
- **Decision Tracking**: When architectural decisions are made, they should be accessible via Context7
- **Issue Resolution**: Check Context7 for historical context on recurring issues

#### Integration with Development Tasks

Context7 works seamlessly with the existing Claude Code skills:

- **Compiler Development Skill**: Context7 maintains history of compiler implementation decisions
- **Project Navigation**: Tracks which episodes/modules have been worked on
- **Testing Strategy**: Records test coverage patterns and testing approaches used
- **Architecture Decisions**: Captures rationale for design choices across the compiler pipeline

#### Limitations and Considerations

- Context7 is **episodic** - it provides historical context but doesn't replace real-time analysis
- Always **validate context** against current code state - files may have changed since last session
- Use context as a **starting point**, not a definitive source - always verify with current state
- **Sensitive information** should not be included in context (API keys, credentials, etc.)

### Configuration

Context7 is configured in `.mcp.json` and is automatically available when working with this project.

## Episode-Specific Guidance

### 概述

当在特定 Episode（EP）模块工作时，应当选择性精简无关当前 EP 的记忆，专注于与当前 EP 相关的技术和架构知识。本指南提供针对不同 EP 阶段的工作焦点和记忆管理策略。

### EP 阶段与关注焦点

| EP 范围 | 核心关注点 | 可忽略的记忆 | 关键关联文件 |
|---------|------------|--------------|--------------|
| **EP1-EP10** (基础阶段) | 词法分析、语法分析、AST 构建、访问者模式 | EP11-EP21 的高级优化、IR 生成、代码生成 | `Cymbol.g4` 语法文件、AST 节点类、Visitor 接口 |
| **EP11-EP20** (编译器核心) | 类型系统、符号表、语义分析、中间表示、控制流图 | EP1-EP10 的基础解析细节、EP21 的高级优化 | `TypeChecker.java`、`SymbolTable.java`、`IRGenerator.java`、`CFG.java` |
| **EP21** (高级优化) | 数据流分析、SSA 形式、优化传递、性能调优 | EP1-EP10 的基础设施、EP11-EP20 的核心编译逻辑 | `DataFlowAnalyzer.java`、`SSAConverter.java`、`OptimizationPass.java` |

### 选择性记忆管理策略

#### 1. 当前 EP 上下文强化
- **深度聚焦**：深入理解当前 EP 模块的特定职责和技术栈
- **关联文件优先**：优先读取和记忆与当前 EP 直接相关的源代码文件
- **测试用例导向**：以当前 EP 的测试用例为引导，理解功能需求

#### 2. 无关 EP 记忆精简
- **层级忽略**：当工作在 EP11-EP20 时，可忽略 EP1-EP10 的具体实现细节
- **概念保留**：保留高层概念（如“AST 节点”），但忽略具体实现类名和方法
- **接口关注**：只关注模块间接口（如 AST 节点类型），忽略内部实现

#### 3. 跨 EP 依赖管理
- **向上依赖**：当前 EP 依赖先前 EP 时，只记忆必要的 API 和接口
- **向下透明**：后续 EP 的功能对当前 EP 透明，无需提前记忆
- **接口契约**：通过清晰的接口契约减少跨 EP 记忆负担

### 工作流程示例

#### 示例：在 EP18（虚拟机）工作时
```bash
# 1. 聚焦 EP18 相关文件
# 关键文件：CymbolStackVM.java、Bytecode指令集、内存管理

# 2. 精简无关记忆
# 忽略：EP1-EP10 的语法分析细节、EP20 的代码生成优化

# 3. 管理跨 EP 依赖
# 只需知道：EP17 产生的字节码格式、EP19 需要的虚拟机接口

# 4. 使用 Context7 记录 EP18 特定决策
# 记录垃圾回收策略、指令集扩展等 EP18 专属决策
```

#### 示例：在 EP20（代码生成）工作时
```bash
# 1. 聚焦 EP20 相关文件
# 关键文件：CodeGenerator.java、寄存器分配、指令选择

# 2. 精简无关记忆
# 忽略：EP1-EP10 的 AST 构建细节、EP18 的虚拟机内部实现

# 3. 管理跨 EP 依赖
# 只需知道：EP19 的优化后 IR、EP18 的目标指令集

# 4. 强化 EP20 特定知识
# 深入记忆代码生成算法、目标平台特性
```

### 工具集成

- **CCLSP**：使用 `find_references` 仅查找当前 EP 范围内的符号引用
- **Context7**：记录当前 EP 的特定决策，避免污染其他 EP 的上下文
- **Compiler Development Skill**：自动提供与当前 EP 相关的编译器知识
- **Task 工具**：fork 子代理时，明确指定 EP 范围和相关文档

### 检查清单

在开始特定 EP 工作前，检查以下事项：

1. [ ] 明确当前 EP 在 21 个阶段中的位置和职责
2. [ ] 识别当前 EP 的关键源代码文件和测试用例
3. [ ] 确定对先前 EP 的最小依赖集合
4. [ ] 规划对后续 EP 的接口设计
5. [ ] 设置 Context7 过滤，聚焦当前 EP 相关历史
6. [ ] 准备 EP 特定的进度文档（如 `ep*/docs/` 目录）

## Notes for Future Claude Code Instances

- This is an **educational project** focused on compiler construction
- The 21-episode structure is **progressive** - later episodes depend on earlier ones
- **ANTLR4** is central to the project - understand grammar files and generated code
- **Visitor pattern** is used extensively for AST traversal
- **Testing is comprehensive** - maintain high coverage standards
- **Documentation is extensive** - check `.qoder/repowiki/` for detailed technical docs
- **Context7 is available** - always check context first for project history and continuity
- The project implements a **complete compiler pipeline** - understand the frontend/middle-end/backend separation