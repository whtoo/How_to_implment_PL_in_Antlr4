---
name: compiler-expert
description: 提供完整的编译器开发专家支持，整合ANTLR4编译器开发专业技术知识和完整的开发生态系统。包括语法分析、语义分析、IR生成、代码优化、虚拟机实现，以及项目构建、测试、调试、部署、MCP服务器配置和跨平台开发工具。优先使用Serena智能代码分析工具。
allowed-tools: mcp__serena__list_dir, mcp__serena__find_file, mcp__serena__search_for_pattern, mcp__serena__get_symbols_overview, mcp__serena__find_symbol, mcp__serena__find_referencing_symbols, mcp__serena__replace_symbol_body, mcp__serena__insert_after_symbol, mcp__serena__insert_before_symbol, mcp__serena__rename_symbol, mcp__serena__write_memory, mcp__serena__read_memory, mcp__serena__list_memories, mcp__serena__check_onboarding_performed, mcp__serena__think_about_collected_information, mcp__serena__think_about_task_adherence, mcp__serena__think_about_whether_you_are_done, mcp__serena__initial_instructions, Read, Grep, Glob, Bash, Edit, Write, mcp__context7__resolve-library-id, mcp__context7__get-library-docs
---

# 编译器专家技能

## 项目概述

**How to implement PL in ANTLR4** 是一个21章节的渐进式编译器教学项目，实现完整的Cymbol语言编译器。项目分为三个主要阶段：

- **EP1-EP10**: 基础阶段（词法语法分析、AST构建、解释器）
- **EP11-EP20**: 编译器阶段（类型系统、语义分析、IR、CFG、代码生成）
- **EP21**: 高级优化（数据流分析、SSA形式）

## 生态概览

**Compiler Expert Ecosystem** 是一个完整的编译器开发专家系统，整合了专业技术知识和完整的开发生态：

### 核心组件
- **专业技术**: ANTLR4编译器开发专业知识（语法分析、语义分析、IR生成、优化、虚拟机）
- **构建系统**: 跨平台自动化构建脚本
- **MCP集成**: 模型上下文协议服务器配置（优先使用Serena智能代码分析）
- **开发工具**: 项目管理和环境检查工具
- **代码分析**: Serena智能符号分析和代码编辑工具

### 项目架构
```
How to implement PL in ANTLR4/
├── .claude/skills/
│   ├── compiler-expert/           # 本技能（合并编译器开发者和生态系统）
│   ├── antlr4-compiler-development/ # 优化版ANTLR4技能
│   └── technical-documentation-writing-and-refactoring/ # 技术文档技能
├── scripts/
│   ├── build.sh                   # Linux/macOS构建脚本
│   ├── build.ps1                  # Windows PowerShell脚本
│   ├── build.bat                  # Windows CMD脚本
│   └── mcp-helper.py              # MCP管理工具
├── mcp-project-config.json        # 项目MCP配置
└── ep1-ep21/                      # 21章节编译器实现
```

## 核心开发流程

### 1. 项目初始化和检查
**环境检查**:
```bash
# 使用MCP辅助工具检查环境
python scripts/mcp-helper.py check

# 查看项目信息
python scripts/mcp-helper.py list
./scripts/build.sh info
```

**环境要求**:
- Java 11+ (推荐Java 21)
- Maven 3.6+
- ANTLR4 (可选，Maven依赖已包含)

### 2. 语法分析和前端（EP1-EP10, EP19）
- **ANTLR4语法文件**: ep20/src/main/antlr4/org/teachfx/antlr4/ep20/parser/Cymbol.g4
- **AST构建**: ep20/src/main/java/org/teachfx/antlr4/ep20/pass/ast/CymbolASTBuilder.java
- **关键命令**:
  ```bash
  # 重新生成解析器代码
  mvn generate-sources -pl ep20

  # 测试语法分析
  mvn test -pl ep20 -Dtest="*ParserTest"

  # 运行编译器前端
  mvn compile exec:java -pl ep19 -Dexec.args="program.cymbol"
  ```

### 3. 语义分析和类型系统（EP11-EP17）
- **符号表实现**: ep20/src/main/java/org/teachfx/antlr4/ep20/symtab/
  - scope/ - 作用域管理（GlobalScope, LocalScope）
  - symbol/ - 符号定义（VariableSymbol, MethodSymbol, StructSymbol）
  - type/ - 类型系统（BuiltInType, StructType, ArrayType）
- **语义检查**: ep20/src/main/java/org/teachfx/antlr4/ep20/pass/sematic/
- **测试命令**:
  ```bash
  # 运行语义分析测试
  mvn test -pl ep17 -Dtest="*SemanticTest"

  # 运行类型检查测试
  mvn test -pl ep17 -Dtest="*TypeCheckTest"
  ```

### 4. 中间表示和代码生成（EP18, EP20）
- **IR生成**: ep20/src/main/java/org/teachfx/antlr4/ep20/ir/
  - ir/expr/ - 表达式IR节点
  - ir/stmt/ - 语句IR节点
  - CymbolIRBuilder.java - IR生成器
- **控制流图**: ep20/src/main/java/org/teachfx/antlr4/ep20/pass/cfg/
  - ControlFlowAnalysis.java - 控制流分析
  - CFG.java - 控制流图实现
- **虚拟机实现**: ep18/src/main/java/org/teachfx/antlr4/ep18/stackvm/
  - CymbolStackVM.java - 栈式虚拟机
  - BytecodeDefinition.java - 字节码定义
  - StackFrame.java - 栈帧管理
- **运行命令**:
  ```bash
  # 运行完整编译器
  mvn compile exec:java -pl ep20 -Dexec.args="program.cymbol"

  # 运行虚拟机
  mvn compile exec:java -pl ep18 -Dexec.mainClass="org.teachfx.antlr4.ep18.stackvm.CymbolStackVM"

  # 测试IR生成
  mvn test -pl ep20 -Dtest="*IRTest"
  ```

### 5. 高级优化（EP21）
- **数据流分析**: ep21/src/main/java/org/teachfx/antlr4/ep21/analysis/dataflow/
- **SSA转换**: ep21/src/main/java/org/teachfx/antlr4/ep21/analysis/ssa/
- **测试命令**:
  ```bash
  mvn test -pl ep21
  ```

## 项目结构导航

### 关键文件位置
- **主编译器入口**: ep20/src/main/java/org/teachfx/antlr4/ep20/Compiler.java
- **最新Cymbol语法**: ep20/src/main/antlr4/org/teachfx/antlr4/ep20/parser/Cymbol.g4
- **AST节点基类**: ep20/src/main/java/org/teachfx/antlr4/ep20/ast/ASTNode.java
- **访问者模式接口**: ep20/src/main/java/org/teachfx/antlr4/ep20/visitor/
- **类型系统基类**: ep20/src/main/java/org/teachfx/antlr4/ep20/symtab/type/Type.java
- **符号基类**: ep20/src/main/java/org/teachfx/antlr4/ep20/symtab/Symbol.java

## 测试和质量保证

### 全面测试策略
```bash
# 运行所有测试
./scripts/build.sh test

# 运行特定模块测试
./scripts/build.sh test ep20

# 生成覆盖率报告
./scripts/build.sh coverage ep20

# 清理和重新构建
./scripts/build.sh clean && ./scripts/build.sh compile
```

### 测试最佳实践
- 单元测试覆盖率 ≥85%
- 核心模块覆盖率 ≥90%
- 新功能测试覆盖率 100%
- 集成测试验证完整编译流程

## Serena智能代码分析工具

### 优先使用Serena工具
本技能优先使用Serena智能代码分析工具，提供高效的代码探索和编辑：

**代码探索**:
- `mcp__serena__find_symbol` - 按符号名搜索代码实体
- `mcp__serena__search_for_pattern` - 模式搜索代码和文档
- `mcp__serena__get_symbols_overview` - 获取文件符号概览

**代码编辑**:
- `mcp__serena__replace_symbol_body` - 替换符号体
- `mcp__serena__insert_after_symbol` - 在符号后插入代码
- `mcp__serena__insert_before_symbol` - 在符号前插入代码
- `mcp__serena__rename_symbol` - 重命名符号

**项目理解**:
- `mcp__serena__list_dir` - 列出目录内容
- `mcp__serena__find_file` - 查找文件
- `mcp__serena__read_memory` - 读取项目记忆
- `mcp__serena__write_memory` - 写入项目记忆

**智能思考**:
- `mcp__serena__think_about_collected_information` - 分析收集的信息
- `mcp__serena__think_about_task_adherence` - 检查任务执行情况
- `mcp__serena__think_about_whether_you_are_done` - 判断任务完成状态

### 使用流程
1. **探索阶段**: 使用 `mcp__serena__get_symbols_overview` 理解文件结构
2. **搜索阶段**: 使用 `mcp__serena__find_symbol` 或 `mcp__serena__search_for_pattern` 定位代码
3. **分析阶段**: 使用 `mcp__serena__think_about_collected_information` 分析信息
4. **编辑阶段**: 使用 `mcp__serena__replace_symbol_body` 等工具进行编辑
5. **验证阶段**: 使用 `mcp__serena__think_about_task_adherence` 确保任务一致性

## 调试和故障排除

### 调试工具链
1. **语法调试**: ANTLR4 TestRig
   ```bash
   java -cp "antlr-4.13.2-complete.jar:target/classes" \
     org.antlr.v4.gui.TestRig Cymbol file -tokens program.cymbol
   ```

2. **AST可视化**: 使用ep19/ep20中的可视化工具
3. **符号表调试**: 启用DEBUG级别日志
4. **IR调试**: 输出中间代码进行验证
5. **VM调试**: 使用虚拟机调试模式

### 常见问题解决
- **语法冲突**: 使用ANTLR4诊断工具分析
- **类型检查失败**: 检查符号表和类型转换规则
- **IR生成错误**: 验证AST到IR的转换逻辑
- **VM执行错误**: 检查字节码生成和栈帧管理

## MCP集成和工具

### MCP服务器配置
项目提供优化的MCP服务器配置：
- **antlr4-compiler**: 文件系统访问
- **project-structure**: 项目结构管理
- **maven-build**: Maven构建工具
- **grammar-parser**: 语法树解析
- **memory**: 记忆功能
- **context7**: 上下文管理
- **serena**: 智能代码分析（优先使用）

### MCP工具使用
```bash
# 启动特定MCP服务器
python scripts/mcp-helper.py start antlr4-compiler

# 查看MCP配置
python scripts/mcp-helper.py check
```

## 跨平台开发支持

### 支持的平台和脚本
- **Linux/macOS**: scripts/build.sh
- **Windows PowerShell**: scripts/build.ps1
- **Windows CMD**: scripts/build.bat
- **跨平台Python**: scripts/mcp-helper.py

### 统一命令接口
所有脚本支持相同的命令集：
```bash
generate [module]    # 生成ANTLR4源文件
compile [module]     # 编译项目
test [module]        # 运行测试
run [module] [file]  # 运行编译器
vm [module] [file]   # 运行虚拟机
clean [module]       # 清理项目
coverage [module]    # 生成覆盖率报告
info                 # 显示项目信息
```

## Cymbol语言特性

### 支持的类型
- 基本类型: int, float, bool, string, void, object
- 数组类型: int[], float[10]
- 结构体类型: struct Point { int x; int y; }
- 类型别名: typedef int MyInt;

### 控制结构
- 条件语句: if, if-else
- 循环语句: while, break, continue
- 函数调用和返回

### 操作符
- 算术: +, -, *, /, %
- 比较: ==, !=, >, >=, <, <=
- 逻辑: &&, ||, !
- 赋值: =
- 数组访问: []
- 结构体访问: .
- 类型转换: (type)expr

## 最佳实践

### 开发工作流
1. **测试驱动开发**: 为新功能先编写测试
2. **渐进实现**: 按照EP1-EP21顺序理解实现
3. **文档同步**: 修改代码时更新 .qoder/repowiki/ 文档
4. **向后兼容**: 确保新功能不影响现有模块

### 代码质量
1. **代码风格**: 遵循Java编码规范
2. **错误处理**: 提供有意义的错误信息和位置
3. **日志调试**: 使用Log4j2进行分级日志记录
4. **性能优化**: 关注关键路径的性能

### 项目维护
1. **依赖管理**: 定期更新依赖版本
2. **安全扫描**: 检查安全漏洞
3. **文档维护**: 保持文档与代码同步
4. **测试维护**: 确保测试的稳定性

## 技能使用指南

### 何时使用此技能
- 开始新的编译器开发项目
- 需要ANTLR4编译器专业技术指导
- 遇到编译器开发技术问题
- 需要完整的开发生态系统支持（构建、测试、部署）
- 要求优先使用Serena智能代码分析工具
- 需要跨平台开发解决方案
- 需要MCP服务器配置帮助

### 技能使用流程
1. 评估项目需求和当前状态
2. 选择合适的开发阶段和工具
3. 优先使用Serena工具进行代码探索和分析
4. 使用对应的脚本和配置进行构建和测试
5. 参考技能文档解决技术问题
6. 验证结果和优化工作流程

### 与其他技能的配合
- **antlr4-compiler-development**: 深度ANTLR4语法开发指导
- **technical-documentation-writing-and-refactoring**: 技术文档编写和重构
- **testing-framework-specification**: 测试框架规范

---
*技能版本: 1.0.0 | 最后更新: 2025-12-22*
*项目: How to implement PL in ANTLR4*
*技能ID: compiler-expert*
*合并来源: compiler-development + compiler-ecosystem*