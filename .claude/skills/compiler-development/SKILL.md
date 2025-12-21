---
name: compiler-development
description: 为ANTLR4编译器项目提供专业开发指导，包括语法分析、语义分析、IR生成、代码优化和虚拟机实现。当用户需要编译器开发、ANTLR4语法设计、类型系统实现、中间代码生成或编译器优化相关帮助时使用此技能。
allowed-tools: Read, Grep, Glob, mcp__cclsp__find_definition, mcp__cclsp__find_references, mcp__cclsp__get_diagnostics, mcp__cclsp__restart_server, mcp__context7__resolve-library-id, mcp__context7__get-library-docs
---

# 编译器开发技能

## 项目概述

**How to implement PL in ANTLR4** 是一个21章节的渐进式编译器教学项目，实现完整的Cymbol语言编译器。项目分为三个主要阶段：
- **EP1-EP10**: 基础阶段（词法语法分析、AST构建、解释器）
- **EP11-EP20**: 编译器阶段（类型系统、语义分析、IR、CFG、代码生成）
- **EP21**: 高级优化（数据流分析、SSA形式）

## 核心开发流程

### 1. 语法分析和前端（EP1-EP10, EP19）
- **ANTLR4语法文件**: ep20/src/main/antlr4/org/teachfx/antlr4/ep20/parser/Cymbol.g4
- **AST构建**: ep20/src/main/java/org/teachfx/antlr4/ep20/pass/ast/CymbolASTBuilder.java
- **关键命令**:
  bash
  # 重新生成解析器代码
  mvn generate-sources -pl ep20

  # 测试语法分析
  mvn test -pl ep20 -Dtest="*ParserTest"

  # 运行编译器前端
  mvn compile exec:java -pl ep19 -Dexec.args="program.cymbol"
  

### 2. 语义分析和类型系统（EP11-EP17）
- **符号表实现**: ep20/src/main/java/org/teachfx/antlr4/ep20/symtab/
  - scope/ - 作用域管理（GlobalScope, LocalScope）
  - symbol/ - 符号定义（VariableSymbol, MethodSymbol, StructSymbol）
  - type/ - 类型系统（BuiltInType, StructType, ArrayType）
- **语义检查**: ep20/src/main/java/org/teachfx/antlr4/ep20/pass/sematic/
- **测试命令**:
  bash
  # 运行语义分析测试
  mvn test -pl ep17 -Dtest="*SemanticTest"

  # 运行类型检查测试
  mvn test -pl ep17 -Dtest="*TypeCheckTest"
  

### 3. 中间表示和代码生成（EP18, EP20）
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
  bash
  # 运行完整编译器
  mvn compile exec:java -pl ep20 -Dexec.args="program.cymbol"

  # 运行虚拟机
  mvn compile exec:java -pl ep18 -Dexec.mainClass="org.teachfx.antlr4.ep18.stackvm.CymbolStackVM"

  # 测试IR生成
  mvn test -pl ep20 -Dtest="*IRTest"
  

### 4. 高级优化（EP21）
- **数据流分析**: ep21/src/main/java/org/teachfx/antlr4/ep21/analysis/dataflow/
- **SSA转换**: ep21/src/main/java/org/teachfx/antlr4/ep21/analysis/ssa/
- **测试命令**:
  bash
  mvn test -pl ep21
  

## 项目结构导航

### 关键文件位置
- **主编译器入口**: ep20/src/main/java/org/teachfx/antlr4/ep20/Compiler.java
- **最新Cymbol语法**: ep20/src/main/antlr4/org/teachfx/antlr4/ep20/parser/Cymbol.g4
- **AST节点基类**: ep20/src/main/java/org/teachfx/antlr4/ep20/ast/ASTNode.java
- **访问者模式接口**: ep20/src/main/java/org/teachfx/antlr4/ep20/visitor/
- **类型系统基类**: ep20/src/main/java/org/teachfx/antlr4/ep20/symtab/type/Type.java
- **符号基类**: ep20/src/main/java/org/teachfx/antlr4/ep20/symtab/Symbol.java

### 测试策略
- **单元测试覆盖率**: ≥85%
- **核心模块覆盖率**: ≥90%
- **新功能覆盖率**: 100%
- **运行测试**:
  bash
  # 运行所有测试
  mvn test

  # 运行特定模块测试
  mvn test -pl ep20

  # 运行特定测试类
  mvn test -pl ep20 -Dtest="ArraysTest"

  # 生成覆盖率报告
  mvn jacoco:report
  

## 开发工作流

### 添加新AST节点
1. 在 ep20/src/main/java/org/teachfx/antlr4/ep20/ast/ 创建节点类
2. 扩展 ASTNode 或相关基类（ExprNode, StmtNode, DeclNode）
3. 实现访问者模式方法
4. 在 Cymbol.g4 中添加语法规则
5. 更新 CymbolASTBuilder 处理新节点
6. 创建对应的测试类

### 修改语法文件
1. 编辑 Cymbol.g4 文件
2. 运行 mvn generate-sources -pl ep20 重新生成解析器
3. 更新 CymbolASTBuilder 和访问者实现
4. 运行相关测试验证: mvn test -pl ep20 -Dtest="*ParserTest"

### 添加新类型
1. 在 ep20/src/main/java/org/teachfx/antlr4/ep20/symtab/type/ 创建类型类
2. 扩展 Type 基类
3. 更新类型检查器 TypeChecker
4. 添加类型转换和提升规则
5. 创建类型测试

### 调试技巧
1. **语法调试**: 使用ANTLR4 TestRig工具
   bash
   java -cp "antlr-4.13.2-complete.jar:target/classes" org.antlr.v4.gui.TestRig Cymbol file -tokens program.cymbol
   
2. **AST可视化**: 检查ep19/ep20中的可视化工具
3. **符号表调试**: 启用DEBUG级别日志
   bash
   -Dlog4j.configurationFile=log4j2-debug.xml
   
4. **IR调试**: 输出中间代码进行验证
5. **VM调试**: 使用虚拟机调试模式

## 多平台脚本使用

项目提供跨平台脚本在 scripts/ 目录：

bash
# Linux/macOS
./scripts/run.sh compile ep20
./scripts/run.sh test ep20
./scripts/run.sh run ep20 "program.cymbol"

# Windows PowerShell
.\scripts\run.ps1 compile ep20
.\scripts\run.ps1 test ep20

# Windows CMD
scripts\run.bat compile ep20
scripts\run.bat test ep20

# Python脚本（跨平台）
python scripts/run.py compile ep20


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

1. **测试驱动开发**: 为新功能先编写测试
2. **渐进实现**: 按照EP1-EP21顺序理解实现
3. **文档同步**: 修改代码时更新 .qoder/repowiki/ 文档
4. **向后兼容**: 确保新功能不影响现有模块
5. **日志调试**: 使用Log4j2进行分级日志记录
6. **代码风格**: 遵循项目Java 21+代码规范
7. **错误处理**: 提供有意义的错误信息和位置

## 常见问题解决

### 语法冲突
**症状**: ANTLR4报告歧义或冲突
**解决**:
1. 使用 -diagnostics 选项分析冲突
2. 重构语法规则，避免歧义
3. 使用谓词或语义谓词消除冲突

### 类型检查失败
**症状**: 类型不匹配错误
**解决**:
1. 检查符号表是否正确填充
2. 验证类型转换规则
3. 检查隐式类型提升

### IR生成错误
**症状**: 生成的IR不正确或崩溃
**解决**:
1. 验证AST到IR的转换规则
2. 检查临时变量管理
3. 验证基本块划分

### VM执行错误
**症状**: 虚拟机崩溃或错误输出
**解决**:
1. 使用VM调试模式
2. 检查字节码生成
3. 验证栈帧管理

## 扩展项目

### 添加新语言特性
1. 在 Cymbol.g4 中添加语法规则
2. 实现AST节点
3. 更新语义分析和类型检查
4. 实现IR生成
5. 更新代码生成
6. 添加测试用例

### 性能优化
1. 分析性能瓶颈
2. 实现优化pass
3. 使用数据流分析
4. 实现SSA形式
5. 进行死代码消除

---

*技能版本: 1.0.0 | 最后更新: 2025-12-14*
*项目: How to implement PL in ANTLR4*
*技能ID: compiler-development*