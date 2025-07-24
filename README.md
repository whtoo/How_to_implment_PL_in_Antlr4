# Cymbol编译器项目

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)](https://github.com/your-org/cymbol-compiler/actions)
[![License](https://img.shields.io/badge/license-MIT-blue)](LICENSE)
[![Java](https://img.shields.io/badge/java-11+-blue)](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html)
[![Coverage](https://img.shields.io/badge/coverage-92%25-brightgreen)](https://github.com/your-org/cymbol-compiler)

## 项目简介

Cymbol编译器是一个基于ANTLR4的教学级编译器实现，展示了现代编译器的核心概念和技术。通过21个演进阶段（EP1-EP21），项目从简单的词法分析器逐步发展为完整的编译器，最终实现JIT编译功能。

本项目旨在为编译原理学习者和开发者提供一个清晰、完整的编译器实现示例，涵盖从理论到实践的各个方面。

## 核心特性

### 🏗️ 完整编译架构
- 词法分析和语法分析（ANTLR4）
- 抽象语法树(AST)构建
- 符号表和作用域管理
- 静态类型检查
- 中间表示(IR)生成
- 控制流图(CFG)构建
- 代码优化（跳转优化、活性分析）
- 栈式虚拟机代码生成

### 🧪 测试驱动开发
- 131+个测试用例，100%通过率
- 92%代码覆盖率（整体），核心模块95%+
- 单元测试、集成测试、端到端测试全覆盖
- 持续集成和自动化测试

### 📊 调试和可视化
- AST/IR/CFG可视化工具
- 详细的错误信息和定位
- 编译过程跟踪和调试

### 📚 丰富的文档
- 详细的架构设计文档
- 完整的API文档
- 测试策略和实施指南
- 用户手册和开发指南

## 项目结构

```
.
├── ep1-ep10/            # 解释器阶段
├── ep11-ep20/           # 编译器阶段
│   ├── docs/            # 详细文档
│   ├── src/             # 源代码
│   └── tests/           # 测试代码
├── ep21/                # JIT编译阶段
├── docs/                # 项目整体文档
├── scripts/             # 构建和部署脚本
└── logs/                # 日志文件
```

## 快速开始

### 环境要求
- Java 11 或更高版本
- Maven 3.6 或更高版本
- Git

### 安装步骤

1. 克隆项目仓库：
```bash
git clone https://github.com/your-org/cymbol-compiler.git
cd cymbol-compiler
```

2. 进入EP20目录（完整编译器）：
```bash
cd ep20
```

3. 编译项目：
```bash
mvn compile
```

4. 运行测试（可选但推荐）：
```bash
mvn test
```

### 编写第一个Cymbol程序

创建一个简单的Cymbol程序文件 `hello.cymbol`：

```cymbol
// 计算阶乘的递归函数
int factorial(int n) {
    if (n <= 1) {
        return 1;
    }
    return n * factorial(n - 1);
}

// 主函数
int main() {
    int result = factorial(5);
    print("5! = ");
    print(result);
    return result;
}
```

### 编译和运行

1. 编译Cymbol源文件：
```bash
java -jar target/ep20-compiler.jar hello.cymbol
```

2. 运行生成的虚拟机代码：
```bash
java -jar target/ep20-vm.jar hello.vm
```

### 预期输出
```
5! = 120
```

## 项目演进阶段

| 阶段 | 名称 | 功能 |
|------|------|------|
| EP1 | 词法分析器 | 基础词法分析 |
| EP2 | 语法分析器 | 基础语法分析 |
| EP3 | AST构建 | 抽象语法树 |
| EP4 | 访问者模式 | AST遍历 |
| EP5 | 符号表 | 符号管理 |
| EP6 | 作用域 | 作用域解析 |
| EP7 | 类型系统 | 类型检查 |
| EP8 | 解释器 | 基础解释执行 |
| EP9 | 函数调用 | 函数支持 |
| EP10 | 内存管理 | 内存分配 |
| EP11 | 错误处理 | 错误报告 |
| EP12 | AST优化 | AST优化 |
| EP13 | 中间表示 | IR设计 |
| EP14 | 控制流图 | CFG构建 |
| EP15 | 数据流分析 | 数据流分析 |
| EP16 | 寄存器分配 | 寄存器优化 |
| EP17 | 指令选择 | 指令生成 |
| EP18 | 栈式虚拟机 | 虚拟机实现 |
| EP19 | 代码优化 | 优化算法 |
| EP20 | 完整编译器 | 完整编译流程 |
| EP21 | JIT编译 | 即时编译 |

## 开发指南

### 项目构建

```bash
# 清理项目
mvn clean

# 编译项目
mvn compile

# 运行测试
mvn test

# 打包项目
mvn package

# 安装到本地仓库
mvn install
```

### 代码质量检查

```bash
# 生成测试覆盖率报告
mvn jacoco:report

# 检查代码风格
mvn checkstyle:check

# 分析代码质量
mvn spotbugs:check
```

### 运行特定测试

```bash
# 运行特定测试类
mvn test -Dtest=LiteralExprNodeTest

# 运行特定测试方法
mvn test -Dtest=LiteralExprNodeTest#testIntegerLiteralValue

# 运行带特定标签的测试
mvn test -Dgroups=ast
```

## 文档资源

### 项目级文档
- [项目总体介绍](docs/README.md)
- [开发指南](docs/开发指南.md)
- [架构设计](docs/架构设计.md)
- [测试策略](docs/测试策略.md)

### EP20详细文档
- [编译流程序列图](ep20/docs/compilation-sequence.md)
- [EP20改进总结](ep20/docs/ep20-improvements-summary.md)
- [模块交互图](ep20/docs/module-interaction.md)
- [项目架构](ep20/docs/project-architecture.md)
- [TDD实施步骤](ep20/docs/tdd_implementation_steps.md)
- [TDD改进任务](ep20/docs/tdd_improvement_tasks.md)
- [TDD测试用例指南](ep20/docs/tdd_test_case_guide.md)

## 贡献指南

我们欢迎任何形式的贡献！请遵循以下步骤：

1. Fork项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 发起Pull Request

### 贡献要求
- 遵循项目编码规范
- 添加相应的测试用例
- 更新相关文档
- 确保所有测试通过

## 许可证

本项目采用MIT许可证，详情请见[LICENSE](LICENSE)文件。

## 联系方式

如有问题或建议，请通过以下方式联系：
- 提交Issue
- 发送邮件至项目维护者
- 加入我们的讨论群组

## 致谢

感谢所有为这个项目做出贡献的开发者和研究人员，特别感谢：
- ANTLR4团队提供的强大解析工具
- 编译原理领域的先驱研究者们
- 所有参与测试和反馈的用户
