---
name: antlr4-compiler-development  
description: 为ANTLR4编译器开发项目提供专业指导，支持21章节渐进式Cymbol语言编译器实现。包括语法分析、语义分析、IR生成、虚拟机实现和优化技术。当用户需要ANTLR4语法设计、编译器前端开发、类型系统实现、中间代码生成或编译器优化相关帮助时使用此技能。
allowed-tools: Read, Grep, Glob, Bash
---

# ANTLR4编译器开发专业技能

## 项目架构概览

**How to implement PL in ANTLR4** 是一个完整的编译器教学项目，实现从词法分析到代码优化的全流程编译器。项目采用21章节渐进式设计：

### 项目阶段划分
- **基础阶段 (EP1-EP10)**: 词法语法分析、AST构建、基础解释器
- **编译器阶段 (EP11-EP20)**: 类型系统、语义分析、中间表示、控制流图、代码生成
- **优化阶段 (EP21)**: 数据流分析、SSA形式、高级优化技术

### 核心模块结构

ep20/src/main/java/org/teachfx/antlr4/ep20/
├── ast/              # 抽象语法树节点定义
├── parser/           # ANTLR4生成的解析器代码
├── symtab/           # 符号表和类型系统
│   ├── scope/        # 作用域管理
│   ├── symbol/       # 符号定义
│   └── type/         # 类型系统
├── pass/             # 编译pass实现
│   ├── ast/          # AST构建
│   ├── sematic/      # 语义分析
│   ├── cfg/          # 控制流图
│   └── optimize/     # 优化pass
├── ir/               # 中间表示
└── Compiler.java     # 主编译器入口


## 开发工作流程

### 1. ANTLR4语法开发
当需要修改或扩展Cymbol语言语法时：

**语法文件位置**: ep20/src/main/antlr4/org/teachfx/antlr4/ep20/parser/Cymbol.g4

**开发步骤**:
1. 编辑Cymbol.g4语法文件
2. 运行 mvn generate-sources -pl ep20 重新生成解析器
3. 更新AST构建器处理新语法结构
4. 运行解析器测试: mvn test -pl ep20 -Dtest="*ParserTest"

**调试技巧**:
bash
# 使用ANTLR4 TestRig调试语法
java -cp "antlr-4.13.2-complete.jar:target/classes" \
  org.antlr.v4.gui.TestRig Cymbol file -tokens program.cymbol

# 查看语法树
java -cp "antlr-4.13.2-complete.jar:target/classes" \
  org.antlr.v4.gui.TestRig Cymbol file -gui program.cymbol


### 2. AST和访问者模式实现
当需要添加新的AST节点时：

**实现位置**: ep20/src/main/java/org/teachfx/antlr4/ep20/ast/

**标准流程**:
1. 创建继承自ASTNode的节点类
2. 实现访问者模式accept方法
3. 更新CymbolASTBuilder.java处理新节点
4. 添加对应的测试用例

**示例**: 添加新语句节点
java
public class PrintStmtNode extends StmtNode {
    private final ExprNode expression;
    
    public PrintStmtNode(ExprNode expression) {
        this.expression = expression;
    }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitPrintStmt(this);
    }
}


### 3. 符号表和类型系统开发
当需要扩展类型系统或修改符号表时：

**符号表结构**: ep20/src/main/java/org/teachfx/antlr4/ep20/symtab/

**类型系统层级**:

Type (抽象基类)
├── BuiltInType (基本类型: int, float, bool, string)
├── StructType (结构体类型)
├── ArrayType (数组类型)
└── FunctionType (函数类型)


**开发步骤**:
1. 在type/目录下创建新类型类
2. 扩展Type基类并实现必要方法
3. 更新TypeChecker添加类型检查规则
4. 添加类型转换和提升逻辑
5. 创建类型系统测试

### 4. 语义分析实现
当需要添加语义检查时：

**语义分析位置**: ep20/src/main/java/org/teachfx/antlr4/ep20/pass/sematic/

**关键组件**:
- SymbolTableBuilder - 符号表构建
- TypeChecker - 类型检查
- DeclarationChecker - 声明检查
- MethodResolver - 方法解析

**测试命令**:
bash
# 运行语义分析测试
mvn test -pl ep17 -Dtest="*SemanticTest"

# 运行类型检查测试  
mvn test -pl ep17 -Dtest="*TypeCheckTest"


### 5. IR生成和控制流分析
当需要实现中间代码生成时：

**IR结构**: ep20/src/main/java/org/teachfx/antlr4/ep20/ir/

ir/
├── expr/         # 表达式IR节点
├── stmt/         # 语句IR节点
├── CymbolIRBuilder.java  # IR生成器
└── temp/         # 临时变量管理


**控制流图**: ep20/src/main/java/org/teachfx/antlr4/ep20/pass/cfg/

**开发流程**:
1. 设计IR节点结构
2. 实现AST到IR的转换
3. 构建控制流图
4. 添加基本块划分
5. 生成目标代码

**测试命令**:
bash
# 测试IR生成
mvn test -pl ep20 -Dtest="*IRTest"

# 运行完整编译器
mvn compile exec:java -pl ep20 -Dexec.args="program.cymbol"


### 6. 虚拟机实现
当需要实现字节码解释器时：

**虚拟机位置**: ep18/src/main/java/org/teachfx/antlr4/ep18/stackvm/

**核心组件**:
- CymbolStackVM.java - 虚拟机实现
- BytecodeDefinition.java - 字节码指令定义
- StackFrame.java - 栈帧管理
- RuntimeContext.java - 运行时上下文

**运行虚拟机**:
bash
mvn compile exec:java -pl ep18 \
  -Dexec.mainClass="org.teachfx.antlr4.ep18.stackvm.CymbolStackVM" \
  -Dexec.args="program.cymbol"


### 7. 高级优化技术
当需要实现编译器优化时：

**优化实现**: ep21/src/main/java/org/teachfx/antlr4/ep21/analysis/

**优化类型**:
- dataflow/ - 数据流分析
- ssa/ - SSA形式转换
- optimize/ - 优化pass实现

**测试优化**:
bash
mvn test -pl ep21


## Cymbol语言特性参考

### 支持的数据类型
- **基本类型**: int, float, bool, string, void
- **数组类型**: int[], float[10], string[]
- **结构体**: struct Point { int x; int y; }
- **类型别名**: typedef int MyInt;

### 控制结构
- **条件语句**: if (condition) { ... } else { ... }
- **循环语句**: while (condition) { ... }, break, continue
- **函数定义**: int add(int a, int b) { return a + b; }

### 操作符支持
- **算术**: +, -, *, /, %
- **比较**: ==, !=, >, >=, <, <=
- **逻辑**: &&, ||, !
- **数组访问**: array[index]
- **结构体访问**: struct.field
- **类型转换**: (int)float_value

## 测试和质量保证

### 测试策略
1. **单元测试覆盖率**: ≥85%
2. **核心模块覆盖率**: ≥90%
3. **新功能覆盖率**: 100%

### 测试命令
bash
# 运行所有测试
mvn test

# 运行特定模块测试
mvn test -pl ep20

# 运行特定测试类
mvn test -pl ep20 -Dtest="ArraysTest"

# 生成覆盖率报告
mvn jacoco:report


### 跨平台脚本
bash
# Linux/macOS
./scripts/run.sh compile ep20
./scripts/run.sh test ep20
./scripts/run.sh run ep20 "program.cymbol"

# Windows PowerShell  
.\scripts\run.ps1 compile ep20

# Python (跨平台)
python scripts/run.py compile ep20


## 调试和故障排除

### 常见问题解决

#### 语法冲突
**症状**: ANTLR4报告歧义或移进/归约冲突
**解决步骤**:
1. 使用 -diagnostics 选项分析冲突
2. 重构语法规则，避免左递归
3. 使用语义谓词消除歧义
4. 检查优先级和结合性声明

#### 类型检查失败
**症状**: 类型不匹配或未定义类型错误
**解决步骤**:
1. 检查符号表是否正确填充作用域信息
2. 验证类型转换规则的实现
3. 检查隐式类型提升逻辑
4. 确认类型等价性判断方法

#### IR生成错误
**症状**: 生成的中间代码不正确或导致VM崩溃
**解决步骤**:
1. 验证AST到IR的转换规则
2. 检查临时变量命名和作用域
3. 验证基本块边界划分
4. 确认phi节点的插入位置

#### 虚拟机执行错误
**症状**: 虚拟机崩溃、栈溢出或错误输出
**解决步骤**:
1. 启用VM调试模式检查字节码执行
2. 验证栈帧管理逻辑
3. 检查内存分配和垃圾回收
4. 确认操作数栈操作正确性

## 扩展指南

### 添加新语言特性
1. **语法设计**: 在Cymbol.g4中添加新规则
2. **AST实现**: 创建对应节点类
3. **语义分析**: 更新类型检查器
4. **IR生成**: 实现中间代码生成
5. **代码生成**: 添加目标代码生成
6. **测试验证**: 创建完整测试用例

### 性能优化
1. **分析瓶颈**: 使用性能分析工具识别热点
2. **优化算法**: 实现更高效的数据结构
3. **内存优化**: 减少内存分配和复制
4. **并行化**: 识别可并行的计算任务
5. **缓存优化**: 实现智能缓存机制

### 新目标平台
1. **设计IR**: 定义独立于目标平台的中间表示
2. **后端接口**: 实现代码生成器接口
3. **指令选择**: 为目标平台选择最优指令
4. **寄存器分配**: 实现目标平台的寄存器分配
5. **指令调度**: 优化指令执行顺序

---

*技能版本: 2.0.0 | 最后更新: 2025-12-16*
*项目: How to implement PL in ANTLR4*
*技能ID: antlr4-compiler-development*