# EP17 - Cymbol 调用图分析与 ANTLR4 升级

## 概述

EP17 实现了一个专注于**调用图分析**的编译器组件，并升级到 **ANTLR 4.13.2** 版本。该模块是编译器前端分析的重要工具，能够生成函数调用关系的可视化图形，为后续的编译器优化和静态分析提供基础。

## 🎉 最近更新 (2025年12月)

### 重大升级完成
- ✅ **ANTLR4 升级** - 从 4.11.0 成功升级到 4.13.2
- ✅ **代码生成更新** - 更新 Cymbol 语法解析器和词法分析器的生成代码
- ✅ **兼容性改进** - 适配新版 Java 编译要求，添加 this-escape 警告抑制
- ✅ **代码质量提升** - 改进导入语句顺序，增强代码可读性

### 核心功能验证
- ✅ **调用图生成** - 成功生成 DOT 格式的函数调用图
- ✅ **语法解析** - Cymbol.g4 语法文件正确解析
- ✅ **AST 遍历** - CallGraphVisitor 正确遍历抽象语法树

## 当前功能特性

### ✅ 已实现功能

#### 1. 调用图分析系统
- **CallGraphVisitor** - 遍历 AST 并收集函数调用关系
- **DOT 格式输出** - 生成标准 DOT 格式的调用图文件
- **函数调用跟踪** - 记录函数间的调用关系

```cymbol
// 示例 Cymbol 程序
int add(int a, int b) {
    return a + b;
}

int multiply(int x, int y) {
    return x * y;
}

int calculate(int a, int b) {
    int sum = add(a, b);
    int product = multiply(a, b);
    return sum + product;
}

void main() {
    int result = calculate(5, 7);
    print(result);
}
```

对应的调用图会显示：
- `main` → `calculate`
- `calculate` → `add`
- `calculate` → `multiply`

#### 2. ANTLR4 升级特性
- **版本升级** - ANTLR 4.11.0 → 4.13.2
- **生成的解析器** - 更新所有生成的 Java 代码
- **兼容性** - 适配新版 Java 编译器
- **警告处理** - 添加 this-escape 警告抑制注解

#### 3. 编译器前端架构
- **词法分析** (CymbolLexer) - 将源代码转换为 token 流
- **语法分析** (CymbolParser) - 构建抽象语法树 (AST)
- **调用图收集** (CallGraphVisitor) - 分析函数调用关系
- **DOT 生成** - 输出可视化调用图

## 编译器架构

### 核心组件

#### 语法文件 (Cymbol.g4)
- 定义了 Cymbol 语言的基础语法
- 支持函数定义、调用、变量声明等

#### 解析器系统
- `CymbolLexer` - 词法分析器
- `CymbolParser` - 语法分析器
- `CymbolBaseListener` - 基础监听器
- `CymbolBaseVisitor` - 基础访问者

#### 调用图系统
- `CallGraphVisitor` - 主要的调用图分析器
- 支持遍历 AST 并提取调用关系
- 生成 DOT 格式的调用图文件

## 使用示例

### 构建项目
```bash
cd ep17
mvn clean compile
```

### 运行调用图生成器
```bash
# 编译示例程序
mvn compile exec:java -Dexec.mainClass="org.teachfx.antlr4.ep17.Compiler"
```

### 查看生成的调用图
```bash
# 查看生成的 DOT 文件
cat src/main/resources/call.dot
```

使用 Graphviz 可视化：
```bash
dot -Tpng src/main/resources/call.dot -o call_graph.png
```

### 示例输出 (DOT 格式)
```
digraph CallGraph {
    main -> calculate
    calculate -> add
    calculate -> multiply
}
```

## 测试和验证

### 测试范围
- **语法解析测试** - 验证 ANTLR4 生成代码的正确性
- **调用图生成测试** - 验证调用关系提取的准确性
- **DOT 输出测试** - 验证生成的 DOT 文件格式正确性

### 运行测试
```bash
# 运行所有测试
mvn test

# 运行特定测试类
mvn test -Dtest=CallGraphTest
```

## 开发指南

### 添加新的调用分析功能
1. 修改 `CallGraphVisitor.java`
2. 在相应方法中记录新的调用关系
3. 更新 DOT 生成逻辑
4. 添加测试用例

### 调试技巧
- 使用 `-Dlog4j.configurationFile=path/to/log4j2-debug.xml` 启用调试日志
- 检查生成的 DOT 文件格式
- 使用 Graphviz 工具可视化调用图

## 性能特性

### 性能指标
- **解析速度** - 快速解析中等大小程序（<1000 行）
- **内存占用** - 低内存占用的 AST 遍历
- **DOT 生成** - 高效的图形格式输出

## 技术细节

### ANTLR4 升级变更
- **生成代码注释更新** - 反映新版本信息
- **导入语句优化** - 改进代码组织
- **方法修正** - `getGrammarFileName` 返回正确文件名
- **代码可读性** - 添加必要的括号提升可读性

### 架构设计
- **访问者模式** - 使用 ANTLR4 的访问者模式遍历 AST
- **文件输出** - 支持多种输出格式（当前为 DOT）
- **可扩展性** - 易于添加新的分析功能

## 未来改进方向

### 短期目标
1. **增强调用分析** - 添加更详细的调用上下文信息
2. **多格式输出** - 支持 JSON、XML 等格式
3. **循环检测** - 识别递归调用和循环调用
4. **性能优化** - 优化大型程序的解析性能

### 长期目标
1. **调用频率统计** - 记录调用次数和性能数据
2. **调用链分析** - 生成完整的调用链
3. **静态单例分析** - 结合 SSA 形式的优化分析
4. **IDE 集成** - 与开发工具集成

## 依赖和版本

### 主要依赖
- **ANTLR4** - 4.13.2
- **Java** - 18+
- **Maven** - 3.8+

### 生成的代码
- `CymbolLexer.java` - 词法分析器
- `CymbolParser.java` - 语法分析器
- `CymbolBaseListener.java` - 监听器基类
- `CymbolBaseVisitor.java` - 访问者基类

## 贡献指南

欢迎贡献调用图分析相关的改进：

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/new-analysis`)
3. 添加测试用例
4. 确保所有测试通过
5. 提交 Pull Request

## 许可证

本项目遵循项目根目录的许可证条款。
