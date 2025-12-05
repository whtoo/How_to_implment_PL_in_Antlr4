# Cymbol编译器项目文档

## 项目概述

Cymbol编译器项目是一个基于ANTLR4的教学级编译器实现，旨在演示现代编译器的核心概念和实现技术。项目通过多个演进阶段（EP1-EP21），逐步实现从简单解释器到完整编译器的功能。

## 项目结构

```
.
├── ep1/                 # 词法分析器实现
├── ep2/                 # 语法分析器实现
├── ep3/                 # AST构建
├── ep4/                 # 访问者模式应用
├── ep5/                 # 符号表实现
├── ep6/                 # 作用域管理
├── ep7/                 # 类型系统
├── ep8/                 # 解释器实现
├── ep9/                 # 函数调用支持
├── ep10/                # 内存管理
├── ep11/                # 错误处理
├── ep12/                # AST优化
├── ep13/                # 中间表示(IR)
├── ep14/                # 控制流图(CFG)
├── ep15/                # 数据流分析
├── ep16/                # 寄存器分配
├── ep17/                # 指令选择
├── ep18/                # 栈式虚拟机
├── ep19/                # 代码优化
├── ep20/                # 完整编译器
│   ├── docs/            # EP20详细文档
│   ├── src/             # 源代码
│   └── tests/           # 测试代码
├── ep21/                # JIT编译
├── docs/                # 项目整体文档
├── scripts/             # 构建和部署脚本
└── logs/                # 日志文件
```

## 各阶段功能概览

### EP1-EP10: 解释器阶段
- 词法分析和语法分析基础
- AST构建和访问者模式
- 符号表和作用域管理
- 类型系统和语义分析
- 简单解释器实现

### EP11-EP20: 编译器阶段
- 中间表示(IR)设计和实现
- 控制流图(CFG)构建
- 数据流分析和优化
- 代码生成和虚拟机
- 完整编译器架构

### EP21: 高级特性
- JIT编译技术
- 高级优化算法
- 性能调优

## EP20编译器特性

EP20代表了项目的完整编译器实现，具有以下核心特性：

### 架构特点
- 完整的多阶段编译架构
- 模块化设计，易于扩展
- 清晰的接口定义和职责分离

### 核心功能
- 词法分析和语法分析（基于ANTLR4）
- 抽象语法树(AST)构建和遍历
- 符号表管理和作用域解析
- 静态类型检查和类型推导
- 地址化中间表示(IR)生成
- 控制流图(CFG)构建和分析
- 代码优化（跳转优化、活性分析）
- 栈式虚拟机代码生成

### 技术亮点
- 完整的测试驱动开发(TDD)实践
- 丰富的调试和可视化支持
- 详细的文档和架构说明
- 高质量的代码和工程实践

## 文档结构

### 项目级文档
- [README.md](README.md) - 项目总体介绍
- [开发指南.md](开发指南.md) - 开发环境搭建和开发流程
- [架构设计.md](架构设计.md) - 整体架构设计
- [测试策略.md](测试策略.md) - 测试框架和策略

### 课程改进建议
- **[编译器构造课程改进建议.md](./编译器构造课程改进建议.md)** - 完整的项目改进建议和优化方案
- **[course/syllabus.md](./course/syllabus.md)** - 详细的课程大纲和教学安排

#### 实验指导材料 (course/labs/)
- **[lab-template.md](./course/labs/lab-template.md)** - 实验指导模板
- **[lab1-antlr4-basics.md](./course/labs/lab1-antlr4-basics.md)** - 实验1：ANTLR4基础语法

### EP20详细文档
EP20阶段包含详细的文档说明：
- [编译流程序列图](../ep20/docs/compilation-sequence.md) - 各阶段编译流程详解
- [EP20改进总结](../ep20/docs/ep20-improvements-summary.md) - 技术成就和创新点
- [模块交互图](../ep20/docs/module-interaction.md) - 各模块间交互关系
- [项目架构](../ep20/docs/project-architecture.md) - 详细架构设计
- [TDD实施步骤](../ep20/docs/tdd_implementation_steps.md) - 测试驱动开发实施指南
- [TDD改进任务](../ep20/docs/tdd_improvement_tasks.md) - 测试改进任务规划
- [TDD测试用例指南](../ep20/docs/tdd_test_case_guide.md) - 测试用例编写规范

## 快速开始

### 环境要求
- Java 11 或更高版本
- Maven 3.6 或更高版本
- ANTLR4 运行时

### 构建项目
```bash
# 克隆项目
git clone <repository-url>

# 进入EP20目录
cd ep20

# 编译项目
mvn compile

# 运行测试
mvn test

# 打包项目
mvn package
```

### 运行编译器
```bash
# 编译Cymbol源文件
java -jar target/ep20-compiler.jar <source-file.cymbol>

# 运行生成的虚拟机代码
java -jar target/ep20-vm.jar <output-file.vm>
```

## 贡献指南

欢迎对项目进行贡献！请遵循以下步骤：

1. Fork项目
2. 创建功能分支
3. 提交更改
4. 发起Pull Request

请确保：
- 遵循项目编码规范
- 添加相应的测试用例
- 更新相关文档
- 通过所有测试

## 许可证

本项目采用MIT许可证，详情请见[LICENSE](LICENSE)文件。

## 联系方式

如有问题或建议，请通过以下方式联系：
- 提交Issue
- 发送邮件至项目维护者