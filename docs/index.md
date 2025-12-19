# 文档索引

> 快速导航项目中的所有文档

---

## 🎯 核心文档

| 文档 | 描述 | 优先级 |
|------|------|--------|
| [README](README.md) | 文档首页和导航 | ⭐⭐⭐ |
| [项目概览](project-overview.md) | 项目背景和现状分析 | ⭐⭐⭐ |
| [项目优化方案](project-optimization-plan.md) | 完整的优化方案 | ⭐⭐⭐ |

---

## 📂 按分类浏览

### 🚀 快速开始
- [环境搭建](development/setup.md)
- [构建指南](development/build-guide.md)
- [第一个程序](tutorials/ep01-lexer.md)
- [快速参考卡](tutorials/cheatsheet.md)

### 🏗️ 架构设计
- [模块结构](architecture/module-structure.md)
- [编译器流水线](architecture/compiler-pipeline.md)
- [依赖关系图](architecture/dependency-graph.md)
- [数据流图](architecture/dataflow.md)

### 🎨 设计文档
- [符号表设计](design/symbol-table.md)
- [类型系统设计](design/type-system.md)
- [IR设计](design/ir-design.md)
- [CFG设计](design/cfg-design.md)
- [优化策略](design/optimization.md)
- [AST设计](design/ast-design.md)

### 📘 API文档
- [符号表API](api/common-symtab/)
  - [Symbol类](api/common-symtab/symbol.md)
  - [Scope接口](api/common-symtab/scope.md)
- [前端API](api/compiler-frontend/)
  - [词法分析器](api/compiler-frontend/lexer.md)
  - [语法分析器](api/compiler-frontend/parser.md)
  - [语义分析](api/compiler-frontend/semantic.md)
- [IR API](api/compiler-ir/)
  - [IR节点](api/compiler-ir/ir-nodes.md)
  - [控制流图](api/compiler-ir/cfg.md)
- [虚拟机API](api/vm/)
  - [指令集](api/vm/instruction-set.md)
  - [运行时](api/vm/runtime.md)

### 🛠️ 开发指南
- [编码规范](development/coding-standards.md)
- [测试指南](development/testing-guide.md)
- [迁移指南](development/migration-guide.md)
- [调试指南](development/debugging.md)
- [贡献指南](development/contributing.md)

### 📚 教程
- [第1集：词法分析](tutorials/ep01-lexer.md)
- [第2集：语法分析](tutorials/ep02-parser.md)
- [第3集：抽象语法树](tutorials/ep03-ast.md)
- [第4集：访问者模式](tutorials/ep04-visitor.md)
- [第5集：符号表](tutorials/ep05-symbol-table.md)
- ...（更多教程）
- [第21集：优化](tutorials/ep21-optimization.md)

### 🔧 故障排除
- [常见问题](troubleshooting/common-issues.md)
- [FAQ](troubleshooting/faq.md)
- [语法分析错误](troubleshooting/parsing-errors.md)
- [类型错误](troubleshooting/type-errors.md)
- [构建错误](troubleshooting/build-errors.md)

### 📖 参考资料
- [ANTLR4参考](reference/antlr4-reference.md)
- [Java参考](reference/java-reference.md)
- [编译器理论](reference/compiler-theory.md)
- [参考书目](reference/bibliography.md)

### 📜 历史文档
- [技术笔记](wiki/technical-notes/)
- [实现细节](wiki/implementation-details/)
- [研究论文](wiki/research-papers/)
- [历史文档](wiki/legacy-notes/)

---

## 🔍 搜索指南

### 按关键词搜索

**代码相关**
- Symbol, Scope → [符号表设计](design/symbol-table.md)
- AST, 访问者 → [AST设计](design/ast-design.md)
- IR, CFG → [IR设计](design/ir-design.md)
- 优化 → [优化策略](design/optimization.md)

**开发相关**
- 构建 → [构建指南](development/build-guide.md)
- 测试 → [测试指南](development/testing-guide.md)
- 调试 → [调试指南](development/debugging.md)
- 贡献 → [贡献指南](development/contributing.md)

**学习相关**
- 入门 → [第一个程序](tutorials/ep01-lexer.md)
- 词法分析 → [第1集](tutorials/ep01-lexer.md)
- 语法分析 → [第2集](tutorials/ep02-parser.md)
- AST → [第3集](tutorials/ep03-ast.md)

### 按模块搜索

**ep17 - 符号表**
- 文档：[符号表设计](design/symbol-table.md)
- API：[符号表API](api/common-symtab/)
- 教程：[第5集](tutorials/ep05-symbol-table.md)

**ep18 - 虚拟机**
- 文档：[虚拟机架构](architecture/vm-architecture.md)
- API：[虚拟机API](api/vm/)
- 教程：[第18集](tutorials/ep18-vm.md)

**ep19 - 前端**
- 文档：[前端架构](architecture/frontend-architecture.md)
- API：[前端API](api/compiler-frontend/)
- 教程：[第19集](tutorials/ep19-frontend.md)

**ep20 - 编译器**
- 文档：[编译器设计](architecture/compiler-design.md)
- API：[IR API](api/compiler-ir/)
- 教程：[第20集](tutorials/ep20-compiler.md)

**ep21 - 优化器**
- 文档：[优化策略](design/optimization.md)
- API：[优化API](api/compiler-ir/optimizer.md)
- 教程：[第21集](tutorials/ep21-optimization.md)

---

## 📊 文档统计

| 分类 | 文档数量 | 目标数量 | 状态 |
|------|----------|----------|------|
| 核心文档 | 3 | 3 | ✅ 完成 |
| 架构文档 | 0 | 25 | ⏳ 待建 |
| 设计文档 | 15 | 30 | ⏳ 部分 |
| API文档 | 0 | 20 | ⏳ 待建 |
| 开发指南 | 5 | 15 | ⏳ 部分 |
| 教程 | 10 | 25 | ⏳ 部分 |
| 故障排除 | 0 | 10 | ⏳ 待建 |
| 参考资料 | 0 | 20 | ⏳ 待建 |
| 历史文档 | 0 | 232 | ⏳ 待迁移 |
| **总计** | **33** | **405** | **⏳ 进行中** |

---

## 🎯 常用链接

### 项目资源
- 项目首页：[/](../README.md)
- 项目概览：[project-overview.md](project-overview.md)
- 优化方案：[project-optimization-plan.md](project-optimization-plan.md)

### 开发资源
- 环境搭建：[development/setup.md](development/setup.md)
- 构建指南：[development/build-guide.md](development/build-guide.md)
- 编码规范：[development/coding-standards.md](development/coding-standards.md)

### 学习资源
- 21集教程：[tutorials/](tutorials/)
- 快速参考：[tutorials/cheatsheet.md](tutorials/cheatsheet.md)
- 常见问题：[troubleshooting/faq.md](troubleshooting/faq.md)

### 外部资源
- ANTLR官网：https://www.antlr.org/
- Java文档：https://docs.oracle.com/javase/specs/
- GitHub仓库：https://github.com/your-org/antlr4-cymbol

---

## 📝 文档更新日志

| 日期 | 版本 | 更新内容 |
|------|------|----------|
| 2025-12-19 | 1.0 | 初始版本，包含核心文档 |

---

*最后更新：2025-12-19*
